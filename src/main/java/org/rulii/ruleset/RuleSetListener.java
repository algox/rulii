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

import org.rulii.bind.NamedScope;
import org.rulii.model.action.Action;
import org.rulii.model.condition.Condition;
import org.rulii.model.function.Function;
import org.rulii.rule.Rule;
import org.rulii.rule.RuleResult;

public interface RuleSetListener {

    void onRuleSetStart(RuleSet<?> ruleSet, NamedScope ruleSetScope);

    void onRuleSetPreConditionCheck(RuleSet<?> ruleSet, Condition condition, boolean result);

    void onRuleSetInitializer(RuleSet<?> ruleSet, Action initializer);

    void onRuleSetRuleRun(RuleSet<?> ruleSet, Rule rule, RuleResult executionResult, RuleSetExecutionStatus status);

    void onRuleSetStop(RuleSet<?> ruleSet, Condition stopCondition, RuleSetExecutionStatus status);

    void onRuleSetFinalizer(RuleSet<?> ruleSet, Action finalizer);

    void onRuleSetResult(RuleSet<?> ruleSet, Function<?> resultExtractor, RuleSetExecutionStatus status);

    void onRuleSetEnd(RuleSet<?> ruleSet, NamedScope ruleSetScope, RuleSetExecutionStatus status);

    void onRuleSetError(RuleSet<?> ruleSet, RuleSetExecutionStatus status, Exception e);
}
