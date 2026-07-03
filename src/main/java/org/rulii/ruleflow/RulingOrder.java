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
import org.rulii.bind.Bindings;
import org.rulii.context.RuleContext;
import org.rulii.context.RuleContextBuilder;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.InputParameter;
import org.rulii.model.UnrulyException;
import org.rulii.model.action.Action;
import org.rulii.model.function.Function;
import org.rulii.ruleflow.command.RuleFlowCommand;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Default implementation of {@link RuleFlow}.
 *
 * <p>Holds immutable pipeline configuration; all mutable per-run state lives in
 * {@link RuleFlowExecutionContext}. Instances are created by
 * {@link RuleFlowBuilderTemplate#build()}.
 *
 * @param <T> the result type.
 *
 * @author Max Arulananthan
 * @since 2.0
 */
final class RulingOrder<T> implements RuleFlow<T> {

    private final RuleFlowDefinition definition;
    private final List<RuleFlowCommand> commands;
    private final List<InputParameter<?>> inputParameters;
    private final Action finalizer;
    private final Function<T> resultExtractor;
    private final RuleFlowExceptionHandler globalHandler;
    private final Consumer<RuleContextBuilder> contextConfigurator;
    private final RuleFlowExecutionStrategy<T> executionStrategy;
    private final RuleFlowExecutionStrategy<CompletableFuture<T>> asyncExecutionStrategy;

    RulingOrder(RuleFlowDefinition definition,
                List<RuleFlowCommand> commands,
                List<InputParameter<?>> inputParameters,
                Action finalizer,
                Function<T> resultExtractor,
                RuleFlowExceptionHandler globalHandler,
                Consumer<RuleContextBuilder> contextConfigurator) {
        super();
        Assert.notNull(definition, "definition cannot be null.");
        Assert.notNull(commands, "commands cannot be null.");
        Assert.notNull(inputParameters, "inputParameters cannot be null.");
        this.definition = definition;
        this.commands = Collections.unmodifiableList(commands);
        this.inputParameters = Collections.unmodifiableList(inputParameters);
        this.finalizer = finalizer;
        this.resultExtractor = resultExtractor;
        this.globalHandler = globalHandler;
        this.contextConfigurator = contextConfigurator;
        this.executionStrategy = RuleFlowExecutionStrategy.build();
        this.asyncExecutionStrategy = RuleFlowExecutionStrategy.buildAsync();
    }

    @Override
    @SuppressWarnings("unchecked")
    public T run(RuleContext context) throws UnrulyException {
        Assert.notNull(context, "context cannot be null.");

        try {
            return executionStrategy.run(this, context);
        } catch (RuleFlowReturn r) {
            return (T) r.getResult();
        }
    }

    @Override
    public T run(BindingDeclaration<?>... params) throws UnrulyException {
        Bindings bindings = Bindings.builder().standard(params);
        RuleContextBuilder builder = RuleContext.builder().with(bindings);
        if (contextConfigurator != null) contextConfigurator.accept(builder);
        return run(builder.build());
    }

    @Override
    public CompletableFuture<T> runAsync(RuleContext ruleContext) {
        Assert.notNull(ruleContext, "ruleContext cannot be null.");
        return asyncExecutionStrategy.run(this, ruleContext);
    }

    @Override
    public CompletableFuture<T> runAsync(RuleContext ruleContext, long timeout, TimeUnit timeUnit) {
        Assert.notNull(ruleContext, "ruleContext cannot be null.");
        Assert.notNull(timeUnit, "timeUnit cannot be null.");
        return asyncExecutionStrategy.run(this, ruleContext).orTimeout(timeout, timeUnit);
    }

    @Override
    public RuleFlowDefinition getDefinition() {
        return definition;
    }

    @Override
    public List<InputParameter<?>> getInputParameters() {
        return inputParameters;
    }

    @Override
    public Action getFinalizer() {
        return finalizer;
    }

    @Override
    public String getName() {
        return definition.getName();
    }

    @Override
    public Function<T> getResultExtractor() {
        return resultExtractor;
    }

    @Override
    public List<RuleFlowCommand> getCommands() {
        return commands;
    }

    @Override
    public RuleFlowExceptionHandler getGlobalHandler() {
        return globalHandler;
    }

    @SuppressWarnings("unchecked")
    T extractResult(RuleContext context) {
        if (resultExtractor != null) return resultExtractor.apply(context);
        return (T) context;
    }

    @Override
    public String toString() {
        return "RulingOrder{" +
                "name='" + getName() + '\'' +
                ", commands=" + commands.size() +
                ", inputParameters=" + inputParameters +
                '}';
    }
}
