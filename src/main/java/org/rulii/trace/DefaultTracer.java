/*
 * This software is licensed under the Apache 2 license, quoted below.
 *
 * Copyright (c) 1999-2026, Algorithmx Inc.
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
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.action.Action;
import org.rulii.model.condition.Condition;
import org.rulii.model.function.Function;
import org.rulii.rule.Rule;
import org.rulii.rule.RuleListener;
import org.rulii.rule.RuleResult;
import org.rulii.ruleflow.RuleFlow;
import org.rulii.ruleflow.RuleFlowListener;
import org.rulii.ruleflow.command.RuleFlowCommand;
import org.rulii.ruleset.RuleSet;
import org.rulii.ruleset.RuleSetExecutionStatus;
import org.rulii.ruleset.RuleSetListener;

import java.util.LinkedHashSet;
import java.util.Set;


/**
 * Default implementation of {@link Tracer}.
 *
 * <p>Maintains separate listener sets for rules, rule sets, and rule flows and dispatches
 * each fire-method to the appropriate set.
 *
 * @author Max Arulananthan
 * @since 1.0
 *
 */
public class DefaultTracer implements Tracer {

    private final Set<RuleListener> ruleListeners = new LinkedHashSet<>();
    private final Set<RuleSetListener> ruleSetListeners = new LinkedHashSet<>();
    private final Set<RuleFlowListener> ruleFlowListeners = new LinkedHashSet<>();

    public DefaultTracer() {
        super();
    }

    @Override
    public void addListener(RuliiListener listener) {
        Assert.notNull(listener, "listener cannot be null.");
        addListener((RuleListener) listener);
        addListener((RuleSetListener) listener);
        addListener((RuleFlowListener) listener);
    }

    @Override
    public void removeListener(RuliiListener listener) {
        Assert.notNull(listener, "listener cannot be null.");
        removeListener((RuleListener) listener);
        removeListener((RuleSetListener) listener);
        removeListener((RuleFlowListener) listener);
    }

    @Override
    public void addListener(RuleListener listener) {
        Assert.notNull(listener, "listener cannot be null.");
        ruleListeners.add(listener);
    }

    @Override
    public boolean removeListener(RuleListener listener) {
        Assert.notNull(listener, "listener cannot be null.");
        return ruleListeners.remove(listener);
    }

    @Override
    public void addListener(RuleSetListener listener) {
        Assert.notNull(listener, "listener cannot be null.");
        ruleSetListeners.add(listener);
    }

    @Override
    public boolean removeListener(RuleSetListener listener) {
        Assert.notNull(listener, "listener cannot be null.");
        return ruleSetListeners.remove(listener);
    }

    @Override
    public void addListener(RuleFlowListener listener) {
        Assert.notNull(listener, "listener cannot be null.");
        ruleFlowListeners.add(listener);
    }

    @Override
    public boolean removeListener(RuleFlowListener listener) {
        Assert.notNull(listener, "listener cannot be null.");
        return ruleFlowListeners.remove(listener);
    }

    @Override
    public void clear() {
        ruleListeners.clear();
        ruleSetListeners.clear();
        ruleFlowListeners.clear();
    }

    @Override
    public void fireOnRuleStart(Rule rule) {
        ruleListeners.forEach(listener -> listener.onRuleStart(rule));
    }

    @Override
    public void fireOnRulePreConditionCheck(Rule rule, Condition condition, boolean result) {
        ruleListeners.forEach(listener -> listener.onPreConditionCheck(rule, condition, result));
    }

    @Override
    public void fireOnRuleConditionCheck(Rule rule, Condition condition, boolean result) {
        ruleListeners.forEach(listener -> listener.onGiven(rule, condition, result));
    }

    @Override
    public void fireOnRuleAction(Rule rule, Action action) {
        ruleListeners.forEach(listener -> listener.onThen(rule, action));
    }

    @Override
    public void fireOnRuleOtherwiseAction(Rule rule, Action action) {
        ruleListeners.forEach(listener -> listener.onOtherwise(rule, action));
    }

    @Override
    public void fireOnRuleError(Rule rule, Exception e) {
        ruleListeners.forEach(listener -> listener.onRuleError(rule, e));
    }

    @Override
    public void fireOnRuleEnd(Rule rule, RuleResult result) {
        ruleListeners.forEach(listener -> listener.onRuleEnd(rule, result));
    }

    @Override
    public void fireOnRuleSetStart(RuleSet<?> ruleSet, NamedScope ruleSetScope) {
        ruleSetListeners.forEach(listener -> listener.onRuleSetStart(ruleSet, ruleSetScope));
    }

    @Override
    public void fireOnRuleSetPreConditionCheck(RuleSet<?> ruleSet, Condition condition, boolean result) {
        ruleSetListeners.forEach(listener -> listener.onRuleSetPreConditionCheck(ruleSet, condition, result));
    }

    @Override
    public void fireOnRuleSetInitializer(RuleSet<?> ruleSet, Action initializer) {
        ruleSetListeners.forEach(listener -> listener.onRuleSetInitializer(ruleSet, initializer));
    }

    @Override
    public void fireOnRuleSetFinalizer(RuleSet<?> ruleSet, Action finalizer) {
        ruleSetListeners.forEach(listener -> listener.onRuleSetFinalizer(ruleSet, finalizer));
    }

    @Override
    public void fireOnRuleSetRuleRun(RuleSet<?> ruleSet, Rule rule, RuleResult executionResult, RuleSetExecutionStatus status) {
        ruleSetListeners.forEach(listener -> listener.onRuleSetRuleRun(ruleSet, rule, executionResult, status));
    }

    @Override
    public void fireOnRuleSetStop(RuleSet<?> ruleSet, Condition stopCondition, RuleSetExecutionStatus status) {
        ruleSetListeners.forEach(listener -> listener.onRuleSetStop(ruleSet, stopCondition, status));
    }

    @Override
    public void fireOnRuleSetResult(RuleSet<?> ruleSet, Function<?> resultExtractor, RuleSetExecutionStatus status) {
        ruleSetListeners.forEach(listener -> listener.onRuleSetResult(ruleSet, resultExtractor, status));
    }

    @Override
    public void fireOnRuleSetEnd(RuleSet<?> ruleSet, NamedScope ruleSetScope, RuleSetExecutionStatus status) {
        ruleSetListeners.forEach(listener -> listener.onRuleSetEnd(ruleSet, ruleSetScope, status));
    }

    @Override
    public void fireOnRuleSetError(RuleSet<?> ruleSet, RuleSetExecutionStatus status, Exception e) {
        ruleSetListeners.forEach(listener -> listener.onRuleSetError(ruleSet, status, e));
    }

    @Override
    public void fireOnRuleFlowStart(RuleFlow<?> ruleFlow, NamedScope ruleFlowScope) {
        ruleFlowListeners.forEach(listener -> listener.onRuleFlowStart(ruleFlow, ruleFlowScope));
    }

    @Override
    public void fireOnRuleFlowCommandExecuted(RuleFlow<?> ruleFlow, RuleFlowCommand command) {
        ruleFlowListeners.forEach(listener -> listener.onRuleFlowCommandExecuted(ruleFlow, command));
    }

    @Override
    public void fireOnRuleFlowEarlyExit(RuleFlow<?> ruleFlow, Object result) {
        ruleFlowListeners.forEach(listener -> listener.onRuleFlowEarlyExit(ruleFlow, result));
    }

    @Override
    public void fireOnRuleFlowExceptionHandled(RuleFlow<?> ruleFlow, Exception e, boolean stepLevel) {
        ruleFlowListeners.forEach(listener -> listener.onRuleFlowExceptionHandled(ruleFlow, e, stepLevel));
    }

    @Override
    public void fireOnRuleFlowFinalizer(RuleFlow<?> ruleFlow, Action finalizer) {
        ruleFlowListeners.forEach(listener -> listener.onRuleFlowFinalizer(ruleFlow, finalizer));
    }

    @Override
    public void fireOnRuleFlowResult(RuleFlow<?> ruleFlow, Function<?> resultExtractor) {
        ruleFlowListeners.forEach(listener -> listener.onRuleFlowResult(ruleFlow, resultExtractor));
    }

    @Override
    public void fireOnRuleFlowError(RuleFlow<?> ruleFlow, Exception e) {
        ruleFlowListeners.forEach(listener -> listener.onRuleFlowError(ruleFlow, e));
    }

    @Override
    public void fireOnRuleFlowEnd(RuleFlow<?> ruleFlow, NamedScope ruleFlowScope) {
        ruleFlowListeners.forEach(listener -> listener.onRuleFlowEnd(ruleFlow, ruleFlowScope));
    }

    @Override
    public String toString() {
        return "DefaultTracer{" +
                "ruleListeners=" + ruleListeners.size() +
                ", ruleSetListeners=" + ruleSetListeners.size() +
                ", ruleFlowListeners=" + ruleFlowListeners.size() +
                '}';
    }
}
