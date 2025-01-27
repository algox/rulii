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
package org.rulii.rule;

import org.rulii.bind.NamedScope;
import org.rulii.context.RuleContext;
import org.rulii.lib.apache.commons.logging.Log;
import org.rulii.lib.apache.commons.logging.LogFactory;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.UnrulyException;

/**
 * DefaultRuleExecutionStrategy is a concrete implementation of RuleExecutionStrategyTemplate.
 * It provides default logic for executing a rule and handling the different stages of rule processing.
 *
 * This class contains a method run(Rule rule, RuleContext ruleContext) which executes the rule and returns the result.
 * It handles creating rule scope, checking pre-condition, checking condition, executing actions, and handling errors during rule execution.
 * The result of the rule execution is returned as a RuleResult object.
 *
 * This class also logs events using a logger and notifies a Tracer object at various stages of rule execution.
 *
 * @author Max Arulananthan
 * @since 1.0
 *
 */
public class DefaultRuleExecutionStrategy extends RuleExecutionStrategyTemplate {

    private static final Log logger = LogFactory.getLog(DefaultRuleExecutionStrategy.class);

    public DefaultRuleExecutionStrategy() {
        super();
    }

    /**
     * Executes the rule and returns the result.
     *
     * @param rule rule to be executed.
     * @param ruleContext the rule context to execute the rule in
     * @return the result of the rule execution
     * @throws UnrulyException if an error occurs during rule execution
     */
    @Override
    public RuleResult run(Rule rule, RuleContext ruleContext) throws UnrulyException {
        Assert.notNull(ruleContext, "context cannot be null");

        // Rule Start Event
        NamedScope ruleScope = createRuleScope(rule, ruleContext);
        // Notify Rule start
        ruleContext.getTracer().fireOnRuleStart(rule, ruleScope);
        if (logger.isDebugEnabled()) logger.debug("Rule [" + rule.getName() + "] Execution. Scope [" + ruleScope.getName() + "] created.");
        RuleResult result = null;

        try {
            boolean preConditionCheck = checkPreCondition(rule, ruleContext);

            // We did not pass the Pre-Condition
            if (!preConditionCheck) {
                if (logger.isDebugEnabled()) logger.debug("Rule [" + rule.getName() + "] pre-condition check failed. Rule is skipped.");
                result = new RuleResult(rule, RuleExecutionStatus.SKIPPED);
                return result;
            }

            // Check the given condition
            boolean conditionCheck = checkCondition(rule, ruleContext);

            // The Condition passed
            if (conditionCheck) {
                if (logger.isDebugEnabled()) logger.debug("Rule [" + rule.getName() + "] given condition passed. Actions to be executed.");
                runActions(rule, ruleContext);
            } else {
                if (logger.isDebugEnabled()) logger.debug("Rule [" + rule.getName() + "] given condition has failed.");
                runOtherwiseAction(rule, ruleContext);
            }

            result = new RuleResult(rule, conditionCheck ? RuleExecutionStatus.PASS : RuleExecutionStatus.FAIL);
            return result;
        } catch (Exception e) {
            logger.error("Rule [" + rule.getName() + "] execution caused an error.", e);
            ruleContext.getTracer().fireOnRuleError(rule, e);
            throw new UnrulyException("Error trying to run Rule [" + rule.getName() + "]", e);
        } finally {
            ruleContext.getBindings().removeScope(ruleScope);
            if (logger.isDebugEnabled()) logger.debug("Rule [" + rule.getName() + "] Executed. Scope [" + ruleScope.getName() + "] cleared.");
            // Notify Rule end
            ruleContext.getTracer().fireOnRuleEnd(rule, result, ruleScope);
        }
    }
}
