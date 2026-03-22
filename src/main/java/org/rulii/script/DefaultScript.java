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
import org.rulii.model.UnrulyException;

import java.util.List;

/**
 * Default concrete implementation of {@link Script} that delegates evaluation to
 * the {@link ScriptProcessor} registered for the script's language in the
 * {@link ScriptProcessorRegistry} held by the executing {@link RuleContext}.
 *
 * <p>Instances are normally created via the fluent builder API rather than directly:
 * <pre>{@code
 * Script<Boolean> script = Script.builder()
 *         .with("ECMAScript", "age >= 18")
 *         .param(new ScriptParameter("age", Integer.class))
 *         .build();
 * }</pre>
 *
 * @param <T> the type of value produced when this script is evaluated.
 *
 * @author Max Arulananthan
 * @since 1.2
 * @see Script
 * @see AbstractScript
 * @see ScriptProcessor
 */
public class DefaultScript<T> extends AbstractScript<T> {

    /**
     * Constructs a new {@code DefaultScript} with the given language, source text, and parameters.
     *
     * @param languageName     the name of the scripting language (e.g. {@code "ECMAScript"}); must not be null or empty.
     * @param script           the script source text to be evaluated; must not be null or empty.
     * @param scriptParameters optional list of typed parameters the script expects; may be null or empty.
     */
    public DefaultScript(String languageName, String script, List<ScriptParameter> scriptParameters) {
        super(languageName, script, scriptParameters);
    }

    /**
     * Evaluates this script within the given rule context.
     *
     * <p>Looks up the {@link ScriptProcessor} for this script's language from
     * {@link RuleContext#getScriptProcessorRegistry()} and delegates evaluation to it.
     * If no processor is registered for the language an {@link UnrulyException} is thrown.
     *
     * @param ruleContext the execution context providing bindings and the processor registry; must not be null.
     * @return the value produced by the script, cast to {@code T}.
     * @throws UnrulyException if no {@link ScriptProcessor} is registered for this script's language,
     *                         or if an error occurs during evaluation.
     */
    @Override
    public T run(RuleContext ruleContext) throws UnrulyException {
        // getScriptProcessor() throws UnrulyException if not found — it never returns null.
        ScriptProcessor scriptProcessor = ruleContext.getScriptProcessorRegistry().getScriptProcessor(getLanguageName());
        return scriptProcessor.evaluate(this, ruleContext);
    }

    @Override
    public String toString() {
        return "Script{" +
                "script='" + getScript() + '\'' +
                "parameters='" + getScriptParameters() + '\'' +
                '}';
    }
}
