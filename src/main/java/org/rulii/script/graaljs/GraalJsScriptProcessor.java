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
package org.rulii.script.graaljs;

import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.SourceSection;
import org.graalvm.polyglot.Value;
import org.rulii.context.RuleContext;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.UnrulyException;
import org.rulii.script.EvaluationException;
import org.rulii.script.Script;
import org.rulii.script.jsr223.JSR223ScriptProcessor;

import javax.script.ScriptContext;
import javax.script.ScriptException;

/**
 * {@link JSR223ScriptProcessor} specialised for GraalJS.
 *
 * <p>GraalJS backs every {@link javax.script.Bindings} it creates with its own polyglot
 * {@link Context}. The base class releases that context after each evaluation, but only when it
 * can be sure the result does not depend on it; by default that means primitive-like results
 * only. This subclass asks GraalJS directly: a result that GraalJS reports as a host object
 * (any Java object, including the rule bindings themselves and the detached copies produced by
 * {@link GraalJsHostAccess}), a string, a number, a boolean or null is independent of the
 * context and the context is released. A result that is still a guest object, which with
 * {@link GraalJsHostAccess#DETACHING} in force means a JavaScript function or class, is a proxy
 * onto the context and would break if the context were closed, so the context is left alive in
 * that case.
 *
 * @author Max Arulananthan
 * @since 2.1
 * @see GraalJsScriptProcessorFactory
 * @see JSR223ScriptProcessor#isContextReleasable(ScriptContext, Object)
 */
public class GraalJsScriptProcessor extends JSR223ScriptProcessor {

    // Null unless the factory reuses contexts.
    private final GraalJsContextPool pool;

    /**
     * Creates a processor over the given GraalJS engine.
     *
     * @param scriptEngine the GraalJS engine to evaluate with; must not be null.
     * @param languageName override for the language name, or {@code null} to use the engine's default.
     * @param bindingsName override for the bindings variable name, or {@code null} to use
     *                     {@link org.rulii.script.ScriptOptions#DEFAULT}.
     */
    public GraalJsScriptProcessor(GraalJSScriptEngine scriptEngine, String languageName, String bindingsName) {
        this(scriptEngine, languageName, bindingsName, null);
    }

    /**
     * Creates a processor that evaluates in the contexts of the given pool instead of a new context per
     * evaluation; see {@link GraalJsScriptProcessorFactory#isReuseContexts()}.
     *
     * @param scriptEngine the GraalJS engine; must not be null.
     * @param languageName override for the language name, or {@code null} to use the engine's default.
     * @param bindingsName override for the bindings variable name, or {@code null} to use the default.
     * @param pool the contexts to evaluate in, or {@code null} for a new context per evaluation.
     */
    GraalJsScriptProcessor(GraalJSScriptEngine scriptEngine, String languageName, String bindingsName, GraalJsContextPool pool) {
        super(scriptEngine, languageName, bindingsName);
        this.pool = pool;
    }

    /**
     * Evaluates in a new context, released afterwards (the default), or in a borrowed one when the factory reuses
     * contexts. A borrowed context goes back for the next evaluation unless the script failed (it is closed: a
     * failure may have ended it) or its result is still a JavaScript object (it stays alive for that result, with
     * the rule bindings in it, and is not used again), which is what happens to a context of its own in those two
     * cases.
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T evaluate(Script<T> script, RuleContext context) {
        if (pool == null) return super.evaluate(script, context);

        Assert.notNull(script, "script cannot be null.");
        Assert.notNull(context, "context cannot be null.");

        GraalJsContextPool.Entry entry = pool.borrow();
        boolean succeeded = false;
        boolean independent = false;

        try {
            Object result;

            try {
                result = entry.run(getBindingsName(), context.getBindings().asMap(), script.getScript());
            } catch (UnrulyException e) {
                throw e;
            } catch (PolyglotException e) {
                // Reported as the JSR-223 engine reports it, so a failure reads the same with and without reuse.
                ScriptException reported = toScriptException(e);
                throw new EvaluationException(script.getScript(), reported.getMessage(), reported);
            } catch (Exception e) {
                throw new EvaluationException(script.getScript(), e.getMessage(), e);
            }

            succeeded = true;
            independent = isIndependent(entry.getContext(), result);
            return (T) result;
        } finally {
            if (!succeeded) pool.fail(entry);
            else if (independent) pool.giveBack(entry);
            else pool.detach(entry);
        }
    }

    /**
     * Returns {@code true} unless GraalJS reports {@code result} as a guest (JavaScript) value.
     *
     * <p>If the classification itself fails for any reason the answer is {@code false}: keeping
     * the context alive reproduces the behaviour prior to context release, whereas closing it
     * wrongly would corrupt the result.
     */
    @Override
    protected boolean isContextReleasable(ScriptContext scriptContext, Object result) {
        if (result == null) return true;

        try {
            return isIndependent(((GraalJSScriptEngine) getScriptEngine()).getPolyglotContext(scriptContext), result);
        } catch (RuntimeException e) {
            return false;
        }
    }

    /**
     * The failure as {@code GraalJSScriptEngine} reports one: a host exception as the cause, a JavaScript error with
     * the place it happened in the script.
     */
    private static ScriptException toScriptException(PolyglotException e) {
        if (e.isHostException()) {
            Throwable host = e.asHostException();
            ScriptException result = new ScriptException(host instanceof Exception ? (Exception) host : new Exception(host));
            result.setStackTrace(e.getStackTrace());
            return result;
        }

        SourceSection location = e.getSourceLocation();

        if (location != null && location.isAvailable()) {
            Source source = location.getSource();
            ScriptException result = new ScriptException(e.getMessage(), source.getPath() != null ? source.getPath() : source.getName(),
                    location.getStartLine(), location.getStartColumn());
            result.initCause(e);
            return result;
        }

        return new ScriptException(e);
    }

    /**
     * Whether the result can outlive the context it was produced in: anything but a guest (JavaScript) value.
     */
    private static boolean isIndependent(Context polyglotContext, Object result) {
        if (result == null) return true;

        try {
            Value value = polyglotContext.asValue(result);
            return value.isNull()
                    || value.isHostObject()
                    || value.isString()
                    || value.isNumber()
                    || value.isBoolean();
        } catch (RuntimeException e) {
            return false;
        }
    }
}
