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

import org.rulii.context.RuleContext;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.UnrulyException;
import org.rulii.model.action.Action;
import org.rulii.model.condition.Condition;
import org.rulii.model.function.Function;
import org.rulii.rule.Rule;
import org.rulii.util.RuleUtils;
import org.rulii.validation.ValidationRule;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * A class that represents a set of rules grouped and executed within a specified context.
 * This class manages rule evaluation execution, precondition checks, stopping conditions,
 * result extraction, initialization, and finalization. It implements the {@link RuleSet} interface
 * to provide a structured way to apply rules to a given context.
 *
 * This is the default implementation for {@link RuleSet}
 *
 * @param <T> the type of the result that this rule set produces after execution
 *
 * @author Max Arulananthan
 * @since 1.0
 *
 */
public class RulingFamily<T> implements RuleSet<T> {

    private final RuleSetDefinition ruleSetDefinition;
    private final List<ValidationRule> inputValidators;
    private final List<Rule> inputValidationRules;
    private final Condition preCondition;
    private final Action initializer;
    private final Action finalizer;
    private final Function<T> resultExtractor;
    private final List<Rule> rules = new LinkedList<>();
    private final Condition stopCondition;
    private final String description;
    private final RuleSetExecutionStrategy<T> syncStrategy;
    private final RuleSetExecutionStrategy<CompletableFuture<T>> asyncStrategy;

    public RulingFamily(RuleSetDefinition ruleSetDefinition,
                        List<ValidationRule> inputValidators,
                        Condition preCondition,
                        Condition stopCondition,
                        Action initializer,
                        Action finalizer,
                        Function<T> resultExtractor,
                        List<Rule> rules) {
        super();
        Assert.notNull(ruleSetDefinition, "ruleSetDefinition cannot be null.");
        Assert.notNull(rules, "rules cannot be null.");
        this.ruleSetDefinition = ruleSetDefinition;
        this.rules.addAll(rules);
        this.inputValidators = inputValidators != null ? Collections.unmodifiableList(inputValidators) : Collections.emptyList();
        this.inputValidationRules = transform(inputValidators);
        this.preCondition = preCondition;
        this.stopCondition = stopCondition;
        this.initializer = initializer;
        this.finalizer = finalizer;
        this.resultExtractor = resultExtractor;
        this.description = ruleSetDefinition.getDescription() != null ? ruleSetDefinition.getDescription() :
                "ruleSet(name = " + getName() + ", size = " + size() + ")";
        this.syncStrategy = RuleSetExecutionStrategy.build();
        this.asyncStrategy = RuleSetExecutionStrategy.buildAsync();
    }

    @Override
    public T run(RuleContext ruleContext) throws UnrulyException {
        Assert.notNull(ruleContext, "context cannot be null");
        return syncStrategy.run(this, ruleContext);
    }

    @Override
    public CompletableFuture<T> runAsync(RuleContext ruleContext) {
        Assert.notNull(ruleContext, "context cannot be null");
        return asyncStrategy.run(this, ruleContext);
    }

    @Override
    public CompletableFuture<T> runAsync(RuleContext ruleContext, long timeOut, TimeUnit timeUnit) {
        Assert.notNull(ruleContext, "context cannot be null");
        Assert.notNull(timeUnit, "timeUnit cannot be null.");
        CompletableFuture<T> result = asyncStrategy.run(this, ruleContext);
        return result.orTimeout(timeOut, timeUnit);
    }

    @Override
    public RuleSetDefinition getDefinition() {
        return ruleSetDefinition;
    }

    @Override
    public Rule getRule(String name) {
        Assert.notNull(name, "name cannot be null.");
        Rule result = null;

        for (Rule item : getRules()) {
            if (name.equals(item.getName())) {
                result = item;
                break;
            }
        }

        return result;
    }

    private List<Rule> transform(List<ValidationRule> inputValidators) {
        if (inputValidators == null || inputValidators.isEmpty()) return Collections.emptyList();

        List<Rule> result = new LinkedList<>();
        for (ValidationRule validationRule : inputValidators) {
            result.add(Rule.builder().build(validationRule));
        }

        return result;
    }

    @Override
    public String getName() {
        return getDefinition().getName();
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public List<ValidationRule> getInputValidators() {
        return inputValidators;
    }

    @Override
    public List<Rule> getInputValidationRules() {
        return inputValidationRules;
    }

    @Override
    public Condition getPreCondition() {
        return preCondition;
    }

    @Override
    public Condition getStopCondition() {
        return stopCondition;
    }

    @Override
    public Action getInitializer() {
        return initializer;
    }

    @Override
    public Action getFinalizer() {
        return finalizer;
    }

    @Override
    public Function<T> getResultExtractor() {
        return resultExtractor;
    }

    @Override
    public Rule getRule(int index) {
        return rules.get(index);
    }

    @Override
    public int size() {
        return getRules().size();
    }

    @Override
    public Iterator<Rule> iterator() {
        return getRules().iterator();
    }

    @Override
    public List<Rule> getRules() {
        return Collections.unmodifiableList(rules);
    }

    /**
     * Constructs a human-readable representation of the RuleSet.
     *
     * @return a formatted string representing the RuleSet containing its name, pre and stop conditions (if present),
     * and a list of rules with their descriptions
     */
    public String prettyPrint() {
        StringBuilder result = new StringBuilder();

        result.append("RuleSet : ").append(getName());
        result.append(System.lineSeparator());
        result.append("Input Validators : ").append(getInputValidators());
        result.append(RuleUtils.TAB);
        if (getPreCondition() != null) result.append("pre : ").append(getPreCondition().getDescription());
        result.append(System.lineSeparator());
        result.append(RuleUtils.TAB);
        if (getStopCondition() != null) result.append("stop : ").append(getStopCondition().getDescription());
        result.append(System.lineSeparator());
        result.append(RuleUtils.TAB);
        result.append("Rules");
        result.append(System.lineSeparator());
        for (Rule rule : getRules()) {
            result.append(RuleUtils.TAB);
            result.append(RuleUtils.TAB);
            result.append(rule.getDescription());
            result.append(System.lineSeparator());
        }
        return result.toString();
    }

    @Override
    public String toString() {
        return prettyPrint();
    }
}
