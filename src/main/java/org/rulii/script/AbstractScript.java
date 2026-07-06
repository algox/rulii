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
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.UnrulyException;

/**
 * Base implementation of {@link Script} that stores the language name, raw source text, and
 * expected return type, and delegates execution to the {@link ScriptProcessor} registered for
 * the language in the current {@link RuleContext}.
 *
 * <p>Concrete subclasses (e.g. {@link org.rulii.script.jsr223.JSR223Script},
 * {@link org.rulii.script.janino.JITScript}) extend this class to hold any additional
 * compiled or engine-specific state.
 *
 * @param <T> the expected return type of the script.
 *
 * @author Max Arulananthan
 * @since 1.2
 * @see Script
 * @see ScriptProcessor
 */
public abstract class AbstractScript<T> implements Script<T> {

    private final String languageName;
    private final String script;
    private Class<?> returnType;

    /**
     * Creates a new Script.
     *
     * @param languageName the scripting language name (e.g. {@code "js"}, {@code "java"});
     *                     must not be null or empty.
     * @param script       the raw script source text; must not be null or empty.
     * @param returnType   the expected result return type; {@code void.class} for scripts with
     *                     no meaningful return value.
     */
    protected AbstractScript(String languageName, String script, Class<?> returnType) {
        super();
        Assert.hasText(languageName, "languageName cannot be empty.");
        Assert.hasText(script, "script cannot be empty.");
        Assert.notNull(returnType, "returnType cannot be null.");
        this.languageName = languageName;
        this.script = script;
        this.returnType = returnType;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Delegates to the {@link ScriptProcessor} registered for {@link #getLanguageName()} in
     * the provided {@code ruleContext}.
     *
     * @throws UnrulyException if no processor is registered for the language, or if the script
     *                         fails during evaluation.
     */
    @Override
    public T run(RuleContext ruleContext) throws UnrulyException {
        return ruleContext.getScriptProcessor(getLanguageName()).evaluate(this, ruleContext);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLanguageName() {
        return languageName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getScript() {
        return script;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getReturnType() {
        return returnType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setReturnType(Class<?> returnType) {
        Assert.notNull(returnType, "returnType cannot be null.");
        this.returnType = returnType;
    }

    @Override
    public String toString() {
        return "AbstractScript{" +
                "languageName='" + languageName + '\'' +
                ", script='" + script + '\'' +
                ", returnType=" + returnType +
                '}';
    }
}
