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
import org.rulii.model.Runnable;
import org.rulii.model.*;
import org.rulii.model.action.Action;
import org.rulii.model.condition.Condition;
import org.rulii.model.function.Function;
import org.rulii.rule.Rule;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Represents a collection of Rules that can be executed together, typically in a specific sequence or according
 * to defined conditions. A RuleSet is identifiable, definable, and capable of running executable logic within
 * the bounds of a given RuleContext.
 *
 * A RuleSet can include an initializer and finalizer action, pre-conditions and stopping conditions,
 * along with a mechanism to extract a result after the execution of its Rules.
 *
 * @param <T> The return type of the RuleSet when executed.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public interface RuleSet<T> extends Runnable<T>, Identifiable, Iterable<Rule>, Definable<RuleSetDefinition>, ScopeDefining {

    /**
     * Creates and returns an instance of RuleSetBuilderBuilder, allowing the construction
     * of RuleSet objects.
     *
     * @return a singleton instance of RuleSetBuilderBuilder for building RuleSet objects.
     */
    static RuleSetBuilderBuilder builder() {
        return RuleSetBuilderBuilder.getInstance();
    }

    /**
     * Executes the set of rules defined within the RuleSet using the provided RuleContext.
     *
     * @param context the RuleContext containing the necessary state and dependencies for rule execution. Must not be null.
     * @return the result of the rule execution as an instance of type T.
     * @throws UnrulyException if an error occurs during rule execution.
     */
    T run(RuleContext context) throws UnrulyException;

    /**
     * Asynchronously executes the rules defined within the RuleSet using the provided RuleContext.
     *
     * @param ruleContext the RuleContext containing the necessary state and dependencies
     *                    for rule execution. Must not be null.
     * @return a CompletableFuture that completes with the result of the rule execution
     *         as an instance of type T.
     */
    CompletableFuture<T> runAsync(RuleContext ruleContext);

    /**
     * Asynchronously runs the rules defined within the RuleSet using the provided RuleContext with a specified timeout.
     *
     * @param ruleContext the RuleContext containing the necessary state and dependencies for rule execution. Must not be null.
     * @param timeOut the maximum time to wait for the completion of the async execution.
     * @param timeUnit the time unit of the timeout value.
     * @return a CompletableFuture that completes with the result of the rule execution as an instance of type T.
     */
    CompletableFuture<T> runAsync(RuleContext ruleContext, long timeOut, TimeUnit timeUnit);

    /**
     * Ruleset name.
     *
     * @return name.
     */
    String getName();

    /**
     * RuleSet description.
     *
     * @return description.
     */
    String getDescription();

    @Override
    RuleSetDefinition getDefinition();

    /**
     * Returns the Condition (if one exists) to be met before the execution of the Rules.
     *
     * @return pre-check before execution of the Rules.
     */
    Condition getPreCondition();

    /**
     * Returns the initializer Action for the RuleSet. This Action is executed before any Rule in the RuleSet is triggered.
     *
     * @return the Action that initializes the RuleSet.
     */
    Action getInitializer();

    /**
     * Returns the Condition that determines when execution should stop.
     *
     * @return stopping condition.
     */
    Condition getStopCondition();

    /**
     * Returns the finalizer Action for the RuleSet. This Action is executed after all Rules in the RuleSet have been
     * processed, regardless of their outcomes.
     *
     * @return the Action that finalizes the RuleSet.
     */
    Action getFinalizer();

    /**
     * Retrieves the result extraction function for this RuleSet.
     *
     * @return a Function instance that is responsible for extracting the result of this RuleSet's execution.
     */
    Function<T> getResultExtractor();

    /**
     * Size of this RuleSet (ie : number of Rules in this RuleSet)
     *
     * @return number of Rules in this RuleSet.
     */
    int size();

    /**
     * Retrieves a Rule from the RuleSet by its name.
     *
     * @param name the name of the Rule to be retrieved.
     * @return the Rule with the specified name, or null if no such Rule exists.
     */
    Rule get(String name);

    /**
     * Retrieves a Rule from the RuleSet based on its index position.
     *
     * @param index the position of the Rule in the RuleSet. Index must be non-negative and less than the size of the RuleSet.
     * @return the Rule at the specified index.
     * @throws IndexOutOfBoundsException if the index is out of range.
     */
    Rule get(int index);

    /**
     * Retrieves the list of rules within the RuleSet.
     *
     * @return a list of {@link Rule} instances contained in the RuleSet. The list may be empty if no rules are defined.
     */
    List<Rule> getRules();

    /**
     * Determines whether the RuleSet is empty. A RuleSet is considered empty if it either has no rules
     * (the list of rules is null) or if the list of rules contains no elements.
     *
     * @return true if the RuleSet is empty, false otherwise.
     */
    default boolean isEmpty() {
        return getRules() == null || getRules().isEmpty();
    }

}

