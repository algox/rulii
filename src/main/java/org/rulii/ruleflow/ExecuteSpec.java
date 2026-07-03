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

import org.rulii.lib.spring.util.Assert;

import java.util.function.Consumer;

/**
 * Step-level configuration returned by the {@code execute()} overload that accepts a spec consumer.
 *
 * <p>Provides compile-time access to the only decorator applicable to execute steps:
 * <ul>
 *   <li>{@link #onException(Class, Consumer)} — attach a step-level exception handler.</li>
 * </ul>
 *
 * <p>{@code as()} and {@code with()} are intentionally absent — {@code execute()} is a
 * void side-effect step that produces no result and accepts no step-scoped parameters.
 *
 * <pre>{@code
 * .execute(action(() -> { throw new UnrulyException("boom"); }), spec -> spec
 *     .onException(UnrulyException.class, b -> b.execute(action(() -> log.add("handled")))))
 * }</pre>
 *
 * @param <SELF> the concrete builder type, matching the enclosing {@link RuleFlowBuilderTemplate}.
 *
 * @author Max Arulananthan
 * @since 2.0
 */
public class ExecuteSpec<SELF extends RuleFlowBuilderTemplate<SELF>> {

    private final RunConstruct construct;
    private final RuleFlowBuilderTemplate<SELF> builder;

    ExecuteSpec(RunConstruct construct, RuleFlowBuilderTemplate<SELF> builder) {
        super();
        this.construct = construct;
        this.builder = builder;
    }

    /**
     * Attaches a step-level exception handler. When this step throws a matching exception
     * the handler body runs, and execution continues with the next step.
     *
     * @param type    the exception type to catch; must not be null.
     * @param handler Consumer defining the handler commands; must not be null.
     * @param <E>     the exception type.
     * @return this spec.
     */
    public <E extends Exception> ExecuteSpec<SELF> onException(Class<E> type, Consumer<SELF> handler) {
        Assert.notNull(type, "type cannot be null.");
        Assert.notNull(handler, "handler cannot be null.");
        construct.setExceptionHandler(builder.buildExceptionHandler(type, handler));
        return this;
    }
}
