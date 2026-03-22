package org.rulii.test.script.js;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.script.ScriptOptions;

/**
 * Unit tests for ScriptOptions.
 */
public class ScriptOptionsTest {

    @Test
    public void testDefaultBindingsName() {
        Assertions.assertEquals("ctx", ScriptOptions.DEFAULT.bindingsName());
    }

    @Test
    public void testCustomBindingsName() {
        ScriptOptions options = new ScriptOptions("bindings");
        Assertions.assertEquals("bindings", options.bindingsName());
    }

    @Test
    public void testNullBindingsNameThrows() {
        Assertions.assertThrows(Exception.class, () -> new ScriptOptions(null));
    }

    @Test
    public void testEmptyBindingsNameThrows() {
        Assertions.assertThrows(Exception.class, () -> new ScriptOptions(""));
    }

    @Test
    public void testDefaultConstantIsNotNull() {
        Assertions.assertNotNull(ScriptOptions.DEFAULT);
    }

    @Test
    public void testEquality() {
        ScriptOptions a = new ScriptOptions("ctx");
        ScriptOptions b = new ScriptOptions("ctx");
        Assertions.assertEquals(a, b);
    }

    @Test
    public void testInequality() {
        ScriptOptions a = new ScriptOptions("ctx");
        ScriptOptions b = new ScriptOptions("bindings");
        Assertions.assertNotEquals(a, b);
    }
}
