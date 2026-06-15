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

import org.rulii.context.RuleContext;
import org.rulii.model.UnrulyException;

import java.util.concurrent.CompletableFuture;

/**
 * Strategy that drives a {@link RuleFlow} execution.
 *
 * <p>Mirrors the {@code RuleSetExecutionStrategy} pattern. Use {@link #build()} for
 * sequential execution and {@link #buildAsync()} for async wrapping.
 *
 * @param <T> the result type.
 *
 * @author Max Arulananthan
 * @since 2.0
 */
public interface RuleFlowExecutionStrategy<T> {

    /**
     * Returns a new sequential strategy.
     *
     * @param <T> result type.
     * @return strategy; never null.
     */
    static <T> RuleFlowExecutionStrategy<T> build() {
        return new DefaultRuleFlowExecutionStrategy<>();
    }

    /**
     * Returns a new async strategy that wraps the sequential strategy.
     *
     * @param <T> result type.
     * @return async strategy; never null.
     */
    static <T> RuleFlowExecutionStrategy<CompletableFuture<T>> buildAsync() {
        return new AsyncRuleFlowExecutionStrategy<>();
    }

    /**
     * Executes the given flow within the provided context.
     *
     * @param ruleFlow the flow to execute; must not be null.
     * @param ruleContext the execution context; must not be null.
     * @return the flow result.
     * @throws UnrulyException on execution errors.
     */
    T run(RuleFlow<?> ruleFlow, RuleContext ruleContext) throws UnrulyException;
}
