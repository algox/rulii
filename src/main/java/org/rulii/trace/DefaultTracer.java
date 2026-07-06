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
import org.rulii.lib.apache.commons.logging.Log;
import org.rulii.lib.apache.commons.logging.LogFactory;
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

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;


/**
 * Default implementation of {@link Tracer}.
 *
 * <p>Maintains separate listener sets for rules, rule sets, and rule flows and dispatches
 * each fire-method to the appropriate set.
 *
 * <p>A listener that throws is logged and skipped, not propagated — a bug in one listener must
 * never affect other listeners, nor be mistaken by the caller for a failure of the rule/ruleset/
 * ruleflow execution that triggered the event.
 *
 * <p>A single {@code Tracer} instance is propagated to derived {@link org.rulii.context.RuleContext}s
 * (e.g. across async rule-flow steps) and so can be fired from multiple threads concurrently;
 * the listener sets are {@link java.util.concurrent.CopyOnWriteArraySet} for thread-safe
 * add/remove/iteration without explicit locking — a good fit given listeners are typically
 * registered up front and rarely added or removed once execution is underway.
 *
 * @author Max Arulananthan
 * @since 1.0
 *
 */
public class DefaultTracer implements Tracer {

    private static final Log logger = LogFactory.getLog(DefaultTracer.class);

    private final Set<RuleListener> ruleListeners = new CopyOnWriteArraySet<>();
    private final Set<RuleSetListener> ruleSetListeners = new CopyOnWriteArraySet<>();
    private final Set<RuleFlowListener> ruleFlowListeners = new CopyOnWriteArraySet<>();

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
    public boolean removeListener(RuliiListener listener) {
        Assert.notNull(listener, "listener cannot be null.");
        boolean removedFromRules = removeListener((RuleListener) listener);
        boolean removedFromRuleSets = removeListener((RuleSetListener) listener);
        boolean removedFromRuleFlows = removeListener((RuleFlowListener) listener);
        return removedFromRules || removedFromRuleSets || removedFromRuleFlows;
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
        fireEvent(ruleListeners, listener -> listener.onRuleStart(rule));
    }

    @Override
    public void fireOnRulePreConditionCheck(Rule rule, Condition condition, boolean result) {
        fireEvent(ruleListeners, listener -> listener.onPreConditionCheck(rule, condition, result));
    }

    @Override
    public void fireOnRuleConditionCheck(Rule rule, Condition condition, boolean result) {
        fireEvent(ruleListeners, listener -> listener.onGiven(rule, condition, result));
    }

    @Override
    public void fireOnRuleAction(Rule rule, Action action) {
        fireEvent(ruleListeners, listener -> listener.onThen(rule, action));
    }

    @Override
    public void fireOnRuleOtherwiseAction(Rule rule, Action action) {
        fireEvent(ruleListeners, listener -> listener.onOtherwise(rule, action));
    }

    @Override
    public void fireOnRuleError(Rule rule, Exception e) {
        fireEvent(ruleListeners, listener -> listener.onRuleError(rule, e));
    }

    @Override
    public void fireOnRuleEnd(Rule rule, RuleResult result) {
        fireEvent(ruleListeners, listener -> listener.onRuleEnd(rule, result));
    }

    @Override
    public void fireOnRuleSetStart(RuleSet<?> ruleSet, NamedScope ruleSetScope) {
        fireEvent(ruleSetListeners, listener -> listener.onRuleSetStart(ruleSet, ruleSetScope));
    }

    @Override
    public void fireOnRuleSetPreConditionCheck(RuleSet<?> ruleSet, Condition condition, boolean result) {
        fireEvent(ruleSetListeners, listener -> listener.onRuleSetPreConditionCheck(ruleSet, condition, result));
    }

    @Override
    public void fireOnRuleSetInitializer(RuleSet<?> ruleSet, Action initializer) {
        fireEvent(ruleSetListeners, listener -> listener.onRuleSetInitializer(ruleSet, initializer));
    }

    @Override
    public void fireOnRuleSetFinalizer(RuleSet<?> ruleSet, Action finalizer) {
        fireEvent(ruleSetListeners, listener -> listener.onRuleSetFinalizer(ruleSet, finalizer));
    }

    @Override
    public void fireOnRuleSetRuleRun(RuleSet<?> ruleSet, Rule rule, RuleResult executionResult, RuleSetExecutionStatus status) {
        fireEvent(ruleSetListeners, listener -> listener.onRuleSetRuleRun(ruleSet, rule, executionResult, status));
    }

    @Override
    public void fireOnRuleSetStop(RuleSet<?> ruleSet, Condition stopCondition, RuleSetExecutionStatus status) {
        fireEvent(ruleSetListeners, listener -> listener.onRuleSetStop(ruleSet, stopCondition, status));
    }

    @Override
    public void fireOnRuleSetResult(RuleSet<?> ruleSet, Function<?> resultExtractor, RuleSetExecutionStatus status) {
        fireEvent(ruleSetListeners, listener -> listener.onRuleSetResult(ruleSet, resultExtractor, status));
    }

    @Override
    public void fireOnRuleSetEnd(RuleSet<?> ruleSet, NamedScope ruleSetScope, RuleSetExecutionStatus status) {
        fireEvent(ruleSetListeners, listener -> listener.onRuleSetEnd(ruleSet, ruleSetScope, status));
    }

    @Override
    public void fireOnRuleSetError(RuleSet<?> ruleSet, RuleSetExecutionStatus status, Exception e) {
        fireEvent(ruleSetListeners, listener -> listener.onRuleSetError(ruleSet, status, e));
    }

    @Override
    public void fireOnRuleFlowStart(RuleFlow<?> ruleFlow, NamedScope ruleFlowScope) {
        fireEvent(ruleFlowListeners, listener -> listener.onRuleFlowStart(ruleFlow, ruleFlowScope));
    }

    @Override
    public void fireOnRuleFlowCommandExecuted(RuleFlow<?> ruleFlow, RuleFlowCommand command) {
        fireEvent(ruleFlowListeners, listener -> listener.onRuleFlowCommandExecuted(ruleFlow, command));
    }

    @Override
    public void fireOnRuleFlowEarlyExit(RuleFlow<?> ruleFlow, Object result) {
        fireEvent(ruleFlowListeners, listener -> listener.onRuleFlowEarlyExit(ruleFlow, result));
    }

    @Override
    public void fireOnRuleFlowExceptionHandled(RuleFlow<?> ruleFlow, Exception e, boolean stepLevel) {
        fireEvent(ruleFlowListeners, listener -> listener.onRuleFlowExceptionHandled(ruleFlow, e, stepLevel));
    }

    @Override
    public void fireOnRuleFlowFinalizer(RuleFlow<?> ruleFlow, Action finalizer) {
        fireEvent(ruleFlowListeners, listener -> listener.onRuleFlowFinalizer(ruleFlow, finalizer));
    }

    @Override
    public void fireOnRuleFlowResult(RuleFlow<?> ruleFlow, Function<?> resultExtractor) {
        fireEvent(ruleFlowListeners, listener -> listener.onRuleFlowResult(ruleFlow, resultExtractor));
    }

    @Override
    public void fireOnRuleFlowError(RuleFlow<?> ruleFlow, Exception e) {
        fireEvent(ruleFlowListeners, listener -> listener.onRuleFlowError(ruleFlow, e));
    }

    @Override
    public void fireOnRuleFlowEnd(RuleFlow<?> ruleFlow, NamedScope ruleFlowScope) {
        fireEvent(ruleFlowListeners, listener -> listener.onRuleFlowEnd(ruleFlow, ruleFlowScope));
    }

    /**
     * Notifies every listener in {@code listeners}, isolating each call so that one listener's
     * exception cannot prevent the remaining listeners from being notified, and cannot propagate
     * into the rule/ruleset/ruleflow execution that triggered the event.
     *
     * @param listeners the listeners to notify.
     * @param action    the per-listener notification to perform.
     */
    private <T> void fireEvent(Set<T> listeners, Consumer<T> action) {
        for (T listener : listeners) {
            try {
                action.accept(listener);
            } catch (Exception e) {
                logger.warn("Tracer listener [" + listener + "] threw an exception handling an event. Ignoring.", e);
            }
        }
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
