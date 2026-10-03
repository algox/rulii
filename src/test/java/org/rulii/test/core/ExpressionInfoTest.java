/*
 * This software is licensed under the Apache 2 license, quoted below.
 *
 * Copyright (c) 1999-2026, Algorithmx Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.rulii.test.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.bind.Bindings;
import org.rulii.context.RuleContext;
import org.rulii.model.ExpressionInfo;
import org.rulii.model.ExpressionInfo.Kind;
import org.rulii.model.action.Action;
import org.rulii.model.action.ChainedAction;
import org.rulii.model.action.ScriptAction;
import org.rulii.model.condition.Condition;
import org.rulii.model.condition.ScriptCondition;
import org.rulii.model.function.ComposeWithAfterFunction;
import org.rulii.model.function.ComposeWithBeforeFunction;
import org.rulii.model.function.Function;
import org.rulii.model.function.ScriptFunction;
import org.rulii.script.Script;
import org.rulii.script.ScriptProcessorManager;
import org.rulii.script.janino.JaninoScriptProcessorFactory;

import java.util.function.UnaryOperator;

import static org.rulii.model.action.Actions.action;
import static org.rulii.model.condition.Conditions.condition;

/**
 * Tests for {@link ExpressionInfo} and {@code getExpression()} on conditions, actions and
 * functions, plus {@link Script#getSourceText()}.
 *
 * @author Max Arulananthan
 * @since 2.1
 *
 */
public class ExpressionInfoTest {

    private static final String LANG = JaninoScriptProcessorFactory.LANGUAGE_NAME;

    private static RuleContext contextWithAge(int age) {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("age", int.class, age);
        return RuleContext.builder().with(bindings).build();
    }

    // -----------------------------------------------------------------------
    // COMPILED
    // -----------------------------------------------------------------------

    @Test
    public void lambdaConditionReportsCompiledWithMethod() {
        Condition c = condition((Integer age) -> age > 10);

        ExpressionInfo e = c.getExpression();
        Assertions.assertEquals(Kind.COMPILED, e.kind());
        Assertions.assertSame(c.getDefinition(), e.method());
        Assertions.assertNull(e.language());
        Assertions.assertNull(e.sourceText());
        Assertions.assertTrue(e.operands().isEmpty());
        Assertions.assertFalse(e.isReadable());
    }

    @Test
    public void nonIntrospectableConditionReportsCompiledWithoutMethod() {
        Condition c = new Condition() {
            @Override
            public Boolean run(RuleContext context) {
                return true;
            }
        };

        ExpressionInfo e = c.getExpression();
        Assertions.assertEquals(Kind.COMPILED, e.kind());
        Assertions.assertNull(e.method());
    }

    @Test
    public void lambdaActionAndFunctionReportCompiled() {
        Action a = action((Integer age) -> {});
        Function<Integer> f = Function.builder().with((Integer age) -> age + 1).build();

        Assertions.assertEquals(Kind.COMPILED, a.getExpression().kind());
        Assertions.assertSame(a.getDefinition(), a.getExpression().method());
        Assertions.assertEquals(Kind.COMPILED, f.getExpression().kind());
        Assertions.assertSame(f.getDefinition(), f.getExpression().method());
    }

    // -----------------------------------------------------------------------
    // SCRIPT
    // -----------------------------------------------------------------------

    @Test
    public void scriptConditionReportsScriptAndStillRuns() {
        Condition c = Condition.builder().build(Script.builder().build(LANG, "ctx.age >= 18"));

        Assertions.assertInstanceOf(ScriptCondition.class, c);
        ExpressionInfo e = c.getExpression();
        Assertions.assertEquals(Kind.SCRIPT, e.kind());
        Assertions.assertEquals(LANG, e.language());
        Assertions.assertEquals("ctx.age >= 18", e.sourceText());
        Assertions.assertNull(e.method());
        Assertions.assertTrue(e.isReadable());
        // Still a normal condition with a method definition.
        Assertions.assertNotNull(c.getDefinition());
        Assertions.assertTrue(c.isTrue(contextWithAge(20)));
        Assertions.assertFalse(c.isTrue(contextWithAge(12)));
    }

    @Test
    public void scriptActionReportsScript() {
        Action a = Action.builder().build(Script.builder().build(LANG, "int x = 1;"));

        Assertions.assertInstanceOf(ScriptAction.class, a);
        ExpressionInfo e = a.getExpression();
        Assertions.assertEquals(Kind.SCRIPT, e.kind());
        Assertions.assertEquals(LANG, e.language());
        Assertions.assertEquals("int x = 1;", e.sourceText());
        a.run(contextWithAge(1));
    }

    @Test
    public void scriptFunctionReportsScript() {
        Function<Object> f = Function.builder().build(Script.builder().build(LANG, "ctx.age + 1"));

        Assertions.assertInstanceOf(ScriptFunction.class, f);
        ExpressionInfo e = f.getExpression();
        Assertions.assertEquals(Kind.SCRIPT, e.kind());
        Assertions.assertEquals("ctx.age + 1", e.sourceText());
        Assertions.assertEquals(21, f.apply(contextWithAge(20)));
    }

    @Test
    public void sourceTextIsTheUnresolvedText() {
        ScriptProcessorManager manager = ScriptProcessorManager.getInstance();
        manager.setScriptTextResolver(text -> text.replace("${min}", "18"));

        try {
            Script<?> script = Script.builder().build(LANG, "ctx.age >= ${min}");
            Assertions.assertEquals("ctx.age >= 18", script.getScript(), "compiler sees the resolved text");
            Assertions.assertEquals("ctx.age >= ${min}", script.getSourceText(), "people see the text as written");

            Condition c = Condition.builder().build(script);
            Assertions.assertEquals("ctx.age >= ${min}", c.getExpression().sourceText());
            Assertions.assertEquals("ctx.age >= 18", c.getExpression().resolvedText(), "the explorer can show what the rule acts on");
            Assertions.assertTrue(c.isTrue(contextWithAge(20)));
        } finally {
            manager.setScriptTextResolver(UnaryOperator.identity());
        }
    }

    @Test
    public void sourceTextEqualsScriptWhenNothingWasResolved() {
        Script<?> script = Script.builder().build(LANG, "ctx.age >= 18");
        Assertions.assertEquals(script.getScript(), script.getSourceText());
        Assertions.assertEquals("ctx.age >= 18", Condition.builder().build(script).getExpression().resolvedText());
        Assertions.assertNull(ExpressionInfo.script(LANG, "ctx.age >= 18").resolvedText(), "unknown when built from text alone");
    }

    // -----------------------------------------------------------------------
    // COMPOSITE
    // -----------------------------------------------------------------------

    @Test
    public void compositeConditionsReportOperatorAndOperands() {
        Condition a = condition((Integer age) -> age > 10);
        Condition b = condition((Integer age) -> age < 65);

        ExpressionInfo and = a.and(b).getExpression();
        Assertions.assertEquals(Kind.COMPOSITE, and.kind());
        Assertions.assertEquals("&&", and.operator());
        Assertions.assertEquals(2, and.operands().size());
        Assertions.assertSame(a.getDefinition(), and.operands().get(0).method());
        Assertions.assertSame(b.getDefinition(), and.operands().get(1).method());

        Assertions.assertEquals("||", a.or(b).getExpression().operator());
        Assertions.assertEquals("^", a.xor(b).getExpression().operator());

        ExpressionInfo not = a.not().getExpression();
        Assertions.assertEquals(Kind.COMPOSITE, not.kind());
        Assertions.assertEquals("!", not.operator());
        Assertions.assertEquals(1, not.operands().size());
        Assertions.assertSame(a.getDefinition(), not.operands().get(0).method());
    }

    @Test
    public void compositeIsReadableOnlyWhenAllOperandsAre() {
        Condition s1 = Condition.builder().build(Script.builder().build(LANG, "ctx.age >= 18"));
        Condition s2 = Condition.builder().build(Script.builder().build(LANG, "ctx.age < 65"));
        Condition lambda = condition((Integer age) -> age > 10);

        Assertions.assertTrue(s1.and(s2).getExpression().isReadable());
        Assertions.assertTrue(s1.not().getExpression().isReadable());
        Assertions.assertFalse(s1.and(lambda).getExpression().isReadable());
    }

    @Test
    public void chainedActionsReportExecutionOrder() {
        Action a = action((Integer age) -> {});
        Action b = action((Integer age) -> {});

        Action andThen = a.andThen(b);
        Assertions.assertInstanceOf(ChainedAction.class, andThen);
        ExpressionInfo e1 = andThen.getExpression();
        Assertions.assertEquals("andThen", e1.operator());
        Assertions.assertSame(a.getDefinition(), e1.operands().get(0).method());
        Assertions.assertSame(b.getDefinition(), e1.operands().get(1).method());

        ExpressionInfo e2 = a.andBefore(b).getExpression();
        Assertions.assertEquals("andBefore", e2.operator());
        Assertions.assertSame(b.getDefinition(), e2.operands().get(0).method());
        Assertions.assertSame(a.getDefinition(), e2.operands().get(1).method());
    }

    @Test
    public void composedFunctionsReportExecutionOrder() {
        Function<Integer> f = Function.builder().with((Integer age) -> age + 1).build();
        Function<Integer> g = Function.builder().with((Integer age) -> age * 2).build();

        Function<Integer> andThen = f.andThen(g, "r");
        Assertions.assertInstanceOf(ComposeWithAfterFunction.class, andThen);
        Assertions.assertEquals("r", ((ComposeWithAfterFunction<?, ?>) andThen).getResultBindingName());
        ExpressionInfo e1 = andThen.getExpression();
        Assertions.assertEquals("andThen", e1.operator());
        Assertions.assertSame(f.getDefinition(), e1.operands().get(0).method());
        Assertions.assertSame(g.getDefinition(), e1.operands().get(1).method());

        Function<Integer> compose = f.compose(g, "r");
        Assertions.assertInstanceOf(ComposeWithBeforeFunction.class, compose);
        ExpressionInfo e2 = compose.getExpression();
        Assertions.assertEquals("compose", e2.operator());
        Assertions.assertSame(g.getDefinition(), e2.operands().get(0).method());
        Assertions.assertSame(f.getDefinition(), e2.operands().get(1).method());
    }

    // -----------------------------------------------------------------------
    // Record validation
    // -----------------------------------------------------------------------

    @Test
    public void recordRejectsIncompleteDefinitions() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> ExpressionInfo.script(null, "x"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> ExpressionInfo.script("java", " "));
        Assertions.assertThrows(IllegalArgumentException.class, () -> ExpressionInfo.composite("&&"));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> ExpressionInfo.composite(null, ExpressionInfo.compiled(null)));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new ExpressionInfo(null, null, null, null, null, null, null));
        // COMPILED without a method is allowed: it means "not introspectable".
        Assertions.assertNotNull(ExpressionInfo.compiled(null));
    }
}
