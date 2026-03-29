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
import org.rulii.script.ScriptCompiler;
import org.rulii.script.ScriptOptions;
import org.rulii.script.ScriptProcessor;
import org.rulii.script.ScriptProcessorFactory;

import javax.script.ScriptEngineFactory;

/**
 * JSR-223 implementation of {@link ScriptProcessorFactory} that wraps a {@link ScriptEngineFactory}
 * to produce language-specific {@link JSR223ScriptProcessor} and {@link JSR223ScriptCompiler} instances.
 *
 * <p>This factory is the default fall-back used by {@link org.rulii.script.ScriptProcessorManager}
 * when no dedicated factory is registered for a language but a JSR-223 engine is available on the
 * class path.  It can also be used as the base for language-specific factories that need to
 * customise the engine or bindings name.
 *
 * @author Max Arulananthan
 * @since 1.2
 * @see JSR223ScriptProcessor
 * @see JSR223ScriptCompiler
 * @see org.rulii.script.ScriptProcessorManager
 */
public class JSR223ScriptProcessorFactory implements ScriptProcessorFactory {

    private final ScriptEngineFactory factory;
    private final String languageName;
    private final String bindingsName;

    /**
     * Creates a factory using the language name and default bindings name from the given engine factory.
     *
     * @param factory the JSR-223 engine factory; must not be null.
     */
    public JSR223ScriptProcessorFactory(ScriptEngineFactory factory) {
        this(factory, null, null);
    }

    /**
     * Creates a factory with explicit overrides for language name and bindings variable name.
     *
     * @param factory      the JSR-223 engine factory; must not be null.
     * @param languageName override for the language name, or {@code null} to use the engine factory's default.
     * @param bindingsName override for the bindings variable name, or {@code null} to use
     *                     {@link org.rulii.script.ScriptOptions#DEFAULT}.
     */
    public JSR223ScriptProcessorFactory(ScriptEngineFactory factory, String languageName, String bindingsName) {
        super();
        Assert.notNull(factory, "factory cannot be null.");
        this.languageName = StringUtils.hasText(languageName) ? languageName : factory.getLanguageName();
        this.bindingsName = StringUtils.hasText(bindingsName) ? bindingsName : ScriptOptions.DEFAULT.bindingsName();
        this.factory = factory;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getLanguageName() {
        return languageName;
    }

    @Override
    public String getBindingsName() {
        return bindingsName;
    }

    @Override
    public ScriptProcessor getScriptProcessor() {
        return new JSR223ScriptProcessor(factory.getScriptEngine(), languageName, bindingsName);
    }

    @Override
    public ScriptCompiler getScriptCompiler() {
        return new JSR223ScriptCompiler(factory.getScriptEngine());
    }
}
