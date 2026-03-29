package org.rulii.test.script.java;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.bind.Bindings;
import org.rulii.context.RuleContext;
import org.rulii.model.action.Action;
import org.rulii.script.BuildScriptException;
import org.rulii.script.Script;
import org.rulii.script.janino.JaninoScriptProcessorFactory;

public class ScriptActionTest {

    private RuleContext contextWith(Bindings bindings) {
        return RuleContext.builder()
                .with(bindings)
                .build();
    }

    public ScriptActionTest() {
        super();
    }

    @Test
    public void testActionRunsWithoutException() {
        RuleContext ctx = contextWith(Bindings.builder().standard());
        Action action = Action.builder().build(Script.builder().build(JaninoScriptProcessorFactory.LANGUAGE_NAME, "int x = 1;"));
        Assertions.assertDoesNotThrow(() -> action.run(ctx));
    }

    @Test
    public void testActionMutatesBinding() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("counter", int.class, 0);
        RuleContext ctx = contextWith(bindings);
        Action action = Action.builder().build(
                Script.builder().build(JaninoScriptProcessorFactory.LANGUAGE_NAME, "ctx.counter = ctx.counter + 1;"));
        action.run(ctx);
        Assertions.assertEquals(1, ((Number) bindings.getValue("counter")).intValue());
    }

    @Test
    public void testActionRunMultipleTimes() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("counter", int.class, 0);
        RuleContext ctx = contextWith(bindings);
        Action action = Action.builder().build(
                Script.builder().build(JaninoScriptProcessorFactory.LANGUAGE_NAME, "ctx.counter = ctx.counter + 1;"));
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
                Script.builder().build(JaninoScriptProcessorFactory.LANGUAGE_NAME,
                        "ctx.fullName = ctx.firstName + ' ' + ctx.lastName;"));
        action.run(ctx);
        Assertions.assertEquals("John Doe", bindings.getValue("fullName"));
    }

    @Test
    public void testActionConditionalUpdate() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("score",  Integer.class,    85);
        bindings.bind("grade",  Character.class, ' ');
        RuleContext ctx = contextWith(bindings);
        Action action = Action.builder().build(
                Script.builder().build(JaninoScriptProcessorFactory.LANGUAGE_NAME,
                        "ctx.grade = ctx.score >= 90 ? 'A' : ctx.score >= 80 ? 'B' : 'C';"));
        action.run(ctx);
        Assertions.assertEquals('B', (Character) bindings.getValue("grade"));
    }

    @Test
    public void testActionDoesNotRequireReturn() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("processed", boolean.class, false);
        RuleContext ctx = contextWith(bindings);
        // Script has a side effect but no explicit return — Action should handle this fine
        Action action = Action.builder().build(
                Script.builder().build(JaninoScriptProcessorFactory.LANGUAGE_NAME, "ctx.processed = true;"));
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
            Action action = Action.builder().build(Script.builder().build(JaninoScriptProcessorFactory.LANGUAGE_NAME, "@@@ bad @@@"));
            action.run(ctx);
        });
    }

    @Test
    public void testActionNullScriptThrows() {
        Assertions.assertThrows(Exception.class, () -> Action.builder().build((Script<?>) null));
    }
}
