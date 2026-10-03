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
import org.rulii.lib.spring.util.Assert;
import org.rulii.script.Script;
import org.rulii.script.ScriptCompiler;
import org.rulii.script.jsr223.JSR223ScriptCompiler;

/**
 * {@link ScriptCompiler} for GraalJS that does not leak the compile-time polyglot context.
 *
 * <p>{@link GraalJSScriptEngine#compile(String)} syntax-checks the source in the engine's own
 * default polyglot {@code Context}, which is created on first use and reclaimed only by an
 * explicit {@link GraalJSScriptEngine#close()}. Because {@link GraalJsScriptProcessorFactory}
 * hands out a fresh engine per compiler, compiling without closing leaves one orphaned context
 * behind per compiled script. This compiler therefore creates an engine, delegates to
 * {@link JSR223ScriptCompiler} for the actual compilation and exception mapping, and closes the
 * engine again. The resulting {@link javax.script.CompiledScript} stays fully usable: evaluation
 * runs in the per-evaluation context supplied by the processor, never in the closed one.
 *
 * @author Max Arulananthan
 * @since 2.1
 * @see GraalJsScriptProcessorFactory
 * @see GraalJsScriptProcessor
 */
public class GraalJsScriptCompiler implements ScriptCompiler {

    private final String languageName;

    /**
     * Creates a compiler that reports the given language name.
     *
     * @param languageName the language name compiled scripts carry; must not be null or empty.
     */
    public GraalJsScriptCompiler(String languageName) {
        super();
        Assert.hasText(languageName, "languageName cannot be empty.");
        this.languageName = languageName;
    }

    @Override
    public String getLanguageName() {
        return languageName;
    }

    @Override
    public <T> Script<T> compile(String script, Class<?> returnType) {
        GraalJSScriptEngine engine = GraalJsScriptProcessorFactory.createEngine();

        try {
            return new JSR223ScriptCompiler(languageName, engine).compile(script, returnType);
        } finally {
            engine.close();
        }
    }
}
