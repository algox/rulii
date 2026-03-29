package org.rulii.test.script.java;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.bind.Bindings;
import org.rulii.context.RuleContext;
import org.rulii.model.UnrulyException;
import org.rulii.script.BuildScriptException;
import org.rulii.script.EvaluationException;
import org.rulii.script.Script;
import org.rulii.script.janino.JaninoScriptProcessorFactory;

/**
 * Integration tests for Janino Script.run() — exercises bindings access,
 * multi-statement scripts, Java stdlib, caching, and error paths.
 *
 * Scripts here use the default void return type and assert via side-effects
 * on bindings.  Bare-expression auto-return is tested in ScriptConditionTest
 * and ScriptFunctionTest where Boolean / Object return types are set.
 */
public class ScriptEvaluationTest {

    private static final String LANG = JaninoScriptProcessorFactory.LANGUAGE_NAME;

    private RuleContext contextWith(Bindings bindings) {
        return RuleContext.builder().with(bindings).build();
    }

    // -----------------------------------------------------------------------
    // Writing back to bindings
    // -----------------------------------------------------------------------

    @Test
    public void testWriteIntToBinding() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("result", int.class, 0);
        Script.builder().build(LANG, "ctx.result = 42;").run(contextWith(bindings));
        Assertions.assertEquals(42, ((Number) bindings.getValue("result")).intValue());
    }

    @Test
    public void testWriteStringToBinding() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("msg", String.class, "");
        Script.builder().build(LANG, "ctx.msg = \"hello\";").run(contextWith(bindings));
        Assertions.assertEquals("hello", bindings.getValue("msg"));
    }

    @Test
    public void testWriteDoubleToBinding() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("pi", double.class, 0.0);
        Script.builder().build(LANG, "ctx.pi = 3.14;").run(contextWith(bindings));
        Assertions.assertEquals(3.14, ((Number) bindings.getValue("pi")).doubleValue(), 0.001);
    }

    @Test
    public void testWriteBooleanToBinding() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("flag", boolean.class, false);
        Script.builder().build(LANG, "ctx.flag = true;").run(contextWith(bindings));
        Assertions.assertEquals(Boolean.TRUE, bindings.getValue("flag"));
    }

    // -----------------------------------------------------------------------
    // Reading and computing from bindings
    // -----------------------------------------------------------------------

    @Test
    public void testReadAndComputeSum() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("a", int.class, 13);
        bindings.bind("b", int.class, 29);
        bindings.bind("sum", int.class, 0);
        Script.builder().build(LANG, "ctx.sum = ctx.a + ctx.b;").run(contextWith(bindings));
        Assertions.assertEquals(42, ((Number) bindings.getValue("sum")).intValue());
    }

    @Test
    public void testReadAndComputeProduct() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("price", double.class, 9.99);
        bindings.bind("qty", int.class, 3);
        bindings.bind("total", double.class, 0.0);
        Script.builder().build(LANG, "ctx.total = ctx.price * ctx.qty;").run(contextWith(bindings));
        Assertions.assertEquals(29.97, ((Number) bindings.getValue("total")).doubleValue(), 0.001);
    }

    @Test
    public void testStringConcatenation() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("firstName", String.class, "John");
        bindings.bind("lastName", String.class, "Doe");
        bindings.bind("fullName", String.class, "");
        Script.builder().build(LANG, "ctx.fullName = ctx.firstName + \" \" + ctx.lastName;")
                .run(contextWith(bindings));
        Assertions.assertEquals("John Doe", bindings.getValue("fullName"));
    }

    // -----------------------------------------------------------------------
    // Multi-statement scripts
    // -----------------------------------------------------------------------

    @Test
    public void testMultiStatementWithLocalVariable() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("x", int.class, 6);
        bindings.bind("out", int.class, 0);
        Script.builder().build(LANG, "int temp = ctx.x * ctx.x;\nctx.out = temp - 1;")
                .run(contextWith(bindings));
        Assertions.assertEquals(35, ((Number) bindings.getValue("out")).intValue());
    }

    @Test
    public void testConditionalAssignmentAdultPath() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("age", int.class, 20);
        bindings.bind("category", String.class, "");
        // Ternary covers both branches without requiring if/else compound statements
        Script.builder().build(LANG,
                "ctx.category = ctx.age >= 18 ? \"adult\" : \"minor\";")
                .run(contextWith(bindings));
        Assertions.assertEquals("adult", bindings.getValue("category"));
    }

    @Test
    public void testConditionalAssignmentMinorPath() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("age", int.class, 15);
        bindings.bind("category", String.class, "");
        Script.builder().build(LANG,
                "ctx.category = ctx.age >= 18 ? \"adult\" : \"minor\";")
                .run(contextWith(bindings));
        Assertions.assertEquals("minor", bindings.getValue("category"));
    }

    @Test
    public void testArithmeticAcrossMultipleStatements() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("base", int.class, 5);
        bindings.bind("result", int.class, 0);
        // Computes base^2 + base using local intermediate variable
        Script.builder().build(LANG,
                "int square = ctx.base * ctx.base;\nctx.result = square + ctx.base;")
                .run(contextWith(bindings));
        Assertions.assertEquals(30, ((Number) bindings.getValue("result")).intValue());
    }

    @Test
    public void testTernaryOperatorAssignment() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("score", int.class, 85);
        bindings.bind("grade", String.class, "");
        Script.builder().build(LANG,
                "ctx.grade = ctx.score >= 90 ? \"A\" : ctx.score >= 80 ? \"B\" : \"C\";")
                .run(contextWith(bindings));
        Assertions.assertEquals("B", bindings.getValue("grade"));
    }

    // -----------------------------------------------------------------------
    // Java standard library access
    // -----------------------------------------------------------------------

    @Test
    public void testJavaStdlibMathAbs() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("result", int.class, 0);
        Script.builder().build(LANG, "ctx.result = Math.abs(-99);").run(contextWith(bindings));
        Assertions.assertEquals(99, ((Number) bindings.getValue("result")).intValue());
    }

    @Test
    public void testJavaStdlibMathMax() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("a", int.class, 7);
        bindings.bind("b", int.class, 13);
        bindings.bind("result", int.class, 0);
        Script.builder().build(LANG, "ctx.result = Math.max(ctx.a, ctx.b);").run(contextWith(bindings));
        Assertions.assertEquals(13, ((Number) bindings.getValue("result")).intValue());
    }

    @Test
    public void testJavaStdlibStringMethod() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("input", String.class, "  hello  ");
        bindings.bind("output", String.class, "");
        Script.builder().build(LANG, "ctx.output = ctx.input.trim().toUpperCase();")
                .run(contextWith(bindings));
        Assertions.assertEquals("HELLO", bindings.getValue("output"));
    }

    // -----------------------------------------------------------------------
    // Script caching — same script instance reused across multiple calls
    // -----------------------------------------------------------------------

    @Test
    public void testAccumulateAcrossMultipleRuns() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("counter", int.class, 0);
        RuleContext ctx = contextWith(bindings);
        Script<Object> inc = Script.builder().build(LANG, "ctx.counter = ctx.counter + 1;");
        inc.run(ctx);
        inc.run(ctx);
        inc.run(ctx);
        Assertions.assertEquals(3, ((Number) bindings.getValue("counter")).intValue());
    }

    @Test
    public void testSameScriptReusedAcrossDifferentContexts() {
        Script<Object> script = Script.builder().build(LANG, "ctx.v = ctx.v * 2;");

        Bindings b1 = Bindings.builder().standard();
        b1.bind("v", int.class, 3);
        script.run(contextWith(b1));

        Bindings b2 = Bindings.builder().standard();
        b2.bind("v", int.class, 7);
        script.run(contextWith(b2));

        Assertions.assertEquals(6,  ((Number) b1.getValue("v")).intValue());
        Assertions.assertEquals(14, ((Number) b2.getValue("v")).intValue());
    }

    @Test
    public void testTwoIndependentScripts() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("a", int.class, 10);
        bindings.bind("b", int.class, 3);
        bindings.bind("sum",  int.class, 0);
        bindings.bind("diff", int.class, 0);
        RuleContext ctx = contextWith(bindings);

        Script.builder().build(LANG, "ctx.sum  = ctx.a + ctx.b;").run(ctx);
        Script.builder().build(LANG, "ctx.diff = ctx.a - ctx.b;").run(ctx);

        Assertions.assertEquals(13, ((Number) bindings.getValue("sum")).intValue());
        Assertions.assertEquals(7,  ((Number) bindings.getValue("diff")).intValue());
    }

    // -----------------------------------------------------------------------
    // Error paths
    // -----------------------------------------------------------------------

    @Test
    public void testSyntaxErrorThrowsBuildScriptException() {
        RuleContext ctx = contextWith(Bindings.builder().standard());
        Assertions.assertThrows(BuildScriptException.class, () ->
                Script.builder().build(LANG, "@@@ not java @@@").run(ctx));
    }

    @Test
    public void testUnknownBindingReadThrowsBuildScriptException() {
        Bindings bindings = Bindings.builder().standard();  // no "ghost" binding
        RuleContext ctx = contextWith(bindings);
        // Reading an unknown binding triggers compile-time lookup failure
        Assertions.assertThrows(BuildScriptException.class, () ->
                Script.builder().build(LANG, "ctx.ghost = ctx.ghost + 1;").run(ctx));
    }

    @Test
    public void testRuntimeExceptionThrowsEvaluationException() {
        RuleContext ctx = contextWith(Bindings.builder().standard());
        Assertions.assertThrows(EvaluationException.class, () ->
                Script.builder().build(LANG, "int x = 1 / 0;").run(ctx));
    }

    @Test
    public void testNullPointerAtRuntimeThrowsEvaluationException() {
        RuleContext ctx = contextWith(Bindings.builder().standard());
        Assertions.assertThrows(EvaluationException.class, () ->
                Script.builder().build(LANG, "String s = null;\nint len = s.length();").run(ctx));
    }

    @Test
    public void testReservedBindingsNameAsVariableThrows() {
        RuleContext ctx = contextWith(Bindings.builder().standard());
        // Declaring a local variable named "ctx" must be rejected
        Assertions.assertThrows(BuildScriptException.class, () ->
                Script.builder().build(LANG, "int ctx = 5;").run(ctx));
    }

    @Test
    public void testIncrementOnBindingThrowsBuildScriptException() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("counter", int.class, 0);
        RuleContext ctx = contextWith(bindings);
        Assertions.assertThrows(BuildScriptException.class, () ->
                Script.builder().build(LANG, "ctx.counter++;").run(ctx));
    }

    @Test
    public void testDecrementOnBindingThrowsBuildScriptException() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("counter", int.class, 5);
        RuleContext ctx = contextWith(bindings);
        Assertions.assertThrows(BuildScriptException.class, () ->
                Script.builder().build(LANG, "ctx.counter--;").run(ctx));
    }

    @Test
    public void testUnknownLanguageThrowsUnrulyException() {
        Assertions.assertThrows(UnrulyException.class, () ->
                Script.builder().build("nosuchlang", "1 + 1"));
    }
}
