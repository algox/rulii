package org.rulii.ruleset;

import org.rulii.bind.NamedScope;
import org.rulii.context.RuleContext;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.UnrulyException;
import org.rulii.rule.Rule;
import org.rulii.rule.RuleResult;

/**
 * Default rule set execution strategy for running a set of rules in a specified order.
 *
 * This class extends the RuleSetExecutionStrategyTemplate and provides the default behavior for executing rules.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class DefaultRuleSetExecutionStrategy<T> extends RuleSetExecutionStrategyTemplate<T> {

    public DefaultRuleSetExecutionStrategy() {
        super();
    }

    @Override
    public T run(RuleSet<?> ruleSet, RuleContext ruleContext) throws UnrulyException {
        Assert.notNull(ruleContext, "context cannot be null");
        RuleSetExecutionStatus ruleSetStatus = new RuleSetExecutionStatus();
        // Create a new Scope for the RuleSet to use
        NamedScope ruleSetScope = createRuleSetScope(ruleSet, ruleContext, ruleSetStatus);
        ruleContext.getTracer().fireOnRuleSetStart(ruleSet, ruleSetScope);
        if (getLogger().isDebugEnabled()) getLogger().debug("RuleSet [" + ruleSet.getName() + "] Execution. Scope [" +  ruleSetScope.getName() + "] created.");

        try {
            // Run the PreCondition if there is one.
            boolean preConditionCheck = checkPreCondition(ruleSet, ruleContext);
            ruleSetStatus.setPreConditionCheck(preConditionCheck);
            // RuleSet did not pass the precondition; Do not execute the rules.
            if (!preConditionCheck) {
                if (getLogger().isDebugEnabled()) getLogger().debug("RuleSet [" + ruleSet.getName() + "] pre-condition check failed. RuleSet is skipped.");
                return null;
            }
            // Run the rules
            runRules(ruleSet, ruleContext, ruleSetStatus);
            return extractResult(ruleSet, ruleContext, ruleSetStatus);
        } catch (Exception e) {
            getLogger().error("RuleSet [" + ruleSet.getName() + "] execution caused an error.", e);
            ruleContext.getTracer().fireOnRuleSetError(ruleSet, ruleSetStatus, e);
            throw new UnrulyException("Error trying to run RuleSet [" + ruleSet.getName() + "]", e);
        } finally {
            removeRuleSetScope(ruleContext, ruleSetScope);
            ruleContext.getTracer().fireOnRuleSetEnd(ruleSet, ruleSetScope, ruleSetStatus);
            if (getLogger().isDebugEnabled()) getLogger().debug("RuleSet [" + ruleSet.getName() + "] Executed. Scope cleared.");
        }
    }

    /**
     * Executes the rules in the RuleSet.
     *
     * @param ruleContext the RuleContext representing the current context (must not be null)
     * @param status      the RuleSetStatus to keep track of rule execution results (must not be null)
     */
    protected void runRules(RuleSet<?> ruleSet, RuleContext ruleContext, RuleSetExecutionStatus status) {
        // Run any PreAction if one is available.
        runInitializer(ruleSet, ruleContext);

        try {
            // Execute the rules/actions in order; STOP if the stopCondition is met.
            for (Rule rule : ruleSet.getRules()) {
                // Run the rule/action
                if (getLogger().isDebugEnabled()) getLogger().debug("RuleSet [" + ruleSet.getName() + "] running rule [" + rule.getName() + "]");
                RuleResult executionResult = rule.run(ruleContext);
                status.add(executionResult);
                // Fire Rule event
                ruleContext.getTracer().fireOnRuleSetRuleRun(ruleSet, rule, executionResult, status);
                // Check to see if we need to stop the execution?
                if (ruleSet.getStopCondition() != null && ruleSet.getStopCondition().run(ruleContext)) {
                    if (getLogger().isDebugEnabled()) getLogger().debug("Stopping RuleSet [" + ruleSet.getName() + "]. Stop condition met.");
                    // Fire Stop event
                    ruleContext.getTracer().fireOnRuleSetStop(ruleSet, ruleSet.getStopCondition(), status);
                    break;
                }
            }
        } finally {
            // Run the Finalizer after executing the Rules
            runFinalizer(ruleSet, ruleContext);
        }
    }
}
