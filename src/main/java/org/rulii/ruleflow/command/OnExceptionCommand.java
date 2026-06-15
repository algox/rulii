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

import org.rulii.bind.*;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.ScopeDefining;
import org.rulii.model.UnrulyException;
import org.rulii.ruleflow.RuleFlowExecutionContext;

import java.util.Collections;
import java.util.List;

/**
 * Pipeline command that handles exceptions of a specific type thrown by a preceding step
 * or by any step in the flow (when used as the global handler).
 *
 * <p>When invoked the exception is bound as {@code "ex"} in a dedicated scope, making it
 * available to handler commands via the normal parameter-matching strategy.
 * The flow continues after the handler completes; call {@code returning()} inside
 * the handler to stop explicitly.
 *
 * @author Max Arulananthan
 * @since 2.0
 */
public class OnExceptionCommand implements RuleFlowCommand, ScopeDefining, CompositeCommand {

    private final Class<? extends Exception> exceptionType;
    private final List<RuleFlowCommand> handlerCommands;

    public OnExceptionCommand(Class<? extends Exception> exceptionType, List<RuleFlowCommand> handlerCommands) {
        super();
        Assert.notNull(exceptionType, "exceptionType cannot be null.");
        Assert.notNull(handlerCommands, "handlerCommands cannot be null.");
        this.exceptionType = exceptionType;
        this.handlerCommands = Collections.unmodifiableList(handlerCommands);
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
     * Returns true if this handler can handle the given exception.
     *
     * @param e the exception to test; must not be null.
     * @return true if {@code e} is an instance of {@link #getExceptionType()}.
     */
    public boolean canHandle(Exception e) {
        Assert.notNull(e, "e cannot be null.");
        return exceptionType.isInstance(e);
    }

    /**
     * Executes the handler commands in a dedicated scope with the exception bound as {@code "ex"}.
     *
     * @param e   the caught exception; must not be null.
     * @param ctx the current execution context; must not be null.
     */
    public void handleException(Exception e, RuleFlowExecutionContext ctx) {
        Assert.notNull(e, "e cannot be null.");
        Assert.notNull(ctx, "ctx cannot be null.");

        ScopedBindings bindings = ctx.getRuleContext().getBindings();
        NamedScope currentScope = bindings.getCurrentScope();

        if (!(currentScope.getBindings() instanceof PromiscuousBinder pb)) throw new UnrulyException("IllegalState: CurrentScope does not allow reserved keyword binding.");

        pb.promiscuousBind(Binding.builder()
                .with(ReservedBindings.EXCEPTION.getName())
                .type(Exception.class)
                .value(e)
                .build());

        for (RuleFlowCommand cmd : handlerCommands) {
            cmd.execute(ctx);
        }
    }

    @Override
    public void execute(RuleFlowExecutionContext ctx) {
        // Global handler registration — invoked via handleException(), not directly.
    }

    @Override
    public List<List<RuleFlowCommand>> getBlocks() {
        return List.of(handlerCommands);
    }
}
