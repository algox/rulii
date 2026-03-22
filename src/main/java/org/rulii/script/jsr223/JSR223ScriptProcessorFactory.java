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
package org.rulii.script.jsr223;

import org.rulii.lib.spring.util.Assert;
import org.rulii.lib.spring.util.StringUtils;
import org.rulii.script.ScriptOptions;
import org.rulii.script.ScriptProcessor;
import org.rulii.script.ScriptProcessorFactory;
import org.rulii.script.graaljs.GraalJsScriptProcessorFactory;

import javax.script.ScriptEngineFactory;

/**
 * {@link ScriptProcessorFactory} that creates {@link ScriptProcessor} instances backed
 * by any JSR-223 compliant scripting engine.
 *
 * <p>Each call to {@link #create()} asks the supplied {@link ScriptEngineFactory} for a
 * fresh {@link javax.script.ScriptEngine} and wraps it in a new
 * {@link JSR223ScriptProcessor}.  If the engine implements
 * {@link javax.script.Compilable}, the processor will compile and cache scripts
 * automatically.
 *
 * <p>The single-argument constructor derives the language name directly from the
 * {@link ScriptEngineFactory} and uses {@link ScriptOptions#DEFAULT} for the bindings
 * variable name ({@code "ctx"}).  The three-argument constructor allows both to be
 * overridden; blank values fall back to the factory/default values.
 *
 * @author Max Arulananthan
 * @since 1.2
 * @see ScriptProcessorFactory
 * @see JSR223ScriptProcessor
 * @see GraalJsScriptProcessorFactory
 */
public class JSR223ScriptProcessorFactory implements ScriptProcessorFactory {

    private final ScriptEngineFactory factory;
    private final String languageName;
    private final String bindingsName;

    /**
     * Constructs a factory from the given {@link ScriptEngineFactory}, using the
     * factory's language name and the default bindings variable name ({@code "ctx"}).
     *
     * @param factory the JSR-223 engine factory to use; must not be null.
     */
    public JSR223ScriptProcessorFactory(ScriptEngineFactory factory) {
        this(factory, null, null);
    }

    /**
     * Constructs a factory with optional overrides for the language name and bindings
     * variable name.
     *
     * <p>If {@code languageName} is blank, the engine factory's language name is used.
     * If {@code bindingsName} is blank, {@code "ctx"} is used.
     *
     * @param factory      the JSR-223 engine factory to use; must not be null.
     * @param languageName override for the language name; may be null/empty.
     * @param bindingsName override for the bindings variable name; may be null/empty.
     */
    public JSR223ScriptProcessorFactory(ScriptEngineFactory factory, String languageName, String bindingsName) {
        super();
        Assert.notNull(factory, "factory cannot be null.");
        this.languageName = StringUtils.hasText(languageName) ? languageName : factory.getLanguageName();
        this.bindingsName = StringUtils.hasText(bindingsName) ? bindingsName : ScriptOptions.DEFAULT.bindingsName();
        this.factory = factory;
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
     * Creates and returns a new {@link JSR223ScriptProcessor} backed by a fresh engine
     * obtained from the underlying {@link ScriptEngineFactory}.
     *
     * @return a new {@link ScriptProcessor}; never null.
     */
    @Override
    public ScriptProcessor create() {
        return new JSR223ScriptProcessor(factory.getScriptEngine(), languageName, bindingsName);
    }
}
