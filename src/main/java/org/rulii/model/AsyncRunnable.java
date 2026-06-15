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
package org.rulii.model;

import org.rulii.context.RuleContext;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Capability interface for constructs that support asynchronous execution.
 *
 * <p>Implemented by {@link org.rulii.ruleset.RuleSet} and {@link org.rulii.ruleflow.RuleFlow}.
 * Allows callers to treat any async-capable construct uniformly without knowing the
 * concrete type.
 *
 * @param <T> the result type produced when execution completes.
 *
 * @author Max Arulananthan
 * @since 2.0
 */
public interface AsyncRunnable<T> {

    /**
     * Executes this construct asynchronously using the given context.
     *
     * @param ruleContext the execution context; must not be null.
     * @return a future that completes with the result.
     */
    CompletableFuture<T> runAsync(RuleContext ruleContext);

    /**
     * Executes this construct asynchronously with a timeout.
     *
     * @param ruleContext the execution context; must not be null.
     * @param timeout     maximum time to wait for completion.
     * @param timeUnit    time unit for the timeout; must not be null.
     * @return a future that completes with the result, or fails if the timeout elapses.
     */
    CompletableFuture<T> runAsync(RuleContext ruleContext, long timeout, TimeUnit timeUnit);
}
