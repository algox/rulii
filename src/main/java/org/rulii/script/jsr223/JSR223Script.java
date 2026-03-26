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
package org.rulii.script.jsr223;

import org.rulii.context.RuleContext;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.UnrulyException;
import org.rulii.script.Script;
import org.rulii.script.ScriptParameter;

import javax.script.CompiledScript;
import java.util.Collections;
import java.util.List;

/**
 * JSR-223 implementation of {@link Script} that holds either a pre-compiled
 * {@link CompiledScript} or a raw source string for interpreted execution.
 *
 * <p>Instances are created by {@link JSR223ScriptCompiler}.  When the underlying
 * {@link javax.script.ScriptEngine} implements {@link javax.script.Compilable}, the
 * {@link #getCompiledScript()} will be non-null and {@link JSR223ScriptProcessor} will
 * use it for faster repeated evaluation; otherwise the engine interprets the raw source
 * on every call.
 *
 * @param <T> the expected return type of the script.
 *
 * @author Max Arulananthan
 * @since 1.2
 * @see JSR223ScriptCompiler
 * @see JSR223ScriptProcessor
 */
public class JSR223Script<T> implements Script<T> {

    private final String languageName;
    private final String script;
    private final CompiledScript compiledScript;
    private final List<ScriptParameter> scriptParameters;

    /**
     * Creates a new {@code JSR223Script}.
     *
     * @param languageName     the scripting language name; must not be null or empty.
     * @param script           the raw script source text; must not be null or empty.
     * @param compiledScript   the pre-compiled form, or {@code null} for interpreted execution.
     * @param scriptParameters the parameter declarations; may be null (treated as empty list).
     */
    public JSR223Script(String languageName, String script, CompiledScript compiledScript, List<ScriptParameter> scriptParameters) {
        super();
        Assert.hasText(languageName, "languageName cannot be empty.");
        Assert.hasText(script, "script cannot be empty.");
        this.languageName = languageName;
        this.script = script;
        this.compiledScript = compiledScript;
        this.scriptParameters = scriptParameters != null ? Collections.unmodifiableList(scriptParameters) : List.of();
    }

    @Override
    public T run(RuleContext ruleContext) throws UnrulyException {
        return ruleContext.getScriptProcessor(getLanguageName()).evaluate(this, ruleContext);
    }

    @Override
    public String getLanguageName() {
        return languageName;
    }

    @Override
    public String getScript() {
        return script;
    }

    @Override
    public List<ScriptParameter> getScriptParameters() {
        return scriptParameters;
    }

    /**
     * Returns the pre-compiled form of the script, or {@code null} if the engine does not support
     * compilation and the script will be interpreted on every evaluation.
     *
     * @return the compiled script, or {@code null}.
     */
    public CompiledScript getCompiledScript() {
        return compiledScript;
    }

    @Override
    public String toString() {
        return "JSR223Script{" +
                "languageName='" + languageName + '\'' +
                ", script='" + script + '\'' +
                ", compiledScript=" + compiledScript +
                ", scriptParameters=" + scriptParameters +
                '}';
    }
}
