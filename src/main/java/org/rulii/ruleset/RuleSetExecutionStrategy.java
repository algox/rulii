package org.rulii.ruleset;

import org.rulii.context.RuleContext;
import org.rulii.model.UnrulyException;

import java.util.concurrent.CompletableFuture;

/**
 * An interface representing a strategy for executing a rule set.
 *
 * @param <T> the type of the result generated by executing the rule set.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public interface RuleSetExecutionStrategy<T> {

    /**
     * Constructs and returns a new instance of DefaultRuleSetExecutionStrategy.
     *
     * @param <T> the type of the result generated by executing the rule set.
     * @return a new DefaultRuleSetExecutionStrategy instance
     */
    static <T> RuleSetExecutionStrategy<T> build() {
        return new DefaultRuleSetExecutionStrategy<>();
    }

    /**
     * Builds and returns a new instance of RuleSetExecutionStrategy for executing a rule set asynchronously using CompletableFuture.
     *
     * @param <T> the type of the result generated by executing the rule set.
     * @return a new RuleSetExecutionStrategy instance for asynchronous execution
     */
    static <T> RuleSetExecutionStrategy<CompletableFuture<T>> buildAsync() {
        return new AsyncRuleSetExecutionStrategy<>();
    }

    /**
     * Executes a ruleSet based on the provided RuleContext.
     *
     * @param ruleSet ruleSet to be executed.
     * @param context the RuleContext containing all necessary information for executing the rule.
     * @return the result generated by executing the ruleSet
     * @throws UnrulyException if an error occurs during ruleSet execution
     */
    T run(RuleSet<?> ruleSet, RuleContext context) throws UnrulyException;
}
