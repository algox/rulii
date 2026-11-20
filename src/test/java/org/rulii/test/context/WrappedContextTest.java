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
package org.rulii.test.context;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.bind.Bindings;
import org.rulii.bind.HiddenContextBindings;
import org.rulii.bind.ScopedBindings;
import org.rulii.context.RuleContext;
import org.rulii.model.condition.Condition;
import org.rulii.model.function.Function;
import org.rulii.rule.Rule;
import org.rulii.ruleflow.RuleFlow;
import org.rulii.ruleset.RuleSet;

import java.util.ArrayList;
import java.util.List;

import static org.rulii.model.action.Actions.action;
import static org.rulii.model.condition.Conditions.condition;
import static org.rulii.model.function.Functions.function;
import static org.rulii.validation.rules.Validators.binding;

/**
 * A context built over the bindings of another context ({@code RuleContext.builder().build(other.getBindings())},
 * {@code rule.run(bindings)} from within an action): the reserved {@code ruleContext} and {@code bindings} are the
 * new context's, and everything else still reaches the given bindings.
 *
 * @author Max Arulananthan
 * @since 2.3
 */
public class WrappedContextTest {

    public WrappedContextTest() {
        super();
    }

    private static final Function<String> CONTEXT_ID_BY_NAME = function((RuleContext ruleContext) -> ruleContext.getId());
    private static final Function<String> CONTEXT_ID_BY_TYPE = function((RuleContext anyName) -> anyName.getId());

    private static RuleContext first() {
        return RuleContext.builder().build(Bindings.builder().standard(x -> "first-x"));
    }

    @Test
    public void theReservedPairIsTheNewContexts() {
        RuleContext first = first();
        RuleContext second = RuleContext.builder().build(first.getBindings());

        Assertions.assertSame(second, second.getBindings().getValue("ruleContext", RuleContext.class));
        Assertions.assertSame(second.getBindings(), second.getBindings().getValue("bindings", Bindings.class));
        Assertions.assertEquals(second.getId(), CONTEXT_ID_BY_NAME.run(second));

        // The first context still finds its own.
        Assertions.assertSame(first, first.getBindings().getValue("ruleContext", RuleContext.class));
        Assertions.assertSame(first.getBindings(), first.getBindings().getValue("bindings", Bindings.class));
        Assertions.assertEquals(first.getId(), CONTEXT_ID_BY_NAME.run(first));
        Assertions.assertEquals(2, first.getBindings().getScopeSize());
    }

    @Test
    public void aParameterMatchedByTypeFindsOneContextAndOneStack() {
        RuleContext first = first();
        RuleContext second = RuleContext.builder().build(first.getBindings());
        second.getBindings().addScope("second-scope");
        second.getBindings().bind("mine", String.class, "second");
        Function<Boolean> seesMine = function((Bindings anyName) -> anyName.contains("mine"));

        Assertions.assertEquals(second.getId(), CONTEXT_ID_BY_TYPE.run(second));
        Assertions.assertTrue(seesMine.run(second));
        Assertions.assertEquals(first.getId(), CONTEXT_ID_BY_TYPE.run(first));
    }

    @Test
    public void theScopesOfTheNewContextAreSeenThroughTheReservedPair() {
        RuleContext first = first();
        RuleContext second = RuleContext.builder().build(first.getBindings());
        second.getBindings().addScope("second-scope");
        second.getBindings().bind("x", String.class, "second-x");
        second.getBindings().bind("y", String.class, "second-y");

        Function<String> byParameter = function((String x) -> x);
        Function<String> byContext = function((RuleContext ruleContext) -> ruleContext.getBindings().getValue("x", String.class));
        Function<String> byBindings = function((Bindings bindings) -> bindings.getValue("y", String.class));

        Assertions.assertEquals("second-x", byParameter.run(second));
        Assertions.assertEquals("second-x", byContext.run(second));
        Assertions.assertEquals("second-y", byBindings.run(second));

        // Nothing of the new context's scope is on the first one.
        Assertions.assertEquals("first-x", first.getBindings().getValue("x", String.class));
        Assertions.assertFalse(first.getBindings().contains("y"));
    }

    @Test
    public void eachContextOfAChainResolvesToItself() {
        RuleContext first = first();
        RuleContext second = RuleContext.builder().build(first.getBindings());
        RuleContext third = RuleContext.builder().build(second.getBindings());

        Assertions.assertEquals(third.getId(), CONTEXT_ID_BY_NAME.run(third));
        Assertions.assertEquals(third.getId(), CONTEXT_ID_BY_TYPE.run(third));
        Assertions.assertEquals(second.getId(), CONTEXT_ID_BY_NAME.run(second));
        Assertions.assertEquals(first.getId(), CONTEXT_ID_BY_NAME.run(first));
        Assertions.assertEquals("first-x", third.getBindings().getValue("x", String.class));
    }

    @Test
    public void theGlobalScopeIsTheGivenBindingsUnlessTheyBelongToAContext() {
        Bindings plain = Bindings.builder().standard(x -> "first-x");
        RuleContext first = RuleContext.builder().build(plain);
        Assertions.assertSame(plain, first.getBindings().getGlobalScope().getBindings());

        RuleContext second = RuleContext.builder().build(first.getBindings());
        Bindings global = second.getBindings().getGlobalScope().getBindings();
        Assertions.assertInstanceOf(HiddenContextBindings.class, global);
        Assertions.assertSame(first.getBindings(), ((HiddenContextBindings) global).getDelegate());
    }

    @Test
    public void whatTheNewContextBindsInItsGlobalScopeLandsInTheGivenBindings() {
        RuleContext first = first();
        RuleContext second = RuleContext.builder().build(first.getBindings());

        second.getBindings().bind("z", String.class, "z-value");
        second.getBindings().setValue("x", "changed");

        Assertions.assertEquals("z-value", first.getBindings().getValue("z", String.class));
        Assertions.assertEquals("changed", first.getBindings().getValue("x", String.class));
    }

    @Test
    public void aRuleRunWithTheBindingsOfAnActionBindsWhereTheCallerReads() {
        Rule inner = Rule.builder().name("inner")
                .then(action((Bindings bindings) -> { bindings.bind("result", Integer.class, 42); }))
                .build();
        Rule outer = Rule.builder().name("outer")
                .then(action((Bindings bindings) -> { inner.run(bindings); }))
                .build();
        RuleContext context = first();

        outer.run(context);

        Assertions.assertEquals(42, context.getBindings().getValue("result", Integer.class).intValue());
        Assertions.assertEquals(2, context.getBindings().getScopeSize());
    }

    @Test
    public void aRuleSetRunWithTheBindingsOfAnActionSeesItsOwnScope() {
        List<String> seen = new ArrayList<>();
        RuleSet<?> inner = RuleSet.builder().with("inner")
                .rule(Rule.builder().name("byContext")
                        .then(action((RuleContext ruleContext) -> {
                            seen.add("ruleContext:" + ruleContext.getBindings().contains("ruleSet"));
                        }))
                        .build())
                .rule(Rule.builder().name("byBindings")
                        .then(action((Bindings bindings) -> {
                            seen.add("bindings:" + bindings.contains("ruleSet"));
                            bindings.bind("tmp", Integer.class, 1);
                        }))
                        .build())
                .build();
        Rule outer = Rule.builder().name("outer")
                .then(action((Bindings bindings) -> { inner.run(bindings); }))
                .build();
        RuleContext context = first();

        outer.run(context);

        Assertions.assertEquals(List.of("ruleContext:true", "bindings:true"), seen);
        // Bound in the rule set's scope and gone with it, as with inner.run(ruleContext).
        Assertions.assertFalse(context.getBindings().contains("tmp"));
        Assertions.assertEquals(2, context.getBindings().getScopeSize());
    }

    @Test
    public void aFlowRunOnTheBindingsOfAContextReadsItsOwnSteps() {
        RuleContext context = RuleContext.builder().build(Bindings.builder().standard(n -> 10));
        RuleFlow<Object> flow = RuleFlow.builder().name("flow")
                .apply(function((Integer n) -> n * 3), spec -> spec.as("y"))
                .apply(binding("y"), spec -> spec.as("z"))
                .apply(function((Integer n) -> n + 1), spec -> spec.as(ScopedBindings.GLOBAL_SCOPE, "kept"))
                .<Object>returning(function((Object z) -> z))
                .build();

        Object result = flow.run(context.getBindings());

        Assertions.assertEquals(30, ((Number) result).intValue());
        // A result bound to the global scope reaches the given bindings; the flow's own scope is gone.
        Assertions.assertEquals(11, context.getBindings().getValue("kept", Integer.class).intValue());
        Assertions.assertFalse(context.getBindings().contains("y"));
        Assertions.assertEquals(2, context.getBindings().getScopeSize());
    }

    @Test
    public void theReadOnlyBindingsOfAConditionCanCarryANewContext() {
        Condition other = condition((RuleContext ruleContext, Integer n) -> ruleContext.getBindings().getValue("n", Integer.class).equals(n));
        Rule rule = Rule.builder().name("rule")
                .given(condition((Bindings bindings) -> other.run(bindings)))
                .build();
        RuleContext context = RuleContext.builder().build(Bindings.builder().standard(n -> 10));

        Assertions.assertTrue(rule.run(context).status().isPass());

        RuleContext readOnly = RuleContext.builder().build(context.getBindings().asImmutable());
        Assertions.assertEquals(readOnly.getId(), CONTEXT_ID_BY_NAME.run(readOnly));
        Assertions.assertEquals(10, readOnly.getBindings().getValue("n", Integer.class).intValue());
        Assertions.assertThrows(UnsupportedOperationException.class, () -> readOnly.getBindings().bind("z", String.class, "z"));
    }

    @Test
    public void aForkOfTheNewContextResolvesToTheFork() {
        RuleContext first = first();
        RuleContext second = RuleContext.builder().build(first.getBindings());
        RuleContext fork = second.fork();

        Assertions.assertEquals(fork.getId(), CONTEXT_ID_BY_NAME.run(fork));
        Assertions.assertEquals(fork.getId(), CONTEXT_ID_BY_TYPE.run(fork));
        Assertions.assertEquals("first-x", fork.getBindings().getValue("x", String.class));

        // Values stay shared with the first context, through the second.
        fork.getBindings().setValue("x", "forked");
        Assertions.assertEquals("forked", first.getBindings().getValue("x", String.class));
    }

    @Test
    public void aForkOfTheNewContextKeepsTheGivenBindingsAsTheyWereAtTheFork() {
        RuleContext first = first();
        first.getBindings().addScope("early-scope");
        first.getBindings().bind("early", String.class, "early-value");
        RuleContext fork = RuleContext.builder().build(first.getBindings()).fork();

        // What the first context pushes and pops afterwards is its own business.
        first.getBindings().removeScope();
        first.getBindings().addScope("late-scope");
        first.getBindings().bind("late", String.class, "late-value");
        first.getBindings().bind("x", String.class, "late-x");

        Assertions.assertEquals("early-value", fork.getBindings().getValue("early", String.class));
        Assertions.assertFalse(fork.getBindings().contains("late"));
        Assertions.assertEquals("first-x", fork.getBindings().getValue("x", String.class));
    }

    @Test
    public void aForkOfTheNewContextSeesTheInnermostOfAShadowedName() {
        RuleContext first = first();
        first.getBindings().addScope("shadow-scope");
        first.getBindings().bind("x", String.class, "shadow-x");
        RuleContext fork = RuleContext.builder().build(first.getBindings()).fork();

        Assertions.assertEquals("shadow-x", fork.getBindings().getValue("x", String.class));

        // The Binding itself is shared: a value set on either side is seen by the other.
        first.getBindings().setValue("x", "changed");
        Assertions.assertEquals("changed", fork.getBindings().getValue("x", String.class));
    }

    @Test
    public void aCopyOfTheNewContextResolvesToTheCopy() {
        RuleContext first = first();
        RuleContext second = RuleContext.builder().build(first.getBindings());
        RuleContext copy = RuleContext.builder().with(second).build();

        Assertions.assertEquals(copy.getId(), CONTEXT_ID_BY_NAME.run(copy));
        Assertions.assertEquals(copy.getId(), CONTEXT_ID_BY_TYPE.run(copy));
        Assertions.assertEquals("first-x", copy.getBindings().getValue("x", String.class));
    }
}
