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
package org.rulii.test.validation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.model.UnrulyException;
import org.rulii.rule.Rule;
import org.rulii.rule.RuleResult;
import org.rulii.validation.RuleViolation;
import org.rulii.validation.RuleViolations;
import org.rulii.validation.Severity;
import org.rulii.validation.rules.alpha.AlphaValidationRule;
import org.rulii.validation.rules.alphnumeric.AlphaNumericValidationRule;
import org.rulii.validation.rules.ascii.AsciiValidationRule;
import org.rulii.validation.rules.asssert.AssertFalseValidationRule;
import org.rulii.validation.rules.asssert.AssertTrueValidationRule;
import org.rulii.validation.rules.binding.MustBeDefinedRule;
import org.rulii.validation.rules.binding.MustNotBeDefinedRule;
import org.rulii.validation.rules.blank.BlankValidationRule;
import org.rulii.validation.rules.decimal.DecimalValidationRule;
import org.rulii.validation.rules.digits.DigitsValidationRule;
import org.rulii.validation.rules.email.EmailValidationRule;
import org.rulii.validation.rules.endswith.EndsWithValidationRule;
import org.rulii.validation.rules.fileexists.FileExistsValidationRule;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
/**
 * Class containing test methods for validating the functionality of Validation Rules.
 *
 * @author Max Arulananthan
 * @since 1.0
 *
 */
public class ValidationRuleTests {

    public ValidationRuleTests() {
        super();
    }

    @Test
    public void alphaTest1() {
        AlphaValidationRule validationRule = new AlphaValidationRule("value");
        Rule rule = Rule.builder().build(validationRule);
        RuleResult result = rule.run(value -> "abc");
        assertTrue(result.status().isPass());
    }

    @Test
    public void alphaTest2() {
        AlphaValidationRule validationRule = new AlphaValidationRule("value", "error.1", Severity.ERROR, "Alpha Error Message", false);
        Rule rule = Rule.builder().build(validationRule);

        RuleResult result = rule.run(value -> "abc");
        assertTrue(result.status().isPass());

        RuleViolations errors = new RuleViolations();
        result = rule.run(ruleViolations -> errors, value -> "abc~");
        assertTrue(result.status().isFail());
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getViolations().size());
        RuleViolation violation = errors.getViolations().get(0);
        assertEquals("error.1", violation.getErrorCode());
        assertEquals(Severity.ERROR, violation.getSeverity());
        assertEquals("Alpha Error Message", violation.getErrorMessage());
    }

    @Test
    public void alphaTest3() {
        Rule rule = Rule.builder().build(new AlphaValidationRule("value"));
        RuleResult result = rule.run(value -> "abc");
        assertTrue(result.status().isPass());

        RuleViolations errors = new RuleViolations();
        result = rule.run(ruleViolations -> errors, value -> "abc~");
        assertTrue(result.status().isFail());
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getViolations().size());
        RuleViolation violation = errors.getViolations().get(0);
        assertEquals(AlphaValidationRule.ERROR_CODE, violation.getErrorCode());
        assertEquals(Severity.ERROR, violation.getSeverity());
        assertEquals("Value must only contain unicode letters. Given abc~.", violation.getErrorMessage());
    }

    @Test
    public void alphaTest4() {
        Rule rule = Rule.builder().build(new AlphaValidationRule("value", "alphaError1",
                Severity.FATAL, "Alpha Error Message", false));
        RuleViolations errors = new RuleViolations();
        RuleResult result = rule.run(ruleViolations -> errors, value -> "abc d");
        assertTrue(result.status().isFail());
        assertTrue(errors.hasSevereErrors());
        assertEquals(1, errors.getViolations().size());
        RuleViolation violation = errors.getViolations().get(0);
        assertEquals("alphaError1", violation.getErrorCode());
        assertEquals(Severity.FATAL, violation.getSeverity());
        assertEquals("Alpha Error Message", violation.getErrorMessage());
    }
    @Test
    public void alphaTest5() {
        AlphaValidationRule validationRule = new AlphaValidationRule("value");
        Rule rule = Rule.builder().build(validationRule);
        Assertions.assertThrows(UnrulyException.class, rule::run);
        RuleResult result = rule.run(value -> null);
        assertTrue(result.status().isPass());
    }

    @Test
    public void alphaTest6() {
        AlphaValidationRule validationRule = new AlphaValidationRule("value");
        Rule rule = Rule.builder().build(validationRule);
        RuleResult result = rule.run(value -> Boolean.TRUE);
        assertTrue(result.status().isSkipped());
    }

    @Test
    public void alphaNumericTest1() {
        AlphaNumericValidationRule validationRule = new AlphaNumericValidationRule("value");
        Rule rule = Rule.builder().build(validationRule);
        RuleResult result = rule.run(value -> "abc1");
        assertTrue(result.status().isPass());
    }

    @Test
    public void alphaNumericTest2() {
        AlphaNumericValidationRule validationRule = new AlphaNumericValidationRule("value",
                "error.1", Severity.ERROR, "Alpha Numeric Error Message", false);
        Rule rule = Rule.builder().build(validationRule);

        RuleResult result = rule.run(value -> "abc1");
        assertTrue(result.status().isPass());

        RuleViolations errors = new RuleViolations();
        result = rule.run(ruleViolations -> errors, value -> "abc1~");
        assertTrue(result.status().isFail());
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getViolations().size());
        RuleViolation violation = errors.getViolations().get(0);
        assertEquals("error.1", violation.getErrorCode());
        assertEquals(Severity.ERROR, violation.getSeverity());
        assertEquals("Alpha Numeric Error Message", violation.getErrorMessage());
    }

    @Test
    public void alphaNumericTest3() {
        Rule rule = Rule.builder().build(new AlphaNumericValidationRule("value"));
        RuleResult result = rule.run(value -> "abc1");
        assertTrue(result.status().isPass());

        RuleViolations errors = new RuleViolations();
        result = rule.run(ruleViolations -> errors, value -> "abc1~");
        assertTrue(result.status().isFail());
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getViolations().size());
        RuleViolation violation = errors.getViolations().get(0);
        assertEquals(AlphaNumericValidationRule.ERROR_CODE, violation.getErrorCode());
        assertEquals(Severity.ERROR, violation.getSeverity());
        assertEquals("Value must only contain alphanumeric letters. Given abc1~.", violation.getErrorMessage());
    }

    @Test
    public void alphaNumericTest4() {
        Rule rule = Rule.builder().build(new AlphaNumericValidationRule("value", "alphaNumericError1", Severity.FATAL, "Alpha Numeric Error Message", false));
        RuleViolations errors = new RuleViolations();
        RuleResult result = rule.run(ruleViolations -> errors, value -> "abc 1");
        assertTrue(result.status().isFail());
        assertTrue(errors.hasSevereErrors());
        assertEquals(1, errors.getViolations().size());
        RuleViolation violation = errors.getViolations().get(0);
        assertEquals("alphaNumericError1", violation.getErrorCode());
        assertEquals(Severity.FATAL, violation.getSeverity());
        assertEquals("Alpha Numeric Error Message", violation.getErrorMessage());
    }

    @Test
    public void alphaNumericTest5() {
        AlphaNumericValidationRule validationRule = new AlphaNumericValidationRule("value");
        Rule rule = Rule.builder().build(validationRule);
        Assertions.assertThrows(UnrulyException.class, rule::run);
        RuleResult result = rule.run(value -> null);
        assertTrue(result.status().isPass());
    }

    @Test
    public void alphaNumericTest6() {
        AlphaNumericValidationRule validationRule = new AlphaNumericValidationRule("value");
        Rule rule = Rule.builder().build(validationRule);
        RuleResult result = rule.run(value -> Boolean.TRUE);
        assertTrue(result.status().isSkipped());
    }

    @Test
    public void asciiTest1() {
        AsciiValidationRule validationRule = new AsciiValidationRule("value");
        Rule rule = Rule.builder().build(validationRule);
        RuleResult result = rule.run(value -> "abc1");
        assertTrue(result.status().isPass());
    }

    @Test
    public void asciiTest2() {
        AsciiValidationRule validationRule = new AsciiValidationRule("someValue", "error.2", Severity.FATAL, "Ascii Error Message");
        Rule rule = Rule.builder().build(validationRule);

        RuleResult result = rule.run(someValue -> "abc1");
        assertTrue(result.status().isPass());

        RuleViolations errors = new RuleViolations();
        result = rule.run(ruleViolations -> errors, someValue -> "abc1é");
        assertTrue(result.status().isFail());
        assertTrue(errors.hasFatalErrors());
        assertEquals(1, errors.getViolations().size());
        RuleViolation violation = errors.getViolations().get(0);
        assertEquals("error.2", violation.getErrorCode());
        assertEquals(Severity.FATAL, violation.getSeverity());
        assertEquals("Ascii Error Message", violation.getErrorMessage());
    }

    @Test
    public void asciiTest3() {
        Rule rule = Rule.builder().build(new AsciiValidationRule("value"));
        RuleResult result = rule.run(value -> "abc1");
        assertTrue(result.status().isPass());

        RuleViolations errors = new RuleViolations();
        result = rule.run(ruleViolations -> errors, value -> "abc1é");
        assertTrue(result.status().isFail());
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getViolations().size());
        RuleViolation violation = errors.getViolations().get(0);
        assertEquals(AsciiValidationRule.ERROR_CODE, violation.getErrorCode());
        assertEquals(Severity.ERROR, violation.getSeverity());
        assertEquals("Value must only contain ASCII printable characters. Given abc1é.", violation.getErrorMessage());
    }

    @Test
    public void asciiTest4() {
        Rule rule = Rule.builder().build(new AsciiValidationRule("value", "asciiError1", Severity.FATAL, "Ascii Error Message"));
        RuleViolations errors = new RuleViolations();
        RuleResult result = rule.run(ruleViolations -> errors, value -> "abc1é");
        assertTrue(result.status().isFail());
        assertTrue(errors.hasSevereErrors());
        assertEquals(1, errors.getViolations().size());
        RuleViolation violation = errors.getViolations().get(0);
        assertEquals("asciiError1", violation.getErrorCode());
        assertEquals(Severity.FATAL, violation.getSeverity());
        assertEquals("Ascii Error Message", violation.getErrorMessage());
    }

    @Test
    public void asciiTest5() {
        AsciiValidationRule validationRule = new AsciiValidationRule("value");
        Rule rule = Rule.builder().build(validationRule);
        Assertions.assertThrows(UnrulyException.class, rule::run);
        RuleResult result = rule.run(value -> null);
        assertTrue(result.status().isPass());
    }

    @Test
    public void asciiTest6() {
        AsciiValidationRule validationRule = new AsciiValidationRule("value");
        Rule rule = Rule.builder().build(validationRule);
        RuleResult result = rule.run(value -> Boolean.TRUE);
        assertTrue(result.status().isSkipped());
    }

    @Test
    public void assertFalseTest1() {
        AssertFalseValidationRule validationRule = new AssertFalseValidationRule("value");
        Rule rule = Rule.builder().build(validationRule);
        RuleResult result = rule.run(value -> false);
        assertTrue(result.status().isPass());
    }

    @Test
    public void assertFalseTest2() {
        AssertFalseValidationRule validationRule = new AssertFalseValidationRule("value", "error.3", Severity.ERROR, "Assert False Error Message");
        Rule rule = Rule.builder().build(validationRule);

        RuleResult result = rule.run(value -> false);
        assertTrue(result.status().isPass());

        RuleViolations errors = new RuleViolations();
        result = rule.run(ruleViolations -> errors, value -> true);
        assertTrue(result.status().isFail());
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getViolations().size());
        RuleViolation violation = errors.getViolations().get(0);
        assertEquals("error.3", violation.getErrorCode());
        assertEquals(Severity.ERROR, violation.getSeverity());
        assertEquals("Assert False Error Message", violation.getErrorMessage());
    }

    @Test
    public void assertFalseTest3() {
        Rule rule = Rule.builder().build(new AssertFalseValidationRule("value"));
        RuleResult result = rule.run(value -> Boolean.FALSE);
        assertTrue(result.status().isPass());

        RuleViolations errors = new RuleViolations();
        result = rule.run(ruleViolations -> errors, value -> true);
        assertTrue(result.status().isFail());
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getViolations().size());
        RuleViolation violation = errors.getViolations().get(0);
        assertEquals(AssertFalseValidationRule.ERROR_CODE, violation.getErrorCode());
        assertEquals(Severity.ERROR, violation.getSeverity());
        assertEquals("Value must be false.", violation.getErrorMessage());
    }

    @Test
    public void assertFalseTest4() {
        Rule rule = Rule.builder().build(new AssertFalseValidationRule("value", "assertFalseError1", Severity.FATAL, "Assert False Error Message"));
        RuleViolations errors = new RuleViolations();
        RuleResult result = rule.run(ruleViolations -> errors, value -> Boolean.TRUE);
        assertTrue(result.status().isFail());
        assertTrue(errors.hasSevereErrors());
        assertEquals(1, errors.getViolations().size());
        RuleViolation violation = errors.getViolations().get(0);
        assertEquals("assertFalseError1", violation.getErrorCode());
        assertEquals(Severity.FATAL, violation.getSeverity());
        assertEquals("Assert False Error Message", violation.getErrorMessage());
    }

    @Test
    public void assertFalseTest5() {
        AssertFalseValidationRule validationRule = new AssertFalseValidationRule("value");
        Rule rule = Rule.builder().build(validationRule);
        Assertions.assertThrows(UnrulyException.class, rule::run);
        RuleResult result = rule.run(value -> null);
        assertTrue(result.status().isPass());
    }

    @Test
    public void assertFalseTest6() {
        AssertFalseValidationRule validationRule = new AssertFalseValidationRule("value");
        Rule rule = Rule.builder().build(validationRule);
        RuleResult result = rule.run(value -> "test");
        assertTrue(result.status().isSkipped());
    }

    @Test
    public void assertTrueTest1() {
        AssertTrueValidationRule validationRule = new AssertTrueValidationRule("value");
        Rule rule = Rule.builder().build(validationRule);
        RuleResult result = rule.run(value -> true);
        assertTrue(result.status().isPass());
    }

    @Test
    public void assertTrueTest2() {
        AssertTrueValidationRule validationRule = new AssertTrueValidationRule("value",
                "error.3", Severity.ERROR, "Assert True Error Message");
        Rule rule = Rule.builder().build(validationRule);

        RuleResult result = rule.run(value -> true);
        assertTrue(result.status().isPass());

        RuleViolations errors = new RuleViolations();
        result = rule.run(ruleViolations -> errors, value -> false);
        assertTrue(result.status().isFail());
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getViolations().size());
        RuleViolation violation = errors.getViolations().get(0);
        assertEquals("error.3", violation.getErrorCode());
        assertEquals(Severity.ERROR, violation.getSeverity());
        assertEquals("Assert True Error Message", violation.getErrorMessage());
    }

    @Test
    public void assertTrueTest3() {
        Rule rule = Rule.builder().build(new AssertTrueValidationRule("value"));
        RuleResult result = rule.run(value -> Boolean.TRUE);
        assertTrue(result.status().isPass());

        RuleViolations errors = new RuleViolations();
        result = rule.run(ruleViolations -> errors, value -> false);
        assertTrue(result.status().isFail());
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getViolations().size());
        RuleViolation violation = errors.getViolations().get(0);
        assertEquals(AssertTrueValidationRule.ERROR_CODE, violation.getErrorCode());
        assertEquals(Severity.ERROR, violation.getSeverity());
        assertEquals("Value must be true.", violation.getErrorMessage());
    }

    @Test
    public void assertTrueTest4() {
        Rule rule = Rule.builder().build(new AssertTrueValidationRule("value", "assertTrueError1", Severity.FATAL, "Assert True Error Message"));
        RuleViolations errors = new RuleViolations();
        RuleResult result = rule.run(ruleViolations -> errors, value -> Boolean.FALSE);
        assertTrue(result.status().isFail());
        assertTrue(errors.hasSevereErrors());
        assertEquals(1, errors.getViolations().size());
        RuleViolation violation = errors.getViolations().get(0);
        assertEquals("assertTrueError1", violation.getErrorCode());
        assertEquals(Severity.FATAL, violation.getSeverity());
        assertEquals("Assert True Error Message", violation.getErrorMessage());
    }

    @Test
    public void assertTrueTest5() {
        AssertTrueValidationRule validationRule = new AssertTrueValidationRule("value");
        Rule rule = Rule.builder().build(validationRule);
        Assertions.assertThrows(UnrulyException.class, rule::run);
        RuleResult result = rule.run(value -> null);
        assertTrue(result.status().isPass());
    }

    @Test
    public void assertTrueTest6() {
        AssertTrueValidationRule validationRule = new AssertTrueValidationRule("value");
        Rule rule = Rule.builder().build(validationRule);
        RuleResult result = rule.run(value -> "test");
        assertTrue(result.status().isSkipped());
    }

    @Test
    public void bindingDefinedTest1() {
        MustBeDefinedRule validationRule = new MustBeDefinedRule("value");
        Rule rule = Rule.builder().build(validationRule);
        RuleResult result = rule.run(value -> true);
        assertTrue(result.status().isPass());
    }

    @Test
    public void bindingDefinedTest2() {
        Rule rule = Rule.builder().build(new MustBeDefinedRule("value"));
        RuleResult result = rule.run(value -> Boolean.TRUE);
        assertTrue(result.status().isPass());

        RuleViolations errors = new RuleViolations();
        result = rule.run(ruleViolations -> errors);
        assertTrue(result.status().isFail());
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getViolations().size());
        RuleViolation violation = errors.getViolations().get(0);
        assertEquals(MustBeDefinedRule.ERROR_CODE, violation.getErrorCode());
        assertEquals(Severity.ERROR, violation.getSeverity());
        assertEquals("Binding value not defined.", violation.getErrorMessage());
    }

    @Test
    public void bindingDefinedTest3() {
        Rule rule = Rule.builder().build(new MustBeDefinedRule("value", "assertTrueError1", Severity.FATAL, "Assert True Error Message"));
        RuleViolations errors = new RuleViolations();
        RuleResult result = rule.run(ruleViolations -> errors);
        assertTrue(result.status().isFail());
        assertTrue(errors.hasSevereErrors());
        assertEquals(1, errors.getViolations().size());
        RuleViolation violation = errors.getViolations().get(0);
        assertEquals("assertTrueError1", violation.getErrorCode());
        assertEquals(Severity.FATAL, violation.getSeverity());
        assertEquals("Assert True Error Message", violation.getErrorMessage());
    }

    @Test
    public void bindingDefinedTest4() {
        MustBeDefinedRule validationRule = new MustBeDefinedRule("value");
        Rule rule = Rule.builder().build(validationRule);
        Assertions.assertThrows(UnrulyException.class, rule::run);
        RuleResult result = rule.run(value -> null);
        assertTrue(result.status().isPass());
    }

    @Test
    public void bindingNotDefinedTest1() {
        MustNotBeDefinedRule validationRule = new MustNotBeDefinedRule("value");
        Rule rule = Rule.builder().build(validationRule);
        RuleResult result = rule.run();
        assertTrue(result.status().isPass());
    }

    @Test
    public void bindingNotDefinedTest2() {
        Rule rule = Rule.builder().build(new MustNotBeDefinedRule("value"));
        RuleResult result = rule.run();
        assertTrue(result.status().isPass());

        RuleViolations errors = new RuleViolations();
        result = rule.run(ruleViolations -> errors, value -> "test");
        assertTrue(result.status().isFail());
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getViolations().size());
        RuleViolation violation = errors.getViolations().get(0);
        assertEquals(MustNotBeDefinedRule.ERROR_CODE, violation.getErrorCode());
        assertEquals(Severity.ERROR, violation.getSeverity());
        assertEquals("Binding value must not be defined.", violation.getErrorMessage());
    }

    @Test
    public void bindingNotDefinedTest3() {
        Rule rule = Rule.builder().build(new MustNotBeDefinedRule("value", "mustNotBeError1", Severity.FATAL, "Binding Must not defined Error Message"));
        RuleViolations errors = new RuleViolations();
        RuleResult result = rule.run(ruleViolations -> errors, value -> "test");
        assertTrue(result.status().isFail());
        assertTrue(errors.hasSevereErrors());
        assertEquals(1, errors.getViolations().size());
        RuleViolation violation = errors.getViolations().get(0);
        assertEquals("mustNotBeError1", violation.getErrorCode());
        assertEquals(Severity.FATAL, violation.getSeverity());
        assertEquals("Binding Must not defined Error Message", violation.getErrorMessage());
    }

    @Test
    public void blankTest1() {
        BlankValidationRule validationRule = new BlankValidationRule("value");
        Rule rule = Rule.builder().build(validationRule);
        RuleResult result = rule.run(value -> "     ");
        assertTrue(result.status().isPass());
    }

    @Test
    public void blankTest2() {
        BlankValidationRule validationRule = new BlankValidationRule("value",
                "error.3", Severity.ERROR, "Blank Error Message");
        Rule rule = Rule.builder().build(validationRule);

        RuleResult result = rule.run(value -> "     ");
        assertTrue(result.status().isPass());

        RuleViolations errors = new RuleViolations();
        result = rule.run(ruleViolations -> errors, value -> "abc");
        assertTrue(result.status().isFail());
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getViolations().size());
        RuleViolation violation = errors.getViolations().get(0);
        assertEquals("error.3", violation.getErrorCode());
        assertEquals(Severity.ERROR, violation.getSeverity());
        assertEquals("Blank Error Message", violation.getErrorMessage());
    }

    @Test
    public void blankTest3() {
        Rule rule = Rule.builder().build(new BlankValidationRule("value"));
        RuleResult result = rule.run(value -> "   ");
        assertTrue(result.status().isPass());

        RuleViolations errors = new RuleViolations();
        result = rule.run(ruleViolations -> errors, value -> "zzzz");
        assertTrue(result.status().isFail());
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getViolations().size());
        RuleViolation violation = errors.getViolations().get(0);
        assertEquals(BlankValidationRule.ERROR_CODE, violation.getErrorCode());
        assertEquals(Severity.ERROR, violation.getSeverity());
        assertEquals("Value must be blank. Given zzzz.", violation.getErrorMessage());
    }

    @Test
    public void blankTest4() {
        Rule rule = Rule.builder().build(new BlankValidationRule("value", "blankError1", Severity.FATAL, "Blank Error Message"));
        RuleViolations errors = new RuleViolations();
        RuleResult result = rule.run(ruleViolations -> errors, value -> "aJHG");
        assertTrue(result.status().isFail());
        assertTrue(errors.hasSevereErrors());
        assertEquals(1, errors.getViolations().size());
        RuleViolation violation = errors.getViolations().get(0);
        assertEquals("blankError1", violation.getErrorCode());
        assertEquals(Severity.FATAL, violation.getSeverity());
        assertEquals("Blank Error Message", violation.getErrorMessage());
    }

    @Test
    public void blankTest5() {
        BlankValidationRule validationRule = new BlankValidationRule("value");
        Rule rule = Rule.builder().build(validationRule);
        Assertions.assertThrows(UnrulyException.class, rule::run);
        RuleResult result = rule.run(value -> null);
        assertTrue(result.status().isPass());
    }

    @Test
    public void blankTest6() {
        BlankValidationRule validationRule = new BlankValidationRule("value");
        Rule rule = Rule.builder().build(validationRule);
        RuleResult result = rule.run(value -> new BigDecimal("123"));
        assertTrue(result.status().isSkipped());
    }

    @Test
    public void decimalTest1() {
        DecimalValidationRule validationRule = new DecimalValidationRule("value");
        Rule rule = Rule.builder().build(validationRule);
        RuleResult result = rule.run(value -> "123.44");
        assertTrue(result.status().isPass());
    }

    @Test
    public void decimalTest2() {
        DecimalValidationRule validationRule = new DecimalValidationRule("value",
                "error.1", Severity.ERROR, "Decimal Error Message", true);
        Rule rule = Rule.builder().build(validationRule);

        RuleResult result = rule.run(value -> "100.00");
        assertTrue(result.status().isPass());
        result = rule.run(value -> "    100.00  ");
        assertTrue(result.status().isPass());

        RuleViolations errors = new RuleViolations();
        result = rule.run(ruleViolations -> errors, value -> "abc112");
        assertTrue(result.status().isFail());
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getViolations().size());
        RuleViolation violation = errors.getViolations().get(0);
        assertEquals("error.1", violation.getErrorCode());
        assertEquals(Severity.ERROR, violation.getSeverity());
        assertEquals("Decimal Error Message", violation.getErrorMessage());
    }

    @Test
    public void decimalTest3() {
        Rule rule = Rule.builder().build(new DecimalValidationRule("value"));
        RuleResult result = rule.run(value -> "199.11");
        assertTrue(result.status().isPass());

        RuleViolations errors = new RuleViolations();
        result = rule.run(ruleViolations -> errors, value -> "kjasd~");
        assertTrue(result.status().isFail());
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getViolations().size());
        RuleViolation violation = errors.getViolations().get(0);
        assertEquals(DecimalValidationRule.ERROR_CODE, violation.getErrorCode());
        assertEquals(Severity.ERROR, violation.getSeverity());
        assertEquals("Invalid decimal kjasd~.", violation.getErrorMessage());
    }

    @Test
    public void decimalTest4() {
        Rule rule = Rule.builder().build(new DecimalValidationRule("value", "decimalError1", Severity.FATAL, "Decimal Error Message", false));
        RuleViolations errors = new RuleViolations();
        RuleResult result = rule.run(ruleViolations -> errors, value -> "abc 1");
        assertTrue(result.status().isFail());
        assertTrue(errors.hasSevereErrors());
        assertEquals(1, errors.getViolations().size());
        RuleViolation violation = errors.getViolations().get(0);
        assertEquals("decimalError1", violation.getErrorCode());
        assertEquals(Severity.FATAL, violation.getSeverity());
        assertEquals("Decimal Error Message", violation.getErrorMessage());
    }

    @Test
    public void decimalTest5() {
        DecimalValidationRule validationRule = new DecimalValidationRule("value");
        Rule rule = Rule.builder().build(validationRule);
        Assertions.assertThrows(UnrulyException.class, rule::run);
        RuleResult result = rule.run(value -> null);
        assertTrue(result.status().isPass());
    }

    @Test
    public void decimalTest6() {
        AlphaNumericValidationRule validationRule = new AlphaNumericValidationRule("value");
        Rule rule = Rule.builder().build(validationRule);
        RuleResult result = rule.run(value -> Boolean.TRUE);
        assertTrue(result.status().isSkipped());
    }

    @Test
    public void digitsTest1() {
        DigitsValidationRule validationRule = new DigitsValidationRule("value", 5, 2);
        Rule rule = Rule.builder().build(validationRule);
        RuleResult result = rule.run(value -> "123.44");
        assertTrue(result.status().isPass());
        result = rule.run(value -> "12345.44");
        assertTrue(result.status().isPass());
        result = rule.run(value -> "123456.44", ruleViolations -> new RuleViolations());
        assertTrue(result.status().isFail());
        result = rule.run(value -> "12345.441", ruleViolations -> new RuleViolations());
        assertTrue(result.status().isFail());
    }

    @Test
    public void digitsTest2() {
        DigitsValidationRule validationRule = new DigitsValidationRule("value",
                "error.1", Severity.ERROR, "Digits Error Message", 5, 2);
        Rule rule = Rule.builder().build(validationRule);

        RuleResult result = rule.run(value -> "100.00");
        assertTrue(result.status().isPass());

        RuleViolations errors = new RuleViolations();
        result = rule.run(ruleViolations -> errors, value -> "123456");
        assertTrue(result.status().isFail());
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getViolations().size());
        RuleViolation violation = errors.getViolations().get(0);
        assertEquals("error.1", violation.getErrorCode());
        assertEquals(Severity.ERROR, violation.getSeverity());
        assertEquals("Digits Error Message", violation.getErrorMessage());
    }

    @Test
    public void digitsTest3() {
        Rule rule = Rule.builder().build(new DigitsValidationRule("value", 2, 2));
        RuleResult result = rule.run(value -> "99.11");
        assertTrue(result.status().isPass());

        RuleViolations errors = new RuleViolations();
        result = rule.run(ruleViolations -> errors, value -> "123.456");
        assertTrue(result.status().isFail());
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getViolations().size());
        RuleViolation violation = errors.getViolations().get(0);
        assertEquals(DigitsValidationRule.ERROR_CODE, violation.getErrorCode());
        assertEquals(Severity.ERROR, violation.getSeverity());
        assertEquals("Invalid Number 123.456. Value must have at most 2 integral digits and 2 fraction digits.", violation.getErrorMessage());
    }

    @Test
    public void digitsTest4() {
        Rule rule = Rule.builder().build(new DigitsValidationRule("value", "digitsError1", Severity.FATAL, "Digits Error Message", 5, 2));
        RuleViolations errors = new RuleViolations();
        RuleResult result = rule.run(ruleViolations -> errors, value -> "12345.123");
        assertTrue(result.status().isFail());
        assertTrue(errors.hasSevereErrors());
        assertEquals(1, errors.getViolations().size());
        RuleViolation violation = errors.getViolations().get(0);
        assertEquals("digitsError1", violation.getErrorCode());
        assertEquals(Severity.FATAL, violation.getSeverity());
        assertEquals("Digits Error Message", violation.getErrorMessage());
    }

    @Test
    public void digitsTest5() {
        DigitsValidationRule validationRule = new DigitsValidationRule("value", 2, 2);
        Rule rule = Rule.builder().build(validationRule);
        Assertions.assertThrows(UnrulyException.class, rule::run);
        RuleResult result = rule.run(value -> null);
        assertTrue(result.status().isPass());
    }

    @Test
    public void digitsTest6() {
        DigitsValidationRule validationRule = new DigitsValidationRule("value", 2, 2);
        Rule rule = Rule.builder().build(validationRule);
        RuleResult result = rule.run(value -> Boolean.TRUE);
        assertTrue(result.status().isSkipped());
    }

    @Test
    public void emailTest1() {
        EmailValidationRule validationRule = new EmailValidationRule("value", true, true);
        Rule rule = Rule.builder().build(validationRule);
        RuleResult result = rule.run(value -> "test@test.com");
        assertTrue(result.status().isPass());
        result = rule.run(value -> "test@test.co.uk");
        assertTrue(result.status().isPass());
        result = rule.run(value -> "test.test@test.com");
        assertTrue(result.status().isPass());
        result = rule.run(value -> "test. test@test.com", ruleViolations -> new RuleViolations());
        assertTrue(result.status().isFail());
        result = rule.run(value -> "abctest.com", ruleViolations -> new RuleViolations());
        assertTrue(result.status().isFail());
    }

    @Test
    public void emailTest2() {
        EmailValidationRule validationRule = new EmailValidationRule("value",
                "error.1", Severity.ERROR, "Email Error Message", true, true);
        Rule rule = Rule.builder().build(validationRule);

        RuleResult result = rule.run(value -> "test@test.ca");
        assertTrue(result.status().isPass());

        RuleViolations errors = new RuleViolations();
        result = rule.run(ruleViolations -> errors, value -> "test @hotmail.com");
        assertTrue(result.status().isFail());
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getViolations().size());
        RuleViolation violation = errors.getViolations().get(0);
        assertEquals("error.1", violation.getErrorCode());
        assertEquals(Severity.ERROR, violation.getSeverity());
        assertEquals("Email Error Message", violation.getErrorMessage());
    }

    @Test
    public void emailTest3() {
        Rule rule = Rule.builder().build(new EmailValidationRule("value", true, false));
        RuleResult result = rule.run(value -> "test@google.ca");
        assertTrue(result.status().isPass());

        RuleViolations errors = new RuleViolations();
        result = rule.run(ruleViolations -> errors, value -> "test_test");
        assertTrue(result.status().isFail());
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getViolations().size());
        RuleViolation violation = errors.getViolations().get(0);
        assertEquals(EmailValidationRule.ERROR_CODE, violation.getErrorCode());
        assertEquals(Severity.ERROR, violation.getSeverity());
        assertEquals("Invalid email address test_test.", violation.getErrorMessage());
    }

    @Test
    public void emailTest4() {
        Rule rule = Rule.builder().build(new EmailValidationRule("value", "emailError1", Severity.FATAL, "Email Error Message", false, false));
        RuleViolations errors = new RuleViolations();
        RuleResult result = rule.run(ruleViolations -> errors, value -> "12345.123");
        assertTrue(result.status().isFail());
        assertTrue(errors.hasSevereErrors());
        assertEquals(1, errors.getViolations().size());
        RuleViolation violation = errors.getViolations().get(0);
        assertEquals("emailError1", violation.getErrorCode());
        assertEquals(Severity.FATAL, violation.getSeverity());
        assertEquals("Email Error Message", violation.getErrorMessage());
    }

    @Test
    public void emailTest5() {
        EmailValidationRule validationRule = new EmailValidationRule("value", true, false);
        Rule rule = Rule.builder().build(validationRule);
        Assertions.assertThrows(UnrulyException.class, rule::run);
        RuleResult result = rule.run(value -> null);
        assertTrue(result.status().isPass());
    }

    @Test
    public void emailTest6() {
        EmailValidationRule validationRule = new EmailValidationRule("value", true, true);
        Rule rule = Rule.builder().build(validationRule);
        RuleResult result = rule.run(value -> Boolean.TRUE);
        assertTrue(result.status().isSkipped());
    }

    @Test
    public void endsWithTest1() {
        EndsWithValidationRule validationRule = new EndsWithValidationRule("value", "a", "b", "c");
        Rule rule = Rule.builder().build(validationRule);
        RuleResult result = rule.run(value -> "applea", ruleViolations -> new RuleViolations());
        assertTrue(result.status().isPass());
        result = rule.run(value -> "appleb");
        assertTrue(result.status().isPass());
        result = rule.run(value -> "applec");
        assertTrue(result.status().isPass());
        result = rule.run(value -> "123456", ruleViolations -> new RuleViolations());
        assertTrue(result.status().isFail());
        result = rule.run(value -> "test", ruleViolations -> new RuleViolations());
        assertTrue(result.status().isFail());
    }

    @Test
    public void endsWithTest2() {
        EndsWithValidationRule validationRule = new EndsWithValidationRule("value",
                "error.1", Severity.ERROR, "Ends with Error Message", "test1", "test2");
        Rule rule = Rule.builder().build(validationRule);

        RuleResult result = rule.run(value -> "appletest1");
        assertTrue(result.status().isPass());

        RuleViolations errors = new RuleViolations();
        result = rule.run(ruleViolations -> errors, value -> "apple");
        assertTrue(result.status().isFail());
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getViolations().size());
        RuleViolation violation = errors.getViolations().get(0);
        assertEquals("error.1", violation.getErrorCode());
        assertEquals(Severity.ERROR, violation.getSeverity());
        assertEquals("Ends with Error Message", violation.getErrorMessage());
    }

    @Test
    public void endsWithTest3() {
        EndsWithValidationRule endsWithValidationRule = new EndsWithValidationRule("value", "test1", "test2");
        Rule rule = Rule.builder().build(endsWithValidationRule);
        RuleResult result = rule.run(value -> "test1");
        assertTrue(result.status().isPass());

        RuleViolations errors = new RuleViolations();
        result = rule.run(ruleViolations -> errors, value -> "test_test");
        assertTrue(result.status().isFail());
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getViolations().size());
        RuleViolation violation = errors.getViolations().get(0);
        assertEquals(EndsWithValidationRule.ERROR_CODE, violation.getErrorCode());
        assertEquals(Severity.ERROR, violation.getSeverity());
        assertEquals("Value test_test must end with one of the given suffixes " + Arrays.toString(endsWithValidationRule.getSuffixes()) + ".", violation.getErrorMessage());
    }

    @Test
    public void endsWithTest4() {
        Rule rule = Rule.builder().build(new EndsWithValidationRule("value", "endsWithError1", Severity.FATAL, "Ends With Error Message", "xxx"));
        RuleViolations errors = new RuleViolations();
        RuleResult result = rule.run(ruleViolations -> errors, value -> "12345.123");
        assertTrue(result.status().isFail());
        assertTrue(errors.hasSevereErrors());
        assertEquals(1, errors.getViolations().size());
        RuleViolation violation = errors.getViolations().get(0);
        assertEquals("endsWithError1", violation.getErrorCode());
        assertEquals(Severity.FATAL, violation.getSeverity());
        assertEquals("Ends With Error Message", violation.getErrorMessage());
    }

    @Test
    public void endsWithTest5() {
        EndsWithValidationRule validationRule = new EndsWithValidationRule("value", "yyy");
        Rule rule = Rule.builder().build(validationRule);
        Assertions.assertThrows(UnrulyException.class, rule::run);
        RuleResult result = rule.run(value -> null);
        assertTrue(result.status().isPass());
    }

    @Test
    public void endsWithTest6() {
        EndsWithValidationRule validationRule = new EndsWithValidationRule("value", "zzz");
        Rule rule = Rule.builder().build(validationRule);
        RuleResult result = rule.run(value -> Boolean.TRUE);
        assertTrue(result.status().isSkipped());
    }

    @Test
    public void fileExistsTest1() {
        FileExistsValidationRule validationRule = new FileExistsValidationRule("value");
        Rule rule = Rule.builder().build(validationRule);
        RuleResult result = rule.run(value -> "c:/temp/test.xyz", ruleViolations -> new RuleViolations());
        assertTrue(result.status().isFail());
    }

    @Test
    public void fileExistsTest2() {
        FileExistsValidationRule validationRule = new FileExistsValidationRule("value",
                "error.1", Severity.ERROR, "File Exists with Error Message");
        Rule rule = Rule.builder().build(validationRule);

        RuleViolations errors = new RuleViolations();
        RuleResult result = rule.run(value -> "c:/temp/appletest1.xyz", ruleViolations -> errors);
        assertTrue(result.status().isFail());
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getViolations().size());
        RuleViolation violation = errors.getViolations().get(0);
        assertEquals("error.1", violation.getErrorCode());
        assertEquals(Severity.ERROR, violation.getSeverity());
        assertEquals("File Exists with Error Message", violation.getErrorMessage());
    }

    @Test
    public void fileExistsTest3() {
        FileExistsValidationRule endsWithValidationRule = new FileExistsValidationRule("value");
        Rule rule = Rule.builder().build(endsWithValidationRule);

        RuleViolations errors = new RuleViolations();
        RuleResult result = rule.run(ruleViolations -> errors, value -> "c:/temp/test_test.abc");
        assertTrue(result.status().isFail());
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getViolations().size());
        RuleViolation violation = errors.getViolations().get(0);
        assertEquals(FileExistsValidationRule.ERROR_CODE, violation.getErrorCode());
        assertEquals(Severity.ERROR, violation.getSeverity());
        assertEquals("File c:/temp/test_test.abc does not exist.", violation.getErrorMessage());
    }

    @Test
    public void fileExistsTest4() {
        Rule rule = Rule.builder().build(new FileExistsValidationRule("value", "fileExistsWithError1", Severity.FATAL, "File Exists With Error Message"));
        RuleViolations errors = new RuleViolations();
        RuleResult result = rule.run(ruleViolations -> errors, value -> "d:/temp/xxx.123");
        assertTrue(result.status().isFail());
        assertTrue(errors.hasSevereErrors());
        assertEquals(1, errors.getViolations().size());
        RuleViolation violation = errors.getViolations().get(0);
        assertEquals("fileExistsWithError1", violation.getErrorCode());
        assertEquals(Severity.FATAL, violation.getSeverity());
        assertEquals("File Exists With Error Message", violation.getErrorMessage());
    }

    @Test
    public void fileExistsTest5() {
        FileExistsValidationRule validationRule = new FileExistsValidationRule("value");
        Rule rule = Rule.builder().build(validationRule);
        Assertions.assertThrows(UnrulyException.class, rule::run);
        RuleResult result = rule.run(value -> null);
        assertTrue(result.status().isPass());
    }

    @Test
    public void fileExistsTest6() {
        FileExistsValidationRule validationRule = new FileExistsValidationRule("value");
        Rule rule = Rule.builder().build(validationRule);
        RuleResult result = rule.run(value -> Boolean.TRUE);
        assertTrue(result.status().isSkipped());
    }

}
