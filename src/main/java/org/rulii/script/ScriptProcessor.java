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

import org.rulii.context.RuleContext;

/**
 * Strategy interface for evaluating a {@link Script} against a rule execution context.
 *
 * <p>A {@code ScriptProcessor} is responsible for a single scripting language
 * (identified by {@link #getLanguageName()}) and knows how to execute a script
 * source text, expose the rule bindings to that script, and return the result.
 *
 * <p>Implementations are registered with a {@link ScriptProcessorRegistry} and
 * retrieved by language name when a {@link Script} is run.  The JSR-223-based
 * implementation is provided by
 * {@link org.rulii.script.jsr223.JSR223ScriptProcessor}.
 *
 * @author Max Arulananthan
 * @since 1.2
 * @see ScriptProcessorRegistry
 * @see org.rulii.script.jsr223.JSR223ScriptProcessor
 */
public interface ScriptProcessor {

    /**
     * Returns the name of the scripting language handled by this processor
     * (e.g. {@code "ECMAScript"}).
     *
     * @return the language name; never null or empty.
     */
    String getLanguageName();

    /**
     * Evaluates the given script within the supplied rule context and returns
     * the result.
     *
     * <p>The processor is responsible for making the bindings available to the
     * script (typically by exposing them as variables in the script's scope) and
     * for converting any engine-level exceptions into appropriate runtime exceptions.
     *
     * @param <T>        the expected return type of the script.
     * @param script     the script to evaluate; must not be null.
     * @param ruleContext the execution context providing bindings and services; must not be null.
     * @return the value produced by the script, cast to {@code T}; may be null.
     * @throws EvaluationException if the script raises an error during evaluation.
     */
    <T> T evaluate(Script<T> script, RuleContext ruleContext);
}
