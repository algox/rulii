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

import org.rulii.bind.Bindings;
import org.rulii.bind.ReservedBindings;
import org.rulii.bind.match.MatchByTypeMatchingStrategy;
import org.rulii.context.RuleContext;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.SourceDefinition;
import org.rulii.model.action.Action;
import org.rulii.model.condition.Condition;
import org.rulii.model.function.Function;
import org.rulii.rule.Rule;
import org.rulii.rule.RuleDefinition;
import org.rulii.util.RuleUtils;
import org.rulii.validation.RuleViolations;
import org.rulii.validation.ValidationException;
import org.rulii.validation.ValidationRule;

import java.util.*;

/**
 * A builder class to construct RuleSets with customizable preconditions, stop conditions, initializers,
 * finalizers, and result extractors.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class RuleSetBuilder {

    private String name;
    private String description = null;
    private final List<ValidationRule> inputValidators = new LinkedList<>();
    private Condition preCondition = null;
    private Condition stopCondition = null;
    private Action initializer = null;
    private Action finalizer = null;
    private Function<?> resultExtractor = (RuleContext ruleContext) -> ruleContext.getBindings().getValue(ReservedBindings.RULE_SET_STATUS.getName());
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
    RuleSetBuilder(RuleSet<?> rules) {
        super();
        Assert.notNull(rules, "rules cannot be null.");
        name(rules.getName());
        description(rules.getDescription());
        if (rules.getInputValidators() != null) inputValidators(rules.getInputValidators());
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
    public RuleSetBuilder name(String name) {
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
    public RuleSetBuilder description(String description) {
        this.description = description;
        return this;
    }

    /**
     * Adds a validation rule to the input validators of this RuleSetBuilder.
     *
     * @param validator the validation rule to add. Must not be null.
     * @return this RuleSetBuilder instance for method chaining.
     */
    public RuleSetBuilder inputValidator(ValidationRule validator) {
        Assert.notNull(validator, "validator cannot be null.");
        this.inputValidators.add(validator);
        return this;
    }

    /**
     * Sets the input validators for this RuleSetBuilder.
     *
     * @param validators the array of ValidationRule objects to set as input validators. Must not be null.
     * @return this RuleSetBuilder instance for method chaining
     */
    public RuleSetBuilder inputValidators(ValidationRule...validators) {
        Assert.notNullArray(validators, "validators cannot be null.");
        Arrays
                .stream(validators)
                .filter(Objects::nonNull)
                .forEach(this::inputValidator);
        return this;
    }

    /**
     * Sets the input validators for this RuleSetBuilder.
     *
     * @param validators a collection of ValidationRule objects to set as input validators. Must not be null.
     * @return this RuleSetBuilder instance for method chaining
     */
    public RuleSetBuilder inputValidators(Collection<ValidationRule> validators) {
        Assert.notNull(validators, "validators cannot be null.");
        validators
                .stream()
                .filter(Objects::nonNull)
                .forEach(this::inputValidator);
        return this;
    }

    /**
     * Clears all input validators set for this RuleSetBuilder instance.
     *
     * @return this RuleSetBuilder instance for method chaining
     */
    public RuleSetBuilder clearInputValidators() {
        this.inputValidators.clear();
        return this;
    }
    /**
     * PreCondition(Optional) Condition to be met before the execution of the RuleSet.
     *
     * @param preCondition pre-check before execution of the RuleSet.
     * @return this for fluency.
     */
    public RuleSetBuilder preCondition(Condition preCondition) {
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
    public RuleSetBuilder stopCondition(Condition condition) {
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
    public RuleSetBuilder initializer(Action initializer) {
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
    public RuleSetBuilder finalizer(Action finalizer) {
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
    public RuleSetBuilder resultExtractor(Function<?> resultExtractor) {
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
    public RuleSetBuilder rule(Rule rule) {
        Assert.notNull(rule, "rule cannot be null");
        this.ruleSetItems.add(rule);
        return this;
    }

    /**
     * Adds a rule at the specified index in the RuleSetBuilder.
     *
     * @param index the index at which the rule should be added. Must be >= 0.
     * @param rule the rule to be added to the RuleSetBuilder. Must not be null.
     * @return this RuleSetBuilder instance for method chaining.
     */
    public RuleSetBuilder rule(int index, Rule rule) {
        Assert.notNull(rule, "rule cannot be null");
        Assert.isTrue(index >= 0, "index must be >= 0");
        this.ruleSetItems.add(index, rule);
        return this;
    }

    /**
     * Sets the rules for this RuleSetBuilder.
     *
     * @param rules the array of rules to set for this RuleSetBuilder. Must not be null.
     * @return this RuleSetBuilder instance for method chaining
     */
    public RuleSetBuilder rules(Rule...rules) {
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
    public RuleSetBuilder rules(Collection<Rule> rules) {
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
    public RuleSetBuilder rules(int index, Rule...rules) {
        Assert.notNullArray(rules, "rules cannot be null.");

        for (int i = rules.length - 1; i >= 0; i--) {
            rule(index, rules[i]);
        }

        return this;
    }

    /**
     * Adds a validation rule to the RuleSetBuilder.
     *
     * @param rule the validation rule to be added
     * @return the RuleSetBuilder instance with the added validation rule
     */
    public RuleSetBuilder validationRule(ValidationRule rule) {
        return rule(Rule.builder().build(rule));
    }

    /**
     * Adds a validation rule at the specified index in the RuleSetBuilder.
     *
     * @param index the index at which the rule should be added. Must be >= 0.
     * @param rule the validation rule to be added. Must not be null.
     * @return the RuleSetBuilder instance with the added validation rule at the specified index
     */
    public RuleSetBuilder validationRule(int index, ValidationRule rule) {
        return rule(index, Rule.builder().build(rule));
    }

    /**
     * Adds an error container and sets up a ValidationException throw action if errors are present.
     *
     * @return this RuleSetBuilder instance for method chaining
     */
    public RuleSetBuilder validating() {
        // Add the error container.
        initializer(Action.builder().with((Bindings bindings, RuleViolations ruleViolations) -> {
                    if (ruleViolations == null) bindings.bind("ruleViolations", new RuleViolations());
                })
                .param(0).matchUsing(MatchByTypeMatchingStrategy.class).build()
                .build());
        // Throw a ValidationException if there are any errors during the run.
        finalizer(Action.builder().with((RuleViolations ruleViolations) -> {
                    if (ruleViolations.hasSevereErrors()) throw new ValidationException("RuleSet [" + getName()
                            + "] validation rules have failed. " + System.lineSeparator() + ruleViolations, ruleViolations);
                }).param(0).matchUsing(MatchByTypeMatchingStrategy.class).build()
                .build());

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
    public List<Rule> getRules() {
        return Collections.unmodifiableList(ruleSetItems);
    }

    /**
     * Retrieves the rule at the specified index from the RuleSetItems.
     *
     * @param index the index of the rule to retrieve. Must be >= 0.
     * @return the Rule object at the specified index.
     */
    public Rule getRule(int index) {
        return ruleSetItems.get(index);
    }

    /**
     * Removes the RuleSetItem at the specified index.
     *
     * @param index the index of the RuleSetItem to be removed. Must be >= 0.
     * @return this RuleSetBuilder instance for method chaining
     */
    public RuleSetBuilder remove(int index) {
        this.ruleSetItems.remove(index);
        return this;
    }

    /**
     * Removes the specified Rule from the RuleSetBuilder.
     *
     * @param rule the rule to be removed from the RuleSetBuilder
     * @return this RuleSetBuilder instance for method chaining
     */
    public RuleSetBuilder remove(Rule rule) {
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
    @SuppressWarnings("unchecked")
    public <T> RuleSet<T> build() {
        return new RulingFamily<>(buildRuleSetDefinition(), getInputValidators(), getPreCondition(), getStopCondition(),
                getInitializer(), getFinalizer(), (Function<T>) getResultExtractor(), Collections.unmodifiableList(getRules()));
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<ValidationRule> getInputValidators() {
        return Collections.unmodifiableList(inputValidators);
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

    public Function<?> getResultExtractor() {
        return resultExtractor;
    }

    @Override
    public String toString() {
        return "RuleSetBuilder{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", inputValidators=" + inputValidators +
                ", preCondition=" + preCondition +
                ", stopCondition=" + stopCondition +
                ", initializer=" + initializer +
                ", finalizer=" + finalizer +
                ", resultExtractor=" + resultExtractor +
                ", ruleSetItems=" + ruleSetItems +
                '}';
    }
}
