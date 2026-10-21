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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.validation.RuleViolation;
import org.rulii.validation.RuleViolations;
import org.rulii.validation.Severity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Tests for the RuleViolation parameter values and the RuleViolations add routing.
 *
 * @author Max Arulananthan
 * @since 2.2.0
 */
public class RuleViolationsTest {

    public RuleViolationsTest() {
        super();
    }

    /**
     * Collector that records every violation delivered through add(RuleViolation).
     */
    private static final class RecordingViolations extends RuleViolations {

        private final List<RuleViolation> seen = new ArrayList<>();

        private RecordingViolations() {
            super();
        }

        @Override
        public void add(RuleViolation error) {
            seen.add(error);
            super.add(error);
        }
    }

    @Test
    public void parameterValuesKeepTheirType() {
        Map<String, Object> params = new java.util.LinkedHashMap<>();
        params.put("value", 42);
        params.put("min", 10L);
        params.put("missing", null);
        RuleViolation violation = new RuleViolation("rule", "code", Severity.ERROR, "message", params);

        Assertions.assertEquals(42, violation.getParameterValues().get("value"));
        Assertions.assertEquals(10L, violation.getParameterValues().get("min"));
        Assertions.assertNull(violation.getParameterValues().get("missing"));
        Assertions.assertTrue(violation.getParameterValues().containsKey("missing"));
        Assertions.assertEquals("42", violation.getParameters().get("value"));
        Assertions.assertEquals("10", violation.getParameters().get("min"));
        Assertions.assertNull(violation.getParameters().get("missing"));
        Assertions.assertEquals(List.of("value", "min", "missing"), new ArrayList<>(violation.getParameters().keySet()));
        Assertions.assertThrows(UnsupportedOperationException.class, () -> violation.getParameterValues().put("x", 1));
        Assertions.assertThrows(UnsupportedOperationException.class, () -> violation.getParameters().put("x", "1"));
    }

    @Test
    public void stringParametersStillWork() {
        RuleViolation violation = new RuleViolation("rule", "code", null, null, Map.of("value", "abc"));
        Assertions.assertEquals("abc", violation.getParameters().get("value"));
        Assertions.assertEquals("abc", violation.getParameterValues().get("value"));
        Assertions.assertTrue(new RuleViolation("rule", "code", null, null, null).getParameters().isEmpty());
    }

    @Test
    public void builderKeepsRawValues() {
        RuleViolation violation = RuleViolation.builder().with("rule").errorCode("code").param("value", 7).build();
        Assertions.assertEquals(7, violation.getParameterValues().get("value"));
        Assertions.assertEquals("7", violation.getParameters().get("value"));
    }

    @Test
    public void everyAddRoutesThroughAddViolation() {
        RecordingViolations violations = new RecordingViolations();
        RuleViolation a = new RuleViolation("a", "code", null, null, null);
        RuleViolation b = new RuleViolation("b", "code", null, null, null);
        RuleViolation c = new RuleViolation("c", "code", null, null, null);

        violations.add(a);
        violations.addAll(b);
        violations.addAll(List.of(c));
        RuleViolation d = violations.add("d", "code");
        RuleViolation e = violations.add("e", "code", Severity.WARNING, "warn");

        Assertions.assertEquals(List.of(a, b, c, d, e), violations.seen);
        Assertions.assertEquals(List.of(a, b, c, d, e), violations.getViolations());
        Assertions.assertEquals(Severity.WARNING, e.getSeverity());
        Assertions.assertEquals("warn", e.getErrorMessage());
    }
}
