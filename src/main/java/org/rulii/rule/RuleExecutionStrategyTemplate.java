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

import org.rulii.context.RuleContext;
import org.rulii.lib.apache.commons.logging.Log;
import org.rulii.lib.apache.commons.logging.LogFactory;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.UnrulyException;
import org.rulii.model.action.Action;

/**
 * RuleExecutionStrategyTemplate is an abstract class that serves as a template for implementing rule execution strategies.
 * It provides methods for checking pre-conditions, conditions, running actions, and running otherwise actions associated with a rule.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public abstract class RuleExecutionStrategyTemplate implements RuleExecutionStrategy {

    private static final Log logger = LogFactory.getLog(RuleExecutionStrategyTemplate.class);

    /**
     * Checks the pre-condition for the rule execution.
     *
     * @param rule the rule that is being executed.
     * @param ruleContext the rule context to execute the pre-condition in
     * @return {@code true} if the pre-condition is satisfied, {@code false} otherwise
     */
    protected boolean checkPreCondition(Rule rule, RuleContext ruleContext) {
        Assert.notNull(rule, "rule cannot be null.");
        Assert.notNull(ruleContext, "ruleContext cannot be null.");

        boolean result = true;

        if (rule.getPreCondition() != null) {
            try {
                // Check the Pre-Condition
                result = rule.getPreCondition().run(ruleContext);
                if (logger.isDebugEnabled()) logger.debug("Rule [" + rule.getName() + "] pre-condition passed.");
            } catch (Exception e) {
                throw new UnrulyException("Rule(" + rule.getName() + ") Pre-condition failed.", e);
            }
            // Notify the pre-condition check
            ruleContext.getTracer().fireOnRulePreConditionCheck(rule, rule.getPreCondition(), result);
        }

        return result;
    }

    /**
     * Checks the given condition.
     *
     * @param rule the rule that is being executed.
     * @param ruleContext the rule context in which the condition is checked
     * @return true if the condition is satisfied, false otherwise
     */
    protected boolean checkCondition(Rule rule, RuleContext ruleContext) {
        Assert.notNull(rule, "rule cannot be null.");
        Assert.notNull(ruleContext, "ruleContext cannot be null.");

        // Check the given condition
        boolean result = true;

        if (rule.getCondition() != null) {
            try {
                result = rule.getCondition().run(ruleContext);
            } catch (Exception e) {
                throw new UnrulyException("Rule(" + rule.getName() + ") Condition failed.", e);
            }
            // Notify the Condition check
            ruleContext.getTracer().fireOnRuleConditionCheck(rule, rule.getCondition(), result);
        }

        return result;
    }

    /**
     * Executes the actions associated with the rule in the given rule context.
     *
     * @param rule the rule that is being executed.
     * @param ruleContext the rule context in which the actions are executed
     */
    protected void runActions(Rule rule, RuleContext ruleContext) {
        Assert.notNull(rule, "rule cannot be null.");
        Assert.notNull(ruleContext, "ruleContext cannot be null.");

        // Execute associated Actions.
        for (Action action : rule.getActions()) {
            try {
                action.run(ruleContext);
            } catch (Exception e) {
                throw new UnrulyException("Rule(" + rule.getName() + ") Action failed.", e);
            }
            if (logger.isDebugEnabled()) logger.debug("Rule [" + rule.getName() + "] Action [" + action.getName() + "] executed.");
            // Notify the Action
            ruleContext.getTracer().fireOnRuleAction(rule, action);
        }
    }

    /**
     * Executes the otherwise action associated with the rule context if it is not null.
     * If the otherwise action fails, an UnrulyException is thrown.
     *
     * @param rule the rule that is being executed.
     * @param ruleContext the rule context in which the action is executed
     */
    protected void runOtherwiseAction(Rule rule, RuleContext ruleContext) {
        Assert.notNull(rule, "rule cannot be null.");
        Assert.notNull(ruleContext, "ruleContext cannot be null.");

        // Execute otherwise Action.
        if (rule.getOtherwiseAction() != null) {
            try {
                rule.getOtherwiseAction().run(ruleContext);
            } catch (Exception e) {
                throw new UnrulyException("Rule(" + rule.getName() + ") Otherwise Action failed.", e);
            }
            if (logger.isDebugEnabled()) logger.debug("Rule [" + rule.getName() + "] Otherwise Action [" + rule.getOtherwiseAction().getName() + "] executed.");
            // Notify the OtherwiseAction
            ruleContext.getTracer().fireOnRuleOtherwiseAction(rule, rule.getOtherwiseAction());
        }
    }
}
