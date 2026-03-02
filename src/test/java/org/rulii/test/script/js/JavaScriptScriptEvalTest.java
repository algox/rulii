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
import org.rulii.bind.Bindings;
import org.rulii.script.*;

import javax.script.ScriptEngine;

/**
 * Tests for {@link CompiledScript} and {@link PlainScript} evaluation using GraalJS.
 *
 * @author Max Arulananthan
 * @since 1.2
 */
public class JavaScriptScriptEvalTest {

    private static ScriptProcessor processor;
    private static ScriptEngine sharedEngine;

    @BeforeAll
    static void setup() {
        sharedEngine = createScriptEngine(createEngine());
        processor = new DefaultScriptProcessor(sharedEngine, "javascript");
    }

    // -----------------------------------------------------------------------
    // CompiledScript – structural properties
    // -----------------------------------------------------------------------

    @Test
    void compiledScriptIsCompiledReturnsTrue() {
        Script script = processor.load("1 + 1");
        Assertions.assertTrue(script.isCompiled());
    }

    @Test
    void compiledScriptGetScriptReturnsOriginalSource() {
        String source = "a * b + c";
        Script script = processor.load(source);
        Assertions.assertEquals(source, script.getScript());
    }

    // -----------------------------------------------------------------------
    // PlainScript – structural properties
    // -----------------------------------------------------------------------

    @Test
    void plainScriptIsCompiledReturnsFalse() {
        PlainScript plain = new PlainScript(sharedEngine, "1 + 1");
        Assertions.assertFalse(plain.isCompiled());
    }

    @Test
    void plainScriptGetScriptReturnsOriginalSource() {
        String source = "x + y";
        PlainScript plain = new PlainScript(sharedEngine, source);
        Assertions.assertEquals(source, plain.getScript());
    }

    // -----------------------------------------------------------------------
    // Literal value evaluation
    // -----------------------------------------------------------------------

    @Test
    void evalIntegerLiteral() {
        Script script = processor.load("42");
        Object result = script.eval(Bindings.builder().standard());
        Assertions.assertEquals(42, ((Number) result).intValue());
    }

    @Test
    void evalStringLiteral() {
        Script script = processor.load("'hello world'");
        String result = script.eval(Bindings.builder().standard());
        Assertions.assertEquals("hello world", result);
    }

    @Test
    void evalBooleanTrue() {
        Script script = processor.load("true");
        Boolean result = script.eval(Bindings.builder().standard());
        Assertions.assertTrue(result);
    }

    @Test
    void evalBooleanFalse() {
        Script script = processor.load("false");
        Boolean result = script.eval(Bindings.builder().standard());
        Assertions.assertFalse(result);
    }

    @Test
    void evalNullReturnsJavaNull() {
        Script script = processor.load("null");
        Object result = script.eval(Bindings.builder().standard());
        Assertions.assertNull(result);
    }

    // -----------------------------------------------------------------------
    // Arithmetic / expression evaluation
    // -----------------------------------------------------------------------

    @Test
    void evalArithmeticExpression() {
        Script script = processor.load("3 + 4 * 2");
        Object result = script.eval(Bindings.builder().standard());
        Assertions.assertEquals(11, ((Number) result).intValue());
    }

    @Test
    void evalBooleanComparisonTrue() {
        Script script = processor.load("10 > 5");
        Boolean result = script.eval(Bindings.builder().standard());
        Assertions.assertTrue(result);
    }

    @Test
    void evalBooleanComparisonFalse() {
        Script script = processor.load("3 > 5");
        Boolean result = script.eval(Bindings.builder().standard());
        Assertions.assertFalse(result);
    }

    @Test
    void evalStringConcatenationLiteral() {
        Script script = processor.load("'foo' + 'bar'");
        String result = script.eval(Bindings.builder().standard());
        Assertions.assertEquals("foobar", result);
    }

    // -----------------------------------------------------------------------
    // Evaluation using bound variables (via DelegatingBindings / GLOBAL_SCOPE)
    // -----------------------------------------------------------------------

    @Test
    void evalReadsSingleBoundVariable() {
        Script script = processor.load("x + 1");
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("x", 9);
        Object result = script.eval(bindings);
        Assertions.assertEquals(10, ((Number) result).intValue());
    }

    @Test
    void evalReadsMultipleBoundVariables() {
        Script script = processor.load("a + b + c");
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("a", 1);
        bindings.bind("b", 2);
        bindings.bind("c", 3);
        Object result = script.eval(bindings);
        Assertions.assertEquals(6, ((Number) result).intValue());
    }

    @Test
    void evalStringConcatenationWithBoundVariables() {
        Script script = processor.load("firstName + ' ' + lastName");
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("firstName", "John");
        bindings.bind("lastName", "Doe");
        String result = script.eval(bindings);
        Assertions.assertEquals("John Doe", result);
    }

    @Test
    void evalBooleanExpressionWithBoundVariableTrue() {
        Script script = processor.load("age >= 18");
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("age", 21);
        Boolean result = script.eval(bindings);
        Assertions.assertTrue(result);
    }

    @Test
    void evalBooleanExpressionWithBoundVariableFalse() {
        Script script = processor.load("age >= 18");
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("age", 15);
        Boolean result = script.eval(bindings);
        Assertions.assertFalse(result);
    }

    @Test
    void evalWithDifferentBindingsEachCall() {
        Script script = processor.load("price * quantity");
        Bindings b1 = Bindings.builder().standard();
        b1.bind("price", 10);
        b1.bind("quantity", 3);

        Bindings b2 = Bindings.builder().standard();
        b2.bind("price", 5);
        b2.bind("quantity", 7);

        Object r1 = script.eval(b1);
        Object r2 = script.eval(b2);
        Assertions.assertEquals(30, ((Number) r1).intValue());
        Assertions.assertEquals(35, ((Number) r2).intValue());
    }

    // -----------------------------------------------------------------------
    // Compiled script evaluated multiple times (reuse)
    // -----------------------------------------------------------------------

    @Test
    void compiledScriptCanBeEvaluatedRepeatedlyWithDifferentBindings() {
        Script script = processor.load("x * x");

        for (int i = 1; i <= 5; i++) {
            Bindings bindings = Bindings.builder().standard();
            bindings.bind("x", i);
            Object result = script.eval(bindings);
            Assertions.assertEquals(i * i, ((Number) result).intValue(), "x=" + i);
        }
    }

    // -----------------------------------------------------------------------
    // Multi-statement script
    // -----------------------------------------------------------------------

    @Test
    void evalMultiStatementScriptReturnsLastExpression() {
        // The return value of eval() is the last expression's value
        Script script = processor.load("var sum = 0; for (var i = 1; i <= n; i++) { sum += i; } sum;");
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("n", 10);
        Object result = script.eval(bindings);
        Assertions.assertEquals(55, ((Number) result).intValue());
    }

    @Test
    void evalTernaryExpression() {
        Script script = processor.load("score >= 60 ? 'pass' : 'fail'");
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("score", 75);
        String result = script.eval(bindings);
        Assertions.assertEquals("pass", result);

        bindings.getBinding("score").setValue(40);
        String result2 = script.eval(bindings);
        Assertions.assertEquals("fail", result2);
    }

    // -----------------------------------------------------------------------
    // PlainScript evaluation
    // -----------------------------------------------------------------------

    @Test
    void plainScriptEvalReturnsCorrectValue() {
        // Create a fresh engine to avoid ENGINE_SCOPE contamination
        ScriptEngine engine = createScriptEngine(createEngine());
        PlainScript plain = new PlainScript(engine, "x + y");

        Bindings bindings = Bindings.builder().standard();
        bindings.bind("x", 4);
        bindings.bind("y", 6);
        Object result = plain.eval(bindings);
        Assertions.assertEquals(10, ((Number) result).intValue());
    }

    @Test
    void plainScriptRuntimeErrorThrowsEvaluationException() {
        ScriptEngine engine = createScriptEngine(createEngine());
        PlainScript plain = new PlainScript(engine, "undeclaredFn()");
        Assertions.assertThrows(EvaluationException.class,
                () -> plain.eval(Bindings.builder().standard()));
    }

    // -----------------------------------------------------------------------
    // Error handling
    // -----------------------------------------------------------------------

    @Test
    void evalReferenceErrorThrowsEvaluationException() {
        Script script = processor.load("undeclaredFunction()");
        Assertions.assertThrows(EvaluationException.class,
                () -> script.eval(Bindings.builder().standard()));
    }

    @Test
    void evaluationExceptionContainsOriginalScriptSource() {
        String source = "nonExistentFn()";
        Script script = processor.load(source);
        EvaluationException ex = Assertions.assertThrows(EvaluationException.class,
                () -> script.eval(Bindings.builder().standard()));
        Assertions.assertEquals(source, ex.getScript());
    }

    @Test
    void evalWithNullBindingsThrowsException() {
        Script script = processor.load("1 + 1");
        Assertions.assertThrows(Exception.class, () -> script.eval(null));
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
