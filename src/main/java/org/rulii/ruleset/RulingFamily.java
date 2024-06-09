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
package org.rulii.ruleset;

import org.rulii.bind.Binding;
import org.rulii.bind.NamedScope;
import org.rulii.bind.PromiscuousBinder;
import org.rulii.bind.ReservedBindings;
import org.rulii.context.RuleContext;
import org.rulii.lang.action.Action;
import org.rulii.lang.condition.Condition;
import org.rulii.lang.function.Function;
import org.rulii.lib.apache.commons.logging.Log;
import org.rulii.lib.apache.commons.logging.LogFactory;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.UnrulyException;
import org.rulii.rule.Rule;
import org.rulii.rule.RuleResult;
import org.rulii.util.RuleUtils;

import java.util.*;

/**
 * Default implementation of the RuleSet.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class RulingFamily<T> implements RuleSet<T> {

    private static final Log logger = LogFactory.getLog(RulingFamily.class);

    private final RuleSetDefinition ruleSetDefinition;
    private final Condition preCondition;
    private final Action initializer;
    private final Action finalizer;
    private final Function<T> resultExtractor;
    private final List<Rule> rules = new LinkedList<>();
    private final Condition stopCondition;
    private final String description;

    public RulingFamily(RuleSetDefinition ruleSetDefinition,
                        Condition preCondition,
                        Condition stopCondition,
                        Action initializer,
                        Action finalizer,
                        Function<T> resultExtractor,
                        List<Rule> rules) {
        super();
        Assert.notNull(ruleSetDefinition, "ruleSetDefinition cannot be null");
        Assert.notNull(resultExtractor, "resultExtractor cannot be null");
        this.ruleSetDefinition = ruleSetDefinition;
        this.rules.addAll(rules);
        this.preCondition = preCondition;
        this.stopCondition = stopCondition;
        this.initializer = initializer;
        this.finalizer = finalizer;
        this.resultExtractor = resultExtractor;
        this.description = ruleSetDefinition.getDescription() != null ? ruleSetDefinition.getDescription() :
                "ruleSet(name = " + getName() + ", size = " + size() + ")";
    }

    @Override
    public T run(RuleContext ruleContext) throws UnrulyException {
        Assert.notNull(ruleContext, "context cannot be null");

        RuleSetExecutionStatus ruleSetStatus = new RuleSetExecutionStatus();
        // Create a new Scope for the RuleSet to use
        NamedScope ruleSetScope = createRuleSetScope(ruleContext, ruleSetStatus);
        ruleContext.getTracer().fireOnRuleSetStart(this, ruleSetScope);
        logger.debug("RuleSet [" + getName() + "] Execution. Scope [" +  ruleSetScope.getName() + "] created.");

        try {
            // Run the PreCondition if there is one.
            boolean preConditionCheck = checkPreCondition(ruleContext);
            ruleSetStatus.setPreConditionCheck(preConditionCheck);

            // RuleSet did not pass the precondition; Do not execute the rules.
            if (!preConditionCheck) {
                logger.debug("RuleSet [" + getName() + "] pre-condition check failed. RuleSet is skipped.");
                return null;
            }

            // Run the rules
            runRules(ruleContext, ruleSetStatus);

            return extractResult(ruleContext, ruleSetStatus);
        } catch (UnrulyException e) {
            if (!e.isFilled()) e.fillStack(ruleContext.getExecutionContext().getStackTrace());
            logger.error("RuleSet [" + getName() + "] execution caused an error.", e);
            throw e;
        } finally {
            removeRuleSetScope(ruleContext, ruleSetScope);
            logger.debug("RuleSet [" + getName() + "] Executed. Scope cleared.");
        }
    }

    /**
     * Executes the rules in the RuleSet.
     *
     * @param ruleContext the RuleContext representing the current context (must not be null)
     * @param status      the RuleSetStatus to keep track of rule execution results (must not be null)
     */
    protected void runRules(RuleContext ruleContext, RuleSetExecutionStatus status) {
        try {
            // Run any PreAction if one is available.
            runInitializer(ruleContext);

            // Execute the rules/actions in order; STOP if the stopCondition is met.
            for (Rule rule : getRules()) {
                // Run the rule/action
                logger.debug("RuleSet [" + getName() + "] running rule [" + rule.getName() + "]");
                RuleResult executionResult = rule.run(ruleContext);
                status.add(executionResult);
                // Fire Rule event
                ruleContext.getTracer().fireOnRuleSetRuleRun(this, rule, executionResult, status);

                // Check to see if we need to stop the execution?
                if (getStopCondition() != null && getStopCondition().run(ruleContext)) {
                    logger.debug("Stopping RuleSet [" + getName() + "]. Stop condition met.");
                    // Fire Stop event
                    ruleContext.getTracer().fireOnRuleSetStop(this, getStopCondition(), status);
                    break;
                }
            }
        } finally {
            // Run the Finalizer after executing the Rules
            runFinalizer(ruleContext);
        }
    }

    /**
     * Creates a NamedScope for the RuleSet.
     * The NamedScope is created based on the given RuleContext and RuleSetResult.
     * This method adds the RuleSet scope to the bindings of the RuleContext.
     * It also binds reserved keywords THIS and RULE_SET_RESULT to their corresponding values.
     *
     * @param ruleContext   the RuleContext representing the current context (must not be null)
     * @param ruleResultSet the RuleSetResult representing the result of the RuleSet (must not be null)
     * @return the created NamedScope
     * @throws UnrulyException if the current scope does not allow reserved keyword binding
     */
    protected NamedScope createRuleSetScope(RuleContext ruleContext, RuleSetExecutionStatus ruleResultSet) {
        Assert.notNull(ruleContext, "ruleContext cannot be null.");
        Assert.notNull(ruleResultSet, "ruleResultSet cannot be null.");

        NamedScope result = ruleContext.getBindings().addScope(getRuleSetScopeName());

        if (!(result.getBindings() instanceof PromiscuousBinder bindings)) {
            throw new UnrulyException("IllegalState CurrentScope does not allow reserved keyword binding.");
        }

        bindings.promiscuousBind(Binding.builder().with(ReservedBindings.THIS.getName())
                .type(RuleSet.class)
                .value(this)
                .build());
        bindings.promiscuousBind(Binding.builder().with(ReservedBindings.RULE_SET_STATUS.getName())
                .type(RuleSetExecutionStatus.class)
                .value(ruleResultSet)
                .build());

        return result;
    }

    /**
     * Retrieves the name of the scope for the rule set.
     *
     * @return the scope name for the rule set
     */
    protected String getRuleSetScopeName() {
        return getName() + "-scope-" + UUID.randomUUID();
    }

    /**
     * Checks the pre-condition for the RuleSet execution.
     *
     * @param ruleContext the rule context to execute the pre-condition in
     * @return {@code true} if the pre-condition is satisfied, {@code false} otherwise
     */
    protected boolean checkPreCondition(RuleContext ruleContext) {
        Assert.notNull(ruleContext, "ruleContext cannot be null.");
        boolean result = true;

        if (getPreCondition() != null) {
            // Check the Pre-Condition
            logger.debug("RuleSet [" + getName() + "] executing pre-condition check.");
            result = getPreCondition().run(ruleContext);
            // Notify the pre-condition check
            ruleContext.getTracer().fireOnRuleSetPreConditionCheck(this, getPreCondition(), result);
        }

        return result;
    }

    /**
     * Runs the Initializer before executing the Rules.
     *
     * @param ruleContext the RuleContext representing the current context (must not be null)
     */
    protected void runInitializer(RuleContext ruleContext) {
        Assert.notNull(ruleContext, "ruleContext cannot be null.");

        // Run the PreAction before executing the Rules
        if (getInitializer() != null) {
            getInitializer().run(ruleContext);
            logger.debug("RuleSet [" + getName() + "] initializer [" + getInitializer().getName() + "] is executed.");
            ruleContext.getTracer().fireOnRuleSetInitializer(this, getInitializer());
        }
    }

    /**
     * Runs the Finalizer after executing the Rules.
     *
     * @param ruleContext the RuleContext representing the current context (must not be null)
     */
    protected void runFinalizer(RuleContext ruleContext) {
        Assert.notNull(ruleContext, "ruleContext cannot be null.");

        // Run the Finalizer after executing the Rules
        if (getFinalizer() != null) {
            getFinalizer().run(ruleContext);
            logger.debug("RuleSet [" + getName() + "] finalizer [" + getFinalizer().getName() + "] is executed.");
            ruleContext.getTracer().fireOnRuleSetFinalizer(this, getFinalizer());
        }
    }

    /**
     * Extracts the result of executing the rules in the RuleSet.
     *
     * @param ruleContext   the RuleContext representing the current context (must not be null)
     * @param ruleSetStatus the RuleSetStatus to keep track of rule execution results (must not be null)
     * @return the result of executing the rules, or null if no result extractor is set
     */
    protected T extractResult(RuleContext ruleContext, RuleSetExecutionStatus ruleSetStatus) {
        T result = null;

        if (getResultExtractor() != null) {
            result = getResultExtractor().apply(ruleContext.getBindings());
            logger.debug("RuleSet [" + getName() + "] result [" + result + "]");
            ruleContext.getTracer().fireOnRuleSetResult(this, getResultExtractor(), ruleSetStatus);
        }

        return result;
    }

    /**
     * Removes the specified NamedScope from the RuleContext's bindings.
     *
     * @param context the RuleContext representing the current context (must not be null)
     * @param target  the NamedScope to be removed (must not be null)
     */
    protected void removeRuleSetScope(RuleContext context, NamedScope target) {
        context.getBindings().removeScope(target);
    }

    @Override
    public RuleSetDefinition getDefinition() {
        return ruleSetDefinition;
    }

    @Override
    public Rule get(String name) {
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

    @Override
    public String getName() {
        return getDefinition().getName();
    }

    @Override
    public String getDescription() {
        return description;
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
    public Rule get(int index) {
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

    public String prettyPrint() {
        StringBuilder result = new StringBuilder();

        result.append("RuleSet : ").append(getName());
        result.append(System.lineSeparator());
        result.append(RuleUtils.TAB);
        if (getPreCondition() != null) result.append("pre : " + getPreCondition().getDescription());
        result.append(System.lineSeparator());
        result.append(RuleUtils.TAB);
        if (getStopCondition() != null) result.append("stop : " + getStopCondition().getDescription());
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
