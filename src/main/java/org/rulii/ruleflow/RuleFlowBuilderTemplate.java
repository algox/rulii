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

import org.rulii.bind.Binding;
import org.rulii.bind.BindingDeclaration;
import org.rulii.bind.Bindings;
import org.rulii.bind.load.BindingLoader;
import org.rulii.context.RuleContextBuilder;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.InputParameter;
import org.rulii.model.SourceDefinition;
import org.rulii.model.UnrulyException;
import org.rulii.model.action.Action;
import org.rulii.model.condition.Condition;
import org.rulii.model.function.Function;
import org.rulii.rule.Rule;
import org.rulii.ruleflow.command.*;
import org.rulii.ruleset.RuleSet;
import org.rulii.util.RuleUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Abstract base for all RuleFlow fluent builders.
 *
 * <p>All pipeline methods return {@code SELF} so that custom builders extending this
 * class inherit every method without duplication.
 *
 * <h3>Uniform construct pipeline</h3>
 * <p>Every pipeline element — {@code bind()}, {@code run()}, {@code when()}, {@code exit()},
 * etc. — creates a {@link CommandConstruct} and registers it via the single internal
 * {@link #addConstruct} method. The previously pending construct is sealed (producing an
 * immutable {@link RuleFlowCommand}) exactly when the next construct arrives, or when
 * {@link #build()} is called. No command is ever partially configured while sitting in
 * the command list.
 *
 * <p>Container constructs ({@code when}, {@code forEach}, {@code scope}) push a
 * {@link FlowConstruct} onto the builder's internal stack and use a {@code Consumer<SELF>}
 * body as the implicit scope delimiter — no explicit {@code end*()} call is needed. After the
 * body returns, the filled construct becomes the new pending element via {@link #addConstruct},
 * just like any other construct. The flow-level {@code onException()} superficially looks the
 * same but works differently: its handler body is captured by running the {@code Consumer<SELF>}
 * against a throwaway {@link #newInstance()} builder (see {@link #buildExceptionHandler}) rather
 * than pushing onto this builder's stack, and the result is stored as the flow's
 * {@link RuleFlowExceptionHandler} rather than added to the command list.
 *
 * <h3>Step configuration</h3>
 * <p>{@code run()} and {@code apply()} overloads that accept a {@code Consumer<RunSpec<SELF>>}
 * expose {@code as()}, {@code with()}, and step-level {@code onException()} on a
 * {@link RunSpec} scoped to that step. {@code execute()} accepts a
 * {@code Consumer<ExecuteSpec<SELF>>} for step-level {@code onException()}. This makes
 * the configuration scope explicit at compile time and eliminates ordering-sensitive
 * top-level decorator methods.
 *
 * <h3>Async pipeline</h3>
 * <p>{@code asyncRun()} launches a Rule, RuleSet, RuleFlow, Function, or Action on the context's
 * executor service and, if configured via {@code as()}, binds the resulting
 * {@link java.util.concurrent.CompletableFuture} into the current scope. {@code await()},
 * {@code awaitAll()}, and {@code awaitAny()} later block on one or more of those bindings.
 * The step's {@link AsyncRunSpec} additionally exposes {@code thenRun()} — a follow-up command
 * body chained onto the future via {@code thenApplyAsync} — and {@code onException()} — a
 * failure handler chained onto the future via {@code handleAsync} that fires as soon as the
 * task or {@code thenRun} continuation fails, whether or not anything ever calls {@code await()}.
 *
 * <h3>Extending the builder</h3>
 * <p>Two extension axes are available:
 * <ul>
 *   <li>{@link #command(RuleFlowCommand)} — injects any pre-built command as a leaf step.</li>
 *   <li>{@link #runContainer(ContainerCommand, Consumer)} — plugs in a custom container command
 *       that controls execution of an enclosed body (retry, parallel, circuit-breaker, etc.).</li>
 * </ul>
 *
 * <pre>{@code
 * public abstract class MyBaseBuilder<SELF extends MyBaseBuilder<SELF>>
 *         extends RuleFlowBuilderTemplate<SELF> {
 *
 *     public SELF retry(int maxAttempts, Consumer<SELF> body) {
 *         return runContainer(new RetryCommand(maxAttempts), body);
 *     }
 * }
 * }</pre>
 *
 * @param <SELF> the concrete builder type, enabling fluent chaining without casting.
 *
 * @author Max Arulananthan
 * @since 2.0
 */
public abstract class RuleFlowBuilderTemplate<SELF extends RuleFlowBuilderTemplate<SELF>> {

    private String name;
    private String description;
    private final Set<InputParameter<?>> inputParameters = new LinkedHashSet<>();
    private Action finalizer;
    private Function<?> resultExtractor;
    private Consumer<RuleContextBuilder> contextConfigurator;
    private RuleFlowExceptionHandler globalHandler;

    // State machine — root command list and construct stack
    private final List<RuleFlowCommand> rootCommands = new ArrayList<>();
    private final Deque<FlowConstruct> stack = new ArrayDeque<>();

    // The single pending construct. Every pipeline method registers a construct here;
    // the previous one is sealed and added to the active command list when a new one arrives.
    private CommandConstruct lastConstruct;

    protected RuleFlowBuilderTemplate() {
        super();
    }

    /**
     * Creates a fresh, empty instance of this builder type.
     *
     * <p>Used by {@link #buildBody} to collect a nested command body (an exception handler's
     * body, or an {@code AsyncRunSpec#thenRun} continuation) in an isolated builder rather than
     * on the current builder's stack.
     *
     * @return a new instance of {@code SELF}; never null.
     */
    protected abstract SELF newInstance();

    @SuppressWarnings("unchecked")
    protected final SELF self() {
        return (SELF) this;
    }

    /** Returns the active command list — top of stack while inside a construct, root otherwise. */
    protected final List<RuleFlowCommand> current() {
        return stack.isEmpty() ? rootCommands : stack.peek().commands();
    }

    /**
     * Registers a construct as the new pending element.
     *
     * <p>Seals any previously pending construct first (producing its immutable command and
     * appending it to the active list), then stores the new construct. The construct is
     * sealed lazily — when the next construct arrives or when {@link #build()} is called.
     *
     * @param construct the construct to register; must not be null.
     */
    protected final void addConstruct(CommandConstruct construct) {
        Assert.notNull(construct, "construct cannot be null.");
        sealLastConstruct();
        lastConstruct = construct;
    }

    /**
     * Seals and flushes the pending construct (if any) into the active command list.
     * No-op when there is no pending construct.
     */
    private void sealLastConstruct() {
        if (lastConstruct != null) {
            current().add(lastConstruct.seal());
            lastConstruct = null;
        }
    }

    /**
     * Returns the {@code context(...)} configurator set on this builder, or {@code null} if none.
     * Package-private accessor used by {@link #buildBody} to detect a nested {@code context()}
     * call on an isolated inner builder instance.
     */
    Consumer<RuleContextBuilder> getContextConfigurator() {
        return contextConfigurator;
    }

    /**
     * Seals any pending construct and returns a snapshot of the root command list.
     * Used by {@link #buildBody} to harvest commands from an inner builder.
     */
    List<RuleFlowCommand> collectBodyCommands() {
        sealLastConstruct();
        return new ArrayList<>(rootCommands);
    }

    /**
     * Sets the flow name (must match {@code [a-zA-Z$_][a-zA-Z0-9$_]*}).
     *
     * @param name the flow name; must not be null or empty.
     * @return this builder.
     */
    public SELF name(String name) {
        Assert.isTrue(RuleUtils.isValidName(name), "RuleFlow name [" + name + "] not valid. It must conform to ["
                + RuleUtils.NAME_REGEX + "]");
        this.name = name;
        return self();
    }

    /**
     * Sets an optional human-readable description for this flow.
     *
     * @param description description text.
     * @return this builder.
     */
    public SELF description(String description) {
        this.description = description;
        return self();
    }

    /**
     * Declares a required input parameter. The flow will throw at runtime if the binding is absent.
     *
     * @param name the binding name; must not be null or empty.
     * @param type the expected type; must not be null.
     * @return this builder.
     */
    public <P> SELF param(String name, Class<P> type) {
        return param(name, type, true);
    }

    /**
     * Declares an input parameter with an explicit required flag.
     *
     * @param name     the binding name.
     * @param type     the expected type.
     * @param required {@code true} if the binding must exist.
     * @return this builder.
     */
    public <P> SELF param(String name, Class<P> type, boolean required) {
        Assert.hasText(name, "name cannot be empty/null.");
        Assert.notNull(type, "type cannot be null.");
        inputParameters.add(new InputParameter<>(name, type, required, null));
        return self();
    }

    /**
     * Declares an optional input parameter with a default value function.
     *
     * @param name         the binding name.
     * @param type         the expected type.
     * @param defaultValue function supplying the default when the binding is absent.
     * @return this builder.
     */
    public <P> SELF param(String name, Class<P> type, Function<P> defaultValue) {
        Assert.hasText(name, "name cannot be empty/null.");
        Assert.notNull(type, "type cannot be null.");
        Assert.notNull(defaultValue, "defaultValue cannot be null.");
        inputParameters.add(new InputParameter<>(name, type, false, defaultValue));
        return self();
    }

    /**
     * Registers an action that always runs after command iteration completes, regardless of
     * early exit or exceptions.
     *
     * @param action the finalizer action; must not be null.
     * @return this builder.
     */
    public SELF finalizer(Action action) {
        Assert.notNull(action, "action cannot be null.");
        sealLastConstruct();
        this.finalizer = action;
        return self();
    }

    /**
     * Sets the typed result extractor evaluated when the flow completes naturally.
     *
     * @param extractor function that extracts {@code T} from the bindings; must not be null.
     * @return this builder.
     */
    public <T> SELF returning(Function<T> extractor) {
        Assert.notNull(extractor, "extractor cannot be null.");
        sealLastConstruct();
        this.resultExtractor = extractor;
        return self();
    }

    /**
     * Sets the result to the current {@link org.rulii.context.RuleContext} (no-arg shorthand).
     *
     * @return this builder.
     */
    public SELF returning() {
        sealLastConstruct();
        this.resultExtractor = null;
        return self();
    }

    /**
     * Customizes the {@link org.rulii.context.RuleContext} used when the flow is invoked
     * without an explicit context, or used to layer settings on top of a caller-supplied
     * context via {@code run(RuleContext)}. Must be the first pipeline step, and may only
     * be called once per flow.
     *
     * @param configurator receives a pre-populated {@link RuleContextBuilder}; must not be null.
     * @return this builder.
     */
    public SELF context(Consumer<RuleContextBuilder> configurator) {
        Assert.notNull(configurator, "configurator cannot be null.");
        Assert.isTrue(contextConfigurator == null && rootCommands.isEmpty() && stack.isEmpty() && lastConstruct == null,
                "context() must be the first step in the flow, and may only be called once.");
        this.contextConfigurator = configurator;
        return self();
    }

    /**
     * Adds one or more bindings to the current scope.
     *
     * @param declarations binding declarations; must not be null.
     * @return this builder.
     */
    public SELF bind(BindingDeclaration<?>... declarations) {
        Assert.notNull(declarations, "declarations cannot be null.");
        addConstruct(new BindConstruct(bindings -> bindings.bind(declarations)));
        return self();
    }

    /**
     * Adds a single named binding with an explicit value to the current scope.
     *
     * @param name  the binding name; must not be null or empty.
     * @param value the initial value; may be null.
     * @return this builder.
     */
    public SELF bind(String name, Object value) {
        Assert.hasText(name, "name cannot be empty/null.");
        addConstruct(new BindConstruct(bindings -> bindings.bind(name, value)));
        return self();
    }

    /**
     * Copies all bindings from an existing {@link Bindings} instance into the current scope.
     *
     * @param source the source bindings to copy from; must not be null.
     * @return this builder.
     */
    public SELF bind(Bindings source) {
        Assert.notNull(source, "source cannot be null.");
        addConstruct(new BindConstruct(bindings -> {
            for (Binding<?> b : source) bindings.bind(b);
        }));
        return self();
    }

    /**
     * Binds properties or map entries from {@code object} into the current scope.
     * If {@code object} is a {@code Map<String, Object>} the map entries are loaded;
     * otherwise JavaBean properties are loaded via {@code PropertyBindingLoader}.
     *
     * @param object the source object; must not be null.
     * @return this builder.
     */
    @SuppressWarnings("unchecked")
    public SELF bind(Object object) {
        Assert.notNull(object, "object cannot be null.");
        addConstruct(new BindConstruct(bindings -> {
            if (object instanceof Map<?, ?> m) bindings.loadMap((Map<String, Object>) m);
            else bindings.loadProperties(object);
        }));
        return self();
    }

    /**
     * Loads bindings from {@code value} using a custom {@link BindingLoader} into the current scope.
     *
     * @param loader the loader strategy; must not be null.
     * @param value  the source value; must not be null.
     * @return this builder.
     */
    public <T> SELF bind(BindingLoader<T> loader, T value) {
        Assert.notNull(loader, "loader cannot be null.");
        Assert.notNull(value,  "value cannot be null.");
        addConstruct(new BindConstruct(bindings -> bindings.load(loader, value)));
        return self();
    }

    /**
     * Adds one or more bindings into a specific named scope.
     *
     * @param scopeName    the target scope; must not be null or empty.
     * @param declarations binding declarations; must not be null.
     * @return this builder.
     */
    public SELF bindTo(String scopeName, BindingDeclaration<?>... declarations) {
        Assert.hasText(scopeName, "scopeName cannot be empty/null.");
        Assert.notNull(declarations, "declarations cannot be null.");
        addConstruct(new BindConstruct(bindings -> bindings.getScopeBindings(scopeName).bind(declarations)));
        return self();
    }

    /**
     * Adds a single named binding into a specific named scope.
     *
     * @param scopeName the target scope; must not be null or empty.
     * @param name      the binding name; must not be null or empty.
     * @param value     the initial value; may be null.
     * @return this builder.
     */
    public SELF bindTo(String scopeName, String name, Object value) {
        Assert.hasText(scopeName, "scopeName cannot be empty/null.");
        Assert.hasText(name, "name cannot be empty/null.");
        addConstruct(new BindConstruct(bindings -> bindings.getScopeBindings(scopeName).bind(name, value)));
        return self();
    }

    /**
     * Copies all bindings from an existing {@link Bindings} instance into a specific named scope.
     *
     * @param scopeName the target scope; must not be null or empty.
     * @param source    the source bindings to copy from; must not be null.
     * @return this builder.
     */
    public SELF bindTo(String scopeName, Bindings source) {
        Assert.hasText(scopeName, "scopeName cannot be empty/null.");
        Assert.notNull(source, "source cannot be null.");
        addConstruct(new BindConstruct(bindings -> {
            Bindings target = bindings.getScopeBindings(scopeName);
            for (Binding<?> b : source) target.bind(b);
        }));
        return self();
    }

    /**
     * Binds properties or map entries from {@code object} into a specific named scope.
     *
     * @param scopeName the target scope; must not be null or empty.
     * @param object    the source object; must not be null.
     * @return this builder.
     */
    @SuppressWarnings("unchecked")
    public SELF bindTo(String scopeName, Object object) {
        Assert.hasText(scopeName, "scopeName cannot be empty/null.");
        Assert.notNull(object, "object cannot be null.");
        addConstruct(new BindConstruct(bindings -> {
            Bindings target = bindings.getScopeBindings(scopeName);
            if (object instanceof Map<?, ?> m) target.loadMap((Map<String, Object>) m);
            else target.loadProperties(object);
        }));
        return self();
    }

    /**
     * Loads bindings from {@code value} using a custom {@link BindingLoader} into a specific named scope.
     *
     * @param scopeName the target scope; must not be null or empty.
     * @param loader    the loader strategy; must not be null.
     * @param value     the source value; must not be null.
     * @return this builder.
     */
    public <T> SELF bindTo(String scopeName, BindingLoader<T> loader, T value) {
        Assert.hasText(scopeName, "scopeName cannot be empty/null.");
        Assert.notNull(loader, "loader cannot be null.");
        Assert.notNull(value,  "value cannot be null.");
        addConstruct(new BindConstruct(bindings -> bindings.getScopeBindings(scopeName).load(loader, value)));
        return self();
    }

    /**
     * Executes a pre-built {@link Rule}.
     *
     * @param rule the rule to run; must not be null.
     * @return this builder.
     */
    public SELF run(Rule rule) {
        Assert.notNull(rule, "rule cannot be null.");
        addConstruct(RunConstruct.of(rule));
        return self();
    }

    /**
     * Executes a pre-built {@link Rule} with step-level configuration.
     *
     * @param rule   the rule to run; must not be null.
     * @param config Consumer that configures the step via {@link RunSpec}; must not be null.
     * @return this builder.
     */
    public SELF run(Rule rule, Consumer<RunSpec<SELF>> config) {
        Assert.notNull(rule, "rule cannot be null.");
        Assert.notNull(config, "config cannot be null.");
        sealLastConstruct();
        RunConstruct construct = RunConstruct.of(rule);
        config.accept(new RunSpec<>(construct, this));
        lastConstruct = construct;
        return self();
    }

    /**
     * Executes a pre-built {@link RuleSet}.
     *
     * @param ruleSet the rule set to run; must not be null.
     * @return this builder.
     */
    public SELF run(RuleSet<?> ruleSet) {
        Assert.notNull(ruleSet, "ruleSet cannot be null.");
        addConstruct(RunConstruct.of(ruleSet));
        return self();
    }

    /**
     * Executes a pre-built {@link RuleSet} with step-level configuration.
     *
     * @param ruleSet the rule set to run; must not be null.
     * @param config  Consumer that configures the step via {@link RunSpec}; must not be null.
     * @return this builder.
     */
    public SELF run(RuleSet<?> ruleSet, Consumer<RunSpec<SELF>> config) {
        Assert.notNull(ruleSet, "ruleSet cannot be null.");
        Assert.notNull(config, "config cannot be null.");
        sealLastConstruct();
        RunConstruct construct = RunConstruct.of(ruleSet);
        config.accept(new RunSpec<>(construct, this));
        lastConstruct = construct;
        return self();
    }

    /**
     * Executes a nested {@link RuleFlow}.
     *
     * @param ruleFlow the nested flow to run; must not be null.
     * @return this builder.
     */
    public SELF run(RuleFlow<?> ruleFlow) {
        Assert.notNull(ruleFlow, "ruleFlow cannot be null.");
        addConstruct(RunConstruct.of(ruleFlow));
        return self();
    }

    /**
     * Executes a nested {@link RuleFlow} with step-level configuration.
     *
     * @param ruleFlow the nested flow to run; must not be null.
     * @param config   Consumer that configures the step via {@link RunSpec}; must not be null.
     * @return this builder.
     */
    public SELF run(RuleFlow<?> ruleFlow, Consumer<RunSpec<SELF>> config) {
        Assert.notNull(ruleFlow, "ruleFlow cannot be null.");
        Assert.notNull(config, "config cannot be null.");
        sealLastConstruct();
        RunConstruct construct = RunConstruct.of(ruleFlow);
        config.accept(new RunSpec<>(construct, this));
        lastConstruct = construct;
        return self();
    }

    /**
     * Executes a runnable looked up by name from the {@link org.rulii.registry.RuleRegistry}.
     *
     * @param nameInRegistry name to look up at runtime; must not be null or empty.
     * @return this builder.
     */
    public SELF run(String nameInRegistry) {
        Assert.hasText(nameInRegistry, "nameInRegistry cannot be empty/null.");
        addConstruct(RunConstruct.ofName(nameInRegistry));
        return self();
    }

    /**
     * Executes a runnable looked up by name from the {@link org.rulii.registry.RuleRegistry},
     * with step-level configuration.
     *
     * @param nameInRegistry name to look up at runtime; must not be null or empty.
     * @param config       Consumer that configures the step via {@link RunSpec}; must not be null.
     * @return this builder.
     */
    public SELF run(String nameInRegistry, Consumer<RunSpec<SELF>> config) {
        Assert.hasText(nameInRegistry, "nameInRegistry cannot be empty/null.");
        Assert.notNull(config, "config cannot be null.");
        sealLastConstruct();
        RunConstruct construct = RunConstruct.ofName(nameInRegistry);
        config.accept(new RunSpec<>(construct, this));
        lastConstruct = construct;
        return self();
    }

    /**
     * Executes a rule looked up by class from the {@link org.rulii.registry.RuleRegistry}.
     *
     * @param registryClass class to look up at runtime; must not be null.
     * @return this builder.
     */
    public SELF run(Class<?> registryClass) {
        Assert.notNull(registryClass, "registryClass cannot be null.");
        addConstruct(RunConstruct.ofClass(registryClass));
        return self();
    }

    /**
     * Executes a rule looked up by class from the {@link org.rulii.registry.RuleRegistry},
     * with step-level configuration.
     *
     * @param registryClass class to look up at runtime; must not be null.
     * @param config        Consumer that configures the step via {@link RunSpec}; must not be null.
     * @return this builder.
     */
    public SELF run(Class<?> registryClass, Consumer<RunSpec<SELF>> config) {
        Assert.notNull(registryClass, "registryClass cannot be null.");
        Assert.notNull(config, "config cannot be null.");
        sealLastConstruct();
        RunConstruct construct = RunConstruct.ofClass(registryClass);
        config.accept(new RunSpec<>(construct, this));
        lastConstruct = construct;
        return self();
    }

    /**
     * Applies a {@link Function}, making its result available in subsequent steps.
     *
     * @param fn the function to apply; must not be null.
     * @return this builder.
     */
    public SELF apply(Function<?> fn) {
        Assert.notNull(fn, "fn cannot be null.");
        addConstruct(RunConstruct.of(fn));
        return self();
    }

    /**
     * Applies a {@link Function} with step-level configuration.
     *
     * @param fn     the function to apply; must not be null.
     * @param config Consumer that configures the step via {@link RunSpec}; must not be null.
     * @return this builder.
     */
    public SELF apply(Function<?> fn, Consumer<RunSpec<SELF>> config) {
        Assert.notNull(fn, "fn cannot be null.");
        Assert.notNull(config, "config cannot be null.");
        sealLastConstruct();
        RunConstruct construct = RunConstruct.of(fn);
        config.accept(new RunSpec<>(construct, this));
        lastConstruct = construct;
        return self();
    }

    /**
     * Executes an {@link Action} (void side-effect).
     *
     * @param action the action to execute; must not be null.
     * @return this builder.
     */
    public SELF execute(Action action) {
        Assert.notNull(action, "action cannot be null.");
        addConstruct(RunConstruct.of(action));
        return self();
    }

    /**
     * Executes an {@link Action} with step-level configuration.
     *
     * @param action the action to execute; must not be null.
     * @param config Consumer that configures the step via {@link ExecuteSpec}; must not be null.
     * @return this builder.
     */
    public SELF execute(Action action, Consumer<ExecuteSpec<SELF>> config) {
        Assert.notNull(action, "action cannot be null.");
        Assert.notNull(config, "config cannot be null.");
        sealLastConstruct();
        RunConstruct construct = RunConstruct.of(action);
        config.accept(new ExecuteSpec<>(construct, this));
        lastConstruct = construct;
        return self();
    }

    /**
     * Launches a pre-built {@link Rule} asynchronously. The resulting
     * {@link java.util.concurrent.CompletableFuture} is not bound to any name.
     *
     * @param rule the rule to run; must not be null.
     * @return this builder.
     */
    public SELF asyncRun(Rule rule) {
        Assert.notNull(rule, "rule cannot be null.");
        addConstruct(AsyncRunConstruct.of(rule));
        return self();
    }

    /**
     * Launches a pre-built {@link Rule} asynchronously with step-level configuration.
     *
     * @param rule   the rule to run; must not be null.
     * @param config Consumer that configures the step via {@link AsyncRunSpec}; must not be null.
     * @return this builder.
     */
    public SELF asyncRun(Rule rule, Consumer<AsyncRunSpec<SELF>> config) {
        Assert.notNull(rule, "rule cannot be null.");
        Assert.notNull(config, "config cannot be null.");
        sealLastConstruct();
        AsyncRunConstruct construct = AsyncRunConstruct.of(rule);
        config.accept(new AsyncRunSpec<>(construct, this));
        lastConstruct = construct;
        return self();
    }

    /**
     * Launches a pre-built {@link RuleSet} asynchronously. The resulting
     * {@link java.util.concurrent.CompletableFuture} is not bound to any name.
     *
     * @param ruleSet the rule set to run; must not be null.
     * @return this builder.
     */
    public SELF asyncRun(RuleSet<?> ruleSet) {
        Assert.notNull(ruleSet, "ruleSet cannot be null.");
        addConstruct(AsyncRunConstruct.of(ruleSet));
        return self();
    }

    /**
     * Launches a pre-built {@link RuleSet} asynchronously with step-level configuration.
     *
     * @param ruleSet the rule set to run; must not be null.
     * @param config  Consumer that configures the step via {@link AsyncRunSpec}; must not be null.
     * @return this builder.
     */
    public SELF asyncRun(RuleSet<?> ruleSet, Consumer<AsyncRunSpec<SELF>> config) {
        Assert.notNull(ruleSet, "ruleSet cannot be null.");
        Assert.notNull(config, "config cannot be null.");
        sealLastConstruct();
        AsyncRunConstruct construct = AsyncRunConstruct.of(ruleSet);
        config.accept(new AsyncRunSpec<>(construct, this));
        lastConstruct = construct;
        return self();
    }

    /**
     * Launches a nested {@link RuleFlow} asynchronously. The resulting
     * {@link java.util.concurrent.CompletableFuture} is not bound to any name.
     *
     * @param ruleFlow the nested flow to run; must not be null.
     * @return this builder.
     */
    public SELF asyncRun(RuleFlow<?> ruleFlow) {
        Assert.notNull(ruleFlow, "ruleFlow cannot be null.");
        addConstruct(AsyncRunConstruct.of(ruleFlow));
        return self();
    }

    /**
     * Launches a nested {@link RuleFlow} asynchronously with step-level configuration.
     *
     * @param ruleFlow the nested flow to run; must not be null.
     * @param config   Consumer that configures the step via {@link AsyncRunSpec}; must not be null.
     * @return this builder.
     */
    public SELF asyncRun(RuleFlow<?> ruleFlow, Consumer<AsyncRunSpec<SELF>> config) {
        Assert.notNull(ruleFlow, "ruleFlow cannot be null.");
        Assert.notNull(config, "config cannot be null.");
        sealLastConstruct();
        AsyncRunConstruct construct = AsyncRunConstruct.of(ruleFlow);
        config.accept(new AsyncRunSpec<>(construct, this));
        lastConstruct = construct;
        return self();
    }

    /**
     * Launches a runnable looked up by name from the {@link org.rulii.registry.RuleRegistry}
     * asynchronously. The resulting {@link java.util.concurrent.CompletableFuture} is not bound.
     *
     * @param nameInRegistry name to look up at runtime; must not be null or empty.
     * @return this builder.
     */
    public SELF asyncRun(String nameInRegistry) {
        Assert.hasText(nameInRegistry, "nameInRegistry cannot be empty/null.");
        addConstruct(AsyncRunConstruct.ofName(nameInRegistry));
        return self();
    }

    /**
     * Launches a runnable looked up by name from the {@link org.rulii.registry.RuleRegistry}
     * asynchronously, with step-level configuration.
     *
     * @param nameInRegistry name to look up at runtime; must not be null or empty.
     * @param config       Consumer that configures the step via {@link AsyncRunSpec}; must not be null.
     * @return this builder.
     */
    public SELF asyncRun(String nameInRegistry, Consumer<AsyncRunSpec<SELF>> config) {
        Assert.hasText(nameInRegistry, "nameInRegistry cannot be empty/null.");
        Assert.notNull(config, "config cannot be null.");
        sealLastConstruct();
        AsyncRunConstruct construct = AsyncRunConstruct.ofName(nameInRegistry);
        config.accept(new AsyncRunSpec<>(construct, this));
        lastConstruct = construct;
        return self();
    }

    /**
     * Launches a rule looked up by class from the {@link org.rulii.registry.RuleRegistry}
     * asynchronously. The resulting {@link java.util.concurrent.CompletableFuture} is not bound.
     *
     * @param registryClass class to look up at runtime; must not be null.
     * @return this builder.
     */
    public SELF asyncRun(Class<?> registryClass) {
        Assert.notNull(registryClass, "registryClass cannot be null.");
        addConstruct(AsyncRunConstruct.ofClass(registryClass));
        return self();
    }

    /**
     * Launches a rule looked up by class from the {@link org.rulii.registry.RuleRegistry}
     * asynchronously, with step-level configuration.
     *
     * @param registryClass class to look up at runtime; must not be null.
     * @param config        Consumer that configures the step via {@link AsyncRunSpec}; must not be null.
     * @return this builder.
     */
    public SELF asyncRun(Class<?> registryClass, Consumer<AsyncRunSpec<SELF>> config) {
        Assert.notNull(registryClass, "registryClass cannot be null.");
        Assert.notNull(config, "config cannot be null.");
        sealLastConstruct();
        AsyncRunConstruct construct = AsyncRunConstruct.ofClass(registryClass);
        config.accept(new AsyncRunSpec<>(construct, this));
        lastConstruct = construct;
        return self();
    }

    /**
     * Blocks until the named {@link java.util.concurrent.CompletableFuture} binding completes,
     * with a default timeout of 30 seconds.
     *
     * <p>The binding is not replaced after completion — it remains a
     * {@code CompletableFuture<T>} so subsequent steps can call {@code future.getNow(null)}.
     *
     * @param bindingName the name of the future binding; must not be null or empty.
     * @return this builder.
     */
    public SELF await(String bindingName) {
        Assert.hasText(bindingName, "bindingName cannot be empty/null.");
        addConstruct(new DirectCommandConstruct(new AwaitCommand(bindingName, 30, TimeUnit.SECONDS)));
        return self();
    }

    /**
     * Blocks until the named {@link java.util.concurrent.CompletableFuture} binding completes,
     * with an explicit timeout.
     *
     * @param bindingName the name of the future binding; must not be null or empty.
     * @param timeout     maximum time to wait.
     * @param timeUnit    time unit for the timeout; must not be null.
     * @return this builder.
     */
    public SELF await(String bindingName, long timeout, TimeUnit timeUnit) {
        Assert.hasText(bindingName, "bindingName cannot be empty/null.");
        Assert.notNull(timeUnit, "timeUnit cannot be null.");
        addConstruct(new DirectCommandConstruct(new AwaitCommand(bindingName, timeout, timeUnit)));
        return self();
    }

    /**
     * Blocks until <em>all</em> named {@link java.util.concurrent.CompletableFuture} bindings
     * complete, with a default timeout of 30 seconds.
     *
     * @param bindingNames the names of the future bindings; must not be null or empty.
     * @return this builder.
     */
    public SELF awaitAll(String... bindingNames) {
        Assert.notNull(bindingNames, "bindingNames cannot be null.");
        addConstruct(new DirectCommandConstruct(new AwaitAllCommand(30, TimeUnit.SECONDS, bindingNames)));
        return self();
    }

    /**
     * Blocks until <em>all</em> named {@link java.util.concurrent.CompletableFuture} bindings
     * complete, with an explicit timeout.
     *
     * @param timeout      maximum time to wait.
     * @param timeUnit     time unit for the timeout; must not be null.
     * @param bindingNames the names of the future bindings; must not be null or empty.
     * @return this builder.
     */
    public SELF awaitAll(long timeout, TimeUnit timeUnit, String... bindingNames) {
        Assert.notNull(timeUnit, "timeUnit cannot be null.");
        Assert.notNull(bindingNames, "bindingNames cannot be null.");
        addConstruct(new DirectCommandConstruct(new AwaitAllCommand(timeout, timeUnit, bindingNames)));
        return self();
    }

    /**
     * Blocks until <em>any one</em> of the named {@link java.util.concurrent.CompletableFuture}
     * bindings completes, with a default timeout of 30 seconds.
     *
     * @param bindingNames the names of the future bindings; must not be null or empty.
     * @return this builder.
     */
    public SELF awaitAny(String... bindingNames) {
        Assert.notNull(bindingNames, "bindingNames cannot be null.");
        addConstruct(new DirectCommandConstruct(new AwaitAnyCommand(30, TimeUnit.SECONDS, bindingNames)));
        return self();
    }

    /**
     * Blocks until <em>any one</em> of the named {@link java.util.concurrent.CompletableFuture}
     * bindings completes, with an explicit timeout.
     *
     * @param timeout      maximum time to wait.
     * @param timeUnit     time unit for the timeout; must not be null.
     * @param bindingNames the names of the future bindings; must not be null or empty.
     * @return this builder.
     */
    public SELF awaitAny(long timeout, TimeUnit timeUnit, String... bindingNames) {
        Assert.notNull(timeUnit, "timeUnit cannot be null.");
        Assert.notNull(bindingNames, "bindingNames cannot be null.");
        addConstruct(new DirectCommandConstruct(new AwaitAnyCommand(timeout, timeUnit, bindingNames)));
        return self();
    }

    /**
     * Injects a pre-built {@link RuleFlowCommand} directly into the pipeline.
     *
     * <p>Use this when the built-in step methods ({@code run()}, {@code apply()},
     * {@code execute()}) do not cover the required behaviour.
     *
     * <pre>{@code
     * builder.command(new MetricsCommand("checkout"))
     * }</pre>
     *
     * @param cmd the command to inject; must not be null.
     * @return this builder.
     */
    public SELF command(RuleFlowCommand cmd) {
        Assert.notNull(cmd, "cmd cannot be null.");
        addConstruct(new DirectCommandConstruct(cmd));
        return self();
    }

    /**
     * Registers a flow-level global exception handler.
     *
     * <p>The global handler is invoked when an exception escapes a step that has no matching
     * step-level handler. After the handler body completes, execution continues with the next
     * command in the flow.
     *
     * <p>To attach a handler to a specific step, use the step's spec consumer:
     * <pre>{@code
     * .run(myRule, spec -> spec.onException(Exception.class, b -> b.bind(handled -> true)))
     * }</pre>
     *
     * @param type    the exception type to catch; must not be null.
     * @param handler Consumer that defines the handler commands; must not be null.
     * @return this builder.
     */
    public <E extends Exception> SELF onException(Class<E> type, Consumer<SELF> handler) {
        Assert.notNull(type, "type cannot be null.");
        Assert.notNull(handler, "handler cannot be null.");
        sealLastConstruct();
        this.globalHandler = buildExceptionHandler(type, handler);
        return self();
    }

    /**
     * Builds a {@link RuleFlowExceptionHandler} by executing the handler body within a temporary
     * anonymous {@link FlowConstruct} pushed onto the builder's stack to capture the body commands.
     *
     * <p>Called by {@link RunSpec} and {@link ExecuteSpec} for step-level handlers, and by
     * {@link #onException} for the flow-level global handler.
     *
     * @param type    the exception type; must not be null.
     * @param handler the handler body consumer; must not be null.
     * @return the sealed handler; never null.
     */
    final <E extends Exception> RuleFlowExceptionHandler buildExceptionHandler(Class<E> type, Consumer<SELF> handler) {
        RuleFlowExceptionHandler result = new RuleFlowExceptionHandler(type);
        result.setBody(buildBody(handler));
        return result;
    }

    /**
     * Captures a nested command body in an isolated {@code SELF} instance without adding it
     * to this builder's active command list. Used for bodies that attach to a spec/handler
     * object rather than running inline in the pipeline (e.g. {@link #buildExceptionHandler}
     * and {@code AsyncRunSpec#thenRun}).
     *
     * @param body the body consumer; must not be null.
     * @return the captured commands; never null.
     * @throws IllegalArgumentException if {@code body} calls {@code context(...)} - a nested
     *         body runs against an already-built {@link org.rulii.context.RuleContext} that
     *         this isolated inner builder never constructs, so a configurator set there would
     *         otherwise be silently discarded.
     */
    final List<RuleFlowCommand> buildBody(Consumer<SELF> body) {
        Assert.notNull(body, "body cannot be null.");
        SELF inner = newInstance();
        body.accept(inner);
        Assert.isTrue(inner.getContextConfigurator() == null,
                "context() is not supported inside a handler/continuation body.");
        return inner.collectBodyCommands();
    }

    /**
     * Evaluates {@code condition} and executes the then-branch when true.
     *
     * @param condition the condition to test; must not be null.
     * @param then      Consumer that defines the then-branch commands; must not be null.
     * @return this builder.
     */
    public SELF when(Condition condition, Consumer<SELF> then) {
        return when(condition, then, null);
    }

    /**
     * Evaluates {@code condition} and executes the matching branch.
     *
     * @param condition the condition to test; must not be null.
     * @param then      Consumer that defines the then-branch commands; must not be null.
     * @param otherwise Consumer that defines the otherwise-branch commands; may be null.
     * @return this builder.
     */
    public SELF when(Condition condition, Consumer<SELF> then, Consumer<SELF> otherwise) {
        Assert.notNull(condition, "condition cannot be null.");
        Assert.notNull(then, "then cannot be null.");

        // flush pending construct before entering the body
        sealLastConstruct();
        WhenConstruct construct = new WhenConstruct(condition);
        stack.push(construct);
        try {
            then.accept(self());
            // flush any construct at the end of the then-body
            sealLastConstruct();

            if (otherwise != null) {
                construct.switchToOtherwise();
                otherwise.accept(self());
                // flush any construct at the end of the otherwise-body
                sealLastConstruct();
            }
        } finally {
            stack.pop();
        }

        // the filled WhenConstruct becomes the new pending element
        addConstruct(construct);
        return self();
    }

    /**
     * Iterates a collection. The framework binds each element under {@code elementName}
     * and the 0-based position under {@code index} for each iteration.
     *
     * @param source      function resolving the collection at runtime; must not be null.
     * @param elementName binding name for each element; must not be null or empty.
     * @param body        Consumer defining the body commands; must not be null.
     * @return this builder.
     */
    public SELF forEach(Function<?> source, String elementName, Consumer<SELF> body) {
        return forEach(source, elementName, null, body);
    }

    /**
     * Iterates a collection with an optional early-stop condition.
     *
     * @param source        function resolving the collection at runtime; must not be null.
     * @param elementName   binding name for each element; must not be null or empty.
     * @param stopCondition evaluated after each element; iteration halts when true. May be null.
     * @param body          Consumer defining the body commands; must not be null.
     * @return this builder.
     */
    public SELF forEach(Function<?> source, String elementName, Condition stopCondition, Consumer<SELF> body) {
        Assert.notNull(source, "source cannot be null.");
        Assert.hasText(elementName, "elementName cannot be empty/null.");
        Assert.notNull(body, "body cannot be null.");

        sealLastConstruct();
        ForEachConstruct construct = new ForEachConstruct(source, elementName, stopCondition);
        stack.push(construct);
        try {
            body.accept(self());
            sealLastConstruct();
        } finally {
            stack.pop();
        }
        addConstruct(construct);
        return self();
    }

    /**
     * Pushes a named binding scope for the duration of the body.
     * Bindings created inside the body are discarded when the scope ends.
     *
     * @param scopeName the scope name; must not be null or empty.
     * @param body      Consumer defining commands inside the scope; must not be null.
     * @return this builder.
     */
    public SELF scope(String scopeName, Consumer<SELF> body) {
        Assert.hasText(scopeName, "scopeName cannot be empty/null.");
        Assert.notNull(body, "body cannot be null.");

        sealLastConstruct();
        ScopeConstruct construct = new ScopeConstruct(scopeName);
        stack.push(construct);
        try {
            body.accept(self());
            sealLastConstruct();
        } finally {
            stack.pop();
        }
        addConstruct(construct);
        return self();
    }

    /**
     * Pushes an anonymous binding scope for the duration of the body.
     *
     * @param body Consumer defining commands inside the scope; must not be null.
     * @return this builder.
     */
    public SELF scope(Consumer<SELF> body) {
        Assert.notNull(body, "body cannot be null.");

        sealLastConstruct();
        ScopeConstruct construct = new ScopeConstruct(null);
        stack.push(construct);
        try {
            body.accept(self());
            sealLastConstruct();
        } finally {
            stack.pop();
        }
        addConstruct(construct);
        return self();
    }

    /**
     * Terminates the flow immediately, returning the result of {@code extractor}.
     * Commands after this at the top level are unreachable (detected at build time).
     *
     * @param extractor function that extracts the result; must not be null.
     * @return this builder.
     */
    public <T> SELF exit(Function<T> extractor) {
        Assert.notNull(extractor, "extractor cannot be null.");
        addConstruct(new ExitConstruct(extractor));
        return self();
    }

    /**
     * Terminates the flow immediately, returning the current
     * {@link org.rulii.context.RuleContext}.
     *
     * @return this builder.
     */
    public SELF exit() {
        addConstruct(new ExitConstruct(null));
        return self();
    }

    /**
     * Extension hook for custom container commands.
     *
     * <p>Implementors call this from their own named method to participate in the
     * same Consumer-body pattern as built-in constructs:
     * <pre>{@code
     * public SELF retry(int maxAttempts, Consumer<SELF> body) {
     *     return runContainer(new RetryCommand(maxAttempts), body);
     * }
     * }</pre>
     *
     * @param cmd  the custom container command; must not be null.
     * @param body Consumer defining the body commands; must not be null.
     * @return this builder.
     */
    protected SELF runContainer(ContainerCommand cmd, Consumer<SELF> body) {
        Assert.notNull(cmd, "cmd cannot be null.");
        Assert.notNull(body, "body cannot be null.");

        sealLastConstruct();
        CustomContainerConstruct construct = new CustomContainerConstruct(cmd);
        stack.push(construct);
        try {
            body.accept(self());
            sealLastConstruct();
        } finally {
            stack.pop();
        }
        addConstruct(construct);
        return self();
    }

    /**
     * Validates the pipeline structure and constructs an immutable {@link RuleFlow}.
     *
     * @param <T> the result type.
     * @return a new {@link RuleFlow}; never null.
     * @throws UnrulyException if the pipeline structure is invalid.
     */
    @SuppressWarnings("unchecked")
    public <T> RuleFlow<T> build() {
        Assert.hasText(name, "name must be set before calling build().");
        sealLastConstruct();
        validate();

        List<InputParameter<?>> params = new ArrayList<>(inputParameters);
        Function<T> extractor = (Function<T>) resultExtractor;

        RuleFlowDefinition def = new RuleFlowDefinition(name, description, SourceDefinition.build(),
                extractor != null ? Object.class : null, rootCommands.size(), params);

        return new RulingOrder<>(def, new ArrayList<>(rootCommands), params, finalizer,
                extractor, globalHandler, contextConfigurator);
    }

    private void validate() {
        validateReachability();
    }

    private void validateReachability() {
        for (int i = 0; i < rootCommands.size(); i++) {
            if (rootCommands.get(i) instanceof ReturningCommand && i < rootCommands.size() - 1)
                throw new UnrulyException("RuleFlow [" + name + "] structural error: unreachable commands after exit() at top level.");
        }
    }

    protected String getName() {
        return name;
    }
}
