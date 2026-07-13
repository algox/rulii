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
import org.rulii.bind.Binding;
import org.rulii.bind.BindingListener;
import org.rulii.bind.Bindings;
import org.rulii.bind.ImmutableBinding;
import org.rulii.bind.ImmutableBindings;
import org.rulii.bind.ImmutableScopedBindings;
import org.rulii.bind.NoSuchBindingException;
import org.rulii.bind.ReservedBindingNameException;
import org.rulii.bind.ReservedBindings;
import org.rulii.bind.ScopedBindings;

import java.util.Optional;
import java.util.Set;

/**
 * Coverage tests for DefaultBindings, Bindings interface defaults, ImmutableBindings,
 * ImmutableBinding, BindingsBuilder, ReservedBindings, and binding loaders.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class BindingsCoverageTest {

    public BindingsCoverageTest() {
        super();
    }

    // =========================================================================
    // DefaultBindings
    // =========================================================================

    @Test
    public void testEqualsAndHashCode() {
        Bindings bindings1 = Bindings.builder().standard(a -> 1, b -> "two");
        Bindings bindings2 = Bindings.builder().standard(a -> 1, b -> "two");
        Bindings different = Bindings.builder().standard(a -> 999);

        // Regression: comparing two DISTINCT Bindings instances used to recurse infinitely
        // (asMap() returns this, so asMap().equals(other.asMap()) called equals() again).
        Assertions.assertEquals(bindings1, bindings2);
        Assertions.assertNotEquals(bindings1, different);
        Assertions.assertEquals(bindings1.hashCode(), bindings2.hashCode());

        Assertions.assertEquals(bindings1, bindings1);
        Assertions.assertNotEquals(bindings1, null);
        Assertions.assertNotEquals(bindings1, "not bindings");
    }

    @Test
    public void testEqualsAndHashCode_immutableWrapper() {
        Bindings source = Bindings.builder().standard(a -> 1);
        Bindings immutable = source.asImmutable();
        Bindings sameContent = Bindings.builder().standard(a -> 1);

        Assertions.assertEquals(immutable, source);
        Assertions.assertEquals(immutable, sameContent);
        // Wrapper and target are equal, so their hash codes must match.
        Assertions.assertEquals(source.hashCode(), immutable.hashCode());
    }

    @Test
    public void testToString() {
        Bindings bindings = Bindings.builder().standard(a -> 1, b -> 2);
        Assertions.assertEquals("DefaultBindings(2)", bindings.toString());
    }

    @Test
    public void testPrettyPrint_skipsBindingsAndIncludesOthers() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("plain", 42);
        bindings.bind("nested", Bindings.builder().standard());

        String text = bindings.prettyPrint("> ");
        Assertions.assertTrue(text.contains("plain"));
        // Bindings-valued entries are excluded from the printout.
        Assertions.assertFalse(text.contains("nested"));
    }

    @Test
    public void testGetNames() {
        Bindings bindings = Bindings.builder().standard(a -> 1, b -> 2);
        Assertions.assertEquals(Set.of("a", "b"), bindings.getNames());
    }

    @Test
    public void testRemoveBindingListener() {
        Bindings bindings = Bindings.builder().standard();
        BindingListener listener = new BindingListener() {};

        bindings.addBindingListener(listener);
        Assertions.assertTrue(bindings.removeBindingListener(listener));
        Assertions.assertFalse(bindings.removeBindingListener(listener));
    }

    @Test
    public void testBindReservedName_throws() {
        Bindings bindings = Bindings.builder().standard();
        String reserved = ReservedBindings.RULE_CONTEXT.getName();

        ReservedBindingNameException e = Assertions.assertThrows(ReservedBindingNameException.class,
                () -> bindings.bind(reserved, 1));
        Assertions.assertEquals(reserved, e.getName());
    }

    @Test
    public void testReservedBindings() {
        Assertions.assertTrue(ReservedBindings.reservedBindingNames().contains("ruleContext"));
        Assertions.assertTrue(ReservedBindings.isReserved("bindings"));
        Assertions.assertFalse(ReservedBindings.isReserved("myOwnName"));
        Assertions.assertTrue(ReservedBindings.RULE_CONTEXT.toString().contains("ruleContext"));
    }

    // =========================================================================
    // Bindings interface defaults
    // =========================================================================

    @Test
    public void testGetOptionalValue() {
        Bindings bindings = Bindings.builder().standard(a -> 1);

        Assertions.assertEquals(Optional.of(1), bindings.getOptionalValue("a"));
        Assertions.assertEquals(Optional.empty(), bindings.getOptionalValue("noSuch"));
        Assertions.assertEquals(Optional.of(1), bindings.getOptionalValue("a", Integer.class));
        // Regression: a missing binding used to NPE (Optional.of on a null lookup) instead of
        // returning empty, and a type mismatch is also a miss - both must be Optional.empty().
        Assertions.assertEquals(Optional.empty(), bindings.getOptionalValue("noSuch", Integer.class));
        Assertions.assertEquals(Optional.empty(), bindings.getOptionalValue("a", String.class));
    }

    @Test
    public void testSetValues() {
        Bindings bindings = Bindings.builder().standard(a -> 1, b -> 2);
        bindings.setValues(a -> 10, b -> 20);

        Assertions.assertEquals(10, (Integer) bindings.getValue("a"));
        Assertions.assertEquals(20, (Integer) bindings.getValue("b"));
    }

    @Test
    public void testIsEmpty() {
        Assertions.assertTrue(Bindings.builder().standard().isEmpty());
        Assertions.assertFalse(Bindings.builder().standard(a -> 1).isEmpty());
    }

    // =========================================================================
    // BindingsBuilder overloads
    // =========================================================================

    @Test
    public void testScopedBuilderOverloads() {
        ScopedBindings scoped1 = Bindings.builder().scoped(a -> 1, b -> 2);
        Assertions.assertEquals(2, scoped1.size());
        Assertions.assertEquals(ScopedBindings.ROOT_SCOPE, scoped1.getRootScope().getName());

        ScopedBindings scoped2 = Bindings.builder().scoped("customRoot");
        Assertions.assertEquals("customRoot", scoped2.getRootScope().getName());

        Bindings rootBindings = Bindings.builder().standard(x -> 42);
        ScopedBindings scoped3 = Bindings.builder().scoped("namedRoot", rootBindings);
        Assertions.assertEquals("namedRoot", scoped3.getRootScope().getName());
        Assertions.assertEquals(42, (Integer) scoped3.getValue("x"));

        ScopedBindings scoped4 = Bindings.builder().scoped("declRoot", a -> 1);
        Assertions.assertEquals("declRoot", scoped4.getRootScope().getName());
        Assertions.assertEquals(1, (Integer) scoped4.getValue("a"));
    }

    @Test
    public void testImmutableBuilderOverloads() {
        ImmutableBindings immutable1 = Bindings.builder().immutable(Bindings.builder().standard(a -> 1));
        Assertions.assertEquals(1, immutable1.size());

        ImmutableBindings immutable2 = Bindings.builder().immutable(a -> 1, b -> 2);
        Assertions.assertEquals(2, immutable2.size());

        ImmutableScopedBindings immutable3 = Bindings.builder().immutable(Bindings.builder().scoped(a -> 1));
        Assertions.assertEquals(1, immutable3.size());
    }

    // =========================================================================
    // ImmutableBindings
    // =========================================================================

    @Test
    public void testImmutableBindings_readsAndIdentity() {
        Bindings source = Bindings.builder().standard(a -> 1, b -> "two");
        Bindings immutable = source.asImmutable();

        Assertions.assertEquals(source.getNames(), immutable.getNames());
        Assertions.assertSame(immutable, immutable.asImmutable());
        Assertions.assertEquals(source.toString(), immutable.toString());
        Assertions.assertEquals(source.prettyPrint("> "), immutable.prettyPrint("> "));
        Assertions.assertDoesNotThrow(immutable::hashCode);

        Assertions.assertEquals(immutable, immutable);
        Assertions.assertEquals(immutable, source);
        Assertions.assertNotEquals(immutable, null);
        Assertions.assertNotEquals(immutable, "not bindings");
    }

    @Test
    public void testImmutableBindings_listenerMethodsThrow() {
        Bindings immutable = Bindings.builder().standard(a -> 1).asImmutable();
        BindingListener listener = new BindingListener() {};

        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> immutable.addBindingListener(listener));
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> immutable.removeBindingListener(listener));
    }

    // =========================================================================
    // ImmutableBinding
    // =========================================================================

    @Test
    public void testImmutableBinding_delegatesReads() {
        Bindings bindings = Bindings.builder().standard();
        Binding<Integer> original = bindings.bind("count", 42);
        Binding<Integer> immutable = original.asImmutable();

        Assertions.assertInstanceOf(ImmutableBinding.class, immutable);
        // asImmutable() snapshots into a NEW binding (by design), so the id is fresh - not equal.
        Assertions.assertNotNull(immutable.getId());
        Assertions.assertEquals(original.getName(), immutable.getName());
        Assertions.assertEquals(original.getTypeAndName(), immutable.getTypeAndName());
        Assertions.assertEquals(original.getSummary(), immutable.getSummary());
        Assertions.assertEquals(original.isFinal(), immutable.isFinal());
        Assertions.assertEquals(original.isPrimary(), immutable.isPrimary());
        Assertions.assertFalse(immutable.isEditable());
        Assertions.assertTrue(immutable.isTypeAcceptable(Integer.class));
        Assertions.assertTrue(immutable.isAssignable(Number.class));
        Assertions.assertDoesNotThrow(immutable::hashCode);
        Assertions.assertNotNull(immutable.getBindingValueListeners());
        Assertions.assertNotNull(immutable.toString());
        // asImmutable() of an already-immutable binding is itself.
        Assertions.assertSame(immutable, immutable.asImmutable());
    }

    @Test
    public void testImmutableBinding_mutatorsThrow() {
        Bindings bindings = Bindings.builder().standard();
        Binding<Integer> immutable = bindings.bind("count", 42).asImmutable();

        Assertions.assertThrows(IllegalStateException.class, () -> immutable.setValue(1));
        Assertions.assertThrows(IllegalStateException.class,
                () -> immutable.addValueListener(new BindingListener() {}));
        Assertions.assertThrows(IllegalStateException.class,
                () -> immutable.removeValueListener(new BindingListener() {}));
    }

    // =========================================================================
    // Binding loaders — delegated setters
    // =========================================================================

    public static class LoaderBean {

        private String name = "initial";
        private int score = 10;
        private final String readOnly = "fixed";

        public LoaderBean() {
            super();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }

        public String getReadOnly() {
            return readOnly;
        }
    }

    @Test
    public void testPropertyLoader_setterWritesThroughToBean() {
        LoaderBean bean = new LoaderBean();
        Bindings bindings = Bindings.builder().standard();
        bindings.loadProperties(bean);

        Assertions.assertEquals("initial", bindings.getValue("name"));

        // Writing through the binding must invoke the bean's setter.
        bindings.setValue("name", "updated");
        Assertions.assertEquals("updated", bean.getName());

        // A getter-only property loads but is not editable.
        Assertions.assertEquals("fixed", bindings.getValue("readOnly"));
        Binding<String> readOnly = bindings.getBinding("readOnly");
        Assertions.assertFalse(readOnly.isEditable());
    }

    @Test
    public void testFieldLoader_setterWritesThroughToBean() {
        LoaderBean bean = new LoaderBean();
        Bindings bindings = Bindings.builder().standard();
        bindings.loadFields(bean);

        Assertions.assertEquals(10, (Integer) bindings.getValue("score"));

        bindings.setValue("score", 99);
        Assertions.assertEquals(99, bean.getScore());

        // Load a second instance of the same class - exercises the field cache.
        Bindings bindings2 = Bindings.builder().standard();
        bindings2.loadFields(new LoaderBean());
        Assertions.assertEquals(10, (Integer) bindings2.getValue("score"));
    }

    // =========================================================================
    // Exceptions
    // =========================================================================

    @Test
    public void testNoSuchBindingException_twoArgConstructor() {
        NoSuchBindingException e = new NoSuchBindingException("myBinding", Integer.class);
        Assertions.assertEquals("myBinding", e.getBindingName());
        Assertions.assertEquals(Integer.class, e.getType());
        Assertions.assertTrue(e.getMessage().contains("myBinding"));

        NoSuchBindingException e2 = new NoSuchBindingException("other");
        Assertions.assertEquals("other", e2.getBindingName());
        Assertions.assertNull(e2.getType());
    }
}
