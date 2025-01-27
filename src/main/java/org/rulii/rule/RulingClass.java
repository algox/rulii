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
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.UnrulyException;
import org.rulii.model.action.Action;
import org.rulii.model.condition.Condition;

import java.util.Collections;
import java.util.List;

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

    private final RuleDefinition ruleDefinition;
    private final Condition preCondition;
    private final Condition condition;
    private final List<Action> actions;
    private final Action otherwiseAction;
    private final T target;
    private final String description;

    private final RuleExecutionStrategy executionStrategy;

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
        this.executionStrategy = RuleExecutionStrategy.build();
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
        return executionStrategy.run(this, ruleContext);
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
