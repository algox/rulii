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

import javax.script.ScriptEngine;
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
public class GraalJsScriptProcessorFactory implements ScriptProcessorFactory {

    /** Default language name used to register and look up this factory: {@value}. */
    public static final String LANGUAGE_NAME = "js";

    private static final boolean available = ClassUtils.isPresent(
            "com.oracle.truffle.js.scriptengine.GraalJSScriptEngine", GraalJsScriptProcessorFactory.class.getClassLoader());

    private final String languageName;
    private final String bindingsName;

    /**
     * Creates a factory with default language name ({@value #LANGUAGE_NAME}) and the default
     * bindings variable name from {@link ScriptOptions#DEFAULT}.
     */
    public GraalJsScriptProcessorFactory() {
        this(LANGUAGE_NAME, ScriptOptions.DEFAULT.bindingsName());
    }

    /**
     * Creates a factory with explicit language name and bindings variable name.
     *
     * @param languageName the language name to register under; must not be null or empty.
     * @param bindingsName the variable name under which bindings are exposed in scripts;
     *                     must not be null or empty.
     */
    public GraalJsScriptProcessorFactory(String languageName, String bindingsName) {
        super();
        Assert.hasText(languageName, "languageName cannot be empty.");
        Assert.hasText(bindingsName, "bindingsName cannot be empty.");
        this.languageName = languageName;
        this.bindingsName = bindingsName;
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

    @Override
    public ScriptProcessor getScriptProcessor() {
        return new GraalJsScriptProcessor(createEngine(), languageName, bindingsName);
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
        return GraalJSScriptEngine.create(getSharedEngine(),
                Context.newBuilder("js")
                        .allowHostAccess(GraalJsHostAccess.DETACHING)
                        .allowHostClassLookup(s -> true)
                        .option("js.ecmascript-version", "2022"));
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
