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
package org.rulii.ruleflow;

import org.rulii.bind.NamedScope;
import org.rulii.model.action.Action;
import org.rulii.model.function.Function;
import org.rulii.ruleflow.command.RuleFlowCommand;

/**
 * Listener interface for observing events during {@link RuleFlow} pipeline execution.
 *
 * <p>All methods have no-op defaults so implementations only override what they need.
 *
 * @author Max Arulananthan
 * @since 2.0
 */
public interface RuleFlowListener {

    /**
     * Invoked after the flow scope is created and before any commands run.
     *
     * @param ruleFlow      the flow that started; never null.
     * @param ruleFlowScope the scope pushed for this run; never null.
     */
    default void onRuleFlowStart(RuleFlow<?> ruleFlow, NamedScope ruleFlowScope) {}

    /**
     * Invoked after each pipeline command completes normally.
     * Not fired when the command throws — use {@link #onRuleFlowEarlyExit} or
     * {@link #onRuleFlowError} for those cases.
     *
     * @param ruleFlow the owning flow; never null.
     * @param command  the command that just executed; never null.
     */
    default void onRuleFlowCommandExecuted(RuleFlow<?> ruleFlow, RuleFlowCommand command) {}

    /**
     * Invoked when an {@code exit()} command terminates the pipeline early.
     * Fires before the flow scope is removed.
     *
     * @param ruleFlow the owning flow; never null.
     * @param result the result value (may be the RuleContext for a no-arg exit).
     */
    default void onRuleFlowEarlyExit(RuleFlow<?> ruleFlow, Object result) {}

    /**
     * Invoked when an exception thrown by a step is successfully handled by
     * a step-level or flow-level exception handler.
     *
     * @param ruleFlow  the owning flow; never null.
     * @param e the exception that was caught; never null.
     * @param stepLevel {@code true} if handled by a step handler, {@code false} if by the global handler.
     */
    default void onRuleFlowExceptionHandled(RuleFlow<?> ruleFlow, Exception e, boolean stepLevel) {}

    /**
     * Invoked after the finalizer action completes successfully.
     *
     * @param ruleFlow the owning flow; never null.
     * @param finalizer the finalizer action; never null.
     */
    default void onRuleFlowFinalizer(RuleFlow<?> ruleFlow, Action finalizer) {}

    /**
     * Invoked after the result extractor function runs.
     *
     * @param ruleFlow the owning flow; never null.
     * @param resultExtractor the function used to extract the result; never null.
     */
    default void onRuleFlowResult(RuleFlow<?> ruleFlow, Function<?> resultExtractor) {}

    /**
     * Invoked when an unhandled exception escapes the pipeline.
     *
     * @param ruleFlow the owning flow; never null.
     * @param e the exception; never null.
     */
    default void onRuleFlowError(RuleFlow<?> ruleFlow, Exception e) {}

    /**
     * Invoked after the flow scope is removed. Always fires, even on early exit or error.
     *
     * @param ruleFlow the flow that completed; never null.
     * @param ruleFlowScope the scope that was used; never null.
     */
    default void onRuleFlowEnd(RuleFlow<?> ruleFlow, NamedScope ruleFlowScope) {}
}
