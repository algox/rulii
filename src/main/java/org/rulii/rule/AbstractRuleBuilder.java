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

import org.rulii.lib.apache.commons.logging.Log;
import org.rulii.lib.apache.commons.logging.LogFactory;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.MethodDefinition;
import org.rulii.model.SourceDefinition;
import org.rulii.model.action.Action;
import org.rulii.model.condition.Condition;
import org.rulii.model.condition.Conditions;
import org.rulii.util.RuleUtils;
import org.rulii.util.RunnableComparator;

import java.util.ArrayList;
import java.util.List;

/**
 * AbstractRuleBuilder is an abstract class that provides a fluent interface for building rule definitions.
 * It allows the creation of rules with a name, description, order, pre-condition, main condition, actions, and an otherwise action.
 *
 * @param <T> the type of the rule target
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public abstract class AbstractRuleBuilder<T> {

    private static final Log logger = LogFactory.getLog(AbstractRuleBuilder.class);

    private final boolean inline;
    private Class<T> ruleClass;
    private String name;
    private String description;
    private Condition preCondition = null;
    private Condition condition;
    private Action otherwiseAction;
    private T target;
    private final List<Action> thenActions = new ArrayList<>();

    /**
     * Initializes a new AbstractRuleBuilder object with the specified inline flag.
     *
     * @param inline indicates whether the rule is inline or not.
     */
    protected AbstractRuleBuilder(boolean inline) {
        super();
        this.inline = inline;
    }

    /**
     * Sets the rule class for the current rule being built.
     *
     * @param ruleClass the class representing the rule.
     * @return the AbstractRuleBuilder instance for method chaining.
     */
    protected AbstractRuleBuilder<T> ruleClass(Class<T> ruleClass) {
        Assert.notNull(ruleClass, "ruleClass cannot be null.");
        this.ruleClass = ruleClass;
        return this;
    }

    /**
     * Name of the Rule.
     *
     * @param name name of the Rule.
     * @return this for fluency.
     */
    public AbstractRuleBuilder<T> name(String name) {
        Assert.isTrue(RuleUtils.isValidName(name), "Rule name [" + name + "] not valid. It must conform to ["
                + RuleUtils.NAME_REGEX + "]");
        this.name = name;
        return this;
    }

    /**
     * Rule description.
     *
     * @param description Rule description.
     * @return this for fluency.
     */
    public AbstractRuleBuilder<T> description(String description) {
        this.description = description;
        return this;
    }

    /**
     * Sets the pre-condition for the rule being built.
     *
     * @param preCondition the pre-condition to be set.
     * @return the AbstractRuleBuilder instance for method chaining.
     */
    public AbstractRuleBuilder<T> preCondition(Condition preCondition) {
        this.preCondition = preCondition;
        return this;
    }

    /**
     * Sets the given condition for the rule being built.
     *
     * @param condition the Condition to set for the rule.
     * @return the AbstractRuleBuilder instance for method chaining.
     */
    public AbstractRuleBuilder<T> given(Condition condition) {
        Assert.notNull(condition, "condition cannot be null.");
        this.condition = condition;
        return this;
    }

    /**
     * Appends the given action to the list of actions to be executed after the current rule.
     *
     * @param action the action to be executed after the rule.
     * @return the AbstractRuleBuilder instance for method chaining.
     */
    public AbstractRuleBuilder<T> then(Action action) {
        Assert.notNull(action, "action cannot be null.");
        this.thenActions.add(action);
        return this;
    }

    /**
     * Sets the action to be executed if none of the conditions of the rule are met.
     *
     * @param action the Action to be executed if no conditions are met.
     * @return the AbstractRuleBuilder instance for method chaining.
     */
    public AbstractRuleBuilder<T> otherwise(Action action) {
        this.otherwiseAction = action;
        return this;
    }

    /**
     * Sets the target for the current rule builder.
     *
     * @param target the target for the rule
     * @return the AbstractRuleBuilder instance for method chaining
     */
    protected AbstractRuleBuilder<T> target(T target) {
        this.target = target;
        return this;
    }

    protected Class<T> getRuleClass() {
        return ruleClass;
    }

    protected String getName() {
        return name;
    }

    protected String getDescription() {
        return description;
    }

    protected Condition getPreCondition() {
        return preCondition;
    }

    protected Condition getCondition() {
        return condition;
    }

    protected List<Action> getThenActions() {
        return thenActions;
    }

    protected Action getOtherwiseAction() {
        return otherwiseAction;
    }

    protected T getTarget() {
        return target;
    }

    /**
     * Builds a RuleDefinition object based on the specified parameters and internal state of the AbstractRuleBuilder.
     * The RuleDefinition consists of information about a rule, including its name, description, order, source details,
     * pre-condition method information, condition method information, then actions method information, and otherwise action method information.
     *
     * @return a RuleDefinition object representing the built rule with all relevant information populated.
     */
    protected RuleDefinition buildRuleDefinition() {
        Assert.notNull(getName(), "Rule Name cannot be null");

        List<MethodDefinition> thenActionDefinitions = new ArrayList<>(thenActions.size());

        for (Action thenAction : thenActions) {
            thenActionDefinitions.add(thenAction.getDefinition());
        }

        return new RuleDefinition(getRuleClass(), inline, getName(), getDescription(),
                SourceDefinition.build(),
                getConditionDefinition(getPreCondition()),
                getConditionDefinition(getCondition()),
                thenActionDefinitions,
                getOtherwiseAction() != null ? getOtherwiseAction().getDefinition() : null);
    }

    /**
     * Validate method ensures that a valid Rule object must have a non-null Condition set. An Assertion error
     * will be thrown if the Condition field is null.
     */
    public void validate() {
        Assert.notNull(getCondition(), "Rule must have a Condition.");
    }

    /**
     * Builds a Rule object based on the specified parameters and internal state of the AbstractRuleBuilder.
     * It validates the Rule object to ensure it has a non-null Condition set.
     * If the target object implements RuleDefinitionAware, it sets the RuleDefinition.
     * Finally, it constructs and returns a new Rule object with the relevant information.
     *
     * @return a Rule object representing the built rule with all the necessary properties set.
     */
    public Rule build() {

        if (getCondition() == null) {
            logger.warn("Rule [" + getName() + "] does not have a Given condition. Defaulting to always return TRUE.");
            condition = Conditions.TRUE();
        }

        validate();
        RuleDefinition ruleDefinition = buildRuleDefinition();

        // Call back to set the RuleDefinition
        if (getTarget() instanceof RuleDefinitionAware) {
            ((RuleDefinitionAware) getTarget()).setRuleDefinition(ruleDefinition);
        }

        return new RulingClass<>(ruleDefinition, getTarget(), getPreCondition(), getCondition(),
                getThenActions(), getOtherwiseAction());
    }

    /**
     * Retrieves the MethodDefinition for the given Condition.
     *
     * @param condition the Condition object for which to retrieve the MethodDefinition
     * @return the MethodDefinition associated with the given Condition, or null if the Condition is null
     */
    private MethodDefinition getConditionDefinition(Condition condition) {
        if (condition == null) return null;
        return condition.getDefinition();
    }
}
