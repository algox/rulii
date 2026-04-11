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
package org.rulii.test.validation;

import org.junit.jupiter.api.Test;
import org.rulii.model.UnrulyException;
import org.rulii.model.function.Function;
import org.rulii.rule.Rule;
import org.rulii.rule.RuleResult;
import org.rulii.validation.BindingFunction;
import org.rulii.validation.RuleViolation;
import org.rulii.validation.RuleViolations;
import org.rulii.validation.Severity;
import org.rulii.validation.rules.Validators;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.rulii.model.function.Functions.function;
import static org.rulii.validation.rules.Validators.*;

/**
 * Tests for the {@link Validators} factory class, specifically covering the three
 * value-source strategies: {@code binding(name)}, {@code value(constant)}, and
 * custom {@code Functions.function(lambda)}.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class ValidatorsTests {

    public ValidatorsTests() {
        super();
    }

    // -----------------------------------------------------------------------
    // binding(String name) factory
    // -----------------------------------------------------------------------

    @Test
    public void bindingReturnsBingFunction() {
        Function<?> fn = binding("age");
        assertInstanceOf(BindingFunction.class, fn);
        assertEquals("age", ((BindingFunction<?>) fn).getBindingName());
    }

    @Test
    public void bindingNamePropagatedToViolationParam() {
        Rule rule = notNull(binding("username")).build();
        RuleViolations errors = new RuleViolations();

        // null value → violation; the param key must equal the binding name
        RuleResult result = rule.run(ruleViolations -> errors, username -> (Object) null);
        assertTrue(result.status().isFail());
        assertEquals(1, errors.getViolations().size());
        RuleViolation v = errors.getViolations().get(0);
        assertTrue(v.getParameters().containsKey("username"),
                "Violation param should be keyed by binding name 'username'");
    }

    @Test
    public void bindingMissingFromContextIsSkipped() {
        // No binding provided → NoSuchBindingException propagates → UnrulyException
        Rule rule = notNull(binding("missingField")).build();
        assertThrows(UnrulyException.class, () -> rule.run());
    }

    @Test
    public void bindingResolvedAtRuntime() {
        Rule rule = notNull(binding("email")).build();

        // First run: value present and non-null → PASS
        RuleResult pass = rule.run(email -> "user@example.com");
        assertTrue(pass.status().isPass());

        // Second run: value present but null → FAIL
        RuleViolations errors = new RuleViolations();
        RuleResult fail = rule.run(ruleViolations -> errors, email -> (Object) null);
        assertTrue(fail.status().isFail());
    }

    @Test
    public void bindingWithMinRule() {
        Rule rule = min(binding("age"), 18L).build();

        RuleResult pass = rule.run(age -> 21);
        assertTrue(pass.status().isPass());

        RuleViolations errors = new RuleViolations();
        RuleResult fail = rule.run(ruleViolations -> errors, age -> 16);
        assertTrue(fail.status().isFail());
        assertTrue(errors.hasErrors());
        RuleViolation v = errors.getViolations().get(0);
        assertTrue(v.getParameters().containsKey("age"),
                "Violation param should be keyed by binding name 'age'");
    }

    @Test
    public void bindingWithMaxRule() {
        Rule rule = max(binding("score"), 100L).build();

        assertTrue(rule.run(score -> 99).status().isPass());
        assertTrue(rule.run(score -> 100).status().isPass());

        RuleViolations errors = new RuleViolations();
        assertTrue(rule.run(ruleViolations -> errors, score -> 101).status().isFail());
        assertFalse(errors.getViolations().isEmpty());
    }

    @Test
    public void bindingWithNotBlankRule() {
        Rule rule = notBlank(binding("name")).build();

        assertTrue(rule.run(name -> "Alice").status().isPass());

        RuleViolations errors = new RuleViolations();
        assertTrue(rule.run(ruleViolations -> errors, name -> "  ").status().isFail());
        assertTrue(errors.hasErrors());
    }

    @Test
    public void bindingWithAlphaRule() {
        Rule rule = alpha(binding("code")).build();

        assertTrue(rule.run(code -> "ABC").status().isPass());

        RuleViolations errors = new RuleViolations();
        assertTrue(rule.run(ruleViolations -> errors, code -> "AB1").status().isFail());
        RuleViolation v = errors.getViolations().get(0);
        assertTrue(v.getParameters().containsKey("code"));
    }

    @Test
    public void bindingWithPositiveRule() {
        Rule rule = positive(binding("amount")).build();

        assertTrue(rule.run(amount -> 5).status().isPass());

        RuleViolations errors = new RuleViolations();
        assertTrue(rule.run(ruleViolations -> errors, amount -> -1).status().isFail());
    }

    @Test
    public void bindingWithNegativeRule() {
        Rule rule = negative(binding("delta")).build();

        assertTrue(rule.run(delta -> -3).status().isPass());

        RuleViolations errors = new RuleViolations();
        assertTrue(rule.run(ruleViolations -> errors, delta -> 0).status().isFail());
    }

    @Test
    public void bindingWithSizeRule() {
        Rule rule = size(binding("tags"), 1, 3).build();

        assertTrue(rule.run(tags -> List.of("a", "b")).status().isPass());

        RuleViolations errors = new RuleViolations();
        assertTrue(rule.run(ruleViolations -> errors, tags -> List.of("a", "b", "c", "d")).status().isFail());
    }

    @Test
    public void bindingWithInRule() {
        Rule rule = in(binding("status"), Arrays.asList("ACTIVE", "INACTIVE")).build();

        assertTrue(rule.run(status -> "ACTIVE").status().isPass());

        RuleViolations errors = new RuleViolations();
        assertTrue(rule.run(ruleViolations -> errors, status -> "PENDING").status().isFail());
    }

    @Test
    public void bindingWithPatternRule() {
        Rule rule = pattern(binding("zip"), "\\d{5}").build();

        assertTrue(rule.run(zip -> "12345").status().isPass());

        RuleViolations errors = new RuleViolations();
        assertTrue(rule.run(ruleViolations -> errors, zip -> "1234").status().isFail());
    }

    @Test
    public void bindingWithEmailRule() {
        Rule rule = email(binding("address")).build();

        assertTrue(rule.run(address -> "test@example.com").status().isPass());

        RuleViolations errors = new RuleViolations();
        assertTrue(rule.run(ruleViolations -> errors, address -> "not-an-email").status().isFail());
    }

    @Test
    public void bindingWithDecimalMinMax() {
        Rule minRule = decimalMin(binding("price"), new BigDecimal("0.01")).build();
        Rule maxRule = decimalMax(binding("price"), new BigDecimal("999.99")).build();

        assertTrue(minRule.run(price -> new BigDecimal("1.00")).status().isPass());
        assertTrue(maxRule.run(price -> new BigDecimal("999.99")).status().isPass());

        RuleViolations errors = new RuleViolations();
        assertTrue(minRule.run(ruleViolations -> errors, price -> new BigDecimal("0.00")).status().isFail());
        assertTrue(errors.hasErrors());
    }

    @Test
    public void bindingWithEqualsRule() {
        Rule rule = Validators.assertEquals(binding("role"), "ADMIN").build();

        assertTrue(rule.run(role -> "ADMIN").status().isPass());

        RuleViolations errors = new RuleViolations();
        assertTrue(rule.run(ruleViolations -> errors, role -> "USER").status().isFail());
    }

    @Test
    public void bindingWithAssertNotEqualsRule() {
        Rule rule = assertNotEquals(binding("status"), "BANNED").build();

        assertTrue(rule.run(status -> "ACTIVE").status().isPass());

        RuleViolations errors = new RuleViolations();
        assertTrue(rule.run(ruleViolations -> errors, status -> "BANNED").status().isFail());
    }

    @Test
    public void bindingWithAssertTrueRule() {
        Rule rule = Validators.assertTrue(binding("enabled")).build();

        assertTrue(rule.run(enabled -> Boolean.TRUE).status().isPass());

        RuleViolations errors = new RuleViolations();
        assertTrue(rule.run(ruleViolations -> errors, enabled -> Boolean.FALSE).status().isFail());
    }

    @Test
    public void bindingWithAssertFalseRule() {
        Rule rule = Validators.assertFalse(binding("locked")).build();

        assertTrue(rule.run(locked -> Boolean.FALSE).status().isPass());

        RuleViolations errors = new RuleViolations();
        assertTrue(rule.run(ruleViolations -> errors, locked -> Boolean.TRUE).status().isFail());
    }

    @Test
    public void bindingWithPastRule() {
        Rule rule = past(binding("birthDate")).build();

        assertTrue(rule.run(birthDate -> LocalDate.now().minusDays(1)).status().isPass());

        RuleViolations errors = new RuleViolations();
        assertTrue(rule.run(ruleViolations -> errors, birthDate -> LocalDate.now().plusDays(1)).status().isFail());
    }

    @Test
    public void bindingWithFutureRule() {
        Rule rule = future(binding("expiryDate")).build();

        assertTrue(rule.run(expiryDate -> LocalDate.now().plusDays(1)).status().isPass());

        RuleViolations errors = new RuleViolations();
        assertTrue(rule.run(ruleViolations -> errors, expiryDate -> LocalDate.now().minusDays(1)).status().isFail());
    }

    @Test
    public void bindingWithStartsWithRule() {
        Rule rule = startsWith(binding("prefix"), "http://", "https://").build();

        assertTrue(rule.run(prefix -> "https://example.com").status().isPass());

        RuleViolations errors = new RuleViolations();
        assertTrue(rule.run(ruleViolations -> errors, prefix -> "ftp://example.com").status().isFail());
    }

    @Test
    public void bindingWithEndsWithRule() {
        Rule rule = endsWith(binding("fileName"), ".pdf", ".docx").build();

        assertTrue(rule.run(fileName -> "report.pdf").status().isPass());

        RuleViolations errors = new RuleViolations();
        assertTrue(rule.run(ruleViolations -> errors, fileName -> "report.txt").status().isFail());
    }

    @Test
    public void bindingWithUpperCaseRule() {
        Rule rule = upperCase(binding("acronym")).build();

        assertTrue(rule.run(acronym -> "NASA").status().isPass());

        RuleViolations errors = new RuleViolations();
        assertTrue(rule.run(ruleViolations -> errors, acronym -> "nasa").status().isFail());
    }

    @Test
    public void bindingWithLowerCaseRule() {
        Rule rule = lowerCase(binding("slug")).build();

        assertTrue(rule.run(slug -> "helloworld").status().isPass());

        RuleViolations errors = new RuleViolations();
        assertTrue(rule.run(ruleViolations -> errors, slug -> "HelloWorld").status().isFail());
    }

    @Test
    public void bindingWithNullRule() {
        Rule rule = isNull(binding("deletedAt")).build();

        assertTrue(rule.run(deletedAt -> (Object) null).status().isPass());

        RuleViolations errors = new RuleViolations();
        assertTrue(rule.run(ruleViolations -> errors, deletedAt -> "2026-01-01").status().isFail());
    }

    @Test
    public void bindingWithNegativeOrZeroRule() {
        Rule rule = negativeOrZero(binding("balance")).build();

        assertTrue(rule.run(balance -> 0).status().isPass());
        assertTrue(rule.run(balance -> -5).status().isPass());

        RuleViolations errors = new RuleViolations();
        assertTrue(rule.run(ruleViolations -> errors, balance -> 1).status().isFail());
    }

    @Test
    public void bindingWithPositiveOrZeroRule() {
        Rule rule = positiveOrZero(binding("quantity")).build();

        assertTrue(rule.run(quantity -> 0).status().isPass());
        assertTrue(rule.run(quantity -> 10).status().isPass());

        RuleViolations errors = new RuleViolations();
        assertTrue(rule.run(ruleViolations -> errors, quantity -> -1).status().isFail());
    }

    @Test
    public void bindingWithDigitsRule() {
        Rule rule = digits(binding("taxId"), 3, 2).build();

        assertTrue(rule.run(taxId -> new BigDecimal("123.45")).status().isPass());

        RuleViolations errors = new RuleViolations();
        assertTrue(rule.run(ruleViolations -> errors, taxId -> new BigDecimal("1234.56")).status().isFail());
    }

    // -----------------------------------------------------------------------
    // value(Object constant) factory
    // -----------------------------------------------------------------------

    @Test
    public void valueFactoryReturnsConstant() {
        // value() wraps in a plain Function (not BindingFunction)
        Function<?> fn = value("hello");
        assertFalse(fn instanceof BindingFunction,
                "value() should not produce a BindingFunction");
    }

    @Test
    public void valueWithNotNullPassesNoBindingsNeeded() {
        // value() provides the constant; no binding required in context
        Rule rule = notNull(value("hello")).build();
        RuleResult result = rule.run(); // completely empty context
        assertTrue(result.status().isPass());
    }

    @Test
    public void valueNullFailsWithNoBindingsNeeded() {
        Rule rule = notNull(value(null)).build();
        RuleViolations errors = new RuleViolations();
        RuleResult result = rule.run(ruleViolations -> errors);
        assertTrue(result.status().isFail());
        assertTrue(errors.hasErrors());
    }

    @Test
    public void valueDefaultParamNameIsValue() {
        Rule rule = notNull(value(null)).build();
        RuleViolations errors = new RuleViolations();
        rule.run(ruleViolations -> errors);
        RuleViolation v = errors.getViolations().get(0);
        assertTrue(v.getParameters().containsKey("value"),
                "value() should use 'value' as the default param name in violations");
    }

    @Test
    public void valueWithMinRule() {
        Rule rule = min(value(25L), 18L).build();
        assertTrue(rule.run().status().isPass());

        Rule failRule = min(value(10L), 18L).build();
        RuleViolations errors = new RuleViolations();
        assertTrue(failRule.run(ruleViolations -> errors).status().isFail());
    }

    @Test
    public void valueWithNotBlankRule() {
        Rule passRule = notBlank(value("hello")).build();
        assertTrue(passRule.run().status().isPass());

        Rule failRule = notBlank(value("")).build();
        RuleViolations errors = new RuleViolations();
        assertTrue(failRule.run(ruleViolations -> errors).status().isFail());
    }

    @Test
    public void valueWithAlphaNumericRule() {
        Rule passRule = alphaNumeric(value("abc123")).build();
        assertTrue(passRule.run().status().isPass());

        Rule failRule = alphaNumeric(value("abc!")).build();
        RuleViolations errors = new RuleViolations();
        assertTrue(failRule.run(ruleViolations -> errors).status().isFail());
    }

    @Test
    public void valueWithSizeRule() {
        Rule passRule = size(value(List.of("a", "b")), 1, 3).build();
        assertTrue(passRule.run().status().isPass());

        Rule failRule = size(value(List.of()), 1, 3).build();
        RuleViolations errors = new RuleViolations();
        assertTrue(failRule.run(ruleViolations -> errors).status().isFail());
    }

    @Test
    public void valueWithEqualsRule() {
        Rule passRule = Validators.assertEquals(value("ADMIN"), "ADMIN").build();
        assertTrue(passRule.run().status().isPass());

        Rule failRule = Validators.assertEquals(value("USER"), "ADMIN").build();
        RuleViolations errors = new RuleViolations();
        assertTrue(failRule.run(ruleViolations -> errors).status().isFail());
    }

    @Test
    public void valueWithInRule() {
        Rule passRule = in(value("RED"), Arrays.asList("RED", "GREEN", "BLUE")).build();
        assertTrue(passRule.run().status().isPass());

        Rule failRule = in(value("YELLOW"), Arrays.asList("RED", "GREEN", "BLUE")).build();
        RuleViolations errors = new RuleViolations();
        assertTrue(failRule.run(ruleViolations -> errors).status().isFail());
    }

    @Test
    public void valueWithPatternRule() {
        Rule passRule = pattern(value("ABC-123"), "[A-Z]{3}-\\d{3}").build();
        assertTrue(passRule.run().status().isPass());

        Rule failRule = pattern(value("abc-123"), "[A-Z]{3}-\\d{3}").build();
        RuleViolations errors = new RuleViolations();
        assertTrue(failRule.run(ruleViolations -> errors).status().isFail());
    }

    @Test
    public void valueWithDecimalRule() {
        Rule passRule = decimal(value("3.14")).build();
        assertTrue(passRule.run().status().isPass());

        Rule failRule = decimal(value("notANumber")).build();
        RuleViolations errors = new RuleViolations();
        assertTrue(failRule.run(ruleViolations -> errors).status().isFail());
    }

    @Test
    public void valueWithNullIsPassedAsConstant() {
        // A value(null) evaluates to null; notNull should fail
        Rule rule = notNull(value(null)).build();
        RuleViolations errors = new RuleViolations();
        assertTrue(rule.run(ruleViolations -> errors).status().isFail());
    }

    // -----------------------------------------------------------------------
    // Custom Functions.function(lambda) factory
    // -----------------------------------------------------------------------

    @Test
    public void customFunctionNotBingFunction() {
        Function<?> fn = function(() -> "computed");
        assertFalse(fn instanceof BindingFunction,
                "Functions.function() should not produce a BindingFunction");
    }

    @Test
    public void customFunctionWithNotNullPass() {
        String computed = "dynamic value";
        Rule rule = notNull(function(() -> computed)).build();
        assertTrue(rule.run().status().isPass());
    }

    @Test
    public void customFunctionWithNotNullFail() {
        Rule rule = notNull(function(() -> (Object) null)).build();
        RuleViolations errors = new RuleViolations();
        assertTrue(rule.run(ruleViolations -> errors).status().isFail());
    }

    @Test
    public void customFunctionDefaultParamNameIsValue() {
        Rule rule = notNull(function(() -> (Object) null)).build();
        RuleViolations errors = new RuleViolations();
        rule.run(ruleViolations -> errors);
        RuleViolation v = errors.getViolations().get(0);
        assertTrue(v.getParameters().containsKey("value"),
                "Functions.function() should use 'value' as the default param name in violations");
    }

    @Test
    public void customFunctionWithAlpha() {
        Rule passRule = alpha(function(() -> "hello")).build();
        assertTrue(passRule.run().status().isPass());

        Rule failRule = alpha(function(() -> "hello1")).build();
        RuleViolations errors = new RuleViolations();
        assertTrue(failRule.run(ruleViolations -> errors).status().isFail());
    }

    @Test
    public void customFunctionWithMin() {
        Rule rule = min(function(() -> 20), 18L).build();
        assertTrue(rule.run().status().isPass());

        Rule failRule = min(function(() -> 15), 18L).build();
        RuleViolations errors = new RuleViolations();
        assertTrue(failRule.run(ruleViolations -> errors).status().isFail());
    }

    @Test
    public void customFunctionWithDynamicComputation() {
        int[] counter = {0};
        // lambda increments each call — proves the function is called at run-time
        Rule rule = min(function(() -> ++counter[0]), 1L).build();

        assertTrue(rule.run().status().isPass()); // counter = 1,2 → passes min(1)
        assertTrue(rule.run().status().isPass()); // counter = 3,4 → still passes
        // function is called twice per run: once in checkType, once in isValid
        assertEquals(4, counter[0]);
    }

    @Test
    public void customFunctionWithPositive() {
        Rule passRule = positive(function(() -> 42)).build();
        assertTrue(passRule.run().status().isPass());

        Rule failRule = positive(function(() -> -1)).build();
        RuleViolations errors = new RuleViolations();
        assertTrue(failRule.run(ruleViolations -> errors).status().isFail());
    }

    @Test
    public void customFunctionWithEmailRule() {
        Rule passRule = email(function(() -> "test@domain.com")).build();
        assertTrue(passRule.run().status().isPass());

        Rule failRule = email(function(() -> "bad-email")).build();
        RuleViolations errors = new RuleViolations();
        assertTrue(failRule.run(ruleViolations -> errors).status().isFail());
    }

    // -----------------------------------------------------------------------
    // Mixed scenarios and builder fluent API
    // -----------------------------------------------------------------------

    @Test
    public void builderFluentApiWithBinding() {
        Rule rule = notNull(binding("token"))
                .errorCode("token.required")
                .severity(Severity.FATAL)
                .message("Token must not be null")
                .build();

        RuleViolations errors = new RuleViolations();
        RuleResult result = rule.run(ruleViolations -> errors, token -> (Object) null);
        assertTrue(result.status().isFail());

        RuleViolation v = errors.getViolations().get(0);
        assertEquals("token.required", v.getErrorCode());
        assertEquals(Severity.FATAL, v.getSeverity());
        assertEquals("Token must not be null", v.getErrorMessage());
        assertTrue(v.getParameters().containsKey("token"));
    }

    @Test
    public void builderFluentApiWithValue() {
        Rule rule = min(value(5L), 10L)
                .errorCode("min.violated")
                .severity(Severity.WARNING)
                .message("Value too small")
                .build();

        RuleViolations errors = new RuleViolations();
        rule.run(ruleViolations -> errors);
        RuleViolation v = errors.getViolations().get(0);
        assertEquals("min.violated", v.getErrorCode());
        assertEquals(Severity.WARNING, v.getSeverity());
        assertEquals("Value too small", v.getErrorMessage());
    }

    @Test
    public void builderFluentApiWithFunction() {
        Rule rule = notBlank(function(() -> ""))
                .errorCode("blank.error")
                .severity(Severity.ERROR)
                .message("Must not be blank")
                .build();

        RuleViolations errors = new RuleViolations();
        rule.run(ruleViolations -> errors);
        RuleViolation v = errors.getViolations().get(0);
        assertEquals("blank.error", v.getErrorCode());
        assertEquals(Severity.ERROR, v.getSeverity());
        assertEquals("Must not be blank", v.getErrorMessage());
    }

    @Test
    public void bindingNullValueFailsForNonNullableRules() {
        // Rules that treat null as invalid — alpha fails on null
        Rule alphaRule = alpha(binding("optional")).build();
        RuleViolations errors = new RuleViolations();
        RuleResult result = alphaRule.run(ruleViolations -> errors, optional -> (Object) null);
        assertTrue(result.status().isFail());
    }

    @Test
    public void valueNullFailsNonNullableRules() {
        // alpha treats null as invalid
        Rule rule = alpha(value(null)).build();
        RuleViolations errors = new RuleViolations();
        assertTrue(rule.run(ruleViolations -> errors).status().isFail());
    }

    @Test
    public void bindingWrongTypeIsSkipped() {
        // min() supports Number and CharSequence; Boolean is neither → SKIP
        Rule rule = min(binding("value"), 10L).build();
        RuleResult result = rule.run(value -> Boolean.TRUE);
        assertTrue(result.status().isSkipped());
    }

    @Test
    public void valueWrongTypeIsSkipped() {
        // alpha() supports CharSequence; Integer is not CharSequence → SKIP
        Rule rule = alpha(value(42)).build();
        RuleResult result = rule.run();
        assertTrue(result.status().isSkipped());
    }

    @Test
    public void multipleBindingRulesIndependent() {
        Rule nameRule  = notBlank(binding("name")).build();
        Rule emailRule = email(binding("email")).build();
        Rule ageRule   = min(binding("age"), 18L).build();

        assertTrue(nameRule.run(name -> "Alice").status().isPass());
        assertTrue(emailRule.run(email -> "alice@example.com").status().isPass());
        assertTrue(ageRule.run(age -> 20).status().isPass());

        RuleViolations errors = new RuleViolations();
        assertTrue(nameRule.run(ruleViolations -> errors, name -> "").status().isFail());
        assertTrue(emailRule.run(ruleViolations -> errors, email -> "not-valid").status().isFail());
        assertTrue(ageRule.run(ruleViolations -> errors, age -> 16).status().isFail());
        assertEquals(3, errors.getViolations().size());
    }
}
