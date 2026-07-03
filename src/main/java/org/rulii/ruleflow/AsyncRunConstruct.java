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
import org.rulii.model.Runnable;
import org.rulii.ruleflow.command.AsyncRunCommand;
import org.rulii.ruleflow.command.RuleFlowCommand;

import java.util.Collections;
import java.util.List;

/**
 * Build-time construct for {@code asyncRun()} steps.
 *
 * <p>Accepts any {@link Runnable} — Rule, RuleSet, RuleFlow, Function, or Action — and seals
 * into an immutable {@link AsyncRunCommand}. Configuration is applied via {@link AsyncRunSpec}.
 *
 * @author Max Arulananthan
 * @since 2.0
 */
class AsyncRunConstruct extends CommandConstruct {

    private final Runnable<?> runnable;
    private final String nameInRegistry;
    private final Class<?> classInRegistry;

    private String bindingName = null;
    private AsyncContextMode contextMode = AsyncContextMode.SHARED;
    private RuleContext customContext = null;
    private String continuationResultBindingName = null;
    private List<RuleFlowCommand> continuation = Collections.emptyList();
    private RuleFlowExceptionHandler exceptionHandler = null;

    private AsyncRunConstruct(Runnable<?> runnable, String nameInRegistry, Class<?> classInRegistry) {
        super();
        this.runnable = runnable;
        this.nameInRegistry = nameInRegistry;
        this.classInRegistry = classInRegistry;
    }

    static AsyncRunConstruct of(Runnable<?> runnable) {
        Assert.notNull(runnable, "runnable cannot be null.");
        return new AsyncRunConstruct(runnable, null, null);
    }

    static AsyncRunConstruct ofName(String nameInRegistry) {
        Assert.hasText(nameInRegistry, "nameInRegistry cannot be empty/null.");
        return new AsyncRunConstruct(null, nameInRegistry, null);
    }

    static AsyncRunConstruct ofClass(Class<?> registryClass) {
        Assert.notNull(registryClass, "registryClass cannot be null.");
        return new AsyncRunConstruct(null, null, registryClass);
    }

    void setBindingName(String bindingName) {
        Assert.hasText(bindingName, "bindingName cannot be empty/null.");
        this.bindingName = bindingName;
    }

    void setContextMode(AsyncContextMode contextMode) {
        Assert.notNull(contextMode, "contextMode cannot be null.");
        this.contextMode = contextMode;
    }

    void setCustomContext(RuleContext customContext) {
        Assert.notNull(customContext, "customContext cannot be null.");
        this.customContext = customContext;
    }

    void setContinuation(String continuationResultBindingName, List<RuleFlowCommand> continuation) {
        Assert.hasText(continuationResultBindingName, "continuationResultBindingName cannot be empty/null.");
        Assert.notNull(continuation, "continuation cannot be null.");
        this.continuationResultBindingName = continuationResultBindingName;
        this.continuation = continuation;
    }

    void setExceptionHandler(RuleFlowExceptionHandler exceptionHandler) {
        Assert.notNull(exceptionHandler, "exceptionHandler cannot be null.");
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    protected RuleFlowCommand buildCommand() {
        return new AsyncRunCommand(runnable, nameInRegistry, classInRegistry,
                bindingName, contextMode, customContext,
                continuationResultBindingName, continuation, exceptionHandler);
    }
}
