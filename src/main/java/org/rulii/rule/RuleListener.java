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
package org.rulii.rule;

import org.rulii.bind.NamedScope;
import org.rulii.model.action.Action;
import org.rulii.model.condition.Condition;

import java.util.EventListener;

public interface RuleListener extends EventListener {

    default void onRuleStart(Rule rule, NamedScope ruleScope) {}

    default void onPreConditionCheck(Rule rule, Condition condition, boolean result) {}

    default void onConditionCheck(Rule rule, Condition condition, boolean result) {};

    default void onAction(Rule rule, Action action) {};

    default void onOtherwiseAction(Rule rule, Action action) {};

    default void onRuleError(Rule rule, Exception e) {};

    default void onRuleEnd(Rule rule, RuleResult result, NamedScope ruleScope) {}
}
