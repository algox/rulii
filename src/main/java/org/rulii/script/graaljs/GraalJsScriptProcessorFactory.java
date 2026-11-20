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

import com.oracle.truffle.js.scriptengine.GraalJSEngineFactory;
import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;
import org.rulii.lib.spring.util.Assert;
import org.rulii.lib.spring.util.ClassUtils;
import org.rulii.script.ScriptCompiler;
import org.rulii.script.ScriptOptions;
import org.rulii.script.ScriptProcessor;
import org.rulii.script.ScriptProcessorFactory;
import org.rulii.script.jsr223.JSR223ScriptCompiler;
import org.rulii.script.jsr223.JSR223ScriptProcessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * {@link ScriptProcessorFactory} implementation for GraalVM's JavaScript engine (GraalJS).
 *
 * <p>This factory creates {@link GraalJsScriptProcessor} and {@link GraalJsScriptCompiler} instances
 * (GraalJS-aware specialisations of {@link JSR223ScriptProcessor} and {@link JSR223ScriptCompiler}
 * that release polyglot contexts promptly) backed by a {@link GraalJSScriptEngine} configured with
 * the following defaults:
 * <ul>
 *   <li>ECMAScript version 2022</li>
 *   <li>Full host access ({@link HostAccess#ALL}), with JavaScript arrays, objects, maps, sets
 *       and dates detached into plain Java copies as they cross into Java; see
 *       {@link GraalJsHostAccess#DETACHING}</li>
 *   <li>Unrestricted host class lookup</li>
 *   <li>Interpreter-only warning suppressed</li>
 *   <li>Nashorn compatibility mode ({@code js.nashorn-compat}) enabled; see below</li>
 * </ul>
 *
 * <p><b>Nashorn compatibility mode.</b> GraalJS does not expose Java bean properties by default:
 * {@code ctx.person.name} silently evaluates to {@code null} unless written as
 * {@code ctx.person.getName()}. Nashorn compatibility mode restores bean-style access
 * ({@code name} resolves to {@code getName()}, {@code active} to {@code isActive()}, and
 * assignment calls {@code setXxx()}), which is what rule authors generally expect. It also
 * enables the Nashorn {@code Java.*} helpers, {@code JavaImporter}, and the {@code exit()} /
 * {@code quit()} globals (these terminate the script with a {@code PolyglotException}, not the
 * JVM). Bean-style access is a GraalJS feature; scripts relying on it will not evaluate
 * identically on a generic JSR-223 engine. GraalJS flags the option as experimental, hence
 * {@code allowExperimentalOptions(true)} on the shared engine. The explicit ECMAScript version
 * above takes precedence over the ES5 default that compatibility mode would otherwise imply.
 *
 * <p>The factory reports {@link #isAvailable()} as {@code false} when the GraalJS classes are
 * not present on the class path, allowing the engine to be an optional dependency.
 *
 * <p>The default language name is {@value #LANGUAGE_NAME}, and the factory also answers to every
 * other name GraalJS registers with JSR-223 (see {@link #getAliases()}); the default bindings variable name
 * is taken from {@link ScriptOptions#DEFAULT}.
 *
 * @author Max Arulananthan
 * @since 1.2
 * @see JSR223ScriptProcessor
 * @see JSR223ScriptCompiler
 */
public class    GraalJsScriptProcessorFactory implements ScriptProcessorFactory, AutoCloseable {

    /** Default language name used to register and look up this factory: {@value}. */
    public static final String LANGUAGE_NAME = "js";

    private static final boolean available = ClassUtils.isPresent(
            "com.oracle.truffle.js.scriptengine.GraalJSScriptEngine", GraalJsScriptProcessorFactory.class.getClassLoader());

    /**
     * System property that turns context reuse on for every factory that is not handed pool options in its
     * constructor: {@value}. The pool options then come from
     * {@link GraalJsPoolOptions#fromSystemProperties()}. See {@link #isReuseContexts()}.
     *
     * @since 2.3
     */
    public static final String REUSE_CONTEXTS_PROPERTY = "rulii.script.graaljs.reuseContexts";

    private final String languageName;
    private final String bindingsName;
    // Null unless this factory reuses contexts.
    private final GraalJsPoolOptions poolOptions;
    // Created when the first processor is asked for, so that a factory that is never used creates no context.
    private volatile GraalJsContextPool pool;
    // The engine object of every processor of a factory that reuses contexts. Those processors evaluate in the
    // pool's contexts and never through their engine, so one serves them all; a new one for each (about 2 µs and
    // 58 KB) would be paid by every rule context that runs a script. Set before the pool is.
    private volatile GraalJSScriptEngine pooledEngine;

    /**
     * Creates a factory with default language name ({@value #LANGUAGE_NAME}) and the default
     * bindings variable name from {@link ScriptOptions#DEFAULT}. Contexts are reused when
     * {@value #REUSE_CONTEXTS_PROPERTY} is {@code true}.
     */
    public GraalJsScriptProcessorFactory() {
        this(LANGUAGE_NAME, ScriptOptions.DEFAULT.bindingsName());
    }

    /**
     * Creates a factory with explicit language name and bindings variable name. Contexts are reused when
     * {@value #REUSE_CONTEXTS_PROPERTY} is {@code true}.
     *
     * @param languageName the language name to register under; must not be null or empty.
     * @param bindingsName the variable name under which bindings are exposed in scripts;
     *                     must not be null or empty.
     */
    public GraalJsScriptProcessorFactory(String languageName, String bindingsName) {
        this(languageName, bindingsName,
                Boolean.getBoolean(REUSE_CONTEXTS_PROPERTY) ? GraalJsPoolOptions.fromSystemProperties() : null, false);
    }

    /**
     * Creates a factory that reuses JavaScript contexts, kept as the given options say. See
     * {@link #isReuseContexts()} for what reuse changes for scripts.
     *
     * @param languageName the language name to register under; must not be null or empty.
     * @param bindingsName the variable name under which bindings are exposed in scripts;
     *                     must not be null or empty.
     * @param poolOptions how the contexts are kept; must not be null ({@link GraalJsPoolOptions#DEFAULT} for
     *                    the defaults).
     * @since 2.3
     */
    public GraalJsScriptProcessorFactory(String languageName, String bindingsName, GraalJsPoolOptions poolOptions) {
        this(languageName, bindingsName, poolOptions, true);
    }

    private GraalJsScriptProcessorFactory(String languageName, String bindingsName, GraalJsPoolOptions poolOptions,
                                          boolean required) {
        super();
        Assert.hasText(languageName, "languageName cannot be empty.");
        Assert.hasText(bindingsName, "bindingsName cannot be empty.");
        if (required) Assert.notNull(poolOptions, "poolOptions cannot be null.");
        this.languageName = languageName;
        this.bindingsName = bindingsName;
        this.poolOptions = poolOptions;
    }

    /**
     * Whether evaluations run in reused JavaScript contexts instead of a new context each.
     *
     * <p>Off by default: every evaluation gets a context of its own, created for it and released after it, so
     * nothing a script does can be seen by another; creating the context is nearly all of what an evaluation
     * costs. Turned on (pool options handed to the constructor, or {@value #REUSE_CONTEXTS_PROPERTY}), an
     * evaluation borrows a context from this factory's pool and gives it back, and costs a few microseconds
     * instead of more than a hundred.
     *
     * <p>What changes for scripts when it is on: nothing for a script that leaves the built-ins alone. What a
     * script declares ({@code var}, {@code let}, {@code const}, functions) ends with it, and a global it leaves
     * behind (a variable assigned without being declared, {@code total = 5}) is removed before the context is used
     * again. What it changes inside a built-in ({@code String.prototype.isBlank = ...},
     * {@code Math.round = ...}) is not undone: whichever script next runs in the same context sees it, until the
     * context is retired by its age, its number of uses or {@link #discardContexts()}. Which context a script
     * runs in is not predictable.
     *
     * @return {@code true} if this factory reuses contexts.
     * @since 2.3
     * @see GraalJsPoolOptions
     */
    public boolean isReuseContexts() {
        return poolOptions != null;
    }

    /**
     * How this factory keeps its contexts.
     *
     * @return the pool options, or null unless it {@link #isReuseContexts() reuses contexts}.
     * @since 2.3
     */
    public GraalJsPoolOptions getPoolOptions() {
        return poolOptions;
    }

    /**
     * What this factory's pool has done so far.
     *
     * @return the counts, or null unless it {@link #isReuseContexts() reuses contexts}.
     * @since 2.3
     */
    public GraalJsPoolStatistics getPoolStatistics() {
        if (poolOptions == null) return null;

        GraalJsContextPool current = pool;
        return current != null ? current.getStatistics() : new GraalJsPoolStatistics(0, 0, 0, 0, 0, 0, 0, 0);
    }

    /**
     * Retires every JavaScript context this factory has created so far: for when a script is known to have left
     * them in a state later scripts should not see. Idle contexts are closed now; a context in the middle of an
     * evaluation finishes it and is closed instead of being kept. The factory stays usable: the next evaluation
     * creates a context. Nothing to do unless it {@link #isReuseContexts() reuses contexts}.
     *
     * @since 2.3
     */
    public void discardContexts() {
        GraalJsContextPool current = pool;
        if (current != null) current.discardAll();
    }

    /**
     * Releases the contexts this factory keeps: the same as {@link #discardContexts()}. Called by a container
     * that manages the factory when it shuts down.
     *
     * @since 2.3
     */
    @Override
    public void close() {
        discardContexts();
    }

    private GraalJsContextPool pool() {
        if (poolOptions == null) return null;

        GraalJsContextPool result = pool;

        if (result == null) {
            synchronized (this) {
                result = pool;

                if (result == null) {
                    pooledEngine = createEngine();
                    result = new GraalJsContextPool(GraalJsScriptProcessorFactory::createContext, bindingsName, poolOptions);
                    pool = result;
                }
            }
        }

        return result;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public String getLanguageName() {
        return languageName;
    }

    /**
     * Returns every name GraalJS itself answers to through {@code ScriptEngineManager}
     * ({@code "JS"}, {@code "JavaScript"}, {@code "ECMAScript"}, {@code "graal.js"}, ...), as
     * reported by {@link GraalJSEngineFactory#getNames()}, minus this factory's own language name.
     *
     * <p>Registering the factory under each of them keeps a lookup such as {@code "javascript"}
     * on this fully configured factory rather than letting it fall through to the generic
     * JSR-223 wrapper around a default GraalJS engine, which has neither the host access policy
     * nor the context release behaviour of this one. Empty when GraalJS is not on the class path.
     */
    @Override
    public Collection<String> getAliases() {
        if (!available) return Collections.emptyList();

        List<String> result = new ArrayList<>(new GraalJSEngineFactory().getNames());
        result.remove(languageName);
        return result;
    }

    @Override
    public String getBindingsName() {
        return bindingsName;
    }

    /**
     * A new processor. Without reuse it has a {@link GraalJSScriptEngine} of its own, which it evaluates through.
     * The processors of a factory that {@link #isReuseContexts() reuses contexts} evaluate in the pool's contexts
     * instead, so they share one engine object, the one {@link JSR223ScriptProcessor#getScriptEngine()} answers
     * with; it takes no part in their evaluations.
     */
    @Override
    public ScriptProcessor getScriptProcessor() {
        GraalJsContextPool contexts = pool();

        // Without reuse a processor evaluates through its engine and needs one of its own.
        return new GraalJsScriptProcessor(contexts != null ? pooledEngine : createEngine(), languageName, bindingsName, contexts);
    }

    @Override
    public ScriptCompiler getScriptCompiler() {
        return new GraalJsScriptCompiler(getLanguageName());
    }

    private static volatile Engine sharedEngine;

    /**
     * Creates a new {@link GraalJSScriptEngine} configured for ES2022 with full host access.
     *
     * <p>Backed by a single, lazily-created, shared {@link Engine} (the expensive, warmup-heavy
     * resource GraalVM recommends reusing across many short-lived {@link Context}s and threads —
     * see {@link #getSharedEngine()}). Each call still builds its own {@link GraalJSScriptEngine}
     * with a private {@link Context.Builder}, since that builder is mutable and not thread-safe;
     * sharing it across concurrently-executing rules would race.
     *
     * <p>The engine's own default polyglot {@code Context} is created lazily and only when the
     * engine itself is asked to evaluate or compile something; evaluation through
     * {@link GraalJsScriptProcessor} never touches it. Callers that do use it (see
     * {@link GraalJsScriptCompiler}) must {@link GraalJSScriptEngine#close() close} the engine
     * afterwards, otherwise that context is never reclaimed.
     *
     * @return a freshly created engine instance; never null.
     */
    static GraalJSScriptEngine createEngine() {
        return GraalJSScriptEngine.create(getSharedEngine(), contextBuilder());
    }

    /**
     * Creates a JavaScript context of its own on the shared {@link Engine}, with the settings the contexts behind
     * {@link #createEngine()} have. For a factory that reuses contexts; the caller closes it.
     *
     * @return a new context; never null.
     */
    static Context createContext() {
        return contextBuilder().engine(getSharedEngine()).build();
    }

    private static Context.Builder contextBuilder() {
        return Context.newBuilder("js")
                .allowHostAccess(GraalJsHostAccess.DETACHING)
                .allowHostClassLookup(s -> true)
                .option("js.ecmascript-version", "2022");
    }

    /**
     * Returns the process-wide, lazily-created {@link Engine} shared by every
     * {@link GraalJsScriptProcessorFactory} instance.
     *
     * <p>{@link Engine} holds no per-evaluation script state (each {@link Context}/evaluation
     * gets its own fresh bindings — see {@link org.rulii.script.jsr223.JSR223ScriptProcessor
     * #buildContext}) and is documented by GraalVM as safe to share across threads and Contexts;
     * only the {@link Context.Builder} used to construct each {@link GraalJSScriptEngine} is
     * NOT thread-safe, which is why that part remains per-call.
     *
     * @return the shared engine; never null.
     */
    private static Engine getSharedEngine() {
        Engine engine = sharedEngine;

        if (engine == null) {
            synchronized (GraalJsScriptProcessorFactory.class) {
                engine = sharedEngine;

                if (engine == null) {
                    engine = Engine.newBuilder("js")
                            // js.nashorn-compat is flagged experimental by GraalJS.
                            .allowExperimentalOptions(true)
                            .option("engine.WarnInterpreterOnly", "false")
                            // Bean-style property access on host objects (ctx.person.name).
                            // The per-Context js.ecmascript-version=2022 overrides the ES5
                            // default this mode would otherwise imply.
                            .option("js.nashorn-compat", "true")
                            .build();
                    sharedEngine = engine;
                }
            }
        }

        return engine;
    }
}
