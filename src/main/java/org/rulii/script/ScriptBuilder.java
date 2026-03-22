/*
 * This software is licensed under the Apache 2 license, quoted below.
 *
 * Copyright (c) 1999-2025, Algorithmx Inc.
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
 * Fluent builder for constructing {@link Script} instances.
 *
 * <p>A {@code ScriptBuilder} is obtained from {@link ScriptBuilderBuilder#with(String, String)}
 * and allows optional {@link ScriptParameter}s to be declared before the script is
 * finalized via {@link #build()}.
 *
 * <pre>{@code
 * Script<Boolean> script = Script.builder()
 *         .with("ECMAScript", "age >= 18")
 *         .param(new ScriptParameter("age", Integer.class))
 *         .build();
 * }</pre>
 *
 * @author Max Arulananthan
 * @since 1.2
 * @see ScriptBuilderBuilder
 * @see Script
 * @see ScriptParameter
 */
public class ScriptBuilder {

    private final String languageName;
    private final String script;
    private final List<ScriptParameter> scriptParameters = new LinkedList<>();

    /**
     * Constructs a new {@code ScriptBuilder} for the given language and source text.
     *
     * @param languageName the name of the scripting language (e.g. {@code "ECMAScript"}); must not be null or empty.
     * @param script       the script source text to be evaluated; must not be null or empty.
     */
    ScriptBuilder(String languageName, String script) {
        super();
        Assert.hasText(languageName, "languageName cannot be empty.");
        Assert.hasText(script, "script cannot be empty.");
        this.script = script;
        this.languageName = languageName;
    }

    /**
     * Adds a typed parameter declaration to the script being built.
     *
     * @param parameter the parameter to add; must not be null.
     * @return this builder, for method chaining.
     */
    public ScriptBuilder param(ScriptParameter parameter) {
        Assert.notNull(parameter, "parameter cannot be null.");
        this.scriptParameters.add(parameter);
        return this;
    }

    /**
     * Builds and returns a new {@link Script} with the language, source text,
     * and parameters configured on this builder.
     *
     * @param <T> the expected return type of the script.
     * @return a new {@link DefaultScript}; never null.
     */
    public <T> Script<T> build() {
        return new DefaultScript<>(languageName, script, scriptParameters);
    }
}
