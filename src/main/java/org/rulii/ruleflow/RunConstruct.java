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
import org.rulii.model.Runnable;
import org.rulii.ruleflow.command.RuleFlowCommand;
import org.rulii.ruleflow.command.RunCommand;

/**
 * Build-time construct for {@code run()}, {@code apply()}, and {@code execute()} steps.
 *
 * <p>Accepts any {@link Runnable} — Rule, RuleSet, RuleFlow, Function, or Action (all of which
 * extend or implement {@link Runnable}). Supports {@code as()} ({@link Bindable}), {@code with()}
 * ({@link Parameterizable}), and step-level {@code onException()} ({@link ExceptionHandleable}).
 * Seals into an immutable {@link RunCommand}.
 *
 * <p>Use the static factory methods to create instances:
 * <ul>
 *   <li>{@link #of(Runnable)} — direct {@link Runnable} (Rule, RuleSet, RuleFlow, or Function)</li>
 *   <li>{@link #ofName(String)} — registry lookup by name</li>
 *   <li>{@link #ofClass(Class)} — registry lookup by class</li>
 * </ul>
 *
 * @author Max Arulananthan
 * @since 2.0
 */
class RunConstruct extends CommandConstruct implements Bindable, Parameterizable, ExceptionHandleable {

    private final Runnable<?> runnable;
    private final String nameInRegistry;
    private final Class<?> classInRegistry;

    private String bindingName = null;
    private String bindingScopeName = null;
    private Object params = null;
    private RuleFlowExceptionHandler exceptionHandler = null;

    private RunConstruct(Runnable<?> runnable, String nameInRegistry, Class<?> classInRegistry) {
        super();
        this.runnable = runnable;
        this.nameInRegistry = nameInRegistry;
        this.classInRegistry = classInRegistry;
    }

    static RunConstruct of(Runnable<?> runnable) {
        Assert.notNull(runnable, "runnable cannot be null.");
        return new RunConstruct(runnable, null, null);
    }

    static RunConstruct ofName(String nameInRegistry) {
        Assert.hasText(nameInRegistry, "nameInRegistry cannot be empty/null.");
        return new RunConstruct(null, nameInRegistry, null);
    }

    static RunConstruct ofClass(Class<?> registryClass) {
        Assert.notNull(registryClass, "registryClass cannot be null.");
        return new RunConstruct(null, null, registryClass);
    }

    @Override
    public void setBindingTarget(String name, String scopeName) {
        this.bindingName = name;
        this.bindingScopeName = scopeName;
    }

    @Override
    public void setParams(Object params) {
        this.params = params;
    }

    @Override
    public void setExceptionHandler(RuleFlowExceptionHandler exceptionHandler) {
        Assert.notNull(exceptionHandler, "exceptionHandler cannot be null.");
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    protected RuleFlowCommand buildCommand() {
        return new RunCommand(runnable, nameInRegistry, classInRegistry, bindingName, bindingScopeName, params, exceptionHandler);
    }
}
