package org.rulii.test.script.js;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.bind.Bindings;
import org.rulii.context.RuleContext;
import org.rulii.model.function.Function;
import org.rulii.script.BuildScriptException;
import org.rulii.script.Script;

/**
 * Tests for Function built from a Script via Function.builder().build(Script).
 */
public class ScriptFunctionTest {

    private RuleContext contextWith(Bindings bindings) {
        return RuleContext.builder()
                .with(bindings)
                .scriptUsing(TestScriptUtils.createEngine())
                .build();
    }

    // -----------------------------------------------------------------------
    // Basic return values
    // -----------------------------------------------------------------------

    @Test
    public void testFunctionReturnsInteger() {
        RuleContext ctx = contextWith(Bindings.builder().standard());
        Function<Object> fn = Function.builder().build(
                Script.builder().build("ECMAScript", "21 * 2"));
        Object result = fn.apply(ctx);
        Assertions.assertEquals(42, ((Number) result).intValue());
    }

    @Test
    public void testFunctionReturnsString() {
        RuleContext ctx = contextWith(Bindings.builder().standard());
        Function<Object> fn = Function.builder().build(
                Script.builder().build("ECMAScript", "'hello from fn'"));
        Assertions.assertEquals("hello from fn", fn.apply(ctx));
    }

    @Test
    public void testFunctionReturnsBoolean() {
        RuleContext ctx = contextWith(Bindings.builder().standard());
        Function<Object> fn = Function.builder().build(
                Script.builder().build("ECMAScript", "10 > 5"));
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
                Script.builder().build("ECMAScript", "Math.PI * ctx.radius * ctx.radius"));
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
                Script.builder().build("ECMAScript", "ctx.a + ctx.b"));
        Assertions.assertEquals(42, ((Number) fn.apply(ctx)).intValue());
    }

    @Test
    public void testFunctionCanAlsoMutateBinding() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("x",   int.class, 5);
        bindings.bind("out", int.class, 0);
        RuleContext ctx = contextWith(bindings);
        Function<Object> fn = Function.builder().build(
                Script.builder().build("ECMAScript", "ctx.out = ctx.x * ctx.x; ctx.out"));
        Object result = fn.apply(ctx);
        Assertions.assertEquals(25, ((Number) result).intValue());
        Assertions.assertEquals(25, ((Number) bindings.getValue("out")).intValue());
    }

    // -----------------------------------------------------------------------
    // Repeated invocations
    // -----------------------------------------------------------------------

    @Test
    public void testFunctionInvokedMultipleTimesReturnsConsistentResult() {
        RuleContext ctx = contextWith(Bindings.builder().standard());
        Function<Object> fn = Function.builder().build(
                Script.builder().build("ECMAScript", "2 + 2"));
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
                Script.builder().build("ECMAScript", "ctx.factor * 10"));

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
        Function<Object> fn = Function.builder().build(
                Script.builder().build("ECMAScript", "@@@ bad @@@"));
        Assertions.assertThrows(BuildScriptException.class, () -> fn.apply(ctx));
    }
}
