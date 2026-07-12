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

import org.rulii.bind.NamedScope;
import org.rulii.context.RuleContext;
import org.rulii.context.RuleContextBuilder;
import org.rulii.model.AsyncRunnable;
import org.rulii.model.Runnable;
import org.rulii.model.UnrulyException;
import org.rulii.registry.RuleRegistry;
import org.rulii.ruleflow.AsyncContextMode;
import org.rulii.ruleflow.RuleFlowExceptionHandler;
import org.rulii.ruleflow.RuleFlowExecutionContext;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;

/**
 * Pipeline command that launches a {@link Runnable} asynchronously and optionally binds
 * the resulting {@link CompletableFuture} into the current scope for later retrieval.
 *
 * <p>If the target implements {@link AsyncRunnable}, its {@code runAsync()} method is used
 * directly. Otherwise the call to {@link Runnable#run(RuleContext)} is submitted to the
 * <em>resolved</em> context's executor service - the calling context's under
 * {@code AsyncContextMode#SHARED}/{@code IMMUTABLE}, or the supplied context's own under
 * {@code AsyncContextMode#CUSTOM} - so {@code withContext(...)}'s executor isolation actually
 * takes effect.
 *
 * <p>The future binding (if any) is always placed in the <em>calling</em> context so the
 * rest of the flow can access it via {@code await()}, {@code awaitAll()}, or {@code awaitAny()}.
 *
 * <p>If a continuation was configured via {@code AsyncRunSpec#thenRun}, it runs on the async
 * completion thread once the task succeeds, and the bound future (if any) reflects completion
 * of the whole chain rather than just the initial task.
 *
 * <p>A failure - of the task or the {@code thenRun} continuation - is checked against the
 * step-level handler configured via {@code AsyncRunSpec#onException} (if any), then against
 * the flow-level global handler (see {@link org.rulii.ruleflow.RuleFlow#getGlobalHandler()}),
 * mirroring the step-level-then-global fallback order {@code RunCommand} uses for synchronous
 * steps. This check always runs as soon as the failure occurs - whether or not anything later
 * calls {@code await()} on this step's binding - since it is chained directly onto the future.
 *
 * @author Max Arulananthan
 * @since 2.0
 */
public class AsyncRunCommand implements RuleFlowCommand {

    private final Runnable<?> runnable;
    private final String nameInRegistry;
    private final Class<?> classInRegistry;
    private final String bindingName;
    private final AsyncContextMode contextMode;
    private final RuleContext customContext;
    private final String continuationResultBindingName;
    private final List<RuleFlowCommand> continuation;
    private final RuleFlowExceptionHandler exceptionHandler;

    public AsyncRunCommand(Runnable<?> runnable, String nameInRegistry, Class<?> classInRegistry,
                           String bindingName, AsyncContextMode contextMode, RuleContext customContext,
                           String continuationResultBindingName, List<RuleFlowCommand> continuation,
                           RuleFlowExceptionHandler exceptionHandler) {
        super();
        this.runnable = runnable;
        this.nameInRegistry = nameInRegistry;
        this.classInRegistry = classInRegistry;
        this.bindingName = bindingName;
        this.contextMode = contextMode;
        this.customContext = customContext;
        this.continuationResultBindingName = continuationResultBindingName;
        this.continuation = continuation != null ? continuation : Collections.emptyList();
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    @SuppressWarnings({"rawtypes"})
    public void execute(RuleFlowExecutionContext ctx) {
        RuleContext ruleContext = ctx.getRuleContext();
        RuleContext effectiveCtx = resolveContext(ruleContext);
        Runnable<?> target = resolveRunnable(ruleContext);
        ExecutorService executor = effectiveCtx.getExecutorService();

        // Bind the caller-visible handle BEFORE launching the task: in SHARED mode the task
        // pushes/pops its own scopes on these very bindings from the executor thread, so
        // binding after launch could land the future inside a transient child scope - and
        // vanish with it when that scope is removed.
        CompletableFuture<Object> handle = null;

        if (bindingName != null) {
            handle = new CompletableFuture<>();
            ruleContext.getBindings().bind(bindingName, handle);
        }

        CompletableFuture<?> future;

        if (target instanceof AsyncRunnable asyncTarget) {
            future = asyncTarget.runAsync(effectiveCtx);
        } else {
            future = CompletableFuture.supplyAsync(() -> target.run(effectiveCtx), executor);
        }

        if (!continuation.isEmpty()) {
            RuleFlowExecutionContext continuationCtx = ctx.withRuleContext(effectiveCtx);
            future = future.thenApplyAsync(result -> runContinuation(result, effectiveCtx, continuationCtx), executor);
        }

        // Always attach a failure stage - even with no step-level handler configured, an
        // unhandled failure still needs the chance to fall back to the flow's global handler.
        future = future.handleAsync((result, ex) -> handleFailure(result, ex, ctx, effectiveCtx), executor);

        if (handle != null) {
            CompletableFuture<Object> boundHandle = handle;
            future.whenComplete((result, ex) -> {
                if (ex != null) boundHandle.completeExceptionally(ex);
                else boundHandle.complete(result);
            });
        }
    }

    private Object runContinuation(Object result, RuleContext effectiveCtx, RuleFlowExecutionContext continuationCtx) {
        NamedScope scope = effectiveCtx.getBindings().addScope("asyncRun-then-" + UUID.randomUUID());

        try {
            effectiveCtx.getBindings().bind(continuationResultBindingName, result);

            for (RuleFlowCommand cmd : continuation) {
                cmd.execute(continuationCtx);
            }
        } finally {
            effectiveCtx.getBindings().removeScope(scope);
        }

        return result;
    }

    private Object handleFailure(Object result, Throwable ex, RuleFlowExecutionContext ctx, RuleContext effectiveCtx) {
        if (ex == null) return result;

        Throwable cause = ex instanceof CompletionException && ex.getCause() != null ? ex.getCause() : ex;

        if (!(cause instanceof Exception causeEx)) {
            throw cause instanceof RuntimeException re ? re : new UnrulyException("Async step failed.", cause);
        }

        boolean stepLevel = exceptionHandler != null && exceptionHandler.canHandle(causeEx);
        RuleFlowExceptionHandler globalHandler = ctx.getRuleFlow().getGlobalHandler();
        RuleFlowExceptionHandler handler = stepLevel ? exceptionHandler
                : (globalHandler != null && globalHandler.canHandle(causeEx) ? globalHandler : null);

        if (handler != null) {
            RuleFlowExecutionContext continuationCtx = ctx.withRuleContext(effectiveCtx);
            handler.handleException(causeEx, continuationCtx);
            ctx.getRuleContext().getTracer().fireOnRuleFlowExceptionHandled(ctx.getRuleFlow(), causeEx, stepLevel);
            return null;
        }

        throw causeEx instanceof UnrulyException ue ? ue : new UnrulyException("Async step failed.", causeEx);
    }

    private RuleContext resolveContext(RuleContext ruleContext) {
        return switch (contextMode) {
            case SHARED -> ruleContext;
            case IMMUTABLE -> {
                RuleContextBuilder builder = RuleContext.builder().with(ruleContext);
                yield builder.bindings(builder.getBindings().asImmutable()).build();
            }
            case CUSTOM -> customContext;
        };
    }

    private Runnable<?> resolveRunnable(RuleContext ruleContext) {

        if (runnable != null) return runnable;

        RuleRegistry registry = ruleContext.getRuleRegistry();
        if (registry == null) throw new UnrulyException("No RuleRegistry configured on RuleContext. Cannot look up ["
                + (nameInRegistry != null ? nameInRegistry : classInRegistry) + "]");

        if (nameInRegistry != null) {
            Runnable<?> result = registry.get(nameInRegistry);
            if (result == null) throw new UnrulyException("No Runnable found in RuleRegistry for name [" + nameInRegistry + "]");
            return result;
        }

        Runnable<?> result = registry.getRule(classInRegistry);
        if (result == null) throw new UnrulyException("No Runnable found in RuleRegistry for class [" + classInRegistry + "]");
        return result;
    }

    public Runnable<?> getRunnable() {
        return runnable;
    }

    public String getNameInRegistry() {
        return nameInRegistry;
    }

    public Class<?> getClassInRegistry() {
        return classInRegistry;
    }

    public String getBindingName() {
        return bindingName;
    }

    public AsyncContextMode getContextMode() {
        return contextMode;
    }

    public RuleContext getCustomContext() {
        return customContext;
    }

    public String getContinuationResultBindingName() {
        return continuationResultBindingName;
    }

    public List<RuleFlowCommand> getContinuation() {
        return continuation;
    }

    public RuleFlowExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }
}
