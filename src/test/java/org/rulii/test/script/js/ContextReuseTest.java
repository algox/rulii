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

import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.bind.Bindings;
import org.rulii.context.RuleContext;
import org.rulii.script.EvaluationException;
import org.rulii.script.Script;
import org.rulii.script.ScriptProcessor;
import org.rulii.script.graaljs.GraalJsPoolOptions;
import org.rulii.script.graaljs.GraalJsPoolStatistics;
import org.rulii.script.graaljs.GraalJsScriptProcessorFactory;
import org.rulii.script.jsr223.JSR223ScriptProcessor;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A {@link GraalJsScriptProcessorFactory} that reuses JavaScript contexts ({@link GraalJsPoolOptions}): scripts
 * give what they give in a context of their own, what a script declares or leaves in the global environment does
 * not reach the next one, and the contexts are kept as the options say.
 *
 * @author Max Arulananthan
 * @since 2.3
 */
public class ContextReuseTest {

    public ContextReuseTest() {
        super();
    }

    private static final GraalJsPoolOptions ONE_CONTEXT = GraalJsPoolOptions.builder().maxIdle(1).build();

    private static GraalJsScriptProcessorFactory reusing(GraalJsPoolOptions options) {
        return new GraalJsScriptProcessorFactory(GraalJsScriptProcessorFactory.LANGUAGE_NAME, "ctx", options);
    }

    // A factory as it is when nothing asks for reuse, however this JVM was started.
    private static GraalJsScriptProcessorFactory notReusing() {
        return withoutTheProperty(() -> new GraalJsScriptProcessorFactory(GraalJsScriptProcessorFactory.LANGUAGE_NAME, "ctx"));
    }

    private static <T> T withoutTheProperty(Supplier<T> supplier) {
        String before = System.getProperty(GraalJsScriptProcessorFactory.REUSE_CONTEXTS_PROPERTY);
        System.clearProperty(GraalJsScriptProcessorFactory.REUSE_CONTEXTS_PROPERTY);

        try {
            return supplier.get();
        } finally {
            if (before != null) System.setProperty(GraalJsScriptProcessorFactory.REUSE_CONTEXTS_PROPERTY, before);
        }
    }

    private static RuleContext context() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("age", Integer.class, 42);
        bindings.bind("name", String.class, "Max");
        bindings.bind("total", Integer.class, 0);
        return RuleContext.builder().with(bindings).build();
    }

    private static Object evaluate(GraalJsScriptProcessorFactory factory, String text) {
        return evaluate(factory, text, context());
    }

    private static Object evaluate(GraalJsScriptProcessorFactory factory, String text, RuleContext context) {
        Script<Object> script = factory.getScriptCompiler().compile(text, Object.class);
        return factory.getScriptProcessor().evaluate(script, context);
    }

    // ---- off unless asked for ------------------------------------------------------------------

    @Test
    public void testContextsAreNotReusedUnlessAskedFor() {
        GraalJsScriptProcessorFactory factory = notReusing();

        Assertions.assertFalse(factory.isReuseContexts());
        Assertions.assertNull(factory.getPoolOptions());
        Assertions.assertNull(factory.getPoolStatistics());
        Assertions.assertFalse(withoutTheProperty(GraalJsScriptProcessorFactory::new).isReuseContexts());

        // Nothing to discard or close, and the factory works as ever.
        factory.discardContexts();
        factory.close();
        Assertions.assertEquals(Boolean.TRUE, evaluate(factory, "ctx.age >= 18"));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new GraalJsScriptProcessorFactory("js", "ctx", (GraalJsPoolOptions) null));
    }

    @Test
    public void testSystemPropertiesTurnReuseOnAndSetTheOptions() {
        String[] names = {GraalJsScriptProcessorFactory.REUSE_CONTEXTS_PROPERTY, GraalJsPoolOptions.PROPERTY_PREFIX + "maxIdle",
                GraalJsPoolOptions.PROPERTY_PREFIX + "idleTimeout", GraalJsPoolOptions.PROPERTY_PREFIX + "maxAge",
                GraalJsPoolOptions.PROPERTY_PREFIX + "maxUses", GraalJsPoolOptions.PROPERTY_PREFIX + "initialSize",
                GraalJsPoolOptions.PROPERTY_PREFIX + "cleanGlobals"};
        String[] before = new String[names.length];
        for (int i = 0; i < names.length; i++) before[i] = System.getProperty(names[i]);

        try {
            for (String name : names) System.clearProperty(name);
            Assertions.assertEquals(GraalJsPoolOptions.DEFAULT, GraalJsPoolOptions.fromSystemProperties());

            System.setProperty(names[0], "true");
            System.setProperty(names[1], "3");
            System.setProperty(names[2], "PT10S");
            System.setProperty(names[3], "PT2M");
            System.setProperty(names[4], "50");
            System.setProperty(names[5], "2");
            System.setProperty(names[6], "false");

            GraalJsScriptProcessorFactory factory = new GraalJsScriptProcessorFactory();
            Assertions.assertTrue(factory.isReuseContexts());
            Assertions.assertEquals(new GraalJsPoolOptions(3, Duration.ofSeconds(10), Duration.ofMinutes(2), 50, 2, false),
                    factory.getPoolOptions());
            Assertions.assertTrue(new GraalJsScriptProcessorFactory("ECMAScript", "ctx").isReuseContexts());

            System.setProperty(names[3], "soon");
            Assertions.assertThrows(IllegalArgumentException.class, GraalJsPoolOptions::fromSystemProperties);
            System.setProperty(names[3], "PT2M");
            System.setProperty(names[1], "many");
            Assertions.assertThrows(IllegalArgumentException.class, GraalJsPoolOptions::fromSystemProperties);

            // The options are read only when reuse is on.
            System.setProperty(names[0], "false");
            Assertions.assertFalse(new GraalJsScriptProcessorFactory().isReuseContexts());
        } finally {
            for (int i = 0; i < names.length; i++) {
                if (before[i] == null) System.clearProperty(names[i]);
                else System.setProperty(names[i], before[i]);
            }
        }
    }

    @Test
    public void testOptionsHaveDefaultsAndRefuseWhatMakesNoSense() {
        GraalJsPoolOptions defaults = GraalJsPoolOptions.DEFAULT;
        Assertions.assertEquals(GraalJsPoolOptions.DEFAULT_MAX_IDLE, defaults.maxIdle());
        Assertions.assertTrue(defaults.maxIdle() >= 2);
        Assertions.assertEquals(Duration.ofMinutes(5), defaults.idleTimeout());
        Assertions.assertEquals(Duration.ofMinutes(30), defaults.maxAge());
        Assertions.assertEquals(1_000, defaults.maxUses());
        Assertions.assertEquals(0, defaults.initialSize());
        Assertions.assertTrue(defaults.cleanGlobals());
        Assertions.assertEquals(defaults, GraalJsPoolOptions.builder().build());

        GraalJsPoolOptions.Builder builder = GraalJsPoolOptions.builder();
        Assertions.assertThrows(IllegalArgumentException.class, () -> builder.maxIdle(0).build());
        Assertions.assertThrows(IllegalArgumentException.class, () -> GraalJsPoolOptions.builder().idleTimeout(null).build());
        Assertions.assertThrows(IllegalArgumentException.class, () -> GraalJsPoolOptions.builder().idleTimeout(Duration.ofSeconds(-1)).build());
        Assertions.assertThrows(IllegalArgumentException.class, () -> GraalJsPoolOptions.builder().maxAge(null).build());
        Assertions.assertThrows(IllegalArgumentException.class, () -> GraalJsPoolOptions.builder().maxAge(Duration.ofSeconds(-1)).build());
        Assertions.assertThrows(IllegalArgumentException.class, () -> GraalJsPoolOptions.builder().maxUses(-1).build());
        Assertions.assertThrows(IllegalArgumentException.class, () -> GraalJsPoolOptions.builder().initialSize(-1).build());
        Assertions.assertThrows(IllegalArgumentException.class, () -> GraalJsPoolOptions.builder().maxIdle(2).initialSize(3).build());

        // No limit is a choice.
        GraalJsPoolOptions unlimited = GraalJsPoolOptions.builder().idleTimeout(Duration.ZERO).maxAge(Duration.ZERO).maxUses(0).build();
        Assertions.assertEquals(0, unlimited.maxUses());
    }

    // ---- scripts give what they give in a context of their own -----------------------------------

    @Test
    public void testScriptsGiveTheSameWithAndWithoutReuse() {
        GraalJsScriptProcessorFactory reusing = reusing(ONE_CONTEXT);
        GraalJsScriptProcessorFactory fresh = notReusing();
        String[] scripts = {"ctx.age >= 18", "ctx.age + 1", "ctx.name + '!'", "null", "undefined", "ctx.missing", "[1, 2, 3]",
                "({a: 1, b: [1, 2]})", "new Map([['k', 1]])", "var limit = 18; ctx.age >= limit", "const limit = 18; ctx.age >= limit",
                "let n = ctx.age; n = n + 1; n", "function over(a) { return a >= 18; } over(ctx.age)",
                "if (ctx.age > 18) { 'adult' } else { 'minor' }", "typeof this.ctx", "this.ctx.age", "typeof globalThis.ctx",
                "new Function('return ctx.age')()", "(0, eval)('ctx.age')", "eval('ctx.age + 1')", "this === globalThis",
                "JSON.stringify({a: ctx.age})", "[1, 2, 3].map(x => x * 2)", "'use strict'; ctx.age", "ctx.age\n>= 18", "// a comment\nctx.age"};

        for (String script : scripts) {
            Object expected = evaluate(fresh, script);

            // Three times over: the second and third run in a context the first has been through.
            for (int run = 0; run < 3; run++) {
                Assertions.assertEquals(expected, evaluate(reusing, script), script);
            }
        }

        GraalJsPoolStatistics statistics = reusing.getPoolStatistics();
        Assertions.assertEquals(1, statistics.created());
        Assertions.assertEquals(scripts.length * 3L - 1, statistics.reused());
        Assertions.assertEquals(0, statistics.cleaned());
        Assertions.assertEquals(0, statistics.failed());
        reusing.close();
    }

    @Test
    public void testAnActionWritesIntoTheBindings() {
        GraalJsScriptProcessorFactory factory = reusing(ONE_CONTEXT);
        RuleContext context = context();

        evaluate(factory, "ctx.total = ctx.age * 2", context);
        Assertions.assertEquals(84, context.getBindings().getValue("total", Integer.class).intValue());

        // The next evaluation gets its own bindings, not the last one's.
        RuleContext other = context();
        other.getBindings().setValue("age", 7);
        Assertions.assertEquals(7, ((Number) evaluate(factory, "ctx.age", other)).intValue());
        factory.close();
    }

    @Test
    public void testFailuresReadTheSameAndCloseTheContext() {
        GraalJsScriptProcessorFactory reusing = reusing(ONE_CONTEXT);
        GraalJsScriptProcessorFactory fresh = notReusing();
        String[] failing = {"nope.field", "null.x", "throw new Error('boom')", "throw 'plain text'", "undefinedFunction()",
                "delete ctx.age", "ctx.age = 'text'"};

        for (String script : failing) {
            EvaluationException expected = Assertions.assertThrows(EvaluationException.class, () -> evaluate(fresh, script), script);
            EvaluationException actual = Assertions.assertThrows(EvaluationException.class, () -> evaluate(reusing, script), script);
            Assertions.assertEquals(expected.getMessage(), actual.getMessage(), script);
            Assertions.assertEquals(expected.getCause().getClass(), actual.getCause().getClass(), script);
        }

        GraalJsPoolStatistics statistics = reusing.getPoolStatistics();
        Assertions.assertEquals(failing.length, statistics.failed());
        Assertions.assertEquals(0, statistics.idle());
        // The pool carries on with a new context.
        Assertions.assertEquals(Boolean.TRUE, evaluate(reusing, "ctx.age >= 18"));
        Assertions.assertEquals(failing.length + 1, reusing.getPoolStatistics().created());
        reusing.close();
    }

    @Test
    public void testAFunctionHandedBackKeepsItsContextAndItsBindings() {
        GraalJsScriptProcessorFactory factory = reusing(ONE_CONTEXT);

        Object function = evaluate(factory, "(function(x) { return x + ctx.age; })");

        Assertions.assertEquals(43, Value.asValue(function).execute(1).asInt());
        GraalJsPoolStatistics statistics = factory.getPoolStatistics();
        Assertions.assertEquals(1, statistics.detached());
        Assertions.assertEquals(0, statistics.idle());

        // The next evaluation runs elsewhere, and the function still works afterwards.
        Assertions.assertEquals(Boolean.TRUE, evaluate(factory, "ctx.age >= 18"));
        Assertions.assertEquals(2, factory.getPoolStatistics().created());
        Assertions.assertEquals(44, Value.asValue(function).execute(2).asInt());
        factory.close();
    }

    // ---- what a script leaves behind -------------------------------------------------------------

    @Test
    public void testWhatAScriptDeclaresEndsWithIt() {
        GraalJsScriptProcessorFactory factory = reusing(ONE_CONTEXT);

        evaluate(factory, "var a = 1; let b = 2; const c = 3; function d() { return 4; } a + b + c + d()");

        Assertions.assertEquals("undefined/undefined/undefined/undefined",
                evaluate(factory, "typeof a + '/' + typeof b + '/' + typeof c + '/' + typeof d"));
        Assertions.assertEquals(1, factory.getPoolStatistics().created());
        Assertions.assertEquals(0, factory.getPoolStatistics().cleaned());
        factory.close();
    }

    @Test
    public void testGlobalsAScriptLeavesBehindAreRemoved() {
        String[][] cases = {
                {"counter = (typeof counter === 'undefined') ? 1 : counter + 1; counter", "typeof counter", "undefined"},
                {"helper = function() { return 1; }; helper()", "typeof helper", "undefined"},
                {"(0, eval)('var sneaky = 1'); sneaky", "typeof sneaky", "undefined"},
                {"Object.defineProperty(globalThis, 'hidden', {value: 1, enumerable: false, configurable: true}); 1", "typeof hidden", "undefined"},
                {"delete globalThis.JSON; typeof JSON", "typeof JSON", "object"},
                {"Math = {round: function() { return -1; }}; extra = 1; Math.round(2.5)", "Math.round(2.5)", "3"},
                {"eval = function() { return 'hijacked'; }; 1", "'eval' + ' ' + 'works'", "eval works"}};

        for (String[] c : cases) {
            GraalJsScriptProcessorFactory factory = reusing(ONE_CONTEXT);
            evaluate(factory, c[0]);

            Assertions.assertEquals(c[2], String.valueOf(evaluate(factory, c[1])), c[0]);
            GraalJsPoolStatistics statistics = factory.getPoolStatistics();
            Assertions.assertEquals(1, statistics.created(), c[0]);
            Assertions.assertEquals(1, statistics.cleaned(), c[0]);
            Assertions.assertEquals(0, statistics.tarnished(), c[0]);
            factory.close();
        }
    }

    @Test
    public void testAnUndeclaredVariableStartsAfreshEveryTime() {
        GraalJsScriptProcessorFactory factory = reusing(ONE_CONTEXT);
        String script = "counter = (typeof counter === 'undefined') ? 1 : counter + 1; counter";

        for (int run = 0; run < 3; run++) {
            Assertions.assertEquals(1, ((Number) evaluate(factory, script)).intValue());
        }

        Assertions.assertEquals(1, factory.getPoolStatistics().created());
        Assertions.assertEquals(3, factory.getPoolStatistics().cleaned());
        factory.close();
    }

    @Test
    public void testGlobalsStayWhenCleaningIsOff() {
        GraalJsScriptProcessorFactory factory = reusing(GraalJsPoolOptions.builder().maxIdle(1).cleanGlobals(false).build());

        evaluate(factory, "counter = 5; counter");

        Assertions.assertEquals("number", evaluate(factory, "typeof counter"));
        Assertions.assertEquals(0, factory.getPoolStatistics().cleaned());
        factory.close();
    }

    @Test
    public void testAContextThatCannotBePutBackIsClosed() {
        String[] permanent = {"Object.defineProperty(globalThis, 'stuck', {value: 1, configurable: false}); stuck",
                "Object.defineProperty(globalThis, 'ctx', {configurable: false}); ctx.age"};

        for (String script : permanent) {
            GraalJsScriptProcessorFactory factory = reusing(ONE_CONTEXT);
            evaluate(factory, script);

            GraalJsPoolStatistics statistics = factory.getPoolStatistics();
            Assertions.assertEquals(1, statistics.tarnished(), script);
            Assertions.assertEquals(0, statistics.idle(), script);
            Assertions.assertEquals("undefined", evaluate(factory, "typeof stuck"), script);
            Assertions.assertEquals(2, factory.getPoolStatistics().created(), script);
            factory.close();
        }
    }

    @Test
    public void testAChangedBuiltInStaysUntilTheContextIsRetired() {
        GraalJsScriptProcessorFactory factory = reusing(ONE_CONTEXT);
        String change = "String.prototype.shout = function() { return this.toUpperCase() + '!'; }; ctx.name.shout()";

        Assertions.assertEquals("MAX!", evaluate(factory, change));
        // The one thing reuse does not undo.
        Assertions.assertEquals("function", evaluate(factory, "typeof ''.shout"));

        factory.discardContexts();
        Assertions.assertEquals("undefined", evaluate(factory, "typeof ''.shout"));
        factory.close();

        // One use per context: every evaluation has a context of its own again.
        GraalJsScriptProcessorFactory once = reusing(GraalJsPoolOptions.builder().maxIdle(1).maxUses(1).build());
        evaluate(once, change);
        Assertions.assertEquals("undefined", evaluate(once, "typeof ''.shout"));
        Assertions.assertEquals(0, once.getPoolStatistics().reused());
        once.close();
    }

    // ---- how the contexts are kept ---------------------------------------------------------------

    @Test
    public void testAContextIsRetiredAfterItsNumberOfUses() {
        GraalJsScriptProcessorFactory factory = reusing(GraalJsPoolOptions.builder().maxIdle(1).maxUses(3).build());

        for (int i = 0; i < 10; i++) evaluate(factory, "ctx.age >= 18");

        GraalJsPoolStatistics statistics = factory.getPoolStatistics();
        Assertions.assertEquals(4, statistics.created());
        Assertions.assertEquals(6, statistics.reused());
        Assertions.assertEquals(3, statistics.retired());
        Assertions.assertEquals(1, statistics.idle());
        factory.close();
    }

    @Test
    public void testAnIdleContextIsRetiredAfterItsIdleTime() throws InterruptedException {
        GraalJsScriptProcessorFactory factory = reusing(GraalJsPoolOptions.builder().maxIdle(1).idleTimeout(Duration.ofMillis(150)).build());

        evaluate(factory, "1");
        evaluate(factory, "1");
        Assertions.assertEquals(1, factory.getPoolStatistics().created());

        Thread.sleep(500);
        evaluate(factory, "1");

        GraalJsPoolStatistics statistics = factory.getPoolStatistics();
        Assertions.assertEquals(2, statistics.created());
        Assertions.assertEquals(1, statistics.retired());
        factory.close();
    }

    @Test
    public void testAContextIsRetiredAtItsAge() throws InterruptedException {
        GraalJsScriptProcessorFactory factory = reusing(GraalJsPoolOptions.builder().maxIdle(1).idleTimeout(Duration.ZERO)
                .maxAge(Duration.ofMillis(150)).build());

        evaluate(factory, "1");
        Thread.sleep(500);
        // Too old when it is asked for again, and too old when it is given back.
        evaluate(factory, "1");

        GraalJsPoolStatistics statistics = factory.getPoolStatistics();
        Assertions.assertEquals(2, statistics.created());
        Assertions.assertEquals(1, statistics.retired());
        factory.close();
    }

    @Test
    public void testContextsAreCreatedUpFrontWithTheFirstProcessor() {
        GraalJsScriptProcessorFactory factory = reusing(GraalJsPoolOptions.builder().maxIdle(4).initialSize(3).build());
        Assertions.assertEquals(new GraalJsPoolStatistics(0, 0, 0, 0, 0, 0, 0, 0), factory.getPoolStatistics());

        ScriptProcessor processor = factory.getScriptProcessor();
        Assertions.assertEquals(3, factory.getPoolStatistics().created());
        Assertions.assertEquals(3, factory.getPoolStatistics().idle());

        processor.evaluate(factory.getScriptCompiler().compile("1", Object.class), context());
        Assertions.assertEquals(3, factory.getPoolStatistics().created());
        Assertions.assertEquals(1, factory.getPoolStatistics().reused());
        factory.close();
        Assertions.assertEquals(0, factory.getPoolStatistics().idle());
        Assertions.assertEquals(3, factory.getPoolStatistics().retired());
    }

    @Test
    public void testEvaluationsAtTheSameMomentEachGetAContextAndTheSpareOnesAreClosed() throws Exception {
        int threads = 3;
        GraalJsScriptProcessorFactory factory = reusing(ONE_CONTEXT);
        Script<Object> script = factory.getScriptCompiler().compile("ctx.pause.apply(null); ctx.age", Object.class);
        CountDownLatch inScript = new CountDownLatch(threads);
        CountDownLatch release = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        try {
            List<Future<Object>> results = new ArrayList<>();

            for (int i = 0; i < threads; i++) {
                results.add(executor.submit(() -> {
                    Bindings bindings = Bindings.builder().standard();
                    bindings.bind("age", Integer.class, 42);
                    bindings.bind("pause", Function.class, (Function<Object, Object>) ignored -> {
                        inScript.countDown();
                        try {
                            release.await(10, TimeUnit.SECONDS);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        return null;
                    });
                    return factory.getScriptProcessor().evaluate(script, RuleContext.builder().with(bindings).build());
                }));
            }

            Assertions.assertTrue(inScript.await(30, TimeUnit.SECONDS));
            // All three are inside their script: three contexts exist and none is idle.
            Assertions.assertEquals(threads, factory.getPoolStatistics().created());
            release.countDown();

            for (Future<Object> result : results) {
                Assertions.assertEquals(42, ((Number) result.get(30, TimeUnit.SECONDS)).intValue());
            }
        } finally {
            release.countDown();
            executor.shutdownNow();
        }

        GraalJsPoolStatistics statistics = factory.getPoolStatistics();
        Assertions.assertEquals(1, statistics.idle());
        Assertions.assertEquals(threads - 1, statistics.retired());
        factory.close();
    }

    @Test
    public void testManyThreadsShareThePool() throws Exception {
        int threads = 8, each = 200;
        GraalJsScriptProcessorFactory factory = reusing(GraalJsPoolOptions.builder().maxIdle(threads).build());
        Script<Object> script = factory.getScriptCompiler().compile("var doubled = ctx.age * 2; doubled + 1", Object.class);
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        try {
            List<Future<Integer>> wrong = new ArrayList<>();

            for (int t = 0; t < threads; t++) {
                int base = t * 1_000;
                wrong.add(executor.submit(() -> {
                    int mistakes = 0;

                    for (int i = 0; i < each; i++) {
                        Bindings bindings = Bindings.builder().standard();
                        bindings.bind("age", Integer.class, base + i);
                        Object result = factory.getScriptProcessor().evaluate(script, RuleContext.builder().with(bindings).build());
                        if (((Number) result).intValue() != (base + i) * 2 + 1) mistakes++;
                    }

                    return mistakes;
                }));
            }

            for (Future<Integer> result : wrong) {
                Assertions.assertEquals(0, result.get(120, TimeUnit.SECONDS).intValue());
            }
        } finally {
            executor.shutdownNow();
        }

        GraalJsPoolStatistics statistics = factory.getPoolStatistics();
        Assertions.assertEquals((long) threads * each, statistics.created() + statistics.reused());
        Assertions.assertTrue(statistics.created() <= threads, "created " + statistics.created());
        Assertions.assertEquals(0, statistics.failed());
        factory.close();
    }

    @Test
    public void testProcessorsOfAReusingFactoryShareOneEngineObject() {
        GraalJsScriptProcessorFactory reusing = reusing(ONE_CONTEXT);
        JSR223ScriptProcessor first = (JSR223ScriptProcessor) reusing.getScriptProcessor();
        JSR223ScriptProcessor second = (JSR223ScriptProcessor) reusing.getScriptProcessor();

        // A processor each, as from any factory; the engine object, which they do not evaluate through, is one.
        Assertions.assertNotSame(first, second);
        Assertions.assertNotNull(first.getScriptEngine());
        Assertions.assertSame(first.getScriptEngine(), second.getScriptEngine());

        Script<Object> script = reusing.getScriptCompiler().compile("ctx.age >= 18", Object.class);
        Assertions.assertEquals(Boolean.TRUE, first.evaluate(script, context()));
        Assertions.assertEquals(Boolean.TRUE, second.evaluate(script, context()));
        Assertions.assertEquals(1, reusing.getPoolStatistics().created());
        reusing.close();

        // Without reuse a processor evaluates through its engine and has its own.
        GraalJsScriptProcessorFactory fresh = notReusing();
        Assertions.assertNotSame(((JSR223ScriptProcessor) fresh.getScriptProcessor()).getScriptEngine(),
                ((JSR223ScriptProcessor) fresh.getScriptProcessor()).getScriptEngine());
    }

    // ---- discarding ------------------------------------------------------------------------------

    @Test
    public void testDiscardRetiresIdleContextsAndTheFactoryCarriesOn() {
        GraalJsScriptProcessorFactory factory = reusing(ONE_CONTEXT);
        evaluate(factory, "1");
        Assertions.assertEquals(1, factory.getPoolStatistics().idle());

        factory.discardContexts();
        Assertions.assertEquals(0, factory.getPoolStatistics().idle());
        Assertions.assertEquals(1, factory.getPoolStatistics().retired());

        Assertions.assertEquals(Boolean.TRUE, evaluate(factory, "ctx.age >= 18"));
        Assertions.assertEquals(2, factory.getPoolStatistics().created());

        // close() is the same thing.
        factory.close();
        Assertions.assertEquals(0, factory.getPoolStatistics().idle());
        Assertions.assertEquals(Boolean.TRUE, evaluate(factory, "ctx.age >= 18"));
        factory.close();
    }

    @Test
    public void testDiscardAlsoRetiresAContextThatIsInUse() throws Exception {
        GraalJsScriptProcessorFactory factory = reusing(ONE_CONTEXT);
        Script<Object> script = factory.getScriptCompiler().compile("String.prototype.late = 1; ctx.pause.apply(null); 'done'", Object.class);
        CountDownLatch inScript = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("pause", Function.class, (Function<Object, Object>) ignored -> {
            inScript.countDown();
            try {
                release.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return null;
        });
        ExecutorService executor = Executors.newSingleThreadExecutor();

        try {
            Future<Object> running = executor.submit(() -> factory.getScriptProcessor().evaluate(script, RuleContext.builder().with(bindings).build()));
            Assertions.assertTrue(inScript.await(30, TimeUnit.SECONDS));

            factory.discardContexts();
            release.countDown();

            Assertions.assertEquals("done", running.get(30, TimeUnit.SECONDS));
        } finally {
            release.countDown();
            executor.shutdownNow();
        }

        // Its context was closed when it came back, not kept.
        Assertions.assertEquals(0, factory.getPoolStatistics().idle());
        Assertions.assertEquals("undefined", evaluate(factory, "typeof ''.late"));
        factory.close();
    }
}
