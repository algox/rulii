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

import org.rulii.bind.NamedScope;
import org.rulii.context.RuleContext;
import org.rulii.lib.spring.core.NestedExceptionUtils;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.UnrulyException;
import org.rulii.rule.Rule;
import org.rulii.rule.RuleResult;
import org.rulii.validation.ValidationException;

/**
 * Default rule set execution strategy for running a set of rules in a specified order.
 *
 * This class extends the RuleSetExecutionStrategyTemplate and provides the default behavior for executing rules.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class DefaultRuleSetExecutionStrategy<T> extends RuleSetExecutionStrategyTemplate<T> {

    public DefaultRuleSetExecutionStrategy() {
        super();
    }

    @SuppressWarnings("unchecked")
    @Override
    public T run(RuleSet<?> ruleSet, RuleContext ruleContext) throws UnrulyException {
        Assert.notNull(ruleContext, "context cannot be null");
        // Run the input validators first (if any)
        runInputValidators(ruleSet, ruleContext);
        // Continue to run the ruleset
        RuleSetExecutionStatus ruleSetStatus = new RuleSetExecutionStatus();
        // Create a new Scope for the RuleSet to use
        NamedScope ruleSetScope = createRuleSetScope(ruleSet, ruleContext, ruleSetStatus);
        ruleContext.getTracer().fireOnRuleSetStart(ruleSet, ruleSetScope);
        if (getLogger().isDebugEnabled()) getLogger().debug("RuleSet [" + ruleSet.getName() + "] Execution. Scope [" +  ruleSetScope.getName() + "] created.");

        try {
            // Run the PreCondition if there is one.
            boolean preConditionCheck = checkPreCondition(ruleSet, ruleContext);
            ruleSetStatus.setPreConditionCheck(preConditionCheck);
            // RuleSet did not pass the precondition; Do not execute the rules.
            if (!preConditionCheck) {
                if (getLogger().isDebugEnabled())
                    getLogger().debug("RuleSet [" + ruleSet.getName() + "] pre-condition check failed. RuleSet is skipped.");
                return (T) ruleSetStatus;
            }
            // Run the rules
            runRules(ruleSet, ruleContext, ruleSetStatus);
            return extractResult(ruleSet, ruleContext, ruleSetStatus);
        } catch (Exception e) {
            Throwable rootCause = NestedExceptionUtils.getRootCause(e);

            // Check if we got an expected ValidationException then rethrow it
            if (rootCause instanceof ValidationException) {
                throw (ValidationException) rootCause;
            } else {
                getLogger().error("RuleSet [" + ruleSet.getName() + "] execution caused an error.", e);
                ruleContext.getTracer().fireOnRuleSetError(ruleSet, ruleSetStatus, e);
                throw new UnrulyException("Error trying to run RuleSet [" + ruleSet.getName() + "]", e);
            }

        } finally {
            removeRuleSetScope(ruleContext, ruleSetScope);
            ruleContext.getTracer().fireOnRuleSetEnd(ruleSet, ruleSetScope, ruleSetStatus);
            if (getLogger().isDebugEnabled()) getLogger().debug("RuleSet [" + ruleSet.getName() + "] Executed. Scope cleared.");
        }
    }

    /**
     * Executes the rules in the RuleSet.
     *
     * @param ruleContext the RuleContext representing the current context (must not be null)
     * @param status      the RuleSetStatus to keep track of rule execution results (must not be null)
     */
    protected void runRules(RuleSet<?> ruleSet, RuleContext ruleContext, RuleSetExecutionStatus status) {
        // Run any PreAction if one is available.
        runInitializer(ruleSet, ruleContext);

        try {
            // Execute the rules/actions in order; STOP if the stopCondition is met.
            for (Rule rule : ruleSet.getRules()) {
                // Run the rule/action
                if (getLogger().isDebugEnabled()) getLogger().debug("RuleSet [" + ruleSet.getName() + "] running rule [" + rule.getName() + "]");
                RuleResult executionResult = rule.run(ruleContext);
                status.add(executionResult);
                // Fire Rule event
                ruleContext.getTracer().fireOnRuleSetRuleRun(ruleSet, rule, executionResult, status);
                // Check to see if we need to stop the execution?
                if (ruleSet.getStopCondition() != null && ruleSet.getStopCondition().run(ruleContext)) {
                    if (getLogger().isDebugEnabled()) getLogger().debug("Stopping RuleSet [" + ruleSet.getName() + "]. Stop condition met.");
                    // Fire Stop event
                    ruleContext.getTracer().fireOnRuleSetStop(ruleSet, ruleSet.getStopCondition(), status);
                    break;
                }
            }
        } finally {
            // Run the Finalizer after executing the Rules
            runFinalizer(ruleSet, ruleContext);
        }
    }
}
