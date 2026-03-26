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

import java.util.LinkedList;
import java.util.List;

/**
 * Fluent builder for constructing a {@link Script} instance for a specific language and source text.
 *
 * <p>Instances are obtained from {@link ScriptBuilderBuilder#with(String, String)} rather than
 * constructed directly.  Use {@link #param(ScriptParameter)} to declare parameters before calling
 * {@link #build()} to compile the script.
 *
 * @author Max Arulananthan
 * @since 1.2
 * @see ScriptBuilderBuilder
 * @see ScriptParameter
 */
public class ScriptBuilder {

    private final ScriptProcessorFactory factory;
    private final String script;
    private final List<ScriptParameter> scriptParameters = new LinkedList<>();

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
     * Declares a parameter that the script expects to receive from the rule bindings.
     *
     * @param parameter the parameter declaration; must not be null.
     * @return this builder, for method chaining.
     */
    public ScriptBuilder param(ScriptParameter parameter) {
        Assert.notNull(parameter, "parameter cannot be null.");
        this.scriptParameters.add(parameter);
        return this;
    }

    /**
     * Compiles the script and returns the resulting {@link Script} instance.
     *
     * @param <T> the expected return type of the script.
     * @return the compiled script; never null.
     * @throws BuildScriptException if the source cannot be compiled.
     */
    public <T> Script<T> build() {
        return factory.getScriptCompiler().compile(script, scriptParameters);
    }
}
