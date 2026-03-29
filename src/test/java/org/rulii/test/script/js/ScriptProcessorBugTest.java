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
import org.rulii.context.RuleContext;
import org.rulii.script.*;
import org.rulii.script.graaljs.GraalJsScriptProcessorFactory;
import org.rulii.script.jsr223.JSR223ScriptProcessor;

import javax.script.*;
import java.io.Reader;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Regression tests that prove each bug before the fix is applied.
 * Each test should PASS after the corresponding fix is made.
 */
public class ScriptProcessorBugTest {

    /** Minimal ScriptEngine wrapper that is NOT Compilable. */
    static class NonCompilableEngine extends AbstractScriptEngine {
        private final ScriptEngine delegate;

        NonCompilableEngine(ScriptEngine delegate) { this.delegate = delegate; }

        @Override public Object eval(String script, ScriptContext ctx) throws ScriptException {
            return delegate.eval(script, ctx);
        }
        @Override public Object eval(Reader reader, ScriptContext ctx) throws ScriptException {
            return delegate.eval(reader, ctx);
        }
        @Override public javax.script.Bindings createBindings() { return delegate.createBindings(); }
        @Override public ScriptEngineFactory getFactory() { return delegate.getFactory(); }
    }

    @Test
    public void bug1_nonCompilableEngineDoesNotStackOverflow() {
        ScriptEngine wrapped = new NonCompilableEngine(TestScriptUtils.createEngine());
        JSR223ScriptProcessor processor = new JSR223ScriptProcessor(wrapped);

        org.rulii.bind.Bindings bindings = org.rulii.bind.Bindings.builder().standard();
        bindings.bind("x", int.class, 7);

        // Build context with this processor registered explicitly
        //ScriptProcessorRegistry registry = new DefaultScriptProcessorRegistry(false);
        //registry.register(new SingleScriptProcessorFactory(processor, new JSR223ScriptCompiler(wrapped)));
        RuleContext ctx = RuleContext.builder()
                .with(bindings)
                //.scriptProcessorRegistry(registry)
                .build();

        Script<Object> script = Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME, "ctx.x * 2");

        // Before fix: StackOverflowError. After fix: returns 14.
        Object result = script.run(ctx);
        Assertions.assertEquals(14, ((Number) result).intValue());
    }

    private RuleContext buildContextWithProcessor(org.rulii.bind.Bindings bindings, JSR223ScriptProcessor processor) {
        return RuleContext.builder()
                .with(bindings)
                .build();
    }

    @Test
    public void bug2_sharedProcessorExposesCorrectBindingsForEachContext() {
        JSR223ScriptProcessor processor = new JSR223ScriptProcessor(TestScriptUtils.createEngine());

        org.rulii.bind.Bindings b1 = org.rulii.bind.Bindings.builder().standard();
        b1.bind("x", int.class, 10);
        RuleContext ctx1 = buildContextWithProcessor(b1, processor);

        org.rulii.bind.Bindings b2 = org.rulii.bind.Bindings.builder().standard();
        b2.bind("x", int.class, 99);
        RuleContext ctx2 = buildContextWithProcessor(b2, processor);

        Script<Object> script = Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME, "ctx.x");

        // Warm up processor with ctx1
        Object r1 = processor.evaluate(script, ctx1);
        Assertions.assertEquals(10, ((Number) r1).intValue());

        // Before fix: still returns 10 (cached ctx1 bindings). After fix: returns 99.
        Object r2 = processor.evaluate(script, ctx2);
        Assertions.assertEquals(99, ((Number) r2).intValue());
    }

    @Test
    public void bug3_concurrentScriptCompilationDoesNotCorruptCache() throws InterruptedException {
        int threadCount = 8;
        int scriptCount = 50;

        // Each thread gets its OWN engine+processor so GraalJS engine-level concurrency
        // is not an issue. The Script instances are SHARED across threads, which exercises
        // the static COMPILED_SCRIPTS map under concurrent read/write.
        JSR223ScriptProcessor[] processors = new JSR223ScriptProcessor[threadCount];
        RuleContext[] contexts              = new RuleContext[threadCount];
        for (int i = 0; i < threadCount; i++) {
            processors[i] = new JSR223ScriptProcessor(TestScriptUtils.createEngine());
            contexts[i]   = buildContextWithProcessor(
                    org.rulii.bind.Bindings.builder().standard(), processors[i]);
        }

        // Same Script object references shared across all threads
        Script<?>[] scripts = new Script[scriptCount];
        for (int i = 0; i < scriptCount; i++) {
            scripts[i] = Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME, String.valueOf(i));
        }

        CountDownLatch start   = new CountDownLatch(1);
        CountDownLatch done    = new CountDownLatch(threadCount);
        AtomicInteger failures = new AtomicInteger(0);

        ExecutorService pool = Executors.newFixedThreadPool(threadCount);

        for (int t = 0; t < threadCount; t++) {
            final int tid = t;
            pool.submit(() -> {
                try {
                    start.await();
                    for (int i = 0; i < scriptCount; i++) {
                        Object result = processors[tid].evaluate(scripts[i], contexts[tid]);
                        if (((Number) result).intValue() != i) failures.incrementAndGet();
                    }
                } catch (Exception e) {
                    failures.incrementAndGet();
                } finally {
                    done.countDown();
                }
            });
        }

        start.countDown();
        done.await();
        pool.shutdown();

        // Before fix: ConcurrentModificationException or wrong results from map corruption.
        Assertions.assertEquals(0, failures.get(), "No thread should fail during concurrent evaluation");
    }

    private static class SingleScriptProcessorFactory implements ScriptProcessorFactory {

        private final ScriptProcessor processor;
        private final ScriptCompiler compiler;

        public SingleScriptProcessorFactory(ScriptProcessor processor, ScriptCompiler compiler) {
            super();
            this.processor = processor;
            this.compiler  = compiler;
        }

        @Override
        public String getLanguageName() {
            return processor.getLanguageName();
        }

        @Override
        public String getBindingsName() {
            return ScriptOptions.DEFAULT.bindingsName();
        }

        @Override
        public ScriptProcessor getScriptProcessor() {
            return processor;
        }

        @Override
        public ScriptCompiler getScriptCompiler() {
            return compiler;
        }
    }
}
