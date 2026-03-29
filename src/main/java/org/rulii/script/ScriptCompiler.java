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
 * Strategy interface for compiling a raw script string into an executable {@link Script} instance.
 *
 * <p>Implementations are language-specific. If the underlying engine supports pre-compilation
 * (e.g. JSR-223 {@link javax.script.Compilable}), the returned {@code Script} will carry a
 * compiled representation; otherwise the source text is retained for interpreted execution.
 *
 * <p>Instances are typically obtained from a {@link ScriptProcessorFactory} rather than
 * constructed directly.
 *
 * @author Max Arulananthan
 * @since 1.2
 * @see ScriptProcessorFactory
 * @see Script
 */
public interface ScriptCompiler {

    /**
     * Returns the name of the scripting language this compiler handles (e.g. {@code "js"}, {@code "groovy"}).
     *
     * @return the language name; never null or empty.
     */
    String getLanguageName();

    /**
     * Compiles the given script source into an executable {@link Script}.
     *
     * @param <T>        the expected return type of the compiled script.
     * @param script     the script source text; must not be null or empty.
     * @param returnType the result return type.
     * @return a compiled (or source-retaining) {@link Script} instance; never null.
     * @throws BuildScriptException if the script source cannot be compiled.
     */
    <T> Script<T> compile(String script, Class<?> returnType);
}
