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
 * Represents a script that can be evaluated within a rule execution context.
 *
 * <p>A {@code Script} encapsulates a scripting-language source text together with
 * the name of the language it is written in and an optional list of typed
 * {@link ScriptParameter}s that the script expects to receive.  When
 * {@link org.rulii.model.Runnable#run(org.rulii.context.RuleContext) run(RuleContext)}
 * is invoked the script is evaluated and its result (of type {@code T}) is returned.
 *
 * <p>New {@code Script} instances are created through the fluent builder API:
 * <pre>{@code
 * Script<Boolean> script = Script.builder()
 *         .with("ECMAScript", "age >= 18")
 *         .build();
 * }</pre>
 *
 * @param <T> the type of value produced when this script is evaluated.
 *
 * @author Max Arulananthan
 * @since 1.2
 * @see ScriptBuilderBuilder
 * @see AbstractScript
 * @see ScriptProcessor
 */
public interface Script<T> extends Runnable<T> {

    /**
     * Returns the singleton {@link ScriptBuilderBuilder} that is the entry point
     * for constructing new {@code Script} instances via the fluent builder API.
     *
     * @return the {@code ScriptBuilderBuilder} singleton; never null.
     */
    static ScriptBuilderBuilder builder() {
        return ScriptBuilderBuilder.getInstance();
    }

    /**
     * Returns the name of the scripting language this script is written in
     * (e.g. {@code "ECMAScript"}).
     *
     * @return the language name; never null or empty.
     */
    String getLanguageName();

    /**
     * Returns the original source text of this script.
     *
     * @return the script source; never null or empty.
     */
    String getScript();

    /**
     * Returns the list of typed parameters declared for this script.
     * The list is empty when no parameters were provided at construction time.
     *
     * @return list of {@link ScriptParameter}s; never null.
     */
    List<ScriptParameter> getScriptParameters();

}
