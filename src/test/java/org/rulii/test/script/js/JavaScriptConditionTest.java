/*
 * This software is licensed under the Apache 2 license, quoted below.
 *
 * Copyright (c) 1999-2025, Algorithmx Inc.
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
package org.rulii.test.script.js;

import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.rulii.model.UnrulyException;
import org.rulii.model.condition.Condition;
import org.rulii.script.DefaultScriptProcessor;
import org.rulii.script.Script;
import org.rulii.script.ScriptProcessor;

import javax.script.ScriptEngine;

/**
 * Integration tests for {@link Condition} built from a JavaScript {@link Script}.
 *
 * <p>A script-based condition takes a {@link org.rulii.bind.Bindings} parameter matched by type.
 * Script variables are supplied as individual {@code BindingDeclaration} lambdas (e.g.
 * {@code age -> 21}); the framework places them in the RuleContext's {@code $ruleBindings}
 * which is the {@code Bindings} instance ultimately received by the script.</p>
 *
 * @author Max Arulananthan
 * @since 1.2
 */
public class JavaScriptConditionTest {

    private static ScriptProcessor processor;

    @BeforeAll
    static void setup() {
        ScriptEngine engine = createScriptEngine(createEngine());
        Assumptions.assumeTrue(engine != null, "GraalJS engine not available – skipping tests.");
        processor = new DefaultScriptProcessor(engine, "javascript");
    }

    // -----------------------------------------------------------------------
    // Basic boolean literals – no bound variables required
    // -----------------------------------------------------------------------

    @Test
    void scriptConditionReturnsTrueLiteral() {
        Condition condition = Condition.builder().build(processor.load("true"));
        Assertions.assertTrue(condition.isTrue());
    }

    @Test
    void scriptConditionReturnsFalseLiteral() {
        Condition condition = Condition.builder().build(processor.load("false"));
        Assertions.assertFalse(condition.isTrue());
    }

    @Test
    void scriptConditionReturnsComparisonLiteralTrue() {
        Condition condition = Condition.builder().build(processor.load("10 > 5"));
        Assertions.assertTrue(condition.isTrue());
    }

    @Test
    void scriptConditionReturnsComparisonLiteralFalse() {
        Condition condition = Condition.builder().build(processor.load("3 > 5"));
        Assertions.assertFalse(condition.isTrue());
    }

    // -----------------------------------------------------------------------
    // Conditions using bound variables
    // Variables are passed as BindingDeclaration lambdas; the name of each
    // lambda parameter becomes the binding name accessible inside the script.
    // -----------------------------------------------------------------------

    @Test
    void scriptConditionAgeOver18WhenAdult() {
        Condition condition = Condition.builder().build(processor.load("age >= 18"));
        Assertions.assertTrue(condition.isTrue(age -> 21));
    }

    @Test
    void scriptConditionAgeOver18WhenMinor() {
        Condition condition = Condition.builder().build(processor.load("age >= 18"));
        Assertions.assertFalse(condition.isTrue(age -> 16));
    }

    @Test
    void scriptConditionWithLogicalAndExpression() {
        Condition condition = Condition.builder().build(
                processor.load("balance > 0 && balance >= minimum"));
        Assertions.assertTrue(condition.isTrue(balance -> 500, minimum -> 100));
        Assertions.assertFalse(condition.isTrue(balance -> 50, minimum -> 100));
    }

    @Test
    void scriptConditionWithStringEqualityTrue() {
        Condition condition = Condition.builder().build(processor.load("status === 'ACTIVE'"));
        Assertions.assertTrue(condition.isTrue(status -> "ACTIVE"));
    }

    @Test
    void scriptConditionWithStringEqualityFalse() {
        Condition condition = Condition.builder().build(processor.load("status === 'ACTIVE'"));
        Assertions.assertFalse(condition.isTrue(status -> "INACTIVE"));
    }

    @Test
    void scriptConditionWithNegationFalse() {
        Condition condition = Condition.builder().build(processor.load("!(x > 10)"));
        Assertions.assertFalse(condition.isTrue(x -> 15));
    }

    @Test
    void scriptConditionWithNegationTrue() {
        Condition condition = Condition.builder().build(processor.load("!(x > 10)"));
        Assertions.assertTrue(condition.isTrue(x -> 5));
    }

    @Test
    void scriptConditionWithLogicalOrExpression() {
        Condition condition = Condition.builder().build(
                processor.load("isPremium || balance > 1000"));
        Assertions.assertTrue(condition.isTrue(isPremium -> true, balance -> 50));
        Assertions.assertTrue(condition.isTrue(isPremium -> false, balance -> 5000));
        Assertions.assertFalse(condition.isTrue(isPremium -> false, balance -> 100));
    }

    @Test
    void scriptConditionEvaluatedRepeatedly() {
        Condition condition = Condition.builder().build(processor.load("score >= threshold"));
        Assertions.assertTrue(condition.isTrue(score -> 80, threshold -> 60));
        Assertions.assertFalse(condition.isTrue(score -> 40, threshold -> 60));
        Assertions.assertTrue(condition.isTrue(score -> 60, threshold -> 60));
    }

    // -----------------------------------------------------------------------
    // Error cases
    // -----------------------------------------------------------------------

    @Test
    void scriptConditionReturningNullThrowsUnrulyException() {
        Condition condition = Condition.builder().build(processor.load("null"));
        Assertions.assertThrows(UnrulyException.class, condition::isTrue);
    }

    @Test
    void scriptConditionReturningNumberThrowsUnrulyException() {
        Condition condition = Condition.builder().build(processor.load("42"));
        Assertions.assertThrows(UnrulyException.class, condition::isTrue);
    }

    @Test
    void scriptConditionReturningStringThrowsUnrulyException() {
        Condition condition = Condition.builder().build(processor.load("'yes'"));
        Assertions.assertThrows(UnrulyException.class, condition::isTrue);
    }

    @Test
    void scriptConditionWithRuntimeErrorThrowsUnrulyException() {
        // EvaluationException extends UnrulyException; the framework may wrap it
        // when propagating through the condition execution pipeline.
        Condition condition = Condition.builder().build(processor.load("undeclaredFunction()"));
        Assertions.assertThrows(UnrulyException.class, condition::isTrue);
    }

    private static Engine createEngine() {
        return Engine.newBuilder()
                .option("engine.WarnInterpreterOnly", "false")
                .allowExperimentalOptions(true)
                .build();
    }

    private static GraalJSScriptEngine createScriptEngine(Engine engine) {
        return GraalJSScriptEngine.create(engine,
                Context.newBuilder("js")
                        .allowHostAccess(HostAccess.NONE)
                        .option("js.ecmascript-version", "2022"));

    }
}
