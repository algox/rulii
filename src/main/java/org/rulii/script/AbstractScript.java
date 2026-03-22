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

import org.rulii.lib.spring.util.Assert;

import java.util.LinkedList;
import java.util.List;

/**
 * Base implementation of {@link Script} that stores the language name, script source text,
 * and an optional list of {@link ScriptParameter}s.
 *
 * <p>Concrete subclasses are responsible for implementing
 * {@link org.rulii.model.Runnable#run(org.rulii.context.RuleContext) run(RuleContext)},
 * which defines how the script is evaluated against a rule context.
 *
 * @param <T> the type of value produced when this script is evaluated.
 *
 * @author Max Arulananthan
 * @since 1.2
 * @see Script
 * @see ScriptParameter
 * @see DefaultScript
 */
public abstract class AbstractScript<T> implements Script<T> {

    private final String languageName;
    private final String script;
    private final List<ScriptParameter> scriptParameters = new LinkedList<>();

    /**
     * Constructs a new {@code AbstractScript} with the given language, source text, and parameters.
     *
     * @param languageName     the name of the scripting language (e.g. {@code "ECMAScript"}); must not be null or empty.
     * @param script           the script source text to be evaluated; must not be null or empty.
     * @param scriptParameters optional list of typed parameters the script expects; may be null or empty.
     * @throws IllegalArgumentException if {@code languageName} or {@code script} is null or empty.
     */
    protected AbstractScript(String languageName, String script, List<ScriptParameter> scriptParameters) {
        super();
        Assert.hasText(languageName, "languageName cannot be empty.");
        Assert.hasText(script, "script cannot be empty.");
        this.languageName = languageName;
        this.script = script;
        if (scriptParameters != null) this.scriptParameters.addAll(scriptParameters);
    }

    /**
     * Returns the name of the scripting language this script is written in.
     *
     * @return the language name; never null or empty.
     */
    @Override
    public String getLanguageName() {
        return languageName;
    }

    /**
     * Returns the source text of this script.
     *
     * @return the script source; never null or empty.
     */
    @Override
    public String getScript() {
        return script;
    }

    /**
     * Returns the list of typed parameters declared for this script.
     * The list is empty when no parameters were provided at construction time.
     *
     * @return mutable list of {@link ScriptParameter}s; never null.
     */
    @Override
    public List<ScriptParameter> getScriptParameters() {
        return scriptParameters;
    }

}
