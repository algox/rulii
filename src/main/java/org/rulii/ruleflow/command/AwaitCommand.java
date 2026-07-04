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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Pipeline command that blocks until a named {@link CompletableFuture} binding completes.
 *
 * <p>The binding is not replaced after completion — it remains a {@code CompletableFuture<T>}
 * in the scope. Subsequent steps can call {@code future.getNow(null)} or {@code future.join()}
 * to retrieve the computed value.
 *
 * <p>A {@link TimeoutException} is wrapped in {@link UnrulyException}; the futures are
 * <em>not</em> cancelled on timeout.
 *
 * @author Max Arulananthan
 * @since 2.0
 */
public class AwaitCommand implements RuleFlowCommand {

    private final String bindingName;
    private final long timeout;
    private final TimeUnit timeUnit;

    public AwaitCommand(String bindingName, long timeout, TimeUnit timeUnit) {
        super();
        Assert.hasText(bindingName, "bindingName cannot be empty/null.");
        Assert.notNull(timeUnit, "timeUnit cannot be null.");
        this.bindingName = bindingName;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
    }

    @Override
    public void execute(RuleFlowExecutionContext ctx) {
        Object value = ctx.getRuleContext().getBindings().getValue(bindingName);

        if (!(value instanceof CompletableFuture<?> future))
            throw new UnrulyException("Binding [" + bindingName + "] is not a CompletableFuture; found: "
                    + (value == null ? "null" : value.getClass().getName()));
        try {
            future.get(timeout, timeUnit);
        } catch (TimeoutException e) {
            throw new UnrulyException("await [" + bindingName + "] timed out after " + timeout + " " + timeUnit + ".", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw cause instanceof UnrulyException ue
                    ? ue
                    : new UnrulyException("Async step [" + bindingName + "] failed.", cause);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new UnrulyException("await [" + bindingName + "] interrupted.", e);
        }
    }

    public String getBindingName() {
        return bindingName;
    }

    public long getTimeout() {
        return timeout;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

}
