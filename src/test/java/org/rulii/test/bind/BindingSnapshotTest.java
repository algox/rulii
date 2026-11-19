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
import org.rulii.bind.BindingValueListener;
import org.rulii.bind.Bindings;
import org.rulii.bind.ImmutableBinding;
import org.rulii.bind.InvalidBindingException;
import org.rulii.bind.ScopedBindings;
import org.rulii.context.RuleContext;
import org.rulii.model.condition.Condition;
import org.rulii.util.TypeReference;

import java.util.ArrayList;
import java.util.List;

/**
 * What the read-only copy of a Binding ({@link Binding#asImmutable()}) is: a snapshot of the Binding as it was,
 * that cannot be written through. These hold for the copy however it is built.
 *
 * @author Max Arulananthan
 * @since 2.3
 */
public class BindingSnapshotTest {

    public BindingSnapshotTest() {
        super();
    }

    @Test
    public void copyKeepsWhatTheBindingSays() {
        Binding<String> original = Binding.builder().with("code").type(String.class).value("abc")
                .primary(true).description("the code").build();
        Binding<String> copy = original.asImmutable();

        Assertions.assertInstanceOf(ImmutableBinding.class, copy);
        Assertions.assertNotSame(original, copy);
        Assertions.assertEquals("code", copy.getName());
        Assertions.assertEquals(String.class, copy.getType());
        Assertions.assertEquals("abc", copy.getValue());
        Assertions.assertTrue(copy.isPrimary());
        Assertions.assertEquals("the code", copy.getDescription());
        Assertions.assertEquals(original.getTypeAndName(), copy.getTypeAndName());
        Assertions.assertEquals(original.getSummary(), copy.getSummary());
        Assertions.assertEquals(original.getTextValue(), copy.getTextValue());
        Assertions.assertEquals(original.hashCode(), copy.hashCode());
        Assertions.assertTrue(copy.equals(original));
        Assertions.assertFalse(copy.isEditable());
    }

    @Test
    public void copyKeepsAGenericType() {
        List<Integer> values = new ArrayList<>(List.of(1, 2));
        Binding<List<Integer>> original = Binding.builder().with("values").type(new TypeReference<List<Integer>>() {})
                .value(values).build();
        Binding<List<Integer>> copy = original.asImmutable();

        Assertions.assertEquals(original.getType(), copy.getType());
        Assertions.assertSame(values, copy.getValue());
    }

    @Test
    public void copyIsASnapshot() {
        Binding<Integer> original = Binding.builder().with("count").type(Integer.class).value(1).build();
        Binding<Integer> copy = original.asImmutable();

        original.setValue(2);

        // A value set afterwards does not show in the copy; a new copy has it.
        Assertions.assertEquals(Integer.valueOf(1), copy.getValue());
        Assertions.assertEquals(Integer.valueOf(2), original.asImmutable().getValue());
    }

    @Test
    public void copyCannotBeWrittenThrough() {
        Binding<Integer> original = Binding.builder().with("count").type(Integer.class).value(1).build();
        Binding<Integer> copy = original.asImmutable();

        Assertions.assertThrows(IllegalStateException.class, () -> copy.setValue(5));
        Assertions.assertThrows(IllegalStateException.class, () -> copy.addValueListener(new BindingValueListener() {}));
        Assertions.assertEquals(Integer.valueOf(1), original.getValue());
        Assertions.assertSame(copy, copy.asImmutable());
    }

    @Test
    public void copyHasAnIdOfItsOwnAndIsNeverFinal() {
        Binding<Integer> original = Binding.builder().with("count").type(Integer.class).value(1).isFinal(true).build();
        Binding<Integer> copy = original.asImmutable();

        Assertions.assertTrue(original.isFinal());
        Assertions.assertFalse(copy.isFinal());
        Assertions.assertNotEquals(original.getId(), copy.getId());
        Assertions.assertEquals(copy.getId(), copy.getId());
    }

    @Test
    public void copyOfAnUneditableBindingIsTheSame() {
        Binding<Integer> original = Binding.builder().with("count").type(Integer.class).value(1).editable(false).build();
        Binding<Integer> copy = original.asImmutable();

        Assertions.assertInstanceOf(ImmutableBinding.class, copy);
        Assertions.assertEquals(Integer.valueOf(1), copy.getValue());
        Assertions.assertFalse(copy.isEditable());
    }

    @Test
    public void copyCarriesTheListenersButNeverCallsThem() {
        List<Object> seen = new ArrayList<>();
        BindingValueListener listener = new BindingValueListener() {
            @Override
            public void onChange(Binding<?> binding, Object oldValue, Object newValue) {
                seen.add(binding);
            }
        };
        Binding<Integer> original = Binding.builder().with("count").type(Integer.class).value(1).valueListeners(listener).build();
        Binding<Integer> copy = original.asImmutable();

        // Taking the copy tells nobody.
        Assertions.assertTrue(seen.isEmpty());
        Assertions.assertEquals(List.of(listener), copy.getBindingValueListeners());
        Assertions.assertThrows(UnsupportedOperationException.class, () -> copy.getBindingValueListeners().clear());

        // A change of the original is reported once, for the original; a listener added later is not in the copy.
        original.addValueListener(new BindingValueListener() {});
        original.setValue(2);
        Assertions.assertEquals(1, seen.size());
        Assertions.assertSame(original, seen.get(0));
        Assertions.assertEquals(1, copy.getBindingValueListeners().size());
        Assertions.assertEquals(2, original.getBindingValueListeners().size());
    }

    @Test
    public void copyOfANullValue() {
        Binding<String> text = Binding.builder().with("text").type(String.class).build();
        Assertions.assertNull(text.asImmutable().getValue());

        // A primitive that was set to null reads as the default of its type in the copy.
        Binding<Integer> count = Binding.builder().with("count").type(int.class).value(5).build();
        count.setValue(null);
        Assertions.assertNull(count.getValue());
        Assertions.assertEquals(Integer.valueOf(0), count.asImmutable().getValue());
    }

    @Test
    public void copyHandsOutBindingsReadOnly() {
        Bindings inner = Bindings.builder().standard(x -> 1);
        Binding<Bindings> original = Binding.builder().with("nested").type(Bindings.class).value(inner).build();
        Binding<Bindings> copy = original.asImmutable();

        Assertions.assertNotSame(inner, copy.getValue());
        Assertions.assertEquals(Integer.valueOf(1), copy.getValue().getValue("x"));
        Assertions.assertThrows(RuntimeException.class, () -> copy.getValue().bind("y", 2));
        Assertions.assertFalse(inner.contains("y"));
        // The original still hands out the Bindings themselves.
        Assertions.assertSame(inner, original.getValue());
    }

    @Test
    public void copyHandsOutARuleContextReadOnly() {
        RuleContext context = RuleContext.builder().build(Bindings.builder().standard(x -> 1));
        Binding<RuleContext> original = Binding.builder().with("context").type(RuleContext.class).value(context).build();
        Binding<RuleContext> copy = original.asImmutable();

        Assertions.assertNotSame(context, copy.getValue());
        Assertions.assertThrows(RuntimeException.class, () -> copy.getValue().getBindings().bind("y", 2));
        Assertions.assertFalse(context.getBindings().contains("y"));
    }

    @Test
    public void copyOfBindingsDeclaredByTheirOwnClassIsRefused() {
        // Declared with the class of the value, the read-only version of the value is not of that type.
        Bindings inner = Bindings.builder().standard(x -> 1);
        Binding<Bindings> original = Binding.builder().with("nested").value(inner).build();

        Assertions.assertEquals(inner.getClass(), original.getType());
        Assertions.assertThrows(InvalidBindingException.class, original::asImmutable);
    }

    @Test
    public void readOnlyViewsHandOutCopies() {
        ScopedBindings bindings = Bindings.builder().scoped();
        Binding<Integer> original = bindings.bind("count", Integer.class, 1);
        ScopedBindings readOnly = bindings.asImmutable();

        Binding<Integer> byName = readOnly.getBinding("count");
        Binding<Integer> byType = readOnly.getBinding("count", Integer.class);

        for (Binding<Integer> copy : List.of(byName, byType)) {
            Assertions.assertInstanceOf(ImmutableBinding.class, copy);
            Assertions.assertEquals(Integer.valueOf(1), copy.getValue());
            Assertions.assertThrows(IllegalStateException.class, () -> copy.setValue(9));
        }

        original.setValue(2);
        Assertions.assertEquals(Integer.valueOf(1), byName.getValue());
        Assertions.assertEquals(Integer.valueOf(2), readOnly.<Integer>getBinding("count").getValue());
        Assertions.assertEquals(Integer.valueOf(2), readOnly.<Integer>getValue("count"));
    }

    @Test
    public void conditionReceivesACopyOfItsBindingParameter() {
        Bindings bindings = Bindings.builder().standard();
        Binding<Integer> original = bindings.bind("count", Integer.class, 1);
        List<Binding<Integer>> received = new ArrayList<>();
        Condition condition = Condition.builder().with((Binding<Integer> count) -> {
            received.add(count);
            return count.getValue() > 0;
        }).build();

        Assertions.assertTrue(condition.isTrue(RuleContext.builder().build(bindings)));
        Assertions.assertTrue(condition.isTrue(RuleContext.builder().build(bindings).asImmutable()));

        Assertions.assertEquals(2, received.size());
        for (Binding<Integer> copy : received) {
            Assertions.assertNotSame(original, copy);
            Assertions.assertFalse(copy.isEditable());
            Assertions.assertThrows(IllegalStateException.class, () -> copy.setValue(9));
        }
        Assertions.assertEquals(Integer.valueOf(1), original.getValue());
    }
}
