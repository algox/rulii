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
import org.rulii.script.ScriptCompiler;
import org.rulii.script.ScriptOptions;
import org.rulii.script.ScriptProcessor;
import org.rulii.script.ScriptProcessorFactory;
import org.rulii.script.jsr223.JSR223ScriptCompiler;
import org.rulii.script.jsr223.JSR223ScriptProcessor;

import javax.script.ScriptEngine;

/**
 * {@link ScriptProcessorFactory} implementation for GraalVM's JavaScript engine (GraalJS).
 *
 * <p>This factory creates {@link JSR223ScriptProcessor} and {@link JSR223ScriptCompiler} instances
 * backed by a {@link GraalJSScriptEngine} configured with the following defaults:
 * <ul>
 *   <li>ECMAScript version 2022</li>
 *   <li>Full host access ({@link HostAccess#ALL})</li>
 *   <li>Unrestricted host class lookup</li>
 *   <li>Interpreter-only warning suppressed</li>
 * </ul>
 *
 * <p>The factory reports {@link #isAvailable()} as {@code false} when the GraalJS classes are
 * not present on the class path, allowing the engine to be an optional dependency.
 *
 * <p>The default language name is {@value #LANGUAGE_NAME}; the default bindings variable name
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

    private static boolean available = false;

    private final String languageName;
    private final String bindingsName;

    static {
        try {
            Class.forName("com.oracle.truffle.js.scriptengine.GraalJSScriptEngine");
            available = true;
        } catch (ClassNotFoundException e) {
            available = false;
        }
    }

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

    @Override
    public String getBindingName() {
        return bindingsName;
    }

    @Override
    public ScriptProcessor getScriptProcessor() {
        return new JSR223ScriptProcessor(createEngine(), languageName, bindingsName);
    }

    @Override
    public ScriptCompiler getScriptCompiler() {
        return new JSR223ScriptCompiler(getLanguageName(), createEngine());
    }

    /**
     * Creates a new {@link GraalJSScriptEngine} configured for ES2022 with full host access.
     *
     * @return a freshly created engine instance; never null.
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
