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

import org.rulii.model.UnrulyException;

/**
 * Entry point for the fluent script-building DSL; provides factory methods for creating
 * {@link ScriptBuilder} instances.
 *
 * <p>This class is a singleton obtained via {@link #getInstance()} or through
 * {@link Script#builder()}.  It delegates language lookup to the shared
 * {@link ScriptProcessorManager}.
 *
 * <p>Typical usage:
 * <pre>{@code
 * Script<Boolean> script = Script.builder()
 *         .with("js", "age >= 18")
 *         .build();
 * }</pre>
 *
 * @author Max Arulananthan
 * @since 1.2
 * @see Script#builder()
 * @see ScriptBuilder
 * @see ScriptProcessorManager
 */
public final class ScriptBuilderBuilder {

    /** Singleton instance. */
    private static final ScriptBuilderBuilder instance = new ScriptBuilderBuilder();
    private static final ScriptProcessorManager scriptProcessorManager = ScriptProcessorManager.getInstance();

    private ScriptBuilderBuilder() {
        super();
    }

    /**
     * Returns the singleton instance of {@code ScriptBuilderBuilder}.
     *
     * @return the singleton instance; never null.
     */
    public static ScriptBuilderBuilder getInstance() {
        return instance;
    }

    /**
     * Creates a {@link ScriptBuilder} for the given scripting language and source text.
     *
     * @param language the scripting language name (e.g. {@code "js"}, {@code "groovy"}); must not be null or empty.
     * @param script   the script source text; must not be null or empty.
     * @return a new {@link ScriptBuilder} ready to accept parameter declarations; never null.
     * @throws org.rulii.model.UnrulyException if no {@link ScriptProcessorFactory} is registered for the language.
     */
    public ScriptBuilder with(String language, String script) {
        ScriptProcessorFactory factory = scriptProcessorManager.getScriptProcessorFactory(language);

        if (factory == null) throw new UnrulyException("No ScriptProcessorFactory found for language: " + language);

        // Apply the configured script-text resolver (e.g. Spring ${property:default}
        // placeholder resolution) before the text reaches the compiler.
        return new ScriptBuilder(factory, scriptProcessorManager.resolveScriptText(script));
    }

    /**
     * Convenience method that compiles and returns the script in a single step.
     *
     * <p>Equivalent to {@code with(language, script).build()}.
     *
     * @param <T>      the expected return type of the script.
     * @param language the scripting language name; must not be null or empty.
     * @param script   the script source text; must not be null or empty.
     * @return the compiled {@link Script}; never null.
     * @throws org.rulii.model.UnrulyException if no factory is found for the language.
     * @throws BuildScriptException if the source cannot be compiled.
     */
    public <T> Script<T> build(String language, String script) {
        return with(language, script).build();
    }
}
