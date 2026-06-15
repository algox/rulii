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
package org.rulii.ruleflow;

import org.rulii.bind.NamedScope;
import org.rulii.context.RuleContext;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.UnrulyException;
import org.rulii.ruleflow.command.RuleFlowCommand;

/**
 * Sequential {@link RuleFlowExecutionStrategy} — iterates the pipeline commands one by one.
 *
 * <p>Execution order:
 * <ol>
 *   <li>{@code checkInputParameters()} — throws if invalid; no scope is created and no tracer
 *       events fire in this case.</li>
 *   <li>Flow scope is pushed.</li>
 *   <li>{@code fireOnRuleFlowStart} — listeners are notified.</li>
 *   <li>Commands are iterated; {@code fireOnRuleFlowCommandExecuted} fires after each one
 *       that completes normally.</li>
 *   <li>Result is extracted; {@code fireOnRuleFlowResult} fires if an extractor was set.</li>
 *   <li>{@code runFinalizer()} fires {@code fireOnRuleFlowFinalizer} — always runs when
 *       command iteration started.</li>
 *   <li>On early exit: {@code fireOnRuleFlowEarlyExit} fires, then {@link RuleFlowReturn}
 *       re-propagates and is caught by {@link RulingOrder#run}.</li>
 *   <li>On unhandled error: {@code fireOnRuleFlowError} fires, then the exception re-propagates.</li>
 *   <li>Flow scope is removed; {@code fireOnRuleFlowEnd} fires — always.</li>
 * </ol>
 *
 * @param <T> the result type.
 *
 * @author Max Arulananthan
 * @since 2.0
 */
class DefaultRuleFlowExecutionStrategy<T> extends RuleFlowExecutionStrategyTemplate<T> {

    DefaultRuleFlowExecutionStrategy() {
        super();
    }

    @Override
    @SuppressWarnings("unchecked")
    public T run(RuleFlow<?> ruleFlow, RuleContext ruleContext) throws UnrulyException {
        Assert.notNull(ruleFlow, "ruleFlow cannot be null.");
        Assert.notNull(ruleContext, "ruleContext cannot be null.");
        Assert.isTrue(ruleFlow instanceof RulingOrder, "ruleFlow must be an instance of RulingOrder.");

        checkInputParameters(ruleFlow, ruleContext);

        RulingOrder<T> order = (RulingOrder<T>) ruleFlow;
        NamedScope ruleFlowScope = createFlowScope(ruleFlow, ruleContext);

        try {
            ruleContext.getTracer().fireOnRuleFlowStart(ruleFlow, ruleFlowScope);
            if (getLogger().isDebugEnabled()) getLogger().debug("RuleFlow [" + ruleFlow.getName() + "] execution started. Scope [" + ruleFlowScope.getName() + "] created.");
            return runCommands(order, ruleContext);
        } catch (RuleFlowReturn r) {
            ruleContext.getTracer().fireOnRuleFlowEarlyExit(ruleFlow, r.getResult());
            throw r;
        } catch (UnrulyException e) {
            ruleContext.getTracer().fireOnRuleFlowError(ruleFlow, e);
            throw e;
        } finally {
            removeFlowScope(ruleContext, ruleFlowScope);
            ruleContext.getTracer().fireOnRuleFlowEnd(ruleFlow, ruleFlowScope);
            if (getLogger().isDebugEnabled()) getLogger().debug("RuleFlow [" + ruleFlow.getName() + "] execution finished. Scope cleared.");
        }
    }

    private T runCommands(RulingOrder<T> order, RuleContext ruleContext) {
        RuleFlowExecutionContext ctx = new RuleFlowExecutionContext(ruleContext, order);
        if (order.getGlobalHandler() != null) ctx.setGlobalHandler(order.getGlobalHandler());

        try {
            for (RuleFlowCommand cmd : order.getCommands()) {
                cmd.execute(ctx);
                ruleContext.getTracer().fireOnRuleFlowCommandExecuted(order, cmd);
            }

            T result = order.extractResult(ruleContext);
            if (order.getResultExtractor() != null)
                ruleContext.getTracer().fireOnRuleFlowResult(order, order.getResultExtractor());
            return result;
        } finally {
            runFinalizer(order, ruleContext);
        }
    }
}
