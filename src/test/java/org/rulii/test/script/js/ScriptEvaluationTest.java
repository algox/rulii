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
import org.junit.jupiter.api.Test;
import org.rulii.bind.Bindings;
import org.rulii.context.RuleContext;
import org.rulii.script.BuildScriptException;
import org.rulii.script.Script;
import org.rulii.script.graaljs.GraalJsScriptProcessorFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Integration tests for Script.run() through a full RuleContext pipeline.
 * Covers data types, bindings mutation, multi-scope, and error propagation.
 */
public class ScriptEvaluationTest {

    private RuleContext contextWith(Bindings bindings) {
        return RuleContext.builder()
                .with(bindings)
                .build();
    }

    // -----------------------------------------------------------------------
    // Basic data-type returns
    // -----------------------------------------------------------------------

    @Test
    public void testReturnsInteger() {
        RuleContext ctx = contextWith(Bindings.builder().standard());
        Object result = Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME, "7").run(ctx);
        Assertions.assertEquals(7, ((Number) result).intValue());
    }

    @Test
    public void testReturnsDouble() {
        RuleContext ctx = contextWith(Bindings.builder().standard());
        Object result = Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME, "3.14").run(ctx);
        Assertions.assertEquals(3.14, ((Number) result).doubleValue(), 0.001);
    }

    @Test
    public void testReturnsString() {
        RuleContext ctx = contextWith(Bindings.builder().standard());
        Object result = Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME, "'rulii'").run(ctx);
        Assertions.assertEquals("rulii", result);
    }

    @Test
    public void testReturnsTrue() {
        RuleContext ctx = contextWith(Bindings.builder().standard());
        Object result = Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME, "true").run(ctx);
        Assertions.assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void testReturnsFalse() {
        RuleContext ctx = contextWith(Bindings.builder().standard());
        Object result = Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME, "false").run(ctx);
        Assertions.assertEquals(Boolean.FALSE, result);
    }

    // -----------------------------------------------------------------------
    // Reading from bindings via ctx
    // -----------------------------------------------------------------------

    @Test
    public void testReadIntBinding() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("num", int.class, 55);
        RuleContext ctx = contextWith(bindings);
        Object result = Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME, "ctx.num").run(ctx);
        Assertions.assertEquals(55, ((Number) result).intValue());
    }

    @Test
    public void testReadStringBinding() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("greeting", String.class, "hello");
        RuleContext ctx = contextWith(bindings);
        Object result = Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME, "ctx.greeting").run(ctx);
        Assertions.assertEquals("hello", result);
    }

    @Test
    public void testReadBooleanBinding() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("active", boolean.class, true);
        RuleContext ctx = contextWith(bindings);
        Object result = Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME, "ctx.active").run(ctx);
        Assertions.assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void testComputeFromTwoBindings() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("price", double.class, 9.99);
        bindings.bind("qty",   int.class,    3);
        RuleContext ctx = contextWith(bindings);
        Object result = Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME, "ctx.price * ctx.qty").run(ctx);
        Assertions.assertEquals(29.97, ((Number) result).doubleValue(), 0.001);
    }

    // -----------------------------------------------------------------------
    // Mutating bindings via ctx
    // -----------------------------------------------------------------------

    @Test
    public void testWriteBackToBinding() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("total", int.class, 0);
        RuleContext ctx = contextWith(bindings);
        Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME, "ctx.total = 100;").run(ctx);
        Assertions.assertEquals(100, ((Number) bindings.getValue("total")).intValue());
    }

    @Test
    public void testWriteBackStringToBinding() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("msg", String.class, "");
        RuleContext ctx = contextWith(bindings);
        Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME, "ctx.msg = 'updated';").run(ctx);
        Assertions.assertEquals("updated", bindings.getValue("msg"));
    }

    @Test
    public void testAccumulateAcrossMultipleRuns() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("counter", int.class, 0);
        RuleContext ctx = contextWith(bindings);
        Script<Object> inc = Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME, "ctx.counter = ctx.counter + 1;");
        inc.run(ctx);
        inc.run(ctx);
        inc.run(ctx);
        Assertions.assertEquals(3, ((Number) bindings.getValue("counter")).intValue());
    }

    // -----------------------------------------------------------------------
    // Multi-statement scripts
    // -----------------------------------------------------------------------

    @Test
    public void testMultiStatementScript() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("x", int.class, 10);
        bindings.bind("y", int.class, 0);
        RuleContext ctx = contextWith(bindings);
        Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME,
                "var temp = ctx.x * 2; ctx.y = temp + 5;").run(ctx);
        Assertions.assertEquals(25, ((Number) bindings.getValue("y")).intValue());
    }

    @Test
    public void testConditionalLogicInScript() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("age",    int.class,     20);
        bindings.bind("result", String.class, "");
        RuleContext ctx = contextWith(bindings);
        Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME,
                "ctx.result = ctx.age >= 18 ? 'adult' : 'minor';").run(ctx);
        Assertions.assertEquals("adult", bindings.getValue("result"));
    }

    @Test
    public void testConditionalMinorPath() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("age",    int.class,    15);
        bindings.bind("result", String.class, "");
        RuleContext ctx = contextWith(bindings);
        Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME,
                "ctx.result = ctx.age >= 18 ? 'adult' : 'minor';").run(ctx);
        Assertions.assertEquals("minor", bindings.getValue("result"));
    }

    // -----------------------------------------------------------------------
    // Error paths
    // -----------------------------------------------------------------------

    @Test
    public void testInvalidScriptSyntaxThrows() {
        RuleContext ctx = contextWith(Bindings.builder().standard());
        Assertions.assertThrows(BuildScriptException.class, () -> {
            Script<Object> bad = Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME, "<<< not valid >>>");
            bad.run(ctx);
        });
    }

    @Test
    public void testScriptForUnknownLanguageThrows() {
        RuleContext ctx = RuleContext.builder().build();
        Assertions.assertThrows(Exception.class, () -> {
            Script<Object> script = Script.builder().build("NoSuchLang", "1 + 1");
            script.run(ctx);
        });
    }

    // -----------------------------------------------------------------------
    // Different Script instances are independent
    // -----------------------------------------------------------------------

    @Test
    public void testTwoScriptsAreMutuallyIndependent() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("a", int.class, 5);
        bindings.bind("b", int.class, 3);
        RuleContext ctx = contextWith(bindings);

        Object sum  = Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME, "ctx.a + ctx.b").run(ctx);
        Object diff = Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME, "ctx.a - ctx.b").run(ctx);

        Assertions.assertEquals(8, ((Number) sum).intValue());
        Assertions.assertEquals(2, ((Number) diff).intValue());
    }

    @Test
    public void testSameScriptReusedAcrossContexts() {
        Script<Object> script = Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME, "ctx.v * 2");

        Bindings b1 = Bindings.builder().standard();
        b1.bind("v", int.class, 3);
        Object r1 = script.run(contextWith(b1));

        Bindings b2 = Bindings.builder().standard();
        b2.bind("v", int.class, 7);
        Object r2 = script.run(contextWith(b2));

        Assertions.assertEquals(6,  ((Number) r1).intValue());
        Assertions.assertEquals(14, ((Number) r2).intValue());
    }

    // -----------------------------------------------------------------------
    // Concurrent execution — GraalJsScriptProcessorFactory shares a single
    // Engine across calls/threads; each evaluation must still get its own
    // isolated bindings/context, with no cross-thread state leakage.
    // -----------------------------------------------------------------------

    @Test
    public void testConcurrentEvaluationsAcrossThreads_doNotShareGlobalState() throws Exception {
        int threadCount = 16;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CyclicBarrier barrier = new CyclicBarrier(threadCount);
        List<Future<Object>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            int value = i;
            futures.add(executor.submit(() -> {
                Bindings bindings = Bindings.builder().standard();
                bindings.bind("n", int.class, value);
                RuleContext ctx = contextWith(bindings);
                barrier.await();
                // A top-level "var" declaration would be visible to every later evaluation if the
                // engine/context (not just the Engine) were shared across threads without isolation.
                return Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME,
                        "var shared = ctx.n; shared;").run(ctx);
            }));
        }

        for (int i = 0; i < threadCount; i++) {
            Assertions.assertEquals(i, ((Number) futures.get(i).get()).intValue());
        }

        executor.shutdown();
    }
}
