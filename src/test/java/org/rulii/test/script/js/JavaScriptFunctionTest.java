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
import org.rulii.model.function.Function;
import org.rulii.script.DefaultScriptProcessor;
import org.rulii.script.Script;
import org.rulii.script.ScriptProcessor;

import javax.script.ScriptEngine;

/**
 * Integration tests for {@link Function} built from a JavaScript {@link Script}.
 *
 * <p>Script variables are supplied as individual {@code BindingDeclaration} lambdas (e.g.
 * {@code a -> 3, b -> 7}); the framework places them in the RuleContext's {@code $ruleBindings},
 * which is the {@code Bindings} instance ultimately received by the script lambda.</p>
 *
 * @author Max Arulananthan
 * @since 1.2
 */
public class JavaScriptFunctionTest {

    private static ScriptProcessor processor;

    @BeforeAll
    static void setup() {
        ScriptEngine engine = createScriptEngine(createEngine());
        processor = new DefaultScriptProcessor(engine, "javascript");
    }

    // -----------------------------------------------------------------------
    // Literal return values – no bound variables required
    // -----------------------------------------------------------------------

    @Test
    void scriptFunctionReturnsIntegerLiteral() {
        Function<Object> fn = Function.builder().build(processor.load("42"));
        Object result = fn.apply();
        Assertions.assertEquals(42, ((Number) result).intValue());
    }

    @Test
    void scriptFunctionReturnsStringLiteral() {
        Function<String> fn = Function.builder().build(processor.load("'hello'"));
        String result = fn.apply();
        Assertions.assertEquals("hello", result);
    }

    @Test
    void scriptFunctionReturnsBooleanTrue() {
        Function<Boolean> fn = Function.builder().build(processor.load("true"));
        Boolean result = fn.apply();
        Assertions.assertTrue(result);
    }

    @Test
    void scriptFunctionReturnsBooleanFalse() {
        Function<Boolean> fn = Function.builder().build(processor.load("false"));
        Boolean result = fn.apply();
        Assertions.assertFalse(result);
    }

    @Test
    void scriptFunctionReturnsNull() {
        Function<Object> fn = Function.builder().build(processor.load("null"));
        Object result = fn.apply();
        Assertions.assertNull(result);
    }

    // -----------------------------------------------------------------------
    // Arithmetic computation with bound variables
    // -----------------------------------------------------------------------

    @Test
    void scriptFunctionAddsNumbers() {
        Function<Object> fn = Function.builder().build(processor.load("a + b"));
        Object result = fn.apply(a -> 3, b -> 7);
        Assertions.assertEquals(10, ((Number) result).intValue());
    }

    @Test
    void scriptFunctionMultipliesNumbers() {
        Function<Object> fn = Function.builder().build(processor.load("x * y"));
        Object result = fn.apply(x -> 6, y -> 7);
        Assertions.assertEquals(42, ((Number) result).intValue());
    }

    @Test
    void scriptFunctionComputesSquare() {
        Function<Object> fn = Function.builder().build(processor.load("n * n"));
        Object result = fn.apply(n -> 7);
        Assertions.assertEquals(49, ((Number) result).intValue());
    }

    @Test
    void scriptFunctionComputesFloatingPoint() {
        Function<Object> fn = Function.builder().build(processor.load("price * qty"));
        Object result = fn.apply(price -> 9.99, qty -> 3);
        Assertions.assertEquals(29.97, ((Number) result).doubleValue(), 0.001);
    }

    // -----------------------------------------------------------------------
    // String operations
    // -----------------------------------------------------------------------

    @Test
    void scriptFunctionConcatenatesStrings() {
        Function<String> fn = Function.builder().build(
                processor.load("greeting + ', ' + name + '!'"));
        String result = fn.apply(greeting -> "Hello", name -> "World");
        Assertions.assertEquals("Hello, World!", result);
    }

    @Test
    void scriptFunctionReturnsSubstring() {
        Function<Object> fn = Function.builder().build(processor.load("prefix + suffix"));
        Object result = fn.apply(prefix -> "foo", suffix -> "bar");
        Assertions.assertEquals("foobar", result);
    }

    // -----------------------------------------------------------------------
    // Boolean / comparison
    // -----------------------------------------------------------------------

    @Test
    void scriptFunctionReturnsBooleanComparisonTrue() {
        Function<Boolean> fn = Function.builder().build(processor.load("score >= threshold"));
        Boolean result = fn.apply(score -> 75, threshold -> 60);
        Assertions.assertTrue(result);
    }

    @Test
    void scriptFunctionReturnsBooleanComparisonFalse() {
        Function<Boolean> fn = Function.builder().build(processor.load("score >= threshold"));
        Boolean result = fn.apply(score -> 40, threshold -> 60);
        Assertions.assertFalse(result);
    }

    // -----------------------------------------------------------------------
    // JS built-ins
    // -----------------------------------------------------------------------

    @Test
    void scriptFunctionUsesJsMathMax() {
        Function<Object> fn = Function.builder().build(processor.load("Math.max(a, b)"));
        Object result = fn.apply(a -> 15, b -> 42);
        Assertions.assertEquals(42, ((Number) result).intValue());
    }

    @Test
    void scriptFunctionUsesJsMathAbs() {
        Function<Object> fn = Function.builder().build(processor.load("Math.abs(n)"));
        Object result = fn.apply(n -> -7);
        Assertions.assertEquals(7, ((Number) result).intValue());
    }

    @Test
    void scriptFunctionUsesJsMathPow() {
        Function<Object> fn = Function.builder().build(processor.load("Math.pow(base, exp)"));
        Object result = fn.apply(base -> 2, exp -> 10);
        Assertions.assertEquals(1024, ((Number) result).intValue());
    }

    // -----------------------------------------------------------------------
    // Re-use / multiple invocations
    // -----------------------------------------------------------------------

    @Test
    void scriptFunctionCanBeAppliedRepeatedly() {
        Function<Object> fn = Function.builder().build(processor.load("x * x"));
        int[] inputs = {1, 2, 3, 4, 5};
        for (int v : inputs) {
            Object result = fn.apply(x -> v);
            Assertions.assertEquals(v * v, ((Number) result).intValue(), "x=" + v);
        }
    }

    // -----------------------------------------------------------------------
    // Error propagation
    // -----------------------------------------------------------------------

    @Test
    void scriptFunctionWithRuntimeErrorThrowsUnrulyException() {
        // EvaluationException extends UnrulyException; the framework may wrap it
        // when propagating through the function execution pipeline.
        Function<Object> fn = Function.builder().build(processor.load("undeclaredFn()"));
        Assertions.assertThrows(UnrulyException.class, fn::apply);
    }

    @Test
    void scriptFunctionWithExplicitJsThrowPropagatesAsUnrulyException() {
        Function<Object> fn = Function.builder().build(
                processor.load("throw new Error('deliberate');"));
        Assertions.assertThrows(UnrulyException.class, fn::apply);
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
