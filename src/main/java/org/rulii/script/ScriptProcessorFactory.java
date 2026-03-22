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

/**
 * Factory interface for creating {@link ScriptProcessor} instances for a specific
 * scripting language.
 *
 * <p>A {@code ScriptProcessorFactory} encapsulates everything needed to produce a
 * ready-to-use {@link ScriptProcessor}: the language name, the bindings variable
 * name, and the logic for constructing the underlying scripting engine.  Factories
 * are used by {@link ScriptProcessorRegistry} implementations to lazily instantiate
 * processors on demand.
 *
 * <p>Built-in implementations are provided for JSR-223 engines
 * ({@link org.rulii.script.jsr223.JSR223ScriptProcessorFactory}) and for GraalVM JS
 * ({@link org.rulii.script.graaljs.GraalJsScriptProcessorFactory}).
 *
 * @author Max Arulananthan
 * @since 1.2
 * @see ScriptProcessor
 * @see ScriptProcessorRegistry
 * @see org.rulii.script.jsr223.JSR223ScriptProcessorFactory
 * @see org.rulii.script.graaljs.GraalJsScriptProcessorFactory
 */
public interface ScriptProcessorFactory {

    /**
     * Returns the name of the scripting language handled by processors created by
     * this factory (e.g. {@code "ECMAScript"}).
     *
     * @return the language name; never null or empty.
     */
    String getLanguageName();

    /**
     * Returns the variable name under which the rule bindings map is exposed inside
     * scripts evaluated by processors created by this factory (e.g. {@code "ctx"}).
     *
     * @return the bindings variable name; never null or empty.
     */
    String getBindingName();

    /**
     * Creates and returns a new {@link ScriptProcessor} for this factory's language.
     * Each call may produce a distinct processor instance backed by a fresh engine.
     *
     * @return a new {@link ScriptProcessor}; never null.
     */
    ScriptProcessor create();
}
