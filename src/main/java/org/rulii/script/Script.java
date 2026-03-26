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

import org.rulii.model.Runnable;

import java.util.List;

/**
 * Represents a compiled or interpreted script that can be executed within a {@link org.rulii.context.RuleContext}.
 *
 * <p>A {@code Script} encapsulates a script source string together with its target scripting language
 * and any declared {@link ScriptParameter}s that the script expects to receive at evaluation time.
 * Implementations are created via the fluent builder API:
 *
 * <pre>{@code
 * Script<Boolean> script = Script.builder()
 *         .with("js", "age >= 18")
 *         .build();
 * }</pre>
 *
 * <p>Scripts are executed through an appropriate {@link ScriptProcessor} that is looked up from the
 * {@link org.rulii.context.RuleContext} by language name at runtime.
 *
 * @param <T> the type of value produced by the script.
 *
 * @author Max Arulananthan
 * @since 1.2
 * @see ScriptProcessor
 * @see ScriptCompiler
 * @see ScriptBuilderBuilder
 */
public interface Script<T> extends Runnable<T> {

    /**
     * Returns the entry point for the fluent script-building DSL.
     *
     * @return the singleton {@link ScriptBuilderBuilder}; never null.
     */
    static ScriptBuilderBuilder builder() {
        return ScriptBuilderBuilder.getInstance();
    }

    /**
     * Returns the name of the scripting language this script is written in (e.g. {@code "js"}, {@code "groovy"}).
     *
     * @return the language name; never null or empty.
     */
    String getLanguageName();

    /**
     * Returns the raw script source text.
     *
     * @return the script source; never null or empty.
     */
    String getScript();

    /**
     * Returns the list of parameters this script declares, in declaration order.
     *
     * @return an unmodifiable list of {@link ScriptParameter}s; never null, may be empty.
     */
    List<ScriptParameter> getScriptParameters();

}
