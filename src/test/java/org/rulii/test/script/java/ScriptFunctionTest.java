package org.rulii.test.script.java;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.bind.Bindings;
import org.rulii.context.RuleContext;
import org.rulii.model.UnrulyException;
import org.rulii.model.function.Function;
import org.rulii.script.BuildScriptException;
import org.rulii.script.Script;
import org.rulii.script.janino.JaninoScriptProcessorFactory;

/**
 * Tests for Function built from a Janino Script.
 *
 * Function.builder().build(Script) sets returnType=Object.class, so the
 * auto-return heuristic applies — bare expressions are auto-wrapped in return.
 */
public class ScriptFunctionTest {

    private static final String LANG = JaninoScriptProcessorFactory.LANGUAGE_NAME;

    private RuleContext contextWith(Bindings bindings) {
        return RuleContext.builder().with(bindings).build();
    }

    // -----------------------------------------------------------------------
    // Literal return values (auto-return)
    // -----------------------------------------------------------------------

    @Test
    public void testFunctionReturnsIntLiteral() {
        RuleContext ctx = contextWith(Bindings.builder().standard());
        Function<Object> fn = Function.builder().build(Script.builder().build(LANG, "42"));
        Assertions.assertEquals(42, ((Number) fn.apply(ctx)).intValue());
    }

    @Test
    public void testFunctionReturnsDoubleLiteral() {
        RuleContext ctx = contextWith(Bindings.builder().standard());
        Function<Object> fn = Function.builder().build(Script.builder().build(LANG, "3.14"));
        Assertions.assertEquals(3.14, ((Number) fn.apply(ctx)).doubleValue(), 0.001);
    }

    @Test
    public void testFunctionReturnsBooleanTrue() {
        RuleContext ctx = contextWith(Bindings.builder().standard());
        Function<Object> fn = Function.builder().build(Script.builder().build(LANG, "10 > 5"));
        Assertions.assertEquals(Boolean.TRUE, fn.apply(ctx));
    }

    @Test
    public void testFunctionReturnsBooleanFalse() {
        RuleContext ctx = contextWith(Bindings.builder().standard());
        Function<Object> fn = Function.builder().build(Script.builder().build(LANG, "10 < 5"));
        Assertions.assertEquals(Boolean.FALSE, fn.apply(ctx));
    }

    @Test
    public void testFunctionReturnsStringLiteral() {
        RuleContext ctx = contextWith(Bindings.builder().standard());
        Function<Object> fn = Function.builder().build(Script.builder().build(LANG, "\"hello from fn\""));
        Assertions.assertEquals("hello from fn", fn.apply(ctx));
    }

    // -----------------------------------------------------------------------
    // Bindings-driven computations
    // -----------------------------------------------------------------------

    @Test
    public void testFunctionComputesFromBinding() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("radius", double.class, 5.0);
        RuleContext ctx = contextWith(bindings);
        Function<Object> fn = Function.builder().build(
                Script.builder().build(LANG, "Math.PI * ctx.radius * ctx.radius"));
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
                Script.builder().build(LANG, "ctx.a + ctx.b"));
        Assertions.assertEquals(42, ((Number) fn.apply(ctx)).intValue());
    }

    @Test
    public void testFunctionWithStringBinding() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("name", String.class, "world");
        RuleContext ctx = contextWith(bindings);
        Function<Object> fn = Function.builder().build(
                Script.builder().build(LANG, "\"hello \" + ctx.name"));
        Assertions.assertEquals("hello world", fn.apply(ctx));
    }

    // -----------------------------------------------------------------------
    // Multi-statement scripts
    // -----------------------------------------------------------------------

    @Test
    public void testFunctionMultiStatementWithExplicitReturn() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("x", int.class, 7);
        RuleContext ctx = contextWith(bindings);
        Function<Object> fn = Function.builder().build(
                Script.builder().build(LANG, "int square = ctx.x * ctx.x;\nreturn square - 1;"));
        Assertions.assertEquals(48, ((Number) fn.apply(ctx)).intValue());
    }

    @Test
    public void testFunctionMultiStatementBareLastExpression() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("x", int.class, 5);
        bindings.bind("out", int.class, 0);
        RuleContext ctx = contextWith(bindings);
        Function<Object> fn = Function.builder().build(
                Script.builder().build(LANG, "ctx.out = ctx.x * ctx.x;\nctx.out;"));
        Assertions.assertEquals(25, ((Number) fn.apply(ctx)).intValue());
        Assertions.assertEquals(25, ((Number) bindings.getValue("out")).intValue());
    }

    @Test
    public void testFunctionWithConditionalLogic() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("score", int.class, 85);
        RuleContext ctx = contextWith(bindings);
        Function<Object> fn = Function.builder().build(
                Script.builder().build(LANG,
                        "ctx.score >= 90 ? \"A\" : ctx.score >= 80 ? \"B\" : \"C\""));
        Assertions.assertEquals("B", fn.apply(ctx));
    }

    // -----------------------------------------------------------------------
    // Java standard library
    // -----------------------------------------------------------------------

    @Test
    public void testFunctionUsesJavaStdlibMath() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("value", double.class, -9.0);
        RuleContext ctx = contextWith(bindings);
        Function<Object> fn = Function.builder().build(
                Script.builder().build(LANG, "Math.sqrt(Math.abs(ctx.value))"));
        Assertions.assertEquals(3.0, ((Number) fn.apply(ctx)).doubleValue(), 0.001);
    }

    @Test
    public void testFunctionUsesStringMethods() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("input", String.class, "  hello  ");
        RuleContext ctx = contextWith(bindings);
        Function<Object> fn = Function.builder().build(
                Script.builder().build(LANG, "ctx.input.trim().toUpperCase()"));
        Assertions.assertEquals("HELLO", fn.apply(ctx));
    }

    // -----------------------------------------------------------------------
    // Repeated invocations — JIT compiled once, reused
    // -----------------------------------------------------------------------

    @Test
    public void testFunctionInvokedMultipleTimesReturnsConsistentResult() {
        RuleContext ctx = contextWith(Bindings.builder().standard());
        Function<Object> fn = Function.builder().build(Script.builder().build(LANG, "2 + 2"));
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
                Script.builder().build(LANG, "ctx.factor * 10"));

        Assertions.assertEquals(30, ((Number) fn.apply(ctx)).intValue());

        bindings.setValue("factor", 7);
        Assertions.assertEquals(70, ((Number) fn.apply(ctx)).intValue());
    }

    @Test
    public void testSameScriptReusedAcrossDifferentContexts() {
        Script<Object> script = Script.builder().build(LANG, "ctx.v * 2");

        Bindings b1 = Bindings.builder().standard();
        b1.bind("v", int.class, 3);
        Object r1 = Function.builder().build(script).apply(contextWith(b1));

        // Note: JITScript caches the evaluator from the first context; v must exist in both
        Bindings b2 = Bindings.builder().standard();
        b2.bind("v", int.class, 7);
        Object r2 = Function.builder().build(script).apply(contextWith(b2));

        Assertions.assertEquals(6,  ((Number) r1).intValue());
        Assertions.assertEquals(14, ((Number) r2).intValue());
    }

    // -----------------------------------------------------------------------
    // Error paths
    // -----------------------------------------------------------------------

    @Test
    public void testFunctionWithSyntaxErrorThrows() {
        RuleContext ctx = contextWith(Bindings.builder().standard());
        Assertions.assertThrows(BuildScriptException.class, () -> {
            Function<Object> fn = Function.builder().build(
                    Script.builder().build(LANG, "@@@ bad @@@"));
            fn.apply(ctx);
        });
    }

    @Test
    public void testFunctionUnknownBindingThrows() {
        RuleContext ctx = contextWith(Bindings.builder().standard());
        Assertions.assertThrows(UnrulyException.class, () -> {
            Function<Object> fn = Function.builder().build(
                    Script.builder().build(LANG, "ctx.ghost + 1"));
            fn.apply(ctx);
        });
    }
}
