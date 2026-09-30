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
package org.rulii.test.ruleflow;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.bind.Bindings;
import org.rulii.bind.load.BindingLoader;
import org.rulii.context.RuleContext;
import org.rulii.model.ExpressionInfo;
import org.rulii.model.action.Action;
import org.rulii.model.condition.Condition;
import org.rulii.model.function.Function;
import org.rulii.rule.Rule;
import org.rulii.ruleflow.AsyncContextMode;
import org.rulii.ruleflow.RuleFlow;
import org.rulii.ruleflow.RuleFlowBuilderTemplate;
import org.rulii.ruleflow.RuleFlowExecutionContext;
import org.rulii.ruleflow.command.BindCommand;
import org.rulii.ruleflow.command.ContainerCommand;
import org.rulii.ruleflow.command.ForEachCommand;
import org.rulii.ruleflow.command.RuleFlowCommand;
import org.rulii.ruleflow.command.WhenCommand;
import org.rulii.ruleflow.info.CommandInfo;
import org.rulii.ruleflow.info.CommandInfo.AwaitKind;
import org.rulii.ruleflow.info.CommandInfo.BindKind;
import org.rulii.ruleflow.info.CommandInfo.Target;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.rulii.model.action.Actions.action;
import static org.rulii.model.condition.Conditions.condition;
import static org.rulii.model.function.Functions.function;

/**
 * Tests that every rule flow command can describe itself through {@link RuleFlowCommand#getInfo()}.
 *
 * @author Max Arulananthan
 * @since 2.1
 */
public class RuleFlowInfoTest {

    public static class Bean {
        public int getCount() {
            return 1;
        }
    }

    private static Rule rule(String name) {
        return Rule.builder().name(name).given(condition(() -> true)).build();
    }

    @SuppressWarnings("unchecked")
    private static <T extends CommandInfo> T info(RuleFlow<?> flow, int index, Class<T> type) {
        CommandInfo result = flow.getCommands().get(index).getInfo();
        Assertions.assertInstanceOf(type, result, "command " + index);
        return (T) result;
    }

    // -----------------------------------------------------------------------
    // Bind
    // -----------------------------------------------------------------------

    @Test
    public void bindsRecordNamesAndTypesButNeverValues() {
        Bindings source = Bindings.builder().standard();
        source.bind("fromBindings", String.class, "v");
        BindingLoader<Map<String, Object>> loader = (bindings, value) -> bindings.loadMap(value);

        RuleFlow<Integer> flow = RuleFlow.builder()
                .name("bindInfoFlow")
                .bind(x -> 42)
                .bind("secret", "hunter2")
                .bind(source)
                .bind(Map.of("fromMap", 7))
                .bind(new Bean())
                .bind(loader, Map.of("k", "v"))
                .<Integer>returning(function((Integer x, String secret, String fromBindings, Integer fromMap,
                                              Integer count, String k) ->
                        x + fromMap + count + secret.length() + fromBindings.length() + k.length()))
                .build();

        CommandInfo.Bind declarations = info(flow, 0, CommandInfo.Bind.class);
        Assertions.assertEquals(BindKind.DECLARATIONS, declarations.kind());
        Assertions.assertNull(declarations.scope());
        Assertions.assertEquals(1, declarations.names().size());
        Assertions.assertEquals("x", declarations.names().get(0).name());
        Assertions.assertNull(declarations.names().get(0).type(), "declarations aren't evaluated for the info");

        CommandInfo.Bind literal = info(flow, 1, CommandInfo.Bind.class);
        Assertions.assertEquals(BindKind.LITERAL, literal.kind());
        Assertions.assertEquals("secret", literal.names().get(0).name());
        Assertions.assertEquals(String.class, literal.names().get(0).type());
        Assertions.assertFalse(literal.toString().contains("hunter2"), "values are never recorded");

        CommandInfo.Bind bindings = info(flow, 2, CommandInfo.Bind.class);
        Assertions.assertEquals(BindKind.BINDINGS, bindings.kind());
        Assertions.assertEquals("fromBindings", bindings.names().get(0).name());
        Assertions.assertEquals(String.class, bindings.names().get(0).type());

        CommandInfo.Bind map = info(flow, 3, CommandInfo.Bind.class);
        Assertions.assertEquals(BindKind.MAP, map.kind());
        Assertions.assertEquals("fromMap", map.names().get(0).name());
        Assertions.assertEquals(Integer.class, map.names().get(0).type());

        CommandInfo.Bind bean = info(flow, 4, CommandInfo.Bind.class);
        Assertions.assertEquals(BindKind.BEAN, bean.kind());
        Assertions.assertTrue(bean.names().isEmpty());
        Assertions.assertEquals(Bean.class.getName(), bean.label());

        CommandInfo.Bind loaded = info(flow, 5, CommandInfo.Bind.class);
        Assertions.assertEquals(BindKind.LOADER, loaded.kind());
        Assertions.assertNotNull(loaded.label());

        // Execution is unchanged: every bind still happens. 42 + 7 + 1 + 7 + 1 + 1.
        Assertions.assertEquals(59, flow.run());
    }

    @Test
    public void bindToRecordsTheScope() {
        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("bindToInfoFlow")
                .scope("s", b -> b
                        .bindTo("s", x -> 1)
                        .bindTo("s", "y", 2)
                        .bindTo("s", Map.of("z", 3))
                        .bindTo("s", new Bean())
                        .bindTo("s", Bindings.builder().standard())
                        .bindTo("s", (BindingLoader<Map<String, Object>>) (bindings, value) -> bindings.loadMap(value), Map.of()))
                .build();

        CommandInfo.Scope scope = info(flow, 0, CommandInfo.Scope.class);
        Assertions.assertEquals("s", scope.name());
        Assertions.assertEquals(6, scope.body().size());
        for (CommandInfo each : scope.body()) {
            Assertions.assertEquals("s", ((CommandInfo.Bind) each).scope());
        }
        Assertions.assertEquals(BindKind.DECLARATIONS, ((CommandInfo.Bind) scope.body().get(0)).kind());
        Assertions.assertEquals(BindKind.LITERAL, ((CommandInfo.Bind) scope.body().get(1)).kind());
        Assertions.assertEquals(BindKind.MAP, ((CommandInfo.Bind) scope.body().get(2)).kind());
        Assertions.assertEquals(BindKind.BEAN, ((CommandInfo.Bind) scope.body().get(3)).kind());
        Assertions.assertEquals(BindKind.BINDINGS, ((CommandInfo.Bind) scope.body().get(4)).kind());
        Assertions.assertEquals(BindKind.LOADER, ((CommandInfo.Bind) scope.body().get(5)).kind());
    }

    @Test
    public void rawBindCommandIsCustom() {
        BindCommand command = new BindCommand(bindings -> bindings.bind("a", 1));
        CommandInfo.Custom custom = (CommandInfo.Custom) command.getInfo();
        Assertions.assertEquals(BindCommand.class.getName(), custom.className());
        Assertions.assertTrue(custom.body().isEmpty());

        BindCommand described = new BindCommand(bindings -> bindings.bind("a", 1),
                () -> CommandInfo.Bind.expression(null, "a", Integer.class, ExpressionInfo.script("el", "1")));
        CommandInfo.Bind bind = (CommandInfo.Bind) described.getInfo();
        Assertions.assertEquals(BindKind.EXPRESSION, bind.kind());
        Assertions.assertEquals("a", bind.names().get(0).name());
        Assertions.assertEquals("1", bind.names().get(0).expression().sourceText());
    }

    // -----------------------------------------------------------------------
    // Run / Apply / Execute
    // -----------------------------------------------------------------------

    @Test
    public void runStepsReportTargetBindingParamsAndHandler() {
        Rule rule = rule("runInfoRule");
        Function<Integer> fn = function((Integer x) -> x + 1);
        Action act = action(() -> {});

        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("runInfoFlow")
                .run(rule, spec -> spec.as("s", "out").with(x -> 1)
                        .onException(IllegalStateException.class, b -> b.bind("handled", true)))
                .run("byName")
                .run(Bean.class)
                .apply(fn, spec -> spec.as("y").with(Map.of("p", 1)))
                .execute(act, spec -> spec.onException(RuntimeException.class, b -> {}))
                .build();

        CommandInfo.Run run = info(flow, 0, CommandInfo.Run.class);
        Assertions.assertInstanceOf(Target.Instance.class, run.target());
        Assertions.assertSame(rule, ((Target.Instance) run.target()).runnable());
        Assertions.assertEquals("out", run.as());
        Assertions.assertEquals("s", run.scope());
        Assertions.assertEquals(BindKind.DECLARATIONS, run.params().kind());
        Assertions.assertEquals("x", run.params().names().get(0).name());
        Assertions.assertEquals(IllegalStateException.class, run.handler().exceptionType());
        Assertions.assertEquals(1, run.handler().body().size());
        Assertions.assertEquals(BindKind.LITERAL, ((CommandInfo.Bind) run.handler().body().get(0)).kind());

        CommandInfo.Run byName = info(flow, 1, CommandInfo.Run.class);
        Assertions.assertEquals("byName", ((Target.ByName) byName.target()).name());
        Assertions.assertNull(byName.as());
        Assertions.assertNull(byName.params());
        Assertions.assertNull(byName.handler());

        CommandInfo.Run byClass = info(flow, 2, CommandInfo.Run.class);
        Assertions.assertEquals(Bean.class, ((Target.ByClass) byClass.target()).type());

        CommandInfo.Apply apply = info(flow, 3, CommandInfo.Apply.class);
        Assertions.assertEquals(ExpressionInfo.Kind.COMPILED, apply.fn().kind());
        Assertions.assertSame(fn.getDefinition(), apply.fn().method());
        Assertions.assertEquals("y", apply.as());
        Assertions.assertNull(apply.scope());
        Assertions.assertEquals(BindKind.MAP, apply.params().kind());
        Assertions.assertEquals("p", apply.params().names().get(0).name());

        CommandInfo.Execute execute = info(flow, 4, CommandInfo.Execute.class);
        Assertions.assertSame(act.getDefinition(), execute.action().method());
        Assertions.assertEquals(RuntimeException.class, execute.handler().exceptionType());
        Assertions.assertTrue(execute.handler().body().isEmpty());
    }

    // -----------------------------------------------------------------------
    // Async
    // -----------------------------------------------------------------------

    @Test
    public void asyncStepsReportModeContinuationAndAwaits() {
        Rule rule = rule("asyncInfoRule");

        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("asyncInfoFlow")
                .asyncRun(rule, spec -> spec.as("f").withImmutableBindings()
                        .thenRun("res", b -> b.bind("done", true))
                        .onException(RuntimeException.class, b -> {}))
                .asyncRun("byName")
                .await("f", 5, TimeUnit.SECONDS)
                .awaitAll("f", "g")
                .awaitAny(1, TimeUnit.MINUTES, "f")
                .build();

        CommandInfo.AsyncRun async = info(flow, 0, CommandInfo.AsyncRun.class);
        Assertions.assertSame(rule, ((Target.Instance) async.target()).runnable());
        Assertions.assertEquals("f", async.as());
        Assertions.assertEquals(AsyncContextMode.IMMUTABLE, async.mode());
        Assertions.assertEquals("res", async.then().as());
        Assertions.assertEquals(1, async.then().body().size());
        Assertions.assertEquals(RuntimeException.class, async.handler().exceptionType());

        CommandInfo.AsyncRun byName = info(flow, 1, CommandInfo.AsyncRun.class);
        Assertions.assertEquals("byName", ((Target.ByName) byName.target()).name());
        Assertions.assertEquals(AsyncContextMode.SHARED, byName.mode());
        Assertions.assertNull(byName.then());

        CommandInfo.Await one = info(flow, 2, CommandInfo.Await.class);
        Assertions.assertEquals(AwaitKind.ONE, one.kind());
        Assertions.assertEquals(List.of("f"), one.names());
        Assertions.assertEquals(Duration.ofSeconds(5), one.timeout());

        CommandInfo.Await all = info(flow, 3, CommandInfo.Await.class);
        Assertions.assertEquals(AwaitKind.ALL, all.kind());
        Assertions.assertEquals(List.of("f", "g"), all.names());
        Assertions.assertEquals(Duration.ofSeconds(30), all.timeout(), "default timeout");

        CommandInfo.Await any = info(flow, 4, CommandInfo.Await.class);
        Assertions.assertEquals(AwaitKind.ANY, any.kind());
        Assertions.assertEquals(Duration.ofMinutes(1), any.timeout());
    }

    // -----------------------------------------------------------------------
    // Control flow
    // -----------------------------------------------------------------------

    @Test
    public void controlFlowReportsNestedBodies() {
        Condition cond = condition(() -> true);
        Function<List<Integer>> source = function(() -> List.of(1, 2));
        Condition stop = condition(() -> false);
        Function<Integer> result = function(() -> 7);

        RuleFlow<Integer> flow = RuleFlow.builder()
                .name("controlInfoFlow")
                .when(cond, b -> b.bind("t", 1), b -> b.bind("o", 2))
                .when(cond, b -> b.exit())
                .forEach(source, "item", stop, b -> b.execute(action(() -> {})))
                .forEach(source, "item", b -> b.bind("z", 1))
                .scope("named", b -> b.bind("n", 1))
                .scope(b -> b.bind("a", 1))
                .onException(IllegalArgumentException.class, b -> b.bind("g", 1))
                .exit(result)
                .build();

        CommandInfo.When when = info(flow, 0, CommandInfo.When.class);
        Assertions.assertSame(cond.getDefinition(), when.condition().method());
        Assertions.assertEquals(1, when.then().size());
        Assertions.assertEquals(1, when.otherwise().size());
        Assertions.assertEquals("t", ((CommandInfo.Bind) when.then().get(0)).names().get(0).name());
        Assertions.assertEquals("o", ((CommandInfo.Bind) when.otherwise().get(0)).names().get(0).name());

        CommandInfo.When whenNoOtherwise = info(flow, 1, CommandInfo.When.class);
        Assertions.assertTrue(whenNoOtherwise.otherwise().isEmpty());
        CommandInfo.Exit plainExit = (CommandInfo.Exit) whenNoOtherwise.then().get(0);
        Assertions.assertNull(plainExit.result(), "exit() returns the rule context");

        CommandInfo.ForEach forEach = info(flow, 2, CommandInfo.ForEach.class);
        Assertions.assertEquals("item", forEach.item());
        Assertions.assertSame(source.getDefinition(), forEach.source().method());
        Assertions.assertSame(stop.getDefinition(), forEach.stop().method());
        Assertions.assertEquals(1, forEach.body().size());
        Assertions.assertInstanceOf(CommandInfo.Execute.class, forEach.body().get(0));

        CommandInfo.ForEach forEachNoStop = info(flow, 3, CommandInfo.ForEach.class);
        Assertions.assertNull(forEachNoStop.stop());

        CommandInfo.Scope named = info(flow, 4, CommandInfo.Scope.class);
        Assertions.assertEquals("named", named.name());
        Assertions.assertEquals(1, named.body().size());

        CommandInfo.Scope anonymous = info(flow, 5, CommandInfo.Scope.class);
        Assertions.assertNull(anonymous.name());

        CommandInfo.Exit exit = info(flow, 6, CommandInfo.Exit.class);
        Assertions.assertSame(result.getDefinition(), exit.result().method());

        CommandInfo.Handler global = flow.getGlobalHandler().getInfo();
        Assertions.assertEquals(IllegalArgumentException.class, global.exceptionType());
        Assertions.assertEquals(1, global.body().size());

        // The live commands expose their parts too.
        WhenCommand whenCommand = (WhenCommand) flow.getCommands().get(0);
        Assertions.assertSame(cond, whenCommand.getCondition());
        Assertions.assertEquals(1, whenCommand.getBody().size());
        Assertions.assertEquals(1, whenCommand.getOtherwiseBody().size());
        ForEachCommand forEachCommand = (ForEachCommand) flow.getCommands().get(2);
        Assertions.assertSame(source, forEachCommand.getListSource());
        Assertions.assertEquals("item", forEachCommand.getElementBindingName());
        Assertions.assertSame(stop, forEachCommand.getStopCondition());

        // Execution is unchanged. The flow above exits early, so run one without the early exit.
        RuleFlow<Integer> runnable = RuleFlow.builder()
                .name("controlRunFlow")
                .when(cond, b -> b.bind("t", 1), b -> b.bind("o", 2))
                .forEach(source, "item", stop, b -> b.execute(action(() -> {})))
                .exit(result)
                .build();
        Assertions.assertEquals(7, runnable.run());
    }

    // -----------------------------------------------------------------------
    // Custom commands
    // -----------------------------------------------------------------------

    public static class TwiceCommand extends ContainerCommand {
        @Override
        public void execute(RuleFlowExecutionContext ctx) {
            for (int i = 0; i < 2; i++) {
                for (RuleFlowCommand cmd : getBody()) cmd.execute(ctx);
            }
        }
    }

    public static class SelfDescribingCommand implements RuleFlowCommand {
        @Override
        public void execute(RuleFlowExecutionContext ctx) {
        }

        @Override
        public CommandInfo getInfo() {
            return new CommandInfo.Scope("self-described", List.of());
        }
    }

    public static class CustomFlowBuilder extends RuleFlowBuilderTemplate<CustomFlowBuilder> {
        @Override
        protected CustomFlowBuilder newInstance() {
            return new CustomFlowBuilder();
        }

        public CustomFlowBuilder twice(Consumer<CustomFlowBuilder> body) {
            return runContainer(new TwiceCommand(), body);
        }
    }

    @Test
    public void customCommandsReportCustomOrTheirOwnInfo() {
        CustomFlowBuilder builder = new CustomFlowBuilder();
        builder.name("customInfoFlow");
        RuleFlow<RuleContext> flow = builder
                .twice(b -> b.bind("a", 1))
                .command(ctx -> {})
                .command(new SelfDescribingCommand())
                .build();

        CommandInfo.Custom container = info(flow, 0, CommandInfo.Custom.class);
        Assertions.assertEquals(TwiceCommand.class.getName(), container.className());
        Assertions.assertEquals(1, container.body().size());
        Assertions.assertInstanceOf(CommandInfo.Bind.class, container.body().get(0));

        CommandInfo.Custom leaf = info(flow, 1, CommandInfo.Custom.class);
        Assertions.assertTrue(leaf.body().isEmpty());
        Assertions.assertNotNull(leaf.className());

        CommandInfo.Scope described = info(flow, 2, CommandInfo.Scope.class);
        Assertions.assertEquals("self-described", described.name());
    }

    @Test
    public void infoOfWholeFlowIsInOrder() {
        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("orderFlow")
                .bind("a", 1)
                .run(rule("r"))
                .scope(b -> b.bind("b", 2))
                .build();

        List<CommandInfo> infos = CommandInfo.of(flow.getCommands());
        Assertions.assertEquals(3, infos.size());
        Assertions.assertInstanceOf(CommandInfo.Bind.class, infos.get(0));
        Assertions.assertInstanceOf(CommandInfo.Run.class, infos.get(1));
        Assertions.assertInstanceOf(CommandInfo.Scope.class, infos.get(2));
        Assertions.assertTrue(CommandInfo.of(null).isEmpty());
        Assertions.assertTrue(CommandInfo.of(List.of()).isEmpty());
    }
}
