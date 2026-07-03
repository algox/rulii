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

import org.rulii.model.*;
import org.rulii.model.Runnable;
import org.rulii.model.action.Action;
import org.rulii.model.function.Function;
import org.rulii.ruleflow.command.RuleFlowCommand;

import java.util.List;

/**
 * Represents a composable and executable flow of rules. Combines synchronous and asynchronous
 * execution capabilities with definable metadata and scoped operations.
 *
 * @param <T> the result type returned after the execution of this flow.
 *
 * @author Max Arulananthan
 * @since 2.0
 */
public interface RuleFlow<T> extends Runnable<T>, AsyncRunnable<T>, Definable<RuleFlowDefinition>, ScopeDefining {

    /**
     * Entry point for the fluent flow-building DSL.
     *
     * @return a new {@link DefaultRuleFlowBuilder}; never null.
     */
    static DefaultRuleFlowBuilder builder() {
        return RuleFlowBuilderBuilder.getInstance().build();
    }

    /**
     * Returns the declared input parameters for this flow.
     *
     * @return immutable list; never null, may be empty.
     */
    List<InputParameter<?>> getInputParameters();

    /**
     * Returns the finalizer action, or {@code null} if none was registered.
     *
     * @return finalizer action; may be null.
     */
    Action getFinalizer();

    /**
     * Provides the result extractor function for the rule flow,
     * allowing custom transformation or extraction of the execution result.
     *
     * @return a {@link Function} representing the result extractor, or {@code null} if
     *         {@code returning(Function)} was never called (the flow returns the {@code RuleContext}).
     */
    Function<?> getResultExtractor();

    /**
     * Retrieves the list of commands associated with the rule flow.
     *
     * @return a list of {@link RuleFlowCommand} objects representing
     *         the executable steps of the rule flow; never null, but may be empty.
     */
    List<RuleFlowCommand> getCommands();

    /**
     * Returns the flow-level global exception handler, or {@code null} if none was registered.
     *
     * @return global handler; may be null.
     */
    RuleFlowExceptionHandler getGlobalHandler();
}
