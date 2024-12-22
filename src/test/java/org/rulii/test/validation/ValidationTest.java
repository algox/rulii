/*
 * This software is licensed under the Apache 2 license, quoted below.
 *
 * Copyright (c) 1999-2024, Algorithmx Inc.
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
package org.rulii.test.validation;

import org.rulii.bind.Bindings;
import org.rulii.context.RuleContext;
import org.rulii.rule.Rule;
import org.rulii.ruleset.RuleSet;
import org.rulii.validation.RuleViolations;
import org.rulii.validation.rules.binding.MustBeDefinedRule;
import org.rulii.validation.rules.binding.MustNotBeDefinedRule;
import org.rulii.validation.rules.future.FutureValidationRule;
import org.rulii.validation.rules.max.MaxValidationRule;
import org.rulii.validation.rules.min.MinValidationRule;
import org.rulii.validation.rules.notblank.NotBlankValidationRule;
import org.rulii.validation.rules.notnull.NotNullValidationRule;
import org.rulii.validation.rules.nulll.NullValidationRule;
import org.rulii.validation.rules.past.PastValidationRule;
import org.rulii.validation.rules.pattern.PatternValidationRule;
import org.rulii.validation.rules.size.SizeValidationRule;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

import static org.rulii.model.condition.Conditions.condition;
/**
 * Test cases for the various Validation Rules.
 *
 * @author Max Arulananthan.
 */
public class ValidationTest {

    public ValidationTest() {
        super();
    }

    @Test
    public void test1() {
        RuleSet rules = RuleSet.builder()
                .with("RuleSet1", "Test Rule Set")
                    .rule(Rule.builder().build(TestRule1.class))
                    .rule(Rule.builder().build(TestRule2.class))
                    .rule(Rule.builder().build(TestRule3.class))
                    .rule(Rule.builder().build(TestRule4.class))
                    .rule(Rule.builder().build(TestRule5.class))
                .build();

        Bindings bindings = Bindings.builder().standard();
        bindings.bind("value", Integer.class, 1);
        bindings.bind("errors", RuleViolations.class, new RuleViolations());

        rules.run(RuleContext.builder().with(bindings).build());
    }

    @Test
    public void test2() {
        RuleSet rules = RuleSet.builder()
                .with("RuleSet2", "Test Rule Set")
                    .rule(Rule.builder().build(TestRule1.class))
                    .rule(Rule.builder().build(TestRule2.class))
                    .rule(Rule.builder().build(TestRule3.class))
                    .rule(Rule.builder().build(TestRule4.class))
                    .rule(Rule.builder().build(TestRule5.class))
                .build();

        rules.run(RuleContext.builder().
                with(Bindings.builder().standard(value -> 75, errors -> new RuleViolations())).build());
    }

    @Test
    public void testNotNullRule() {
        RuleViolations errors = new RuleViolations();
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("value",1);
        bindings.bind("e", errors);

        RuleSet rules = RuleSet.builder()
                .with("RuleSet2", "Test Rule Set")
                    .rule(Rule.builder().build(new NotNullValidationRule("value")))
                .build();

        RuleContext context = RuleContext.builder().with(bindings).build();
        rules.run(context);
        Assert.assertTrue(errors.size() == 0);
    }

    @Test
    public void testNullRule() {
        RuleViolations errors = new RuleViolations();
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("b", String.class, null);
        bindings.bind("e", errors);

        RuleSet rules = RuleSet.builder()
                .with("RuleSet2", "Test Rule Set")
                    .rule(Rule.builder().build(new NullValidationRule("value")))
                .build();

        rules.run(RuleContext.builder().with(bindings).build());
        Assert.assertTrue(errors.size() == 0);
    }

    @Test
    public void testStringLengthRule() {
        RuleViolations errors = new RuleViolations();
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("value", String.class, "  ");
        bindings.bind("e", errors);

        RuleSet rules = RuleSet.builder()
                .with("RuleSet2", "Test Rule Set")
                    .rule(Rule.builder().build(new SizeValidationRule("value", 1, Integer.MAX_VALUE)))
                .build();

        rules.run(RuleContext.builder().with(bindings).build());
        Assert.assertTrue(errors.size() == 0);
    }

    @Test
    public void testStringTextRule() {
        RuleViolations errors = new RuleViolations();
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("value", String.class, "  a");
        bindings.bind("e", errors);

        RuleSet rules = RuleSet.builder()
                .with("RuleSet2", "Test Rule Set")
                    .rule(Rule.builder().build(new NotBlankValidationRule("value")))
                .build();

        rules.run(RuleContext.builder().with(bindings).build());
        Assert.assertTrue(errors.size() == 0);
    }

    @Test
    public void testStringPatternRule() {
        RuleViolations errors = new RuleViolations();
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("value", String.class, "ababab");
        bindings.bind("e", errors);

        RuleSet rules = RuleSet.builder()
                .with("RuleSet2", "Test Rule Set")
                    .rule(Rule.builder().build(new PatternValidationRule("value","[z]*")))
                .build();

        rules.run(RuleContext.builder().with(bindings).build());
        Assert.assertTrue(errors.size() == 1);
    }


    @Test
    public void testFutureDateRule() {
        RuleViolations errors = new RuleViolations();
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("value", new Date());
        bindings.bind("e", errors);

        RuleSet rules = RuleSet.builder()
                .with("RuleSet", "Test Rule Set")
                    .rule(Rule.builder().build(new FutureValidationRule("value")))
                .build();

        rules.run(RuleContext.builder().with(bindings).build());
    }

    @Test
    public void tesPastDateRule() {
        RuleViolations errors = new RuleViolations();
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("value", new Date());
        bindings.bind("e", errors);

        RuleSet rules = RuleSet.builder()
                .with("RuleSet", "Test Rule Set")
                    .rule(Rule.builder().build(new PastValidationRule("value")))
                .build();

        rules.run(RuleContext.builder().with(bindings).build());
        Assert.assertTrue(errors.size() == 0);
    }


    @Test
    public void testMaxRule() {
        RuleViolations errors = new RuleViolations();
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("value", 25);
        bindings.bind("e", errors);

        RuleSet rules = RuleSet.builder()
                .with("RuleSet", "Test Rule Set")
                    .rule(Rule.builder().build(new MaxValidationRule("value", 50)))
                    .rule(Rule.builder().build(new MaxValidationRule("value", 20)))
                .build();

        rules.run(RuleContext.builder().with(bindings).build());
        Assert.assertTrue(errors.size() == 1);
    }

    @Test
    public void testMinRule() {
        RuleViolations errors = new RuleViolations();
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("value", 10);
        bindings.bind("e", errors);

        RuleSet rules = RuleSet.builder()
                .with("RuleSet", "Test Rule Set")
                    .rule(Rule.builder().build(new MinValidationRule("value", 11)))
                    .rule(Rule.builder().build(new MinValidationRule("value", 5)))
                    .rule(Rule.builder().build(new MinValidationRule("value", 25)))
                    .rule(Rule.builder().build(new MinValidationRule("value",25)))
                .build();

        rules.run(RuleContext.builder().with(bindings).build());
        Assert.assertTrue(errors.size() == 3);
    }

    @Test
    public void testMustBeDefinedRule() {
        RuleViolations errors = new RuleViolations();
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("a", 22);
        bindings.bind("e", errors);

        RuleSet rules = RuleSet.builder()
                .with("RuleSet", "Test Rule Set")
                    .preCondition(condition(() -> true))
                    .rule(Rule.builder().build(new MustBeDefinedRule("c")))
                    .rule(Rule.builder().build(new MustBeDefinedRule("a")))
                .build();

        rules.run(RuleContext.builder().with(bindings).build());
        Assert.assertTrue(errors.size() == 1);
    }

    @Test
    public void testMustNotBeDefinedRule() {
        RuleViolations errors = new RuleViolations();
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("a", 22);
        bindings.bind("e", errors);

        RuleSet rules = RuleSet.builder()
                .with("RuleSet", "Test Rule Set")
                    .rule(Rule.builder().build(new MustNotBeDefinedRule("a")))
                    .rule(Rule.builder().build(new MustNotBeDefinedRule("c")))
                .build();

        rules.run(RuleContext.builder().with(bindings).build());
        Assert.assertTrue(errors.size() == 1);
    }

    @Test
    public void testLambdaValidationRule() {
        RuleViolations errors = new RuleViolations();
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("a", 22);
        bindings.bind("e", errors);

        Rule rule = Rule.builder()
                .validationRule("testRule", "errorCode.100")
                .given(condition((Integer a) -> a < 20)).build();

        rule.run(RuleContext.builder().with(bindings).build());
        Assert.assertTrue(errors.size() == 1);
    }
}
