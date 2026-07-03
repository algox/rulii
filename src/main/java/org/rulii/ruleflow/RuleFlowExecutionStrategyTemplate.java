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

import org.rulii.bind.Binding;
import org.rulii.bind.NamedScope;
import org.rulii.bind.PromiscuousBinder;
import org.rulii.bind.ReservedBindings;
import org.rulii.context.RuleContext;
import org.rulii.lib.apache.commons.logging.Log;
import org.rulii.lib.apache.commons.logging.LogFactory;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.InputParameter;
import org.rulii.model.UnrulyException;

import java.util.UUID;

/**
 * Abstract base for {@link RuleFlowExecutionStrategy} implementations.
 *
 * <p>Provides shared utilities: input-parameter validation, scope lifecycle,
 * finalizer execution, and result extraction.
 *
 * @param <T> the result type.
 *
 * @author Max Arulananthan
 * @since 2.0
 */
public abstract class RuleFlowExecutionStrategyTemplate<T> implements RuleFlowExecutionStrategy<T> {

    private static final Log LOGGER = LogFactory.getLog(RuleFlowExecutionStrategyTemplate.class);

    protected RuleFlowExecutionStrategyTemplate() {
        super();
    }

    /**
     * Validates all declared input parameters against the current bindings.
     * Throws {@link UnrulyException} if a required parameter is missing or has the wrong type.
     *
     * @param ruleFlow the flow whose parameters to check; must not be null.
     * @param ruleContext the active context; must not be null.
     */
    protected void checkInputParameters(RuleFlow<?> ruleFlow, RuleContext ruleContext) {
        ruleFlow.getInputParameters()
                .forEach(p -> checkInputParameter(p, ruleFlow, ruleContext));
    }

    /**
     * Validates a single input parameter against the current bindings, throwing if a required
     * parameter is missing or has the wrong type, and applying the default value function when
     * an optional parameter's binding is absent or {@code null}.
     *
     * @param parameter the parameter to check; must not be null.
     * @param ruleFlow the flow whose parameter this is; must not be null.
     * @param ruleContext the active context; must not be null.
     */
    protected void checkInputParameter(InputParameter<?> parameter, RuleFlow<?> ruleFlow, RuleContext ruleContext) {
        Binding<?> binding = ruleContext.getBindings().getBinding(parameter.name());

        if (parameter.required() && (binding == null || (binding.getValue() != null && !binding.isAssignable(parameter.type()))))
            throw new UnrulyException("RuleFlow [" + ruleFlow.getName() + "] requires input parameter ["
                    + parameter.name() + "] type [" + parameter.type() + "]");

        if (parameter.defaultValue() != null && (binding == null || binding.getValue() == null))
            ruleContext.getBindings().bind(parameter.name(), parameter.defaultValue().apply(ruleContext));
    }

    /**
     * Creates and pushes a dedicated scope for the flow, binding {@code $ruleFlow} into it.
     *
     * @param ruleFlow the flow being executed.
     * @param ruleContext the active context.
     * @return the new scope.
     */
    protected NamedScope createFlowScope(RuleFlow<?> ruleFlow, RuleContext ruleContext) {
        Assert.notNull(ruleFlow, "ruleFlow cannot be null.");
        Assert.notNull(ruleContext, "ruleContext cannot be null.");

        NamedScope scope = ruleContext.getBindings().addScope(getFlowScopeName(ruleFlow));

        if (!(scope.getBindings() instanceof PromiscuousBinder bindings))
            throw new UnrulyException("IllegalState: CurrentScope does not allow reserved keyword binding.");

        bindings.promiscuousBind(Binding.builder().with(ReservedBindings.RULE_FLOW.getName())
                .type(RuleFlow.class)
                .value(ruleFlow)
                .build());

        return scope;
    }

    /**
     * Pops the flow scope.
     *
     * @param ruleContext the active context.
     */
    protected void removeFlowScope(RuleContext ruleContext, NamedScope scope) {
        Assert.notNull(ruleContext, "ruleContext cannot be null.");
        Assert.notNull(scope, "scope cannot be null.");
        ruleContext.getBindings().removeScope(scope);
    }

    /**
     * Runs the finalizer action (if any). Wraps any thrown exception in {@link UnrulyException}.
     *
     * @param ruleFlow the flow whose finalizer to run.
     * @param ruleContext the active context.
     */
    protected void runFinalizer(RuleFlow<?> ruleFlow, RuleContext ruleContext) {
        Assert.notNull(ruleFlow, "ruleFlow cannot be null.");
        Assert.notNull(ruleContext, "ruleContext cannot be null.");

        if (ruleFlow.getFinalizer() != null) {

            try {
                ruleFlow.getFinalizer().run(ruleContext);
            } catch (Exception e) {
                throw new UnrulyException("RuleFlow [" + ruleFlow.getName() + "] finalizer failed.", e);
            }

            if (LOGGER.isDebugEnabled()) LOGGER.debug("RuleFlow [" + ruleFlow.getName() + "] finalizer executed.");
            ruleContext.getTracer().fireOnRuleFlowFinalizer(ruleFlow, ruleFlow.getFinalizer());
        }
    }

    /**
     * Generates a unique scope name for the given flow's per-run flow scope.
     *
     * @param ruleFlow the flow being executed; must not be null.
     * @return a scope name unique to this run; never null.
     */
    protected String getFlowScopeName(RuleFlow<?> ruleFlow) {
        return ruleFlow.getName() + "-scope-" + UUID.randomUUID();
    }

    /**
     * Returns the shared logger for this strategy hierarchy.
     *
     * @return the logger; never null.
     */
    protected Log getLogger() {
        return LOGGER;
    }
}
