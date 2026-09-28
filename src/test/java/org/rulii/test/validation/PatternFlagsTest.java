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
import org.rulii.validation.rules.pattern.PatternValidationRule;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.rulii.validation.rules.Validators.*;

/**
 * Tests for regex flag support on {@link PatternValidationRule}: {@code flags(int)} with
 * {@link java.util.regex.Pattern} constants, interaction with the caseSensitive
 * convenience, and preservation of the entire-input match semantics.
 *
 * @author Max Arulananthan
 * @since 2.1
 */
public class PatternFlagsTest {

    public PatternFlagsTest() {
        super();
    }

    // -----------------------------------------------------------------------
    // Existing behavior preserved
    // -----------------------------------------------------------------------

    @Test
    public void caseSensitiveByDefault() {
        Rule rule = pattern(binding("code"), "[a-z]+").build();
        RuleViolations errors = new RuleViolations();

        assertTrue(rule.run(code -> "abc").status().isPass());
        assertTrue(rule.run(ruleViolations -> errors, code -> "ABC").status().isFail());
    }

    @Test
    public void caseSensitiveConveniencePreserved() {
        Rule rule = pattern(binding("code"), "[a-z]+").caseSensitive(false).build();

        assertTrue(rule.run(code -> "ABC").status().isPass());
        assertEquals(Pattern.CASE_INSENSITIVE,
                ((PatternValidationRule) rule.getTarget()).getFlags());
    }

    @Test
    public void entireInputMatchSemanticsPreserved() {
        // The pattern matches against the entire input, not a substring.
        Rule rule = pattern(binding("code"), "b").build();
        RuleViolations errors = new RuleViolations();
        assertTrue(rule.run(ruleViolations -> errors, code -> "abc").status().isFail());
    }

    // -----------------------------------------------------------------------
    // flags(int)
    // -----------------------------------------------------------------------

    @Test
    public void caseInsensitiveViaFlags() {
        Rule rule = pattern(binding("code"), "[a-z]+")
                .flags(Pattern.CASE_INSENSITIVE)
                .build();

        assertTrue(rule.run(code -> "ABC").status().isPass());
    }

    @Test
    public void dotallFlag() {
        Rule withoutFlag = pattern(binding("text"), "a.b").build();
        RuleViolations errors = new RuleViolations();
        assertTrue(withoutFlag.run(ruleViolations -> errors, text -> "a\nb").status().isFail());

        Rule withFlag = pattern(binding("text"), "a.b").flags(Pattern.DOTALL).build();
        assertTrue(withFlag.run(text -> "a\nb").status().isPass());
    }

    @Test
    public void combinedFlags() {
        Rule rule = pattern(binding("text"), "a.b")
                .flags(Pattern.DOTALL | Pattern.CASE_INSENSITIVE)
                .build();

        assertTrue(rule.run(text -> "A\nB").status().isPass());
    }

    @Test
    public void flagsCombineWithCaseSensitiveConvenience() {
        Rule rule = pattern(binding("text"), "a.b")
                .caseSensitive(false)
                .flags(Pattern.DOTALL)
                .build();

        RuleResult result = rule.run(text -> "A\nB");
        assertTrue(result.status().isPass());
        assertEquals(Pattern.DOTALL | Pattern.CASE_INSENSITIVE,
                ((PatternValidationRule) rule.getTarget()).getFlags());
    }

    @Test
    public void commentsFlag() {
        Rule rule = pattern(binding("code"), "[a-z]+  # letters only")
                .flags(Pattern.COMMENTS)
                .build();

        assertTrue(rule.run(code -> "abc").status().isPass());
    }
}
