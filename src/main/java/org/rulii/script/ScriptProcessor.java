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

import org.rulii.context.RuleContext;

/**
 * Strategy interface for evaluating a {@link Script} against a given {@link RuleContext}.
 *
 * <p>A {@code ScriptProcessor} is language-specific and handles the runtime execution of
 * scripts that have previously been compiled (or left as source) by a {@link ScriptCompiler}.
 * It bridges the rule context bindings into the script's execution environment and returns
 * whatever value the script produces.
 *
 * <p>Instances are typically obtained from a {@link ScriptProcessorFactory} and looked up at
 * runtime from the {@link RuleContext} by language name.
 *
 * @author Max Arulananthan
 * @since 1.2
 * @see ScriptProcessorFactory
 * @see Script
 */
public interface ScriptProcessor {

    /**
     * Returns the name of the scripting language this processor handles (e.g. {@code "js"}, {@code "groovy"}).
     *
     * @return the language name; never null or empty.
     */
    String getLanguageName();

    /**
     * Returns the name of the variable under which the rule bindings map is exposed inside scripts.
     *
     * @return the bindings variable name; never null or empty.
     */
    String getBindingsName();

    /**
     * Evaluates the given script using the bindings available in the provided {@link RuleContext}.
     *
     * @param <T>         the expected return type.
     * @param script      the script to evaluate; must not be null.
     * @param ruleContext the context providing bindings and services; must not be null.
     * @return the value produced by the script, or {@code null} if the script produces no value.
     * @throws EvaluationException if the script fails during evaluation.
     */
    <T> T evaluate(Script<T> script, RuleContext ruleContext);
}
