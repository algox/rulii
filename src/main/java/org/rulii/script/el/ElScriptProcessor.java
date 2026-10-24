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
package org.rulii.script.el;

import jakarta.el.ELException;
import org.rulii.context.RuleContext;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.UnrulyException;
import org.rulii.script.EvaluationException;
import org.rulii.script.Script;
import org.rulii.script.ScriptProcessor;

/**
 * Evaluates a compiled {@link ElScript} against the bindings of a {@link RuleContext}: an EL context is
 * built per evaluation ({@link BindingsELContext}: the bindings first, then the standard resolvers), so the
 * bindings are reachable through the root object ({@code ctx.age}) and as bare identifiers
 * ({@code age}). Stateless, so one processor serves any number of threads.
 *
 * @author Max Arulananthan
 * @since 2.2
 */
public class ElScriptProcessor implements ScriptProcessor {

    private final String languageName;
    private final String bindingsName;

    public ElScriptProcessor(String languageName, String bindingsName) {
        super();
        Assert.hasText(languageName, "languageName cannot be empty.");
        Assert.hasText(bindingsName, "bindingsName cannot be empty.");
        this.languageName = languageName;
        this.bindingsName = bindingsName;
    }

    @Override
    public String getLanguageName() {
        return languageName;
    }

    @Override
    public String getBindingsName() {
        return bindingsName;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T evaluate(Script<T> script, RuleContext ruleContext) {
        Assert.notNull(script, "script cannot be null.");
        Assert.notNull(ruleContext, "ruleContext cannot be null.");
        Assert.isTrue(script instanceof ElScript, "script [" + script + "] was not compiled by the EL compiler.");

        BindingsELContext context = new BindingsELContext(ElSupport.expressionFactory(), ruleContext.getBindings(), bindingsName);

        try {
            return (T) ((ElScript<T>) script).getExpression().getValue(context);
        } catch (UnrulyException e) {
            throw e;
        } catch (ELException e) {
            throw new EvaluationException(script.getScript(), "cannot evaluate EL expression [" + script.getScript() + "]: " + e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        return "ElScriptProcessor{languageName='" + languageName + "', bindingsName='" + bindingsName + "'}";
    }
}
