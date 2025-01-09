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
package org.rulii.trace;

import org.rulii.bind.Binding;
import org.rulii.bind.BindingListener;
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

public interface Tracer {

    static TracerBuilder builder() {
        return new TracerBuilder();
    }

    void addBindingListener(BindingListener listener);

    boolean removeBindingListener(BindingListener listener);

    void addRuleListener(RuleListener listener);

    boolean removeRuleListener(RuleListener listener);

    void addRuleSetListener(RuleSetListener listener);

    boolean removeRuleSetListener(RuleSetListener listener);

    void clear();

    void fireOnBind(Binding<?> binding);

    void fireOnScopeAdd(String name);

    void fireOnScopeRemove(NamedScope scope);

    void fireOnChange(Binding<?> binding, Object oldValue, Object newValue);

    void fireOnRuleStart(Rule rule, NamedScope ruleScope);

    void fireOnRulePreConditionCheck(Rule rule, Condition condition, boolean result);

    void fireOnRuleConditionCheck(Rule rule, Condition condition, boolean result);

    void fireOnRuleAction(Rule rule, Action action);

    void fireOnRuleOtherwiseAction(Rule rule, Action action);

    void fireOnRuleError(Rule rule, Exception e);

    void fireOnRuleEnd(Rule rule, RuleResult result, NamedScope ruleScope);

    void fireOnRuleSetStart(RuleSet<?> ruleSet, NamedScope ruleSetScope);

    void fireOnRuleSetPreConditionCheck(RuleSet<?> ruleSet, Condition condition, boolean result);

    void fireOnRuleSetInitializer(RuleSet<?> ruleSet, Action initializer);

    void fireOnRuleSetRuleRun(RuleSet<?> ruleSet, Rule rule, RuleResult executionResult, RuleSetExecutionStatus status);

    void fireOnRuleSetStop(RuleSet<?> ruleSet, Condition stopCondition, RuleSetExecutionStatus status);

    void fireOnRuleSetFinalizer(RuleSet<?> ruleSet, Action finalizer);

    void fireOnRuleSetResult(RuleSet<?> ruleSet, Function<?> resultExtractor, RuleSetExecutionStatus status);

    void fireOnRuleSetEnd(RuleSet<?> ruleSet, NamedScope ruleSetScope, RuleSetExecutionStatus status);

    void fireOnRuleSetError(RuleSet<?> rule, RuleSetExecutionStatus status, Exception e);
}