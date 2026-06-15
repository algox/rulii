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
import org.rulii.context.RuleContextBuilder;
import org.rulii.ruleflow.command.*;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.InputParameter;
import org.rulii.model.SourceDefinition;
import org.rulii.model.UnrulyException;
import org.rulii.model.action.Action;
import org.rulii.model.condition.Condition;
import org.rulii.model.function.Function;
import org.rulii.rule.Rule;
import org.rulii.ruleset.RuleSet;
import org.rulii.util.RuleUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Fluent builder for constructing a {@link RuleFlow} pipeline.
 *
 * <p>Obtain instances via {@link RuleFlow#builder()}.
 *
 * <h3>Usage example</h3>
 * <pre>{@code
 * RuleFlow<String> flow = RuleFlow.builder()
 *     .name("myFlow")
 *     .param("orderId", String.class)
 *     .run(lookupOrderRule)
 *     .when(Condition.builder().build((String orderId) -> orderId != null))
 *         .run(processOrderRule)
 *     .returning((String result) -> result)
 *     .build();
 * }</pre>
 *
 * @author Max Arulananthan
 * @since 2.0
 */
public class RuleFlowBuilder {

    private String name;
    private String description;
    private final Set<InputParameter<?>> inputParameters = new LinkedHashSet<>();
    private Action finalizer;
    private Function<?> resultExtractor;
    private OnExceptionCommand globalHandler;
    private Consumer<RuleContextBuilder> contextConfigurator;
    private final List<RuleFlowCommand> commands = new ArrayList<>();

    RuleFlowBuilder() {
        super();
    }

    /**
     * Sets the flow name (must match {@code [a-zA-Z$_][a-zA-Z0-9$_]*}).
     *
     * @param name the flow name; must not be null or empty.
     * @return this builder.
     */
    public RuleFlowBuilder name(String name) {
        Assert.isTrue(RuleUtils.isValidName(name), "RuleFlow name [" + name + "] not valid. It must conform to ["
                + RuleUtils.NAME_REGEX + "]");
        this.name = name;
        return this;
    }

    /**
     * Sets an optional description for this flow.
     *
     * @param description human-readable description.
     * @return this builder.
     */
    public RuleFlowBuilder description(String description) {
        this.description = description;
        return this;
    }

    /**
     * Declares a required input parameter.
     *
     * @param name the binding name; must not be null or empty.
     * @param type the expected type; must not be null.
     * @return this builder.
     */
    public <P> RuleFlowBuilder param(String name, Class<P> type) {
        return param(name, type, true);
    }

    /**
     * Declares an input parameter with an explicit required flag.
     *
     * @param name the binding name; must not be null or empty.
     * @param type the expected type; must not be null.
     * @param required {@code true} if the binding must exist and have the correct type.
     * @return this builder.
     */
    public <P> RuleFlowBuilder param(String name, Class<P> type, boolean required) {
        Assert.hasText(name, "name cannot be empty/null.");
        Assert.notNull(type, "type cannot be null.");
        inputParameters.add(new InputParameter<>(name, type, required, null));
        return this;
    }

    /**
     * Declares an optional input parameter with a default value.
     *
     * @param name the binding name; must not be null or empty.
     * @param type the expected type; must not be null.
     * @param defaultValue function supplying the default when the binding is absent; must not be null.
     * @return this builder.
     */
    public <P> RuleFlowBuilder param(String name, Class<P> type, Function<P> defaultValue) {
        Assert.hasText(name, "name cannot be empty/null.");
        Assert.notNull(type, "type cannot be null.");
        Assert.notNull(defaultValue, "defaultValue cannot be null.");
        inputParameters.add(new InputParameter<>(name, type, false, defaultValue));
        return this;
    }

    /**
     * Registers an action that always runs after command iteration completes, regardless of
     * early exit via {@code returning()} or exceptions.
     *
     * @param action the finalizer action; must not be null.
     * @return this builder.
     */
    public RuleFlowBuilder finalizer(Action action) {
        Assert.notNull(action, "action cannot be null.");
        this.finalizer = action;
        return this;
    }

    /**
     * Registers a flow-level exception handler. When any step throws an unhandled
     * {@link UnrulyException} of the given type, the handler commands run in a dedicated scope
     * with the exception bound as {@code "ex"}.
     *
     * @param type the exception type to match; must not be null.
     * @param handler builder consumer defining the handler commands; must not be null.
     * @return this builder.
     */
    public <E extends Exception> RuleFlowBuilder onException(Class<E> type, Consumer<RuleFlowBuilder> handler) {
        Assert.notNull(type, "type cannot be null.");
        Assert.notNull(handler, "handler cannot be null.");
        RuleFlowBuilder handlerBuilder = new RuleFlowBuilder();
        handler.accept(handlerBuilder);
        this.globalHandler = new OnExceptionCommand(type, handlerBuilder.commands);
        return this;
    }

    /**
     * Customizes the {@link org.rulii.context.RuleContext} when the flow is invoked
     * via {@link RuleFlow#run(BindingDeclaration[])} (i.e. without an explicit context).
     * Must be the first pipeline call.
     *
     * @param configurator a consumer that receives a pre-populated {@link RuleContextBuilder}; must not be null.
     * @return this builder.
     */
    public RuleFlowBuilder context(Consumer<RuleContextBuilder> configurator) {
        Assert.notNull(configurator, "configurator cannot be null.");
        this.contextConfigurator = configurator;
        commands.add(new ContextCommand(configurator));
        return this;
    }

    /**
     * Configures the flow to extract a typed result using the given function when
     * execution reaches the end naturally (without an explicit {@code returning(value)}).
     *
     * @param extractor function applied to the {@link org.rulii.context.RuleContext}; must not be null.
     * @return this builder.
     */
    public <T> RuleFlowBuilder returning(Function<T> extractor) {
        Assert.notNull(extractor, "extractor cannot be null.");
        this.resultExtractor = extractor;
        return this;
    }

    /**
     * Sets the result to the current {@link org.rulii.context.RuleContext} (no-arg shorthand).
     *
     * @return this builder.
     */
    public RuleFlowBuilder returning() {
        this.resultExtractor = null;
        return this;
    }

    /**
     * Adds one or more bindings to the current scope.
     *
     * @param declarations binding declarations; must not be null.
     * @return this builder.
     */
    public RuleFlowBuilder bind(BindingDeclaration<?>... declarations) {
        Assert.notNull(declarations, "declarations cannot be null.");
        commands.add(new BindCommand(bindings -> bindings.bind(declarations)));
        return this;
    }

    /**
     * Adds a single named binding with an explicit value to the current scope.
     *
     * <p>Prefer this form over {@link #bind(BindingDeclaration[])} when the binding name is
     * a runtime {@code String} value or the value is a pre-computed expression.
     *
     * @param name the binding name; must not be null or empty.
     * @param value the initial value; may be null.
     * @return this builder.
     */
    public RuleFlowBuilder bind(String name, Object value) {
        Assert.hasText(name, "name cannot be empty/null.");
        commands.add(new BindCommand(bindings -> bindings.bind(name, value)));
        return this;
    }

    /**
     * Adds one or more bindings to a specific named scope.
     *
     * <p>The named scope must already exist on the scope stack when this command executes,
     * or an exception will be thrown at runtime.
     *
     * @param scopeName the name of the target scope; must not be null or empty.
     * @param declarations binding declarations; must not be null.
     * @return this builder.
     */
    public RuleFlowBuilder bindTo(String scopeName, BindingDeclaration<?>... declarations) {
        Assert.hasText(scopeName, "scopeName cannot be empty/null.");
        Assert.notNull(declarations, "declarations cannot be null.");
        commands.add(new BindCommand(bindings -> bindings.getScopeBindings(scopeName).bind(declarations)));
        return this;
    }

    /**
     * Adds a single named binding with an explicit value to a specific named scope.
     *
     * <p>The named scope must already exist on the scope stack when this command executes,
     * or an exception will be thrown at runtime.
     *
     * @param scopeName the name of the target scope; must not be null or empty.
     * @param name the binding name; must not be null or empty.
     * @param value the initial value; may be null.
     * @return this builder.
     */
    public RuleFlowBuilder bindTo(String scopeName, String name, Object value) {
        Assert.hasText(scopeName, "scopeName cannot be empty/null.");
        Assert.hasText(name, "name cannot be empty/null.");
        commands.add(new BindCommand(bindings -> bindings.getScopeBindings(scopeName).bind(name, value)));
        return this;
    }

    /**
     * Adds a scope push. Pair with {@link #endScope()}.
     *
     * @param scopeName the name for the new scope; must not be null or empty.
     * @return this builder.
     */
    public RuleFlowBuilder scope(String scopeName) {
        Assert.hasText(scopeName, "scopeName cannot be empty/null.");
        commands.add(new ScopeCommand(scopeName));
        return this;
    }

    /**
     * Adds an anonymous scope push. Pair with {@link #endScope()}.
     *
     * @return this builder.
     */
    public RuleFlowBuilder scope() {
        commands.add(new ScopeCommand(null));
        return this;
    }

    /**
     * Pops the current scope. Pair with a preceding {@link #scope()} or {@link #scope(String)}.
     *
     * @return this builder.
     */
    public RuleFlowBuilder endScope() {
        commands.add(new EndScopeCommand());
        return this;
    }

    /**
     * Runs a {@link Rule} and optionally binds the result or handles step exceptions.
     *
     * @param rule the rule to run; must not be null.
     * @return a {@link CommandResultBuilder} for optional {@code as()} / {@code onException()} chaining.
     */
    public CommandResultBuilder run(Rule rule) {
        Assert.notNull(rule, "rule cannot be null.");
        RunCommand cmd = RunCommand.of(rule, null, null, null, null);
        commands.add(cmd);
        return new CommandResultBuilder(this, cmd);
    }

    /**
     * Runs a {@link RuleSet} and optionally binds the result or handles step exceptions.
     *
     * @param ruleSet the rule set to run; must not be null.
     * @return a {@link CommandResultBuilder} for optional chaining.
     */
    public CommandResultBuilder run(RuleSet<?> ruleSet) {
        Assert.notNull(ruleSet, "ruleSet cannot be null.");
        RunCommand cmd = RunCommand.of(ruleSet, null, null, null, null);
        commands.add(cmd);
        return new CommandResultBuilder(this, cmd);
    }

    /**
     * Runs a nested {@link RuleFlow} and optionally binds the result or handles step exceptions.
     *
     * @param ruleFlow the nested flow to run; must not be null.
     * @return a {@link CommandResultBuilder} for optional chaining.
     */
    public CommandResultBuilder run(RuleFlow<?> ruleFlow) {
        Assert.notNull(ruleFlow, "ruleFlow cannot be null.");
        RunCommand cmd = RunCommand.of(ruleFlow, null, null, null, null);
        commands.add(cmd);
        return new CommandResultBuilder(this, cmd);
    }

    /**
     * Runs a runnable looked up by name from the {@link org.rulii.registry.RuleRegistry}.
     *
     * @param registryName name to look up at runtime; must not be null or empty.
     * @return a {@link CommandResultBuilder} for optional chaining.
     */
    public CommandResultBuilder run(String registryName) {
        Assert.hasText(registryName, "registryName cannot be empty/null.");
        RunCommand cmd = RunCommand.ofName(registryName, null, null, null, null);
        commands.add(cmd);
        return new CommandResultBuilder(this, cmd);
    }

    /**
     * Runs a rule looked up by class from the {@link org.rulii.registry.RuleRegistry}.
     *
     * @param registryClass class to look up at runtime; must not be null.
     * @return a {@link CommandResultBuilder} for optional chaining.
     */
    public CommandResultBuilder run(Class<?> registryClass) {
        Assert.notNull(registryClass, "registryClass cannot be null.");
        RunCommand cmd = RunCommand.ofClass(registryClass, null, null, null, null);
        commands.add(cmd);
        return new CommandResultBuilder(this, cmd);
    }

    /**
     * Applies a {@link Function} and optionally binds the result or handles step exceptions.
     *
     * @param fn the function to apply; must not be null.
     * @return a {@link CommandResultBuilder} for optional chaining.
     */
    public CommandResultBuilder apply(Function<?> fn) {
        Assert.notNull(fn, "fn cannot be null.");
        ApplyCommand cmd = new ApplyCommand(fn, null, null, null);
        commands.add(cmd);
        return new CommandResultBuilder(this, cmd);
    }

    /**
     * Executes an {@link Action} (void side-effect) with optional step-level exception handling.
     *
     * <p>Use this instead of {@link #apply(Function)} when there is no result to bind.
     *
     * @param action the action to execute; must not be null.
     * @return an {@link ExecuteCommandBuilder} for optional {@code onException()} chaining.
     */
    public ExecuteCommandBuilder execute(Action action) {
        Assert.notNull(action, "action cannot be null.");
        ExecuteCommand cmd = new ExecuteCommand(action, null);
        commands.add(cmd);
        return new ExecuteCommandBuilder(this, cmd);
    }

    /**
     * Evaluates a condition and executes a branch. Returns a {@link WhenBuilder} to define the then-branch.
     *
     * @param condition the condition to evaluate; must not be null.
     * @return a {@link WhenBuilder} for defining then / otherwise branches.
     */
    public WhenBuilder when(Condition condition) {
        Assert.notNull(condition, "condition cannot be null.");
        return new WhenBuilder(this, condition);
    }

    /**
     * Iterates a collection and executes body commands for each element.
     *
     * @param listSource function resolving the collection; must not be null.
     * @param elementBindingName binding name for each element; must not be null or empty.
     * @param body consumer defining body commands; must not be null.
     * @return this builder.
     */
    public RuleFlowBuilder forEach(Function<?> listSource, String elementBindingName, Consumer<RuleFlowBuilder> body) {
        return forEach(listSource, elementBindingName, null, body);
    }

    /**
     * Iterates a collection with an optional stop condition.
     *
     * @param listSource function resolving the collection; must not be null.
     * @param elementBindingName binding name for each element; must not be null or empty.
     * @param stopCondition evaluated after each element; iteration stops when true. May be null.
     * @param body consumer defining body commands; must not be null.
     * @return this builder.
     */
    public RuleFlowBuilder forEach(Function<?> listSource, String elementBindingName,
                                   Condition stopCondition, Consumer<RuleFlowBuilder> body) {
        Assert.notNull(listSource, "listSource cannot be null.");
        Assert.hasText(elementBindingName, "elementBindingName cannot be empty/null.");
        Assert.notNull(body, "body cannot be null.");
        RuleFlowBuilder bodyBuilder = new RuleFlowBuilder();
        body.accept(bodyBuilder);
        commands.add(new ForEachCommand(listSource, bodyBuilder.commands, elementBindingName, stopCondition));
        return this;
    }

    /**
     * Terminates the flow immediately and returns the result of the given extractor.
     * Any commands after this at the top level are unreachable (detected at build time).
     *
     * @param extractor function that extracts the result; must not be null.
     * @return this builder.
     */
    public RuleFlowBuilder exit(Function<?> extractor) {
        Assert.notNull(extractor, "extractor cannot be null.");
        commands.add(new ReturningCommand(extractor));
        return this;
    }

    /**
     * Terminates the flow immediately, returning the current
     * {@link org.rulii.context.RuleContext} as the result.
     *
     * @return this builder.
     */
    public RuleFlowBuilder exit() {
        commands.add(new ReturningCommand(null));
        return this;
    }

    void addCommand(RuleFlowCommand command) {
        commands.add(command);
    }

    void replaceCommand(RuleFlowCommand old, RuleFlowCommand replacement) {
        int idx = commands.lastIndexOf(old);
        if (idx >= 0) commands.set(idx, replacement);
        else commands.add(replacement);
    }

    /**
     * Validates the pipeline and constructs an immutable {@link RuleFlow}.
     *
     * @param <T> the result type.
     * @return a new {@link RuleFlow}; never null.
     * @throws UnrulyException if the pipeline structure is invalid.
     */
    @SuppressWarnings("unchecked")
    public <T> RuleFlow<T> build() {
        Assert.hasText(name, "name must be set before calling build().");
        validate(commands);

        List<InputParameter<?>> params = new ArrayList<>(inputParameters);
        Function<T> extractor = (Function<T>) resultExtractor;

        RuleFlowDefinition def = new RuleFlowDefinition(name, description, SourceDefinition.build(),
                extractor != null ? Object.class : null, commands.size(), params);

        return new RulingOrder<>(def, new ArrayList<>(commands), params, finalizer, extractor, globalHandler, contextConfigurator);
    }

    private void validate(List<RuleFlowCommand> cmds) {
        validateScopes(cmds);
        validateContextPosition(cmds);
        validateReachability(cmds);
    }

    private void validateScopes(List<RuleFlowCommand> cmds) {
        int depth = 0;

        for (RuleFlowCommand cmd : cmds) {
            if (cmd instanceof ScopeCommand) depth++;
            else if (cmd instanceof EndScopeCommand) {
                depth--;
                if (depth < 0) throw new UnrulyException("RuleFlow [" + name + "] structural error: orphaned endScope() — more endScope() calls than scope() calls.");
            } else if (cmd instanceof CompositeCommand composite) {
                for (List<RuleFlowCommand> block : composite.getBlocks()) validateScopes(block);
            }
        }

        if (depth != 0) throw new UnrulyException("RuleFlow [" + name + "] structural error: unclosed scope() — " + depth + " scope(s) missing a matching endScope().");
    }

    private void validateContextPosition(List<RuleFlowCommand> cmds) {
        for (int i = 0; i < cmds.size(); i++) {
            if (cmds.get(i) instanceof ContextCommand && i != 0) throw new UnrulyException("RuleFlow [" + name + "] structural error: context() must be the first command.");
        }
    }

    private void validateReachability(List<RuleFlowCommand> cmds) {
        for (int i = 0; i < cmds.size(); i++) {
            if (cmds.get(i) instanceof ReturningCommand && i < cmds.size() - 1) throw new UnrulyException("RuleFlow [" + name + "] structural error: unreachable commands after exit() at top level.");
        }
    }

    /**
     * Continuation builder returned by {@link RuleFlowBuilder#when(Condition)}.
     * Defines the then-branch (and optionally the otherwise-branch) of a {@code when} step.
     */
    public static final class WhenBuilder {

        private final RuleFlowBuilder outer;
        private final Condition condition;

        WhenBuilder(RuleFlowBuilder outer, Condition condition) {
            super();
            this.outer = outer;
            this.condition = condition;
        }

        /** Shorthand: single-rule then-branch. */
        public WhenContinuationBuilder run(Rule rule) {
            Assert.notNull(rule, "rule cannot be null.");
            List<RuleFlowCommand> then = List.of(RunCommand.of(rule, null, null, null, null));
            return new WhenContinuationBuilder(outer, condition, then);
        }

        /** Shorthand: single-rule-set then-branch. */
        public WhenContinuationBuilder run(RuleSet<?> ruleSet) {
            Assert.notNull(ruleSet, "ruleSet cannot be null.");
            List<RuleFlowCommand> then = List.of(RunCommand.of(ruleSet, null, null, null, null));
            return new WhenContinuationBuilder(outer, condition, then);
        }

        /** Shorthand: single-flow then-branch. */
        public WhenContinuationBuilder run(RuleFlow<?> ruleFlow) {
            Assert.notNull(ruleFlow, "ruleFlow cannot be null.");
            List<RuleFlowCommand> then = List.of(RunCommand.of(ruleFlow, null, null, null, null));
            return new WhenContinuationBuilder(outer, condition, then);
        }

        /** Shorthand: registry-name then-branch. */
        public WhenContinuationBuilder run(String registryName) {
            Assert.hasText(registryName, "registryName cannot be empty/null.");
            List<RuleFlowCommand> then = List.of(RunCommand.ofName(registryName, null, null, null, null));
            return new WhenContinuationBuilder(outer, condition, then);
        }

        /** Shorthand: single-function then-branch. */
        public WhenContinuationBuilder apply(Function<?> fn) {
            Assert.notNull(fn, "fn cannot be null.");
            List<RuleFlowCommand> then = List.of(new ApplyCommand(fn, null, null, null));
            return new WhenContinuationBuilder(outer, condition, then);
        }

        /** Shorthand: registry-class then-branch. */
        public WhenContinuationBuilder run(Class<?> registryClass) {
            Assert.notNull(registryClass, "registryClass cannot be null.");
            List<RuleFlowCommand> then = List.of(RunCommand.ofClass(registryClass, null, null, null, null));
            return new WhenContinuationBuilder(outer, condition, then);
        }

        /** Shorthand: single-action then-branch. */
        public WhenContinuationBuilder execute(Action action) {
            Assert.notNull(action, "action cannot be null.");
            List<RuleFlowCommand> then = List.of(new ExecuteCommand(action, null));
            return new WhenContinuationBuilder(outer, condition, then);
        }

        /** Shorthand: single-binding then-branch. */
        public WhenContinuationBuilder bind(BindingDeclaration<?>... declarations) {
            Assert.notNull(declarations, "declarations cannot be null.");
            List<RuleFlowCommand> then = List.of(new BindCommand(bindings -> bindings.bind(declarations)));
            return new WhenContinuationBuilder(outer, condition, then);
        }

        /** Shorthand: name/value binding then-branch. */
        public WhenContinuationBuilder bind(String name, Object value) {
            Assert.hasText(name, "name cannot be empty/null.");
            List<RuleFlowCommand> then = List.of(new BindCommand(bindings -> bindings.bind(name, value)));
            return new WhenContinuationBuilder(outer, condition, then);
        }

        /** Shorthand: scope-targeted binding then-branch. */
        public WhenContinuationBuilder bindTo(String scopeName, BindingDeclaration<?>... declarations) {
            Assert.hasText(scopeName, "scopeName cannot be empty/null.");
            Assert.notNull(declarations, "declarations cannot be null.");
            List<RuleFlowCommand> then = List.of(new BindCommand(bindings -> bindings.getScopeBindings(scopeName).bind(declarations)));
            return new WhenContinuationBuilder(outer, condition, then);
        }

        /** Shorthand: scope-targeted name/value binding then-branch. */
        public WhenContinuationBuilder bindTo(String scopeName, String name, Object value) {
            Assert.hasText(scopeName, "scopeName cannot be empty/null.");
            Assert.hasText(name, "name cannot be empty/null.");
            List<RuleFlowCommand> then = List.of(new BindCommand(bindings -> bindings.getScopeBindings(scopeName).bind(name, value)));
            return new WhenContinuationBuilder(outer, condition, then);
        }

        /** Shorthand: forEach then-branch. */
        public WhenContinuationBuilder forEach(Function<?> listSource, String elementBindingName, Consumer<RuleFlowBuilder> body) {
            return forEach(listSource, elementBindingName, null, body);
        }

        /** Shorthand: forEach then-branch with stop condition. */
        public WhenContinuationBuilder forEach(Function<?> listSource, String elementBindingName, Condition stopCondition, Consumer<RuleFlowBuilder> body) {
            Assert.notNull(listSource, "listSource cannot be null.");
            Assert.hasText(elementBindingName, "elementBindingName cannot be empty/null.");
            Assert.notNull(body, "body cannot be null.");
            RuleFlowBuilder b = new RuleFlowBuilder();
            body.accept(b);
            List<RuleFlowCommand> then = List.of(new ForEachCommand(listSource, b.commands, elementBindingName, stopCondition));
            return new WhenContinuationBuilder(outer, condition, then);
        }

        /** Shorthand: conditional early exit with a result extractor. */
        public WhenContinuationBuilder exit(Function<?> extractor) {
            Assert.notNull(extractor, "extractor cannot be null.");
            List<RuleFlowCommand> then = List.of(new ReturningCommand(extractor));
            return new WhenContinuationBuilder(outer, condition, then);
        }

        /** Shorthand: conditional early exit returning the current RuleContext. */
        public WhenContinuationBuilder exit() {
            List<RuleFlowCommand> then = List.of(new ReturningCommand(null));
            return new WhenContinuationBuilder(outer, condition, then);
        }

        /** Multi-command then-branch via a nested builder. */
        public WhenContinuationBuilder then(Consumer<RuleFlowBuilder> branch) {
            Assert.notNull(branch, "branch cannot be null.");
            RuleFlowBuilder b = new RuleFlowBuilder();
            branch.accept(b);
            return new WhenContinuationBuilder(outer, condition, b.commands);
        }
    }

    /**
     * Builder returned after the then-branch of a {@code when} step.
     * Allows an optional {@code otherwise} branch, then seals the {@link WhenCommand}.
     */
    public static final class WhenContinuationBuilder {

        private final RuleFlowBuilder outer;
        private final Condition condition;
        private final List<RuleFlowCommand> thenCommands;

        WhenContinuationBuilder(RuleFlowBuilder outer, Condition condition, List<RuleFlowCommand> thenCommands) {
            super();
            this.outer = outer;
            this.condition = condition;
            this.thenCommands = thenCommands;
        }

        /**
         * Seals the WhenCommand with an otherwise-branch defined by the given builder consumer.
         *
         * @param branch consumer that populates the otherwise commands; must not be null.
         * @return the outer builder.
         */
        public RuleFlowBuilder otherwise(Consumer<RuleFlowBuilder> branch) {
            Assert.notNull(branch, "branch cannot be null.");
            RuleFlowBuilder b = new RuleFlowBuilder();
            branch.accept(b);
            return seal(b.commands);
        }

        public RuleFlowBuilder bind(BindingDeclaration<?>... declarations) { return sealEmpty().bind(declarations); }

        public RuleFlowBuilder bind(String name, Object value) { return sealEmpty().bind(name, value); }

        public RuleFlowBuilder bindTo(String scopeName, BindingDeclaration<?>... declarations) { return sealEmpty().bindTo(scopeName, declarations); }

        public RuleFlowBuilder bindTo(String scopeName, String name, Object value) { return sealEmpty().bindTo(scopeName, name, value); }

        public WhenBuilder when(Condition c) { return sealEmpty().when(c); }

        public CommandResultBuilder run(Rule rule) { return sealEmpty().run(rule); }

        public CommandResultBuilder run(RuleSet<?> rs) { return sealEmpty().run(rs); }

        public CommandResultBuilder run(RuleFlow<?> rf) { return sealEmpty().run(rf); }

        public CommandResultBuilder run(String name) { return sealEmpty().run(name); }

        public CommandResultBuilder run(Class<?> cls) { return sealEmpty().run(cls); }

        public CommandResultBuilder apply(Function<?> fn) { return sealEmpty().apply(fn); }

        public ExecuteCommandBuilder execute(Action action) { return sealEmpty().execute(action); }

        public RuleFlowBuilder forEach(Function<?> src, String elem, Consumer<RuleFlowBuilder> body) { return sealEmpty().forEach(src, elem, body); }

        public RuleFlowBuilder scope(String n) { return sealEmpty().scope(n); }

        public RuleFlowBuilder scope() { return sealEmpty().scope(); }

        public RuleFlowBuilder endScope() { return sealEmpty().endScope(); }

        public RuleFlowBuilder exit(Function<?> extractor) { return sealEmpty().exit(extractor); }

        public RuleFlowBuilder exit() { return sealEmpty().exit(); }

        public <T> RuleFlowBuilder returning(Function<T> extractor) { return sealEmpty().returning(extractor); }

        public RuleFlowBuilder returning() { return sealEmpty().returning(); }

        public RuleFlowBuilder finalizer(Action a) { return sealEmpty().finalizer(a); }

        public <E extends Exception> RuleFlowBuilder onException(Class<E> t, Consumer<RuleFlowBuilder> h) { return sealEmpty().onException(t, h); }

        public <T> RuleFlow<T> build() { return sealEmpty().build(); }

        private RuleFlowBuilder sealEmpty() {
            return seal(List.of());
        }

        private RuleFlowBuilder seal(List<RuleFlowCommand> otherwiseCommands) {
            outer.addCommand(new WhenCommand(condition, thenCommands, otherwiseCommands));
            return outer;
        }
    }

    /**
     * Builder returned by {@code run()} and {@code apply()} on the outer {@link RuleFlowBuilder}.
     * Allows optional result-binding ({@code as()}) and step-level exception handling
     * ({@code onException()}), then seals the command and delegates back to the outer builder.
     */
    public static final class CommandResultBuilder {

        private final RuleFlowBuilder outer;
        private final RuleFlowCommand baseCommand; // RunCommand or ApplyCommand without binding/handler
        private String bindingName;
        private String bindingScopeName;
        private OnExceptionCommand stepHandler;
        private Object stepParams;

        CommandResultBuilder(RuleFlowBuilder outer, RuleFlowCommand baseCommand) {
            super();
            this.outer = outer;
            this.baseCommand = baseCommand;
        }

        /**
         * Binds the command's result into the current scope under {@code bindingName}.
         *
         * @param bindingName the name to bind under; must not be null or empty.
         * @return this builder for optional further chaining.
         */
        public CommandResultBuilder as(String bindingName) {
            Assert.hasText(bindingName, "bindingName cannot be empty/null.");
            this.bindingName = bindingName;
            return this;
        }

        /**
         * Binds the command's result into a named scope.
         *
         * @param scopeName the scope to bind into; must not be null or empty.
         * @param bindingName the name to bind under; must not be null or empty.
         * @return this builder for optional further chaining.
         */
        public CommandResultBuilder as(String scopeName, String bindingName) {
            Assert.hasText(scopeName, "scopeName cannot be empty/null.");
            Assert.hasText(bindingName, "bindingName cannot be empty/null.");
            this.bindingScopeName = scopeName;
            this.bindingName = bindingName;
            return this;
        }

        /**
         * Adds a step-level exception handler. Handler commands run when this step throws
         * an exception of the given type.
         *
         * @param type the exception type to catch; must not be null.
         * @param handler builder consumer defining handler commands; must not be null.
         * @return this builder for optional {@code as()} chaining.
         */
        public <E extends Exception> CommandResultBuilder onException(Class<E> type, Consumer<RuleFlowBuilder> handler) {
            Assert.notNull(type, "type cannot be null.");
            Assert.notNull(handler, "handler cannot be null.");
            RuleFlowBuilder hb = new RuleFlowBuilder();
            handler.accept(hb);
            this.stepHandler = new OnExceptionCommand(type, hb.commands);
            return this;
        }

        /**
         * Supplies step-scoped parameters via {@link BindingDeclaration} lambdas.
         *
         * <p>The declarations are bound into a temporary anonymous scope that exists only for
         * the duration of this step and is removed in a {@code finally} block after the runnable
         * completes. They never outlive the step or become visible to subsequent pipeline commands.
         *
         * @param params one or more binding declarations; must not be null.
         * @return this builder for optional further chaining.
         */
        public CommandResultBuilder with(BindingDeclaration<?>...params) {
            Assert.notNull(params, "params cannot be null.");
            this.stepParams = params;
            return this;
        }

        /**
         * Supplies step-scoped parameters from a JavaBean POJO or a {@code Map<String, Object>}.
         *
         * <p>JavaBean properties (or map entries) are bound into a temporary anonymous scope for
         * the duration of this step only.
         *
         * @param params non-null POJO or {@code Map<String, Object>}.
         * @return this builder for optional further chaining.
         */
        public CommandResultBuilder with(Object params) {
            Assert.notNull(params, "params cannot be null.");
            this.stepParams = params;
            return this;
        }

        public RuleFlowBuilder bind(BindingDeclaration<?>... declarations) { return seal().bind(declarations); }

        public RuleFlowBuilder bind(String name, Object value) { return seal().bind(name, value); }

        public RuleFlowBuilder bindTo(String scopeName, BindingDeclaration<?>... declarations) { return seal().bindTo(scopeName, declarations); }

        public RuleFlowBuilder bindTo(String scopeName, String name, Object value) { return seal().bindTo(scopeName, name, value); }

        public WhenBuilder when(Condition c) { return seal().when(c); }

        public CommandResultBuilder run(Rule rule) { return seal().run(rule); }

        public CommandResultBuilder run(RuleSet<?> rs) { return seal().run(rs); }

        public CommandResultBuilder run(RuleFlow<?> rf) { return seal().run(rf); }

        public CommandResultBuilder run(String name) { return seal().run(name); }

        public CommandResultBuilder run(Class<?> cls) { return seal().run(cls); }

        public CommandResultBuilder apply(Function<?> fn) { return seal().apply(fn); }

        public ExecuteCommandBuilder execute(Action action) { return seal().execute(action); }

        public RuleFlowBuilder forEach(Function<?> src, String elem, Consumer<RuleFlowBuilder> body) { return seal().forEach(src, elem, body); }

        public RuleFlowBuilder scope(String n) { return seal().scope(n); }

        public RuleFlowBuilder scope() { return seal().scope(); }

        public RuleFlowBuilder endScope() { return seal().endScope(); }

        public RuleFlowBuilder exit(Function<?> extractor) { return seal().exit(extractor); }

        public RuleFlowBuilder exit() { return seal().exit(); }

        public <T> RuleFlowBuilder returning(Function<T> extractor) { return seal().returning(extractor); }

        public RuleFlowBuilder returning() { return seal().returning(); }

        public RuleFlowBuilder finalizer(Action a) { return seal().finalizer(a); }

        public <T> RuleFlow<T> build() { return seal().build(); }

        private RuleFlowBuilder seal() {
            RuleFlowCommand sealed = rebuildWithOptions();
            outer.replaceCommand(baseCommand, sealed);
            return outer;
        }

        private RuleFlowCommand rebuildWithOptions() {
            if (baseCommand instanceof RunCommand) {
                // Rebuild RunCommand with binding + handler options
                return rebuildRunCommand();
            } else if (baseCommand instanceof ApplyCommand apply) {
                return new ApplyCommand(getApplyFunction(apply), bindingName, bindingScopeName, stepHandler);
            }
            return baseCommand;
        }

        private RunCommand rebuildRunCommand() {
            RunCommand src = (RunCommand) baseCommand;
            if (src.getRunnable() != null)
                return RunCommand.of(src.getRunnable(), bindingName, bindingScopeName, stepHandler, stepParams);
            if (src.getRegistryName() != null)
                return RunCommand.ofName(src.getRegistryName(), bindingName, bindingScopeName, stepHandler, stepParams);
            return RunCommand.ofClass(src.getRegistryClass(), bindingName, bindingScopeName, stepHandler, stepParams);
        }

        @SuppressWarnings("unchecked")
        private static Function<?> getApplyFunction(ApplyCommand cmd) {
            return cmd.getFunction();
        }
    }

    /**
     * Builder returned by {@link RuleFlowBuilder#execute(Action)}.
     *
     * <p>Allows optional step-level exception handling via {@code onException()}, then seals
     * the command and delegates back to the outer builder. Unlike {@link CommandResultBuilder},
     * this builder has no {@code as()} methods because the action produces no result.
     */
    public static final class ExecuteCommandBuilder {

        private final RuleFlowBuilder outer;
        private final ExecuteCommand baseCommand;
        private OnExceptionCommand stepHandler;

        ExecuteCommandBuilder(RuleFlowBuilder outer, ExecuteCommand baseCommand) {
            super();
            this.outer = outer;
            this.baseCommand = baseCommand;
        }

        /**
         * Adds a step-level exception handler. Handler commands run when this action throws
         * an exception of the given type.
         *
         * @param type the exception type to catch; must not be null.
         * @param handler builder consumer defining handler commands; must not be null.
         * @return this builder for optional further chaining.
         */
        public <E extends Exception> ExecuteCommandBuilder onException(Class<E> type, Consumer<RuleFlowBuilder> handler) {
            Assert.notNull(type, "type cannot be null.");
            Assert.notNull(handler, "handler cannot be null.");
            RuleFlowBuilder hb = new RuleFlowBuilder();
            handler.accept(hb);
            this.stepHandler = new OnExceptionCommand(type, hb.commands);
            return this;
        }

        public RuleFlowBuilder bind(BindingDeclaration<?>... declarations) { return seal().bind(declarations); }

        public RuleFlowBuilder bind(String name, Object value) { return seal().bind(name, value); }

        public RuleFlowBuilder bindTo(String scopeName, BindingDeclaration<?>... declarations) { return seal().bindTo(scopeName, declarations); }

        public RuleFlowBuilder bindTo(String scopeName, String name, Object value) { return seal().bindTo(scopeName, name, value); }

        public WhenBuilder when(Condition c) { return seal().when(c); }

        public CommandResultBuilder run(Rule rule) { return seal().run(rule); }

        public CommandResultBuilder run(RuleSet<?> rs) { return seal().run(rs); }

        public CommandResultBuilder run(RuleFlow<?> rf) { return seal().run(rf); }

        public CommandResultBuilder run(String name) { return seal().run(name); }

        public CommandResultBuilder run(Class<?> cls) { return seal().run(cls); }

        public CommandResultBuilder apply(Function<?> fn) { return seal().apply(fn); }

        public ExecuteCommandBuilder execute(Action action) { return seal().execute(action); }

        public RuleFlowBuilder forEach(Function<?> src, String elem, Consumer<RuleFlowBuilder> body) { return seal().forEach(src, elem, body); }

        public RuleFlowBuilder scope(String n) { return seal().scope(n); }

        public RuleFlowBuilder scope() { return seal().scope(); }

        public RuleFlowBuilder endScope() { return seal().endScope(); }

        public RuleFlowBuilder exit(Function<?> extractor) { return seal().exit(extractor); }

        public RuleFlowBuilder exit() { return seal().exit(); }

        public <T> RuleFlowBuilder returning(Function<T> extractor) { return seal().returning(extractor); }

        public RuleFlowBuilder returning() { return seal().returning(); }

        public RuleFlowBuilder finalizer(Action a) { return seal().finalizer(a); }

        public <T> RuleFlow<T> build() { return seal().build(); }

        private RuleFlowBuilder seal() {
            ExecuteCommand sealed = new ExecuteCommand(baseCommand.getAction(), stepHandler);
            outer.replaceCommand(baseCommand, sealed);
            return outer;
        }
    }
}
