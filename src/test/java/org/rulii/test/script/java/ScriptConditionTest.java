package org.rulii.test.script.java;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.bind.Bindings;
import org.rulii.context.RuleContext;
import org.rulii.model.UnrulyException;
import org.rulii.model.condition.Condition;
import org.rulii.script.BuildScriptException;
import org.rulii.script.Script;
import org.rulii.script.janino.JaninoScriptProcessorFactory;

/**
 * Tests for Condition built from a Janino Script.
 *
 * Condition.builder().build(Script) sets returnType=Boolean.class, so the
 * auto-return heuristic applies — bare expressions without "return" are supported.
 */
public class ScriptConditionTest {

    private static final String LANG = JaninoScriptProcessorFactory.LANGUAGE_NAME;

    private RuleContext contextWith(Bindings bindings) {
        return RuleContext.builder().with(bindings).build();
    }

    // -----------------------------------------------------------------------
    // Literal true / false
    // -----------------------------------------------------------------------

    @Test
    public void testConditionReturnsTrue() {
        RuleContext ctx = contextWith(Bindings.builder().standard());
        Condition condition = Condition.builder().build(Script.builder().build(LANG, "true"));
        Assertions.assertTrue(condition.isTrue(ctx));
    }

    @Test
    public void testConditionReturnsFalse() {
        RuleContext ctx = contextWith(Bindings.builder().standard());
        Condition condition = Condition.builder().build(Script.builder().build(LANG, "false"));
        Assertions.assertFalse(condition.isTrue(ctx));
    }

    // -----------------------------------------------------------------------
    // Bare expression (auto-return) — with and without trailing semicolon
    // -----------------------------------------------------------------------

    @Test
    public void testConditionReadsBinding() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("age", int.class, 20);
        RuleContext ctx = contextWith(bindings);
        Condition condition = Condition.builder().build(Script.builder().build(LANG, "ctx.age >= 18"));
        Assertions.assertTrue(condition.isTrue(ctx));
    }

    @Test
    public void testConditionBareExpressionWithSemicolon() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("age", int.class, 20);
        RuleContext ctx = contextWith(bindings);
        Condition condition = Condition.builder().build(Script.builder().build(LANG, "ctx.age >= 18;"));
        Assertions.assertTrue(condition.isTrue(ctx));
    }

    @Test
    public void testConditionFalseWithBinding() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("age", int.class, 15);
        RuleContext ctx = contextWith(bindings);
        Condition condition = Condition.builder().build(Script.builder().build(LANG, "ctx.age >= 18"));
        Assertions.assertFalse(condition.isTrue(ctx));
    }

    // -----------------------------------------------------------------------
    // Multi-statement with bare last expression
    // -----------------------------------------------------------------------

    @Test
    public void testConditionMultiStatementBareLastExpression() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("age", int.class, 20);
        RuleContext ctx = contextWith(bindings);
        // Increment age via local, then evaluate — last statement auto-wrapped in return
        Condition condition = Condition.builder().build(
                Script.builder().build(LANG, "ctx.age = ctx.age + 1;\nctx.age >= 18;"));
        Assertions.assertTrue(condition.isTrue(ctx));
    }

    @Test
    public void testConditionMultiStatementWithExplicitReturn() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("score", int.class, 85);
        RuleContext ctx = contextWith(bindings);
        Condition condition = Condition.builder().build(
                Script.builder().build(LANG,
                        "int threshold = 80;\nreturn ctx.score >= threshold;"));
        Assertions.assertTrue(condition.isTrue(ctx));
    }

    // -----------------------------------------------------------------------
    // Various binding types
    // -----------------------------------------------------------------------

    @Test
    public void testConditionWithStringEquality() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("role", String.class, "admin");
        RuleContext ctx = contextWith(bindings);
        Condition condition = Condition.builder().build(
                Script.builder().build(LANG, "ctx.role.equals(\"admin\")"));
        Assertions.assertTrue(condition.isTrue(ctx));
    }

    @Test
    public void testConditionWithStringEqualityFalse() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("role", String.class, "user");
        RuleContext ctx = contextWith(bindings);
        Condition condition = Condition.builder().build(
                Script.builder().build(LANG, "ctx.role.equals(\"admin\")"));
        Assertions.assertFalse(condition.isTrue(ctx));
    }

    @Test
    public void testConditionWithMultipleBindings() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("min",   int.class, 0);
        bindings.bind("max",   int.class, 100);
        bindings.bind("value", int.class, 50);
        RuleContext ctx = contextWith(bindings);
        Condition condition = Condition.builder().build(
                Script.builder().build(LANG, "ctx.value > ctx.min && ctx.value < ctx.max"));
        Assertions.assertTrue(condition.isTrue(ctx));
    }

    @Test
    public void testConditionWithDoubleComparison() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("balance", double.class, 150.75);
        RuleContext ctx = contextWith(bindings);
        Condition condition = Condition.builder().build(
                Script.builder().build(LANG, "ctx.balance >= 100.0"));
        Assertions.assertTrue(condition.isTrue(ctx));
    }

    @Test
    public void testConditionWithBooleanBinding() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("active", boolean.class, true);
        RuleContext ctx = contextWith(bindings);
        Condition condition = Condition.builder().build(
                Script.builder().build(LANG, "ctx.active"));
        Assertions.assertTrue(condition.isTrue(ctx));
    }

    // -----------------------------------------------------------------------
    // Repeated invocations — script is JIT-compiled once and reused
    // -----------------------------------------------------------------------

    @Test
    public void testConditionInvokedMultipleTimesConsistent() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("age", int.class, 25);
        RuleContext ctx = contextWith(bindings);
        Condition condition = Condition.builder().build(Script.builder().build(LANG, "ctx.age >= 18"));
        for (int i = 0; i < 5; i++) {
            Assertions.assertTrue(condition.isTrue(ctx));
        }
    }

    @Test
    public void testConditionResultChangesWithBindingValue() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("age", int.class, 20);
        RuleContext ctx = contextWith(bindings);
        Condition condition = Condition.builder().build(Script.builder().build(LANG, "ctx.age >= 18"));

        Assertions.assertTrue(condition.isTrue(ctx));
        bindings.setValue("age", 15);
        Assertions.assertFalse(condition.isTrue(ctx));
    }

    // -----------------------------------------------------------------------
    // Error paths
    // -----------------------------------------------------------------------

    @Test
    public void testConditionNullScriptThrows() {
        Assertions.assertThrows(Exception.class,
                () -> Condition.builder().build((Script<?>) null));
    }

    @Test
    public void testConditionSyntaxErrorThrowsOnEvaluation() {
        RuleContext ctx = contextWith(Bindings.builder().standard());
        Assertions.assertThrows(BuildScriptException.class, () -> {
            Condition condition = Condition.builder().build(Script.builder().build(LANG, "@@@ bad @@@"));
            condition.isTrue(ctx);
        });
    }

    @Test
    public void testConditionUnknownBindingThrows() {
        RuleContext ctx = contextWith(Bindings.builder().standard());
        Assertions.assertThrows(UnrulyException.class, () -> {
            // "ghost" binding does not exist in the context
            Condition condition = Condition.builder().build(
                    Script.builder().build(LANG, "ctx.ghost > 0"));
            condition.isTrue(ctx);
        });
    }
}
