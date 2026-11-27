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
 *   <li>Flow scope is pushed; {@code applyDefaultValues()} binds the defaults of absent optional
 *       parameters in it. A failing default removes the scope again and propagates, with no
 *       tracer events.</li>
 *   <li>{@code fireOnRuleFlowStart} — listeners are notified.</li>
 *   <li>Commands are iterated; {@code fireOnRuleFlowCommandExecuted} fires after each one
 *       that completes normally. A command whose failure the flow-level handler matches fires
 *       {@code fireOnRuleFlowExceptionHandled} instead once the handler body completes, and the
 *       iteration continues with the next top-level command.</li>
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
            applyDefaultValues(ruleFlow, ruleContext);
        } catch (Throwable e) {
            // A failing default is an input failure: no events, and no scope left behind.
            removeFlowScope(ruleContext, ruleFlowScope);
            throw e;
        }

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
            // fireOnRuleFlowEnd is documented to always fire - nest removeFlowScope's own finally
            // so a scope-removal failure can't prevent the fire call from running.
            try {
                removeFlowScope(ruleContext, ruleFlowScope);
            } finally {
                ruleContext.getTracer().fireOnRuleFlowEnd(ruleFlow, ruleFlowScope);
                if (getLogger().isDebugEnabled()) getLogger().debug("RuleFlow [" + ruleFlow.getName() + "] execution finished. Scope cleared.");
            }
        }
    }

    /**
     * Runs one top-level command. A failure that nothing inside the command handled goes to the
     * flow-level handler; once the handler body completes the flow carries on with the next
     * top-level command. An early exit, a failure the handler does not match and a failure of the
     * handler body itself all propagate.
     */
    private void runCommand(RuleFlowCommand cmd, RulingOrder<T> order, RuleFlowExecutionContext ctx) {
        try {
            cmd.execute(ctx);
        } catch (RuleFlowReturn r) {
            // An early exit is an UnrulyException too; it is never a failure to handle.
            throw r;
        } catch (UnrulyException e) {
            RuleFlowExceptionHandler globalHandler = order.getGlobalHandler();
            if (globalHandler == null || !globalHandler.canHandle(e)) throw e;

            globalHandler.handleException(e, ctx);
            ctx.getRuleContext().getTracer().fireOnRuleFlowExceptionHandled(order, e, false);
            return;
        }

        ctx.getRuleContext().getTracer().fireOnRuleFlowCommandExecuted(order, cmd);
    }

    private T runCommands(RulingOrder<T> order, RuleContext ruleContext) {
        RuleFlowExecutionContext ctx = new RuleFlowExecutionContext(ruleContext, order);
        Throwable pending = null;

        try {
            for (RuleFlowCommand cmd : order.getCommands()) {
                runCommand(cmd, order, ctx);
            }

            T result = order.extractResult(ruleContext);
            if (order.getResultExtractor() != null) ruleContext.getTracer().fireOnRuleFlowResult(order, order.getResultExtractor());
            return result;
        } catch (RuleFlowReturn r) {
            pending = r;
            throw r;
        } catch (UnrulyException e) {
            pending = e;
            throw e;
        } finally {
            // If an exception/early-exit is already propagating, a finalizer failure must not
            // silently replace it (ordinary try/finally semantics would otherwise discard the
            // original). UnrulyException disables exception suppression (see its constructors),
            // so addSuppressed() would be a silent no-op here - log instead, so the finalizer
            // failure is at least visible, and let the original keep propagating unchanged.
            try {
                runFinalizer(order, ruleContext);
            } catch (UnrulyException finalizerFailure) {
                if (pending != null) {
                    getLogger().error("RuleFlow [" + order.getName() + "] finalizer failed while ["
                            + pending + "] was already propagating.", finalizerFailure);
                } else {
                    throw finalizerFailure;
                }
            }
        }
    }
}
