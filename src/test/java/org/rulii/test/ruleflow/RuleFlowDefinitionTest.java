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
package org.rulii.test.ruleflow;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.context.RuleContext;
import org.rulii.model.InputParameter;
import org.rulii.model.action.Action;
import org.rulii.model.function.Function;
import org.rulii.ruleflow.RuleFlow;
import org.rulii.ruleflow.RuleFlowDefinition;
import org.rulii.ruleflow.info.CommandInfo;
import org.rulii.script.Script;
import org.rulii.script.janino.JaninoScriptProcessorFactory;

import static org.rulii.model.action.Actions.action;
import static org.rulii.model.condition.Conditions.condition;
import static org.rulii.model.function.Functions.function;

/**
 * Tests that a {@link RuleFlowDefinition} carries the whole structure of a flow.
 *
 * @author Max Arulananthan
 * @since 2.1
 */
public class RuleFlowDefinitionTest {

    @Test
    public void definitionCarriesTheWholeStructure() {
        Function<Integer> result = function(() -> 7);
        Action finalizer = action(() -> {});

        RuleFlow<Integer> flow = RuleFlow.builder()
                .name("definitionFlow")
                .description("A flow with everything")
                .param("orderId", String.class, true, "The order id")
                .param("limit", Integer.class, function(() -> 10), "Max items")
                .param("plain", String.class)
                .bind("a", 1)
                .when(condition(() -> true), b -> b.bind("t", 1))
                .onException(IllegalStateException.class, b -> b.bind("g", 1))
                .finalizer(finalizer)
                .returning(result)
                .build();

        RuleFlowDefinition def = flow.getDefinition();
        Assertions.assertEquals("definitionFlow", def.getName());
        Assertions.assertEquals("A flow with everything", def.getDescription());
        Assertions.assertNotNull(def.getSource());

        Assertions.assertEquals(3, def.getInputParameters().size());
        InputParameter<?> orderIdParam = def.getInputParameters().get(0);
        Assertions.assertEquals("orderId", orderIdParam.name());
        Assertions.assertTrue(orderIdParam.required());
        Assertions.assertEquals("The order id", orderIdParam.description());
        InputParameter<?> limit = def.getInputParameters().get(1);
        Assertions.assertFalse(limit.required());
        Assertions.assertNotNull(limit.defaultValue());
        Assertions.assertEquals("Max items", limit.description());
        Assertions.assertNull(def.getInputParameters().get(2).description());

        Assertions.assertEquals(2, def.getCommandCount());
        Assertions.assertEquals(2, def.getCommands().size());
        Assertions.assertInstanceOf(CommandInfo.Bind.class, def.getCommands().get(0));
        Assertions.assertInstanceOf(CommandInfo.When.class, def.getCommands().get(1));

        Assertions.assertEquals(IllegalStateException.class, def.getGlobalHandler().exceptionType());
        Assertions.assertEquals(1, def.getGlobalHandler().body().size());
        Assertions.assertSame(finalizer.getDefinition(), def.getFinalizer().method());
        Assertions.assertSame(result.getDefinition(), def.getReturning().method());
        Assertions.assertEquals(Integer.class, def.getResultType(), "taken from the extractor's declared return type");

        Assertions.assertEquals(7, flow.run(orderId -> "ABC", plain -> "x"));
    }

    @Test
    public void resultTypeComesFromTheExplicitTypeWhenGiven() {
        RuleFlow<Number> flow = RuleFlow.builder()
                .name("typedFlow")
                .returning(Number.class, function(() -> 7))
                .build();

        Assertions.assertEquals(Number.class, flow.getDefinition().getResultType());
        Assertions.assertEquals(7, flow.run());
    }

    @Test
    public void resultTypeFallsBackToObjectForScripts() {
        Function<Object> script = Function.builder().build(
                Script.builder().build(JaninoScriptProcessorFactory.LANGUAGE_NAME, "7"));

        RuleFlow<Object> flow = RuleFlow.builder()
                .name("scriptFlow")
                .returning(script)
                .build();

        Assertions.assertEquals(Object.class, flow.getDefinition().getResultType());
        Assertions.assertEquals("7", flow.getDefinition().getReturning().sourceText());
    }

    @Test
    public void resultTypeIsNullWithoutAnExtractor() {
        RuleFlow<RuleContext> plain = RuleFlow.builder().name("plainFlow").bind("a", 1).build();
        Assertions.assertNull(plain.getDefinition().getResultType());
        Assertions.assertNull(plain.getDefinition().getReturning());
        Assertions.assertNull(plain.getDefinition().getFinalizer());
        Assertions.assertNull(plain.getDefinition().getGlobalHandler());

        RuleFlow<RuleContext> reset = RuleFlow.builder().name("resetFlow")
                .returning(Number.class, function(() -> 7))
                .returning()
                .build();
        Assertions.assertNull(reset.getDefinition().getResultType());
        Assertions.assertNull(reset.getDefinition().getReturning());
    }

    @Test
    public void contextLabelNamesTheConfigurator() {
        RuleFlow<RuleContext> plain = RuleFlow.builder().name("noContextFlow").bind("a", 1).build();
        Assertions.assertNull(plain.getDefinition().getContextLabel());

        RuleFlow<RuleContext> labelled = RuleFlow.builder().name("labelledFlow")
                .context(builder -> {}, "orderContext")
                .bind("a", 1)
                .build();
        Assertions.assertEquals("orderContext", labelled.getDefinition().getContextLabel());

        RuleFlow<RuleContext> unlabelled = RuleFlow.builder().name("unlabelledFlow")
                .context(builder -> {})
                .bind("a", 1)
                .build();
        Assertions.assertNotNull(unlabelled.getDefinition().getContextLabel(), "falls back to the configurator class name");
    }
}
