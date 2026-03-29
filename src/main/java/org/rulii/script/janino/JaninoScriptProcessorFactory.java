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
package org.rulii.script.janino;

import org.rulii.lib.spring.util.Assert;
import org.rulii.script.ScriptCompiler;
import org.rulii.script.ScriptOptions;
import org.rulii.script.ScriptProcessor;
import org.rulii.script.ScriptProcessorFactory;

/**
 * {@link ScriptProcessorFactory} implementation that produces {@link JaninoScriptProcessor} and
 * {@link JaninoScriptCompiler} instances for JIT-compiled Java script fragments.
 *
 * <p>This factory is registered via the {@link java.util.ServiceLoader} mechanism in
 * {@code META-INF/services/org.rulii.script.ScriptProcessorFactory} and is loaded automatically
 * by {@link org.rulii.script.ScriptProcessorManager} when Janino is on the class path.
 *
 * <p>{@link #isAvailable()} returns {@code false} when {@code org.codehaus.janino.SimpleCompiler}
 * cannot be found, allowing Janino to be an optional dependency.
 *
 * <p>The default language name is {@value #LANGUAGE_NAME}; the default bindings variable name is
 * taken from {@link ScriptOptions#DEFAULT}.
 *
 * @author Max Arulananthan
 * @since 1.2
 * @see JaninoScriptProcessor
 * @see JaninoScriptCompiler
 * @see org.rulii.script.ScriptProcessorManager
 */
public class JaninoScriptProcessorFactory implements ScriptProcessorFactory {

    /** Language name used to register and look up this factory: {@value}. */
    public static final String LANGUAGE_NAME = "java";

    private static boolean available;

    static {
        try {
            Class.forName("org.codehaus.janino.SimpleCompiler");
            available = true;
        } catch (ClassNotFoundException e) {
            available = false;
        }
    }

    private final String languageName;
    private final String bindingsName;

    /**
     * Creates a factory with the default language name ({@value #LANGUAGE_NAME}) and the default
     * bindings variable name from {@link ScriptOptions#DEFAULT}.
     */
    public JaninoScriptProcessorFactory() {
        this(LANGUAGE_NAME, ScriptOptions.DEFAULT.bindingsName());
    }

    /**
     * Creates a factory with explicit language name and bindings variable name overrides.
     *
     * @param languageName the language name to register under; must not be null or empty.
     * @param bindingsName the variable name under which bindings are exposed inside scripts;
     *                     must not be null or empty.
     */
    public JaninoScriptProcessorFactory(String languageName, String bindingsName) {
        super();
        Assert.hasText(languageName, "languageName cannot be empty.");
        Assert.hasText(bindingsName, "bindingsName cannot be empty.");
        this.languageName = languageName;
        this.bindingsName = bindingsName;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code true} if {@code org.codehaus.janino.SimpleCompiler} is present on the class path.
     */
    @Override
    public boolean isAvailable() {
        return available;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLanguageName() {
        return languageName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBindingsName() {
        return bindingsName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ScriptProcessor getScriptProcessor() {
        return new JaninoScriptProcessor(languageName, bindingsName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ScriptCompiler getScriptCompiler() {
        return new JaninoScriptCompiler(languageName, bindingsName);
    }
}
