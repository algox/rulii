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
package org.rulii.script.janino;

import org.codehaus.janino.ScriptEvaluator;
import org.rulii.script.AbstractScript;
import org.rulii.script.Script;

/**
 * Janino-backed {@link Script} that holds a lazily-compiled {@link ScriptEvaluator}.
 *
 * <p>Instances are created by {@link JaninoScriptCompiler#compile(String, Class)} and carry only
 * the raw source text until the first evaluation.  On the first call to
 * {@link JaninoScriptProcessor#evaluate evaluate}, {@link JaninoScriptCompiler} parses and
 * JIT-compiles the source against the live bindings and stores the resulting
 * {@link ScriptEvaluator} via {@link #setEvaluator}.  All subsequent evaluations reuse the
 * cached evaluator, avoiding repeated compilation.
 *
 * @param <T> the expected return type of the script.
 *
 * @author Max Arulananthan
 * @since 1.2
 * @see JaninoScriptCompiler
 * @see JaninoScriptProcessor
 */
public class JITScript<T> extends AbstractScript<T> {

    private volatile ScriptEvaluator evaluator;

    /**
     * Creates a new {@code JITScript} with no compiled evaluator.
     *
     * <p>The evaluator is set on the first evaluation via {@link #setEvaluator}.
     *
     * @param languageName the scripting language name; must not be null or empty.
     * @param script       the raw script source text; must not be null or empty.
     * @param returnType   the expected result return type.
     */
    public JITScript(String languageName, String script, Class<?> returnType) {
        super(languageName, script, returnType);
    }

    /**
     * Returns the compiled {@link ScriptEvaluator}, or {@code null} if the script has not yet
     * been compiled.
     *
     * @return the evaluator, or {@code null} on the first call before compilation.
     */
    public ScriptEvaluator getEvaluator() {
        return evaluator;
    }

    /**
     * Stores the compiled evaluator on the first invocation; subsequent calls are ignored so that
     * a compiled evaluator is never overwritten.
     *
     * @param evaluator the compiled evaluator to cache; should not be null.
     */
    public synchronized void setEvaluator(ScriptEvaluator evaluator) {
        if (this.evaluator != null) return;
        this.evaluator = evaluator;
    }

    @Override
    public String toString() {
        return "JITScript{" +
                "evaluator=" + evaluator +
                '}';
    }
}
