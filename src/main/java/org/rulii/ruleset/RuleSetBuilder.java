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

import org.rulii.bind.ReservedBindings;
import org.rulii.context.RuleContext;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.SourceDefinition;
import org.rulii.model.action.Action;
import org.rulii.model.condition.Condition;
import org.rulii.model.function.Function;
import org.rulii.rule.Rule;
import org.rulii.rule.RuleDefinition;
import org.rulii.util.RuleUtils;

import java.util.*;

/**
 * A builder class to construct RuleSets with customizable preconditions, stop conditions, initializers,
 * finalizers, and result extractors.
 *
 * @param <T> the type of the result extracted by the RuleSet.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class RuleSetBuilder<T> {

    private String name;
    private String description = null;
    private Condition preCondition = null;
    private Condition stopCondition = null;
    private Action initializer = null;
    private Action finalizer = null;
    private Function<T> resultExtractor = (RuleContext ruleContext) -> ruleContext.getBindings().getValue(ReservedBindings.RULE_SET_STATUS.getName());
    private final LinkedList<Rule> ruleSetItems = new LinkedList<>();

    /**
     * Constructs a new RuleSetBuilder instance with the provided name.
     *
     * @param name the name of the RuleSetBuilder
     */
    RuleSetBuilder(String name) {
        super();
        name(name);
    }

    /**
     * Initializes a new RuleSetBuilder instance with the provided name and description.
     *
     * @param name        the name of the RuleSetBuilder
     * @param description the description of the RuleSetBuilder
     */
    RuleSetBuilder(String name, String description) {
        super();
        name(name);
        description(description);
    }

    /**
     * Constructs a RuleSetBuilder with the provided RuleSet, initializing the builder with the information from the given rules.
     *
     * @param rules the RuleSet containing rules to be used for initialization. Must not be null.
     */
    RuleSetBuilder(RuleSet<T> rules) {
        super();
        Assert.notNull(rules, "rules cannot be null.");
        name(rules.getName());
        description(rules.getDescription());
        if (rules.getPreCondition() != null) preCondition(rules.getPreCondition());
        if (rules.getStopCondition() != null) stopCondition(rules.getStopCondition());
        if (rules.getInitializer() != null) initializer(rules.getInitializer());
        if (rules.getFinalizer() != null) finalizer(rules.getFinalizer());
        resultExtractor(rules.getResultExtractor());
        this.rules(rules.getRules());
    }

    /**
     * Sets the name of the RuleSetBuilder.
     *
     * @param name the name to set for the RuleSetBuilder
     * @return this RuleSetBuilder instance for method chaining
     */
    public RuleSetBuilder<T> name(String name) {
        Assert.isTrue(RuleUtils.isValidName(name), "RuleSet name [" + name + "] not valid. It must conform to ["
                + RuleUtils.NAME_REGEX + "]");
        this.name = name;
        return this;
    }

    /**
     * Sets the description for the RuleSetBuilder.
     *
     * @param description the description to be set for the RuleSetBuilder
     * @return this RuleSetBuilder instance for method chaining
     */
    public RuleSetBuilder<T> description(String description) {
        this.description = description;
        return this;
    }

    /**
     * PreCondition(Optional) Condition to be met before the execution of the RuleSet.
     *
     * @param preCondition pre-check before execution of the RuleSet.
     * @return this for fluency.
     */
    public RuleSetBuilder<T> preCondition(Condition preCondition) {
        Assert.notNull(preCondition, "preCondition cannot be null.");
        this.preCondition = preCondition;
        return this;
    }

    /**
     * Condition that determines when execution should stop.
     *
     * @param condition stopping condition.
     * @return this for fluency.
     */
    public RuleSetBuilder<T> stopCondition(Condition condition) {
        Assert.notNull(condition, "stopCondition cannot be null.");
        this.stopCondition = condition;
        return this;
    }

    /**
     * PreAction(Optional) Action to be executed before the execution of the RuleSet.
     *
     * @param initializer action before execution of the RuleSet.
     * @return this for fluency.
     */
    public RuleSetBuilder<T> initializer(Action initializer) {
        Assert.notNull(initializer, "initializer cannot be null.");
        this.initializer = initializer;
        return this;
    }

    /**
     * PostAction(Optional) Action to be executed after the execution of the RuleSet.
     *
     * @param finalizer post-action before execution of the RuleSet.
     * @return this for fluency.
     */
    public RuleSetBuilder<T> finalizer(Action finalizer) {
        Assert.notNull(finalizer, "finalizer cannot be null.");
        this.finalizer = finalizer;
        return this;
    }

    /**
     * Sets the result extractor function for the RuleSetBuilder.
     *
     * @param resultExtractor the function used to extract results
     * @return this RuleSetBuilder instance for method chaining
     */
    public RuleSetBuilder<T> resultExtractor(Function<T> resultExtractor) {
        Assert.notNull(resultExtractor, "resultExtractor cannot be null.");
        this.resultExtractor = resultExtractor;
        return this;
    }

    /**
     * Adds a rule to the RuleSetBuilder.
     *
     * @param rule the rule to be added to the RuleSetBuilder. Must not be null.
     * @return this RuleSetBuilder instance for method chaining.
     */
    public RuleSetBuilder<T> rule(Rule rule) {
        Assert.notNull(rule, "rule cannot be null");
        getRules().add(rule);
        return this;
    }

    /**
     * Adds a rule at the specified index in the RuleSetBuilder.
     *
     * @param index the index at which the rule should be added. Must be >= 0.
     * @param rule the rule to be added to the RuleSetBuilder. Must not be null.
     * @return this RuleSetBuilder instance for method chaining.
     */
    public RuleSetBuilder<T> rule(int index, Rule rule) {
        Assert.notNull(rule, "rule cannot be null");
        Assert.isTrue(index >= 0, "index must be >= 0");
        getRules().add(index, rule);
        return this;
    }

    /**
     * Sets the rules for this RuleSetBuilder.
     *
     * @param rules the array of rules to set for this RuleSetBuilder. Must not be null.
     * @return this RuleSetBuilder instance for method chaining
     */
    public RuleSetBuilder<T> rules(Rule...rules) {
        Assert.notNullArray(rules, "rules cannot be null.");
        Arrays
                .stream(rules)
                .filter(Objects::nonNull)
                .forEach(this::rule);
        return this;
    }

    /**
     * Sets the rules for this RuleSetBuilder.
     *
     * @param rules the collection of rules to be set for this RuleSetBuilder. Must not be null.
     * @return this RuleSetBuilder instance for method chaining
     */
    public RuleSetBuilder<T> rules(Collection<Rule> rules) {
        Assert.notNull(rules, "rules cannot be null.");
        rules
                .stream()
                .filter(Objects::nonNull)
                .forEach(this::rule);
        return this;
    }

    /**
     * Sets the rules at the specified index in the RuleSetBuilder.
     *
     * @param index the index at which the rules should be added. Must be >= 0.
     * @param rules the array of rules to be added to the RuleSetBuilder. Must not be null.
     * @return this RuleSetBuilder instance for method chaining
     */
    public RuleSetBuilder<T> rules(int index, Rule...rules) {
        Assert.notNullArray(rules, "rules cannot be null.");

        for (int i = rules.length - 1; i >= 0; i--) {
            rule(index, rules[i]);
        }

        return this;
    }

    /**
     * Returns the size of the RuleSetItems.
     *
     * @return the size of the RuleSetItems
     */
    public int size() {
        return ruleSetItems.size();
    }

    /**
     * Retrieves the list of rules associated with this RuleSetBuilder.
     *
     * @return LinkedList of Rule objects representing the rules in this RuleSetBuilder
     */
    protected LinkedList<Rule> getRules() {
        return ruleSetItems;
    }

    /**
     * Removes the RuleSetItem at the specified index.
     *
     * @param index the index of the RuleSetItem to be removed. Must be >= 0.
     * @return this RuleSetBuilder instance for method chaining
     */
    public RuleSetBuilder<T> remove(int index) {
        this.ruleSetItems.remove(index);
        return this;
    }

    /**
     * Removes the specified Rule from the RuleSetBuilder.
     *
     * @param rule the rule to be removed from the RuleSetBuilder
     * @return this RuleSetBuilder instance for method chaining
     */
    public RuleSetBuilder<T> remove(Rule rule) {
        this.ruleSetItems.remove(rule);
        return this;
    }

    /**
     * Builds a RuleSetDefinition based on the rules provided in the RuleSetBuilder instance.
     *
     * @return a RuleSetDefinition object containing the name, description, source definition, pre-condition,
     * stop condition, and an array of rule definitions for the rules in the RuleSetBuilder.
     */
    protected RuleSetDefinition buildRuleSetDefinition() {
        List<RuleDefinition> definitions = new ArrayList<>(getRules().size());

        getRules().forEach(r -> {
            definitions.add(r.getDefinition());
        });

        return new RuleSetDefinition(getName(), getDescription(), SourceDefinition.build(),
                getPreCondition() != null ? getPreCondition().getDefinition() : null,
                getStopCondition() != null ? getStopCondition().getDefinition() : null,
                definitions.toArray(new RuleDefinition[definitions.size()]));
    }

    /**
     * Builds and returns a RuleSet instance based on the current configuration of the RuleSetBuilder.
     *
     * @return a RuleSet instance constructed with the defined RuleSetDefinition, pre-condition, stop-condition,
     *         initializer, finalizer, result-extractor, and list of rules.
     */
    public RuleSet<T> build() {
        return new RulingFamily<T>(buildRuleSetDefinition(), getPreCondition(), getStopCondition(),
                getInitializer(), getFinalizer(), getResultExtractor(), Collections.unmodifiableList(getRules()));
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Condition getPreCondition() {
        return preCondition;
    }

    public Condition getStopCondition() {
        return stopCondition;
    }

    public Action getInitializer() {
        return initializer;
    }

    public Action getFinalizer() {
        return finalizer;
    }

    public Function<T> getResultExtractor() {
        return resultExtractor;
    }

    @Override
    public String toString() {
        return "RuleSetBuilder{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", preCondition=" + preCondition +
                ", stopCondition=" + stopCondition +
                ", initializer=" + initializer +
                ", finalizer=" + finalizer +
                ", resultExtractor=" + resultExtractor +
                ", ruleSetItems=" + ruleSetItems +
                '}';
    }
}
