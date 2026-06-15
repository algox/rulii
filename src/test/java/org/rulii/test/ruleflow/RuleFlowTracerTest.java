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
import org.rulii.bind.NamedScope;
import org.rulii.context.RuleContext;
import org.rulii.model.UnrulyException;
import org.rulii.model.action.Action;
import org.rulii.model.function.Function;
import org.rulii.rule.Rule;
import org.rulii.ruleflow.RuleFlow;
import org.rulii.ruleflow.RuleFlowListener;
import org.rulii.ruleflow.command.RuleFlowCommand;
import org.rulii.trace.Tracer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.rulii.model.action.Actions.action;
import static org.rulii.model.condition.Conditions.condition;
import static org.rulii.model.function.Functions.function;

/**
 * Tests verifying that the {@link RuleFlowListener} tracer events fire at the right moments.
 *
 * @author Max Arulananthan
 * @since 2.0
 */
public class RuleFlowTracerTest {

    public RuleFlowTracerTest() {
        super();
    }

    private RuleContext contextWithListener(RuleFlowListener listener) {
        Tracer tracer = Tracer.builder().build();
        tracer.addListener(listener);
        return RuleContext.builder()
                .with(Bindings.builder().standard())
                .traceUsing(tracer)
                .build();
    }

    // =========================================================================
    // onRuleFlowStart / onRuleFlowEnd
    // =========================================================================

    @Test
    public void testStart_firesWithScopeAndFlow() {
        AtomicReference<RuleFlow<?>> capturedFlow = new AtomicReference<>();
        AtomicReference<NamedScope> capturedScope = new AtomicReference<>();

        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("startFlow")
                .bind(x -> 1)
                .build();

        RuleContext ctx = contextWithListener(new RuleFlowListener() {
            @Override
            public void onRuleFlowStart(RuleFlow<?> ruleFlow, NamedScope ruleFlowScope) {
                capturedFlow.set(ruleFlow);
                capturedScope.set(ruleFlowScope);
            }
        });

        flow.run(ctx);

        assertSame(flow, capturedFlow.get());
        assertNotNull(capturedScope.get());
    }

    @Test
    public void testEnd_firesAfterNormalCompletion() {
        AtomicBoolean endFired = new AtomicBoolean(false);

        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("endFlow")
                .bind(x -> 1)
                .build();

        flow.run(contextWithListener(new RuleFlowListener() {
            @Override
            public void onRuleFlowEnd(RuleFlow<?> ruleFlow, NamedScope ruleFlowScope) {
                endFired.set(true);
            }
        }));

        assertTrue(endFired.get());
    }

    @Test
    public void testEnd_firesAfterEarlyExit() {
        AtomicBoolean endFired = new AtomicBoolean(false);

        RuleFlow<String> flow = RuleFlow.builder()
                .name("endExitFlow")
                .bind(result -> "done")
                .<String>exit(function((String result) -> result))
                .build();

        flow.run(contextWithListener(new RuleFlowListener() {
            @Override
            public void onRuleFlowEnd(RuleFlow<?> ruleFlow, NamedScope ruleFlowScope) {
                endFired.set(true);
            }
        }));

        assertTrue(endFired.get());
    }

    @Test
    public void testEnd_firesAfterUnhandledError() {
        AtomicBoolean endFired = new AtomicBoolean(false);

        Rule badRule = Rule.builder().name("bad")
                .then(action(() -> { throw new UnrulyException("boom"); })).build();

        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("endErrorFlow")
                .run(badRule)
                .build();

        RuleContext ctx = contextWithListener(new RuleFlowListener() {
            @Override
            public void onRuleFlowEnd(RuleFlow<?> ruleFlow, NamedScope ruleFlowScope) {
                endFired.set(true);
            }
        });

        assertThrows(UnrulyException.class, () -> flow.run(ctx));
        assertTrue(endFired.get());
    }

    // =========================================================================
    // onRuleFlowCommandExecuted
    // =========================================================================

    @Test
    public void testCommandExecuted_firesForEachNormalCommand() {
        AtomicInteger count = new AtomicInteger(0);

        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("cmdFlow")
                .bind(a -> 1)
                .bind(b -> 2)
                .bind(c -> 3)
                .build();

        flow.run(contextWithListener(new RuleFlowListener() {
            @Override
            public void onRuleFlowCommandExecuted(RuleFlow<?> ruleFlow, RuleFlowCommand command) {
                count.incrementAndGet();
            }
        }));

        assertEquals(3, count.get());
    }

    @Test
    public void testCommandExecuted_notFiredForReturningCommand() {
        List<String> commandTypes = new ArrayList<>();

        RuleFlow<String> flow = RuleFlow.builder()
                .name("exitCmdFlow")
                .bind(result -> "early")
                .<String>exit(function((String result) -> result))
                .build();

        flow.run(contextWithListener(new RuleFlowListener() {
            @Override
            public void onRuleFlowCommandExecuted(RuleFlow<?> ruleFlow, RuleFlowCommand command) {
                commandTypes.add(command.getClass().getSimpleName());
            }
        }));

        // BindCommand fires, ReturningCommand does not (it throws RuleFlowReturn)
        assertEquals(1, commandTypes.size());
        assertEquals("BindCommand", commandTypes.get(0));
    }

    @Test
    public void testCommandExecuted_notFiredForFailedCommand() {
        AtomicInteger count = new AtomicInteger(0);

        Rule badRule = Rule.builder().name("bad")
                .then(action(() -> { throw new UnrulyException("fail"); })).build();

        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("failCmdFlow")
                .run(badRule)
                .build();

        RuleContext ctx = contextWithListener(new RuleFlowListener() {
            @Override
            public void onRuleFlowCommandExecuted(RuleFlow<?> ruleFlow, RuleFlowCommand command) {
                count.incrementAndGet();
            }
        });

        assertThrows(UnrulyException.class, () -> flow.run(ctx));
        assertEquals(0, count.get());
    }

    // =========================================================================
    // onRuleFlowEarlyExit
    // =========================================================================

    @Test
    public void testEarlyExit_firesWithResult() {
        AtomicReference<Object> capturedResult = new AtomicReference<>();

        RuleFlow<String> flow = RuleFlow.builder()
                .name("earlyExitFlow")
                .bind(result -> "exitValue")
                .<String>exit(function((String result) -> result))
                .build();

        flow.run(contextWithListener(new RuleFlowListener() {
            @Override
            public void onRuleFlowEarlyExit(RuleFlow<?> ruleFlow, Object result) {
                capturedResult.set(result);
            }
        }));

        assertEquals("exitValue", capturedResult.get());
    }

    @Test
    public void testEarlyExit_notFiredOnNormalCompletion() {
        AtomicBoolean earlyExitFired = new AtomicBoolean(false);

        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("noExitFlow")
                .bind(x -> 1)
                .build();

        flow.run(contextWithListener(new RuleFlowListener() {
            @Override
            public void onRuleFlowEarlyExit(RuleFlow<?> ruleFlow, Object result) {
                earlyExitFired.set(true);
            }
        }));

        assertFalse(earlyExitFired.get());
    }

    // =========================================================================
    // onRuleFlowResult
    // =========================================================================

    @Test
    public void testResult_firesWhenExtractorSet() {
        AtomicReference<Function<?>> capturedExtractor = new AtomicReference<>();

        RuleFlow<String> flow = RuleFlow.builder()
                .name("resultFlow")
                .bind(greeting -> "hello")
                .<String>returning(function((String greeting) -> greeting.toUpperCase()))
                .build();

        flow.run(contextWithListener(new RuleFlowListener() {
            @Override
            public void onRuleFlowResult(RuleFlow<?> ruleFlow, Function<?> resultExtractor) {
                capturedExtractor.set(resultExtractor);
            }
        }));

        assertNotNull(capturedExtractor.get());
    }

    @Test
    public void testResult_notFiredWhenNoExtractor() {
        AtomicBoolean resultFired = new AtomicBoolean(false);

        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("noResultFlow")
                .bind(x -> 1)
                .build();

        flow.run(contextWithListener(new RuleFlowListener() {
            @Override
            public void onRuleFlowResult(RuleFlow<?> ruleFlow, Function<?> resultExtractor) {
                resultFired.set(true);
            }
        }));

        assertFalse(resultFired.get());
    }

    // =========================================================================
    // onRuleFlowFinalizer
    // =========================================================================

    @Test
    public void testFinalizer_firesAfterFinalizerRuns() {
        AtomicReference<Action> capturedFinalizer = new AtomicReference<>();
        Action finalizerAction = action(() -> {});

        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("finalizerFlow")
                .bind(x -> 1)
                .finalizer(finalizerAction)
                .build();

        flow.run(contextWithListener(new RuleFlowListener() {
            @Override
            public void onRuleFlowFinalizer(RuleFlow<?> ruleFlow, Action finalizer) {
                capturedFinalizer.set(finalizer);
            }
        }));

        assertSame(finalizerAction, capturedFinalizer.get());
    }

    @Test
    public void testFinalizer_firesOnEarlyExit() {
        AtomicBoolean finalizerFired = new AtomicBoolean(false);

        RuleFlow<String> flow = RuleFlow.builder()
                .name("finalizerExitFlow")
                .bind(result -> "done")
                .<String>exit(function((String result) -> result))
                .finalizer(action(() -> {}))
                .build();

        flow.run(contextWithListener(new RuleFlowListener() {
            @Override
            public void onRuleFlowFinalizer(RuleFlow<?> ruleFlow, Action finalizer) {
                finalizerFired.set(true);
            }
        }));

        assertTrue(finalizerFired.get());
    }

    @Test
    public void testFinalizer_notFiredWhenNoFinalizerSet() {
        AtomicBoolean finalizerFired = new AtomicBoolean(false);

        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("noFinalizerFlow")
                .bind(x -> 1)
                .build();

        flow.run(contextWithListener(new RuleFlowListener() {
            @Override
            public void onRuleFlowFinalizer(RuleFlow<?> ruleFlow, Action finalizer) {
                finalizerFired.set(true);
            }
        }));

        assertFalse(finalizerFired.get());
    }

    // =========================================================================
    // onRuleFlowExceptionHandled
    // =========================================================================

    @Test
    public void testExceptionHandled_stepLevelHandler() {
        AtomicBoolean handledFired = new AtomicBoolean(false);
        AtomicBoolean capturedStepLevel = new AtomicBoolean(false);

        Rule badRule = Rule.builder().name("bad")
                .then(action(() -> { throw new UnrulyException("step boom"); })).build();

        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("stepHandlerTracerFlow")
                .run(badRule)
                    .onException(UnrulyException.class, b -> b.bind(handled -> true))
                .build();

        flow.run(contextWithListener(new RuleFlowListener() {
            @Override
            public void onRuleFlowExceptionHandled(RuleFlow<?> ruleFlow, Exception e, boolean stepLevel) {
                handledFired.set(true);
                capturedStepLevel.set(stepLevel);
            }
        }));

        assertTrue(handledFired.get());
        assertTrue(capturedStepLevel.get());
    }

    @Test
    public void testExceptionHandled_globalHandler() {
        AtomicBoolean handledFired = new AtomicBoolean(false);
        AtomicBoolean capturedStepLevel = new AtomicBoolean(true);

        Rule badRule = Rule.builder().name("bad")
                .then(action(() -> { throw new UnrulyException("global boom"); })).build();

        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("globalHandlerTracerFlow")
                .run(badRule)
                    .returning()
                .onException(UnrulyException.class, b -> b.bind(handled -> true))
                .build();

        flow.run(contextWithListener(new RuleFlowListener() {
            @Override
            public void onRuleFlowExceptionHandled(RuleFlow<?> ruleFlow, Exception e, boolean stepLevel) {
                handledFired.set(true);
                capturedStepLevel.set(stepLevel);
            }
        }));

        assertTrue(handledFired.get());
        assertFalse(capturedStepLevel.get());
    }

    // =========================================================================
    // onRuleFlowError
    // =========================================================================

    @Test
    public void testError_firesOnUnhandledException() {
        AtomicReference<Exception> capturedError = new AtomicReference<>();

        Rule badRule = Rule.builder().name("bad")
                .then(action(() -> { throw new UnrulyException("unhandled"); })).build();

        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("errorFlow")
                .run(badRule)
                .build();

        RuleContext ctx = contextWithListener(new RuleFlowListener() {
            @Override
            public void onRuleFlowError(RuleFlow<?> ruleFlow, Exception e) {
                capturedError.set(e);
            }
        });

        assertThrows(UnrulyException.class, () -> flow.run(ctx));
        assertNotNull(capturedError.get());
    }

    @Test
    public void testError_notFiredWhenExceptionIsHandled() {
        AtomicBoolean errorFired = new AtomicBoolean(false);

        Rule badRule = Rule.builder().name("bad")
                .then(action(() -> { throw new UnrulyException("handled"); })).build();

        RuleFlow<RuleContext> flow = RuleFlow.builder()
                .name("handledErrorFlow")
                .run(badRule)
                    .onException(UnrulyException.class, b -> b.bind(ok -> true))
                .build();

        flow.run(contextWithListener(new RuleFlowListener() {
            @Override
            public void onRuleFlowError(RuleFlow<?> ruleFlow, Exception e) {
                errorFired.set(true);
            }
        }));

        assertFalse(errorFired.get());
    }

    // =========================================================================
    // Event ordering
    // =========================================================================

    @Test
    public void testEventOrder_normalCompletion() {
        List<String> events = new ArrayList<>();
        Action finalizerAction = action(() -> {});

        Rule logRule = Rule.builder().name("log")
                .then(action(() -> {})).build();

        RuleFlow<String> flow = RuleFlow.builder()
                .name("orderFlow")
                .run(logRule)
                .<String>returning(function((RuleContext ctx) -> "done"))
                .finalizer(finalizerAction)
                .build();

        flow.run(contextWithListener(new RuleFlowListener() {
            @Override public void onRuleFlowStart(RuleFlow<?> f, NamedScope s)       { events.add("start"); }
            @Override public void onRuleFlowCommandExecuted(RuleFlow<?> f, RuleFlowCommand c) { events.add("cmd"); }
            @Override public void onRuleFlowResult(RuleFlow<?> f, Function<?> e)     { events.add("result"); }
            @Override public void onRuleFlowFinalizer(RuleFlow<?> f, Action a)       { events.add("finalizer"); }
            @Override public void onRuleFlowEnd(RuleFlow<?> f, NamedScope s)         { events.add("end"); }
        }));

        assertEquals(List.of("start", "cmd", "result", "finalizer", "end"), events);
    }

    @Test
    public void testEventOrder_earlyExit() {
        List<String> events = new ArrayList<>();

        RuleFlow<String> flow = RuleFlow.builder()
                .name("exitOrderFlow")
                .bind(result -> "x")
                .<String>exit(function((String result) -> result))
                .finalizer(action(() -> {}))
                .build();

        flow.run(contextWithListener(new RuleFlowListener() {
            @Override public void onRuleFlowStart(RuleFlow<?> f, NamedScope s)        { events.add("start"); }
            @Override public void onRuleFlowCommandExecuted(RuleFlow<?> f, RuleFlowCommand c) { events.add("cmd"); }
            @Override public void onRuleFlowFinalizer(RuleFlow<?> f, Action a)        { events.add("finalizer"); }
            @Override public void onRuleFlowEarlyExit(RuleFlow<?> f, Object r)        { events.add("exit"); }
            @Override public void onRuleFlowEnd(RuleFlow<?> f, NamedScope s)          { events.add("end"); }
        }));

        // BindCommand fires cmd, ReturningCommand throws so exit fires before end;
        // finalizer runs inside runCommands finally (before exit catch in strategy)
        assertEquals(List.of("start", "cmd", "finalizer", "exit", "end"), events);
    }
}
