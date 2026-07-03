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
package org.rulii.ruleflow.command;

import org.rulii.lib.spring.util.Assert;
import org.rulii.model.UnrulyException;
import org.rulii.ruleflow.RuleFlowExecutionContext;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Pipeline command that blocks until <em>any one</em> of the named {@link CompletableFuture}
 * bindings completes (successfully or exceptionally).
 *
 * <p>Individual futures are not replaced after completion — they remain as
 * {@code CompletableFuture<T>} bindings. If the first-to-complete future completed
 * exceptionally, the exception is re-thrown as an {@link UnrulyException}. Remaining
 * in-flight futures continue running in the background.
 *
 * <p>A {@link TimeoutException} is wrapped in {@link UnrulyException}; futures are
 * <em>not</em> cancelled on timeout.
 *
 * @author Max Arulananthan
 * @since 2.0
 */
public class AwaitAnyCommand implements RuleFlowCommand {

    private final long timeout;
    private final TimeUnit timeUnit;
    private final String[] bindingNames;

    public AwaitAnyCommand(long timeout, TimeUnit timeUnit, String... bindingNames) {
        super();
        Assert.notNull(timeUnit, "timeUnit cannot be null.");
        Assert.notNull(bindingNames, "bindingNames cannot be null.");
        Assert.isTrue(bindingNames.length > 0, "At least one binding name must be provided.");
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        this.bindingNames = Arrays.copyOf(bindingNames, bindingNames.length);
    }

    @Override
    public void execute(RuleFlowExecutionContext ctx) {
        CompletableFuture<?>[] futures = resolveFutures(ctx);
        try {
            CompletableFuture.anyOf(futures).get(timeout, timeUnit);
        } catch (TimeoutException e) {
            throw new UnrulyException("awaitAny " + Arrays.toString(bindingNames)
                    + " timed out after " + timeout + " " + timeUnit + ".", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw cause instanceof UnrulyException ue ? ue
                    : new UnrulyException("Async step failed: " + Arrays.toString(bindingNames) + ".", cause);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new UnrulyException("awaitAny " + Arrays.toString(bindingNames) + " interrupted.", e);
        }
    }

    private CompletableFuture<?>[] resolveFutures(RuleFlowExecutionContext ctx) {
        CompletableFuture<?>[] futures = new CompletableFuture<?>[bindingNames.length];
        for (int i = 0; i < bindingNames.length; i++) {
            String name = bindingNames[i];
            Object value = ctx.getRuleContext().getBindings().getValue(name);
            if (!(value instanceof CompletableFuture<?> f))
                throw new UnrulyException("Binding [" + name + "] is not a CompletableFuture; found: "
                        + (value == null ? "null" : value.getClass().getName()));
            futures[i] = f;
        }
        return futures;
    }

    public long getTimeout() {
        return timeout;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public String[] getBindingNames() {
        return Arrays.copyOf(bindingNames, bindingNames.length);
    }

}
