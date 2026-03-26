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
 * Factory interface for creating language-specific {@link ScriptProcessor} and {@link ScriptCompiler} instances.
 *
 * <p>Each implementation corresponds to one scripting language (or engine variant). Implementations
 * may be registered manually via {@link ScriptProcessorManager#register(ScriptProcessorFactory)} or
 * discovered automatically via the {@link java.util.ServiceLoader} mechanism by registering the
 * implementation class in {@code META-INF/services/org.rulii.script.ScriptProcessorFactory}.
 *
 * <p>{@link #isAvailable()} should return {@code false} when the underlying engine or runtime
 * dependency is not present on the class path, which allows optional engine JARs to be handled
 * gracefully at startup.
 *
 * @author Max Arulananthan
 * @since 1.2
 * @see ScriptProcessor
 * @see ScriptCompiler
 * @see ScriptProcessorManager
 */
public interface ScriptProcessorFactory {

    /**
     * Returns {@code true} if this factory's underlying script engine is available on the class path.
     *
     * <p>The default implementation always returns {@code true}; override to add a class-presence check.
     *
     * @return {@code true} if the engine is available; {@code false} otherwise.
     */
    default boolean isAvailable() {
        return true;
    }

    /**
     * Returns the name of the scripting language this factory supports (e.g. {@code "js"}, {@code "groovy"}).
     *
     * @return the language name; never null or empty.
     */
    String getLanguageName();

    /**
     * Returns the name of the variable under which the rule bindings map will be exposed inside scripts.
     *
     * @return the bindings variable name; never null or empty.
     */
    String getBindingName();

    /**
     * Creates and returns a new {@link ScriptProcessor} for this language.
     *
     * @return a new processor instance; never null.
     */
    ScriptProcessor getScriptProcessor();

    /**
     * Creates and returns a new {@link ScriptCompiler} for this language.
     *
     * @return a new compiler instance; never null.
     */
    ScriptCompiler getScriptCompiler();
}
