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
import org.rulii.context.RuleContext;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.UnrulyException;
import org.rulii.script.BuildScriptException;
import org.rulii.script.EvaluationException;
import org.rulii.script.Script;
import org.rulii.script.ScriptProcessor;

/**
 * {@link ScriptProcessor} implementation that evaluates {@link JITScript} instances by
 * JIT-compiling Java script fragments with the Janino compiler on the first call and reusing
 * the compiled bytecode on subsequent calls.
 *
 * <h2>Compilation and caching</h2>
 * <p>On the first {@link #evaluate} call for a given {@link JITScript}, this processor
 * delegates to {@link JaninoScriptCompiler} to parse, rewrite (via {@link ASTMutator}), and
 * compile the script against the live {@link org.rulii.bind.Bindings}.  The resulting
 * {@link ScriptEvaluator} is cached in the {@code JITScript} so that all subsequent
 * evaluations skip compilation entirely.
 *
 * <h2>Exception mapping</h2>
 * <ul>
 *   <li>{@link BuildScriptException} — propagated as-is when compilation fails.</li>
 *   <li>Any other {@link Exception} thrown during evaluation — wrapped in
 *       {@link EvaluationException}.</li>
 * </ul>
 *
 * @author Max Arulananthan
 * @since 1.2
 * @see JaninoScriptCompiler
 * @see JITScript
 * @see JaninoScriptProcessorFactory
 */
public class JaninoScriptProcessor implements ScriptProcessor {

    private final String languageName;
    private final String bindingsName;

    /**
     * Creates a new {@code JaninoScriptProcessor}.
     *
     * @param languageName the language identifier used to look up this processor in the
     *                     {@link org.rulii.script.ScriptProcessorManager} (e.g. {@code "java"});
     *                     must not be null or empty.
     * @param bindingsName the name of the bindings variable exposed inside scripts (e.g. {@code "ctx"});
     *                     must not be null or empty.
     */
    public JaninoScriptProcessor(String languageName, String bindingsName) {
        super();
        Assert.hasText(languageName, "languageName cannot be empty.");
        Assert.hasText(bindingsName, "bindingsName cannot be empty.");
        this.languageName = languageName;
        this.bindingsName = bindingsName;
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
    public String getBindingsName() {
        return bindingsName;
    }

    /**
     * {@inheritDoc}
     *
     * <p>On the first call the script is compiled via {@link JaninoScriptCompiler} and the
     * resulting {@link ScriptEvaluator} is cached inside the {@link JITScript}.  Subsequent
     * calls invoke the cached evaluator directly.
     *
     * @throws BuildScriptException if the script cannot be compiled (syntax error, unknown binding,
     *                              reserved variable name, etc.).
     * @throws EvaluationException  if the compiled script throws an exception at runtime.
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T evaluate(Script<T> script, RuleContext ruleContext) {
        JITScript<T> jitScript = (JITScript<T>) script;

        try {
            ScriptEvaluator evaluator = jitScript.getEvaluator();

            if (evaluator == null) {
                synchronized (jitScript) {
                    evaluator = jitScript.getEvaluator();

                    if (evaluator == null) {
                        JaninoScriptCompiler compiler = new JaninoScriptCompiler(languageName, bindingsName);
                        evaluator = compiler.compile(jitScript.getScript(), ruleContext.getBindings(), jitScript.getReturnType());
                        jitScript.setEvaluator(evaluator);
                    }
                }
            }

            return (T) evaluator.evaluate(new Object[] {ruleContext.getBindings()});
        } catch (UnrulyException e) {
            throw e;
        } catch (Exception e) {
            throw new EvaluationException(jitScript.getScript(), e.getMessage(), e);
        }
    }
}
