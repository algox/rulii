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

import java.util.concurrent.atomic.AtomicLong;

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

    private static final AtomicLong SCOPE_COUNTER = new AtomicLong();

    private static final Log LOGGER = LogFactory.getLog(RuleFlowExecutionStrategyTemplate.class);

    protected RuleFlowExecutionStrategyTemplate() {
        super();
    }

    /**
     * Validates all declared input parameters against the current bindings, before the flow's
     * scope is created. Throws {@link UnrulyException} if a required parameter is missing or has
     * the wrong type.
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
     * parameter is missing or has the wrong type. Default values are applied separately, by
     * {@link #applyDefaultValues}, once the flow's scope exists.
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
    }

    /**
     * Applies the default value of every optional input parameter whose binding is absent or
     * {@code null}, in declaration order, so a default can read the ones before it. Called with
     * the flow's scope current: a default is bound in that scope and goes with it, so it never
     * reaches the caller's bindings and is evaluated again on every run.
     *
     * @param ruleFlow the flow whose parameters to apply; must not be null.
     * @param ruleContext the active context; must not be null.
     * @since 2.3
     */
    protected void applyDefaultValues(RuleFlow<?> ruleFlow, RuleContext ruleContext) {
        ruleFlow.getInputParameters()
                .forEach(p -> applyDefaultValue(p, ruleFlow, ruleContext));
    }

    /**
     * Binds the default value of one input parameter in the current scope when the parameter
     * has a default and its binding is absent or {@code null}.
     *
     * @param parameter the parameter; must not be null.
     * @param ruleFlow the flow whose parameter this is; must not be null.
     * @param ruleContext the active context; must not be null.
     * @throws UnrulyException naming the flow and the parameter, with the failure as its cause,
     *         when the default value cannot be evaluated.
     * @since 2.3
     */
    protected void applyDefaultValue(InputParameter<?> parameter, RuleFlow<?> ruleFlow, RuleContext ruleContext) {
        if (parameter.defaultValue() == null) return;

        Binding<?> binding = ruleContext.getBindings().getBinding(parameter.name());

        if (binding == null || binding.getValue() == null) {
            Object value;

            try {
                value = parameter.defaultValue().apply(ruleContext);
            } catch (RuntimeException e) {
                throw new UnrulyException("RuleFlow [" + ruleFlow.getName() + "] could not evaluate the default value of input parameter ["
                        + parameter.name() + "] type [" + parameter.type() + "]", e);
            }

            ruleContext.getBindings().bind(parameter.name(), value);
        }
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
        // A counter, not a UUID: unique within the JVM, which is all a scope name has to be, at a fraction of the cost.
        return ruleFlow.getName() + "-scope-" + SCOPE_COUNTER.incrementAndGet();
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
