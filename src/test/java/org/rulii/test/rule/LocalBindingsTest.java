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
package org.rulii.test.rule;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.annotation.Given;
import org.rulii.annotation.Otherwise;
import org.rulii.annotation.PreCondition;
import org.rulii.annotation.Then;
import org.rulii.bind.Binding;
import org.rulii.bind.Bindings;
import org.rulii.bind.LocalBindings;
import org.rulii.bind.ReservedBindingNameException;
import org.rulii.bind.ReservedBindings;
import org.rulii.bind.RuleRunBindings;
import org.rulii.bind.ScopedBindings;
import org.rulii.context.RuleContext;
import org.rulii.model.UnrulyException;
import org.rulii.rule.Rule;
import org.rulii.rule.RuleExecutionStatus;
import org.rulii.rule.RuleResult;

import java.util.ArrayList;
import java.util.List;

import static org.rulii.model.action.Actions.action;
import static org.rulii.model.condition.Conditions.condition;

/**
 * {@link LocalBindings}: a class-based rule that declares the parameter gets fresh bindings per run, shared by
 * its methods, invisible to the caller.
 *
 * @author Max Arulananthan
 * @since 2.2
 */
public class LocalBindingsTest {

    public LocalBindingsTest() {
        super();
    }

    @Test
    public void conditionHandsToThen() {
        Rule rule = Rule.builder().build(Handoff.class);
        RuleContext ctx = RuleContext.builder().build(value -> 20);
        int scopes = ctx.getBindings().getScopeSize();

        RuleResult result = rule.run(ctx);

        Assertions.assertEquals(RuleExecutionStatus.PASS, result.status());
        Assertions.assertEquals(40, (Integer) ctx.getBindings().getValue("thenSaw"));
        Assertions.assertFalse(ctx.getBindings().contains("otherwiseSaw"));
        // nothing of the run is left behind
        Assertions.assertFalse(ctx.getBindings().contains(ReservedBindings.LOCAL_BINDINGS.getName()));
        Assertions.assertFalse(ctx.getBindings().contains("doubled"));
        Assertions.assertTrue(ctx.getBindings().getAllBindings(LocalBindings.class).isEmpty());
        Assertions.assertEquals(scopes, ctx.getBindings().getScopeSize());
    }

    @Test
    public void conditionHandsToOtherwise() {
        Rule rule = Rule.builder().build(Handoff.class);
        RuleContext ctx = RuleContext.builder().build(value -> 5);

        RuleResult result = rule.run(ctx);

        Assertions.assertEquals(RuleExecutionStatus.FAIL, result.status());
        Assertions.assertEquals(10, (Integer) ctx.getBindings().getValue("otherwiseSaw"));
        Assertions.assertFalse(ctx.getBindings().contains("thenSaw"));
    }

    @Test
    public void preConditionHandsToCondition() {
        Rule rule = Rule.builder().build(PreConditionHandoff.class);
        Assertions.assertEquals(RuleExecutionStatus.PASS, rule.run(RuleContext.builder().build()).status());
    }

    @Test
    public void eachRunHasItsOwn() {
        Rule rule = Rule.builder().build(Handoff.class);
        RuleContext ctx = RuleContext.builder().build(value -> 20);

        rule.run(ctx);
        ctx.getBindings().setValue("value", 4);
        ctx.getBindings().setValue("thenSaw", null);
        rule.run(ctx);

        Assertions.assertEquals(8, (Integer) ctx.getBindings().getValue("otherwiseSaw"));
        Assertions.assertNull(ctx.getBindings().getValue("thenSaw"));
    }

    @Test
    public void definitionRecordsTheRequest() {
        Assertions.assertTrue(Rule.builder().build(Handoff.class).getDefinition().usesLocalBindings());
        Assertions.assertTrue(Rule.builder().build(PreConditionHandoff.class).getDefinition().usesLocalBindings());
        Assertions.assertTrue(Rule.builder().build(ThenOnly.class).getDefinition().usesLocalBindings());
        Assertions.assertFalse(Rule.builder().build(Plain.class).getDefinition().usesLocalBindings());
        Assertions.assertFalse(Rule.builder().name("lambda").given(condition((Integer value) -> value > 1)).build().getDefinition().usesLocalBindings());
        Assertions.assertTrue(Rule.builder().build(Handoff.class).getDefinition().toString().contains("usesLocalBindings=true"));
    }

    @Test
    public void lambdaRulesCannotDeclareIt() {
        UnrulyException inCondition = Assertions.assertThrows(UnrulyException.class, () -> Rule.builder().name("lambda")
                .given(condition((LocalBindings localBindings) -> true))
                .build());
        Assertions.assertTrue(inCondition.getMessage().contains("class-based rules"));

        Assertions.assertThrows(UnrulyException.class, () -> Rule.builder().name("lambda")
                .given(condition(() -> true))
                .then(action((LocalBindings localBindings) -> {}))
                .build());
        Assertions.assertThrows(UnrulyException.class, () -> Rule.builder().name("lambda")
                .given(condition(() -> false))
                .otherwise(action((LocalBindings localBindings) -> {}))
                .build());
    }

    @Test
    public void ruleWithoutTheRequestRunsOnTheCallerContext() {
        Plain target = new Plain();
        Rule rule = Rule.builder().with(target).build();
        RuleContext ctx = RuleContext.builder().build();

        rule.run(ctx);

        Assertions.assertSame(ctx, target.context);
        Assertions.assertSame(ctx.getBindings(), target.bindings);
    }

    @Test
    public void reservedPairAndBindingsTypeStillResolveToTheCaller() {
        Observer target = new Observer();
        Rule rule = Rule.builder().with(target).build();
        RuleContext ctx = RuleContext.builder().build();

        rule.run(ctx);

        Assertions.assertSame(ctx, target.context, "ruleContext is the caller's, not the per-run child");
        Assertions.assertSame(ctx.getBindings(), target.bindings, "bindings is the caller's stack, not the view");
        Assertions.assertSame(ctx.getBindings(), target.other, "a Bindings parameter matched by type is the caller's stack");
        Assertions.assertNotNull(target.local);
        Assertions.assertEquals("set in given", target.local.getValue("mark"));
    }

    @Test
    public void recursiveRunsHaveDistinctLocalBindings() {
        Recursive target = new Recursive();
        Rule rule = Rule.builder().with(target).build();
        target.self = rule;
        RuleContext ctx = RuleContext.builder().build(depth -> 2);

        rule.run(ctx);

        // innermost run logs first; each run still sees the depth it bound locally
        Assertions.assertEquals(List.of("0:0", "1:1", "2:2"), target.log);
        Assertions.assertFalse(ctx.getBindings().contains(ReservedBindings.LOCAL_BINDINGS.getName()));
    }

    @Test
    public void runsOnAnImmutableContext() {
        Counting target = new Counting();
        Rule rule = Rule.builder().with(target).build();

        RuleResult result = rule.run(RuleContext.builder().build().asImmutable());

        Assertions.assertEquals(RuleExecutionStatus.PASS, result.status());
        Assertions.assertEquals(List.of(1), target.seen);
    }

    @Test
    public void isTrueGetsLocalBindingsToo() {
        Rule rule = Rule.builder().build(Handoff.class);
        RuleContext ctx = RuleContext.builder().build(value -> 20);

        Assertions.assertTrue(rule.isTrue(ctx));
        ctx.getBindings().setValue("value", 5);
        Assertions.assertFalse(rule.isTrue(ctx));
        Assertions.assertFalse(ctx.getBindings().contains("thenSaw"));
        Assertions.assertFalse(ctx.getBindings().contains(ReservedBindings.LOCAL_BINDINGS.getName()));
    }

    @Test
    public void nameIsReserved() {
        Assertions.assertThrows(ReservedBindingNameException.class,
                () -> Bindings.builder().standard().bind(ReservedBindings.LOCAL_BINDINGS.getName(), 1));
        Assertions.assertTrue(ReservedBindings.isReserved("localBindings"));
    }

    // --- the view ----------------------------------------------------------------------------------

    @Test
    public void viewAnswersOnlyForItsEntry() {
        ScopedBindings caller = Bindings.builder().scoped();
        caller.bind("a", 1);
        LocalBindings local = Bindings.builder().local();
        RuleRunBindings view = new RuleRunBindings(caller, local);

        Assertions.assertSame(local, view.getValue(ReservedBindings.LOCAL_BINDINGS.getName()));
        Assertions.assertSame(local, view.getLocalBindings());
        Assertions.assertSame(caller, view.getDelegate());
        Assertions.assertEquals(1, (Integer) view.getValue("a"));
        Assertions.assertTrue(view.contains("localBindings"));
        Assertions.assertTrue(view.contains("localBindings", LocalBindings.class));
        Assertions.assertTrue(view.contains("localBindings", Bindings.class), "by name, a wider type is fine");
        // by type: exactly LocalBindings, never Bindings
        Assertions.assertEquals(1, view.getBindings(LocalBindings.class).size());
        Assertions.assertEquals(1, view.getAllBindings(LocalBindings.class).size());
        Assertions.assertTrue(view.getBindings(Bindings.class).isEmpty());
        Assertions.assertTrue(view.getAllBindings(Bindings.class).isEmpty());
        Assertions.assertEquals(1, view.getAllBindings("localBindings").size());
        Assertions.assertTrue(view.getBinding("localBindings").isFinal());
    }

    @Test
    public void viewWritesThroughToTheCaller() {
        ScopedBindings caller = Bindings.builder().scoped();
        RuleRunBindings view = new RuleRunBindings(caller, Bindings.builder().local());

        view.bind("x", "y");
        Assertions.assertEquals("y", caller.getValue("x"));
        Assertions.assertSame(caller.getCurrentScope(), view.getCurrentScope());

        view.addScope("inner");
        Assertions.assertEquals(2, caller.getScopeSize());
        Assertions.assertEquals(2, view.getScopeSize());
        view.bind("z", 3);
        Assertions.assertEquals(3, (Integer) caller.getValue("z"));
        view.removeScope();
        Assertions.assertFalse(caller.contains("z"));

        ScopedBindings fork = view.fork();
        Assertions.assertFalse(fork.contains("localBindings"), "local bindings stay with the run");
        Assertions.assertEquals("y", fork.getValue("x"));
    }

    @Test
    public void viewListsItsEntryWithTheCallers() {
        ScopedBindings caller = Bindings.builder().scoped();
        caller.bind("a", 1);
        RuleRunBindings view = new RuleRunBindings(caller, Bindings.builder().local());

        Assertions.assertEquals(caller.size() + 1, view.size());
        Assertions.assertTrue(view.getNames().containsAll(List.of("a", "localBindings")));
        Assertions.assertTrue(view.asMap().containsKey("localBindings"));
        Assertions.assertEquals(caller.uniqueSize() + 1, view.uniqueSize());
        int count = 0;
        for (Binding<?> binding : view) {
            count++;
        }
        Assertions.assertEquals(caller.size() + 1, count);
        Assertions.assertTrue(view.prettyPrint("\n").contains("localBindings"));
        Assertions.assertTrue(view.toString().startsWith("RuleRunBindings("));
    }

    @Test
    public void nestedViewsInnermostWins() {
        ScopedBindings caller = Bindings.builder().scoped();
        LocalBindings outerLocal = Bindings.builder().local();
        LocalBindings innerLocal = Bindings.builder().local();
        RuleRunBindings outer = new RuleRunBindings(caller, outerLocal);
        RuleRunBindings inner = new RuleRunBindings(outer, innerLocal);

        Assertions.assertSame(innerLocal, inner.getValue("localBindings"));
        Assertions.assertSame(outerLocal, outer.getValue("localBindings"));
        Assertions.assertEquals(1, inner.getAllBindings(LocalBindings.class).size());
        Assertions.assertSame(innerLocal, inner.getAllBindings(LocalBindings.class).get(0).getValue());
        inner.bind("deep", true);
        Assertions.assertTrue(caller.contains("deep"));
    }

    @Test
    public void immutableViewStillHandsOutTheMutableLocalBindings() {
        ScopedBindings caller = Bindings.builder().scoped();
        LocalBindings local = Bindings.builder().local();
        ScopedBindings immutable = new RuleRunBindings(caller, local).asImmutable();

        LocalBindings seen = immutable.getValue("localBindings");
        Assertions.assertSame(local, seen);
        seen.bind("fromCondition", 1);
        Assertions.assertEquals(1, (Integer) local.getValue("fromCondition"));
        Assertions.assertThrows(UnsupportedOperationException.class, () -> immutable.bind("x", 1));
    }

    // --- fixtures ----------------------------------------------------------------------------------

    @org.rulii.annotation.Rule
    public static class Handoff {

        public Handoff() {
            super();
        }

        @Given
        public boolean given(Integer value, LocalBindings local) {
            local.bind("doubled", value * 2);
            return value > 10;
        }

        @Then
        public void then(LocalBindings local, Bindings bindings) {
            bindings.bind("thenSaw", local.<Integer>getValue("doubled"));
        }

        @Otherwise
        public void otherwise(LocalBindings local, Bindings bindings) {
            bindings.bind("otherwiseSaw", local.<Integer>getValue("doubled"));
        }
    }

    @org.rulii.annotation.Rule
    public static class PreConditionHandoff {

        public PreConditionHandoff() {
            super();
        }

        @PreCondition
        public boolean pre(LocalBindings local) {
            local.bind("seen", "pre");
            return true;
        }

        @Given
        public boolean given(LocalBindings local) {
            return "pre".equals(local.getValue("seen"));
        }
    }

    @org.rulii.annotation.Rule
    public static class ThenOnly {

        public ThenOnly() {
            super();
        }

        @Given
        public boolean given() {
            return true;
        }

        @Then
        public void then(LocalBindings local) {
            local.bind("x", 1);
        }
    }

    @org.rulii.annotation.Rule
    public static class Plain {

        RuleContext context;
        Bindings bindings;

        public Plain() {
            super();
        }

        @Given
        public boolean given() {
            return true;
        }

        @Then
        public void then(RuleContext ruleContext, Bindings bindings) {
            this.context = ruleContext;
            this.bindings = bindings;
        }
    }

    @org.rulii.annotation.Rule
    public static class Observer {

        RuleContext context;
        Bindings bindings;
        Bindings other;
        LocalBindings local;

        public Observer() {
            super();
        }

        @Given
        public boolean given(LocalBindings local) {
            local.bind("mark", "set in given");
            return true;
        }

        @Then
        public void then(RuleContext ruleContext, Bindings bindings, Bindings other, LocalBindings local) {
            this.context = ruleContext;
            this.bindings = bindings;
            this.other = other;
            this.local = local;
        }
    }

    @org.rulii.annotation.Rule
    public static class Recursive {

        Rule self;
        final List<String> log = new ArrayList<>();

        public Recursive() {
            super();
        }

        @Given
        public boolean given(Integer depth, LocalBindings local) {
            local.bind("mine", depth);
            return true;
        }

        @Then
        public void then(Integer depth, LocalBindings local, RuleContext ruleContext) {
            if (depth > 0) {
                ruleContext.getBindings().setValue("depth", depth - 1);
                self.run(ruleContext);
            }

            log.add(depth + ":" + local.getValue("mine"));
        }
    }

    @org.rulii.annotation.Rule
    public static class Counting {

        final List<Integer> seen = new ArrayList<>();

        public Counting() {
            super();
        }

        @Given
        public boolean given(LocalBindings local) {
            local.bind("x", 1);
            return true;
        }

        @Then
        public void then(LocalBindings local) {
            seen.add(local.getValue("x"));
        }
    }
}
