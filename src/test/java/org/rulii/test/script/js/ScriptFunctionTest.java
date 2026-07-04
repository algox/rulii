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
import org.rulii.model.function.Function;
import org.rulii.script.BuildScriptException;
import org.rulii.script.EvaluationException;
import org.rulii.script.Script;
import org.rulii.script.graaljs.GraalJsScriptProcessorFactory;

/**
 * Tests for Function built from a Script via Function.builder().build(Script).
 */
public class ScriptFunctionTest {

    private RuleContext contextWith(Bindings bindings) {
        return RuleContext.builder()
                .with(bindings)
                .build();
    }

    // -----------------------------------------------------------------------
    // Basic return values
    // -----------------------------------------------------------------------

    @Test
    public void testFunctionReturnsInteger() {
        RuleContext ctx = contextWith(Bindings.builder().standard());
        Function<Object> fn = Function.builder().build(
                Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME, "21 * 2"));
        Object result = fn.apply(ctx);
        Assertions.assertEquals(42, ((Number) result).intValue());
    }

    @Test
    public void testFunctionReturnsString() {
        RuleContext ctx = contextWith(Bindings.builder().standard());
        Function<Object> fn = Function.builder().build(
                Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME, "'hello from fn'"));
        Assertions.assertEquals("hello from fn", fn.apply(ctx));
    }

    @Test
    public void testFunctionReturnsBoolean() {
        RuleContext ctx = contextWith(Bindings.builder().standard());
        Function<Object> fn = Function.builder().build(
                Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME, "10 > 5"));
        Assertions.assertEquals(Boolean.TRUE, fn.apply(ctx));
    }

    // -----------------------------------------------------------------------
    // Bindings-driven functions
    // -----------------------------------------------------------------------

    @Test
    public void testFunctionComputesFromBinding() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("radius", double.class, 5.0);
        RuleContext ctx = contextWith(bindings);
        Function<Object> fn = Function.builder().build(
                Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME, "Math.PI * ctx.radius * ctx.radius"));
        double area = ((Number) fn.apply(ctx)).doubleValue();
        Assertions.assertEquals(Math.PI * 25, area, 0.001);
    }

    @Test
    public void testFunctionSumsTwoBindings() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("a", int.class, 13);
        bindings.bind("b", int.class, 29);
        RuleContext ctx = contextWith(bindings);
        Function<Object> fn = Function.builder().build(
                Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME, "ctx.a + ctx.b"));
        Assertions.assertEquals(42, ((Number) fn.apply(ctx)).intValue());
    }

    @Test
    public void testFunctionCannotMutateBinding() {
        // Function.couldChangeState() is false, so - like Condition - its RuleContext argument
        // is converted to an immutable snapshot before invocation; a script attempting to write
        // through it now fails, rather than silently succeeding.
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("x",   int.class, 5);
        bindings.bind("out", int.class, 0);
        RuleContext ctx = contextWith(bindings);
        Function<Object> fn = Function.builder().build(
                Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME, "ctx.out = ctx.x * ctx.x; ctx.out"));
        Assertions.assertThrows(EvaluationException.class, () -> fn.apply(ctx));
        Assertions.assertEquals(0, ((Number) bindings.getValue("out")).intValue());
    }

    // -----------------------------------------------------------------------
    // Repeated invocations
    // -----------------------------------------------------------------------

    @Test
    public void testFunctionInvokedMultipleTimesReturnsConsistentResult() {
        RuleContext ctx = contextWith(Bindings.builder().standard());
        Function<Object> fn = Function.builder().build(
                Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME, "2 + 2"));
        for (int i = 0; i < 5; i++) {
            Assertions.assertEquals(4, ((Number) fn.apply(ctx)).intValue());
        }
    }

    @Test
    public void testFunctionResultChangesWithBindingValue() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("factor", int.class, 3);
        RuleContext ctx = contextWith(bindings);
        Function<Object> fn = Function.builder().build(
                Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME, "ctx.factor * 10"));

        Assertions.assertEquals(30, ((Number) fn.apply(ctx)).intValue());

        bindings.setValue("factor", 7);
        Assertions.assertEquals(70, ((Number) fn.apply(ctx)).intValue());
    }

    // -----------------------------------------------------------------------
    // Error cases
    // -----------------------------------------------------------------------

    @Test
    public void testFunctionWithInvalidScriptThrows() {
        RuleContext ctx = contextWith(Bindings.builder().standard());
        Assertions.assertThrows(BuildScriptException.class, () -> {
            Function<Object> fn = Function.builder().build(
                    Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME, "@@@ bad @@@"));
            fn.apply(ctx);
        });
    }
}
