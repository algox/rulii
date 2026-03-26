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
package org.rulii.test.script.js;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.bind.Bindings;
import org.rulii.context.RuleContext;
import org.rulii.model.action.Action;
import org.rulii.script.BuildScriptException;
import org.rulii.script.Script;
import org.rulii.script.graaljs.GraalJsScriptProcessorFactory;

/**
 * Tests for Action built from a Script via Action.builder().build(Script).
 */
public class ScriptActionTest {

    private RuleContext contextWith(Bindings bindings) {
        return RuleContext.builder()
                .with(bindings)
                .build();
    }

    // -----------------------------------------------------------------------
    // Basic execution
    // -----------------------------------------------------------------------

    @Test
    public void testActionRunsWithoutException() {
        RuleContext ctx = contextWith(Bindings.builder().standard());
        Action action = Action.builder().build(Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME, "var x = 1;"));
        Assertions.assertDoesNotThrow(() -> action.run(ctx));
    }

    @Test
    public void testActionMutatesBinding() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("counter", int.class, 0);
        RuleContext ctx = contextWith(bindings);
        Action action = Action.builder().build(
                Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME, "ctx.counter = ctx.counter + 1;"));
        action.run(ctx);
        Assertions.assertEquals(1, ((Number) bindings.getValue("counter")).intValue());
    }

    @Test
    public void testActionRunMultipleTimes() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("counter", int.class, 0);
        RuleContext ctx = contextWith(bindings);
        Action action = Action.builder().build(
                Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME, "ctx.counter = ctx.counter + 1;"));
        action.run(ctx);
        action.run(ctx);
        action.run(ctx);
        Assertions.assertEquals(3, ((Number) bindings.getValue("counter")).intValue());
    }

    // -----------------------------------------------------------------------
    // Bindings interaction
    // -----------------------------------------------------------------------

    @Test
    public void testActionReadsAndWritesMultipleBindings() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("firstName", String.class, "John");
        bindings.bind("lastName",  String.class, "Doe");
        bindings.bind("fullName",  String.class, "");
        RuleContext ctx = contextWith(bindings);
        Action action = Action.builder().build(
                Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME,
                        "ctx.fullName = ctx.firstName + ' ' + ctx.lastName;"));
        action.run(ctx);
        Assertions.assertEquals("John Doe", bindings.getValue("fullName"));
    }

    @Test
    public void testActionConditionalUpdate() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("score",  int.class,    85);
        bindings.bind("grade",  String.class, "");
        RuleContext ctx = contextWith(bindings);
        Action action = Action.builder().build(
                Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME,
                        "ctx.grade = ctx.score >= 90 ? 'A' : ctx.score >= 80 ? 'B' : 'C';"));
        action.run(ctx);
        Assertions.assertEquals("B", bindings.getValue("grade"));
    }

    @Test
    public void testActionDoesNotRequireReturn() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("processed", boolean.class, false);
        RuleContext ctx = contextWith(bindings);
        // Script has a side effect but no explicit return — Action should handle this fine
        Action action = Action.builder().build(
                Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME, "ctx.processed = true;"));
        action.run(ctx);
        Assertions.assertEquals(Boolean.TRUE, bindings.getValue("processed"));
    }

    // -----------------------------------------------------------------------
    // Error cases
    // -----------------------------------------------------------------------

    @Test
    public void testActionWithInvalidScriptThrows() {
        RuleContext ctx = contextWith(Bindings.builder().standard());
        Assertions.assertThrows(BuildScriptException.class, () -> {
            Action action = Action.builder().build(Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME, "@@@ bad @@@"));
            action.run(ctx);
        });
    }

    @Test
    public void testActionNullScriptThrows() {
        Assertions.assertThrows(Exception.class, () -> Action.builder().build((Script<?>) null));
    }
}
