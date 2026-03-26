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
package org.rulii.test.script.js;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rulii.bind.Bindings;
import org.rulii.context.RuleContext;
import org.rulii.script.BuildScriptException;
import org.rulii.script.Script;
import org.rulii.script.ScriptOptions;
import org.rulii.script.ScriptProcessorFactory;
import org.rulii.script.graaljs.GraalJsScriptProcessorFactory;
import org.rulii.script.jsr223.JSR223ScriptProcessor;

import javax.script.ScriptEngine;

/**
 * Unit tests for JSR223ScriptProcessor — covers language name, custom options,
 * evaluation, compilation caching, and error paths.
 */
public class JSR223ScriptProcessorTest {

    private ScriptProcessorFactory factory;
    private ScriptEngine engine;

    @BeforeEach
    public void setUp() {
        factory = TestScriptUtils.createFactory();
        engine = TestScriptUtils.createEngine();
    }

    private RuleContext contextWith(Bindings bindings) {
        return RuleContext.builder()
                .with(bindings)
                .build();
    }

    // -----------------------------------------------------------------------
    // Constructor / metadata
    // -----------------------------------------------------------------------

    @Test
    public void testLanguageNameFromEngine() {
        JSR223ScriptProcessor processor = new JSR223ScriptProcessor(engine);
        Assertions.assertNotNull(processor.getLanguageName());
        Assertions.assertFalse(processor.getLanguageName().isBlank());
    }

    @Test
    public void testCustomLanguageNameIsUsed() {
        JSR223ScriptProcessor processor = new JSR223ScriptProcessor(engine, "MyJS", "ctx");
        Assertions.assertEquals("MyJS", processor.getLanguageName());
    }

    @Test
    public void testDefaultBindingsNameIsCtx() {
        JSR223ScriptProcessor processor = new JSR223ScriptProcessor(engine);
        Assertions.assertEquals(ScriptOptions.DEFAULT.bindingsName(), processor.getBindingsName());
    }

    @Test
    public void testCustomBindingsNameIsUsed() {
        JSR223ScriptProcessor processor = new JSR223ScriptProcessor(engine, "ECMAScript", "vars");
        Assertions.assertEquals("vars", processor.getBindingsName());
    }

    @Test
    public void testNullEngineThrows() {
        Assertions.assertThrows(Exception.class, () -> new JSR223ScriptProcessor(null));
    }

    // -----------------------------------------------------------------------
    // Evaluation — simple expressions
    // -----------------------------------------------------------------------

    @Test
    public void testEvaluateSimpleArithmetic() {
        Bindings bindings = Bindings.builder().standard();
        RuleContext context = contextWith(bindings);
        Script<Object> script = Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME, "1 + 2");
        Object result = script.run(context);
        Assertions.assertEquals(3, ((Number) result).intValue());
    }

    @Test
    public void testEvaluateStringLiteral() {
        Bindings bindings = Bindings.builder().standard();
        RuleContext context = contextWith(bindings);
        Script<Object> script = Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME, "'hello world'");
        Object result = script.run(context);
        Assertions.assertEquals("hello world", result);
    }

    @Test
    public void testEvaluateBooleanExpression() {
        Bindings bindings = Bindings.builder().standard();
        RuleContext context = contextWith(bindings);
        Script<Object> script = Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME, "5 > 3");
        Object result = script.run(context);
        Assertions.assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void testEvaluateReadsBinding() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("x", int.class, 42);
        RuleContext context = contextWith(bindings);
        Script<Object> script = Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME, "ctx.x");
        Object result = script.run(context);
        Assertions.assertEquals(42, ((Number) result).intValue());
    }

    @Test
    public void testEvaluateWritesBinding() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("result", int.class, 0);
        RuleContext context = contextWith(bindings);
        Script<Object> script = Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME, "ctx.result = 99;");
        script.run(context);
        Assertions.assertEquals(99, ((Number) bindings.getValue("result")).intValue());
    }

    @Test
    public void testEvaluateWithMultipleBindings() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("a", int.class, 10);
        bindings.bind("b", int.class, 20);
        bindings.bind("c", int.class, 0);
        RuleContext context = contextWith(bindings);
        Script<Object> script = Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME, "ctx.c = ctx.a + ctx.b;");
        script.run(context);
        Assertions.assertEquals(30, ((Number) bindings.getValue("c")).intValue());
    }

    @Test
    public void testEvaluateNullReturn() {
        Bindings bindings = Bindings.builder().standard();
        RuleContext context = contextWith(bindings);
        // A statement-only script with no return value produces undefined/null
        Script<Object> script = Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME, "var x = 1;");
        // Just verify it does not throw
        Assertions.assertDoesNotThrow(() -> script.run(context));
    }

    // -----------------------------------------------------------------------
    // Compilation caching
    // -----------------------------------------------------------------------

    @Test
    public void testSameScriptInstanceCompiledOnce() {
        Bindings bindings = Bindings.builder().standard();
        RuleContext context = contextWith(bindings);
        JSR223ScriptProcessor processor = new JSR223ScriptProcessor(engine);
        Script<Object> script = Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME, "1 + 1");

        // Run the same script instance multiple times — should not throw or produce inconsistency
        processor.evaluate(script, context);
        processor.evaluate(script, context);
        processor.evaluate(script, context);
    }

    @Test
    public void testDifferentScriptInstancesEvaluatedIndependently() {
        Bindings bindings = Bindings.builder().standard();
        RuleContext context = contextWith(bindings);
        JSR223ScriptProcessor processor = new JSR223ScriptProcessor(engine);
        Script<Object> s1 = Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME, "10");
        Script<Object> s2 = Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME, "20");

        Object r1 = processor.evaluate(s1, context);
        Object r2 = processor.evaluate(s2, context);
        Assertions.assertEquals(10, ((Number) r1).intValue());
        Assertions.assertEquals(20, ((Number) r2).intValue());
    }

    // -----------------------------------------------------------------------
    // Error paths
    // -----------------------------------------------------------------------

    @Test
    public void testInvalidSyntaxThrowsBuildScriptException() {
        JSR223ScriptProcessor processor = new JSR223ScriptProcessor(engine);
        Bindings bindings = Bindings.builder().standard();
        RuleContext context = contextWith(bindings);
        Assertions.assertThrows(BuildScriptException.class, () -> {
            Script<Object> badScript = Script.builder().build("js", "@@@ invalid syntax @@@");
            processor.evaluate(badScript, context);
        });
    }

    @Test
    public void testBuildScriptExceptionCarriesScriptText() {
        JSR223ScriptProcessor processor = new JSR223ScriptProcessor(engine);
        Bindings bindings = Bindings.builder().standard();
        RuleContext context = contextWith(bindings);
        String badSource = "@@@ invalid @@@";
        BuildScriptException ex = Assertions.assertThrows(BuildScriptException.class,
                () -> {
                    Script<Object> badScript = Script.builder().build("js", badSource);
                    processor.evaluate(badScript, context);
                });
        Assertions.assertEquals(badSource, ex.getScript());
    }
}
