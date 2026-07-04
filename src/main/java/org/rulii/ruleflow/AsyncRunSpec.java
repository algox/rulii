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
import org.rulii.lib.spring.util.Assert;

import java.util.function.Consumer;

/**
 * Step-level configuration returned by {@code asyncRun()} overloads that accept a spec consumer.
 *
 * <p>Provides compile-time access to decorators applicable only to async steps:
 * <ul>
 *   <li>{@link #as(String)} — bind the resulting {@link java.util.concurrent.CompletableFuture}
 *       into the current scope under the given name.</li>
 *   <li>{@link #withImmutableBindings()} — launch the async step in a new context backed by
 *       an immutable snapshot of the caller's bindings ({@link AsyncContextMode#IMMUTABLE}).</li>
 *   <li>{@link #withContext(RuleContext)} — launch the async step using a completely separate,
 *       user-supplied context ({@link AsyncContextMode#CUSTOM}).</li>
 *   <li>{@link #thenRun(String, Consumer)} — chain a follow-up command body that runs on the
 *       async completion thread once the step finishes, without blocking the flow.</li>
 *   <li>{@link #onException(Class, Consumer)} - recover from a failure of the task or, if
 *       configured, the {@code thenRun} continuation, without needing to {@code await()}.</li>
 * </ul>
 *
 * <p>Each method returns {@code this} for fluent chaining within the spec consumer:
 * <pre>{@code
 * .asyncRun(pricingRuleSet, spec -> spec
 *     .as("pricingFuture")
 *     .withImmutableBindings())
 * .awaitAll("pricingFuture", "inventoryFuture")
 * }</pre>
 *
 * @param <SELF> the concrete builder type, matching the enclosing {@link RuleFlowBuilderTemplate}.
 *
 * @author Max Arulananthan
 * @since 2.0
 */
public class AsyncRunSpec<SELF extends RuleFlowBuilderTemplate<SELF>> {

    private final AsyncRunConstruct construct;
    private final RuleFlowBuilderTemplate<SELF> builder;

    AsyncRunSpec(AsyncRunConstruct construct, RuleFlowBuilderTemplate<SELF> builder) {
        super();
        this.construct = construct;
        this.builder = builder;
    }

    /**
     * Binds the async step's {@link java.util.concurrent.CompletableFuture} into the current
     * scope under {@code bindingName}. Use {@code await(bindingName)} later to block until
     * the future completes.
     *
     * @param bindingName the binding name; must not be null or empty.
     * @return this spec.
     */
    public AsyncRunSpec<SELF> as(String bindingName) {
        Assert.hasText(bindingName, "bindingName cannot be empty/null.");
        construct.setBindingName(bindingName);
        return this;
    }

    /**
     * Launches the async step in a new {@link RuleContext} backed by an immutable snapshot of
     * the caller's current bindings. The async task can read all values visible at the time
     * {@code asyncRun} executes, but cannot mutate the caller's bindings.
     *
     * @return this spec.
     */
    public AsyncRunSpec<SELF> withImmutableBindings() {
        construct.setContextMode(AsyncContextMode.IMMUTABLE);
        return this;
    }

    /**
     * Launches the async step using the supplied {@link RuleContext}. The async task operates
     * on a completely independent context with its own bindings and services.
     *
     * @param context the context for the async step; must not be null.
     * @return this spec.
     */
    public AsyncRunSpec<SELF> withContext(RuleContext context) {
        Assert.notNull(context, "context cannot be null.");
        construct.setContextMode(AsyncContextMode.CUSTOM);
        construct.setCustomContext(context);
        return this;
    }

    /**
     * Chains a follow-up command body that runs once the async step's task completes
     * successfully — analogous to {@link java.util.concurrent.CompletableFuture#thenApply}.
     *
     * <p>The body runs on the same thread that completes the async task (via the calling
     * {@link RuleContext}'s executor service), against the async step's resolved context —
     * the caller's shared context under {@link AsyncContextMode#SHARED}, or the isolated
     * context under {@link AsyncContextMode#IMMUTABLE}/{@link AsyncContextMode#CUSTOM}. The
     * resolved value is bound under {@code resultBindingName} for the duration of the body,
     * in a scope that is removed once the body completes.
     *
     * <p>If the async task fails, the body does not run and the failure propagates unchanged.
     * If the body itself throws, the failure is only observed by a later {@code await}/
     * {@code awaitAll}/{@code awaitAny} on this step's {@link #as(String)} binding unless an
     * {@link #onException(Class, Consumer)} handler is attached — like {@code CompletableFuture},
     * an unobserved, unhandled failure here is silently dropped.
     *
     * <p>Unlike {@code CompletableFuture.thenApply}, the body does not replace the resolved
     * value — the future produced by this step still yields the async task's original result,
     * not whatever the body's last command computed.
     *
     * @param resultBindingName the binding name for the resolved value inside the body; must not be null or empty.
     * @param body Consumer defining the follow-up commands; must not be null.
     * @return this spec.
     */
    public AsyncRunSpec<SELF> thenRun(String resultBindingName, Consumer<SELF> body) {
        Assert.hasText(resultBindingName, "resultBindingName cannot be empty/null.");
        Assert.notNull(body, "body cannot be null.");
        construct.setContinuation(resultBindingName, builder.buildBody(body));
        return this;
    }

    /**
     * Attaches a handler that recovers from a matching failure of this async step - the task
     * itself, or, if {@link #thenRun(String, Consumer)} was configured, the continuation.
     *
     * <p>Unlike {@link #thenRun(String, Consumer)}, this handler runs whether or not anything
     * later calls {@code await()}/{@code awaitAll()}/{@code awaitAny()} on this step's future:
     * it is chained onto the future itself, so it fires as soon as the failure occurs. The
     * caught exception is bound under the reserved name {@code "ex"} in a dedicated scope for
     * the duration of the handler body, exactly like {@link RunSpec#onException} and the
     * flow-level {@link RuleFlowBuilderTemplate#onException}.
     *
     * <p>When the handler runs, the step's future (and any {@link #as(String)} binding) resolves
     * successfully with {@code null} rather than failing - the recovery has no result value to
     * offer, matching the "swallow and continue" semantics of the synchronous handlers. A
     * failure that does not match {@code type} falls back to the flow-level global handler (if
     * one is registered and matches); only if neither matches is the failure left to propagate,
     * still observable via {@code await()}.
     *
     * @param type    the exception type to catch; must not be null.
     * @param handler Consumer defining the handler commands; must not be null.
     * @param <E>     the exception type.
     * @return this spec.
     */
    public <E extends Exception> AsyncRunSpec<SELF> onException(Class<E> type, Consumer<SELF> handler) {
        Assert.notNull(type, "type cannot be null.");
        Assert.notNull(handler, "handler cannot be null.");
        construct.setExceptionHandler(builder.buildExceptionHandler(type, handler));
        return this;
    }
}
