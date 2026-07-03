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

import org.rulii.bind.*;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.UnrulyException;
import org.rulii.ruleflow.command.RuleFlowCommand;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Carries the handler commands for a caught exception of a specific type.
 *
 * <p>Attached to a {@link RuleFlow} as a flow-level global handler (via
 * {@link RuleFlowBuilderTemplate#onException}) or to an individual step via
 * {@link RunSpec#onException} / {@link ExecuteSpec#onException}. Unlike the
 * former {@code OnExceptionCommand}, this class is not a pipeline command —
 * it is a plain configuration object invoked explicitly by {@link RuleFlow}
 * execution machinery.
 *
 * <p>When invoked, the exception is bound under the reserved name {@code "ex"} in
 * the current scope so that handler commands can access it via normal parameter matching.
 * Execution continues from the next pipeline step after the handler returns; call
 * {@code exit()} inside the handler body to stop the flow explicitly.
 *
 * @author Max Arulananthan
 * @since 2.0
 */
public class RuleFlowExceptionHandler {

    private final Class<? extends Exception> exceptionType;
    private List<RuleFlowCommand> body = Collections.emptyList();

    RuleFlowExceptionHandler(Class<? extends Exception> exceptionType) {
        super();
        Assert.notNull(exceptionType, "exceptionType cannot be null.");
        this.exceptionType = exceptionType;
    }

    /**
     * Returns the exception type this handler matches.
     *
     * @return exception type; never null.
     */
    public Class<? extends Exception> getExceptionType() {
        return exceptionType;
    }

    /**
     * Returns true if this handler can process the given exception.
     *
     * @param e the exception to test; must not be null.
     * @return true if {@code e} is an instance of {@link #getExceptionType()}.
     */
    public boolean canHandle(Exception e) {
        Assert.notNull(e, "e cannot be null.");
        return exceptionType.isInstance(e);
    }

    /**
     * Pushes a dedicated handler scope, binds the exception as {@code "ex"} into it, runs
     * the handler body, then removes the scope in a {@code finally} block. All bindings
     * created by the body are discarded when the scope is removed; use side effects
     * (e.g. {@code execute(action(() -> log.add(...)))}) to propagate results out of the
     * handler.
     *
     * @param e   the caught exception; must not be null.
     * @param ctx the current execution context; must not be null.
     */
    public void handleException(Exception e, RuleFlowExecutionContext ctx) {
        Assert.notNull(e, "e cannot be null.");
        Assert.notNull(ctx, "ctx cannot be null.");

        ScopedBindings bindings = ctx.getRuleContext().getBindings();
        NamedScope handlerScope = bindings.addScope(getScopeName());

        if (!(handlerScope.getBindings() instanceof PromiscuousBinder pb)) throw new UnrulyException("IllegalState: Handler scope does not allow reserved keyword binding.");

        pb.promiscuousBind(Binding.builder()
                .with(ReservedBindings.EXCEPTION.getName())
                .type(Exception.class)
                .value(e)
                .build());

        try {
            for (RuleFlowCommand cmd : body) {
                cmd.execute(ctx);
            }
        } finally {
            bindings.removeScope(handlerScope);
        }
    }

    void setBody(List<RuleFlowCommand> body) {
        Assert.notNull(body, "body cannot be null.");
        this.body = Collections.unmodifiableList(body);
    }

    List<RuleFlowCommand> getBody() {
        return body;
    }

    protected String getScopeName() {
        return "exception-scope-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
