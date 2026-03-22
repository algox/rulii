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

import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;
import org.rulii.lib.spring.util.Assert;
import org.rulii.script.ScriptOptions;
import org.rulii.script.ScriptProcessor;
import org.rulii.script.ScriptProcessorFactory;
import org.rulii.script.jsr223.JSR223ScriptProcessor;

import javax.script.ScriptEngine;

/**
 * {@link ScriptProcessorFactory} that creates {@link ScriptProcessor} instances backed
 * by the GraalVM JavaScript engine (GraalJS).
 *
 * <p>Each call to {@link #create()} builds a fresh {@link GraalJSScriptEngine} with the
 * following configuration:
 * <ul>
 *   <li>Full host access ({@link HostAccess#ALL}) — Java objects passed into scripts
 *       are fully accessible.</li>
 *   <li>Unrestricted host class lookup — scripts may reference any Java class on the
 *       classpath.</li>
 *   <li>ECMAScript 2022 language level.</li>
 *   <li>Interpreter-only warning suppressed (useful in environments without the
 *       GraalVM compiler).</li>
 * </ul>
 *
 * <p>The default constructor uses {@code "js"} as the language name and
 * {@link ScriptOptions#DEFAULT} for the bindings variable name ({@code "ctx"}).
 *
 * @author Max Arulananthan
 * @since 1.2
 * @see ScriptProcessorFactory
 * @see JSR223ScriptProcessor
 * @see org.rulii.script.jsr223.JSR223ScriptProcessorFactory
 */
public class GraalJsScriptProcessorFactory implements ScriptProcessorFactory {

    private final String languageName;
    private final String bindingsName;

    /**
     * Constructs a factory using {@code "js"} as the language name and
     * {@link ScriptOptions#DEFAULT} for the bindings variable name.
     */
    public GraalJsScriptProcessorFactory() {
        this("js", ScriptOptions.DEFAULT.bindingsName());
    }

    /**
     * Constructs a factory with explicit language name and bindings variable name overrides.
     *
     * @param languageName the language name to advertise (e.g. {@code "js"}); must not be null or empty.
     * @param bindingsName the variable name under which the rule bindings are exposed inside scripts;
     *                     must not be null or empty.
     */
    public GraalJsScriptProcessorFactory(String languageName, String bindingsName) {
        super();
        Assert.hasText(languageName, "languageName cannot be empty.");
        Assert.hasText(bindingsName, "bindingsName cannot be empty.");
        this.languageName = languageName;
        this.bindingsName = bindingsName;
    }

    /**
     * Returns the language name this factory produces processors for.
     *
     * @return the language name; never null or empty.
     */
    @Override
    public String getLanguageName() {
        return languageName;
    }

    /**
     * Returns the bindings variable name used by processors created by this factory.
     *
     * @return the bindings variable name; never null or empty.
     */
    @Override
    public String getBindingName() {
        return bindingsName;
    }

    /**
     * Creates and returns a new {@link JSR223ScriptProcessor} backed by a freshly
     * constructed GraalJS engine.
     *
     * @return a new {@link ScriptProcessor}; never null.
     */
    @Override
    public ScriptProcessor create() {
        return new JSR223ScriptProcessor(createEngine(), languageName, bindingsName);
    }

    /**
     * Builds a new {@link GraalJSScriptEngine} with full host access, unrestricted
     * class lookup, ECMAScript 2022, and the interpreter-only warning suppressed.
     *
     * @return a configured {@link ScriptEngine}; never null.
     */
    private static ScriptEngine createEngine() {
        Engine engine = Engine.newBuilder("js")
                .option("engine.WarnInterpreterOnly", "false")
                .build();
        return GraalJSScriptEngine.create(engine,
                Context.newBuilder("js")
                        .allowHostAccess(HostAccess.ALL)
                        .allowHostClassLookup(s -> true)
                        .option("js.ecmascript-version", "2022"));
    }
}
