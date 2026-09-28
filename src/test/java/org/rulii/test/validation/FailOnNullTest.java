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
import org.rulii.validation.ValueValidationRule;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.rulii.validation.rules.Validators.*;

/**
 * Tests for the {@code failOnNull} option on {@link ValueValidationRule}: default null
 * handling preserved, null skipping via the precondition, and the presence-rule guards
 * (notNull/notBlank/notEmpty reject {@code failOnNull(false)}).
 *
 * @author Max Arulananthan
 * @since 2.1
 */
public class FailOnNullTest {

    public FailOnNullTest() {
        super();
    }

    // -----------------------------------------------------------------------
    // Default behavior (failOnNull = true) unchanged
    // -----------------------------------------------------------------------

    @Test
    public void nullValueFailsByDefault() {
        Rule rule = size(binding("tags"), 1, 3).build();
        RuleViolations errors = new RuleViolations();

        RuleResult result = rule.run(ruleViolations -> errors, tags -> (Object) null);
        assertTrue(result.status().isFail());
        assertTrue(errors.hasErrors());
    }

    @Test
    public void ruleExposesFailOnNullDefault() {
        Rule rule = size(binding("tags"), 1, 3).build();
        assertTrue(((ValueValidationRule) rule.getTarget()).isFailOnNull());
    }

    // -----------------------------------------------------------------------
    // failOnNull(false) — null skips via the precondition
    // -----------------------------------------------------------------------

    @Test
    public void failOnNullFalseSkipsNullValue() {
        Rule rule = size(binding("tags"), 1, 3).failOnNull(false).build();
        RuleViolations errors = new RuleViolations();

        RuleResult result = rule.run(ruleViolations -> errors, tags -> (Object) null);
        assertTrue(result.status().isSkipped());
        assertFalse(errors.hasErrors());
        assertFalse(((ValueValidationRule) rule.getTarget()).isFailOnNull());
    }

    @Test
    public void failOnNullFalseStillValidatesNonNullValues() {
        Rule rule = size(binding("tags"), 1, 3).failOnNull(false).build();
        RuleViolations errors = new RuleViolations();

        RuleResult fail = rule.run(ruleViolations -> errors, tags -> List.of());
        assertTrue(fail.status().isFail());
        assertTrue(errors.hasErrors());

        RuleResult pass = rule.run(tags -> List.of("a", "b"));
        assertTrue(pass.status().isPass());
    }

    @Test
    public void failOnNullFalseWorksOnMinRule() {
        Rule rule = min(binding("age"), 18L).failOnNull(false).build();

        RuleResult skipped = rule.run(age -> (Object) null);
        assertTrue(skipped.status().isSkipped());

        RuleViolations errors = new RuleViolations();
        RuleResult fail = rule.run(ruleViolations -> errors, age -> 16);
        assertTrue(fail.status().isFail());
    }

    // -----------------------------------------------------------------------
    // Presence-rule guards
    // -----------------------------------------------------------------------

    @Test
    public void notNullRejectsFailOnNullFalse() {
        assertThrows(IllegalArgumentException.class,
                () -> notNull(binding("username")).failOnNull(false));
    }

    @Test
    public void notBlankRejectsFailOnNullFalse() {
        assertThrows(IllegalArgumentException.class,
                () -> notBlank(binding("username")).failOnNull(false));
    }

    @Test
    public void notEmptyRejectsFailOnNullFalse() {
        assertThrows(IllegalArgumentException.class,
                () -> notEmpty(binding("tags")).failOnNull(false));
    }

    @Test
    public void presenceRulesAllowFailOnNullTrue() {
        Rule rule = notNull(binding("username")).failOnNull(true).build();
        RuleViolations errors = new RuleViolations();

        RuleResult result = rule.run(ruleViolations -> errors, username -> (Object) null);
        assertTrue(result.status().isFail());
    }
}
