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
import org.rulii.model.ResultType;
import org.rulii.model.SourceDefinition;
import org.rulii.model.UnrulyException;
import org.rulii.model.condition.Condition;
import org.rulii.model.function.Function;
import org.rulii.rule.Rule;
import org.rulii.ruleset.RuleSet;
import org.rulii.ruleset.RuleSetDefinition;
import org.rulii.ruleset.RuleSetExecutionStatus;
import org.rulii.ruleset.RulingFamily;
import org.rulii.script.Script;
import org.rulii.script.janino.JaninoScriptProcessorFactory;
import org.rulii.validation.RuleViolations;

import java.util.List;

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

    @Test
    public void resultTypeIsTheStatusByDefault() {
        RuleSet<?> rules = RuleSet.builder().with("defaultResultSet").build();
        RuleSetDefinition def = rules.getDefinition();

        Assertions.assertEquals(RuleSetExecutionStatus.class, def.getResultType());
        Assertions.assertEquals(ResultType.byDefault(RuleSetExecutionStatus.class), def.getResolvedResultType());
        Assertions.assertInstanceOf(RuleSetExecutionStatus.class, rules.run());
        Assertions.assertTrue(((RulingFamily<?>) rules).prettyPrint().contains("Result Type : RuleSetExecutionStatus (default)"));
    }

    @Test
    public void resultTypeIsDerivedFromATypedExtractor() {
        RuleSet<RuleViolations> rules = RuleSet.builder().with("derivedResultSet")
                .resultExtractor(function((RuleViolations violations) -> violations))
                .build();

        Assertions.assertEquals(RuleViolations.class, rules.getDefinition().getResultType());
        Assertions.assertEquals(ResultType.Origin.DERIVED, rules.getDefinition().getResolvedResultType().getOrigin());
    }

    @Test
    public void resultTypeIsUnknownForAScriptUnlessDeclared() {
        Function<Object> script = Function.builder().build(
                Script.builder().build(JaninoScriptProcessorFactory.LANGUAGE_NAME, "7"));

        RuleSet<?> unknown = RuleSet.builder().with("scriptResultSet").resultExtractor(script).build();
        Assertions.assertNull(unknown.getDefinition().getResultType(), "no longer reported as Object");
        Assertions.assertEquals(ResultType.unknown(), unknown.getDefinition().getResolvedResultType());
        Assertions.assertFalse(((RulingFamily<?>) unknown).prettyPrint().contains("Result Type"));

        RuleSet<?> declared = RuleSet.builder().with("declaredScriptResultSet")
                .resultExtractor(script)
                .resultType(Integer.class)
                .build();
        Assertions.assertEquals(ResultType.declared(Integer.class), declared.getDefinition().getResolvedResultType());

        RuleSet<Integer> typed = RuleSet.builder().with("typedScriptResultSet")
                .resultExtractor(Integer.class, Function.builder().build(
                        Script.builder().build(JaninoScriptProcessorFactory.LANGUAGE_NAME, "7")))
                .build();
        Assertions.assertEquals(ResultType.declared(Integer.class), typed.getDefinition().getResolvedResultType());
        Assertions.assertEquals(7, typed.run());
    }

    @Test
    public void declaredTypeSurvivesALaterExtractorAndTheDefaultExtractor() {
        RuleSet<?> kept = RuleSet.builder().with("keptResultSet")
                .resultType(Number.class)
                .resultExtractor(function(() -> 7))
                .build();
        Assertions.assertEquals(ResultType.declared(Number.class), kept.getDefinition().getResolvedResultType(), "last explicit wins");

        RuleSet<?> onDefault = RuleSet.builder().with("declaredDefaultResultSet")
                .resultType(Object.class)
                .build();
        Assertions.assertEquals(ResultType.declared(Object.class), onDefault.getDefinition().getResolvedResultType(),
                "Object is kept when the author says so");
    }

    @Test
    public void copyKeepsADeclaredOrDefaultType() {
        RuleSet<?> original = RuleSet.builder().with("originalSet").build();
        RuleSet<?> copy = RuleSet.builder().with(original).build();
        Assertions.assertEquals(ResultType.declared(RuleSetExecutionStatus.class), copy.getDefinition().getResolvedResultType(),
                "the copied extractor is no longer the default, so the type is carried over");

        RuleSet<?> declared = RuleSet.builder().with("declaredSet").resultType(Number.class).resultExtractor(function(() -> 7)).build();
        Assertions.assertEquals(ResultType.declared(Number.class), RuleSet.builder().with(declared).build().getDefinition().getResolvedResultType());

        RuleSet<?> derived = RuleSet.builder().with("derivedSet").resultExtractor(function(() -> 7)).build();
        Assertions.assertEquals(ResultType.derived(Integer.class), RuleSet.builder().with(derived).build().getDefinition().getResolvedResultType());
    }

    @Test
    public void legacyConstructorDerivesTheType() {
        Function<Integer> extractor = function(() -> 7);
        RuleSetDefinition def = new RuleSetDefinition("legacySet", null, SourceDefinition.build(), List.of(),
                null, null, null, null, extractor.getDefinition(), null, List.of(), false);
        Assertions.assertEquals(ResultType.derived(Integer.class), def.getResolvedResultType());

        RuleSetDefinition bare = new RuleSetDefinition("bareLegacySet", null, SourceDefinition.build(), List.of(),
                null, null, null, null, null, null, List.of(), false);
        Assertions.assertEquals(ResultType.unknown(), bare.getResolvedResultType());
        Assertions.assertNull(bare.getResultType());
    }

    @Test
    public void declaredTypeMustAgreeWithTheExtractorAndTheErrorHandler() {
        // The typed overload cannot even express a mismatch; a type stated separately can.
        UnrulyException extractor = Assertions.assertThrows(UnrulyException.class, () -> RuleSet.builder().with("badExtractorSet")
                .resultType(String.class)
                .resultExtractor(function(() -> 7))
                .build());
        Assertions.assertEquals("RuleSet [badExtractorSet] declares result type [String] but its result extractor returns [Integer]; "
                + "declare the type the result extractor returns, or change the result extractor.", extractor.getMessage());

        UnrulyException handler = Assertions.assertThrows(UnrulyException.class, () -> RuleSet.builder().with("badHandlerSet")
                .resultType(String.class)
                .errorHandler(function(() -> 7))
                .build());
        Assertions.assertTrue(handler.getMessage().contains("error handler returns [Integer]"), handler.getMessage());

        // Compatible: a subtype, and an extractor the engine cannot read.
        RuleSet<?> compatible = RuleSet.builder().with("compatibleSet")
                .resultType(Number.class)
                .resultExtractor(function(() -> 7))
                .errorHandler(function(() -> 7L))
                .build();
        Assertions.assertEquals(ResultType.declared(Number.class), compatible.getDefinition().getResolvedResultType());
    }
}
