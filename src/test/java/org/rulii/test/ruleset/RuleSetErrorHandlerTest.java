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
package org.rulii.test.ruleset;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.bind.Bindings;
import org.rulii.context.RuleContext;
import org.rulii.model.UnrulyException;
import org.rulii.model.function.Functions;
import org.rulii.rule.Rule;
import org.rulii.ruleset.RuleSet;
import org.rulii.ruleset.RuleSetExecutionStatus;
import org.rulii.validation.RuleViolations;
import org.rulii.validation.ValidationException;
import org.rulii.validation.rules.notnull.NotNullValidationRule;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.rulii.model.action.Actions.action;
import static org.rulii.model.condition.Conditions.condition;
import static org.rulii.model.function.Functions.function;

/**
 * Unit tests for the ErrorHandler feature on RuleSets.
 *
 * <p>Errors that propagate to the RuleSet error handler are those thrown from lifecycle
 * methods (initializer, finalizer) or from explicitly thrown exceptions (e.g. ValidationException
 * from a validating finalizer). Condition evaluation returning false is a valid rule outcome, not
 * an error — only unchecked exceptions that escape the lifecycle methods trigger the handler.
 */
public class RuleSetErrorHandlerTest {

    // -----------------------------------------------------------------------
    // Default error handler
    // -----------------------------------------------------------------------

    @Test
    public void testDefaultErrorHandlerIsSet() {
        RuleSet<?> ruleSet = RuleSet.builder()
                .with("testDefaultSet")
                .build();

        Assertions.assertNotNull(ruleSet.getErrorHandler());
    }

    @Test
    public void testDefaultErrorHandlerWrapsRuntimeExceptionInUnrulyException() {
        // Initializer throws a raw RuntimeException — default handler wraps it in UnrulyException.
        RuleSet<?> ruleSet = RuleSet.builder()
                .with("testDefaultWrap")
                .initializer(action(() -> { throw new RuntimeException("boom"); }))
                .rule(Rule.builder().build(condition(() -> true)))
                .build();

        RuleContext ctx = RuleContext.builder().build(Bindings.builder().standard());

        UnrulyException ex = Assertions.assertThrows(UnrulyException.class, () -> ruleSet.run(ctx));
        Assertions.assertTrue(ex.getMessage().contains("testDefaultWrap"));
    }

    @Test
    public void testDefaultErrorHandlerRethrowsValidationException() {
        // validating() installs a finalizer that throws ValidationException when violations exist.
        // The default error handler unwraps the root cause and rethrows ValidationException.
        RuleSet<?> ruleSet = RuleSet.builder()
                .with("testValidationRethrow")
                .validating()
                .rule(new NotNullValidationRule("fieldA"))
                .build();

        Bindings bindings = Bindings.builder().standard();
        bindings.bind("ruleViolations", RuleViolations.class, new RuleViolations());
        bindings.bind("fieldA", String.class, null);   // null value → violation fires

        RuleContext ctx = RuleContext.builder().build(bindings);

        Assertions.assertThrows(ValidationException.class, () -> ruleSet.run(ctx));
    }

    // -----------------------------------------------------------------------
    // Custom error handler — invocation
    // -----------------------------------------------------------------------

    @Test
    public void testCustomErrorHandlerIsInvokedOnInitializerFailure() {
        AtomicBoolean handlerCalled = new AtomicBoolean(false);

        RuleSet<?> ruleSet = RuleSet.builder()
                .with("testCustomInvoked")
                .initializer(action(() -> { throw new RuntimeException("init error"); }))
                .rule(Rule.builder().build(condition(() -> true)))
                .errorHandler(function((Exception ex) -> {
                    handlerCalled.set(true);
                    return null;
                }))
                .build();

        RuleContext ctx = RuleContext.builder().build(Bindings.builder().standard());
        ruleSet.run(ctx);

        Assertions.assertTrue(handlerCalled.get());
    }

    @Test
    public void testCustomErrorHandlerReceivesCorrectException() {
        AtomicReference<Exception> captured = new AtomicReference<>();

        RuleSet<?> ruleSet = RuleSet.builder()
                .with("testExceptionCapture")
                .initializer(action(() -> { throw new IllegalStateException("specific error"); }))
                .rule(Rule.builder().build(condition(() -> true)))
                .errorHandler(function((Exception ex) -> {
                    captured.set(ex);
                    return null;
                }))
                .build();

        RuleContext ctx = RuleContext.builder().build(Bindings.builder().standard());
        ruleSet.run(ctx);

        Assertions.assertNotNull(captured.get());
        // Walk to root cause — the original IllegalStateException.
        Throwable cause = captured.get();
        while (cause.getCause() != null) cause = cause.getCause();
        Assertions.assertInstanceOf(IllegalStateException.class, cause);
        Assertions.assertEquals("specific error", cause.getMessage());
    }

    @Test
    public void testCustomErrorHandlerCanReturnRecoveryValue() {
        RuleSet<String> ruleSet = RuleSet.<String>builder()
                .with("testRecovery")
                .initializer(action(() -> { throw new RuntimeException("failure"); }))
                .rule(Rule.builder().build(condition(() -> true)))
                .errorHandler(function((Exception ex) -> "recovered"))
                .build();

        RuleContext ctx = RuleContext.builder().build(Bindings.builder().standard());
        String result = ruleSet.run(ctx);

        Assertions.assertEquals("recovered", result);
    }

    @Test
    public void testCustomErrorHandlerCanSwallowErrorAndReturnNull() {
        RuleSet<?> ruleSet = RuleSet.builder()
                .with("testSwallow")
                .initializer(action(() -> { throw new RuntimeException("swallowed"); }))
                .rule(Rule.builder().build(condition(() -> true)))
                .errorHandler(function((Exception ex) -> null))
                .build();

        RuleContext ctx = RuleContext.builder().build(Bindings.builder().standard());

        Assertions.assertDoesNotThrow(() -> ruleSet.run(ctx));
        Assertions.assertNull(ruleSet.run(ctx));
    }

    @Test
    public void testCustomErrorHandlerCanRethrowDifferentException() {
        // The framework wraps exceptions thrown from within the error handler in UnrulyException.
        // Verify the root cause is the exception the custom handler threw.
        RuleSet<?> ruleSet = RuleSet.builder()
                .with("testRethrow")
                .initializer(action(() -> { throw new RuntimeException("original"); }))
                .rule(Rule.builder().build(condition(() -> true)))
                .errorHandler(function((Exception ex) -> {
                    throw new UnsupportedOperationException("custom rethrow");
                }))
                .build();

        RuleContext ctx = RuleContext.builder().build(Bindings.builder().standard());

        UnrulyException thrown = Assertions.assertThrows(UnrulyException.class, () -> ruleSet.run(ctx));
        Throwable cause = thrown;
        while (cause.getCause() != null) cause = cause.getCause();
        Assertions.assertInstanceOf(UnsupportedOperationException.class, cause);
        Assertions.assertEquals("custom rethrow", cause.getMessage());
    }

    // -----------------------------------------------------------------------
    // Exception accessible via the "ex" binding
    // -----------------------------------------------------------------------

    @Test
    public void testExceptionIsAccessibleViaExBinding() {
        AtomicReference<Object> exFromBindings = new AtomicReference<>();

        RuleSet<?> ruleSet = RuleSet.builder()
                .with("testExBinding")
                .initializer(action(() -> { throw new RuntimeException("binding-check"); }))
                .rule(Rule.builder().build(condition(() -> true)))
                .errorHandler(Functions.function((RuleContext ruleContext, Exception ex) -> {
                    // The exception is also reachable via the "ex" binding.
                    exFromBindings.set(ruleContext.getBindings().getValue("ex"));
                    return null;
                }))
                .build();

        RuleContext ctx = RuleContext.builder().build(Bindings.builder().standard());
        ruleSet.run(ctx);

        Assertions.assertNotNull(exFromBindings.get());
        Assertions.assertInstanceOf(Exception.class, exFromBindings.get());
    }

    // -----------------------------------------------------------------------
    // Error handler receives RuleSet and RuleSetExecutionStatus
    // -----------------------------------------------------------------------

    @Test
    public void testCustomErrorHandlerReceivesRuleSetReference() {
        AtomicReference<String> capturedName = new AtomicReference<>();

        RuleSet<?> ruleSet = RuleSet.builder()
                .with("testRuleSetRef")
                .initializer(action(() -> { throw new RuntimeException("error"); }))
                .rule(Rule.builder().build(condition(() -> true)))
                .errorHandler(Functions.function((RuleSet<?> ruleSet1, Exception ex) -> {
                    capturedName.set(ruleSet1.getName());
                    return null;
                }))
                .build();

        RuleContext ctx = RuleContext.builder().build(Bindings.builder().standard());
        ruleSet.run(ctx);

        Assertions.assertEquals("testRuleSetRef", capturedName.get());
    }

    @Test
    public void testCustomErrorHandlerReceivesRuleSetStatus() {
        AtomicReference<RuleSetExecutionStatus> capturedStatus = new AtomicReference<>();

        // Use a finalizer so the two rules both execute first, then the error fires.
        RuleSet<?> ruleSet = RuleSet.builder()
                .with("testStatus")
                .rule(Rule.builder().build(condition(() -> true)))
                .rule(Rule.builder().build(condition(() -> true)))
                .finalizer(action(() -> { throw new RuntimeException("finalizer error"); }))
                .errorHandler(Functions.function((RuleSetExecutionStatus ruleSetStatus, Exception ex) -> {
                    capturedStatus.set(ruleSetStatus);
                    return null;
                }))
                .build();

        RuleContext ctx = RuleContext.builder().build(Bindings.builder().standard());
        ruleSet.run(ctx);

        Assertions.assertNotNull(capturedStatus.get());
        // Both rules ran before the finalizer threw, so 2 results are recorded.
        Assertions.assertEquals(2, capturedStatus.get().size());
    }

    // -----------------------------------------------------------------------
    // Error from lifecycle methods (initializer / finalizer)
    // -----------------------------------------------------------------------

    @Test
    public void testCustomErrorHandlerIsInvokedWhenInitializerThrows() {
        AtomicBoolean handlerCalled = new AtomicBoolean(false);

        RuleSet<?> ruleSet = RuleSet.builder()
                .with("testInitializerError")
                .initializer(action(() -> { throw new RuntimeException("initializer failed"); }))
                .rule(Rule.builder().build(condition(() -> true)))
                .errorHandler(function((Exception ex) -> {
                    handlerCalled.set(true);
                    return null;
                }))
                .build();

        RuleContext ctx = RuleContext.builder().build(Bindings.builder().standard());
        ruleSet.run(ctx);

        Assertions.assertTrue(handlerCalled.get());
    }

    @Test
    public void testCustomErrorHandlerIsInvokedWhenFinalizerThrows() {
        AtomicBoolean handlerCalled = new AtomicBoolean(false);

        RuleSet<?> ruleSet = RuleSet.builder()
                .with("testFinalizerError")
                .rule(Rule.builder().build(condition(() -> true)))
                .finalizer(action(() -> { throw new RuntimeException("finalizer failed"); }))
                .errorHandler(function((Exception ex) -> {
                    handlerCalled.set(true);
                    return null;
                }))
                .build();

        RuleContext ctx = RuleContext.builder().build(Bindings.builder().standard());
        ruleSet.run(ctx);

        Assertions.assertTrue(handlerCalled.get());
    }

    // -----------------------------------------------------------------------
    // Error handler not invoked on clean execution
    // -----------------------------------------------------------------------

    @Test
    public void testErrorHandlerNotCalledOnSuccessfulExecution() {
        AtomicBoolean handlerCalled = new AtomicBoolean(false);

        RuleSet<?> ruleSet = RuleSet.builder()
                .with("testNoError")
                .rule(Rule.builder().build(condition(() -> true)))
                .errorHandler(function((Exception ex) -> {
                    handlerCalled.set(true);
                    return null;
                }))
                .build();

        RuleContext ctx = RuleContext.builder().build(Bindings.builder().standard());
        ruleSet.run(ctx);

        Assertions.assertFalse(handlerCalled.get());
    }

    // -----------------------------------------------------------------------
    // Builder validation
    // -----------------------------------------------------------------------

    @Test
    public void testSetErrorHandlerNullThrows() {
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                RuleSet.builder()
                        .with("testNullHandler")
                        .errorHandler(null)
        );
    }

    @Test
    public void testCustomErrorHandlerReplacesDefault() {
        // Default handler would throw UnrulyException; custom handler swallows the error.
        AtomicBoolean customCalled = new AtomicBoolean(false);

        RuleSet<?> ruleSet = RuleSet.builder()
                .with("testReplace")
                .initializer(action(() -> { throw new RuntimeException("test"); }))
                .rule(Rule.builder().build(condition(() -> true)))
                .errorHandler(function((Exception ex) -> {
                    customCalled.set(true);
                    return null;
                }))
                .build();

        RuleContext ctx = RuleContext.builder().build(Bindings.builder().standard());

        // Default would throw; custom swallows — assertDoesNotThrow proves custom replaced default.
        Assertions.assertDoesNotThrow(() -> ruleSet.run(ctx));
        Assertions.assertTrue(customCalled.get());
    }

    // -----------------------------------------------------------------------
    // Multiple invocations — handler called each time
    // -----------------------------------------------------------------------

    @Test
    public void testErrorHandlerInvokedOnEveryRunInvocation() {
        AtomicReference<Integer> callCount = new AtomicReference<>(0);

        RuleSet<?> ruleSet = RuleSet.builder()
                .with("testMultiRun")
                .initializer(action(() -> { throw new RuntimeException("error"); }))
                .rule(Rule.builder().build(condition(() -> true)))
                .errorHandler(function((Exception ex) -> {
                    callCount.updateAndGet(n -> n + 1);
                    return null;
                }))
                .build();

        RuleContext ctx1 = RuleContext.builder().build(Bindings.builder().standard());
        RuleContext ctx2 = RuleContext.builder().build(Bindings.builder().standard());

        ruleSet.run(ctx1);
        ruleSet.run(ctx2);

        Assertions.assertEquals(2, callCount.get());
    }
}
