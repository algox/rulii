/*
 * This software is licensed under the Apache 2 license, quoted below.
 *
 * Copyright (c) 1999-2025, Algorithmx Inc.
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
package org.rulii.ruleset;

import org.rulii.bind.Binding;
import org.rulii.bind.NamedScope;
import org.rulii.bind.PromiscuousBinder;
import org.rulii.bind.ReservedBindings;
import org.rulii.context.RuleContext;
import org.rulii.lib.apache.commons.logging.Log;
import org.rulii.lib.apache.commons.logging.LogFactory;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.UnrulyException;
import org.rulii.rule.Rule;
import org.rulii.validation.RuleViolations;
import org.rulii.validation.ValidationException;

import java.util.UUID;

/**
 * An abstract class representing a template for executing a rule set.
 *
 * @param <T> the type of the result generated by executing the rule set.
 *
 * @author Max Arulananthan
 * @since 1.0
 *
 */
public abstract class RuleSetExecutionStrategyTemplate<T> implements RuleSetExecutionStrategy<T> {

    private static final Log logger = LogFactory.getLog(RuleSetExecutionStrategyTemplate.class);

    protected RuleSetExecutionStrategyTemplate() {
        super();
    }

    /**
     * Runs input validators for the given rule set and rule context.
     *
     * @param ruleSet the rule set to validate (must not be null)
     * @param ruleContext the rule context representing the current context (must not be null)
     * @throws ValidationException if input validation fails
     */
    protected void runInputValidators(RuleSet<?> ruleSet, RuleContext ruleContext) throws ValidationException {
        Assert.notNull(ruleSet, "ruleSet cannot be null.");
        Assert.notNull(ruleContext, "ruleContext cannot be null.");

        if (ruleSet.getInputValidators() == null || ruleSet.getInputValidators().isEmpty()) return;

        NamedScope scope = ruleContext.getBindings().addScope();
        RuleViolations violations = new RuleViolations();

        try {
            ruleContext.getBindings().bind("ruleViolations", violations);

            for (Rule inputValidator : ruleSet.getInputValidationRules()) {
                inputValidator.run(ruleContext);
            }

        } catch (Exception e) {
            throw new ValidationException("RuleSet [" + ruleSet.getName() + "] input validation failed.", e, violations);
        } finally {
            ruleContext.getBindings().removeScope(scope);
        }

        ruleContext.getTracer().fireOnRuleSetInputCheck(ruleSet, violations);

        if (violations.hasSevereErrors()) {
            throw new ValidationException("Input validation failed for RuleSet [" + ruleSet.getName() + "]", violations);
        }
    }

    /**
     * Creates a NamedScope for the RuleSet.
     * The NamedScope is created based on the given RuleContext and RuleSetResult.
     * This method adds the RuleSet scope to the bindings of the RuleContext.
     * It also binds reserved keywords THIS and RULE_SET_RESULT to their corresponding values.
     *
     * @param ruleSet       ruleSet to be executed.
     * @param ruleContext   the RuleContext representing the current context (must not be null)
     * @param ruleResultSet the RuleSetResult representing the result of the RuleSet (must not be null)
     * @return the created NamedScope
     * @throws UnrulyException if the current scope does not allow reserved keyword binding
     */
    protected NamedScope createRuleSetScope(RuleSet<?> ruleSet, RuleContext ruleContext, RuleSetExecutionStatus ruleResultSet) {
        Assert.notNull(ruleSet, "ruleSet cannot be null.");
        Assert.notNull(ruleContext, "ruleContext cannot be null.");
        Assert.notNull(ruleResultSet, "ruleResultSet cannot be null.");

        NamedScope result = ruleContext.getBindings().addScope(getRuleSetScopeName(ruleSet));

        if (!(result.getBindings() instanceof PromiscuousBinder bindings)) {
            throw new UnrulyException("IllegalState CurrentScope does not allow reserved keyword binding.");
        }

        bindings.promiscuousBind(Binding.builder().with(ReservedBindings.RULE_SET.getName())
                .type(RuleSet.class)
                .value(ruleSet)
                .build());

        bindings.promiscuousBind(Binding.builder().with(ReservedBindings.RULE_SET_STATUS.getName())
                .type(RuleSetExecutionStatus.class)
                .value(ruleResultSet)
                .build());

        return result;
    }

    /**
     * Retrieves the name of the scope for the rule set.
     *
     * @param ruleSet ruleSet to be executed.
     * @return the scope name for the rule set
     */
    protected String getRuleSetScopeName(RuleSet<?> ruleSet) {
        Assert.notNull(ruleSet, "ruleSet cannot be null.");
        return ruleSet.getName() + "-scope-" + UUID.randomUUID();
    }

    /**
     * Checks the pre-condition for the RuleSet execution.
     * @param ruleSet       ruleSet to be executed.
     * @param ruleContext the rule context to execute the pre-condition in
     * @return {@code true} if the pre-condition is satisfied, {@code false} otherwise
     */
    protected boolean checkPreCondition(RuleSet<?> ruleSet, RuleContext ruleContext) {
        Assert.notNull(ruleSet, "ruleSet cannot be null.");
        Assert.notNull(ruleContext, "ruleContext cannot be null.");
        boolean result = true;

        if (ruleSet.getPreCondition() != null) {
            // Check the Pre-Condition
            if (getLogger().isDebugEnabled()) getLogger().debug("RuleSet [" + ruleSet.getName() + "] executing pre-condition check.");
            try {
                result = ruleSet.getPreCondition().run(ruleContext);
            } catch (Exception e) {
                throw new UnrulyException("RuleSet(" + ruleSet.getName() + ") Pre-condition failed.", e);
            }
            // Notify the pre-condition check
            ruleContext.getTracer().fireOnRuleSetPreConditionCheck(ruleSet, ruleSet.getPreCondition(), result);
        }

        return result;
    }

    /**
     * Runs the Initializer before executing the Rules.
     *
     * @param ruleSet     ruleSet to be executed.
     * @param ruleContext the RuleContext representing the current context (must not be null)
     */
    protected void runInitializer(RuleSet<?> ruleSet, RuleContext ruleContext) {
        Assert.notNull(ruleSet, "ruleSet cannot be null.");
        Assert.notNull(ruleContext, "ruleContext cannot be null.");

        // Run the PreAction before executing the Rules
        if (ruleSet.getInitializer() != null) {
            try {
                ruleSet.getInitializer().run(ruleContext);
            } catch (Exception e) {
                throw new UnrulyException("RuleSet(" + ruleSet.getName() + ") Initializer failed.", e);
            }
            if (getLogger().isDebugEnabled()) getLogger().debug("RuleSet [" + ruleSet.getName() + "] initializer [" + ruleSet.getInitializer().getName() + "] is executed.");
            ruleContext.getTracer().fireOnRuleSetInitializer(ruleSet, ruleSet.getInitializer());
        }
    }

    /**
     * Runs the Finalizer after executing the Rules.
     *
     * @param ruleSet     ruleSet to be executed.
     * @param ruleContext the RuleContext representing the current context (must not be null)
     */
    protected void runFinalizer(RuleSet<?> ruleSet, RuleContext ruleContext) {
        Assert.notNull(ruleSet, "ruleSet cannot be null.");
        Assert.notNull(ruleContext, "ruleContext cannot be null.");

        // Run the Finalizer after executing the Rules
        if (ruleSet.getFinalizer() != null) {
            try {
                ruleSet.getFinalizer().run(ruleContext);
            } catch (Exception e) {
                throw new UnrulyException("RuleSet(" + ruleSet.getName() + ") Finalizer failed.", e);
            }
            if (getLogger().isDebugEnabled()) getLogger().debug("RuleSet [" + ruleSet.getName() + "] finalizer [" + ruleSet.getFinalizer().getName() + "] is executed.");
            ruleContext.getTracer().fireOnRuleSetFinalizer(ruleSet, ruleSet.getFinalizer());
        }
    }

    /**
     * Extracts the result of executing the rules in the RuleSet.
     *
     * @param ruleSet       ruleSet to be executed.
     * @param ruleContext   the RuleContext representing the current context (must not be null)
     * @param ruleSetStatus the RuleSetStatus to keep track of rule execution results (must not be null)
     * @return the result of executing the rules, or null if no result extractor is set
     */
    @SuppressWarnings("unchecked")
    protected T extractResult(RuleSet<?> ruleSet, RuleContext ruleContext, RuleSetExecutionStatus ruleSetStatus) {
        Assert.notNull(ruleSet, "ruleSet cannot be null.");
        Assert.notNull(ruleContext, "ruleContext cannot be null.");
        T result = null;

        if (ruleSet.getResultExtractor() != null) {
            try {
                result = (T) ruleSet.getResultExtractor().apply(ruleContext.getBindings());
            } catch (Exception e) {
                throw new UnrulyException("RuleSet(" + ruleSet.getName() + ") ResultExtractor failed.", e);
            }
            if (getLogger().isDebugEnabled()) getLogger().debug("RuleSet [" + ruleSet.getName() + "] result [" + result + "]");
            ruleContext.getTracer().fireOnRuleSetResult(ruleSet, ruleSet.getResultExtractor(), ruleSetStatus);
        }

        return result;
    }

    /**
     * Removes the specified NamedScope from the RuleContext's bindings.
     *
     * @param ruleContext the RuleContext representing the current context (must not be null)
     * @param target  the NamedScope to be removed (must not be null)
     */
    protected void removeRuleSetScope(RuleContext ruleContext, NamedScope target) {
        Assert.notNull(ruleContext, "ruleContext cannot be null.");
        ruleContext.getBindings().removeScope(target);
    }

    protected Log getLogger() {
        return logger;
    }
}
