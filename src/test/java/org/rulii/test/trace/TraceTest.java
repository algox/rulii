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
import org.rulii.validation.rules.alphnumeric.AlphaNumericValidationRule;
import org.rulii.validation.rules.notempty.NotEmptyValidationRule;
import org.rulii.validation.rules.notnull.NotNullValidationRule;
import org.rulii.validation.rules.numeric.NumericValidationRule;
import org.rulii.validation.rules.uppercase.UpperCaseValidationRule;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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
                .inputValidator(new NotNullValidationRule("preConditionFlag"))
                .preCondition(Conditions.condition((Boolean preConditionFlag) -> preConditionFlag))
                .initializer(Actions.action((Binding<Integer> value) -> value.setValue(0)))
                .validationRule(new AlphaNumericValidationRule("a"))
                .validationRule(new NotEmptyValidationRule("a"))
                .validationRule(new NotNullValidationRule("b"))
                .validationRule(new NumericValidationRule("b"))
                .validationRule(new UpperCaseValidationRule("c"))
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
                .validationRule(new AlphaNumericValidationRule("a"))
                .validationRule(new NotEmptyValidationRule("a"))
                .validationRule(new NotNullValidationRule("b"))
                .validationRule(new NumericValidationRule("b"))
                .validationRule(new UpperCaseValidationRule("c"))
                .stopCondition(RuleSetConditions.stopWhenOneFailsOrSkipped())
                .validating()
                .build();

        RuleContext context = RuleContext.builder()
                .with()
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
                .validationRule(new AlphaNumericValidationRule("a"))
                .validationRule(new NotEmptyValidationRule("a"))
                .validationRule(new NotNullValidationRule("b"))
                .validationRule(new NumericValidationRule("b"))
                .validationRule(new UpperCaseValidationRule("c"))
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
}
