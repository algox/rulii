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
import org.rulii.model.InputParameter;
import org.rulii.model.condition.Condition;
import org.rulii.rule.Rule;
import org.rulii.ruleset.RuleSet;
import org.rulii.ruleset.RuleSetDefinition;

import static org.rulii.model.condition.Conditions.condition;
import static org.rulii.model.function.Functions.function;

/**
 * Tests the input parameters, stop condition and error handler on {@link RuleSetDefinition}.
 *
 * @author Max Arulananthan
 * @since 2.1
 */
public class RuleSetDefinitionTest {

    @Test
    @SuppressWarnings("removal")
    public void definitionCarriesParametersStopConditionAndErrorHandler() {
        Condition stop = condition(() -> false);

        RuleSet<?> rules = RuleSet.builder().with("definitionSet", "A described rule set")
                .param("orderId", String.class, true, "The order id")
                .param("limit", Integer.class, function(() -> 10), "Max items")
                .param("plain", String.class)
                .rule(Rule.builder().name("rule1").given(condition(() -> true)).build())
                .stopCondition(stop)
                .build();

        RuleSetDefinition def = rules.getDefinition();
        Assertions.assertEquals("definitionSet", def.getName());

        Assertions.assertEquals(3, def.getInputParameters().size());
        Assertions.assertEquals(rules.getInputParameters(), def.getInputParameters());
        InputParameter<?> orderId = def.getInputParameters().get(0);
        Assertions.assertEquals("orderId", orderId.name());
        Assertions.assertTrue(orderId.required());
        Assertions.assertEquals("The order id", orderId.description());
        InputParameter<?> limit = def.getInputParameters().get(1);
        Assertions.assertFalse(limit.required());
        Assertions.assertEquals("Max items", limit.description());
        Assertions.assertNull(def.getInputParameters().get(2).description());

        Assertions.assertSame(stop.getDefinition(), def.getStopConditionDefinition());
        Assertions.assertSame(def.getStopConditionDefinition(), def.getStopActionDefinition(), "deprecated alias");
        Assertions.assertNotNull(def.getErrorHandlerDefinition(), "the default error handler is a function");
        Assertions.assertEquals(1, def.getDefinitions().size());
    }

    @Test
    public void definitionWithoutHooksHasNulls() {
        RuleSet<?> rules = RuleSet.builder().with("bareSet").build();
        Assertions.assertFalse(rules.getDefinition().isValidating());
        Assertions.assertTrue(RuleSet.builder().with("validatingSet").validating().build().getDefinition().isValidating());
        RuleSetDefinition def = rules.getDefinition();

        Assertions.assertTrue(def.getInputParameters().isEmpty());
        Assertions.assertNull(def.getStopConditionDefinition());
        Assertions.assertNull(def.getPreConditionDefinition());
        Assertions.assertNull(def.getInitActionDefinition());
    }

    @Test
    public void inputParameterKeepsTheOldConstructorAndNameEquality() {
        InputParameter<String> old = new InputParameter<>("a", String.class, true, null);
        InputParameter<String> described = new InputParameter<>("a", String.class, false, null, "desc");

        Assertions.assertNull(old.description());
        Assertions.assertEquals("desc", described.description());
        Assertions.assertEquals(old, described, "equality is by name only");
        Assertions.assertEquals(old.hashCode(), described.hashCode());
        Assertions.assertTrue(described.toString().contains("desc"));
    }
}
