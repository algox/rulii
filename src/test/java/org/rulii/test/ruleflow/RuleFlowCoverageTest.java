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
import org.rulii.bind.ScopedBindings;
import org.rulii.context.RuleContext;
import org.rulii.model.UnrulyException;
import org.rulii.registry.RuleRegistry;
import org.rulii.rule.Rule;
import org.rulii.ruleflow.AsyncContextMode;
import org.rulii.ruleflow.RuleFlow;
import org.rulii.ruleflow.RuleFlowBuilderTemplate;
import org.rulii.ruleflow.RuleFlowDefinition;
import org.rulii.ruleflow.RuleFlowExecutionContext;
import org.rulii.ruleflow.RuleFlowListener;
import org.rulii.ruleflow.command.AsyncRunCommand;
import org.rulii.ruleflow.command.AwaitAllCommand;
import org.rulii.ruleflow.command.AwaitAnyCommand;
import org.rulii.ruleflow.command.AwaitCommand;
import org.rulii.ruleflow.command.ContainerCommand;
import org.rulii.ruleflow.command.RuleFlowCommand;
import org.rulii.ruleflow.command.RunCommand;
import org.rulii.ruleset.RuleSet;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.rulii.model.action.Actions.action;
import static org.rulii.model.condition.Conditions.condition;
import static org.rulii.model.function.Functions.function;

/**
 * Coverage tests for RuleFlow - registry-based steps, builder overloads, custom
 * containers/commands, named-scope result binding, and await failure paths.
 *
 * @author Max Arulananthan
 * @since 2.0
 */
public class RuleFlowCoverageTest {

    public RuleFlowCoverageTest() {
        super();
    }

    // =========================================================================
    // Registry-based execution
    // =========================================================================

    @org.rulii.annotation.Rule
    public static class RegistryClassRule {

        public RegistryClassRule() {
            super();
        }

        @org.rulii.annotation.Given
        public boolean when() {
            return true;
        }
    }

    private static RuleRegistry buildRegistry(AtomicInteger counter) {
        Rule namedRule = Rule.builder().name("regRule")
                .given(condition(() -> true))
                .then(action(() -> { counter.incrementAndGet(); }))
                .build();
        RuleSet<?> ruleSet = RuleSet.builder().with("regRuleSet").rule(namedRule).build();
        RuleFlow<?> nested = RuleFlow.builder().name("regFlow")
                .execute(action(() -> { counter.incrementAndGet(); })).build();

        return RuleRegistry.builder()
                .register(namedRule)
                .register(ruleSet)
                .register(nested)
                .register(Rule.builder().build(RegistryClassRule.class))
                .build();
    }

    @Test
    public void testRunByRegistryNameAndClass() {
        AtomicInteger counter = new AtomicInteger();
        RuleContext ctx = RuleContext.builder().with(Bindings.builder().standard()).registry(buildRegistry(counter)).build();

        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("registryFlow")
                .run("regRule")
                .run("regRuleSet", spec -> spec.as("rsResult"))
                .run(RegistryClassRule.class)
                .run(RegistryClassRule.class, spec -> spec.as("classResult"))
                .build();

        flow.run(ctx);
        // regRule ran once directly and once inside regRuleSet.
        Assertions.assertEquals(2, counter.get());
    }

    @Test
    public void testRunDirectRuleSetAndFlowWithSpecs() {
        AtomicInteger counter = new AtomicInteger();
        Rule rule = Rule.builder().name("innerRule")
                .given(condition(() -> true))
                .then(action(() -> { counter.incrementAndGet(); }))
                .build();
        RuleSet<?> ruleSet = RuleSet.builder().with("directRuleSet").rule(rule).build();
        RuleFlow<?> nested = RuleFlow.builder().name("nestedFlow")
                .execute(action(() -> { counter.incrementAndGet(); })).build();

        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("directFlow")
                .run(ruleSet)                                        // no-spec RuleSet overload
                // Bind into the global scope so the results survive the flow scope being popped.
                .run(ruleSet, spec -> spec.as(ScopedBindings.GLOBAL_SCOPE, "rsResult"))
                .run(nested, spec -> spec.as(ScopedBindings.GLOBAL_SCOPE, "nestedResult"))
                .apply(function(() -> 5))                            // no-spec apply
                .build();

        RuleContext ctx = flow.run();
        Assertions.assertEquals(3, counter.get());
        Assertions.assertNotNull(ctx.getBindings().getValue("rsResult"));
        Assertions.assertNotNull(ctx.getBindings().getValue("nestedResult"));
    }

    @Test
    public void testRunByName_defaultContextEmptyRegistry_throws() {
        // The default RuleContext always carries an (empty) registry, so a name lookup
        // fails with "not found" rather than "no registry configured".
        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("noRegistryFlow")
                .run("regRule")
                .build();

        UnrulyException e = Assertions.assertThrows(UnrulyException.class, flow::run);
        boolean found = false;
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t.getMessage() != null && t.getMessage().contains("No Runnable found")) {
                found = true;
                break;
            }
        }
        Assertions.assertTrue(found);
    }

    @Test
    public void testRunByName_notFoundInRegistry_throws() {
        RuleContext ctx = RuleContext.builder().with(Bindings.builder().standard()).registry(buildRegistry(new AtomicInteger())).build();
        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("missingNameFlow")
                .run("noSuchRunnable")
                .build();

        Assertions.assertThrows(UnrulyException.class, () -> flow.run(ctx));
    }

    @Test
    public void testRunByClass_notFoundInRegistry_throws() {
        RuleContext ctx = RuleContext.builder().with(Bindings.builder().standard()).registry(buildRegistry(new AtomicInteger())).build();
        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("missingClassFlow")
                .run(String.class)
                .build();

        Assertions.assertThrows(UnrulyException.class, () -> flow.run(ctx));
    }

    @Test
    public void testAsyncRunOverloads() {
        AtomicInteger counter = new AtomicInteger();
        RuleContext ctx = RuleContext.builder().with(Bindings.builder().standard()).registry(buildRegistry(counter)).build();

        Rule rule = Rule.builder().name("asyncRule")
                .given(condition(() -> true))
                .then(action(() -> { counter.incrementAndGet(); }))
                .build();
        RuleSet<?> ruleSet = RuleSet.builder().with("asyncRuleSet").rule(rule).build();
        RuleFlow<?> nested = RuleFlow.builder().name("asyncNested")
                .execute(action(() -> { counter.incrementAndGet(); })).build();

        // withImmutableBindings() isolates each task in its own context: in the default SHARED
        // mode all tasks push/pop scopes on the caller's own scope stack, racing with the main
        // thread's as() bindings (an f-binding can land in a task's transient scope and vanish
        // with it - observed as intermittent NoSuchBindingException on awaitAll).
        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("asyncOverloadFlow")
                .asyncRun(ruleSet, spec -> spec.as("f1").withImmutableBindings())
                .asyncRun(nested, spec -> spec.as("f2").withImmutableBindings())
                .asyncRun("regRule", spec -> spec.as("f3").withImmutableBindings())
                .asyncRun(RegistryClassRule.class, spec -> spec.as("f4").withImmutableBindings())
                .awaitAll("f1", "f2", "f3", "f4")
                .build();

        flow.run(ctx);
        // asyncRule (via ruleSet), asyncNested, and regRule each increment once.
        Assertions.assertEquals(3, counter.get());
    }

    @Test
    public void testAsyncRunFireAndForgetOverloads_buildAndRun() {
        AtomicInteger counter = new AtomicInteger();
        RuleContext ctx = RuleContext.builder().with(Bindings.builder().standard()).registry(buildRegistry(counter)).build();

        Rule rule = Rule.builder().name("fafRule")
                .given(condition(() -> true))
                .build();
        RuleSet<?> ruleSet = RuleSet.builder().with("fafRuleSet").rule(rule).build();
        RuleFlow<?> nested = RuleFlow.builder().name("fafNested")
                .execute(action(() -> {})).build();

        // No as() binding - fire-and-forget; the flow itself must still complete cleanly.
        // SHARED-mode tasks race on the caller's scope stack (see testAsyncRunOverloads), so a
        // task may fail in the background - but with nothing awaiting the futures, the flow's
        // own completion must be unaffected either way.
        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("fireAndForgetFlow")
                .asyncRun(ruleSet)
                .asyncRun(nested)
                .asyncRun("regRule")
                .asyncRun(RegistryClassRule.class)
                .build();

        Assertions.assertDoesNotThrow(() -> flow.run(ctx));
    }

    // =========================================================================
    // RunSpec.as(scope, binding) â€” named-scope result target
    // =========================================================================

    @Test
    public void testRunSpec_asIntoNamedScope() {
        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("namedScopeResultFlow")
                .apply(function(() -> 21), spec -> spec.as(ScopedBindings.GLOBAL_SCOPE, "half"))
                .build();

        RuleContext ctx = flow.run();
        Assertions.assertEquals(21, (Integer) ctx.getBindings().getValue("half"));
    }

    // =========================================================================
    // Custom commands and containers
    // =========================================================================

    public static class CustomFlowBuilder extends RuleFlowBuilderTemplate<CustomFlowBuilder> {

        public CustomFlowBuilder() {
            super();
        }

        @Override
        protected CustomFlowBuilder newInstance() {
            return new CustomFlowBuilder();
        }

        public CustomFlowBuilder twice(Consumer<CustomFlowBuilder> body) {
            return runContainer(new TwiceCommand(), body);
        }

        public String peekName() {
            return getName();
        }
    }

    public static class TwiceCommand extends ContainerCommand {

        public TwiceCommand() {
            super();
        }

        @Override
        public void execute(RuleFlowExecutionContext ctx) {
            for (int i = 0; i < 2; i++) {
                for (RuleFlowCommand cmd : getBody()) {
                    cmd.execute(ctx);
                }
            }
        }
    }

    @Test
    public void testCustomContainer_executesBodyTwice() {
        AtomicInteger counter = new AtomicInteger();

        CustomFlowBuilder builder = new CustomFlowBuilder();
        builder.name("customContainerFlow");
        Assertions.assertEquals("customContainerFlow", builder.peekName());

        RuleFlow<RuleContext> flow = builder
                .twice(b -> b.execute(action(() -> { counter.incrementAndGet(); })))
                .build();

        flow.run();
        Assertions.assertEquals(2, counter.get());
    }

    @Test
    public void testCommand_injectsCustomLeafCommand() {
        AtomicInteger counter = new AtomicInteger();

        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("customCommandFlow")
                .command(ctx -> counter.incrementAndGet())
                .build();

        flow.run();
        Assertions.assertEquals(1, counter.get());
    }

    // =========================================================================
    // Await commands â€” failure paths and getters
    // =========================================================================

    @Test
    public void testAwait_timeout_throws() {
        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("awaitTimeoutFlow")
                .bind("never", new CompletableFuture<Integer>())
                .await("never", 50, TimeUnit.MILLISECONDS)
                .build();

        UnrulyException e = Assertions.assertThrows(UnrulyException.class, flow::run);
        Assertions.assertTrue(e.getMessage().contains("timed out"));
    }

    @Test
    public void testAwaitAll_timeout_throws() {
        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("awaitAllTimeoutFlow")
                .bind("never", new CompletableFuture<Integer>())
                .awaitAll(50, TimeUnit.MILLISECONDS, "never")
                .build();

        Assertions.assertThrows(UnrulyException.class, flow::run);
    }

    @Test
    public void testAwaitAny_timeout_throws() {
        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("awaitAnyTimeoutFlow")
                .bind("never", new CompletableFuture<Integer>())
                .awaitAny(50, TimeUnit.MILLISECONDS, "never")
                .build();

        Assertions.assertThrows(UnrulyException.class, flow::run);
    }

    @Test
    public void testAwait_failedFuture_throws() {
        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("awaitFailedFlow")
                .bind("failed", CompletableFuture.failedFuture(new IllegalStateException("boom")))
                .await("failed")
                .build();

        Assertions.assertThrows(UnrulyException.class, flow::run);
    }

    @Test
    public void testAwaitAll_failedFuture_throws() {
        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("awaitAllFailedFlow")
                .bind("ok", CompletableFuture.completedFuture(1))
                .bind("failed", CompletableFuture.failedFuture(new IllegalStateException("boom")))
                .awaitAll("ok", "failed")
                .build();

        Assertions.assertThrows(UnrulyException.class, flow::run);
    }

    @Test
    public void testAwaitAny_completedFutureWins() {
        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("awaitAnyHappyFlow")
                .bind("slow", new CompletableFuture<Integer>())
                .bind("fast", CompletableFuture.completedFuture(42))
                .awaitAny("slow", "fast")
                .build();

        Assertions.assertDoesNotThrow(() -> { flow.run(); });
    }

    @Test
    public void testAwait_bindingIsNotAFuture_throws() {
        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("awaitNotFutureFlow")
                .bind("notFuture", 42)
                .await("notFuture")
                .build();

        UnrulyException e = Assertions.assertThrows(UnrulyException.class, flow::run);
        Assertions.assertTrue(e.getMessage().contains("CompletableFuture"));
    }

    @Test
    public void testAwaitAll_bindingIsNotAFuture_throws() {
        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("awaitAllNotFutureFlow")
                .bind("notFuture", 42)
                .awaitAll("notFuture")
                .build();

        Assertions.assertThrows(UnrulyException.class, flow::run);
    }

    @Test
    public void testAwaitAny_bindingIsNotAFuture_throws() {
        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("awaitAnyNotFutureFlow")
                .bind("notFuture", 42)
                .awaitAny("notFuture")
                .build();

        Assertions.assertThrows(UnrulyException.class, flow::run);
    }

    @Test
    public void testAwaitCommand_getters() {
        AwaitCommand await = new AwaitCommand("myFuture", 5, TimeUnit.SECONDS);
        Assertions.assertEquals("myFuture", await.getBindingName());
        Assertions.assertEquals(5L, await.getTimeout());
        Assertions.assertEquals(TimeUnit.SECONDS, await.getTimeUnit());
    }

    @Test
    public void testAwaitAllCommand_gettersAndValidation() {
        AwaitAllCommand awaitAll = new AwaitAllCommand(7, TimeUnit.MINUTES, "a", "b");
        Assertions.assertEquals(7L, awaitAll.getTimeout());
        Assertions.assertEquals(TimeUnit.MINUTES, awaitAll.getTimeUnit());
        Assertions.assertArrayEquals(new String[]{"a", "b"}, awaitAll.getBindingNames());

        Assertions.assertThrows(IllegalArgumentException.class, () -> new AwaitAllCommand(1, TimeUnit.SECONDS));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new AwaitAllCommand(1, null, "a"));
    }

    @Test
    public void testAwaitAnyCommand_gettersAndValidation() {
        AwaitAnyCommand awaitAny = new AwaitAnyCommand(3, TimeUnit.HOURS, "x");
        Assertions.assertEquals(3L, awaitAny.getTimeout());
        Assertions.assertEquals(TimeUnit.HOURS, awaitAny.getTimeUnit());
        Assertions.assertArrayEquals(new String[]{"x"}, awaitAny.getBindingNames());

        Assertions.assertThrows(IllegalArgumentException.class, () -> new AwaitAnyCommand(1, TimeUnit.SECONDS));
    }

    // =========================================================================
    // Command getters
    // =========================================================================

    @Test
    public void testRunCommand_getters() {
        Rule rule = Rule.builder().name("getterRule").given(condition(() -> true)).build();
        RunCommand command = new RunCommand(rule, null, null, "result", "myScope", "params", null);

        Assertions.assertSame(rule, command.getRunnable());
        Assertions.assertNull(command.getNameInRegistry());
        Assertions.assertNull(command.getClassInRegistry());
        Assertions.assertEquals("result", command.getBindingName());
        Assertions.assertEquals("myScope", command.getBindingScopeName());
        Assertions.assertEquals("params", command.getParams());
        Assertions.assertNull(command.getExceptionHandler());
    }

    @Test
    public void testAsyncRunCommand_getters() {
        Rule rule = Rule.builder().name("asyncGetterRule").given(condition(() -> true)).build();
        AsyncRunCommand command = new AsyncRunCommand(rule, null, null, "future",
                AsyncContextMode.SHARED, null, null, null, null);

        Assertions.assertSame(rule, command.getRunnable());
        Assertions.assertNull(command.getNameInRegistry());
        Assertions.assertNull(command.getClassInRegistry());
        Assertions.assertEquals("future", command.getBindingName());
        Assertions.assertEquals(AsyncContextMode.SHARED, command.getContextMode());
        Assertions.assertNull(command.getCustomContext());
        Assertions.assertNull(command.getContinuationResultBindingName());
        Assertions.assertTrue(command.getContinuation().isEmpty());
        Assertions.assertNull(command.getExceptionHandler());
    }

    // =========================================================================
    // Definition and listener defaults
    // =========================================================================

    @Test
    public void testRuleFlowDefinition() {
        RuleFlow<Integer> flow = RuleFlow.builder()
                .name("definitionFlow")
                .description("definition test")
                .bind(x -> 1)
                .<Integer>returning(function((Integer x) -> x))
                .build();

        RuleFlowDefinition definition = flow.getDefinition();
        Assertions.assertNotNull(definition.getSource());
        Assertions.assertNotNull(definition.getResultType());
        Assertions.assertTrue(definition.toString().contains("definitionFlow"));

        // Without returning(), the result type is undeclared.
        RuleFlow<RuleContext> plain = RuleFlow.builder().name("plainFlow").bind(x -> 1).build();
        Assertions.assertNull(plain.getDefinition().getResultType());
    }

    @Test
    public void testRuleFlowListener_defaultsAreNoOps() {
        RuleFlowListener listener = new RuleFlowListener() {};
        Assertions.assertDoesNotThrow(() -> {
            listener.onRuleFlowFinalizer(null, null);
            listener.onRuleFlowResult(null, null);
        });
    }
}
