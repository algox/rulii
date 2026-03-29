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

/**
 * Represents an executable script fragment written in a supported scripting language.
 *
 * <p>A {@code Script} carries the raw source text, the target language name, and the expected
 * return type.  Execution is delegated to the {@link ScriptProcessor} registered for the language
 * in the current {@link org.rulii.context.RuleContext}.
 *
 * <p>Scripts are created through the fluent DSL:
 * <pre>{@code
 * Script<Boolean> isAdult = Script.builder()
 *         .build("java", "ctx.age >= 18");
 * }</pre>
 *
 * <p>A script may be wrapped by a {@link org.rulii.model.condition.Condition},
 * {@link org.rulii.model.action.Action}, or {@link org.rulii.model.function.Function} via their
 * respective builder {@code build(Script)} overloads, which set the appropriate return type
 * ({@code Boolean.class}, {@code void.class}, or {@code Object.class}) before the first evaluation.
 *
 * @param <T> the expected return type of the script.
 *
 * @author Max Arulananthan
 * @since 1.2
 * @see ScriptBuilder
 * @see ScriptBuilderBuilder
 * @see ScriptProcessor
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
     * Returns the expected return type of the script.
     *
     * <p>The default return type when built via {@link ScriptBuilder} is {@code void.class}.
     * Condition/Action/Function wrappers override this before the first evaluation.
     *
     * @return the return type; never null.
     */
    Class<?> getReturnType();

    /**
     * Overrides the expected return type of the script.
     *
     * <p>Called by framework wrappers (e.g. {@link org.rulii.model.condition.Condition},
     * {@link org.rulii.model.action.Action}) before the first evaluation to communicate the
     * required result type to the underlying {@link ScriptProcessor}.
     *
     * @param returnType the new return type; must not be null.
     */
    void setReturnType(Class<?> returnType);

}
