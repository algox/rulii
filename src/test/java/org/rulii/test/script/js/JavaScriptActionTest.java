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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.rulii.model.UnrulyException;
import org.rulii.model.action.Action;
import org.rulii.script.DefaultScriptProcessor;
import org.rulii.script.Script;
import org.rulii.script.ScriptProcessor;

import javax.script.ScriptEngine;

/**
 * Integration tests for {@link Action} built from a JavaScript {@link Script}.
 *
 * <p>Script variables are supplied as individual {@code BindingDeclaration} lambdas (e.g.
 * {@code value -> 10}); the framework places them in the RuleContext's {@code $ruleBindings},
 * which is the {@code Bindings} instance ultimately received by the script lambda.</p>
 *
 * @author Max Arulananthan
 * @since 1.2
 */
public class JavaScriptActionTest {

    private static ScriptProcessor processor;

    @BeforeAll
    static void setup() {
        ScriptEngine engine = createScriptEngine(createEngine());
        processor = new DefaultScriptProcessor(engine, "javascript");
    }

    // -----------------------------------------------------------------------
    // Basic execution – no variables required
    // -----------------------------------------------------------------------

    @Test
    void scriptActionRunsWithoutError() {
        Action action = Action.builder().build(processor.load("var unused = 1 + 1;"));
        Assertions.assertDoesNotThrow(() -> { action.run(); });
    }

    @Test
    void scriptActionWithEmptyBodyRunsSuccessfully() {
        Action action = Action.builder().build(processor.load("/* no-op */"));
        Assertions.assertDoesNotThrow(() -> { action.run(); });
    }

    @Test
    void scriptActionRunsMultipleTimesWithoutError() {
        Action action = Action.builder().build(processor.load("var r = 1 + 2;"));
        for (int i = 0; i < 5; i++) {
            final int iteration = i;
            Assertions.assertDoesNotThrow(() -> { action.run(); },
                    "Action should run without error on iteration " + iteration);
        }
    }

    // -----------------------------------------------------------------------
    // Binding variable access during action execution
    //
    // Variables passed as BindingDeclaration lambdas (name -> value) end up
    // in the RuleContext's $ruleBindings, making them accessible in the script.
    // -----------------------------------------------------------------------

    @Test
    void scriptActionCanReadBoundVariableWithoutError() {
        // Script would throw a ReferenceError if 'value' were not in the bindings.
        // Completing without exception confirms the variable was resolved.
        Action action = Action.builder().build(
                processor.load("if (value < 0) { throw new Error('negative'); }"));
        Assertions.assertDoesNotThrow(() -> { action.run(value -> 10); });
    }

    @Test
    void scriptActionGuardWithTrueConditionPasses() {
        Action action = Action.builder().build(
                processor.load("if (!active) { throw new Error('must be active'); }"));
        Assertions.assertDoesNotThrow(() -> { action.run(active -> true); });
    }

    @Test
    void scriptActionGuardWithFalseConditionThrowsUnrulyException() {
        Action action = Action.builder().build(
                processor.load("if (!active) { throw new Error('must be active'); }"));
        Assertions.assertThrows(UnrulyException.class, () -> action.run(active -> false));
    }

    @Test
    void scriptActionReadsMultipleBoundVariables() {
        Action action = Action.builder().build(
                processor.load(
                        "if (a < 0 || b < 0) { throw new Error('values must be non-negative'); }"));
        Assertions.assertDoesNotThrow(() -> { action.run(a -> 3, b -> 7); });
    }

    @Test
    void scriptActionWithMultipleStatementsRunsSuccessfully() {
        Action action = Action.builder().build(
                processor.load(
                        "var sum = x + 1;" +
                        "var product = sum * 2;" +
                        "if (product < 0) { throw new Error('unexpected negative'); }"));
        Assertions.assertDoesNotThrow(() -> { action.run(x -> 5); });
    }

    // -----------------------------------------------------------------------
    // Error propagation
    // -----------------------------------------------------------------------

    @Test
    void scriptActionWithRuntimeErrorThrowsUnrulyException() {
        // EvaluationException extends UnrulyException; the framework may wrap it
        // when propagating through the action execution pipeline.
        Action action = Action.builder().build(processor.load("undeclaredFunction()"));
        Assertions.assertThrows(UnrulyException.class, action::run);
    }

    @Test
    void scriptActionWithExplicitJsThrowPropagatesAsUnrulyException() {
        Action action = Action.builder().build(
                processor.load("throw new Error('deliberate error');"));
        Assertions.assertThrows(UnrulyException.class, action::run);
    }

    /*@Test
    void modifyBindingTest() {
        Bindings bindings = Bindings.builder().scoped();
        bindings.bind("x", 10);
        bindings.bind("y", 20);
        bindings.bind("z", 0);

        Action action = Action.builder().build(processor.load("bindings.setValue('z', x + y);"));
        action.run(bindings);
        Assertions.assertEquals(30, (Integer) bindings.getValue("z"));
    }*/

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
