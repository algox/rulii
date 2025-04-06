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
package org.rulii.trace;

import org.rulii.bind.NamedScope;
import org.rulii.model.action.Action;
import org.rulii.model.condition.Condition;
import org.rulii.model.function.Function;
import org.rulii.rule.Rule;
import org.rulii.rule.RuleListener;
import org.rulii.rule.RuleResult;
import org.rulii.ruleset.RuleSet;
import org.rulii.ruleset.RuleSetExecutionStatus;
import org.rulii.ruleset.RuleSetListener;
import org.rulii.validation.RuleViolations;

/**
 * The Tracer interface represents a mechanism for tracking and triggering events related to rule and rule set execution.
 * Users can implement this interface to provide custom behavior when specific events occur.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public interface Tracer {

    static TracerBuilder builder() {
        return new TracerBuilder();
    }

    /**
     * Adds a RuliiListener to receive events related to rule and ruleset execution within the framework.
     *
     * @param listener the RuliiListener to be added
     */
    void addListener(RuliiListener listener);

    /**
     * Removes the specified RuliiListener from receiving events related to rule and ruleset execution within the framework.
     *
     * @param listener the RuliiListener to be removed from receiving events
     */
    void removeListener(RuliiListener listener);

    /**
     * Adds a RuleListener to receive events related to rule execution within the framework.
     *
     * @param listener the RuleListener to be added
     */
    void addListener(RuleListener listener);

    /**
     * Removes a RuleListener from receiving events related to rule execution within the framework.
     *
     * @param listener the RuleListener to be removed
     * @return true if the RuleListener was successfully removed, false otherwise
     */
    boolean removeListener(RuleListener listener);

    /**
     * Adds a RuleSetListener to receive events related to the execution of RuleSets.
     *
     * @param listener the RuleSetListener to be added
     */
    void addListener(RuleSetListener listener);

    /**
     * Removes a RuleSetListener from receiving events related to the execution of RuleSets.
     *
     * @param listener the RuleSetListener to be removed. Must not be null.
     * @return true if the RuleSetListener was successfully removed, false otherwise.
     */
    boolean removeListener(RuleSetListener listener);

    /**
     * Clears any existing data or state associated with this object.
     */
    void clear();

    /**
     * Fires an event when a rule has started execution.
     *
     * @param rule the Rule that has started execution
     */
    void fireOnRuleStart(Rule rule);

    /**
     * Fires an event when a pre-condition check is performed on a Rule.
     *
     * @param rule the Rule on which the pre-condition check is being performed
     * @param condition the Condition representing the pre-condition being checked
     * @param result the result of the pre-condition check (true if satisfied, false otherwise)
     */
    void fireOnRulePreConditionCheck(Rule rule, Condition condition, boolean result);

    /**
     * Fires an event when a rule's condition check is performed.
     *
     * @param rule the Rule on which the condition check is being performed
     * @param condition the Condition representing the condition being checked
     * @param result the result of the condition check (true if satisfied, false otherwise)
     */
    void fireOnRuleConditionCheck(Rule rule, Condition condition, boolean result);

    /**
     * Fires an event when a specific action is executed for a given rule within the framework.
     *
     * @param rule the Rule for which the action is being executed
     * @param action the Action being executed
     */
    void fireOnRuleAction(Rule rule, Action action);

    /**
     * Fires an event when an otherwise action is executed for a given rule within the framework.
     *
     * @param rule the Rule for which the otherwise action is being executed
     * @param action the Action representing the otherwise action being executed
     */
    void fireOnRuleOtherwiseAction(Rule rule, Action action);

    /**
     * Fires an event when an error occurs during the execution of a Rule.
     *
     * @param rule the Rule where the error occurred
     * @param e the Exception that was thrown
     */
    void fireOnRuleError(Rule rule, Exception e);

    /**
     * Fires an event when a rule has completed its execution.
     *
     * @param rule the Rule that has completed execution
     * @param result the result of the rule execution
     */
    void fireOnRuleEnd(Rule rule, RuleResult result);

    /**
     * Notifies listeners that a RuleSet has started.
     *
     * @param ruleSet the RuleSet that has started
     * @param ruleSetScope the scope of the RuleSet
     */
    void fireOnRuleSetStart(RuleSet<?> ruleSet, NamedScope ruleSetScope);


    /**
     * Fires an event when a RuleSet's input is checked for validation by the framework.
     *
     * @param ruleSet the RuleSet for which the input is being checked
     * @param violations the RuleViolations object containing any validation errors found during the input check
     */
    void fireOnRuleSetInputCheck(RuleSet<?> ruleSet, RuleViolations violations);

    /**
     *
     * Fires an event when a pre-condition check is performed on a RuleSet.
     *
     * @param ruleSet the RuleSet on which the pre-condition check is being performed
     * @param condition the Condition representing the pre-condition being checked
     * @param result the result of the pre-condition check (true if satisfied, false otherwise)
     */
    void fireOnRuleSetPreConditionCheck(RuleSet<?> ruleSet, Condition condition, boolean result);

    /**
     * Fires an event to initialize a RuleSet with a given initializer action.
     *
     * @param ruleSet the RuleSet to be initialized
     * @param initializer the Action to initialize the RuleSet with
     */
    void fireOnRuleSetInitializer(RuleSet<?> ruleSet, Action initializer);

    /**
     * Fires an event when a rule within a rule set is run.
     *
     * @param ruleSet the RuleSet containing the rule being executed
     * @param rule the Rule being executed
     * @param executionResult the result of the rule execution
     * @param status the status of the RuleSet execution
     */
    void fireOnRuleSetRuleRun(RuleSet<?> ruleSet, Rule rule, RuleResult executionResult, RuleSetExecutionStatus status);

    /**
     * Fires an event when a rule set has stopped its execution.
     *
     * @param ruleSet the RuleSet that has completed its execution
     * @param stopCondition the Condition that caused the rule set to stop
     * @param status the status of the RuleSet execution
     */
    void fireOnRuleSetStop(RuleSet<?> ruleSet, Condition stopCondition, RuleSetExecutionStatus status);

    /**
     * Fires an event when the finalizer action is executed for a RuleSet.
     *
     * @param ruleSet the RuleSet for which the finalizer action is being executed
     * @param finalizer the Action representing the finalizer action being executed
     */
    void fireOnRuleSetFinalizer(RuleSet<?> ruleSet, Action finalizer);

    /**
     * Fires an event when a rule set has produced a result after execution.
     *
     * @param ruleSet the RuleSet that has completed its execution
     * @param resultExtractor a Function used to extract the result from the RuleSet
     * @param status the status of the RuleSet execution
     */
    void fireOnRuleSetResult(RuleSet<?> ruleSet, Function<?> resultExtractor, RuleSetExecutionStatus status);

    /**
     * Fires an event when a RuleSet has completed its execution.
     *
     * @param ruleSet the RuleSet that has completed execution
     * @param ruleSetScope the scope of the RuleSet
     * @param status the status of the RuleSet execution
     */
    void fireOnRuleSetEnd(RuleSet<?> ruleSet, NamedScope ruleSetScope, RuleSetExecutionStatus status);

    /**
     *
     * Fires an event when an error occurs in the execution of a RuleSet.
     *
     * @param rule the RuleSet where the error occurred
     * @param status the status of the RuleSet execution
     * @param e the Exception that was thrown
     */
    void fireOnRuleSetError(RuleSet<?> rule, RuleSetExecutionStatus status, Exception e);
}