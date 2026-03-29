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

import org.rulii.lib.spring.util.Assert;
import org.rulii.model.UnrulyException;
import org.rulii.script.BuildScriptException;
import org.rulii.script.Script;
import org.rulii.script.ScriptCompiler;

import javax.script.Compilable;
import javax.script.ScriptEngine;

/**
 * JSR-223 implementation of {@link ScriptCompiler} that compiles a script source string into
 * a {@link JSR223Script}.
 *
 * <p>If the provided {@link ScriptEngine} implements {@link Compilable}, the source is compiled
 * once and stored in a {@link javax.script.CompiledScript}; subsequent evaluations benefit from
 * the pre-compiled form.  When the engine does not support compilation the raw source is retained
 * and interpreted on every call.
 *
 * @author Max Arulananthan
 * @since 1.2
 * @see JSR223Script
 * @see JSR223ScriptProcessorFactory
 */
public class JSR223ScriptCompiler implements ScriptCompiler {

    private final String languageName;
    private final Compilable compilable;

    /**
     * Creates a compiler using the language name reported by the engine's factory.
     *
     * @param scriptEngine the JSR-223 engine; must not be null.
     */
    public JSR223ScriptCompiler(ScriptEngine scriptEngine) {
        this(scriptEngine.getFactory().getLanguageName(), scriptEngine);
    }

    /**
     * Creates a compiler with an explicit language name override.
     *
     * @param languageName an override for the language name, or {@code null} to use the engine's
     *                     own reported language name.
     * @param scriptEngine the JSR-223 engine; must not be null.
     */
    public JSR223ScriptCompiler(String languageName, ScriptEngine scriptEngine) {
        super();
        Assert.notNull(scriptEngine, "scriptEngine cannot be null.");
        this.languageName = languageName != null ? languageName : scriptEngine.getFactory().getLanguageName();
        this.compilable = scriptEngine instanceof Compilable ? (Compilable) scriptEngine : null;
    }

    @Override
    public String getLanguageName() {
        return languageName;
    }

    @Override
    public <T> Script<T> compile(String script, Class<?> returnType) {
        if (compilable == null) return new JSR223Script<>(languageName, script, null, returnType);

        try {
            return new JSR223Script<>(languageName, script, compilable.compile(script), returnType);
        } catch (UnrulyException e) {
            throw e;
        } catch (Exception e) {
            throw new BuildScriptException(script, e.getMessage(), e);
        }
    }
}
