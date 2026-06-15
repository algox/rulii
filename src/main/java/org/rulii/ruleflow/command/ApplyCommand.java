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
package org.rulii.ruleflow.command;

import org.rulii.bind.Bindings;
import org.rulii.context.RuleContext;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.UnrulyException;
import org.rulii.model.function.Function;
import org.rulii.ruleflow.RuleFlowExecutionContext;

/**
 * Pipeline command that applies a {@link Function} and optionally binds the result
 * into the current scope.
 *
 * @author Max Arulananthan
 * @since 2.0
 */
public class ApplyCommand implements RuleFlowCommand {

    private final Function<?> function;
    private final String bindingName;
    private final String bindingScopeName;
    private final OnExceptionCommand stepHandler;

    public ApplyCommand(Function<?> function, String bindingName, String bindingScopeName,
                        OnExceptionCommand stepHandler) {
        super();
        Assert.notNull(function, "function cannot be null.");
        this.function = function;
        this.bindingName = bindingName;
        this.bindingScopeName = bindingScopeName;
        this.stepHandler = stepHandler;
    }

    public Function<?> getFunction() { return function; }

    @Override
    public void execute(RuleFlowExecutionContext ctx) {
        try {
            Object result = function.apply(ctx.getRuleContext());
            if (bindingName != null) bindResult(ctx.getRuleContext(), result);
        } catch (UnrulyException e) {
            if (!handleException(e, ctx)) throw e;
        }
    }

    private void bindResult(RuleContext ruleContext, Object result) {
        Bindings bindings = bindingScopeName != null
                ? ruleContext.getBindings().getScopeBindings(bindingName)
                : ruleContext.getBindings();
        bindings.bind(bindingName, result);
    }

    private boolean handleException(UnrulyException e, RuleFlowExecutionContext ctx) {

        if (stepHandler != null && stepHandler.canHandle(e)) {
            stepHandler.handleException(e, ctx);
            return true;
        }

        if (ctx.getGlobalHandler() != null && ctx.getGlobalHandler().canHandle(e)) {
            ctx.getGlobalHandler().handleException(e, ctx);
            return true;
        }

        return false;
    }
}
