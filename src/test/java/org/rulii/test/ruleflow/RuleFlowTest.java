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

import org.junit.jupiter.api.Test;
import org.rulii.bind.Bindings;
import org.rulii.context.RuleContext;
import org.rulii.model.UnrulyException;
import org.rulii.model.action.Action;
import org.rulii.model.condition.Condition;
import org.rulii.model.function.Function;
import org.rulii.registry.RuleRegistry;
import org.rulii.rule.Rule;
import org.rulii.ruleflow.RuleFlow;
import org.rulii.trace.Tracer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.rulii.model.action.Actions.action;
import static org.rulii.model.condition.Conditions.condition;
import static org.rulii.model.function.Functions.function;

/**
 * Comprehensive tests for the RuleFlow fluent pipeline API.
 *
 * @author Max Arulananthan
 * @since 2.0
 */
public class RuleFlowTest {

    public RuleFlowTest() {
        super();
    }

    public static class Order {
        private final String orderId;
        private final int amount;

        public Order(String orderId, int amount) {
            super();
            this.orderId = orderId;
            this.amount = amount;
        }

        public String getOrderId() { return orderId; }
        public int getAmount() { return amount; }
    }

    // =========================================================================
    // Basic execution — bind, apply, run
    // =========================================================================

    @Test
    public void testBindStep() {
        RuleFlow<Integer> flow = RuleFlow.builder()
                .name("bindFlow")
                .bind(x -> 42)
                .<Integer>returning(function((Integer x) -> x))
                .build();

        assertEquals(Integer.valueOf(42), flow.run());
    }

    @Test
    public void testApplyStep_bindsResult() {
        RuleFlow<Integer> flow = RuleFlow.builder()
                .name("applyFlow")
                .apply(function((Integer x) -> x * 3)).as("result")
                .<Integer>returning(function((Integer result) -> result))
                .build();

        Integer result = flow.run(x -> 10);
        assertEquals(30, result);
    }

    @Test
    public void testRunRule_executesAction() {
        List<String> log = new ArrayList<>();
        Rule logRule = Rule.builder()
                .name("logRule")
                .then(action(() -> log.add("executed")))
                .build();

        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("runRuleFlow")
                .run(logRule)
                .build();

        flow.run();
        assertEquals(List.of("executed"), log);
    }

    @Test
    public void testRunRule_receivesBinding() {
        List<Integer> captured = new ArrayList<>();
        Rule captureRule = Rule.builder()
                .name("captureRule")
                .then(action((Integer value) -> captured.add(value)))
                .build();

        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("captureFlow")
                .run(captureRule)
                .build();

        flow.run(value -> 99);
        assertEquals(List.of(99), captured);
    }

    @Test
    public void testDefaultResult_isRuleContext() {
        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("defaultResultFlow")
                .bind(x -> 1)
                .build();

        Object result = flow.run();
        assertInstanceOf(RuleContext.class, result);
    }

    // =========================================================================
    // Input parameters
    // =========================================================================

    @Test
    public void testRequiredParam_present() {
        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("paramFlow")
                .param("orderId", String.class)
                .build();

        assertDoesNotThrow(() -> flow.run(orderId -> "ABC-123"));
    }

    @Test
    public void testRequiredParam_missing_throws() {
        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("missingParamFlow")
                .param("orderId", String.class)
                .build();

        assertThrows(UnrulyException.class, flow::run);
    }

    @Test
    public void testOptionalParam_defaultApplied() {
        Function<Integer> defaultDiscount = function((RuleContext ctx) -> 5);
        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("defaultParamFlow")
                .param("discount", Integer.class, defaultDiscount)
                .build();

        RuleContext ctx = flow.run();
        assertEquals(Integer.valueOf(5), ctx.getBindings().getValue("discount"));
    }

    @Test
    public void testOptionalParam_suppliedOverridesDefault() {
        Function<Integer> defaultDiscount = function((RuleContext ctx) -> 5);
        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("overrideDefaultFlow")
                .param("discount", Integer.class, defaultDiscount)
                .build();

        RuleContext ctx = flow.run(discount -> 15);
        assertEquals(Integer.valueOf(15), ctx.getBindings().getValue("discount"));
    }

    @Test
    public void testOptionalParam_notRequired_missingIsOk() {
        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("optionalParamFlow")
                .param("extra", String.class, false)
                .build();

        assertDoesNotThrow(() -> { flow.run(); });
    }

    // =========================================================================
    // Returning — result extraction
    // =========================================================================

    @Test
    public void testReturning_extractsTypedResult() {
        RuleFlow<String> flow = RuleFlow.builder()
                .name("returningFlow")
                .bind(greeting -> "hello")
                .<String>returning(function((String greeting) -> greeting.toUpperCase()))
                .build();

        assertEquals("HELLO", flow.run());
    }

    @Test
    public void testReturning_noArg_returnsContext() {
        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("returningNoArgFlow")
                .bind(x -> 7)
                .returning()
                .build();

        assertInstanceOf(RuleContext.class, flow.run());
    }

    // =========================================================================
    // Early exit — exit()
    // =========================================================================

    @Test
    public void testExit_stopsExecution() {
        List<String> log = new ArrayList<>();
        Rule afterExitRule = Rule.builder().name("afterExit")
                .then(action(() -> log.add("should not run")))
                .build();

        RuleFlow<String> flow = RuleFlow.builder()
                .name("exitFlow")
                .when(condition(() -> true)).then(b -> b
                        .bind(result -> "early")
                        .<String>exit(function((String result) -> result)))
                .run(afterExitRule)
                .build();

        assertEquals("early", flow.run());
        assertTrue(log.isEmpty());
    }

    @Test
    public void testExitNoArg_returnsContext() {
        List<String> log = new ArrayList<>();
        Rule afterExitRule = Rule.builder().name("afterExitRule")
                .then(action(() -> log.add("should not run")))
                .build();

        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("exitNoArgFlow")
                .when(condition(() -> true)).then(b -> b.exit())
                .run(afterExitRule)
                .build();

        assertInstanceOf(RuleContext.class, flow.run());
        assertTrue(log.isEmpty());
    }

    @Test
    public void testExit_insideWhenBranch_stopsFlow() {
        List<String> log = new ArrayList<>();
        Rule afterRule = Rule.builder().name("after")
                .then(action(() -> log.add("after")))
                .build();

        RuleFlow<String> flow = RuleFlow.builder()
                .name("exitInBranchFlow")
                .when(condition(() -> true)).then(b -> b
                        .bind(r -> "exitValue")
                        .<String>exit(function((String r) -> r)))
                .run(afterRule)
                .build();

        assertEquals("exitValue", flow.run());
        assertTrue(log.isEmpty());
    }

    // =========================================================================
    // when / then / otherwise
    // =========================================================================

    @Test
    public void testWhen_conditionTrue_runsThenbranch() {
        List<String> log = new ArrayList<>();
        Rule thenRule = Rule.builder().name("thenRule")
                .then(action(() -> log.add("then"))).build();
        Rule otherwiseRule = Rule.builder().name("otherwiseRule")
                .then(action(() -> log.add("otherwise"))).build();

        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("whenThenFlow")
                .when(condition((Integer x) -> x > 0)).run(thenRule).otherwise(b -> b.run(otherwiseRule))
                .build();

        flow.run(x -> 10);
        assertEquals(List.of("then"), log);
    }

    @Test
    public void testWhen_conditionFalse_runsOtherwise() {
        List<String> log = new ArrayList<>();
        Rule thenRule = Rule.builder().name("thenRule")
                .then(action(() -> log.add("then"))).build();
        Rule otherwiseRule = Rule.builder().name("otherwiseRule")
                .then(action(() -> log.add("otherwise"))).build();

        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("whenOtherwiseFlow")
                .when(condition((Integer x) -> x > 0)).run(thenRule).otherwise(b -> b.run(otherwiseRule))
                .build();

        flow.run(x -> -5);
        assertEquals(List.of("otherwise"), log);
    }

    @Test
    public void testWhen_noOtherwise_conditionFalse_doesNothing() {
        List<String> log = new ArrayList<>();
        Rule thenRule = Rule.builder().name("thenRule")
                .then(action(() -> log.add("then"))).build();

        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("whenNoOtherwiseFlow")
                .when(condition(() -> false)).run(thenRule)
                .build();

        flow.run();
        assertTrue(log.isEmpty());
    }

    @Test
    public void testWhen_multiCommandThenBranch() {
        List<String> captured = new ArrayList<>();
        Rule captureRule = Rule.builder().name("capture")
                .then(action((String step, String step2) -> { captured.add(step); captured.add(step2); }))
                .build();

        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("whenMultiFlow")
                .when(condition(() -> true)).then(b -> b
                        .bind(step -> "a")
                        .bind(step2 -> "b"))
                .run(captureRule)
                .build();

        flow.run();
        assertEquals("a", captured.get(0));
        assertEquals("b", captured.get(1));
    }

    @Test
    public void testWhen_commandsContinueAfterSealed() {
        List<String> log = new ArrayList<>();
        Rule afterRule = Rule.builder().name("afterWhen")
                .then(action(() -> log.add("after"))).build();
        Rule thenRule = Rule.builder().name("thenRule")
                .then(action(() -> log.add("then"))).build();

        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("whenContinueFlow")
                .when(condition(() -> true)).run(thenRule)
                .run(afterRule)
                .build();

        flow.run();
        assertEquals(List.of("then", "after"), log);
    }

    @Test
    public void testWhen_otherwiseMultiCommandBranch() {
        List<String> captured = new ArrayList<>();
        Rule captureRule = Rule.builder().name("capture")
                .then(action((String y, String z) -> { captured.add(y); captured.add(z); }))
                .build();

        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("otherwiseMultiFlow")
                .when(condition(() -> false)).then(b -> b.bind(x -> "then"))
                        .otherwise(b -> b.bind(y -> "ow1").bind(z -> "ow2"))
                .run(captureRule)
                .build();

        flow.run();
        assertEquals("ow1", captured.get(0));
        assertEquals("ow2", captured.get(1));
    }

    // =========================================================================
    // forEach
    // =========================================================================

    @Test
    public void testForEach_iteratesAll() {
        List<Integer> seen = new ArrayList<>();
        Rule collectRule = Rule.builder().name("collect")
                .then(action((Integer item) -> seen.add(item))).build();
        Function<List<Integer>> listFn = function((RuleContext ctx) -> List.of(1, 2, 3));

        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("forEachFlow")
                .forEach(listFn, "item", b -> b.run(collectRule))
                .build();

        flow.run();
        assertEquals(List.of(1, 2, 3), seen);
    }

    @Test
    public void testForEach_indexBinding() {
        List<Integer> indices = new ArrayList<>();
        Rule indexRule = Rule.builder().name("indexRule")
                .then(action((Integer index) -> indices.add(index))).build();
        Function<List<String>> listFn = function((RuleContext ctx) -> List.of("a", "b", "c"));

        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("forEachIndexFlow")
                .forEach(listFn, "item", b -> b.run(indexRule))
                .build();

        flow.run();
        assertEquals(List.of(0, 1, 2), indices);
    }

    @Test
    public void testForEach_stopCondition() {
        List<Integer> seen = new ArrayList<>();
        Rule collectRule = Rule.builder().name("collect")
                .then(action((Integer item) -> seen.add(item))).build();
        Function<List<Integer>> listFn = function((RuleContext ctx) -> List.of(10, 20, 30, 40, 50));
        Condition stopAt1 = condition((Integer index) -> index >= 1);

        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("forEachStopFlow")
                .forEach(listFn, "item", stopAt1, b -> b.run(collectRule))
                .build();

        flow.run();
        assertEquals(List.of(10, 20), seen);
    }

    @Test
    public void testForEach_emptyCollection_doesNothing() {
        List<String> log = new ArrayList<>();
        Rule rule = Rule.builder().name("r")
                .then(action(() -> log.add("ran"))).build();
        Function<List<String>> emptyFn = function((RuleContext ctx) -> List.of());

        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("forEachEmptyFlow")
                .forEach(emptyFn, "item", b -> b.run(rule))
                .build();

        flow.run();
        assertTrue(log.isEmpty());
    }

    // =========================================================================
    // scope / endScope
    // =========================================================================

    @Test
    public void testScope_bindingsDiscardedAfterEnd() {
        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("scopeFlow")
                .scope("myScope")
                .bind(temp -> "temporary")
                .endScope()
                .build();

        RuleContext ctx = flow.run();
        assertNull(ctx.getBindings().getBinding("temp"));
    }

    @Test
    public void testAnonymousScope_bindingsDiscarded() {
        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("anonScopeFlow")
                .scope()
                .bind(inner -> "inside")
                .endScope()
                .build();

        RuleContext ctx = flow.run();
        assertNull(ctx.getBindings().getBinding("inner"));
    }

    @Test
    public void testScope_outerBindingsStillAccessible() {
        List<Object> observed = new ArrayList<>();
        Rule checkRule = Rule.builder().name("check")
                .then(action((RuleContext ctx) -> {
                    observed.add(ctx.getBindings().getValue("outer"));
                    observed.add(ctx.getBindings().getBinding("inner"));
                }))
                .build();

        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("scopeOuterFlow")
                .bind(outer -> "persistent")
                .scope()
                .bind(inner -> "gone")
                .endScope()
                .run(checkRule)
                .build();

        flow.run();
        assertEquals("persistent", observed.get(0));
        assertNull(observed.get(1));
    }

    // =========================================================================
    // Finalizer
    // =========================================================================

    @Test
    public void testFinalizer_runsOnNormalCompletion() {
        List<String> log = new ArrayList<>();

        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("finalizerFlow")
                .bind(x -> 1)
                .finalizer(action(() -> log.add("finalized")))
                .build();

        flow.run();
        assertEquals(List.of("finalized"), log);
    }

    @Test
    public void testFinalizer_runsOnEarlyExit() {
        List<String> log = new ArrayList<>();

        RuleFlow<String> flow = RuleFlow.builder()
                .name("finalizerExitFlow")
                .bind(result -> "done")
                .<String>exit(function((String result) -> result))
                .finalizer(action(() -> log.add("finalized")))
                .build();

        assertEquals("done", flow.run());
        assertEquals(List.of("finalized"), log);
    }

    @Test
    public void testFinalizer_doesNotRunWhenInputCheckFails() {
        List<String> log = new ArrayList<>();

        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("finalizerMissingParamFlow")
                .param("required", String.class)
                .finalizer(action(() -> log.add("finalized")))
                .build();

        assertThrows(UnrulyException.class, flow::run);
        assertTrue(log.isEmpty());
    }

    // =========================================================================
    // Exception handling — step-level
    // =========================================================================

    @Test
    public void testStepHandler_catchesAndContinues() {
        List<String> log = new ArrayList<>();
        Rule badRule = Rule.builder().name("bad")
                .then(action(() -> { throw new UnrulyException("boom"); })).build();
        Rule afterRule = Rule.builder().name("after")
                .then(action(() -> log.add("after"))).build();

        RuleFlow<Boolean> flow = RuleFlow.builder()
                .name("stepHandlerFlow")
                .run(badRule)
                    .onException(UnrulyException.class, b -> b.bind(handled -> true))
                .run(afterRule)
                .<Boolean>returning(function((Boolean handled) -> handled))
                .build();

        Boolean result = flow.run();
        assertEquals(List.of("after"), log);
        assertEquals(true, result);
    }

    @Test
    public void testStepHandler_exceptionBoundAsEx() {
        List<String> messages = new ArrayList<>();
        Rule badRule = Rule.builder().name("bad")
                .then(action(() -> { throw new UnrulyException("testMessage"); })).build();

        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("stepHandlerExFlow")
                .run(badRule)
                    .onException(UnrulyException.class, b ->
                            b.run(Rule.builder().name("catchRule")
                                    .then(action((UnrulyException ex) -> messages.add(ex.getClass().getSimpleName())))
                                    .build()))
                .build();

        flow.run();
        assertEquals(List.of("UnrulyException"), messages);
    }

    @Test
    public void testStepHandler_typeMismatch_rethrows() {
        Rule badRule = Rule.builder().name("bad")
                .then(action(() -> { throw new UnrulyException("unhandled"); })).build();

        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("noMatchHandlerFlow")
                .run(badRule)
                    .onException(IllegalArgumentException.class, b -> b.bind(handled -> true))
                .build();

        assertThrows(UnrulyException.class, flow::run);
    }

    // =========================================================================
    // Exception handling — global handler
    // =========================================================================

    @Test
    public void testGlobalHandler_catchesAndContinues() {
        List<String> log = new ArrayList<>();
        Rule badRule = Rule.builder().name("bad")
                .then(action(() -> { throw new UnrulyException("global boom"); })).build();
        Rule afterRule = Rule.builder().name("after")
                .then(action(() -> log.add("after"))).build();

        RuleFlow<Boolean> flow = RuleFlow.builder()
                .name("globalHandlerFlow")
                .run(badRule)
                .run(afterRule)
                    .<Boolean>returning(function((Boolean globalHandled) -> globalHandled))
                .onException(UnrulyException.class, b -> b.bind(globalHandled -> true))
                .build();

        Boolean result = flow.run();
        assertEquals(List.of("after"), log);
        assertEquals(true, result);
    }

    @Test
    public void testStepHandlerTakesPrecedenceOverGlobal() {
        Rule badRule = Rule.builder().name("bad")
                .then(action(() -> { throw new UnrulyException("precedence"); })).build();

        RuleFlow<String> flow = RuleFlow.builder()
                .name("precedenceFlow")
                .run(badRule)
                    .onException(UnrulyException.class, b -> b.bind(handler -> "step"))
                    .<String>returning(function((String handler) -> handler))
                .onException(UnrulyException.class, b -> b.bind(handler -> "global"))
                .build();

        assertEquals("step", flow.run());
    }

    // =========================================================================
    // run() via BindingDeclarations vs explicit RuleContext
    // =========================================================================

    @Test
    public void testRunWithBindingDeclarations() {
        RuleFlow<String> flow = RuleFlow.builder()
                .name("declFlow")
                .param("name", String.class)
                .<String>returning(function((String name) -> name + " world"))
                .build();

        assertEquals("hello world", flow.run(name -> "hello"));
    }

    @Test
    public void testRunWithExplicitContext() {
        RuleFlow<String> flow = RuleFlow.builder()
                .name("ctxParamFlow")
                .param("name", String.class)
                .<String>returning(function((String name) -> name.toUpperCase()))
                .build();

        Bindings bindings = Bindings.builder().standard();
        bindings.bind("name", "world");
        assertEquals("WORLD", flow.run(RuleContext.builder().build(bindings)));
    }

    // =========================================================================
    // Async execution
    // =========================================================================

    @Test
    public void testRunAsync_completesWithResult() throws ExecutionException, InterruptedException {
        RuleFlow<String> flow = RuleFlow.builder()
                .name("asyncFlow")
                .bind(greeting -> "async")
                .<String>returning(function((String greeting) -> greeting + "!"))
                .build();

        assertEquals("async!", flow.runAsync(RuleContext.builder().build()).get());
    }

    @Test
    public void testRunAsync_withTimeout() throws ExecutionException, InterruptedException {
        RuleFlow<Integer> flow = RuleFlow.builder()
                .name("asyncTimeoutFlow")
                .bind(n -> 42)
                .<Integer>returning(function((Integer n) -> n))
                .build();

        assertEquals(42, (int) flow.runAsync(RuleContext.builder().build(), 5, TimeUnit.SECONDS).get());
    }

    @Test
    public void testRunAsync_exceptionPropagatesToFuture() {
        Rule badRule = Rule.builder().name("bad")
                .then(action(() -> { throw new UnrulyException("async fail"); })).build();

        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("asyncFailFlow")
                .run(badRule)
                .build();

        CompletableFuture<RuleContext> future = flow.runAsync(RuleContext.builder().build());
        ExecutionException ex = assertThrows(ExecutionException.class, future::get);
        assertInstanceOf(UnrulyException.class, ex.getCause());
    }

    // =========================================================================
    // Nested RuleFlow
    // =========================================================================

    @Test
    public void testNestedFlow_innerExecuted() {
        List<String> log = new ArrayList<>();

        RuleFlow<RuleContext> inner = RuleFlow.builder()
                .name("innerFlow")
                .run(Rule.builder().name("innerRule")
                        .then(action(() -> log.add("inner"))).build())
                .build();

        RuleFlow<RuleContext> outer = RuleFlow.builder()
                .name("outerFlow")
                .run(Rule.builder().name("outerRule")
                        .then(action(() -> log.add("outer"))).build())
                .run(inner)
                .build();

        outer.run();
        assertEquals(List.of("outer", "inner"), log);
    }

    // =========================================================================
    // Registry lookup
    // =========================================================================

    @Test
    public void testRunByName_fromRegistry() {
        List<String> log = new ArrayList<>();
        Rule rule = Rule.builder().name("registeredRule")
                .then(action(() -> log.add("ran"))).build();

        RuleRegistry registry = RuleRegistry.builder().register(rule).build();

        RuleContext ctx = RuleContext.builder()
                .with(Bindings.builder().standard())
                .registry(registry)
                .build();

        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("registryNameFlow")
                .run("registeredRule")
                .build();

        flow.run(ctx);
        assertEquals(List.of("ran"), log);
    }

    @Test
    public void testRunByName_notFound_throws() {
        RuleRegistry registry = RuleRegistry.builder().build();
        RuleContext ctx = RuleContext.builder()
                .with(Bindings.builder().standard())
                .registry(registry)
                .build();

        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("registryMissingFlow")
                .run("noSuchRule")
                .build();

        assertThrows(UnrulyException.class, () -> flow.run(ctx));
    }

    @Test
    public void testRunWithoutRegistry_throws() {
        RuleContext ctx = RuleContext.builder().build(Bindings.builder().standard());

        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("noRegistryFlow")
                .run("anyRule")
                .build();

        assertThrows(UnrulyException.class, () -> flow.run(ctx));
    }

    // =========================================================================
    // as() — binding results
    // =========================================================================

    @Test
    public void testApplyAs_resultAccessibleInFlow() {
        RuleFlow<Integer> flow = RuleFlow.builder()
                .name("applyAsFlow")
                .apply(function((Integer x) -> x * 2)).as("doubled")
                .<Integer>returning(function((Integer doubled) -> doubled))
                .build();

        assertEquals(20, (int) flow.run(x -> 10));
    }

    // =========================================================================
    // context() — configurator for run(BindingDeclaration...)
    // =========================================================================

    @Test
    public void testContext_configuratorAppliedOnDeclarationRun() {
        List<String> log = new ArrayList<>();
        Action captureTracer = action((Tracer tracer) -> log.add("tracer-found"));

        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("contextFlow")
                .context(builder -> builder.traceUsing(Tracer.builder().build()))
                .run(Rule.builder().name("r").then(captureTracer).build())
                .build();

        assertDoesNotThrow(() -> flow.run(x -> "value"));
    }

    // =========================================================================
    // Build-time structural validation
    // =========================================================================

    @Test
    public void testBuild_missingName_throws() {
        assertThrows(Exception.class, () ->
                RuleFlow.builder()
                        .bind(x -> 1)
                        .build());
    }

    @Test
    public void testBuild_unclosedScope_throws() {
        assertThrows(UnrulyException.class, () ->
                RuleFlow.builder()
                        .name("badScopeFlow")
                        .scope("unclosed")
                        .build());
    }

    @Test
    public void testBuild_orphanedEndScope_throws() {
        assertThrows(UnrulyException.class, () ->
                RuleFlow.builder()
                        .name("orphanEndScopeFlow")
                        .endScope()
                        .build());
    }

    @Test
    public void testBuild_unreachableCommandsAfterTopLevelExit_throws() {
        Rule unreachable = Rule.builder().name("unreachable")
                .then(action(() -> {})).build();

        assertThrows(UnrulyException.class, () ->
                RuleFlow.builder()
                        .name("unreachableFlow")
                        .exit()
                        .run(unreachable)
                        .build());
    }

    @Test
    public void testBuild_exitInsideBranch_doesNotThrow() {
        assertDoesNotThrow(() ->
                RuleFlow.builder()
                        .name("exitInBranchFlow")
                        .when(condition(() -> true)).then(b -> b.exit())
                        .build());
    }

    // =========================================================================
    // Definition metadata
    // =========================================================================

    @Test
    public void testDefinition_nameDescriptionCommandCount() {
        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("metaFlow")
                .description("A test flow")
                .bind(x -> 1)
                .bind(y -> 2)
                .build();

        assertEquals("metaFlow", flow.getName());
        assertEquals("metaFlow", flow.getDefinition().getName());
        assertEquals("A test flow", flow.getDefinition().getDescription());
        assertEquals(2, flow.getDefinition().getCommandCount());
    }

    @Test
    public void testDefinition_inputParametersReflected() {
        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("paramMetaFlow")
                .param("orderId", String.class)
                .param("amount", Integer.class, false)
                .build();

        List<org.rulii.model.InputParameter<?>> params = flow.getDefinition().getInputParameters();
        assertEquals(2, params.size());
        assertEquals("orderId", params.get(0).name());
        assertTrue(params.get(0).required());
        assertEquals("amount", params.get(1).name());
        assertFalse(params.get(1).required());
    }

    // =========================================================================
    // getFinalizer / getInputParameters accessors
    // =========================================================================

    @Test
    public void testGetFinalizer_nullWhenNotSet() {
        assertNull(RuleFlow.builder().name("noFinalizerFlow").build().getFinalizer());
    }

    @Test
    public void testGetFinalizer_nonNullWhenSet() {
        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("hasFinalizerFlow")
                .finalizer(action(() -> {}))
                .build();

        assertNotNull(flow.getFinalizer());
    }

    @Test
    public void testGetInputParameters_emptyWhenNone() {
        assertTrue(RuleFlow.builder().name("noParamsFlow").build().getInputParameters().isEmpty());
    }

    // =========================================================================
    // Full pipeline integration
    // =========================================================================

    @Test
    public void testFullPipeline_largeAmount() {
        List<String> log = new ArrayList<>();

        Rule validateRule = Rule.builder().name("validate")
                .given(condition((Integer amount) -> amount > 0))
                .then(action((Integer amount) -> log.add("validated:" + amount)))
                .build();
        Rule processRule = Rule.builder().name("process")
                .then(action((Integer amount) -> log.add("processed:" + amount)))
                .build();
        Rule bonusRule = Rule.builder().name("bonus")
                .then(action(() -> log.add("bonus")))
                .build();

        RuleFlow<String> flow = RuleFlow.builder()
                .name("fullPipeline")
                .param("amount", Integer.class)
                .run(validateRule)
                .run(processRule)
                .when(condition((Integer amount) -> amount > 100)).run(bonusRule)
                .<String>returning(function((RuleContext ctx) -> "done"))
                .build();

        assertEquals("done", flow.run(amount -> 150));
        assertEquals(List.of("validated:150", "processed:150", "bonus"), log);
    }

    @Test
    public void testFullPipeline_smallAmount_noBonusRule() {
        List<String> log = new ArrayList<>();

        Rule processRule = Rule.builder().name("process")
                .then(action((Integer amount) -> log.add("processed:" + amount)))
                .build();
        Rule bonusRule = Rule.builder().name("bonus")
                .then(action(() -> log.add("bonus")))
                .build();

        RuleFlow<String> flow = RuleFlow.builder()
                .name("fullPipelineSmall")
                .param("amount", Integer.class)
                .run(processRule)
                .when(condition((Integer amount) -> amount > 100)).run(bonusRule)
                .<String>returning(function((RuleContext ctx) -> "done"))
                .build();

        assertEquals("done", flow.run(amount -> 50));
        assertEquals(List.of("processed:50"), log);
    }

    // =========================================================================
    // run(...).with(...) — step-scoped parameter injection
    // =========================================================================

    @Test
    public void testWith_bindingDeclarations_visibleInsideStep() {
        List<String> captured = new ArrayList<>();
        Rule captureRule = Rule.builder().name("capture")
                .then(action((String greeting) -> captured.add(greeting)))
                .build();

        RuleFlow<Void> flow = RuleFlow.builder()
                .name("withBindingDeclarations")
                .run(captureRule).with(greeting -> "hello")
                .<Void>returning(function((RuleContext ctx) -> null))
                .build();

        flow.run();
        assertEquals(List.of("hello"), captured);
    }

    @Test
    public void testWith_bindingDeclarations_notVisibleAfterStep() {
        List<Object> observed = new ArrayList<>();
        Rule observeRule = Rule.builder().name("observe")
                .then(action((RuleContext ctx) -> observed.add(ctx.getBindings().getBinding("greeting"))))
                .build();
        Rule targetRule = Rule.builder().name("target")
                .then(action((String greeting) -> {}))
                .build();

        RuleFlow<Void> flow = RuleFlow.builder()
                .name("withNotLeaking")
                .run(targetRule).with(greeting -> "hello")
                .run(observeRule)
                .<Void>returning(function((RuleContext ctx) -> null))
                .build();

        flow.run();
        assertNull(observed.get(0));
    }

    @Test
    public void testWith_multipleDeclarations_allVisibleInsideStep() {
        List<Object> captured = new ArrayList<>();
        Rule captureRule = Rule.builder().name("capture")
                .then(action((String firstName, Integer age) -> {
                    captured.add(firstName);
                    captured.add(age);
                }))
                .build();

        RuleFlow<Void> flow = RuleFlow.builder()
                .name("withMultipleDeclarations")
                .run(captureRule).with(firstName -> "Alice", age -> 30)
                .<Void>returning(function((RuleContext ctx) -> null))
                .build();

        flow.run();
        assertEquals("Alice", captured.get(0));
        assertEquals(30, captured.get(1));
    }

    @Test
    public void testWith_pojo_propertiesBoundForStep() {
        List<String> captured = new ArrayList<>();
        Rule captureRule = Rule.builder().name("capture")
                .then(action((String orderId) -> captured.add(orderId)))
                .build();

        RuleFlow<Void> flow = RuleFlow.builder()
                .name("withPojo")
                .run(captureRule).with(new Order("ORD-42", 100))
                .<Void>returning(function((RuleContext ctx) -> null))
                .build();

        flow.run();
        assertEquals(List.of("ORD-42"), captured);
    }

    @Test
    public void testWith_pojo_notVisibleAfterStep() {
        List<Object> observed = new ArrayList<>();
        Rule observeRule = Rule.builder().name("observe")
                .then(action((RuleContext ctx) -> observed.add(ctx.getBindings().getBinding("orderId"))))
                .build();
        Rule targetRule = Rule.builder().name("target")
                .then(action((String orderId) -> {}))
                .build();

        RuleFlow<Void> flow = RuleFlow.builder()
                .name("withPojoNotLeaking")
                .run(targetRule).with(new Order("ORD-99", 0))
                .run(observeRule)
                .<Void>returning(function((RuleContext ctx) -> null))
                .build();

        flow.run();
        assertNull(observed.get(0));
    }

    @Test
    public void testWith_shadowsFlowScopeBinding() {
        List<String> captured = new ArrayList<>();
        Rule captureRule = Rule.builder().name("capture")
                .then(action((String label) -> captured.add(label)))
                .build();

        RuleFlow<Void> flow = RuleFlow.builder()
                .name("withShadowing")
                .bind(label -> "flow-level")
                .run(captureRule).with(label -> "step-level")
                .<Void>returning(function((RuleContext ctx) -> null))
                .build();

        flow.run();
        assertEquals(List.of("step-level"), captured);
    }

    @Test
    public void testWith_flowScopeRestoredAfterStep() {
        List<String> captured = new ArrayList<>();
        Rule before = Rule.builder().name("before")
                .then(action((String label) -> captured.add("before:" + label)))
                .build();
        Rule target = Rule.builder().name("target")
                .then(action((String label) -> captured.add("during:" + label)))
                .build();
        Rule after = Rule.builder().name("after")
                .then(action((String label) -> captured.add("after:" + label)))
                .build();

        RuleFlow<Void> flow = RuleFlow.builder()
                .name("withShadowingRestore")
                .bind(label -> "flow-level")
                .run(before)
                .run(target).with(label -> "step-level")
                .run(after)
                .<Void>returning(function((RuleContext ctx) -> null))
                .build();

        flow.run();
        assertEquals("before:flow-level", captured.get(0));
        assertEquals("during:step-level", captured.get(1));
        assertEquals("after:flow-level", captured.get(2));
    }

    // =========================================================================
    // bind(String name, Object value) — name/value convenience form
    // =========================================================================

    @Test
    public void testBindNameValue_bindsToCurrentScope() {
        List<String> captured = new ArrayList<>();
        Rule captureRule = Rule.builder().name("capture")
                .then(action((String greeting) -> captured.add(greeting)))
                .build();

        RuleFlow<Void> flow = RuleFlow.builder()
                .name("bindNameValue")
                .bind("greeting", "hello")
                .run(captureRule)
                .<Void>returning(function((RuleContext ctx) -> null))
                .build();

        flow.run();
        assertEquals(List.of("hello"), captured);
    }

    @Test
    public void testBindNameValue_nullValueAllowed() {
        List<Object> captured = new ArrayList<>();
        Rule captureRule = Rule.builder().name("capture")
                .then(action((RuleContext ctx) -> captured.add(ctx.getBindings().getValue("item"))))
                .build();

        RuleFlow<Void> flow = RuleFlow.builder()
                .name("bindNameValueNull")
                .bind("item", null)
                .run(captureRule)
                .<Void>returning(function((RuleContext ctx) -> null))
                .build();

        flow.run();
        assertEquals(1, captured.size());
        assertNull(captured.get(0));
    }

    @Test
    public void testBindNameValue_mixedWithDeclarationForm() {
        List<String> captured = new ArrayList<>();
        Rule captureRule = Rule.builder().name("capture")
                .then(action((String first, String last) -> {
                    captured.add(first);
                    captured.add(last);
                }))
                .build();

        RuleFlow<Void> flow = RuleFlow.builder()
                .name("bindNameValueMixed")
                .bind("first", "Alice")
                .bind(last -> "Smith")
                .run(captureRule)
                .<Void>returning(function((RuleContext ctx) -> null))
                .build();

        flow.run();
        assertEquals("Alice", captured.get(0));
        assertEquals("Smith", captured.get(1));
    }

    // =========================================================================
    // bindTo(String scopeName, ...) — scope-targeted binding
    // =========================================================================

    @Test
    public void testBindTo_declarationsLandInNamedScope() {
        List<String> captured = new ArrayList<>();
        Rule captureRule = Rule.builder().name("capture")
                .then(action((String token) -> captured.add(token)))
                .build();

        RuleFlow<Void> flow = RuleFlow.builder()
                .name("bindToDeclarations")
                .scope("authScope")
                    .bindTo("authScope", token -> "abc-123")
                    .run(captureRule)
                .endScope()
                .<Void>returning(function((RuleContext ctx) -> null))
                .build();

        flow.run();
        assertEquals(List.of("abc-123"), captured);
    }

    @Test
    public void testBindTo_nameValueLandsInNamedScope() {
        List<String> captured = new ArrayList<>();
        Rule captureRule = Rule.builder().name("capture")
                .then(action((String token) -> captured.add(token)))
                .build();

        RuleFlow<Void> flow = RuleFlow.builder()
                .name("bindToNameValue")
                .scope("authScope")
                    .bindTo("authScope", "token", "xyz-789")
                    .run(captureRule)
                .endScope()
                .<Void>returning(function((RuleContext ctx) -> null))
                .build();

        flow.run();
        assertEquals(List.of("xyz-789"), captured);
    }

    @Test
    public void testBindTo_outerScopeFromInnerScope() {
        List<String> captured = new ArrayList<>();
        Rule captureRule = Rule.builder().name("capture")
                .then(action((String result) -> captured.add(result)))
                .build();

        RuleFlow<Void> flow = RuleFlow.builder()
                .name("bindToOuter")
                .scope("outer")
                    .scope("inner")
                        .bindTo("outer", "result", "from-inner")
                        .run(captureRule)
                    .endScope()
                .endScope()
                .<Void>returning(function((RuleContext ctx) -> null))
                .build();

        flow.run();
        assertEquals(List.of("from-inner"), captured);
    }

    // =========================================================================
    // execute(Action) — void side-effect command
    // =========================================================================

    @Test
    public void testExecute_actionRunsAndProducesNoResult() {
        List<String> log = new ArrayList<>();

        RuleFlow<Void> flow = RuleFlow.builder()
                .name("executeBasic")
                .execute(action((String name) -> log.add("hello " + name)))
                .bind(name -> "world")
                .<Void>returning(function((RuleContext ctx) -> null))
                .build();

        flow.run(name -> "world");
        assertEquals(List.of("hello world"), log);
    }

    @Test
    public void testExecute_multipleActionsRunInOrder() {
        List<String> log = new ArrayList<>();

        RuleFlow<Void> flow = RuleFlow.builder()
                .name("executeOrdered")
                .execute(action(() -> log.add("first")))
                .execute(action(() -> log.add("second")))
                .execute(action(() -> log.add("third")))
                .<Void>returning(function((RuleContext ctx) -> null))
                .build();

        flow.run();
        assertEquals(List.of("first", "second", "third"), log);
    }

    @Test
    public void testExecute_canReadFlowScopeBindings() {
        List<String> captured = new ArrayList<>();

        RuleFlow<Void> flow = RuleFlow.builder()
                .name("executeReadsBindings")
                .bind(label -> "flow-label")
                .execute(action((String label) -> captured.add(label)))
                .<Void>returning(function((RuleContext ctx) -> null))
                .build();

        flow.run();
        assertEquals(List.of("flow-label"), captured);
    }

    @Test
    public void testExecute_onException_catchesAndContinues() {
        List<String> log = new ArrayList<>();

        RuleFlow<Void> flow = RuleFlow.builder()
                .name("executeOnException")
                .execute(action(() -> { throw new UnrulyException("boom"); }))
                    .onException(UnrulyException.class, b -> b.execute(action(() -> log.add("handled"))))
                .execute(action(() -> log.add("after")))
                .<Void>returning(function((RuleContext ctx) -> null))
                .build();

        flow.run();
        assertEquals(List.of("handled", "after"), log);
    }

    @Test
    public void testExecute_mixedWithRunAndApply() {
        List<String> log = new ArrayList<>();
        Rule runRule = Rule.builder().name("runRule")
                .then(action(() -> log.add("run")))
                .build();

        RuleFlow<String> flow = RuleFlow.builder()
                .name("executeMixed")
                .execute(action(() -> log.add("execute")))
                .run(runRule)
                .<String>returning(function((RuleContext ctx) -> String.join(",", log)))
                .build();

        String result = flow.run();
        assertEquals(List.of("execute", "run"), log);
        assertEquals("execute,run", result);
    }
}
