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
import org.rulii.script.*;

import javax.script.*;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * JSR-223-based implementation of {@link ScriptProcessor} that evaluates scripts
 * using a {@link ScriptEngine} obtained from the Java Scripting API.
 *
 * <p>When the underlying engine implements {@link Compilable}, each distinct
 * {@link Script} source text is compiled once and the resulting
 * {@link CompiledScript} is cached in a process-wide
 * {@code WeakHashMap} (wrapped in a synchronized map for thread safety).  The cache
 * is keyed by {@link Script} identity so that the compiled form is automatically
 * eligible for garbage collection once the owning {@code Script} instance is no
 * longer reachable.
 *
 * <p>For each evaluation a fresh {@link SimpleScriptContext} is constructed so that
 * bindings from different {@link RuleContext}s cannot leak between calls.  The rule
 * bindings are exposed inside the script as a {@link java.util.Map} variable whose
 * name is determined by {@link ScriptOptions#bindingsName()} (default: {@code "ctx"}).
 *
 * <p>Instances are normally created and registered automatically by
 * {@link org.rulii.script.DefaultScriptProcessorRegistry} when JSR-223
 * auto-discovery is enabled.
 *
 * @author Max Arulananthan
 * @since 1.2
 * @see ScriptProcessor
 * @see ScriptOptions
 * @see org.rulii.script.DefaultScriptProcessorRegistry
 */
public class JSR223ScriptProcessor implements ScriptProcessor {

    /** Process-wide cache mapping Script instances to their compiled forms. */
    private static final Map<Script<?>, CompiledScript> COMPILED_SCRIPTS = Collections.synchronizedMap(new WeakHashMap<>());

    private final ScriptEngine scriptEngine;
    private final String languageName;
    private final String bindingsName;
    private final Compilable compilable;

    /**
     * Constructs a {@code JSR223ScriptProcessor} with the specified script engine.
     *
     * @param scriptEngine the JSR-223 script engine to use for evaluation; must not be null.
     */
    public JSR223ScriptProcessor(ScriptEngine scriptEngine) {
        this(scriptEngine, null, null);
    }

    /**
     * Constructs a {@code JSR223ScriptProcessor} with explicit language name and
     * bindings variable name overrides.
     *
     * <p>If {@code languageName} is blank, the engine factory's language name is used.
     * If {@code bindingsName} is blank, {@code "ctx"} is used.
     *
     * @param scriptEngine the JSR-223 engine to use for evaluation; must not be null.
     * @param languageName the language name to advertise from {@link #getLanguageName()};
     *                     may be null/empty (falls back to engine factory name).
     * @param bindingsName the variable name under which the rule bindings are exposed
     *                     inside the script; may be null/empty (falls back to {@code "ctx"}).
     */
    public JSR223ScriptProcessor(ScriptEngine scriptEngine, String languageName, String bindingsName) {
        super();
        Assert.notNull(scriptEngine, "scriptEngine cannot be null.");
        this.scriptEngine = scriptEngine;
        this.languageName = StringUtils.hasText(languageName) ? languageName : scriptEngine.getFactory().getLanguageName();
        this.bindingsName = StringUtils.hasText(bindingsName) ? bindingsName : ScriptOptions.DEFAULT.bindingsName();
        this.compilable = scriptEngine instanceof Compilable ? (Compilable) scriptEngine : null;
    }

    /**
     * Evaluates the given script within the supplied rule context.
     *
     * <p>A fresh {@link ScriptContext} is built for every invocation to prevent
     * binding leakage between calls.  If the engine supports {@link Compilable},
     * the compiled form of the script is retrieved from the cache (compiling on
     * first use) and evaluated; otherwise the source text is interpreted directly.
     *
     * @param <T>     the expected return type.
     * @param script  the script to evaluate; must not be null.
     * @param context the rule context providing bindings; must not be null.
     * @return the value produced by the script, cast to {@code T}; may be null.
     * @throws EvaluationException if the script raises an error during evaluation.
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T evaluate(Script<T> script, RuleContext context) {
        ScriptContext scriptContext = buildContext(context);

        if (compilable == null) {
            try {
                return (T) scriptEngine.eval(script.getScript(), scriptContext);
            } catch (ScriptException e) {
                throw new EvaluationException(script.getScript(), e.getMessage(), e);
            }
        }

        CompiledScript compiledScript = getCompiledScript(script);

        try {
            return (T) compiledScript.eval(scriptContext);
        } catch (ScriptException e) {
            throw new EvaluationException(script.getScript(), e.getMessage(), e);
        }
    }

    /**
     * Returns the scripting language name advertised by this processor.
     *
     * @return the language name; never null or empty.
     */
    @Override
    public String getLanguageName() {
        return languageName;
    }

    /**
     * Returns the variable name under which the rule bindings map is exposed inside
     * scripts evaluated by processors created by this factory (e.g. {@code "ctx"}).
     *
     * @return the bindings variable name; never null or empty.
     */
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
     * Returns the compiled form of the given script, compiling it on first use
     * and caching it for subsequent calls.
     *
     * @param script the script to compile; must not be null.
     * @return the compiled script; never null.
     * @throws BuildScriptException if compilation fails.
     */
    protected CompiledScript getCompiledScript(Script<?> script) {
        CompiledScript cached = COMPILED_SCRIPTS.get(script);
        if (cached != null) return cached;
        CompiledScript compiled = compile(script);
        COMPILED_SCRIPTS.put(script, compiled);
        return compiled;
    }

    /**
     * Compiles the given script source text via the {@link Compilable} interface.
     *
     * @param script the script to compile; must not be null.
     * @return the compiled script; never null.
     * @throws BuildScriptException if the engine does not support {@code Compilable}
     *                              or if a compilation error occurs.
     */
    protected CompiledScript compile(Script<?> script) {
        if (compilable == null) throw new BuildScriptException(script.getScript(), "ScriptEngine [" + languageName + "] does not support Compilable.");

        try {
            return compilable.compile(script.getScript());
        } catch (ScriptException e) {
            throw new BuildScriptException(script.getScript(), e.getMessage(), e);
        }
    }

    /**
     * Builds a fresh {@link ScriptContext} for the given rule context, exposing the
     * rule bindings under the configured {@link #getBindingsName() bindings variable name}.
     *
     * @param context the rule context whose bindings should be exposed; must not be null.
     * @return a new {@link SimpleScriptContext} ready for use in a single evaluation.
     */
    protected ScriptContext buildContext(RuleContext context) {
        ScriptContext result = new SimpleScriptContext();
        Bindings scriptBindings = scriptEngine.createBindings();
        scriptBindings.put(getBindingsName(), context.getBindings().asMap());
        result.setBindings(scriptBindings, ScriptContext.ENGINE_SCOPE);
        return result;
    }
}
