/*
 * This software is licensed under the Apache 2 license, quoted below.
 *
 * Copyright (c) 1999-2025, Algorithmx Inc.
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
package org.rulii.test.ruleset;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.bind.Binding;
import org.rulii.bind.Bindings;
import org.rulii.context.RuleContext;
import org.rulii.model.action.Actions;
import org.rulii.model.condition.Conditions;
import org.rulii.rule.Rule;
import org.rulii.ruleset.RuleSet;
import org.rulii.ruleset.RuleSetBuilder;
import org.rulii.ruleset.RuleSetConditions;
import org.rulii.ruleset.RuleSetExecutionStatus;
import org.rulii.validation.RuleViolations;
import org.rulii.validation.ValidationException;
import org.rulii.validation.ValidationExceptionThrowingRule;
import org.rulii.validation.ValidationRule;
import org.rulii.validation.rules.alphnumeric.AlphaNumericValidationRule;
import org.rulii.validation.rules.email.EmailValidationRule;
import org.rulii.validation.rules.notempty.NotEmptyValidationRule;
import org.rulii.validation.rules.notnull.NotNullValidationRule;
import org.rulii.validation.rules.numeric.NumericValidationRule;
import org.rulii.validation.rules.size.SizeValidationRule;
import org.rulii.validation.rules.uppercase.UpperCaseValidationRule;
import org.rulii.validation.rules.url.UrlValidationRule;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.rulii.model.action.Actions.action;
import static org.rulii.model.condition.Conditions.condition;
import static org.rulii.model.function.Functions.function;

/**
 * Tests for RuleSets.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class RuleSetTest {

    public RuleSetTest() {
        super();
    }

    @Test
    public void test1() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("y", String.class, "");
        bindings.bind("a", String.class, "");
        bindings.bind("b", String.class, "hello");
        bindings.bind("c", Integer.class, 20);
        bindings.bind("x", BigDecimal.class, new BigDecimal("100.00"));

        Rule rule6 = Rule.builder()
                .name("Rule6")
                .given(Conditions.TRUE())
                .then(action((Binding<Integer> c) -> c.setValue(c.getValue() + 1)))
                .build();

        RuleSet<?> rules = RuleSet.builder()
                .with("RuleSet1", "Test Rule Set")
                .rule(Rule.builder()
                        .name("Rule1")
                        .given(condition((String y) -> y.isEmpty()))
                        .then(action((Binding<Integer> c) -> c.setValue(0)))
                        .build())
                .rule(Rule.builder()
                        .name("Rule2")
                        .given(condition((String a, BigDecimal x) -> x != null))
                        .then(action((Binding<Integer> c) -> c.setValue(c.getValue() + 1)))
                        .build())
                .rule(Rule.builder()
                        .name("Rule3")
                        .given(condition((String a, String b, Integer c) -> c == 20 && "hello".equals(b)))
                        .then(action((Binding<Integer> c) -> c.setValue(c.getValue() + 1)))
                        .build())
                .rule(rule6)
                .build();

        Rule rule2 = rules.getRule("Rule2");
        Rule rule3 = rules.getRule("Rule3");
        rules.run(bindings);

        Assertions.assertNotNull(rule2);
        Assertions.assertNotNull(rule3);
        Assertions.assertEquals(2, (int) bindings.getValue("c", Integer.class));
        Assertions.assertTrue(rule3.getCondition().isTrue(a -> "", b -> "hello", c -> 20));
    }

    @Test
    public void test2() {
        RuleSet<?> rules = RuleSet.builder().with("TestRuleSet", "Sample Test RuleSet using a Class")
                .rule(Rule.builder()
                        .name("Rule1")
                        .given(condition((String y) -> y.isEmpty()))
                        .then(action((Binding<Integer> c) -> c.setValue(0)))
                        .build())
                .rule(Rule.builder()
                        .name("Rule2")
                        .given(condition((String a, BigDecimal x) -> x != null))
                        .then(action((Binding<Integer> c) -> c.setValue(c.getValue() + 1)))
                        .build())
                .rule(Rule.builder()
                        .name("Rule3")
                        .given(condition((String a, String b, Integer c) -> c == 20 && "hello".equals(b)))
                        .then(action((Binding<Integer> c) -> c.setValue(c.getValue() + 1)))
                        .build())
                .rule(Rule.builder()
                        .name("Rule6")
                        .given(Conditions.TRUE())
                        .then(action((Binding<Integer> c) -> c.setValue(c.getValue() + 1)))
                        .build())
                .build();

        Rule rule1 = rules.getRule("Rule1");
        Rule rule2 = rules.getRule("Rule2");
        Rule rule3 = rules.getRule("Rule3");
        Rule rule6 = rules.getRule("Rule6");

        Assertions.assertNotNull(rule1);
        Assertions.assertNotNull(rule2);
        Assertions.assertNotNull(rule3);
        Assertions.assertNotNull(rule6);
    }

    @Test
    public void test3() {
        RuleSet<?> rules = RuleSet.builder().with("TestRuleSet", "Sample Test RuleSet using a Class").build();
        rules.run();
        Assertions.assertEquals(rules.getName(), "TestRuleSet");
        Assertions.assertEquals(rules.getDescription(), "Sample Test RuleSet using a Class");
    }

    @Test
    public void test4() {
        Rule notNullValidationRule = Rule.builder().build(new NotNullValidationRule("value"));
        Rule notEmptyValidationRule = Rule.builder().build(new NotEmptyValidationRule("value"));
        Rule sizeValidationRule = Rule.builder().build(new SizeValidationRule("value", 1, 5));
        Rule urlValidationRule = Rule.builder().build(new UrlValidationRule("value"));
        Rule emailValidationRule = Rule.builder().build(new EmailValidationRule("value"));

        RuleSetBuilder builder = RuleSet.builder().with("TestRuleSet")
                .description("Some Description")
                .preCondition(Conditions.TRUE())
                .initializer(Actions.EMPTY_ACTION())
                .finalizer(Actions.EMPTY_ACTION())
                .rule(notNullValidationRule)
                .rule(notEmptyValidationRule)
                .rule(sizeValidationRule)
                .rule(urlValidationRule)
                .rule(emailValidationRule);

        Assertions.assertEquals(builder.getPreCondition(), Conditions.TRUE());
        Assertions.assertEquals(builder.getInitializer(), Actions.EMPTY_ACTION());
        Assertions.assertEquals(builder.getFinalizer(), Actions.EMPTY_ACTION());
        Assertions.assertEquals(builder.getRule(0), notNullValidationRule);
        Assertions.assertEquals(builder.getRule(1), notEmptyValidationRule);
        Assertions.assertEquals(builder.getRule(2), sizeValidationRule);
        Assertions.assertEquals(builder.getRule(3), urlValidationRule);
        Assertions.assertEquals(builder.getRules().get(4), emailValidationRule);
    }

    @Test
    public void test5() {
        Rule notNullValidationRule = Rule.builder().build(new NotNullValidationRule("value"));
        Rule notEmptyValidationRule = Rule.builder().build(new NotEmptyValidationRule("value"));
        Rule sizeValidationRule = Rule.builder().build(new SizeValidationRule("value", 1, 5));
        Rule urlValidationRule = Rule.builder().build(new UrlValidationRule("value"));
        Rule emailValidationRule = Rule.builder().build(new EmailValidationRule("value"));

        RuleSet<RuleSetExecutionStatus> ruleSet = RuleSet.builder().with("TestRuleSet")
                .description("Some Description")
                .preCondition(Conditions.TRUE())
                .initializer(Actions.EMPTY_ACTION())
                .finalizer(Actions.EMPTY_ACTION())
                .rule(notNullValidationRule)
                .rule(notEmptyValidationRule)
                .rule(sizeValidationRule)
                .rule(urlValidationRule)
                .rule(emailValidationRule).build();

        Assertions.assertEquals(ruleSet.getPreCondition(), Conditions.TRUE());
        Assertions.assertEquals(ruleSet.getInitializer(), Actions.EMPTY_ACTION());
        Assertions.assertEquals(ruleSet.getFinalizer(), Actions.EMPTY_ACTION());
        Assertions.assertEquals(ruleSet.getRule(0), notNullValidationRule);
        Assertions.assertEquals(ruleSet.getRule(1), notEmptyValidationRule);
        Assertions.assertEquals(ruleSet.getRule(2), sizeValidationRule);
        Assertions.assertEquals(ruleSet.getRule(3), urlValidationRule);
        Assertions.assertEquals(ruleSet.getRules().get(4), emailValidationRule);
    }

    @Test
    public void test6() {
        ValidationRule notNullValidationRule = new NotNullValidationRule("a");
        ValidationRule notEmptyValidationRule = new NotEmptyValidationRule("b");
        ValidationRule sizeValidationRule = new SizeValidationRule("c", 1, 5);
        ValidationRule urlValidationRule = new UrlValidationRule("d");
        ValidationRule emailValidationRule = new EmailValidationRule("e");

        RuleSet<?> ruleSet = RuleSet.builder().with("TestRuleSet")
                .description("Some Description")
                .inputValidator(notNullValidationRule)
                .inputValidators(notEmptyValidationRule)
                .inputValidators(sizeValidationRule)
                .inputValidators(urlValidationRule)
                .inputValidators(emailValidationRule)
                .build();

        ruleSet.run(a -> "abcd", b -> "123", c -> List.of(1, 2, 3), d -> "http://www.google.ca", e -> "test@test.com");
    }

    @Test
    public void test7() {
        RuleSet<?> ruleSet = RuleSet.builder().with("TestRuleSet")
                .description("Some Description")
                .inputValidator(new NotNullValidationRule("a"))
                .inputValidators(new NotEmptyValidationRule("b"))
                .inputValidators(new SizeValidationRule("c", 1, 5))
                .inputValidators(new UrlValidationRule("d"))
                .inputValidators(new EmailValidationRule("e"))
                .build();

        ValidationException validationException = null;

        try {
            ruleSet.run(a -> null, b -> "", c -> List.of(), d -> "google.ca", e -> "testtest.com");
        } catch (ValidationException e) {
            validationException = e;
        }

        Assertions.assertNotNull(validationException);
        Assertions.assertEquals(validationException.getViolations().size(), 5);
        Assertions.assertEquals(validationException.getViolations().getViolation(0).getErrorCode(), NotNullValidationRule.ERROR_CODE);
        Assertions.assertEquals(validationException.getViolations().getViolation(1).getErrorCode(), NotEmptyValidationRule.ERROR_CODE);
        Assertions.assertEquals(validationException.getViolations().getViolation(2).getErrorCode(), SizeValidationRule.ERROR_CODE);
        Assertions.assertEquals(validationException.getViolations().getViolation(3).getErrorCode(), UrlValidationRule.ERROR_CODE);
        Assertions.assertEquals(validationException.getViolations().getViolation(4).getErrorCode(), EmailValidationRule.ERROR_CODE);
    }

    @Test
    public void test8() {
        RuleSet<RuleSetExecutionStatus> ruleSet = RuleSet.builder().with("TestRuleSet")
                .preCondition(Conditions.FALSE())
                .inputValidator(new NotNullValidationRule("a"))
                .initializer(Actions.action((Binding<Integer> a) -> a.setValue(100)))
                .build();

        Bindings bindings = Bindings.builder().scoped();
        bindings.bind("a", 10);
        RuleSetExecutionStatus status = ruleSet.run(bindings);
        Assertions.assertFalse(status.isPreConditionCheck());
        Assertions.assertEquals((int) bindings.getValue("a"), 10);
    }

    @Test
    public void test9() {
        RuleSet<RuleSetExecutionStatus> ruleSet = RuleSet.builder().with("TestRuleSet")
                .preCondition(Conditions.TRUE())
                .inputValidator(new NotNullValidationRule("a"))
                .initializer(Actions.action((Binding<Integer> a) -> a.setValue(100)))
                .build();

        Bindings bindings = Bindings.builder().scoped();
        bindings.bind("a", 10);
        RuleSetExecutionStatus status = ruleSet.run(bindings);
        Assertions.assertTrue(status.isPreConditionCheck());
        Assertions.assertEquals((int) bindings.getValue("a"), 100);
    }

    @Test
    public void test10() {
        RuleSet<RuleSetExecutionStatus> ruleSet = RuleSet.builder().with("TestRuleSet")
                .preCondition(Conditions.TRUE())
                .inputValidator(new NotNullValidationRule("a"))
                .initializer(Actions.action((Binding<Integer> a) -> a.setValue(100)))
                .finalizer(Actions.action((Binding<Integer> a) -> a.setValue(0)))
                .build();

        Bindings bindings = Bindings.builder().scoped();
        bindings.bind("a", 10);
        RuleSetExecutionStatus status = ruleSet.run(bindings);
        Assertions.assertTrue(status.isPreConditionCheck());
        Assertions.assertEquals((int) bindings.getValue("a"), 0);
    }

    @Test
    public void test11() {
        RuleSet<RuleSetExecutionStatus> ruleSet = RuleSet.builder().with("TestRuleSet")
                .inputValidator(new NotNullValidationRule("a"))
                .inputValidator(new NotNullValidationRule("b"))
                .inputValidator(new NotNullValidationRule("violations"))
                .preCondition(Conditions.TRUE())
                .initializer(Actions.action((Binding<Integer> a) -> a.setValue(100)))
                .rule(Rule.builder().build(new NotEmptyValidationRule("b")))
                .rule(Rule.builder().build(new ValidationExceptionThrowingRule()))
                .finalizer(Actions.action((Binding<Integer> a) -> a.setValue(0)))
                .build();

        Bindings bindings = Bindings.builder().scoped();
        bindings.bind("a", 10);
        bindings.bind("b", "");
        bindings.bind("violations", new RuleViolations());

        Assertions.assertThrowsExactly(ValidationException.class, () -> {
            RuleSetExecutionStatus status = ruleSet.run(bindings);
            Assertions.assertTrue(status.isPreConditionCheck());
            Assertions.assertEquals((int) bindings.getValue("a"), 0);
        });
    }

    @Test
    public void test12() {
        RuleSet<RuleSetExecutionStatus> ruleSet = RuleSet.builder().with("TestRuleSet")
                .inputValidator(new NotNullValidationRule("a"))
                .inputValidator(new NotNullValidationRule("b"))
                .inputValidator(new NotNullValidationRule("violations"))
                .initializer(Actions.action((Binding<Integer> a) -> a.setValue(100)))
                .rule(Rule.builder().build(new NotEmptyValidationRule("b")))
                .rule(Rule.builder().build(new ValidationExceptionThrowingRule()))
                .finalizer(Actions.action((Binding<Integer> a) -> a.setValue(0)))
                .build();

        RuleSetBuilder builder = RuleSet.builder()
                .with(ruleSet)
                .rule(0, Rule.builder().build(new UrlValidationRule("d")))
                .rule(0, Rule.builder().build(new EmailValidationRule("e")));
        RuleSet<?> updatedRuleSet = builder.build();

        Assertions.assertEquals(updatedRuleSet.getRule(0).getTarget().getClass(), EmailValidationRule.class);
        Assertions.assertEquals(updatedRuleSet.getRule(1).getTarget().getClass(), UrlValidationRule.class);
        Assertions.assertEquals(updatedRuleSet.getRule(2).getTarget().getClass(), NotEmptyValidationRule.class);
        Assertions.assertEquals(updatedRuleSet.getRule(3).getTarget().getClass(), ValidationExceptionThrowingRule.class);
    }

    @Test
    public void test13() {
        RuleSet<Integer> ruleSet = RuleSet.builder().with("TestRuleSet")
                .inputValidator(new NotNullValidationRule("a"))
                .initializer(Actions.action((Binding<Integer> a) -> a.setValue(0)))
                .rule(Rule.builder()
                        .name("Rule1")
                        .then(action((Binding<Integer> a) -> a.setValue(a.getValue() + 1)))
                        .build())
                .rule(Rule.builder()
                        .name("Rule2")
                        .then(action((Binding<Integer> a) -> a.setValue(a.getValue() + 1)))
                        .build())
                .rule(Rule.builder()
                        .name("Rule3")
                        .then(action((Binding<Integer> a) -> a.setValue(a.getValue() + 1)))
                        .build())
                .rule(Rule.builder()
                        .name("Rule4")
                        .then(action((Binding<Integer> a) -> a.setValue(a.getValue() + 1)))
                        .build())
                .rule(Rule.builder()
                        .name("Rule5")
                        .then(action((Binding<Integer> a) -> a.setValue(a.getValue() + 1)))
                        .build())
                .resultExtractor(function((Integer a) -> a))
                .build();

        Integer result = ruleSet.run(a -> 100);
        Assertions.assertEquals(result, 5);
    }

    @Test
    public void test14() {
        RuleSet<RuleSetExecutionStatus> ruleSet = RuleSet.builder().with("TestRuleSet")
                .validationRule(new AlphaNumericValidationRule("a"))
                .validationRule(new NotEmptyValidationRule("a"))
                .validationRule(new NotNullValidationRule("b"))
                .validationRule(new NumericValidationRule("b"))
                .validationRule(new UpperCaseValidationRule("c"))
                .validating()
                .build();

        try {
            ruleSet.run(a -> "", b -> "bbb", c -> "ccc");
        } catch (ValidationException e) {
            Assertions.assertEquals(e.getViolations().getViolation(0).getErrorCode(), "notEmptyValidationRule.errorCode");
            Assertions.assertEquals(e.getViolations().getViolation(1).getErrorCode(), "numericValidationRule.errorCode");
            Assertions.assertEquals(e.getViolations().getViolation(2).getErrorCode(), "upperCaseValidationRule.errorCode");
        }
    }

    @Test
    public void test15() {
        RuleSet<RuleSetExecutionStatus> ruleSet = RuleSet.builder().with("TestRuleSet")
                .validationRule(new AlphaNumericValidationRule("a"))
                .validationRule(new NotEmptyValidationRule("a"))
                .validationRule(new NotNullValidationRule("b"))
                .validationRule(new NumericValidationRule("b"))
                .validationRule(new UpperCaseValidationRule("c"))
                .stopCondition(RuleSetConditions.stopOnPassCount(2))
                .validating()
                .build();
        RuleSetExecutionStatus result = ruleSet.run(a -> "aaa", b -> null, c -> "ccc");
        Assertions.assertEquals(result.size(), 2);
    }

    @Test
    public void test16() {
        RuleSet<RuleSetExecutionStatus> ruleSet = RuleSet.builder().with("TestRuleSet")
                .validationRule(new AlphaNumericValidationRule("a"))
                .validationRule(new NotEmptyValidationRule("a"))
                .validationRule(new NotNullValidationRule("b"))
                .validationRule(new NumericValidationRule("b"))
                .validationRule(new UpperCaseValidationRule("c"))
                .stopCondition(RuleSetConditions.stopWhenOnePasses())
                .validating()
                .build();
        RuleSetExecutionStatus result = ruleSet.run(a -> "aaa", b -> null, c -> "ccc");
        Assertions.assertEquals(result.size(), 1);
    }

    @Test
    public void test17() {
        RuleSet<RuleSetExecutionStatus> ruleSet = RuleSet.builder().with("TestRuleSet")
                .validationRule(new AlphaNumericValidationRule("a"))
                .validationRule(new NotEmptyValidationRule("a"))
                .validationRule(new NotNullValidationRule("b"))
                .validationRule(new NumericValidationRule("b"))
                .validationRule(new UpperCaseValidationRule("c"))
                .stopCondition(RuleSetConditions.stopOnFailCount(2))
                .validating()
                .build();
        try {
            ruleSet.run(a -> "", b -> null, c -> "ccc");
        } catch (ValidationException e) {
            Assertions.assertEquals(e.getViolations().size(), 2);
        }

    }

    @Test
    public void test18() {
        RuleSet<RuleSetExecutionStatus> ruleSet = RuleSet.builder().with("TestRuleSet")
                .validationRule(new AlphaNumericValidationRule("a"))
                .validationRule(new NotEmptyValidationRule("a"))
                .validationRule(new NotNullValidationRule("b"))
                .validationRule(new NumericValidationRule("b"))
                .validationRule(new UpperCaseValidationRule("c"))
                .stopCondition(RuleSetConditions.stopWhenOneFails())
                .validating()
                .build();
        try {
            ruleSet.run(a -> "", b -> null, c -> "ccc");
        } catch (ValidationException e) {
            Assertions.assertEquals(e.getViolations().size(), 1);
        }

    }

    @Test
    public void test19() {
        RuleSet<RuleSetExecutionStatus> ruleSet = RuleSet.builder().with("TestRuleSet")
                .validationRule(new AlphaNumericValidationRule("a"))
                .validationRule(new NotEmptyValidationRule("a"))
                .validationRule(new NotNullValidationRule("b"))
                .validationRule(new NumericValidationRule("b"))
                .validationRule(new UpperCaseValidationRule("c"))
                .stopCondition(RuleSetConditions.stopOnSkipCount(2))
                .validating()
                .build();
        RuleSetExecutionStatus result = ruleSet.run();
        Assertions.assertEquals(result.size(), 2);
    }

    @Test
    public void test20() {
        RuleSet<RuleSetExecutionStatus> ruleSet = RuleSet.builder().with("TestRuleSet")
                .validationRule(new AlphaNumericValidationRule("a"))
                .validationRule(new NotEmptyValidationRule("a"))
                .validationRule(new NotNullValidationRule("b"))
                .validationRule(new NumericValidationRule("b"))
                .validationRule(new UpperCaseValidationRule("c"))
                .stopCondition(RuleSetConditions.stopWhenOneFailsOrSkipped())
                .validating()
                .build();
        RuleSetExecutionStatus result = ruleSet.run();
        Assertions.assertEquals(result.size(), 1);
    }

    @Test
    public void test21() {
        RuleSet<RuleSetExecutionStatus> ruleSet = RuleSet.builder().with("TestRuleSet")
                .preCondition(RuleSetConditions.allMustPass())
                .validationRule(new AlphaNumericValidationRule("a"))
                .validationRule(new NotEmptyValidationRule("a"))
                .validationRule(new NotNullValidationRule("b"))
                .validationRule(new NumericValidationRule("b"))
                .validationRule(new UpperCaseValidationRule("c"))
                .validating()
                .build();
        RuleSetExecutionStatus result = ruleSet.run(a -> "abc", b -> "333", c -> "A");
        Assertions.assertEquals(result.size(), 5);
    }

    @Test
    public void test22() {
        RuleSet<RuleSetExecutionStatus> ruleSet = RuleSet.builder().with("TestRuleSet")
                .preCondition(RuleSetConditions.oneMustPass())
                .validationRule(new AlphaNumericValidationRule("a"))
                .validationRule(new NotEmptyValidationRule("a"))
                .validationRule(new NotNullValidationRule("b"))
                .validationRule(new NumericValidationRule("b"))
                .validationRule(new UpperCaseValidationRule("c"))
                .validating()
                .build();
        RuleSetExecutionStatus result = ruleSet.run(a -> "abc", b -> "333", c -> "A");
        Assertions.assertEquals(result.size(), 5);
    }

    @Test
    public void test23() {
        RuleSet<RuleSetExecutionStatus> ruleSet = RuleSet.builder().with("TestRuleSet")
                .preCondition(RuleSetConditions.noneCanPass())
                .validationRule(new AlphaNumericValidationRule("a"))
                .validationRule(new NotEmptyValidationRule("a"))
                .validationRule(new NotNullValidationRule("b"))
                .validationRule(new NumericValidationRule("b"))
                .validationRule(new UpperCaseValidationRule("c"))
                .validating()
                .build();
        RuleSetExecutionStatus result = ruleSet.run();
        Assertions.assertEquals(result.size(), 5);
    }

    @Test
    public void test24() {
        RuleSet<RuleSetExecutionStatus> ruleSet = RuleSet.builder().with("TestRuleSet")
                .preCondition(RuleSetConditions.onlyOneCanPass())
                .validationRule(new AlphaNumericValidationRule("a"))
                .validationRule(new NotEmptyValidationRule("a"))
                .validationRule(new NotNullValidationRule("b"))
                .validationRule(new NumericValidationRule("b"))
                .validationRule(new UpperCaseValidationRule("c"))
                .build();
        RuleSetExecutionStatus result = ruleSet.run(c -> "ABC");
        Assertions.assertEquals(result.size(), 5);
    }

    @Test
    public void test25() {
        RuleSet<RuleSetExecutionStatus> ruleSet = RuleSet.builder().with("TestRuleSet")
                .preCondition(RuleSetConditions.allMustPass())
                .validationRule(new AlphaNumericValidationRule("a"))
                .validationRule(new NotEmptyValidationRule("a"))
                .validationRule(new NotNullValidationRule("b"))
                .validationRule(new NumericValidationRule("b"))
                .validationRule(new UpperCaseValidationRule("c"))
                .validating()
                .build();
        RuleSetExecutionStatus result = ruleSet.run(c -> "ABC");
        Assertions.assertFalse(result.isPreConditionCheck());
        Assertions.assertEquals(result.size(), 0);
    }

    @Test
    public void test26() {
        RuleSet<RuleSetExecutionStatus> ruleSet = RuleSet.builder().with("TestRuleSet")
                .preCondition(RuleSetConditions.oneMustPass())
                .validationRule(new AlphaNumericValidationRule("a"))
                .validationRule(new NotEmptyValidationRule("a"))
                .validationRule(new NotNullValidationRule("b"))
                .validationRule(new NumericValidationRule("b"))
                .validationRule(new UpperCaseValidationRule("c"))
                .validating()
                .build();
        RuleSetExecutionStatus result = ruleSet.run();
        Assertions.assertFalse(result.isPreConditionCheck());
        Assertions.assertEquals(result.size(), 0);
    }

    @Test
    public void test27() {
        RuleSet<RuleSetExecutionStatus> ruleSet = RuleSet.builder().with("TestRuleSet")
                .preCondition(RuleSetConditions.noneCanPass())
                .validationRule(new AlphaNumericValidationRule("a"))
                .validationRule(new NotEmptyValidationRule("a"))
                .validationRule(new NotNullValidationRule("b"))
                .validationRule(new NumericValidationRule("b"))
                .validationRule(new UpperCaseValidationRule("c"))
                .validating()
                .build();
        RuleSetExecutionStatus result = ruleSet.run(c -> "ABC");
        Assertions.assertFalse(result.isPreConditionCheck());
        Assertions.assertEquals(result.size(), 0);
    }

    @Test
    public void test28() {
        RuleSet<RuleSetExecutionStatus> ruleSet = RuleSet.builder().with("TestRuleSet")
                .preCondition(RuleSetConditions.onlyOneCanPass())
                .validationRule(new AlphaNumericValidationRule("a"))
                .validationRule(new NotEmptyValidationRule("a"))
                .validationRule(new NotNullValidationRule("b"))
                .validationRule(new NumericValidationRule("b"))
                .validationRule(new UpperCaseValidationRule("c"))
                .build();
        RuleSetExecutionStatus result = ruleSet.run(a -> "abc", b -> "333", c -> "A");
        Assertions.assertFalse(result.isPreConditionCheck());
        Assertions.assertEquals(result.size(), 0);
    }

    @Test
    public void test29() throws ExecutionException, InterruptedException {
        RuleSet<RuleSetExecutionStatus> ruleSet = RuleSet.builder().with("TestRuleSet")
                .validationRule(new AlphaNumericValidationRule("a"))
                .validationRule(new NotEmptyValidationRule("a"))
                .validationRule(new NotNullValidationRule("b"))
                .validationRule(new NumericValidationRule("b"))
                .validationRule(new UpperCaseValidationRule("c"))
                .build();

        CompletableFuture<RuleSetExecutionStatus> future = ruleSet.runAsync(RuleContext.builder().build(a -> "abc", b -> "333", c -> "A"));
        RuleSetExecutionStatus result = future.get();
        Assertions.assertTrue(result.isPreConditionCheck());
        Assertions.assertEquals(result.size(), 5);
    }

    @Test
    public void test30() throws ExecutionException, InterruptedException {
        RuleSet<RuleSetExecutionStatus> ruleSet = RuleSet.builder().with("TestRuleSet")
                .validationRule(new AlphaNumericValidationRule("a"))
                .validationRule(new NotEmptyValidationRule("a"))
                .validationRule(new NotNullValidationRule("b"))
                .validationRule(new NumericValidationRule("b"))
                .validationRule(new UpperCaseValidationRule("c"))
                .build();

        CompletableFuture<RuleSetExecutionStatus> future = ruleSet.runAsync(RuleContext.builder().build(a -> "abc", b -> "333", c -> "A"));
        future.thenAccept((RuleSetExecutionStatus result) -> {
            Assertions.assertTrue(result.isPreConditionCheck());
            Assertions.assertEquals(result.size(), 5);
        });
    }

    @Test
    public void test31() throws ExecutionException, InterruptedException {
        RuleSet<RuleSetExecutionStatus> ruleSet = RuleSet.builder().with("TestRuleSet")
                .validationRule(new AlphaNumericValidationRule("a"))
                .validationRule(new NotEmptyValidationRule("a"))
                .validationRule(new NotNullValidationRule("b"))
                .validationRule(new NumericValidationRule("b"))
                .validationRule(new UpperCaseValidationRule("c"))
                .finalizer(action(() -> {
                    try {
                        Thread.sleep(250);
                    } catch (Exception e) {}
                }))
                .build();

        Assertions.assertThrowsExactly(ExecutionException.class, () -> {
                    CompletableFuture<RuleSetExecutionStatus> future = ruleSet.runAsync(RuleContext.builder().build(a -> "abc", b -> "333", c -> "A"),
                            100, TimeUnit.MILLISECONDS);
                    future.get();
                });
    }
}
