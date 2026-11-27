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

import org.junit.jupiter.api.Test;
import org.rulii.bind.Bindings;
import org.rulii.context.RuleContext;
import org.rulii.model.UnrulyException;
import org.rulii.rule.Rule;
import org.rulii.ruleset.RuleSet;
import org.rulii.validation.RuleViolations;
import org.rulii.validation.Severity;
import org.rulii.validation.ValidationException;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.rulii.model.action.Actions.action;
import static org.rulii.model.function.Functions.function;
import static org.rulii.validation.rules.Validators.binding;
import static org.rulii.validation.rules.Validators.notBlank;

/**
 * The default value of a rule set's optional input parameter is bound in the rule set's own scope:
 * its rules see it, the caller never does, and it is evaluated again on every run.
 */
public class RuleSetInputDefaultsTest {

    public RuleSetInputDefaultsTest() {
        super();
    }

    @Test
    public void defaultIsSeenByTheRulesAndDoesNotOutliveTheRun() {
        RuleSet<Integer> ruleSet = RuleSet.builder().with("discountSet")
                .param("discount", Integer.class, function(() -> 5))
                .resultExtractor(function((Integer discount) -> discount))
                .build();
        Bindings bindings = Bindings.builder().standard();
        RuleContext ruleContext = RuleContext.builder().build(bindings);
        int scopes = ruleContext.getBindings().getScopeSize();

        assertEquals(Integer.valueOf(5), ruleSet.run(ruleContext));
        assertFalse(ruleContext.getBindings().contains("discount"));
        assertFalse(bindings.contains("discount"));
        assertEquals(scopes, ruleContext.getBindings().getScopeSize());
    }

    @Test
    public void defaultIsEvaluatedOnEveryRunInTheSameContext() {
        AtomicInteger calls = new AtomicInteger();
        RuleSet<Integer> ruleSet = RuleSet.builder().with("counterSet")
                .param("ticket", Integer.class, function(() -> calls.incrementAndGet()))
                .resultExtractor(function((Integer ticket) -> ticket))
                .build();
        RuleContext ruleContext = RuleContext.builder().build();

        assertEquals(Integer.valueOf(1), ruleSet.run(ruleContext));
        assertEquals(Integer.valueOf(2), ruleSet.run(ruleContext));
    }

    @Test
    public void nullBoundByTheCallerGetsTheDefaultInsideAndStaysNullOutside() {
        RuleSet<Integer> ruleSet = RuleSet.builder().with("nullSet")
                .param("discount", Integer.class, function(() -> 5))
                .resultExtractor(function((Integer discount) -> discount))
                .build();
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("discount", Integer.class, null);

        assertEquals(Integer.valueOf(5), ruleSet.run(RuleContext.builder().build(bindings)));
        assertTrue(bindings.contains("discount"));
        assertNull(bindings.getValue("discount"));
    }

    @Test
    public void failingDefaultPropagatesAndLeavesNoScopeBehind() {
        RuleSet<?> ruleSet = RuleSet.builder().with("failingDefaultSet")
                .param("discount", Integer.class, function((RuleContext c) -> { throw new IllegalStateException("no discount"); }))
                .build();
        RuleContext ruleContext = RuleContext.builder().build();
        int scopes = ruleContext.getBindings().getScopeSize();

        UnrulyException thrown = assertThrows(UnrulyException.class, () -> ruleSet.run(ruleContext));
        assertTrue(thrown.getMessage().startsWith("RuleSet [failingDefaultSet] could not evaluate the default value of input parameter [discount]"));
        assertTrue(causedBy(thrown, IllegalStateException.class, "no discount"));
        assertEquals(scopes, ruleContext.getBindings().getScopeSize());
    }

    @Test
    public void validatingRuleSetCollectsIntoItsOwnDefaultCollectorEveryRun() {
        RuleSet<?> ruleSet = RuleSet.builder().with("validatingSet")
                .validating()
                .rule(notBlank(binding("name")).severity(Severity.WARNING).build())
                .build();
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("name", "");
        RuleContext ruleContext = RuleContext.builder().build(bindings);

        // A warning does not throw; the defaulted collector goes with the rule set's scope.
        ruleSet.run(ruleContext);
        assertFalse(ruleContext.getBindings().contains("ruleViolations"));
        ruleSet.run(ruleContext);
        assertFalse(ruleContext.getBindings().contains("ruleViolations"));
    }

    @Test
    public void validatingRuleSetStillReportsIntoTheCallersCollector() {
        RuleSet<?> ruleSet = RuleSet.builder().with("validatingSet")
                .validating()
                .rule(notBlank(binding("name")).severity(Severity.WARNING).build())
                .build();
        RuleViolations violations = new RuleViolations();
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("name", "");
        bindings.bind("ruleViolations", violations);

        ruleSet.run(RuleContext.builder().build(bindings));

        assertEquals(1, violations.size());
        assertSame(violations, bindings.getValue("ruleViolations"));
    }

    @Test
    public void validatingRuleSetStillThrowsWithTheViolationsOnASevereError() {
        RuleSet<?> ruleSet = RuleSet.builder().with("validatingSet")
                .validating()
                .rule(notBlank(binding("name")).build())
                .build();

        ValidationException thrown = assertThrows(ValidationException.class, () -> ruleSet.run(name -> ""));
        assertEquals(1, thrown.getViolations().size());
    }

    @Test
    public void asyncRunKeepsTheDefaultInsideTheFork() throws Exception {
        RuleSet<Integer> ruleSet = RuleSet.builder().with("asyncDefaultSet")
                .param("discount", Integer.class, function(() -> 5))
                .rule(Rule.builder().name("noop").then(action(() -> { })).build())
                .resultExtractor(function((Integer discount) -> discount))
                .build();
        Bindings bindings = Bindings.builder().standard();
        RuleContext ruleContext = RuleContext.builder().build(bindings);

        assertEquals(Integer.valueOf(5), ruleSet.runAsync(ruleContext).get());
        assertFalse(bindings.contains("discount"));
    }

    private static boolean causedBy(Throwable thrown, Class<? extends Throwable> type, String message) {
        for (Throwable cause = thrown.getCause(); cause != null; cause = cause.getCause()) {
            if (type.isInstance(cause) && message.equals(cause.getMessage())) return true;
        }

        return false;
    }
}
