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

import org.rulii.ruleflow.command.RuleFlowCommand;

/**
 * Root build-time abstraction for every pipeline element.
 *
 * <p>Every pipeline method in {@link RuleFlowBuilderTemplate} — {@code bind()}, {@code run()},
 * {@code when()}, {@code exit()}, etc. — creates a concrete {@code CommandConstruct} and
 * registers it via {@code addConstruct()}. When the next construct arrives (or {@code build()}
 * is called), {@link #seal()} delegates to {@link #buildCommand()} to produce an immutable
 * {@link RuleFlowCommand}.
 *
 * <h3>Capability interfaces</h3>
 * <p>Constructs advertise optional build-time capabilities by implementing:
 * <ul>
 *   <li>{@link Bindable} — the result can be bound to a named binding via {@link RunSpec#as}.</li>
 *   <li>{@link Parameterizable} — step-scoped parameter injection via {@link RunSpec#with}.</li>
 *   <li>{@link ExceptionHandleable} — step-level exception handling via
 *       {@link RunSpec#onException} or {@link ExecuteSpec#onException}.</li>
 * </ul>
 * These are configured through the step-specific spec consumers ({@link RunSpec},
 * {@link ExecuteSpec}) rather than through top-level builder methods.
 *
 * <h3>Sub-hierarchies</h3>
 * <ul>
 *   <li>{@link FlowConstruct} — container constructs ({@code when}, {@code forEach},
 *       {@code scope}, {@code onException}) that accumulate a body of commands while
 *       sitting on the builder's internal stack.</li>
 *   <li>Direct subclasses — leaf constructs ({@code BindConstruct}, {@code ContextConstruct},
 *       {@code ExitConstruct}, {@code DirectCommandConstruct}) and the concrete runnable
 *       construct ({@code RunConstruct}) that implements the relevant capability interfaces.</li>
 * </ul>
 *
 * @author Max Arulananthan
 * @since 2.0
 */
abstract class CommandConstruct {

    protected CommandConstruct() {
        super();
    }

    /**
     * Produces the immutable runtime command.
     *
     * <p>This method is {@code final}. Subclasses implement {@link #buildCommand()} to produce
     * the concrete command from the construct state.
     *
     * @return the sealed command; never null.
     */
    final RuleFlowCommand seal() {
        return buildCommand();
    }

    /**
     * Creates the immutable command using the current construct state.
     * Called by {@link #seal()}.
     *
     * @return the immutable runtime command; never null.
     */
    protected abstract RuleFlowCommand buildCommand();
}
