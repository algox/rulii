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
 * Async {@link RuleFlowExecutionStrategy} — wraps the sequential strategy in a
 * {@link CompletableFuture#supplyAsync} using the context's executor service.
 *
 * @param <T> the flow result type (not the future wrapper type).
 *
 * @author Max Arulananthan
 * @since 2.0
 */
class AsyncRuleFlowExecutionStrategy<T> extends RuleFlowExecutionStrategyTemplate<CompletableFuture<T>> {

    AsyncRuleFlowExecutionStrategy() {
        super();
    }

    @Override
    @SuppressWarnings("unchecked")
    public CompletableFuture<T> run(RuleFlow<?> ruleFlow, RuleContext ruleContext) throws UnrulyException {
        return CompletableFuture.supplyAsync(() -> {
            RuleFlowExecutionStrategy<T> strategy = RuleFlowExecutionStrategy.build();

            try {
                return strategy.run(ruleFlow, ruleContext);
            } catch (RuleFlowReturn r) {
                return (T) r.getResult();
            }

        }, ruleContext.getExecutorService());
    }
}
