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

import org.rulii.bind.NamedScope;
import org.rulii.model.action.Action;
import org.rulii.model.condition.Condition;
import org.rulii.model.function.Function;
import org.rulii.rule.Rule;
import org.rulii.rule.RuleResult;
import org.rulii.validation.RuleViolations;

/**
 * Listener interface for observing events related to the execution of RuleSets.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public interface RuleSetListener {

    /**
     * Invoked when a RuleSet has started execution.
     *
     * @param ruleSet the RuleSet that has started execution. Must not be null.
     * @param ruleSetScope the NamedScope associated with the RuleSet. Must not be null.
     */
    default void onRuleSetStart(RuleSet<?> ruleSet, NamedScope ruleSetScope) {}

    /**
     * Method called when checking input against a RuleSet to identify any rule violations.
     *
     * @param ruleSet the RuleSet to check input against. Must not be null.
     * @param violations the RuleViolations object to store any violations found. Must not be null.
     */
    default void onRuleSetInputCheck(RuleSet<?> ruleSet, RuleViolations violations) {}

    /**
     * Callback method invoked when a pre-condition check is performed on a RuleSet.
     *
     * @param ruleSet the RuleSet on which the pre-condition check is being performed. Must not be null.
     * @param condition the Condition being checked. Must not be null.
     * @param result the result of the pre-condition check, indicating whether the condition is met.
     */
    default void onRuleSetPreConditionCheck(RuleSet<?> ruleSet, Condition condition, boolean result) {}

    /**
     * This method is called when a RuleSet is being initialized by applying the provided Action.
     *
     * @param ruleSet the RuleSet being initialized. Must not be null.
     * @param initializer the Action to be applied for initialization. Must not be null.
     */
    default void onRuleSetInitializer(RuleSet<?> ruleSet, Action initializer) {}

    /**
     * Invoked when a specific Rule within a RuleSet is executed.
     *
     * @param ruleSet the RuleSet to which the Rule belongs. Must not be null.
     * @param rule the Rule that was executed. Must not be null.
     * @param executionResult the result of the Rule execution. Must not be null.
     * @param status the status of the RuleSet execution after the Rule execution. Must not be null.
     */
    default void onRuleSetRuleRun(RuleSet<?> ruleSet, Rule rule, RuleResult executionResult, RuleSetExecutionStatus status) {}

    /**
     * Callback method invoked when a RuleSet is stopped due to the provided stop condition being met or during execution.
     *
     * @param ruleSet the RuleSet that is stopped. Must not be null.
     * @param stopCondition the Condition that caused the RuleSet to stop. Must not be null.
     * @param status the status of the RuleSet execution at the time of stopping. Must not be null.
     */
    default void onRuleSetStop(RuleSet<?> ruleSet, Condition stopCondition, RuleSetExecutionStatus status) {}

    /**
     * Executes the finalizer action after all Rules in the RuleSet have been processed, regardless of their outcomes.
     *
     * @param ruleSet the RuleSet for which the finalizer action is being executed. Must not be null.
     * @param finalizer the Action to be executed as the finalizer. Must not be null.
     */
    default void onRuleSetFinalizer(RuleSet<?> ruleSet, Action finalizer) {}

    /**
     * Invoked when the result of a RuleSet execution is available.
     *
     * @param ruleSet the RuleSet for which the result is being reported. Must not be null.
     * @param resultExtractor the function to extract the result of the RuleSet execution. Must not be null.
     * @param status the status of the RuleSet execution. Must not be null.
     */
    default void onRuleSetResult(RuleSet<?> ruleSet, Function<?> resultExtractor, RuleSetExecutionStatus status) {}

    /**
     * Callback method invoked when an error occurs during the execution of a RuleSet.
     *
     * @param ruleSet the RuleSet associated with the error. Must not be null.
     * @param status the status of the RuleSet execution at the time of the error. Must not be null.
     * @param e the Exception that occurred during RuleSet execution.
     */
    default void onRuleSetError(RuleSet<?> ruleSet, RuleSetExecutionStatus status, Exception e) {}

    /**
     * Called when a RuleSet has completed execution.
     *
     * @param ruleSet the RuleSet that has completed execution. Must not be null.
     * @param ruleSetScope the NamedScope associated with the RuleSet. Must not be null.
     * @param status the status of the RuleSet execution after completion. Must not be null.
     */
    default void onRuleSetEnd(RuleSet<?> ruleSet, NamedScope ruleSetScope, RuleSetExecutionStatus status) {}

}
