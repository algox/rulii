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
package org.rulii.test.trace;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.bind.*;
import org.rulii.context.RuleContext;
import org.rulii.model.UnrulyException;
import org.rulii.model.action.Action;
import org.rulii.model.action.Actions;
import org.rulii.model.condition.Condition;
import org.rulii.model.condition.Conditions;
import org.rulii.model.function.Function;
import org.rulii.rule.Rule;
import org.rulii.rule.RuleListener;
import org.rulii.rule.RuleResult;
import org.rulii.ruleset.RuleSet;
import org.rulii.ruleset.RuleSetConditions;
import org.rulii.ruleset.RuleSetExecutionStatus;
import org.rulii.ruleset.RuleSetListener;
import org.rulii.validation.RuleViolations;
import org.rulii.validation.ValidationException;
import org.rulii.validation.rules.notempty.NotEmptyValidationRule;
import org.rulii.validation.rules.notnull.NotNullValidationRule;
import org.rulii.validation.rules.numeric.NumericValidationRule;
import org.rulii.validation.rules.uppercase.UpperCaseValidationRule;

import org.rulii.trace.DefaultTracer;
import org.rulii.trace.RuliiListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.rulii.validation.rules.Validators.binding;

/**
 * Class representing TraceTest.
 *
 * This class contains a series of test methods annotated with @Test.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class TraceTest {

    public TraceTest() {
        super();
    }

    @Test
    public void test1() {
        Bindings bindings = Bindings.builder().standard();
        AtomicInteger value = new AtomicInteger(0);
        bindings.addBindingListener(new BindingListener() {
            @Override
            public void onBind(Binding<?> binding) {
                Assertions.assertEquals(binding.getName(), "bind1");
                Assertions.assertEquals(binding.getValue(), "value1");
                value.set(1);
            }
        });

        bindings.bind("bind1", "value1");
        Assertions.assertEquals(value.get(), 1);
    }

    @Test
    public void test2() {
        ScopedBindings bindings = Bindings.builder().scoped();
        AtomicInteger value = new AtomicInteger(0);
        bindings.addBindingListener(new BindingListener() {

            @Override
            public void onScopeAdd(NamedScope scope) {
                Assertions.assertEquals(scope.getName(), "scope1");
                value.incrementAndGet();
            }

            @Override
            public void onScopeRemove(NamedScope scope) {
                Assertions.assertEquals(scope.getName(), "scope1");
                value.incrementAndGet();
            }
        });


        bindings.addScope("scope1");
        bindings.bind("bind1", "value1");
        bindings.removeScope("scope1");
        Assertions.assertEquals(value.get(), 2);
    }

    @Test
    public void test3() {
        ScopedBindings bindings = Bindings.builder().scoped();
        AtomicInteger value = new AtomicInteger(0);
        bindings.addBindingListener(new BindingListener() {

            @Override
            public void onBind(Binding<?> binding) {
                Assertions.assertEquals(binding.getName(), "bind1");
                Assertions.assertEquals(binding.getValue(), "value1");
                value.incrementAndGet();
            }

            @Override
            public void onScopeAdd(NamedScope scope) {
                Assertions.assertEquals(scope.getName(), "scope1");
                value.incrementAndGet();
            }

            @Override
            public void onScopeRemove(NamedScope scope) {
                Assertions.assertEquals(scope.getName(), "scope1");
                value.incrementAndGet();
            }
        });


        bindings.addScope("scope1");
        bindings.bind("bind1", "value1");
        bindings.removeScope("scope1");
        Assertions.assertEquals(value.get(), 3);
    }

    @Test
    public void test4() {
        ScopedBindings bindings = Bindings.builder().scoped();
        AtomicInteger value = new AtomicInteger(0);

        bindings.addBindingListener(new BindingListener() {

            @Override
            public void onBind(Binding<?> binding) {
                Assertions.assertEquals(binding.getName(), "bind1");
                Assertions.assertEquals(binding.getValue(), "value1");
                value.incrementAndGet();
            }

           });
        bindings.addBindingListener(new BindingListener() {

            @Override
            public void onScopeAdd(NamedScope scope) {
                Assertions.assertEquals(scope.getName(), "scope1");
                value.incrementAndGet();
            }

            @Override
            public void onScopeRemove(NamedScope scope) {
                Assertions.assertEquals(scope.getName(), "scope1");
                value.incrementAndGet();
            }
        });

        bindings.addScope("scope1");
        bindings.bind("bind1", "value1");
        bindings.removeScope("scope1");
        Assertions.assertEquals(value.get(), 3);
    }

    @Test
    public void test5() {
        Rule rule = Rule.builder()
                .name("Rule1")
                .description("Test Rule")
                .preCondition(Conditions.condition((Boolean preConditionFlag) -> preConditionFlag))
                .given(Conditions.condition((Boolean conditionFlag) -> conditionFlag))
                .then(Actions.action((Binding<Integer> value) -> value.setValue(value.getValue() + 100)))
                .then(Actions.action((Binding<Integer> value) -> value.setValue(value.getValue() + 200)))
                .otherwise(Actions.action((Binding<Integer> value) -> value.setValue(value.getValue() + 1000)))
                .build();

        RuleContext context = RuleContext.builder()
                .with(preConditionFlag -> true, conditionFlag -> true, value -> 0)
                .build();

        AtomicInteger value = new AtomicInteger(0);
        context.getTracer().addListener(new RuleListener() {
            @Override
            public void onRuleStart(Rule rule) {
                value.incrementAndGet();
            }

            @Override
            public void onRuleEnd(Rule rule, RuleResult result) {
                value.incrementAndGet();
            }
        });

        rule.run(context);
        Assertions.assertEquals(value.get(), 2);
    }

    @Test
    public void test6() {
        Rule rule = Rule.builder()
                .name("Rule1")
                .description("Test Rule")
                .preCondition(Conditions.condition((Boolean preConditionFlag) -> preConditionFlag))
                .given(Conditions.condition((Boolean conditionFlag) -> conditionFlag))
                .then(Actions.action((Binding<Integer> counter) -> counter.setValue(counter.getValue() + 100)))
                .then(Actions.action((Binding<Integer> counter) -> counter.setValue(counter.getValue() + 200)))
                .otherwise(Actions.action((Binding<Integer> counter) -> counter.setValue(counter.getValue() + 1000)))
                .build();

        RuleContext context = RuleContext.builder()
                .with(preConditionFlag -> false, conditionFlag -> false, counter -> 0)
                .build();

        AtomicInteger value = new AtomicInteger(0);
        context.getTracer().addListener(new RuleListener() {
            @Override
            public void onRuleStart(Rule rule) {
                value.incrementAndGet();
            }

            @Override
            public void onPreConditionCheck(Rule rule, Condition condition, boolean result) {
                Assertions.assertFalse(result);
                value.incrementAndGet();
            }

            @Override
            public void onRuleEnd(Rule rule, RuleResult result) {
                value.incrementAndGet();
            }
        });

        rule.run(context);
        Assertions.assertEquals(value.get(), 3);
    }

    @Test
    public void test7() {
        Rule rule = Rule.builder()
                .name("Rule1")
                .description("Test Rule")
                .preCondition(Conditions.condition((Boolean preConditionFlag) -> preConditionFlag))
                .given(Conditions.condition((Boolean conditionFlag) -> conditionFlag))
                .then(Actions.action((Binding<Integer> value) -> value.setValue(value.getValue() + 100)))
                .then(Actions.action((Binding<Integer> value) -> value.setValue(value.getValue() + 200)))
                .otherwise(Actions.action((Binding<Integer> value) -> value.setValue(value.getValue() + 1000)))
                .build();

        RuleContext context = RuleContext.builder()
                .with(preConditionFlag -> true, conditionFlag -> false, value -> 0)
                .build();

        AtomicInteger value = new AtomicInteger(0);
        context.getTracer().addListener(new RuleListener() {
            @Override
            public void onRuleStart(Rule rule) {
                value.incrementAndGet();
            }

            @Override
            public void onPreConditionCheck(Rule rule, Condition condition, boolean result) {
                Assertions.assertTrue(result);
                value.incrementAndGet();
            }

            @Override
            public void onGiven(Rule rule, Condition condition, boolean result) {
                Assertions.assertFalse(result);
                value.incrementAndGet();
            }

            @Override
            public void onRuleEnd(Rule rule, RuleResult result) {
                value.incrementAndGet();
            }
        });

        rule.run(context);
        Assertions.assertEquals(value.get(), 4);
    }

    @Test
    public void test8() {
        Rule rule = Rule.builder()
                .name("Rule1")
                .description("Test Rule")
                .preCondition(Conditions.condition((Boolean preConditionFlag) -> preConditionFlag))
                .given(Conditions.condition((Boolean conditionFlag) -> conditionFlag))
                .then(Actions.action((Binding<Integer> counter) -> counter.setValue(counter.getValue() + 100)))
                .then(Actions.action((Binding<Integer> counter) -> counter.setValue(counter.getValue() + 200)))
                .otherwise(Actions.action((Binding<Integer> value) -> value.setValue(value.getValue() + 1000)))
                .build();

        RuleContext context = RuleContext.builder()
                .with(preConditionFlag -> true, conditionFlag -> true, counter -> 0)
                .build();

        AtomicInteger value = new AtomicInteger(0);
        context.getTracer().addListener(new RuleListener() {
            @Override
            public void onRuleStart(Rule rule) {
                value.incrementAndGet();
            }

            @Override
            public void onPreConditionCheck(Rule rule, Condition condition, boolean result) {
                Assertions.assertTrue(result);
                value.incrementAndGet();
            }

            @Override
            public void onGiven(Rule rule, Condition condition, boolean result) {
                Assertions.assertTrue(result);
                value.incrementAndGet();
            }

            @Override
            public void onThen(Rule rule, Action action) {
                value.incrementAndGet();
            }

            @Override
            public void onRuleEnd(Rule rule, RuleResult result) {
                value.incrementAndGet();
            }
        });

        rule.run(context);
        Assertions.assertEquals(value.get(), 6);
        Assertions.assertEquals((Integer) context.getBindings().getValue("counter"), 300);
    }

    @Test
    public void test9() {
        Rule rule = Rule.builder()
                .name("Rule1")
                .description("Test Rule")
                .preCondition(Conditions.condition((Boolean preConditionFlag) -> preConditionFlag))
                .given(Conditions.condition((Boolean conditionFlag) -> conditionFlag))
                .then(Actions.action((Binding<Integer> counter) -> counter.setValue(counter.getValue() + 100)))
                .then(Actions.action((Binding<Integer> counter) -> counter.setValue(counter.getValue() + 200)))
                .otherwise(Actions.action((Binding<Integer> counter) -> counter.setValue(counter.getValue() + 1000)))
                .build();

        RuleContext context = RuleContext.builder()
                .with(preConditionFlag -> true, conditionFlag -> false, counter -> 0)
                .build();

        AtomicInteger value = new AtomicInteger(0);
        context.getTracer().addListener(new RuleListener() {
            @Override
            public void onRuleStart(Rule rule) {
                value.incrementAndGet();
            }

            @Override
            public void onPreConditionCheck(Rule rule, Condition condition, boolean result) {
                Assertions.assertTrue(result);
                value.incrementAndGet();
            }

            @Override
            public void onGiven(Rule rule, Condition condition, boolean result) {
                Assertions.assertFalse(result);
                value.incrementAndGet();
            }

            @Override
            public void onThen(Rule rule, Action action) {
                value.incrementAndGet();
            }

            @Override
            public void onOtherwise(Rule rule, Action action) {
                value.incrementAndGet();
            }

            @Override
            public void onRuleEnd(Rule rule, RuleResult result) {
                value.incrementAndGet();
            }
        });

        rule.run(context);
        Assertions.assertEquals(value.get(), 5);
        Assertions.assertEquals((Integer) context.getBindings().getValue("counter"), 1000);
    }

    @Test
    public void test10() {
        Rule rule = Rule.builder()
                .name("Rule1")
                .description("Test Rule")
                .preCondition(Conditions.condition((Boolean preConditionFlag) -> preConditionFlag))
                .given(Conditions.condition((Boolean conditionFlag) -> conditionFlag))
                .then(Actions.action((Binding<Integer> counter) -> counter.setValue(counter.getValue() + 100)))
                .then(Actions.action((Binding<Integer> counter) -> counter.setValue(counter.getValue() + 200)))
                .otherwise(Actions.action((Binding<Integer> counter) -> counter.setValue(counter.getValue() + 1000)))
                .build();

        RuleContext context = RuleContext.builder()
                .with(preConditionFlag -> true, conditionFlag -> false, counter -> null)
                .build();

        AtomicInteger value = new AtomicInteger(0);
        context.getTracer().addListener(new RuleListener() {
            @Override
            public void onRuleStart(Rule rule) {
                value.incrementAndGet();
            }

            @Override
            public void onPreConditionCheck(Rule rule, Condition condition, boolean result) {
                Assertions.assertTrue(result);
                value.incrementAndGet();
            }

            @Override
            public void onGiven(Rule rule, Condition condition, boolean result) {
                Assertions.assertFalse(result);
                value.incrementAndGet();
            }

            @Override
            public void onThen(Rule rule, Action action) {
                value.incrementAndGet();
            }

            @Override
            public void onOtherwise(Rule rule, Action action) {
                value.incrementAndGet();
            }

            @Override
            public void onRuleError(Rule rule, Exception e) {
                value.incrementAndGet();
            }

            @Override
            public void onRuleEnd(Rule rule, RuleResult result) {
                value.incrementAndGet();
            }
        });

        try {
            rule.run(context);
        } catch (UnrulyException e) {
            Assertions.assertEquals(value.get(), 5);
        }
    }

    @Test
    public void test11() {
        RuleSet<RuleSetExecutionStatus> ruleSet = RuleSet.builder().with("TestRuleSet")
                .preCondition(Conditions.condition((Boolean preConditionFlag) -> preConditionFlag))
                .initializer(Actions.action((Binding<Integer> value) -> value.setValue(0)))
                .rule(NotNullValidationRule.builder(binding("a")).build())
                .rule(NotEmptyValidationRule.builder(binding("a")).build())
                .rule(NotNullValidationRule.builder(binding("b")).build())
                .rule(NumericValidationRule.builder(binding("b")).build())
                .rule(UpperCaseValidationRule.builder(binding("c")).build())
                .finalizer(Actions.action((Binding<Integer> value) -> value.setValue(100)))
                .build();

        RuleContext context = RuleContext.builder()
                .with(preConditionFlag -> true, value -> 0, a -> "abc", b -> 100, c -> "C")
                .build();

        AtomicBoolean start = new AtomicBoolean(false);
        AtomicBoolean inputCheck = new AtomicBoolean(false);;
        AtomicBoolean preConditionCheck = new AtomicBoolean(false);;
        AtomicBoolean init = new AtomicBoolean(false);;
        AtomicInteger run = new AtomicInteger(0);
        AtomicBoolean result = new AtomicBoolean(false);;
        AtomicBoolean fin = new AtomicBoolean(false);;
        AtomicBoolean end = new AtomicBoolean(false);;

        context.getTracer().addListener(new RuleSetListener() {
            @Override
            public void onRuleSetStart(RuleSet<?> ruleSet, NamedScope ruleSetScope) {
                start.set(true);
            }

            @Override
            public void onRuleSetInputCheck(RuleSet<?> ruleSet, RuleViolations violations) {
                inputCheck.set(violations.hasSevereErrors());
            }

            @Override
            public void onRuleSetPreConditionCheck(RuleSet<?> ruleSet, Condition condition, boolean result) {
                preConditionCheck.set(result);
            }

            @Override
            public void onRuleSetInitializer(RuleSet<?> ruleSet, Action initializer) {
                init.set(true);
            }

            @Override
            public void onRuleSetRuleRun(RuleSet<?> ruleSet, Rule rule, RuleResult executionResult, RuleSetExecutionStatus status) {
                run.incrementAndGet();
            }

            @Override
            public void onRuleSetResult(RuleSet<?> ruleSet, Function<?> resultExtractor, RuleSetExecutionStatus status) {
                result.set(true);
            }

            @Override
            public void onRuleSetFinalizer(RuleSet<?> ruleSet, Action finalizer) {
                fin.set(true);
            }

            @Override
            public void onRuleSetEnd(RuleSet<?> ruleSet, NamedScope ruleSetScope, RuleSetExecutionStatus status) {
                end.set(true);
            }
        });

        ruleSet.run(context);
        Assertions.assertTrue(start.get());
        Assertions.assertFalse(inputCheck.get());
        Assertions.assertTrue(preConditionCheck.get());
        Assertions.assertTrue(init.get());
        Assertions.assertEquals(run.get(), 5);
        Assertions.assertTrue(result.get());
        Assertions.assertTrue(fin.get());
        Assertions.assertTrue(end.get());
    }

    @Test
    public void test12() {
        RuleSet<RuleSetExecutionStatus> ruleSet = RuleSet.builder().with("TestRuleSet")
                .rule(NumericValidationRule.builder(binding("a")).build())
                .rule(NotEmptyValidationRule.builder(binding("a")).build())
                .rule(NotNullValidationRule.builder(binding("b")).build())
                .rule(NumericValidationRule.builder(binding("b")).build())
                .rule(UpperCaseValidationRule.builder(binding("c")).build())
                .stopCondition(RuleSetConditions.stopWhenOneFailsOrSkipped())
                .validating()
                .build();

        RuleContext context = RuleContext.builder()
                .with(a -> Boolean.TRUE, b -> "123", c -> "ABC")
                .build();

        AtomicBoolean stop = new AtomicBoolean(false);
        context.getTracer().addListener(new RuleSetListener() {
            @Override
            public void onRuleSetStop(RuleSet<?> ruleSet, Condition stopCondition, RuleSetExecutionStatus status) {
                stop.set(true);
            }
        });

        ruleSet.run(context);
        Assertions.assertTrue(stop.get());
    }

    @Test
    public void test13() {
        RuleSet<RuleSetExecutionStatus> ruleSet = RuleSet.builder().with("TestRuleSet")
                .rule(NotNullValidationRule.builder(binding("a")).build())
                .rule(NotEmptyValidationRule.builder(binding("a")).build())
                .rule(NotNullValidationRule.builder(binding("b")).build())
                .rule(NumericValidationRule.builder(binding("b")).build())
                .rule(UpperCaseValidationRule.builder(binding("c")).build())
                .stopCondition(RuleSetConditions.stopWhenOneFails())
                .validating()
                .build();

        RuleContext context = RuleContext.builder()
                .with(a -> "", b -> null, c -> "ccc")
                .build();

        AtomicBoolean error = new AtomicBoolean(false);
        context.getTracer().addListener(new RuleSetListener() {
            @Override
            public void onRuleSetStop(RuleSet<?> ruleSet, Condition stopCondition, RuleSetExecutionStatus status) {
                error.set(true);
            }
        });

        try {
            ruleSet.run(context);
        } catch (ValidationException e) {
            Assertions.assertTrue(error.get());
        }
    }

    @Test
    public void test14() {
        RuleContext context = RuleContext.builder().build();
        RuleListener listener = new RuleListener() {
        };

        context.getTracer().addListener(listener);
        Assertions.assertTrue(context.getTracer().removeListener(listener));
        Assertions.assertFalse(context.getTracer().removeListener(listener));
    }

    @Test
    public void test15() {
        RuleContext context = RuleContext.builder().build();
        RuleSetListener listener = new RuleSetListener() {
        };

        context.getTracer().addListener(listener);
        Assertions.assertTrue(context.getTracer().removeListener(listener));
        Assertions.assertFalse(context.getTracer().removeListener(listener));
    }

    @Test
    public void testRemoveListener_ruliiListener_returnsSuccessSignal() {
        RuleContext context = RuleContext.builder().build();
        RuliiListener listener = new RuliiListener() {
        };

        context.getTracer().addListener(listener);
        Assertions.assertTrue(context.getTracer().removeListener(listener));
        Assertions.assertFalse(context.getTracer().removeListener(listener));
    }

    @Test
    public void testThrowingListener_onRuleStart_doesNotAbortRuleExecution() {
        Rule rule = Rule.builder()
                .name("Rule1")
                .description("Test Rule")
                .given(Conditions.condition((Boolean conditionFlag) -> conditionFlag))
                .then(Actions.action((Binding<Integer> value) -> value.setValue(value.getValue() + 100)))
                .build();

        RuleContext context = RuleContext.builder()
                .with(conditionFlag -> true, value -> 0)
                .build();

        context.getTracer().addListener(new RuleListener() {
            @Override
            public void onRuleStart(Rule rule) {
                throw new RuntimeException("boom - a listener bug must not affect rule execution");
            }
        });

        // A broken listener must not prevent the rule from running, nor surface as a rule failure.
        RuleResult result = Assertions.assertDoesNotThrow(() -> rule.run(context));
        Assertions.assertEquals(100, (int) context.getBindings().getValue("value"));
    }

    @Test
    public void testThrowingListener_onGiven_doesNotReportRuleAsErrored() {
        Rule rule = Rule.builder()
                .name("Rule1")
                .description("Test Rule")
                .given(Conditions.condition((Boolean conditionFlag) -> conditionFlag))
                .then(Actions.action((Binding<Integer> value) -> value.setValue(value.getValue() + 100)))
                .build();

        RuleContext context = RuleContext.builder()
                .with(conditionFlag -> true, value -> 0)
                .build();

        context.getTracer().addListener(new RuleListener() {
            @Override
            public void onGiven(Rule rule, Condition condition, boolean result) {
                throw new RuntimeException("boom - a listener bug must not turn a PASS into an ERROR");
            }
        });

        Assertions.assertDoesNotThrow(() -> rule.run(context));
        Assertions.assertEquals(100, (int) context.getBindings().getValue("value"));
    }

    @Test
    public void testThrowingListener_doesNotPreventOtherListenersFromBeingNotified() {
        Rule rule = Rule.builder()
                .name("Rule1")
                .description("Test Rule")
                .given(Conditions.condition((Boolean conditionFlag) -> conditionFlag))
                .then(Actions.action((Binding<Integer> value) -> value.setValue(value.getValue() + 100)))
                .build();

        RuleContext context = RuleContext.builder()
                .with(conditionFlag -> true, value -> 0)
                .build();

        AtomicBoolean secondListenerNotified = new AtomicBoolean(false);

        context.getTracer().addListener(new RuleListener() {
            @Override
            public void onRuleEnd(Rule rule, RuleResult result) {
                throw new RuntimeException("boom - first listener throws");
            }
        });
        context.getTracer().addListener(new RuleListener() {
            @Override
            public void onRuleEnd(Rule rule, RuleResult result) {
                secondListenerNotified.set(true);
            }
        });

        rule.run(context);
        Assertions.assertTrue(secondListenerNotified.get());
    }

    @Test
    public void testConcurrentFireAndAddRemoveListener_doesNotThrow() throws Exception {
        // A single Tracer instance is propagated to derived RuleContexts and can be fired from
        // multiple threads (e.g. across async rule-flow steps) - this documents that concurrent
        // firing and concurrent add/remove of listeners don't corrupt the underlying listener set.
        DefaultTracer tracer = new DefaultTracer();
        Rule rule = Rule.builder()
                .name("Rule1")
                .description("Test Rule")
                .given(Conditions.condition(() -> true))
                .build();

        int threadCount = 8;
        int iterations = 2000;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        AtomicBoolean failed = new AtomicBoolean(false);
        List<Future<?>> futures = new ArrayList<>();

        for (int t = 0; t < threadCount; t++) {
            boolean fireThread = t % 2 == 0;
            futures.add(executor.submit(() -> {
                try {
                    for (int i = 0; i < iterations; i++) {
                        if (fireThread) {
                            tracer.fireOnRuleStart(rule);
                        } else {
                            RuleListener listener = new RuleListener() {
                            };
                            tracer.addListener(listener);
                            tracer.removeListener(listener);
                        }
                    }
                } catch (Exception e) {
                    failed.set(true);
                }
            }));
        }

        for (Future<?> future : futures) future.get();
        executor.shutdown();

        Assertions.assertFalse(failed.get());
    }
}
