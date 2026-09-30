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
package org.rulii.ruleflow.info;

import org.rulii.bind.Binding;
import org.rulii.bind.BindingDeclaration;
import org.rulii.bind.Bindings;
import org.rulii.bind.load.BindingLoader;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.ExpressionInfo;
import org.rulii.model.Runnable;
import org.rulii.ruleflow.AsyncContextMode;
import org.rulii.ruleflow.command.RuleFlowCommand;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Describes what a {@link RuleFlowCommand} does, without running it. One record per kind of
 * command; container commands carry the info of their body.
 *
 * <p>These are values. They never hold bound values (which may be secrets or live services):
 * a {@link Bind} records the names and types that will be bound, not the values.
 *
 * <p>Custom commands report {@link Custom} by default (with their body when they are a
 * container). A custom command can override {@link RuleFlowCommand#getInfo()} to report real
 * structure instead.
 *
 * @author Max Arulananthan
 * @since 2.1
 */
public sealed interface CommandInfo permits CommandInfo.Run, CommandInfo.Apply, CommandInfo.Execute,
        CommandInfo.AsyncRun, CommandInfo.Await, CommandInfo.Bind, CommandInfo.When, CommandInfo.ForEach,
        CommandInfo.Scope, CommandInfo.Exit, CommandInfo.Custom {

    /**
     * Describes each command in order.
     *
     * @param commands commands; may be null or empty.
     * @return unmodifiable list of infos; never null.
     */
    static List<CommandInfo> of(List<? extends RuleFlowCommand> commands) {
        if (commands == null || commands.isEmpty()) return List.of();
        List<CommandInfo> result = new ArrayList<>(commands.size());
        for (RuleFlowCommand command : commands) result.add(command.getInfo());
        return Collections.unmodifiableList(result);
    }

    private static <T> List<T> copy(List<T> list) {
        return list == null || list.isEmpty() ? List.of() : Collections.unmodifiableList(new ArrayList<>(list));
    }

    // -----------------------------------------------------------------------------------------
    // Parts
    // -----------------------------------------------------------------------------------------

    /**
     * What a run step targets: a runnable instance, or a late-bound registry lookup.
     */
    sealed interface Target permits Target.Instance, Target.ByName, Target.ByClass {

        /** A runnable resolved at build time (a Java instance, or an XML bean reference). */
        record Instance(Runnable<?> runnable) implements Target {
            public Instance {
                Assert.notNull(runnable, "runnable cannot be null.");
            }
        }

        /** Looked up in the {@link org.rulii.registry.RuleRegistry} by name when the step runs. */
        record ByName(String name) implements Target {
            public ByName {
                Assert.hasText(name, "name cannot be empty.");
            }
        }

        /** Looked up in the {@link org.rulii.registry.RuleRegistry} by class when the step runs. */
        record ByClass(Class<?> type) implements Target {
            public ByClass {
                Assert.notNull(type, "type cannot be null.");
            }
        }

        /**
         * Picks the target from a run command's fields: the instance if present, else the name,
         * else the class.
         */
        static Target of(Runnable<?> runnable, String name, Class<?> type) {
            if (runnable != null) return new Instance(runnable);
            if (name != null) return new ByName(name);
            return new ByClass(type);
        }
    }

    /**
     * An exception handler: the exception type it catches and the commands it runs.
     */
    record Handler(Class<? extends Exception> exceptionType, List<CommandInfo> body) {
        public Handler {
            Assert.notNull(exceptionType, "exceptionType cannot be null.");
            body = copy(body);
        }
    }

    /**
     * The continuation of an async run: the name the result is bound under and the commands run.
     */
    record Continuation(String as, List<CommandInfo> body) {
        public Continuation {
            Assert.hasText(as, "as cannot be empty.");
            body = copy(body);
        }
    }

    /** Which futures an await waits for. */
    enum AwaitKind { ONE, ALL, ANY }

    /** How a bind step gets the values it binds. */
    enum BindKind {
        /** One name bound to a value given at build time. */
        LITERAL,
        /** {@link BindingDeclaration} lambdas, evaluated when the step runs. */
        DECLARATIONS,
        /** Copies every binding of a {@link Bindings} instance. */
        BINDINGS,
        /** Loads the entries of a {@link Map}. */
        MAP,
        /** Loads the properties of a bean. */
        BEAN,
        /** Runs a {@link BindingLoader} over a value. */
        LOADER,
        /** One name bound to the result of an expression evaluated when the step runs. */
        EXPRESSION
    }

    /**
     * A name that a bind step binds. The value itself is never recorded.
     *
     * @param name       binding name.
     * @param type       value type when known; null otherwise.
     * @param expression expression that produces the value ({@link BindKind#EXPRESSION} only).
     */
    record BoundName(String name, Type type, ExpressionInfo expression) {
        public BoundName {
            Assert.hasText(name, "name cannot be empty.");
        }
    }

    // -----------------------------------------------------------------------------------------
    // Commands
    // -----------------------------------------------------------------------------------------

    /**
     * Runs a rule, rule set or rule flow.
     *
     * @param target  what runs.
     * @param as      binding name for the result; null when not bound.
     * @param scope   scope the result is bound in; null for the current scope.
     * @param params  parameters bound in a fresh scope for the run; null when none.
     * @param handler step-level exception handler; null when none.
     */
    record Run(Target target, String as, String scope, Bind params, Handler handler) implements CommandInfo {
        public Run {
            Assert.notNull(target, "target cannot be null.");
        }
    }

    /**
     * Applies a function.
     *
     * @param fn      the function.
     * @param as      binding name for the result; null when not bound.
     * @param scope   scope the result is bound in; null for the current scope.
     * @param params  parameters bound in a fresh scope for the call; null when none.
     * @param handler step-level exception handler; null when none.
     */
    record Apply(ExpressionInfo fn, String as, String scope, Bind params, Handler handler) implements CommandInfo {
        public Apply {
            Assert.notNull(fn, "fn cannot be null.");
        }
    }

    /**
     * Executes an action.
     *
     * @param action  the action.
     * @param handler step-level exception handler; null when none.
     */
    record Execute(ExpressionInfo action, Handler handler) implements CommandInfo {
        public Execute {
            Assert.notNull(action, "action cannot be null.");
        }
    }

    /**
     * Launches a rule, rule set or rule flow asynchronously.
     *
     * @param target  what runs.
     * @param as      binding name for the future; null when not bound.
     * @param mode    which rule context the async run sees.
     * @param then    continuation run with the result; null when none.
     * @param handler step-level exception handler; null when none.
     */
    record AsyncRun(Target target, String as, AsyncContextMode mode, Continuation then, Handler handler) implements CommandInfo {
        public AsyncRun {
            Assert.notNull(target, "target cannot be null.");
            Assert.notNull(mode, "mode cannot be null.");
        }
    }

    /**
     * Waits for one, all or any of the named futures.
     *
     * @param kind    one, all or any.
     * @param names   binding names of the futures.
     * @param timeout how long to wait.
     */
    record Await(AwaitKind kind, List<String> names, Duration timeout) implements CommandInfo {
        public Await {
            Assert.notNull(kind, "kind cannot be null.");
            Assert.notEmpty(names, "names cannot be empty.");
            Assert.notNull(timeout, "timeout cannot be null.");
            names = copy(names);
        }
    }

    /**
     * Binds values into a scope. Records names and types only, never values.
     *
     * @param scope target scope; null for the current scope.
     * @param kind  how the values are obtained.
     * @param names names bound, where known; empty for {@link BindKind#BEAN} and {@link BindKind#LOADER}.
     * @param label class name of the bean or loader for {@link BindKind#BEAN} and {@link BindKind#LOADER}; null otherwise.
     */
    record Bind(String scope, BindKind kind, List<BoundName> names, String label) implements CommandInfo {
        public Bind {
            Assert.notNull(kind, "kind cannot be null.");
            names = copy(names);
        }

        /** {@code bind(name, value)}: the value's class is recorded, the value is not. */
        public static Bind literal(String scope, String name, Object value) {
            return new Bind(scope, BindKind.LITERAL,
                    List.of(new BoundName(name, value != null ? value.getClass() : null, null)), null);
        }

        /** {@code bind(x -> ...)}: names only; the declarations are evaluated when the step runs. */
        public static Bind declarations(String scope, BindingDeclaration<?>... declarations) {
            List<BoundName> names = new ArrayList<>();
            if (declarations != null) {
                for (BindingDeclaration<?> declaration : declarations) {
                    names.add(new BoundName(declaration.name(), null, null));
                }
            }
            return new Bind(scope, BindKind.DECLARATIONS, names, null);
        }

        /** {@code bind(Bindings)}: the name and type of each binding in the source. */
        public static Bind bindings(String scope, Bindings source) {
            List<BoundName> names = new ArrayList<>();
            if (source != null) {
                for (Binding<?> binding : source) {
                    names.add(new BoundName(binding.getName(), binding.getType(), null));
                }
            }
            return new Bind(scope, BindKind.BINDINGS, names, null);
        }

        /** {@code bind(Object)}: a map's keys and value classes, or a bean's class. */
        public static Bind object(String scope, Object object) {
            Assert.notNull(object, "object cannot be null.");

            if (object instanceof Map<?, ?> map) {
                List<BoundName> names = new ArrayList<>();
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    Object value = entry.getValue();
                    names.add(new BoundName(String.valueOf(entry.getKey()), value != null ? value.getClass() : null, null));
                }
                return new Bind(scope, BindKind.MAP, names, null);
            }

            return new Bind(scope, BindKind.BEAN, List.of(), object.getClass().getName());
        }

        /** {@code bind(loader, value)}: the loader's class; the names it produces aren't known. */
        public static Bind loader(String scope, BindingLoader<?> loader) {
            Assert.notNull(loader, "loader cannot be null.");
            return new Bind(scope, BindKind.LOADER, List.of(), loader.getClass().getName());
        }

        /** One name bound to the result of an expression evaluated when the step runs. */
        public static Bind expression(String scope, String name, Type type, ExpressionInfo expression) {
            Assert.notNull(expression, "expression cannot be null.");
            return new Bind(scope, BindKind.EXPRESSION, List.of(new BoundName(name, type, expression)), null);
        }

        /**
         * The parameters of a run step, as given to {@code with(...)}: a declaration, an array
         * of declarations, a map or a bean.
         *
         * @param params the parameters object; may be null.
         * @return bind info, or null when there are no parameters.
         */
        public static Bind params(Object params) {
            if (params == null) return null;
            if (params instanceof BindingDeclaration<?> declaration) return declarations(null, declaration);
            if (params instanceof BindingDeclaration<?>[] declarations) return declarations(null, declarations);
            return object(null, params);
        }
    }

    /**
     * Runs one of two branches depending on a condition.
     *
     * @param condition the condition.
     * @param then      commands run when true.
     * @param otherwise commands run when false; empty when there is no otherwise branch.
     */
    record When(ExpressionInfo condition, List<CommandInfo> then, List<CommandInfo> otherwise) implements CommandInfo {
        public When {
            Assert.notNull(condition, "condition cannot be null.");
            then = copy(then);
            otherwise = copy(otherwise);
        }
    }

    /**
     * Runs the body once per element of a collection.
     *
     * @param item   binding name of the current element.
     * @param source function that produces the collection.
     * @param stop   condition checked after each iteration; null when none.
     * @param body   commands run per element.
     */
    record ForEach(String item, ExpressionInfo source, ExpressionInfo stop, List<CommandInfo> body) implements CommandInfo {
        public ForEach {
            Assert.hasText(item, "item cannot be empty.");
            Assert.notNull(source, "source cannot be null.");
            body = copy(body);
        }
    }

    /**
     * Runs the body in a new binding scope.
     *
     * @param name scope name; null for an anonymous scope.
     * @param body commands run in the scope.
     */
    record Scope(String name, List<CommandInfo> body) implements CommandInfo {
        public Scope {
            body = copy(body);
        }
    }

    /**
     * Ends the flow with a result.
     *
     * @param result function that produces the result; null when the flow returns its rule context.
     */
    record Exit(ExpressionInfo result) implements CommandInfo {}

    /**
     * A command rulii doesn't know the structure of.
     *
     * @param className the command's class.
     * @param body      the body when the command is a container; empty otherwise.
     */
    record Custom(String className, List<CommandInfo> body) implements CommandInfo {
        public Custom {
            Assert.hasText(className, "className cannot be empty.");
            body = copy(body);
        }
    }
}
