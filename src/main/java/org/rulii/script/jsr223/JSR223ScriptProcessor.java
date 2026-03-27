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
import org.rulii.lib.spring.util.StringUtils;
import org.rulii.script.EvaluationException;
import org.rulii.script.Script;
import org.rulii.script.ScriptOptions;
import org.rulii.script.ScriptProcessor;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.SimpleScriptContext;


/**
 * JSR-223 implementation of {@link ScriptProcessor} that evaluates a {@link JSR223Script}
 * against a {@link org.rulii.context.RuleContext}.
 *
 * <p>Before evaluation, the processor builds a {@link ScriptContext} that exposes the rule
 * bindings as a plain {@code Map} under the variable name returned by {@link #getBindingsName()}.
 * If the script carries a pre-compiled {@link javax.script.CompiledScript} it is used directly;
 * otherwise the raw source is interpreted by the underlying engine.
 *
 * @author Max Arulananthan
 * @since 1.2
 * @see JSR223Script
 * @see JSR223ScriptProcessorFactory
 */
public class JSR223ScriptProcessor implements ScriptProcessor {

    private final ScriptEngine scriptEngine;
    private final String languageName;
    private final String bindingsName;

    /**
     * Creates a processor using the language name and default bindings name from the engine's factory.
     *
     * @param scriptEngine the JSR-223 engine to use for evaluation; must not be null.
     */
    public JSR223ScriptProcessor(ScriptEngine scriptEngine) {
        this(scriptEngine, null, null);
    }

    /**
     * Creates a processor with explicit language name and bindings variable name overrides.
     *
     * @param scriptEngine the JSR-223 engine to use for evaluation; must not be null.
     * @param languageName override for the language name, or {@code null} to use the engine's default.
     * @param bindingsName override for the bindings variable name, or {@code null} to use
     *                     {@link org.rulii.script.ScriptOptions#DEFAULT}.
     */
    public JSR223ScriptProcessor(ScriptEngine scriptEngine, String languageName, String bindingsName) {
        super();
        Assert.notNull(scriptEngine, "scriptEngine cannot be null.");
        this.scriptEngine = scriptEngine;
        this.languageName = StringUtils.hasText(languageName) ? languageName : scriptEngine.getFactory().getLanguageName();
        this.bindingsName = StringUtils.hasText(bindingsName) ? bindingsName : ScriptOptions.DEFAULT.bindingsName();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T evaluate(Script<T> script, RuleContext context) {
        Assert.notNull(script, "script cannot be null.");
        Assert.notNull(context, "context cannot be null.");

        ScriptContext scriptContext = buildContext(context);
        JSR223Script<T> jsr223Script = (JSR223Script<T>) script;

        if (jsr223Script.getCompiledScript() == null) {
            try {
                return (T) scriptEngine.eval(script.getScript(), scriptContext);
            } catch (Exception e) {
                throw new EvaluationException(script.getScript(), e.getMessage(), e);
            }
        }

        try {
            return (T) jsr223Script.getCompiledScript().eval(scriptContext);
        } catch (Exception e) {
            throw new EvaluationException(script.getScript(), e.getMessage(), e);
        }
    }

    @Override
    public String getLanguageName() {
        return languageName;
    }

    public String getBindingName() {
        return bindingsName;
    }

    /**
     * Returns the variable name under which the rule bindings map is exposed
     * inside scripts evaluated by this processor.
     *
     * @return the bindings variable name; never null or empty.
     */
    public String getBindingsName() {
        return bindingsName;
    }

    /**
     * Returns the underlying JSR-223 {@link ScriptEngine} used for evaluation.
     *
     * @return the script engine; never null.
     */
    public ScriptEngine getScriptEngine() {
        return scriptEngine;
    }

    /**
     * Builds the {@link ScriptContext} that will be passed to the engine during evaluation.
     *
     * <p>The rule bindings are exposed as a plain {@code Map} under the variable name
     * returned by {@link #getBindingsName()}.
     *
     * @param context the current rule context; must not be null.
     * @return a configured {@link ScriptContext}; never null.
     */
    protected ScriptContext buildContext(RuleContext context) {
        ScriptContext result = new SimpleScriptContext();
        Bindings scriptBindings = scriptEngine.createBindings();
        scriptBindings.put(getBindingsName(), context.getBindings().asMap());
        result.setBindings(scriptBindings, ScriptContext.ENGINE_SCOPE);
        return result;
    }
}
