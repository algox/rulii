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

/**
 * Singleton entry point for the {@link Script} builder API.
 *
 * <p>{@code ScriptBuilderBuilder} is accessed via {@link Script#builder()} and provides
 * two paths to creating a {@link Script}:
 * <ul>
 *   <li>{@link #with(String, String)} — returns a {@link ScriptBuilder} that allows
 *       optional {@link ScriptParameter}s to be added before calling
 *       {@link ScriptBuilder#build()}.</li>
 *   <li>{@link #build(String, String)} — convenience shortcut that creates a
 *       parameter-free {@link Script} in one call.</li>
 * </ul>
 *
 * <pre>{@code
 * // With parameters
 * Script<Boolean> script = Script.builder()
 *         .with("ECMAScript", "age >= 18")
 *         .param(new ScriptParameter("age", Integer.class))
 *         .build();
 *
 * // Without parameters
 * Script<Double> script = Script.builder().build("ECMAScript", "Math.PI");
 * }</pre>
 *
 * @author Max Arulananthan
 * @since 1.2
 * @see Script
 * @see ScriptBuilder
 */
public final class ScriptBuilderBuilder {

    /** Singleton instance. */
    private static final ScriptBuilderBuilder instance = new ScriptBuilderBuilder();

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
     * Returns a {@link ScriptBuilder} pre-configured with the given language and
     * source text.  Use the returned builder to add {@link ScriptParameter}s and
     * then call {@link ScriptBuilder#build()}.
     *
     * @param language the scripting language name (e.g. {@code "ECMAScript"}); must not be null or empty.
     * @param script   the script source text; must not be null or empty.
     * @return a new {@link ScriptBuilder}; never null.
     */
    public ScriptBuilder with(String language, String script) {
        return new ScriptBuilder(language, script);
    }

    /**
     * Convenience method that creates a parameter-free {@link Script} directly
     * from the given language and source text.
     *
     * @param <T>      the expected return type of the script.
     * @param language the scripting language name (e.g. {@code "ECMAScript"}); must not be null or empty.
     * @param script   the script source text; must not be null or empty.
     * @return a new {@link Script}; never null.
     */
    public <T> Script<T> build(String language, String script) {
        return new ScriptBuilder(language, script).build();
    }
}
