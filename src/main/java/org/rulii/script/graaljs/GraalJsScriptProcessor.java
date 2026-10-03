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
import org.graalvm.polyglot.Value;
import org.rulii.script.jsr223.JSR223ScriptProcessor;

import javax.script.ScriptContext;

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

    /**
     * Creates a processor over the given GraalJS engine.
     *
     * @param scriptEngine the GraalJS engine to evaluate with; must not be null.
     * @param languageName override for the language name, or {@code null} to use the engine's default.
     * @param bindingsName override for the bindings variable name, or {@code null} to use
     *                     {@link org.rulii.script.ScriptOptions#DEFAULT}.
     */
    public GraalJsScriptProcessor(GraalJSScriptEngine scriptEngine, String languageName, String bindingsName) {
        super(scriptEngine, languageName, bindingsName);
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
            Context polyglotContext = ((GraalJSScriptEngine) getScriptEngine()).getPolyglotContext(scriptContext);
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
