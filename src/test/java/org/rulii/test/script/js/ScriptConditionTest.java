package org.rulii.test.script.js;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.bind.Bindings;
import org.rulii.context.RuleContext;
import org.rulii.model.UnrulyException;
import org.rulii.model.condition.Condition;
import org.rulii.script.Script;

/**
 * Tests for Condition built from a Script via Condition.builder().build(Script).
 */
public class ScriptConditionTest {

    private RuleContext contextWith(Bindings bindings) {
        return RuleContext.builder()
                .with(bindings)
                .scriptUsing(TestScriptUtils.createEngine())
                .build();
    }

    // -----------------------------------------------------------------------
    // Basic true / false
    // -----------------------------------------------------------------------

    @Test
    public void testConditionReturnsTrue() {
        RuleContext ctx = contextWith(Bindings.builder().standard());
        Condition condition = Condition.builder().build(Script.builder().build("ECMAScript", "true"));
        Assertions.assertTrue(condition.isTrue(ctx));
    }

    @Test
    public void testConditionReturnsFalse() {
        RuleContext ctx = contextWith(Bindings.builder().standard());
        Condition condition = Condition.builder().build(Script.builder().build("ECMAScript", "false"));
        Assertions.assertFalse(condition.isTrue(ctx));
    }

    // -----------------------------------------------------------------------
    // Bindings-driven conditions
    // -----------------------------------------------------------------------

    @Test
    public void testConditionReadsBinding() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("age", int.class, 20);
        RuleContext ctx = contextWith(bindings);
        Condition condition = Condition.builder().build(
                Script.builder().build("ECMAScript", "ctx.age >= 18"));
        Assertions.assertTrue(condition.isTrue(ctx));
    }

    @Test
    public void testConditionFailsBinding() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("age", int.class, 15);
        RuleContext ctx = contextWith(bindings);
        Condition condition = Condition.builder().build(
                Script.builder().build("ECMAScript", "ctx.age >= 18"));
        Assertions.assertFalse(condition.isTrue(ctx));
    }

    @Test
    public void testConditionWithStringBinding() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("role", String.class, "admin");
        RuleContext ctx = contextWith(bindings);
        Condition condition = Condition.builder().build(
                Script.builder().build("ECMAScript", "ctx.role === 'admin'"));
        Assertions.assertTrue(condition.isTrue(ctx));
    }

    @Test
    public void testConditionWithMultipleBindings() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("min",   int.class, 0);
        bindings.bind("max",   int.class, 100);
        bindings.bind("value", int.class, 50);
        RuleContext ctx = contextWith(bindings);
        Condition condition = Condition.builder().build(
                Script.builder().build("ECMAScript", "ctx.value > ctx.min && ctx.value < ctx.max"));
        Assertions.assertTrue(condition.isTrue(ctx));
    }

    // -----------------------------------------------------------------------
    // Error cases
    // -----------------------------------------------------------------------

    @Test
    public void testConditionScriptReturningNonBooleanThrows() {
        RuleContext ctx = contextWith(Bindings.builder().standard());
        Condition condition = Condition.builder().build(
                Script.builder().build("ECMAScript", "42"));
        Assertions.assertThrows(UnrulyException.class, () -> condition.isTrue(ctx));
    }

    @Test
    public void testConditionScriptReturningNullThrows() {
        RuleContext ctx = contextWith(Bindings.builder().standard());
        Condition condition = Condition.builder().build(
                Script.builder().build("ECMAScript", "null"));
        Assertions.assertThrows(UnrulyException.class, () -> condition.isTrue(ctx));
    }

    @Test
    public void testConditionNullScriptThrows() {
        Assertions.assertThrows(Exception.class, () -> Condition.builder().build((Script<?>) null));
    }
}
