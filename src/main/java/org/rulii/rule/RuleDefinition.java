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

import org.rulii.model.Definition;
import org.rulii.model.MethodDefinition;
import org.rulii.model.SourceDefinition;
import org.rulii.lib.spring.util.Assert;
import org.rulii.util.RuleUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Indicates the class with this annotation is Rule and it will follow the "rules" of a being a Rule.
 *
 * The only requirement for a class to be considered a Rule is to have a "when" method (aka Condition in standard Rule terms).
 * The when method can take arbitrary number of arguments but must return a boolean value. The boolean is the result of
 * the conditionDefinition of the rule.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public final class RuleDefinition implements Definition, Comparable<RuleDefinition> {

    // Name of the Rule
    private String name;
    // Description of the Rule
    private String description;
    private final boolean inline;
    // Order of the Rule
    private final Integer order;
    private final SourceDefinition sourceDefinition;

    // Rule class
    private final Class<?> ruleClass;
    // PreCondition method details
    private final MethodDefinition preConditionDefinition;
    // Given method details
    private final MethodDefinition conditionDefinition;
    // Associated Then actions
    private final List<MethodDefinition> thenActionDefinitions;
    // Otherwise action
    private final MethodDefinition otherwiseActionDefinition;

    /**
     * Creates a RuleDefinition taking in all the required parameters.
     *
     * @param ruleClass Rule implementation class.
     * @param inline determines whether the rule was written inline (ie: lambda)
     * @param name Rule name.
     * @param description Rule description.
     * @param order rule order.
     * @param sourceDefinition source details.
     * @param preConditionDefinition Pre-Condition meta information.
     * @param conditionDefinition Given condition meta information.
     * @param thenActionDefinitions Then Action(s) meta information.
     * @param otherwiseActionDefinition Otherwise Action meta information.
     */
    public RuleDefinition(Class<?> ruleClass, boolean inline, String name, String description, int order,
                          SourceDefinition sourceDefinition,
                          MethodDefinition preConditionDefinition,
                          MethodDefinition conditionDefinition,
                          List<MethodDefinition> thenActionDefinitions,
                          MethodDefinition otherwiseActionDefinition) {
        super();
        Assert.notNull(ruleClass, "Rule class cannot be null.");
        setName(name);
        this.ruleClass = ruleClass;
        this.inline = inline;
        this.description = description;
        this.order = order;
        this.sourceDefinition = sourceDefinition;
        this.preConditionDefinition = preConditionDefinition;
        this.conditionDefinition = conditionDefinition;
        this.thenActionDefinitions = thenActionDefinitions != null ? Collections.unmodifiableList(thenActionDefinitions) : null;
        this.otherwiseActionDefinition = otherwiseActionDefinition;
    }

    /**
     * The implementing Rule class.
     *
     * @return Rule class.
     */
    public Class<?> getRuleClass() {
        return ruleClass;
    }

    public boolean isInline() {
        return inline;
    }

    /**
     * Name of the Rule.
     *
     * @return name of rule. If not specified the simple class name is used.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Rule description.
     *
     * @return description of what the rule does.
     */
    @Override
    public String getDescription() {
        return description;
    }

    void setName(String name) {
        Assert.isTrue(RuleUtils.isValidName(name), "Rule name must match ["
                + RuleUtils.NAME_REGEX + "] Given [" + name + "]");
        this.name = name;
    }

    public int getOrder() {
        return order;
    }

    void setDescription(String description) {
        this.description = description;
    }

    public MethodDefinition getPreConditionDefinition() {
        return preConditionDefinition;
    }

    /**
     * Condition details.
     *
     * @return meta information rule implementing method.
     */
    public MethodDefinition getConditionDefinition() {
        return conditionDefinition;
    }

    /**
     * Then Action details.
     *
     * @return meta information about the associated actions.
     */
    public List<MethodDefinition> getThenActionDefinitions() {
        return thenActionDefinitions;
    }

    /**
     * Otherwise Action details.
     *
     * @return meta information about the otherwise action.
     */
    public MethodDefinition getOtherwiseActionDefinition() {
        return otherwiseActionDefinition;
    }

    @Override
    public SourceDefinition getSource() {
        return sourceDefinition;
    }

    /**
     * Determines if the conditionDefinition is a statically implemented method call (such as a lambda).
     *
     * @return true if statically implemented; false otherwise.
     */
    public boolean isStatic() {
        return conditionDefinition.isStatic();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RuleDefinition that = (RuleDefinition) o;
        return ruleClass.equals(that.ruleClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ruleClass);
    }

    @Override
    public int compareTo(RuleDefinition other) {
        int result = order.compareTo(other.getOrder());
        return result != 0 ? result : getName().compareTo(other.getName());
    }

    @Override
    public String toString() {
        return "RuleDefinition{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", inline=" + inline +
                ", order=" + order +
                ", sourceDefinition=" + sourceDefinition +
                ", ruleClass=" + ruleClass +
                ", preConditionDefinition=" + preConditionDefinition +
                ", conditionDefinition=" + conditionDefinition +
                ", thenActionDefinitions=" + thenActionDefinitions +
                ", otherwiseActionDefinition=" + otherwiseActionDefinition +
                '}';
    }
}
