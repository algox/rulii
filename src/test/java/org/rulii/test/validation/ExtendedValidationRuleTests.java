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
import org.rulii.rule.Rule;
import org.rulii.rule.RuleResult;
import org.rulii.validation.RuleViolations;
import org.rulii.validation.rules.codepointlength.CodePointLengthValidationRule;
import org.rulii.validation.rules.duration.DurationMaxValidationRule;
import org.rulii.validation.rules.duration.DurationMinValidationRule;
import org.rulii.validation.rules.ean.EanValidationRule;
import org.rulii.validation.rules.isbn.IsbnValidationRule;
import org.rulii.validation.rules.modcheck.LuhnCheckValidationRule;
import org.rulii.validation.rules.modcheck.Mod10CheckValidationRule;
import org.rulii.validation.rules.modcheck.Mod11CheckValidationRule;
import org.rulii.validation.rules.normalized.NormalizedValidationRule;
import org.rulii.validation.rules.unique.UniqueElementsValidationRule;
import org.rulii.validation.rules.uuid.UuidValidationRule;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.rulii.validation.rules.Validators.*;

/**
 * Tests for the validation rules added in 2.2: the constraints Bean Validation users know from other providers,
 * the URL options, and the decimal rules' inclusive parameter.
 *
 * @author Max Arulananthan
 * @since 2.2
 */
public class ExtendedValidationRuleTests {

    public ExtendedValidationRuleTests() {
        super();
    }

    @Test
    public void uniqueElementsValidationRuleTest() {
        Rule rule = uniqueElements(binding("value")).build();
        RuleResult result = rule.run(value -> List.of("a", "b", "c"));
        assertTrue(result.status().isPass());
        result = rule.run(value -> List.of());
        assertTrue(result.status().isPass());
        result = rule.run(value -> new HashSet<>(List.of(1, 2)));
        assertTrue(result.status().isPass());
        RuleViolations violations = new RuleViolations();
        result = rule.run(value -> List.of("a", "b", "a"), ruleViolations -> violations);
        assertTrue(result.status().isFail());
        assertEquals(UniqueElementsValidationRule.ERROR_CODE, violations.getViolation(0).getErrorCode());
        result = rule.run(value -> Arrays.asList(null, null), ruleViolations -> new RuleViolations());
        assertTrue(result.status().isFail());
    }

    @Test
    public void normalizedValidationRuleTest() {
        Rule rule = normalized(binding("value")).build();
        RuleResult result = rule.run(value -> "café");
        assertTrue(result.status().isPass(), "precomposed e-acute is NFC");
        result = rule.run(value -> "");
        assertTrue(result.status().isPass());
        RuleViolations violations = new RuleViolations();
        result = rule.run(value -> "café", ruleViolations -> violations);
        assertTrue(result.status().isFail(), "decomposed e + combining acute is not NFC");
        assertEquals("NFC", violations.getViolation(0).getParameters().get("form"));

        Rule nfd = normalized(binding("value")).form(Normalizer.Form.NFD).build();
        assertTrue(nfd.run(value -> "café").status().isPass());
        assertTrue(nfd.run(value -> "café", ruleViolations -> new RuleViolations()).status().isFail());
        assertThrows(IllegalArgumentException.class, () -> normalized(binding("value")).form(null));
    }

    @Test
    public void codePointLengthValidationRuleTest() {
        Rule rule = codePointLength(binding("value"), 2, 4).build();
        RuleResult result = rule.run(value -> "ab");
        assertTrue(result.status().isPass());
        result = rule.run(value -> "abcd");
        assertTrue(result.status().isPass());
        // Two supplementary characters: four chars, two code points.
        result = rule.run(value -> "😀😀");
        assertTrue(result.status().isPass());
        RuleViolations violations = new RuleViolations();
        result = rule.run(value -> "a", ruleViolations -> violations);
        assertTrue(result.status().isFail());
        assertEquals("2", violations.getViolation(0).getParameters().get("min"));
        assertEquals("4", violations.getViolation(0).getParameters().get("max"));
        result = rule.run(value -> "abcde", ruleViolations -> new RuleViolations());
        assertTrue(result.status().isFail());

        // Normalization before counting: e + combining acute is two code points, one once composed.
        Rule composed = codePointLength(binding("value"), 1, 1).normalization(Normalizer.Form.NFC).build();
        assertTrue(composed.run(value -> "é").status().isPass());
        assertTrue(codePointLength(binding("value"), 1, 1).build().run(value -> "é", ruleViolations -> new RuleViolations()).status().isFail());
        assertThrows(IllegalArgumentException.class, () -> codePointLength(binding("value"), -1, 2).build());
        assertThrows(IllegalArgumentException.class, () -> codePointLength(binding("value"), 3, 2).build());
    }

    @Test
    public void uuidValidationRuleTest() {
        Rule rule = uuid(binding("value")).build();
        assertTrue(rule.run(value -> "123e4567-e89b-12d3-a456-426614174000").status().isPass());
        assertTrue(rule.run(value -> "00000000-0000-0000-0000-000000000000").status().isPass(), "nil allowed by default");
        assertTrue(rule.run(value -> UUID.randomUUID().toString()).status().isPass());
        RuleViolations violations = new RuleViolations();
        assertTrue(rule.run(value -> "123E4567-E89B-12D3-A456-426614174000", ruleViolations -> violations).status().isFail(), "upper case");
        assertEquals(UuidValidationRule.ERROR_CODE, violations.getViolation(0).getErrorCode());
        assertTrue(rule.run(value -> "", ruleViolations -> new RuleViolations()).status().isFail(), "empty not allowed by default");
        assertTrue(rule.run(value -> "123e4567-e89b-12d3-a456-42661417400", ruleViolations -> new RuleViolations()).status().isFail(), "35 chars");
        assertTrue(rule.run(value -> "123e4567-e89b-12d3-a456-42661417400g", ruleViolations -> new RuleViolations()).status().isFail(), "not hex");
        assertTrue(rule.run(value -> "123e4567-e89b-62d3-a456-426614174000", ruleViolations -> new RuleViolations()).status().isFail(), "version 6");
        assertTrue(rule.run(value -> "123e4567-e89b-12d3-e456-426614174000", ruleViolations -> new RuleViolations()).status().isFail(), "variant 3");
        assertTrue(rule.run(value -> "123e4567-e89b-12d3-7456-426614174000").status().isPass(), "variant 0");
        assertTrue(rule.run(value -> "123e4567-e89b-12d3-c456-426614174000").status().isPass(), "variant 2");

        Rule strict = uuid(binding("value")).allowNil(false).allowEmpty(true).versions(4).variants(1)
                .letterCase(UuidValidationRule.LetterCase.UPPER_CASE).build();
        assertTrue(strict.run(value -> "").status().isPass());
        assertTrue(strict.run(value -> "123E4567-E89B-42D3-A456-426614174000").status().isPass());
        assertTrue(strict.run(value -> "00000000-0000-0000-0000-000000000000", ruleViolations -> new RuleViolations()).status().isFail());
        assertTrue(strict.run(value -> "123e4567-e89b-42d3-a456-426614174000", ruleViolations -> new RuleViolations()).status().isFail(), "lower case");
        assertTrue(strict.run(value -> "123E4567-E89B-12D3-A456-426614174000", ruleViolations -> new RuleViolations()).status().isFail(), "version 1");
        assertTrue(uuid(binding("value")).letterCase(UuidValidationRule.LetterCase.INSENSITIVE).build()
                .run(value -> "123E4567-e89b-12d3-A456-426614174000").status().isPass());
        assertThrows(IllegalArgumentException.class, () -> uuid(binding("value")).versions((int[]) null));
        assertThrows(IllegalArgumentException.class, () -> uuid(binding("value")).letterCase(null));
    }

    @Test
    public void decimalRulesReportInclusive() {
        RuleViolations violations = new RuleViolations();
        Rule max = decimalMax(binding("value"), new BigDecimal("10")).inclusive(false).build();
        assertTrue(max.run(value -> new BigDecimal("10"), ruleViolations -> violations).status().isFail());
        assertEquals("false", violations.getViolation(0).getParameters().get("inclusive"));
        assertEquals("10", violations.getViolation(0).getParameters().get("max"));
        assertEquals("Value 10 must not be greater than 10.", violations.getViolation(0).getErrorMessage());

        RuleViolations minViolations = new RuleViolations();
        Rule min = decimalMin(binding("value"), new BigDecimal("10")).build();
        assertTrue(min.run(value -> new BigDecimal("9"), ruleViolations -> minViolations).status().isFail());
        assertEquals("true", minViolations.getViolation(0).getParameters().get("inclusive"));
    }

    @Test
    public void durationMinValidationRuleTest() {
        Rule rule = durationMin(binding("value"), Duration.ofMinutes(5)).build();
        assertTrue(rule.run(value -> Duration.ofMinutes(5)).status().isPass());
        assertTrue(rule.run(value -> Duration.ofHours(1)).status().isPass());
        RuleViolations violations = new RuleViolations();
        assertTrue(rule.run(value -> Duration.ofMinutes(4), ruleViolations -> violations).status().isFail());
        assertEquals(DurationMinValidationRule.ERROR_CODE, violations.getViolation(0).getErrorCode());
        assertEquals("PT5M", violations.getViolation(0).getParameters().get("min"));
        assertEquals("true", violations.getViolation(0).getParameters().get("inclusive"));

        Rule exclusive = durationMin(binding("value"), Duration.ofMinutes(5)).inclusive(false).build();
        assertTrue(exclusive.run(value -> Duration.ofMinutes(5), ruleViolations -> new RuleViolations()).status().isFail());
        assertTrue(exclusive.run(value -> Duration.ofMinutes(5).plusNanos(1)).status().isPass());
        assertThrows(IllegalArgumentException.class, () -> durationMin(binding("value"), null));
    }

    @Test
    public void durationMaxValidationRuleTest() {
        Rule rule = durationMax(binding("value"), Duration.ofMinutes(5)).build();
        assertTrue(rule.run(value -> Duration.ofMinutes(5)).status().isPass());
        assertTrue(rule.run(value -> Duration.ZERO).status().isPass());
        RuleViolations violations = new RuleViolations();
        assertTrue(rule.run(value -> Duration.ofMinutes(6), ruleViolations -> violations).status().isFail());
        assertEquals(DurationMaxValidationRule.ERROR_CODE, violations.getViolation(0).getErrorCode());
        assertEquals("PT5M", violations.getViolation(0).getParameters().get("max"));

        Rule exclusive = durationMax(binding("value"), Duration.ofMinutes(5)).inclusive(false).build();
        assertTrue(exclusive.run(value -> Duration.ofMinutes(5), ruleViolations -> new RuleViolations()).status().isFail());
        assertTrue(exclusive.run(value -> Duration.ofMinutes(4)).status().isPass());
        assertThrows(IllegalArgumentException.class, () -> durationMax(binding("value"), null));
    }

    @Test
    public void luhnCheckValidationRuleTest() {
        Rule rule = luhnCheck(binding("value")).build();
        assertTrue(rule.run(value -> "79927398713").status().isPass());
        assertTrue(rule.run(value -> "4111 1111 1111 1111").status().isPass(), "non-digits ignored by default");
        assertTrue(rule.run(value -> "4111-1111-1111-1111").status().isPass());
        RuleViolations violations = new RuleViolations();
        assertTrue(rule.run(value -> "79927398710", ruleViolations -> violations).status().isFail());
        assertEquals(LuhnCheckValidationRule.ERROR_CODE, violations.getViolation(0).getErrorCode());
        assertTrue(rule.run(value -> "7992739871X", ruleViolations -> new RuleViolations()).status().isFail(), "check character must be a digit");
        assertTrue(rule.run(value -> "", ruleViolations -> new RuleViolations()).status().isFail());
        assertTrue(rule.run(value -> "3", ruleViolations -> new RuleViolations()).status().isFail(), "no digits before the check digit");

        Rule strict = luhnCheck(binding("value")).ignoreNonDigitCharacters(false).build();
        assertTrue(strict.run(value -> "4111 1111 1111 1111", ruleViolations -> new RuleViolations()).status().isFail());
        assertTrue(strict.run(value -> "4111111111111111").status().isPass());

        // A range inside a longer value, and an explicit check digit position.
        Rule ranged = luhnCheck(binding("value")).startIndex(2).endIndex(13).build();
        assertTrue(ranged.run(value -> "xx79927398713yy").status().isPass());
        Rule positioned = luhnCheck(binding("value")).startIndex(0).endIndex(10).checkDigitIndex(10).build();
        assertTrue(positioned.run(value -> "79927398713").status().isPass());
        assertTrue(positioned.run(value -> "79927398710", ruleViolations -> new RuleViolations()).status().isFail());
        assertTrue(ranged.run(value -> "xx", ruleViolations -> new RuleViolations()).status().isFail(), "range out of bounds");
    }

    @Test
    public void mod10CheckValidationRuleTest() {
        Rule rule = mod10Check(binding("value")).build();
        assertTrue(rule.run(value -> "4006381333931").status().isPass(), "EAN-13");
        assertTrue(rule.run(value -> "9780306406157").status().isPass(), "ISBN-13");
        assertTrue(rule.run(value -> "978-0-306-40615-7").status().isPass(), "separators ignored");
        RuleViolations violations = new RuleViolations();
        assertTrue(rule.run(value -> "4006381333932", ruleViolations -> violations).status().isFail());
        assertEquals(Mod10CheckValidationRule.ERROR_CODE, violations.getViolation(0).getErrorCode());
        assertEquals("3", violations.getViolation(0).getParameters().get("multiplier"));
        assertEquals("1", violations.getViolation(0).getParameters().get("weight"));

        // Multiplier and weight swapped: the odd positions from the right now weigh 1.
        Rule swapped = mod10Check(binding("value")).multiplier(1).weight(3).build();
        assertTrue(swapped.run(value -> "4006381333931", ruleViolations -> new RuleViolations()).status().isFail());
        assertTrue(swapped.run(value -> "1234567890128", ruleViolations -> new RuleViolations()).status().isFail());
        assertTrue(mod10Check(binding("value")).ignoreNonDigitCharacters(false).build()
                .run(value -> "978-0-306-40615-7", ruleViolations -> new RuleViolations()).status().isFail());
    }

    @Test
    public void mod11CheckValidationRuleTest() {
        Rule rule = mod11Check(binding("value")).build();
        assertTrue(rule.run(value -> "0306406152").status().isPass(), "ISBN-10");
        assertTrue(rule.run(value -> "080442957X").status().isPass(), "check value 10 written as X");
        assertTrue(rule.run(value -> "00").status().isPass(), "check value 11 written as 0");
        RuleViolations violations = new RuleViolations();
        assertTrue(rule.run(value -> "0306406153", ruleViolations -> violations).status().isFail());
        assertEquals(Mod11CheckValidationRule.ERROR_CODE, violations.getViolation(0).getErrorCode());
        assertTrue(rule.run(value -> "0-306-40615-2", ruleViolations -> new RuleViolations()).status().isFail(), "non-digits fail by default");
        assertTrue(mod11Check(binding("value")).ignoreNonDigitCharacters(true).build().run(value -> "0-306-40615-2").status().isPass());

        assertTrue(mod11Check(binding("value")).build().run(value -> "123455").status().isPass(), "right to left");
        assertTrue(mod11Check(binding("value")).processingDirection(Mod11CheckValidationRule.ProcessingDirection.LEFT_TO_RIGHT).build()
                .run(value -> "123457").status().isPass(), "left to right");
        assertTrue(mod11Check(binding("value")).threshold(3).build().run(value -> "123458").status().isPass(), "weights start over past the threshold");
        assertTrue(mod11Check(binding("value")).treatCheck10As('A').build().run(value -> "080442957A").status().isPass());
        assertTrue(mod11Check(binding("value")).treatCheck10As('A').build().run(value -> "080442957X", ruleViolations -> new RuleViolations()).status().isFail());
        assertThrows(IllegalArgumentException.class, () -> mod11Check(binding("value")).processingDirection(null));
    }

    @Test
    public void eanValidationRuleTest() {
        Rule rule = ean(binding("value")).build();
        assertTrue(rule.run(value -> "4006381333931").status().isPass());
        RuleViolations violations = new RuleViolations();
        assertTrue(rule.run(value -> "4006381333932", ruleViolations -> violations).status().isFail());
        assertEquals(EanValidationRule.ERROR_CODE, violations.getViolation(0).getErrorCode());
        assertEquals("EAN13", violations.getViolation(0).getParameters().get("type"));
        assertTrue(rule.run(value -> "73513537", ruleViolations -> new RuleViolations()).status().isFail(), "wrong length");
        assertTrue(rule.run(value -> "400638133393a", ruleViolations -> new RuleViolations()).status().isFail(), "not a digit");

        Rule ean8 = ean(binding("value")).type(EanValidationRule.Type.EAN8).build();
        assertTrue(ean8.run(value -> "73513537").status().isPass());
        assertTrue(ean8.run(value -> "73513538", ruleViolations -> new RuleViolations()).status().isFail());
        assertTrue(ean8.run(value -> "4006381333931", ruleViolations -> new RuleViolations()).status().isFail());
        assertThrows(IllegalArgumentException.class, () -> ean(binding("value")).type(null));
    }

    @Test
    public void isbnValidationRuleTest() {
        Rule rule = isbn(binding("value")).build();
        assertTrue(rule.run(value -> "9780306406157").status().isPass());
        assertTrue(rule.run(value -> "978-0-306-40615-7").status().isPass(), "separators ignored");
        RuleViolations violations = new RuleViolations();
        assertTrue(rule.run(value -> "9780306406158", ruleViolations -> violations).status().isFail());
        assertEquals(IsbnValidationRule.ERROR_CODE, violations.getViolation(0).getErrorCode());
        assertEquals("ISBN_13", violations.getViolation(0).getParameters().get("type"));
        assertTrue(rule.run(value -> "0306406152", ruleViolations -> new RuleViolations()).status().isFail(), "an ISBN-10 is not an ISBN-13");

        Rule isbn10 = isbn(binding("value")).type(IsbnValidationRule.Type.ISBN_10).build();
        assertTrue(isbn10.run(value -> "0306406152").status().isPass());
        assertTrue(isbn10.run(value -> "0-306-40615-2").status().isPass());
        assertTrue(isbn10.run(value -> "0-8044-2957-X").status().isPass());
        assertTrue(isbn10.run(value -> "0-8044-2957-x").status().isPass(), "lower case x");
        assertTrue(isbn10.run(value -> "0306406153", ruleViolations -> new RuleViolations()).status().isFail());
        assertTrue(isbn10.run(value -> "X306406152", ruleViolations -> new RuleViolations()).status().isFail(), "X only in the last position");
        assertTrue(isbn10.run(value -> "9780306406157", ruleViolations -> new RuleViolations()).status().isFail());

        Rule any = isbn(binding("value")).type(IsbnValidationRule.Type.ANY).build();
        assertTrue(any.run(value -> "0306406152").status().isPass());
        assertTrue(any.run(value -> "9780306406157").status().isPass());
        assertTrue(any.run(value -> "978030640615", ruleViolations -> new RuleViolations()).status().isFail(), "twelve digits");
        assertThrows(IllegalArgumentException.class, () -> isbn(binding("value")).type(null));
    }

    @Test
    public void urlValidationRuleOptionsTest() {
        assertTrue(url(binding("value")).protocol("https").build().run(value -> "https://www.google.com").status().isPass());
        RuleViolations violations = new RuleViolations();
        assertTrue(url(binding("value")).protocol("https").build().run(value -> "http://www.google.com", ruleViolations -> violations).status().isFail());
        assertEquals("https", violations.getViolation(0).getParameters().get("protocol"));
        assertTrue(url(binding("value")).host("www.google.com").build().run(value -> "http://www.google.com/search").status().isPass());
        assertTrue(url(binding("value")).host("www.google.com").build().run(value -> "http://www.apple.com", ruleViolations -> new RuleViolations()).status().isFail());
        assertTrue(url(binding("value")).port(8080).build().run(value -> "http://www.google.com:8080/x").status().isPass());
        assertTrue(url(binding("value")).port(8080).build().run(value -> "http://www.google.com/x", ruleViolations -> new RuleViolations()).status().isFail());
        assertTrue(url(binding("value")).regexp(".*google.*").build().run(value -> "http://www.google.com").status().isPass());
        assertTrue(url(binding("value")).regexp(".*apple.*").build().run(value -> "http://www.google.com", ruleViolations -> new RuleViolations()).status().isFail());
        assertTrue(url(binding("value")).regexp(".*GOOGLE.*").flags(java.util.regex.Pattern.CASE_INSENSITIVE).build().run(value -> "http://www.google.com").status().isPass());
        assertTrue(url(binding("value")).protocol("").host("").port(-1).regexp("").build().run(value -> "http://www.google.com").status().isPass(), "empty options check nothing");
        assertTrue(url(binding("value")).protocol("http").build().run(value -> "abc", ruleViolations -> new RuleViolations()).status().isFail());
    }
}
