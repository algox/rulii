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
package org.rulii.rule;

import org.rulii.bind.NamedScope;
import org.rulii.model.action.Action;
import org.rulii.model.condition.Condition;

import java.util.EventListener;

/**
 * This interface represents a listener for events related to rule execution within the framework.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public interface RuleListener extends EventListener {

    /**
     * This method is called when a rule is about to start execution.
     *
     * @param rule the rule that is about to start execution
     * @param ruleScope the named scope associated with the rule
     */
    default void onRuleStart(Rule rule, NamedScope ruleScope) {}

    /**
     * This method is called before performing a pre-condition check for a rule.
     *
     * @param rule the rule being evaluated
     * @param condition the condition for the rule being checked
     * @param result the result of the evaluation
     */
    default void onPreConditionCheck(Rule rule, Condition condition, boolean result) {}

    /**
     * This method is called when a condition check is being performed for a rule.
     *
     * @param rule the rule for which the condition is being checked
     * @param condition the condition being evaluated
     * @param result the result of the condition evaluation
     */
    default void onConditionCheck(Rule rule, Condition condition, boolean result) {};

    /**
     * This method is called when an action is triggered in relation to a rule execution.
     *
     * @param rule the rule associated with the action
     * @param action the action being triggered
     */
    default void onAction(Rule rule, Action action) {};

    /**
     * This method is called when an otherwise action is triggered in relation to a rule execution if no other conditions are met.
     *
     * @param rule the rule associated with the otherwise action
     * @param action the otherwise action being triggered
     */
    default void onOtherwiseAction(Rule rule, Action action) {};

    /**
     * This method is called when an error occurs during the execution of a rule.
     *
     * @param rule the rule that encountered the error
     * @param e the exception that was thrown
     */
    default void onRuleError(Rule rule, Exception e) {};

    /**
     * This method is called when a rule has completed its execution.
     *
     * @param rule the rule that has completed execution
     * @param result the result of the rule execution
     * @param ruleScope the named scope associated with the rule
     */
    default void onRuleEnd(Rule rule, RuleResult result, NamedScope ruleScope) {}
}
