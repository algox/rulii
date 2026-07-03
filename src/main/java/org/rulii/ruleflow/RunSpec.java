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

import org.rulii.bind.BindingDeclaration;
import org.rulii.lib.spring.util.Assert;

import java.util.function.Consumer;

/**
 * Step-level configuration returned by {@code run()} and {@code apply()} overloads that
 * accept a spec consumer.
 *
 * <p>Provides compile-time access to decorators applicable only to runnable steps:
 * <ul>
 *   <li>{@link #as(String)} — bind the step's result into the current scope.</li>
 *   <li>{@link #as(String, String)} — bind the step's result into a named scope.</li>
 *   <li>{@link #with(BindingDeclaration[])} — inject step-scoped parameters from binding declarations.</li>
 *   <li>{@link #with(Object)} — inject step-scoped parameters from a POJO or {@code Map}.</li>
 *   <li>{@link #onException(Class, Consumer)} — attach a step-level exception handler.</li>
 * </ul>
 *
 * <p>Each method returns {@code this} for fluent chaining within the spec consumer:
 * <pre>{@code
 * .run(pricingRule, spec -> spec
 *     .as("price")
 *     .with(discount -> 0.1)
 *     .onException(Exception.class, b -> b.run(fallbackRule)))
 * }</pre>
 *
 * @param <SELF> the concrete builder type, matching the enclosing {@link RuleFlowBuilderTemplate}.
 *
 * @author Max Arulananthan
 * @since 2.0
 */
public class RunSpec<SELF extends RuleFlowBuilderTemplate<SELF>> {

    private final RunConstruct construct;
    private final RuleFlowBuilderTemplate<SELF> builder;

    RunSpec(RunConstruct construct, RuleFlowBuilderTemplate<SELF> builder) {
        super();
        this.construct = construct;
        this.builder = builder;
    }

    /**
     * Binds the step's result into the current scope under {@code bindingName}.
     *
     * @param bindingName the binding name; must not be null or empty.
     * @return this spec.
     */
    public RunSpec<SELF> as(String bindingName) {
        Assert.hasText(bindingName, "bindingName cannot be empty/null.");
        construct.setBindingTarget(bindingName, null);
        return this;
    }

    /**
     * Binds the step's result into a specific named scope.
     *
     * @param scopeName   the target scope; must not be null or empty.
     * @param bindingName the binding name; must not be null or empty.
     * @return this spec.
     */
    public RunSpec<SELF> as(String scopeName, String bindingName) {
        Assert.hasText(scopeName, "scopeName cannot be empty/null.");
        Assert.hasText(bindingName, "bindingName cannot be empty/null.");
        construct.setBindingTarget(bindingName, scopeName);
        return this;
    }

    /**
     * Supplies step-scoped parameters via {@link BindingDeclaration} lambdas.
     * Bindings exist only for the duration of the step.
     *
     * @param params one or more binding declarations; must not be null.
     * @return this spec.
     */
    public RunSpec<SELF> with(BindingDeclaration<?>... params) {
        Assert.notNull(params, "params cannot be null.");
        construct.setParams(params);
        return this;
    }

    /**
     * Supplies step-scoped parameters from a JavaBean POJO or a {@code Map<String, Object>}.
     * Bindings exist only for the duration of the step.
     *
     * @param params non-null POJO or {@code Map<String, Object>}.
     * @return this spec.
     */
    public RunSpec<SELF> with(Object params) {
        Assert.notNull(params, "params cannot be null.");
        construct.setParams(params);
        return this;
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
    public <E extends Exception> RunSpec<SELF> onException(Class<E> type, Consumer<SELF> handler) {
        Assert.notNull(type, "type cannot be null.");
        Assert.notNull(handler, "handler cannot be null.");
        construct.setExceptionHandler(builder.buildExceptionHandler(type, handler));
        return this;
    }
}
