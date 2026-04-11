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
package org.rulii.test.ruleset;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.bind.Binding;
import org.rulii.bind.Bindings;
import org.rulii.context.RuleContext;
import org.rulii.model.UnrulyException;
import org.rulii.model.action.Actions;
import org.rulii.model.condition.Conditions;
import org.rulii.model.function.Functions;
import org.rulii.rule.Rule;
import org.rulii.ruleset.RuleSet;
import org.rulii.ruleset.RuleSetBuilder;
import org.rulii.ruleset.RuleSetConditions;
import org.rulii.ruleset.RuleSetExecutionStatus;
import org.rulii.test.rule.ConsistentDateRule;
import org.rulii.validation.RuleViolations;
import org.rulii.validation.ValidationException;
import org.rulii.validation.ValidationExceptionThrowingRule;
import org.rulii.validation.rules.asssert.AssertEqualsValidationRule;
import org.rulii.validation.rules.asssert.AssertNotEqualsValidationRule;
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
import static org.rulii.validation.rules.Validators.binding;

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
        Rule notNullValidationRule = NotNullValidationRule.builder(binding("value")).build();
        Rule notEmptyValidationRule = NotEmptyValidationRule.builder(binding("value")).build();
        Rule sizeValidationRule = SizeValidationRule.builder(binding("value"), 1, 5).build();
        Rule urlValidationRule = UrlValidationRule.builder(binding("value")).build();
        Rule emailValidationRule = EmailValidationRule.builder(binding("value")).build();

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
        Rule notNullValidationRule = NotNullValidationRule.builder(binding("value")).build();
        Rule notEmptyValidationRule = NotEmptyValidationRule.builder(binding("value")).build();
        Rule sizeValidationRule = SizeValidationRule.builder(binding("value"), 1, 5).build();
        Rule urlValidationRule = UrlValidationRule.builder(binding("value")).build();
        Rule emailValidationRule = EmailValidationRule.builder(binding("value")).build();

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
        Rule notNullValidationRule = NotNullValidationRule.builder(binding("a")).build();
        Rule notEmptyValidationRule = NotEmptyValidationRule.builder(binding("b")).build();
        Rule sizeValidationRule = SizeValidationRule.builder(binding("c"), 1, 5).build();
        Rule urlValidationRule = UrlValidationRule.builder(binding("d")).build();
        Rule emailValidationRule = EmailValidationRule.builder(binding("e")).build();

        RuleSet<?> ruleSet = RuleSet.builder().with("TestRuleSet")
                .description("Some Description")
                .param("a", String.class)
                .param("b", String.class)
                .param("c", List.class)
                .param("d", String.class)
                .param("e", String.class)
                .rule(notNullValidationRule)
                .rule(notEmptyValidationRule)
                .rule(sizeValidationRule)
                .rule(urlValidationRule)
                .rule(emailValidationRule)
                .build();

        ruleSet.run(a -> "abcd", b -> "123", c -> List.of(1, 2, 3), d -> "http://www.google.ca", e -> "test@test.com");
    }

    @Test
    public void test7() {
        RuleSet<?> ruleSet = RuleSet.builder().with("TestRuleSet")
                .description("Some Description")
                .rule(NotNullValidationRule.builder(binding("a")).build())
                .rule(NotEmptyValidationRule.builder(binding("b")).build())
                .rule(SizeValidationRule.builder(binding("c"), 1, 5).build())
                .rule(UrlValidationRule.builder(binding("d")).build())
                .rule(EmailValidationRule.builder(binding("e")).build())
                .validating()
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
                .initializer(Actions.action((Binding<Integer> a) -> a.setValue(100)))
                .rule(NotNullValidationRule.builder(binding("a")).build())
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
                .rule(NotNullValidationRule.builder(binding("a")).build())
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
                .rule(NotNullValidationRule.builder(binding("a")).build())
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
                .preCondition(Conditions.TRUE())
                .initializer(Actions.action((Binding<Integer> a) -> a.setValue(100)))
                .rule(NotNullValidationRule.builder(binding("a")).build())
                .rule(NotNullValidationRule.builder(binding("b")).build())
                .rule(NotNullValidationRule.builder(binding("violations")).build())
                .rule(NotEmptyValidationRule.builder(binding("b")).build())
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
                .initializer(Actions.action((Binding<Integer> a) -> a.setValue(100)))
                .rule(NotNullValidationRule.builder(binding("a")).build())
                .rule(NotNullValidationRule.builder(binding("b")).build())
                .rule(NotNullValidationRule.builder(binding("violations")).build())
                .rule(NotEmptyValidationRule.builder(binding("b")).build())
                .rule(Rule.builder().build(new ValidationExceptionThrowingRule()))
                .finalizer(Actions.action((Binding<Integer> a) -> a.setValue(0)))
                .build();

        RuleSetBuilder builder = RuleSet.builder()
                .with(ruleSet)
                .rule(0, UrlValidationRule.builder(binding("d")).build())
                .rule(0, EmailValidationRule.builder(binding("e")).build());
        RuleSet<?> updatedRuleSet = builder.build();

        Assertions.assertEquals(updatedRuleSet.getRule(0).getTarget().getClass(), EmailValidationRule.class);
        Assertions.assertEquals(updatedRuleSet.getRule(1).getTarget().getClass(), UrlValidationRule.class);
        Assertions.assertEquals(updatedRuleSet.getRule(2).getTarget().getClass(), NotNullValidationRule.class);
        Assertions.assertEquals(updatedRuleSet.getRule(6).getTarget().getClass(), ValidationExceptionThrowingRule.class);
    }

    @Test
    public void test13() {
        RuleSet<Integer> ruleSet = RuleSet.builder().with("TestRuleSet")
                .rule(NotNullValidationRule.builder(binding("a")).build())
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
                .rule(NotNullValidationRule.builder(binding("a")).build())
                .rule(NotEmptyValidationRule.builder(binding("a")).build())
                .rule(NotNullValidationRule.builder(binding("b")).build())
                .rule(NumericValidationRule.builder(binding("b")).build())
                .rule(UpperCaseValidationRule.builder(binding("c")).build())
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
                .rule(NotNullValidationRule.builder(binding("a")).build())
                .rule(NotEmptyValidationRule.builder(binding("a")).build())
                .rule(NotNullValidationRule.builder(binding("b")).build())
                .rule(NumericValidationRule.builder(binding("b")).build())
                .rule(UpperCaseValidationRule.builder(binding("c")).build())
                .stopCondition(RuleSetConditions.stopOnPassCount(2))
                .validating()
                .build();
        RuleSetExecutionStatus result = ruleSet.run(a -> "aaa", b -> null, c -> "ccc");
        Assertions.assertEquals(result.size(), 2);
    }

    @Test
    public void test16() {
        RuleSet<RuleSetExecutionStatus> ruleSet = RuleSet.builder().with("TestRuleSet")
                .rule(NotNullValidationRule.builder(binding("a")).build())
                .rule(NotEmptyValidationRule.builder(binding("a")).build())
                .rule(NotNullValidationRule.builder(binding("b")).build())
                .rule(NumericValidationRule.builder(binding("b")).build())
                .rule(UpperCaseValidationRule.builder(binding("c")).build())
                .stopCondition(RuleSetConditions.stopWhenOnePasses())
                .validating()
                .build();
        RuleSetExecutionStatus result = ruleSet.run(a -> "aaa", b -> null, c -> "ccc");
        Assertions.assertEquals(result.size(), 1);
    }

    @Test
    public void test17() {
        RuleSet<RuleSetExecutionStatus> ruleSet = RuleSet.builder().with("TestRuleSet")
                .rule(NotNullValidationRule.builder(binding("a")).build())
                .rule(NotEmptyValidationRule.builder(binding("a")).build())
                .rule(NotNullValidationRule.builder(binding("b")).build())
                .rule(NumericValidationRule.builder(binding("b")).build())
                .rule(UpperCaseValidationRule.builder(binding("c")).build())
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
                .rule(NotNullValidationRule.builder(binding("a")).build())
                .rule(NotEmptyValidationRule.builder(binding("a")).build())
                .rule(NotNullValidationRule.builder(binding("b")).build())
                .rule(NumericValidationRule.builder(binding("b")).build())
                .rule(UpperCaseValidationRule.builder(binding("c")).build())
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
                .rule(NumericValidationRule.builder(binding("a")).build())
                .rule(UpperCaseValidationRule.builder(binding("a")).build())
                .rule(NotNullValidationRule.builder(binding("b")).build())
                .rule(NumericValidationRule.builder(binding("b")).build())
                .rule(UpperCaseValidationRule.builder(binding("c")).build())
                .stopCondition(RuleSetConditions.stopOnSkipCount(2))
                .validating()
                .build();
        RuleSetExecutionStatus result = ruleSet.run(a -> Boolean.TRUE, b -> "123", c -> "ABC");
        Assertions.assertEquals(result.size(), 2);
    }

    @Test
    public void test20() {
        RuleSet<RuleSetExecutionStatus> ruleSet = RuleSet.builder().with("TestRuleSet")
                .rule(NumericValidationRule.builder(binding("a")).build())
                .rule(NotEmptyValidationRule.builder(binding("a")).build())
                .rule(NotNullValidationRule.builder(binding("b")).build())
                .rule(NumericValidationRule.builder(binding("b")).build())
                .rule(UpperCaseValidationRule.builder(binding("c")).build())
                .stopCondition(RuleSetConditions.stopWhenOneFailsOrSkipped())
                .validating()
                .build();
        RuleSetExecutionStatus result = ruleSet.run(a -> Boolean.TRUE, b -> "123", c -> "ABC");
        Assertions.assertEquals(result.size(), 1);
    }

    @Test
    public void test21() {
        RuleSet<RuleSetExecutionStatus> ruleSet = RuleSet.builder().with("TestRuleSet")
                .preCondition(RuleSetConditions.allMustPass())
                .rule(NotNullValidationRule.builder(binding("a")).build())
                .rule(NotEmptyValidationRule.builder(binding("a")).build())
                .rule(NotNullValidationRule.builder(binding("b")).build())
                .rule(NumericValidationRule.builder(binding("b")).build())
                .rule(UpperCaseValidationRule.builder(binding("c")).build())
                .validating()
                .build();
        RuleSetExecutionStatus result = ruleSet.run(a -> "abc", b -> "333", c -> "A");
        Assertions.assertEquals(result.size(), 5);
    }

    @Test
    public void test22() {
        RuleSet<RuleSetExecutionStatus> ruleSet = RuleSet.builder().with("TestRuleSet")
                .preCondition(RuleSetConditions.oneMustPass())
                .rule(NotNullValidationRule.builder(binding("a")).build())
                .rule(NotEmptyValidationRule.builder(binding("a")).build())
                .rule(NotNullValidationRule.builder(binding("b")).build())
                .rule(NumericValidationRule.builder(binding("b")).build())
                .rule(UpperCaseValidationRule.builder(binding("c")).build())
                .validating()
                .build();
        RuleSetExecutionStatus result = ruleSet.run(a -> "abc", b -> "333", c -> "A");
        Assertions.assertEquals(result.size(), 5);
    }

    @Test
    public void test23() {
        RuleSet<RuleSetExecutionStatus> ruleSet = RuleSet.builder().with("TestRuleSet")
                .preCondition(RuleSetConditions.noneCanPass())
                .rule(NumericValidationRule.builder(binding("a")).build())
                .rule(UpperCaseValidationRule.builder(binding("a")).build())
                .rule(NumericValidationRule.builder(binding("b")).build())
                .rule(UpperCaseValidationRule.builder(binding("b")).build())
                .rule(UpperCaseValidationRule.builder(binding("c")).build())
                .validating()
                .build();
        RuleSetExecutionStatus result = ruleSet.run(a -> Boolean.TRUE, b -> Boolean.TRUE, c -> Boolean.TRUE);
        Assertions.assertEquals(result.size(), 5);
    }

    @Test
    public void test24() {
        RuleSet<RuleSetExecutionStatus> ruleSet = RuleSet.builder().with("TestRuleSet")
                .preCondition(RuleSetConditions.onlyOneCanPass())
                .rule(NumericValidationRule.builder(binding("a")).build())
                .rule(UpperCaseValidationRule.builder(binding("a")).build())
                .rule(NumericValidationRule.builder(binding("b")).build())
                .rule(UpperCaseValidationRule.builder(binding("b")).build())
                .rule(UpperCaseValidationRule.builder(binding("c")).build())
                .build();
        RuleSetExecutionStatus result = ruleSet.run(a -> Boolean.TRUE, b -> Boolean.TRUE, c -> "ABC");
        Assertions.assertEquals(result.size(), 5);
    }

    @Test
    public void test25() {
        RuleSet<RuleSetExecutionStatus> ruleSet = RuleSet.builder().with("TestRuleSet")
                .preCondition(RuleSetConditions.allMustPass())
                .rule(NumericValidationRule.builder(binding("a")).build())
                .rule(UpperCaseValidationRule.builder(binding("a")).build())
                .rule(NumericValidationRule.builder(binding("b")).build())
                .rule(UpperCaseValidationRule.builder(binding("b")).build())
                .rule(UpperCaseValidationRule.builder(binding("c")).build())
                .validating()
                .build();
        RuleSetExecutionStatus result = ruleSet.run(a -> Boolean.TRUE, b -> Boolean.TRUE, c -> Boolean.TRUE);
        Assertions.assertFalse(result.isPreConditionCheck());
        Assertions.assertEquals(result.size(), 0);
    }

    @Test
    public void test26() {
        RuleSet<RuleSetExecutionStatus> ruleSet = RuleSet.builder().with("TestRuleSet")
                .preCondition(RuleSetConditions.oneMustPass())
                .rule(NumericValidationRule.builder(binding("a")).build())
                .rule(UpperCaseValidationRule.builder(binding("a")).build())
                .rule(NumericValidationRule.builder(binding("b")).build())
                .rule(UpperCaseValidationRule.builder(binding("b")).build())
                .rule(UpperCaseValidationRule.builder(binding("c")).build())
                .validating()
                .build();
        RuleSetExecutionStatus result = ruleSet.run(a -> Boolean.TRUE, b -> Boolean.TRUE, c -> Boolean.TRUE);
        Assertions.assertFalse(result.isPreConditionCheck());
        Assertions.assertEquals(result.size(), 0);
    }

    @Test
    public void test27() {
        RuleSet<RuleSetExecutionStatus> ruleSet = RuleSet.builder().with("TestRuleSet")
                .preCondition(RuleSetConditions.noneCanPass())
                .rule(NumericValidationRule.builder(binding("a")).build())
                .rule(UpperCaseValidationRule.builder(binding("a")).build())
                .rule(NumericValidationRule.builder(binding("b")).build())
                .rule(UpperCaseValidationRule.builder(binding("b")).build())
                .rule(UpperCaseValidationRule.builder(binding("c")).build())
                .validating()
                .build();
        RuleSetExecutionStatus result = ruleSet.run(a -> Boolean.TRUE, b -> Boolean.TRUE, c -> "ABC");
        Assertions.assertFalse(result.isPreConditionCheck());
        Assertions.assertEquals(result.size(), 0);
    }

    @Test
    public void test28() {
        RuleSet<RuleSetExecutionStatus> ruleSet = RuleSet.builder().with("TestRuleSet")
                .preCondition(RuleSetConditions.onlyOneCanPass())
                .rule(NotNullValidationRule.builder(binding("a")).build())
                .rule(NotEmptyValidationRule.builder(binding("a")).build())
                .rule(NotNullValidationRule.builder(binding("b")).build())
                .rule(NumericValidationRule.builder(binding("b")).build())
                .rule(UpperCaseValidationRule.builder(binding("c")).build())
                .build();
        RuleSetExecutionStatus result = ruleSet.run(a -> "abc", b -> "333", c -> "A");
        Assertions.assertFalse(result.isPreConditionCheck());
        Assertions.assertEquals(result.size(), 0);
    }

    @Test
    public void test29() throws ExecutionException, InterruptedException {
        RuleSet<RuleSetExecutionStatus> ruleSet = RuleSet.builder().with("TestRuleSet")
                .rule(NotNullValidationRule.builder(binding("a")).build())
                .rule(NotEmptyValidationRule.builder(binding("a")).build())
                .rule(NotNullValidationRule.builder(binding("b")).build())
                .rule(NumericValidationRule.builder(binding("b")).build())
                .rule(UpperCaseValidationRule.builder(binding("c")).build())
                .build();

        CompletableFuture<RuleSetExecutionStatus> future = ruleSet.runAsync(RuleContext.builder().build(a -> "abc", b -> "333", c -> "A"));
        RuleSetExecutionStatus result = future.get();
        Assertions.assertTrue(result.isPreConditionCheck());
        Assertions.assertEquals(result.size(), 5);
    }

    @Test
    public void test30() {
        RuleSet<RuleSetExecutionStatus> ruleSet = RuleSet.builder().with("TestRuleSet")
                .rule(NotNullValidationRule.builder(binding("a")).build())
                .rule(NotEmptyValidationRule.builder(binding("a")).build())
                .rule(NotNullValidationRule.builder(binding("b")).build())
                .rule(NumericValidationRule.builder(binding("b")).build())
                .rule(UpperCaseValidationRule.builder(binding("c")).build())
                .build();

        CompletableFuture<RuleSetExecutionStatus> future = ruleSet.runAsync(RuleContext.builder().build(a -> "abc", b -> "333", c -> "A"));
        future.thenAccept((RuleSetExecutionStatus result) -> {
            Assertions.assertTrue(result.isPreConditionCheck());
            Assertions.assertEquals(result.size(), 5);
        });
    }

    @Test
    public void test31() {
        RuleSet<RuleSetExecutionStatus> ruleSet = RuleSet.builder().with("TestRuleSet")
                .rule(NotNullValidationRule.builder(binding("a")).build())
                .rule(NotEmptyValidationRule.builder(binding("a")).build())
                .rule(NotNullValidationRule.builder(binding("b")).build())
                .rule(NumericValidationRule.builder(binding("b")).build())
                .rule(UpperCaseValidationRule.builder(binding("c")).build())
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

    @Test
    public void test32() {
        RuleSet<RuleSetExecutionStatus> ruleSet = RuleSet.builder()
                .with("TestRuleSet")
                .param("a", String.class)
                .build();

        Assertions.assertThrowsExactly(UnrulyException.class, () -> ruleSet.run());
    }

    @Test
    public void test33() {
        RuleSet<RuleSetExecutionStatus> ruleSet = RuleSet.builder()
                .with("TestRuleSet")
                .param("a", String.class)
                .build();

        ruleSet.run(a -> "Hello world!");
    }

    @Test
    public void test34() {
        RuleSet<RuleSetExecutionStatus> ruleSet = RuleSet.builder()
                .with("TestRuleSet")
                .param("a", String.class)
                .build();

        Assertions.assertThrowsExactly(UnrulyException.class, () -> ruleSet.run(a -> 123));
    }

    @Test
    public void test35() {
        RuleSet<RuleSetExecutionStatus> ruleSet = RuleSet.builder()
                .with("TestRuleSet")
                .param("a", String.class, function(() -> "hello world!"))
                .build();

        ruleSet.run();
    }

    @Test
    public void test36() {
        RuleSet<String> ruleSet = RuleSet.builder()
                .with("TestRuleSet")
                .param("a", String.class, function(() -> "hello world!"))
                .resultExtractor(Functions.function((String a) -> a))
                .build();

        String value = ruleSet.run();
        Assertions.assertEquals(value, "hello world!");
    }

    @Test
    public void test37() {
        RuleSet<String> ruleSet = RuleSet.builder()
                .with("TestRuleSet")
                .param("a", String.class, function(() -> "hello world!"))
                .resultExtractor(Functions.function((String a) -> a))
                .build();

        String value = ruleSet.run(a -> "new value");
        Assertions.assertEquals(value, "new value");
    }

    @Test
    public void test38() {
        RuleSet<String> ruleSet = RuleSet.builder()
                .with("TestRuleSet")
                .param("a", String.class, function(() -> "hello world!"))
                .rule(AssertEqualsValidationRule.builder(binding("a"), "hello world!").build())
                .rule(AssertNotEqualsValidationRule.builder(binding("a"), "abc").build())
                .validating()
                .build();

        ruleSet.run();
    }

    @Test
    public void test39() {
        RuleSet<RuleViolations> ruleSet = RuleSet.builder()
                // RuleSet name
                .with("testRuleSet")
                // Description of the RuleSet
                .description("validation rules")
                // Input Parameter definitions
                .param("a", String.class)
                .param("b", Integer.class)
                .param("c", String.class)
                .param("violations", RuleViolations.class)
                // Rules
                .rule(NotNullValidationRule.builder(binding("a")).build())
                .rule(NotEmptyValidationRule.builder(binding("a")).build())
                .rule(NotNullValidationRule.builder(binding("b")).build())
                .rule(NumericValidationRule.builder(binding("b")).build())
                .rule(UpperCaseValidationRule.builder(binding("c")).build())
                .rule(Rule.builder().build(ConsistentDateRule.class))
                // When to stop the execution of the RuleSet
                .stopCondition(condition((RuleViolations violations) -> violations.hasErrors()))
                // Result of the RuleSet execution
                .resultExtractor(function((RuleViolations violations) -> violations))
                .build();

        // Create your bindings
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("a", "aaa");
        bindings.bind("b", 123);
        bindings.bind("c", "ABC");
        bindings.bind("flag", true);
        bindings.bind("violations", new RuleViolations());

        //Run the RuleSet
        RuleViolations violations = ruleSet.run(bindings);

        // Found errors
        if (violations.hasErrors()) {
            throw new ValidationException(violations);
        }
    }
}
