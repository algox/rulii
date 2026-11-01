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
import org.rulii.model.ResultType;
import org.rulii.model.UnrulyException;
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
    public void resultTypeIsUnknownForScripts() {
        Function<Object> script = Function.builder().build(
                Script.builder().build(JaninoScriptProcessorFactory.LANGUAGE_NAME, "7"));

        RuleFlow<Object> flow = RuleFlow.builder()
                .name("scriptFlow")
                .returning(script)
                .build();

        Assertions.assertNull(flow.getDefinition().getResultType(), "no longer reported as Object");
        Assertions.assertEquals(ResultType.unknown(), flow.getDefinition().getResolvedResultType());
        Assertions.assertEquals("7", flow.getDefinition().getReturning().sourceText());
        Assertions.assertTrue(flow.toString().contains("resultType=unknown"));
    }

    @Test
    public void resultTypeCanBeStatedOnItsOwn() {
        Function<Object> script = Function.builder().build(
                Script.builder().build(JaninoScriptProcessorFactory.LANGUAGE_NAME, "7"));

        RuleFlow<Object> after = RuleFlow.builder().name("statedAfterFlow")
                .returning(script)
                .resultType(Integer.class)
                .build();
        Assertions.assertEquals(ResultType.declared(Integer.class), after.getDefinition().getResolvedResultType());
        Assertions.assertEquals(Integer.class, after.getDefinition().getResultType());
        Assertions.assertEquals(7, after.run());

        RuleFlow<Object> before = RuleFlow.builder().name("statedBeforeFlow")
                .resultType(Integer.class)
                .returning(script)
                .build();
        Assertions.assertEquals(ResultType.declared(Integer.class), before.getDefinition().getResolvedResultType(),
                "returning(Function) keeps a stated type");

        RuleFlow<Number> typed = RuleFlow.builder().name("typedFlow")
                .returning(Number.class, function(() -> 7))
                .returning(function(() -> 8))
                .build();
        Assertions.assertEquals(ResultType.declared(Number.class), typed.getDefinition().getResolvedResultType(), "last explicit wins");
        Assertions.assertEquals(8, typed.run());

        Assertions.assertEquals(ResultType.derived(Integer.class),
                RuleFlow.builder().name("derivedFlow").returning(function(() -> 7)).build().getDefinition().getResolvedResultType());
        Assertions.assertEquals(ResultType.none(),
                RuleFlow.builder().name("noneFlow").bind("a", 1).build().getDefinition().getResolvedResultType());
    }

    @Test
    public void statedTypeWithoutAnExtractorIsRejected() {
        UnrulyException noReturning = Assertions.assertThrows(UnrulyException.class, () -> RuleFlow.builder().name("contextFlow")
                .resultType(Integer.class)
                .bind("a", 1)
                .build());
        Assertions.assertEquals("RuleFlow [contextFlow] declares result type [Integer] but returns its RuleContext; "
                + "add returning(extractor) or remove the result type.", noReturning.getMessage());

        Assertions.assertThrows(UnrulyException.class, () -> RuleFlow.builder().name("resetFlow")
                .returning(function(() -> 7))
                .returning()
                .resultType(Integer.class)
                .build(), "returning() resets the extractor, so a type stated after it has nothing to describe");
    }

    @Test
    public void statedTypeMustAgreeWithTheExtractorAndTheExits() {
        // The typed overload cannot even express a mismatch; a type stated separately can.
        UnrulyException extractor = Assertions.assertThrows(UnrulyException.class, () -> RuleFlow.builder().name("badReturningFlow")
                .resultType(String.class)
                .returning(function(() -> 7))
                .build());
        Assertions.assertEquals("RuleFlow [badReturningFlow] declares result type [String] but its returning extractor returns [Integer]; "
                + "declare the type the returning extractor returns, or change the returning extractor.", extractor.getMessage());

        UnrulyException exit = Assertions.assertThrows(UnrulyException.class, () -> RuleFlow.builder().name("badExitFlow")
                .resultType(String.class)
                .when(condition(() -> true), b -> b.forEach(function(() -> java.util.List.of(1)), "item", body -> body.exit(function(() -> 7))))
                .returning(function(() -> "done"))
                .build());
        Assertions.assertTrue(exit.getMessage().contains("exit returns [Integer]"), exit.getMessage());

        UnrulyException otherwise = Assertions.assertThrows(UnrulyException.class, () -> RuleFlow.builder().name("badOtherwiseExitFlow")
                .resultType(String.class)
                .when(condition(() -> false), b -> b.bind("t", 1), b -> b.exit(function(() -> 7)))
                .returning(function(() -> "done"))
                .build());
        Assertions.assertTrue(otherwise.getMessage().contains("exit returns [Integer]"), otherwise.getMessage());

        UnrulyException handler = Assertions.assertThrows(UnrulyException.class, () -> RuleFlow.builder().name("badHandlerExitFlow")
                .resultType(String.class)
                .onException(IllegalStateException.class, b -> b.exit(function(() -> 7)))
                .returning(function(() -> "done"))
                .build());
        Assertions.assertTrue(handler.getMessage().contains("exit returns [Integer]"), handler.getMessage());

        // The same exit under a derived type builds: a derived type never rejects a flow.
        RuleFlow<String> derived = RuleFlow.builder().name("derivedExitFlow")
                .when(condition(() -> false), b -> b.exit(function(() -> 7)))
                .returning(function(() -> "done"))
                .build();
        Assertions.assertEquals(ResultType.derived(String.class), derived.getDefinition().getResolvedResultType());
        Assertions.assertEquals("done", derived.run());

        // Compatible exits: a subtype, and an exit with the context.
        RuleFlow<Number> compatible = RuleFlow.builder().name("compatibleExitFlow")
                .resultType(Number.class)
                .when(condition(() -> false), b -> b.exit(function(() -> 7L)))
                .when(condition(() -> false), b -> b.exit())
                .returning(function(() -> 7))
                .build();
        Assertions.assertEquals(7, compatible.run());
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
