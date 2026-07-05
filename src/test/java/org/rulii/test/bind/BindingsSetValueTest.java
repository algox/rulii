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
package org.rulii.test.bind;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.bind.*;

/**
 * Tests for the updated setValue() (now returns previous value) and
 * the new setValueOrBind() method across standard, scoped and immutable Bindings.
 */
public class BindingsSetValueTest {

    // -----------------------------------------------------------------------
    // setValue(String, T) — returns previous value
    // -----------------------------------------------------------------------

    @Test
    public void testSetValueReturnsPreviousValue() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("x", String.class, "original");
        String previous = bindings.setValue("x", "updated");
        Assertions.assertEquals("original", previous);
    }

    @Test
    public void testSetValueUpdatesValue() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("x", String.class, "original");
        bindings.setValue("x", "updated");
        Assertions.assertEquals("updated", bindings.getValue("x"));
    }

    @Test
    public void testSetValueReturnsPreviousNull() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("x", String.class, null);
        String previous = bindings.setValue("x", "hello");
        Assertions.assertNull(previous);
    }

    @Test
    public void testSetValueReturnsNewValueAfterChain() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("counter", Integer.class, 1);
        Integer p1 = bindings.setValue("counter", 2);
        Integer p2 = bindings.setValue("counter", 3);
        Assertions.assertEquals(1, p1);
        Assertions.assertEquals(2, p2);
        Assertions.assertEquals(3, (Integer) bindings.getValue("counter"));
    }

    @Test
    public void testSetValueUnknownNameThrows() {
        Bindings bindings = Bindings.builder().standard();
        Assertions.assertThrows(NoSuchBindingException.class,
                () -> bindings.setValue("missing", "value"));
    }

    @Test
    public void testSetValueWrongTypeThrows() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("x", String.class, "hello");
        Assertions.assertThrows(InvalidBindingException.class,
                () -> bindings.setValue("x", 42));
    }

    @Test
    public void testSetValueOnScopedBindingsReturnsPrevious() {
        Bindings scoped = Bindings.builder().scoped();
        scoped.bind("x", int.class, 10);
        Integer previous = scoped.setValue("x", 99);
        Assertions.assertEquals(10, previous);
        Assertions.assertEquals(99, (Integer) scoped.getValue("x"));
    }

    // -----------------------------------------------------------------------
    // setValue(BindingDeclaration<T>) — returns previous value
    // -----------------------------------------------------------------------

    @Test
    public void testSetValueViaDeclarationReturnsPrevious() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("score", int.class, 50);
        Integer previous = bindings.setValue(score -> 100);
        Assertions.assertEquals(50, previous);
        Assertions.assertEquals(100, (Integer) bindings.getValue("score"));
    }

    // -----------------------------------------------------------------------
    // setValueOrBind(String, T) — sets if exists, binds if not
    // -----------------------------------------------------------------------

    @Test
    public void testSetValueOrBindCreatesNewBinding() {
        Bindings bindings = Bindings.builder().standard();
        Object previous = bindings.setValueOrBind("newKey", "hello");
        Assertions.assertNull(previous);
        Assertions.assertTrue(bindings.contains("newKey"));
        Assertions.assertEquals("hello", bindings.getValue("newKey"));
    }

    @Test
    public void testSetValueOrBindUpdatesExistingAndReturnsPrevious() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("x", String.class, "first");
        String previous = bindings.setValueOrBind("x", "second");
        Assertions.assertEquals("first", previous);
        Assertions.assertEquals("second", bindings.getValue("x"));
    }

    @Test
    public void testSetValueOrBindReturnsNullForNewBinding() {
        Bindings bindings = Bindings.builder().standard();
        Object result = bindings.setValueOrBind("brandNew", 42);
        Assertions.assertNull(result);
    }

    @Test
    public void testSetValueOrBindWithNullValueCreatesBinding() {
        Bindings bindings = Bindings.builder().standard();
        Object previous = bindings.setValueOrBind("nullKey", null);
        Assertions.assertNull(previous);
        Assertions.assertTrue(bindings.contains("nullKey"));
    }

    @Test
    public void testSetValueOrBindCalledTwiceReturnsPreviousOnSecond() {
        Bindings bindings = Bindings.builder().standard();
        bindings.setValueOrBind("counter", 1);
        Object previous = bindings.setValueOrBind("counter", 2);
        Assertions.assertEquals(1, ((Number) previous).intValue());
    }

    @Test
    public void testSetValueOrBindOnScopedBindings() {
        Bindings scoped = Bindings.builder().scoped();
        scoped.setValueOrBind("flag", true);
        Assertions.assertEquals(Boolean.TRUE, scoped.getValue("flag"));
        Boolean previous = scoped.setValueOrBind("flag", false);
        Assertions.assertEquals(Boolean.TRUE, previous);
        Assertions.assertEquals(Boolean.FALSE, scoped.getValue("flag"));
    }

    // -----------------------------------------------------------------------
    // ImmutableBindings — setValue and setValueOrBind throw
    // -----------------------------------------------------------------------

    @Test
    public void testSetValueOnImmutableThrows() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("x", String.class, "value");
        Bindings immutable = bindings.asImmutable();
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> immutable.setValue("x", "new"));
    }

    @Test
    public void testSetValueOrBindOnImmutableThrows() {
        Bindings bindings = Bindings.builder().standard();
        Bindings immutable = bindings.asImmutable();
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> immutable.setValueOrBind("x", "value"));
    }

    @Test
    public void testSetValueOnImmutableScopedThrows() {
        // ImmutableScopedBindings must block setValue() on existing bindings too, not just
        // structural changes (add/remove scope, add binding) — matching ImmutableBindings'
        // behavior and the read-only contract Condition/Function rely on for their RuleContext
        // argument.
        ScopedBindings scoped = Bindings.builder().scoped();
        scoped.bind("x", String.class, "original");
        ScopedBindings immutable = scoped.asImmutable();
        Assertions.assertThrows(IllegalStateException.class, () -> immutable.setValue("x", "updated"));
        Assertions.assertEquals("original", scoped.getValue("x"), "Original binding must be unaffected.");
    }

    @Test
    public void testSetValueOrBindOnImmutableScopedThrows() {
        // setValueOrBind() would try to create a new binding on a non-existent key,
        // which ImmutableScopedBindings blocks.
        ScopedBindings scoped = Bindings.builder().scoped();
        ScopedBindings immutable = scoped.asImmutable();
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> immutable.setValueOrBind("newKey", "value"));
    }
}
