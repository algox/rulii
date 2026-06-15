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

import org.rulii.lib.spring.util.Assert;
import org.rulii.model.UnrulyException;
import org.rulii.model.action.Action;
import org.rulii.ruleflow.RuleFlowExecutionContext;

/**
 * Pipeline command that executes an {@link Action} and produces no result.
 *
 * <p>This is the void counterpart of {@link ApplyCommand}: use {@code apply()} when
 * the result needs to be bound into a scope; use {@code execute()} when side-effects
 * are the goal and no return value is needed.
 *
 * @author Max Arulananthan
 * @since 2.0
 */
public class ExecuteCommand implements RuleFlowCommand {

    private final Action action;
    private final OnExceptionCommand stepHandler;

    public ExecuteCommand(Action action, OnExceptionCommand stepHandler) {
        super();
        Assert.notNull(action, "action cannot be null.");
        this.action = action;
        this.stepHandler = stepHandler;
    }

    public Action getAction() { return action; }

    @Override
    public void execute(RuleFlowExecutionContext ctx) {
        try {
            action.run(ctx.getRuleContext());
        } catch (UnrulyException e) {
            if (!handleException(e, ctx)) throw e;
        }
    }

    private boolean handleException(UnrulyException e, RuleFlowExecutionContext ctx) {

        if (stepHandler != null && stepHandler.canHandle(e)) {
            stepHandler.handleException(e, ctx);
            ctx.getRuleContext().getTracer().fireOnRuleFlowExceptionHandled(ctx.getRuleFlow(), e, true);
            return true;
        }

        if (ctx.getGlobalHandler() != null && ctx.getGlobalHandler().canHandle(e)) {
            ctx.getGlobalHandler().handleException(e, ctx);
            ctx.getRuleContext().getTracer().fireOnRuleFlowExceptionHandled(ctx.getRuleFlow(), e, false);
            return true;
        }

        return false;
    }
}
