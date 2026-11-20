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
package org.rulii.script.graaljs;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.rulii.lib.spring.util.Assert;

import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Supplier;

/**
 * The JavaScript contexts of a {@link GraalJsScriptProcessorFactory} that reuses them.
 *
 * <p>Creating a JavaScript context (a whole global environment with its built-ins) costs about thirty times what
 * running a rule's script in it does. An evaluation borrows a context from here, runs its script and gives the
 * context back, so the next evaluation finds one ready. A context is used by one evaluation at a time; evaluations
 * that run at the same moment each get their own, and the pool creates one whenever none is idle. Nothing here
 * waits and no thread of its own runs: a context's age and idle time are looked at when the pool is used.
 *
 * <p>A script is run through {@code eval} inside a function: what it declares ({@code var}, {@code let},
 * {@code const}, functions) lives and dies with that call, and the value of its last statement comes back, as it does
 * for a script run on its own. The rule bindings are a global variable for the length of the evaluation, as they
 * are in a context of their own. Globals a script leaves behind (a variable it assigned without declaring) are
 * removed before the context is used again, unless {@link GraalJsPoolOptions#cleanGlobals()} is off. What a script
 * changes inside a built-in ({@code String.prototype.shout = ...}) stays until the context is retired.
 *
 * @author Max Arulananthan
 * @since 2.3
 */
final class GraalJsContextPool {

    // The parameter the script's text is handed over in; a script could read it, nothing else.
    private static final String SCRIPT_PARAMETER    = "__rulii_script__";

    private static final String RUNNER              =
            "(function(" + SCRIPT_PARAMETER + ") { return eval(" + SCRIPT_PARAMETER + "); })";

    // Remembers the global object as it is when the context is new. count() is the number of its properties, or -1
    // when eval was replaced (the runner depends on it); reset() removes what was added and puts back what was
    // replaced or deleted. The functions it needs are taken now, so a script that replaces them later changes nothing.
    private static final String GLOBALS             =
            "(function() {\n" +
            "  var global = globalThis, ownKeys = Reflect.ownKeys, describe = Object.getOwnPropertyDescriptor;\n" +
            "  var define = Object.defineProperty, same = Object.is, original = eval;\n" +
            "  var keys = ownKeys(global), saved = [];\n" +
            "  for (var i = 0; i < keys.length; i++) saved[i] = describe(global, keys[i]);\n" +
            "  return {\n" +
            "    count: function() { return global.eval === original ? ownKeys(global).length : -1; },\n" +
            "    reset: function() {\n" +
            "      var now = ownKeys(global);\n" +
            "      for (var i = 0; i < now.length; i++) {\n" +
            "        var known = false;\n" +
            "        for (var j = 0; j < keys.length && !known; j++) known = keys[j] === now[i];\n" +
            "        if (!known) delete global[now[i]];\n" +
            "      }\n" +
            "      for (var i = 0; i < keys.length; i++) {\n" +
            "        var d = saved[i], c = describe(global, keys[i]);\n" +
            "        if (c === undefined || !same(c.value, d.value) || c.get !== d.get || c.set !== d.set) define(global, keys[i], d);\n" +
            "      }\n" +
            "    }\n" +
            "  };\n" +
            "})()";

    private final Supplier<Context> contexts;
    private final String bindingsName;
    private final GraalJsPoolOptions options;
    private final long idleTimeoutNanos;
    private final long maxAgeNanos;
    private final Deque<Entry> idle = new ConcurrentLinkedDeque<>();
    private final AtomicInteger idleCount = new AtomicInteger();
    // Raised by discardAll(): a context of an earlier generation is closed instead of being used or kept.
    private final AtomicInteger generation = new AtomicInteger();

    private final LongAdder created = new LongAdder();
    private final LongAdder reused = new LongAdder();
    private final LongAdder retired = new LongAdder();
    private final LongAdder cleaned = new LongAdder();
    private final LongAdder tarnished = new LongAdder();
    private final LongAdder failed = new LongAdder();
    private final LongAdder detached = new LongAdder();

    /**
     * @param contexts creates a new context; called whenever an evaluation finds none idle.
     * @param bindingsName the name scripts know the rule bindings by.
     * @param options how the contexts are kept.
     */
    GraalJsContextPool(Supplier<Context> contexts, String bindingsName, GraalJsPoolOptions options) {
        super();
        Assert.notNull(contexts, "contexts cannot be null.");
        Assert.hasText(bindingsName, "bindingsName cannot be empty.");
        Assert.notNull(options, "options cannot be null.");
        this.contexts = contexts;
        this.bindingsName = bindingsName;
        this.options = options;
        this.idleTimeoutNanos = options.idleTimeout().toNanos();
        this.maxAgeNanos = options.maxAge().toNanos();

        for (int i = 0; i < options.initialSize(); i++) {
            idleCount.incrementAndGet();
            idle.addLast(newEntry());
        }
    }

    /**
     * An idle context, the most recently used one first, or a new one. Idle contexts that have outlived their age
     * or their idle time, or were discarded, are closed on the way.
     *
     * @return a context for one evaluation; hand it to {@link #giveBack}, {@link #fail} or {@link #detach} afterwards.
     */
    Entry borrow() {
        long now = System.nanoTime();
        Entry entry;

        while ((entry = idle.pollFirst()) != null) {
            idleCount.decrementAndGet();

            if (entry.generation == generation.get() && !isExpired(entry, now)) {
                reused.increment();
                return entry;
            }

            retire(entry);
        }

        return newEntry();
    }

    /**
     * Takes a context back after an evaluation whose result does not depend on it: the rule bindings are taken out
     * of it, what the script left in the global environment is removed, and it is kept for the next evaluation. It
     * is closed instead when it has reached its age or its number of uses, was discarded in the meantime, cannot be
     * put back the way it was, or enough contexts are idle already.
     *
     * @param entry the borrowed context.
     */
    void giveBack(Entry entry) {
        long now = System.nanoTime();
        entry.uses++;
        entry.lastUsed = now;

        if (!entry.unbind(bindingsName) || (options.cleanGlobals() && !restoreGlobals(entry))) {
            tarnished.increment();
            entry.close();
            return;
        }

        if (entry.generation != generation.get()
                || (options.maxUses() > 0 && entry.uses >= options.maxUses())
                || (maxAgeNanos > 0 && now - entry.created >= maxAgeNanos)) {
            retire(entry);
            return;
        }

        if (idleCount.incrementAndGet() <= options.maxIdle()) {
            idle.addFirst(entry);
            return;
        }

        idleCount.decrementAndGet();
        retire(entry);
    }

    /**
     * Closes a context whose script failed: the failure may have ended the context.
     *
     * @param entry the borrowed context.
     */
    void fail(Entry entry) {
        failed.increment();
        entry.close();
    }

    /**
     * Lets go of a context whose result is still a JavaScript object (a function, for one). The context stays alive
     * for that result, with the rule bindings still in it, and is never used for another evaluation.
     *
     * @param entry the borrowed context.
     */
    void detach(Entry entry) {
        detached.increment();
    }

    /**
     * Retires every context created so far: the idle ones are closed now, the ones in use when their evaluation
     * gives them back. The pool stays usable; the next evaluation creates a context.
     */
    void discardAll() {
        generation.incrementAndGet();
        Entry entry;

        while ((entry = idle.pollFirst()) != null) {
            idleCount.decrementAndGet();
            retire(entry);
        }
    }

    /**
     * @return what the pool has done so far.
     */
    GraalJsPoolStatistics getStatistics() {
        return new GraalJsPoolStatistics(created.sum(), reused.sum(), retired.sum(), cleaned.sum(), tarnished.sum(),
                failed.sum(), detached.sum(), idle.size());
    }

    private boolean isExpired(Entry entry, long now) {
        return (idleTimeoutNanos > 0 && now - entry.lastUsed >= idleTimeoutNanos)
                || (maxAgeNanos > 0 && now - entry.created >= maxAgeNanos);
    }

    /**
     * Whether the global object is as it was when the context was new, after removing what a script left if need be.
     */
    private boolean restoreGlobals(Entry entry) {
        try {
            if (entry.count.execute().asInt() == entry.baseline) return true;

            entry.reset.execute();
            cleaned.increment();
            return entry.count.execute().asInt() == entry.baseline;
        } catch (RuntimeException e) {
            return false;
        }
    }

    private void retire(Entry entry) {
        retired.increment();
        entry.close();
    }

    private Entry newEntry() {
        Context context = contexts.get();

        try {
            Value runner = context.eval("js", RUNNER);
            Value globals = context.eval("js", GLOBALS);
            Entry result = new Entry(context, runner, globals.getMember("count"), globals.getMember("reset"), generation.get());
            created.increment();
            return result;
        } catch (RuntimeException e) {
            context.close();
            throw e;
        }
    }

    /**
     * A context with what it takes to run a script in it and to put it back the way it was. Used by one thread at
     * a time: the one that borrowed it.
     */
    static final class Entry {

        private final Context context;
        private final Value bindings;
        private final Value runner;
        private final Value count;
        private final Value reset;
        private final int baseline;
        private final int generation;
        private final long created;
        private long lastUsed;
        private int uses;

        private Entry(Context context, Value runner, Value count, Value reset, int generation) {
            super();
            this.context = context;
            this.bindings = context.getBindings("js");
            this.runner = runner;
            this.count = count;
            this.reset = reset;
            this.baseline = count.execute().asInt();
            this.generation = generation;
            this.created = System.nanoTime();
            this.lastUsed = created;
        }

        Context getContext() {
            return context;
        }

        /**
         * Runs the script with the given bindings as a global variable of the given name, and hands the result over
         * as a JSR-223 engine does. The bindings stay in the context until it is given back.
         *
         * @param bindingsName the name scripts know the rule bindings by.
         * @param ruleBindings the rule bindings, as the map scripts see.
         * @param script the script text.
         * @return the script's value.
         */
        Object run(String bindingsName, Map<String, Object> ruleBindings, String script) {
            bindings.putMember(bindingsName, ruleBindings);
            return runner.execute(script).as(Object.class);
        }

        /**
         * Takes the rule bindings out again.
         *
         * @return false if they are still there (the script made the variable permanent).
         */
        private boolean unbind(String bindingsName) {
            try {
                return bindings.removeMember(bindingsName) || !bindings.hasMember(bindingsName);
            } catch (RuntimeException e) {
                return false;
            }
        }

        void close() {
            try {
                context.close();
            } catch (RuntimeException ignored) {
                // Best effort: a context that could not be closed is in no worse a state than before.
            }
        }
    }
}
