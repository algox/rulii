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

import org.rulii.bind.Binding;
import org.rulii.bind.NamedScope;
import org.rulii.bind.PromiscuousBinder;
import org.rulii.bind.ReservedBindings;
import org.rulii.context.RuleContext;
import org.rulii.lib.apache.commons.logging.Log;
import org.rulii.lib.apache.commons.logging.LogFactory;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.UnrulyException;
import org.rulii.model.action.Action;
import org.rulii.model.condition.Condition;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Default implementation for <code>Rule</code>
 * Represents a rule that can be executed based on specified conditions and actions.
 *
 * This class implements the Rule interface and provides methods to define and execute a rule.
 * The rule can have a pre-condition, a condition to be evaluated, then actions to be executed
 * if the condition is met, and an optional otherwise action to be executed if the condition fails.
 *
 * When the rule is executed using the run method, the pre-condition is first checked. If the pre-condition is
 * satisfied, the specified condition is evaluated. If the condition holds true, then actions associated with
 * the rule are executed. If the condition fails, the otherwise action is executed.
 *
 * Detailed logging is performed during rule execution, and exceptions are thrown if any errors occur.
 *
 * @param <T> the type of target object the rule is defined for
 */
public class RulingClass<T> implements Rule {

    private static final Log logger = LogFactory.getLog(RulingClass.class);

    private final RuleDefinition ruleDefinition;
    private final Condition preCondition;
    private final Condition condition;
    private final List<Action> actions;
    private final Action otherwiseAction;
    private final T target;
    private final String description;

    /**
     * Rule defined with all the given properties.
     *
     * @param ruleDefinition meta information.
     * @param target target Rule class.
     * @param preCondition pre-condition.
     * @param condition given condition.
     * @param thenActions all the Then actions.
     * @param otherwiseAction the Otherwise action (optional);
     */
    RulingClass(RuleDefinition ruleDefinition, T target, Condition preCondition,
                Condition condition, List<Action> thenActions, Action otherwiseAction) {
        super();
        Assert.notNull(ruleDefinition, "ruleDefinition cannot be null");
        this.ruleDefinition = ruleDefinition;
        this.target = target;
        this.preCondition = preCondition;
        this.condition = condition;
        // Then actions (optional)
        this.actions = thenActions != null ? Collections.unmodifiableList(thenActions) : Collections.emptyList();
        // Otherwise action (Optional)
        this.otherwiseAction = otherwiseAction;
        this.description = createDescription(ruleDefinition);
    }

    /**
     * Executes the rule and returns the result.
     *
     * @param ruleContext the rule context to execute the rule in
     * @return the result of the rule execution
     * @throws UnrulyException if an error occurs during rule execution
     */
    @Override
    public final RuleResult run(RuleContext ruleContext) throws UnrulyException {
        Assert.notNull(ruleContext, "context cannot be null");

        // Rule Start Event
        NamedScope ruleScope = createRuleScope(ruleContext);
        // Notify Rule start
        ruleContext.getTracer().fireOnRuleStart(this, ruleScope);
        if (logger.isDebugEnabled()) logger.debug("Rule [" + getName() + "] Execution. Scope [" + ruleScope.getName() + "] created.");
        RuleResult result = null;

        try {
            boolean preConditionCheck = checkPreCondition(ruleContext);

            // We did not pass the Pre-Condition
            if (!preConditionCheck) {
                if (logger.isDebugEnabled()) logger.debug("Rule [" + getName() + "] pre-condition check failed. Rule is skipped.");
                result = new RuleResult(this, RuleExecutionStatus.SKIPPED);
                return result;
            }

            // Check the given condition
            boolean conditionCheck = checkCondition(ruleContext);

            // The Condition passed
            if (conditionCheck) {
                if (logger.isDebugEnabled()) logger.debug("Rule [" + getName() + "] given condition passed. Actions to be executed.");
                runActions(ruleContext);
            } else {
                if (logger.isDebugEnabled()) logger.debug("Rule [" + getName() + "] given condition has failed.");
                runOtherwiseAction(ruleContext);
            }

            result = new RuleResult(this, conditionCheck ? RuleExecutionStatus.PASS : RuleExecutionStatus.FAIL);
            return result;
        } catch (Exception e) {
            logger.error("Rule [" + getName() + "] execution caused an error.", e);
            ruleContext.getTracer().fireOnRuleError(this, e);
            throw new UnrulyException("Error trying to run Rule [" + getName() + "]", e);
        } finally {
            ruleContext.getBindings().removeScope(ruleScope);
            if (logger.isDebugEnabled()) logger.debug("Rule [" + getName() + "] Executed. Scope [" + ruleScope.getName() + "] cleared.");
            // Notify Rule end
            ruleContext.getTracer().fireOnRuleEnd(this, result, ruleScope);
        }
    }

    /**
     * Creates a new rule scope for the given rule context. The rule scope is a container for variables and functions
     * that can be accessed within the context of the rule execution.
     *
     * @param ruleContext the rule context in which the rule scope is created (cannot be null)
     * @return the created rule scope
     * @throws UnrulyException if the current scope in the rule context is not a PromiscuousBinder
     */
    protected NamedScope createRuleScope(RuleContext ruleContext) {
        NamedScope result = ruleContext.getBindings().addScope(getRuleScopeName());

        if (!(result.getBindings() instanceof PromiscuousBinder bindings)) {
            throw new UnrulyException("IllegalState CurrentScope does not allow reserved keyword binding.");
        }

        bindings.promiscuousBind(Binding.builder().with(ReservedBindings.THIS.getName()).type(Rule.class).value(this).build());

        return result;
    }

    /**
     *
     * Returns the name of the rule scope. The rule scope name is generated by concatenating the name of the rule
     * with the string "-scope-" and a random UUID.
     *
     * @return the name of the rule scope
     */
    protected String getRuleScopeName() {
        return getName() + "-scope-" + UUID.randomUUID();
    }

    /**
     * Checks the pre-condition for the rule execution.
     *
     * @param ruleContext the rule context to execute the pre-condition in
     * @return {@code true} if the pre-condition is satisfied, {@code false} otherwise
     */
    protected boolean checkPreCondition(RuleContext ruleContext) {
        boolean result = true;

        if (getPreCondition() != null) {
            try {
                // Check the Pre-Condition
                result = getPreCondition().run(ruleContext);
            } catch (Exception e) {
                throw new UnrulyException("Rule(" + getName() + ") Pre-condition failed.", e);
            }
            // Notify the pre-condition check
            ruleContext.getTracer().fireOnRulePreConditionCheck(this, getPreCondition(), result);
        }

        return result;
    }

    /**
     * Checks the given condition.
     *
     * @param ruleContext the rule context in which the condition is checked
     * @return true if the condition is satisfied, false otherwise
     */
    protected boolean checkCondition(RuleContext ruleContext) {
        // Check the given condition
        boolean result = true;

        if (getCondition() != null) {
            try {
                result = getCondition().run(ruleContext);
            } catch (Exception e) {
                throw new UnrulyException("Rule(" + getName() + ") Condition failed.", e);
            }
            // Notify the Condition check
            ruleContext.getTracer().fireOnRuleConditionCheck(this, getCondition(), result);
        }

        return result;
    }

    /**
     * Executes the actions associated with the rule in the given rule context.
     *
     * @param ruleContext the rule context in which the actions are executed
     */
    protected void runActions(RuleContext ruleContext) {
        // Execute associated Actions.
        for (Action action : getActions()) {
            try {
                action.run(ruleContext);
            } catch (Exception e) {
                throw new UnrulyException("Rule(" + getName() + ") Action failed.", e);
            }
            if (logger.isDebugEnabled()) logger.debug("Rule [" + getName() + "] Action [" + action.getName() + "] executed.");
            // Notify the Action
            ruleContext.getTracer().fireOnRuleAction(this, action);
        }
    }

    /**
     * Executes the otherwise action associated with the rule context if it is not null.
     * If the otherwise action fails, an UnrulyException is thrown.
     *
     * @param ruleContext the rule context in which the action is executed
     */
    protected void runOtherwiseAction(RuleContext ruleContext) {
        // Execute otherwise Action.
        if (getOtherwiseAction() != null) {
            try {
                getOtherwiseAction().run(ruleContext);
            } catch (Exception e) {
                throw new UnrulyException("Rule(" + getName() + ") Otherwise Action failed.", e);
            }
            if (logger.isDebugEnabled()) logger.debug("Rule [" + getName() + "] Otherwise Action [" + getOtherwiseAction().getName() + "] executed.");
            // Notify the OtherwiseAction
            ruleContext.getTracer().fireOnRuleOtherwiseAction(this, getOtherwiseAction());
        }
    }

    @Override
    public Condition getPreCondition() {
        return preCondition;
    }

    @Override
    public Condition getCondition() {
        return condition;
    }

    @Override
    public RuleDefinition getDefinition() {
        return ruleDefinition;
    }

    @Override
    public String getName() {
        return ruleDefinition.getName();
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public T getTarget() {
        return target;
    }

    @Override
    public List<Action> getActions() {
        return actions;
    }

    @Override
    public Action getOtherwiseAction() {
        return otherwiseAction;
    }

    @Override
    public String toString() {
        return getDescription();
    }

    /**
     * Creates the description for a Rule based on the provided RuleDefinition.
     *
     * @param ruleDefinition the RuleDefinition object containing information about the Rule
     * @return the description of the Rule, if available; otherwise, a default description based on the RuleDefinition
     */
    protected static String createDescription(RuleDefinition ruleDefinition) {
        if (ruleDefinition.getDescription() != null) return ruleDefinition.getDescription();

        StringBuilder result = new StringBuilder("rule(name = " + ruleDefinition.getName());
        if (!ruleDefinition.isInline())
            result.append(", class = ").append(ruleDefinition.getRuleClass().getSimpleName());
        result.append(")");
        return result.toString();
    }
}
