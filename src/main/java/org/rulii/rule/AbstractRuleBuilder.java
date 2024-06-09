/*
 * This software is licensed under the Apache 2 license, quoted below.
 *
 * Copyright (c) 1999-2024, Algorithmx Inc.
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

import org.rulii.lang.action.Action;
import org.rulii.lang.condition.Condition;
import org.rulii.lang.condition.Conditions;
import org.rulii.model.MethodDefinition;
import org.rulii.model.SourceDefinition;
import org.rulii.lib.spring.core.Ordered;
import org.rulii.lib.spring.util.Assert;
import org.rulii.util.RuleUtils;
import org.rulii.util.RunnableComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractRuleBuilder<T> {

    private final boolean inline;
    private Class<T> ruleClass;
    private String name;
    private String description;
    private int order = Ordered.LOWEST_PRECEDENCE;
    private Condition preCondition = null;
    private Condition condition = Conditions.TRUE();
    private Action otherwiseAction;
    private T target;
    private final List<Action> thenActions = new ArrayList<>();
    private RuleDefinition ruleDefinition = null;

    protected AbstractRuleBuilder(boolean inline) {
        super();
        this.inline = inline;
    }

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

    public AbstractRuleBuilder<T> order(int order) {
        this.order = order;
        return this;
    }

    public AbstractRuleBuilder<T> pre(Condition preCondition) {
        this.preCondition = preCondition;
        return this;
    }

    public AbstractRuleBuilder<T> given(Condition condition) {
        Assert.notNull(condition, "condition cannot be null.");
        this.condition = condition;
        return this;
    }

    public AbstractRuleBuilder<T> then(Action action) {
        Assert.notNull(action, "action cannot be null.");
        this.thenActions.add(action);
        return this;
    }

    public AbstractRuleBuilder<T> otherwise(Action action) {
        this.otherwiseAction = action;
        return this;
    }

    protected AbstractRuleBuilder<T> target(T target) {
        this.target = target;
        return this;
    }

    protected RuleDefinition getRuleDefinition() {
        return ruleDefinition;
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

    protected Integer getOrder() {
        return order;
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

    public RuleDefinition buildRuleDefinition() {
        Assert.notNull(getName(), "Rule Name cannot be null");

        // Sort Then Action per Order
        Collections.sort(thenActions, new RunnableComparator());

        List<MethodDefinition> thenActionDefinitions = new ArrayList(thenActions.size());

        for (Action thenAction : thenActions) {
            thenActionDefinitions.add(thenAction.getDefinition());
        }

        return new RuleDefinition(getRuleClass(), inline, getName(), getDescription(), getOrder(),
                SourceDefinition.build(),
                getConditionDefinition(getPreCondition()),
                getConditionDefinition(getCondition()),
                thenActionDefinitions,
                getOtherwiseAction() != null ? getOtherwiseAction().getDefinition() : null);
    }

    public void validate() {
        Assert.notNull(getCondition(), "Rule must have a Condition.");
    }

    public Rule build() {
        validate();
        RuleDefinition ruleDefinition = buildRuleDefinition();

        // Call back to set the RuleDefinition
        if (getTarget() instanceof RuleDefinitionAware) {
            ((RuleDefinitionAware) getTarget()).setRuleDefinition(ruleDefinition);
        }

        return new RulingClass(ruleDefinition, getTarget(), getPreCondition(), getCondition(),
                getThenActions(), getOtherwiseAction());
    }

    private MethodDefinition getConditionDefinition(Condition condition) {
        if (condition == null) return null;
        return condition.getDefinition();
    }
}
