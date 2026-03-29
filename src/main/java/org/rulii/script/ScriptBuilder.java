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
package org.rulii.script;

import org.rulii.lib.spring.util.Assert;

/**
 * Fluent builder for constructing a {@link Script} instance for a specific language and source text.
 *
 * <p>Instances are obtained from {@link ScriptBuilderBuilder#with(String, String)} rather than
 * constructed directly.
 *
 * @author Max Arulananthan
 * @since 1.2
 * @see ScriptBuilderBuilder
 */
public class ScriptBuilder {

    private final ScriptProcessorFactory factory;
    private final String script;
    private Class<?> returnType = void.class;
    /**
     * Package-private constructor — use {@link ScriptBuilderBuilder#with(String, String)}.
     *
     * @param factory the factory for the target scripting language; must not be null.
     * @param script  the script source text; must not be null or empty.
     */
    ScriptBuilder(ScriptProcessorFactory factory, String script) {
        super();
        Assert.notNull(factory, "factory cannot be null.");
        Assert.hasText(script, "script cannot be empty.");
        this.factory = factory;
        this.script = script;
    }

    /**
     * Sets the expected return type of the script.
     *
     * <p>The default is {@code void.class}.  Pass {@code Boolean.class} for condition scripts,
     * {@code Object.class} for function scripts, or any other type as needed.
     *
     * @param returnType the expected return type; must not be null.
     * @return this builder, for chaining.
     */
    public ScriptBuilder returnType(Class<?> returnType) {
        this.returnType = returnType;
        return this;
    }

    /**
     * Compiles and returns the {@link Script}.
     *
     * <p>Delegates to {@link ScriptCompiler#compile(String, Class)} for the factory associated
     * with the chosen language.
     *
     * @param <T> the expected return type of the compiled script.
     * @return the compiled (or lazily-compiled) script; never null.
     * @throws BuildScriptException if the underlying compiler rejects the source.
     */
    public <T> Script<T> build() {
        return factory.getScriptCompiler().compile(script, returnType);
    }
}
