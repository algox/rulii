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
import org.rulii.bind.ContextHider;
import org.rulii.bind.HiddenContextBindings;
import org.rulii.bind.NoSuchBindingException;
import org.rulii.context.RuleContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * {@link HiddenContextBindings}: the bindings of a context without its reserved {@code ruleContext} and
 * {@code bindings}, everything else passed through.
 *
 * @author Max Arulananthan
 * @since 2.3
 */
public class HiddenContextBindingsTest {

    public HiddenContextBindingsTest() {
        super();
    }

    private static RuleContext context() {
        return RuleContext.builder().build(Bindings.builder().standard(x -> "v"));
    }

    private static Set<String> names(Iterable<? extends Binding<?>> bindings) {
        Set<String> result = new TreeSet<>();
        for (Binding<?> binding : bindings) result.add(binding.getName());
        return result;
    }

    @Test
    public void bindingsWithoutAContextAreReturnedAsTheyAre() {
        Bindings plain = Bindings.builder().standard(x -> "v");
        Assertions.assertSame(plain, HiddenContextBindings.of(plain));

        Bindings scoped = Bindings.builder().scoped();
        Assertions.assertSame(scoped, HiddenContextBindings.of(scoped));

        Assertions.assertThrows(IllegalArgumentException.class, () -> HiddenContextBindings.of(null));
    }

    @Test
    public void theReservedPairOfAContextIsHidden() {
        RuleContext context = context();
        Bindings view = HiddenContextBindings.of(context.getBindings());

        Assertions.assertInstanceOf(HiddenContextBindings.class, view);
        Assertions.assertInstanceOf(ContextHider.class, view);
        Assertions.assertFalse(context.getBindings() instanceof ContextHider);
        Assertions.assertSame(context.getBindings(), ((HiddenContextBindings) view).getDelegate());

        Assertions.assertNull(view.getBinding("ruleContext"));
        Assertions.assertNull(view.getBinding("bindings"));
        Assertions.assertNull(view.getBinding("ruleContext", RuleContext.class));
        Assertions.assertNull(view.getBinding("bindings", Bindings.class));
        Assertions.assertFalse(view.contains("ruleContext"));
        Assertions.assertFalse(view.contains("bindings"));
        Assertions.assertThrows(NoSuchBindingException.class, () -> view.getValue("ruleContext"));
        Assertions.assertTrue(view.getBindings(RuleContext.class).isEmpty());
        Assertions.assertTrue(view.getBindings(Bindings.class).isEmpty());

        Assertions.assertEquals(Set.of("x"), view.getNames());
        Assertions.assertEquals(Set.of("x"), names(view));
        Assertions.assertEquals(Map.of("x", "v"), view.asMap());
        Assertions.assertEquals(1, view.size());
        Assertions.assertFalse(view.isEmpty());

        // The context keeps its own.
        Assertions.assertSame(context, context.getBindings().getValue("ruleContext", RuleContext.class));
        Assertions.assertEquals(3, context.getBindings().size());
    }

    @Test
    public void aViewIsNotWrappedAgain() {
        Bindings view = HiddenContextBindings.of(context().getBindings());
        Assertions.assertSame(view, HiddenContextBindings.of(view));
    }

    @Test
    public void everythingElseIsTheDelegates() {
        RuleContext context = context();
        Bindings view = HiddenContextBindings.of(context.getBindings());

        Assertions.assertSame(context.getBindings().getBinding("x"), view.getBinding("x"));
        Assertions.assertSame(context.getBindings().getBinding("x"), view.getBinding("x", String.class));
        Assertions.assertNull(view.getBinding("x", Integer.class));
        Assertions.assertEquals("v", view.getValue("x", String.class));

        // Binds and values go to the context's bindings.
        view.bind("y", String.class, "w");
        Assertions.assertEquals("w", context.getBindings().getValue("y", String.class));
        view.setValue("x", "changed");
        Assertions.assertEquals("changed", context.getBindings().getValue("x", String.class));

        Assertions.assertEquals(Set.of("x", "y"), view.getNames());
        Assertions.assertEquals(2, view.size());
    }

    @Test
    public void listenersAreTheDelegates() {
        RuleContext context = context();
        Bindings view = HiddenContextBindings.of(context.getBindings());
        List<String> bound = new ArrayList<>();
        BindingListener listener = new BindingListener() {
            @Override
            public void onBind(Binding<?> binding) {
                bound.add(binding.getName());
            }
        };

        view.addBindingListener(listener);
        context.getBindings().bind("y", String.class, "w");
        Assertions.assertEquals(List.of("y"), bound);

        Assertions.assertTrue(view.removeBindingListener(listener));
        context.getBindings().bind("z", String.class, "w");
        Assertions.assertEquals(List.of("y"), bound);
    }

    @Test
    public void aLookupByTypeKeepsTheOtherBindingsOfTheScope() {
        // The first scope of a fork holds the fork's reserved pair next to what the fork binds.
        RuleContext fork = context().fork();
        fork.getBindings().bind("a", String.class, "1");
        fork.getBindings().bind("b", String.class, "2");
        Bindings scope = fork.getBindings().getCurrentBindings();
        Bindings view = HiddenContextBindings.of(scope);

        Assertions.assertEquals(Set.of("a", "b", "bindings", "ruleContext"), names(scope.getBindings(Object.class)));
        Assertions.assertEquals(Set.of("a", "b"), names(view.getBindings(Object.class)));
        Assertions.assertEquals(Set.of("a", "b"), names(view.getBindings(String.class)));
        Assertions.assertTrue(view.getBindings(Integer.class).isEmpty());
        Assertions.assertEquals(2, view.size());
    }

    @Test
    public void theReadOnlyVersionHidesThePairToo() {
        RuleContext context = context();
        Bindings readOnly = HiddenContextBindings.of(context.getBindings()).asImmutable();

        Assertions.assertNull(readOnly.getBinding("ruleContext"));
        Assertions.assertEquals("v", readOnly.getValue("x", String.class));
        Assertions.assertThrows(UnsupportedOperationException.class, () -> readOnly.bind("y", String.class, "w"));
        Assertions.assertFalse(context.getBindings().contains("y"));
    }

    @Test
    public void theReadOnlyBindingsOfAContextAreHiddenTheSameWay() {
        RuleContext context = context();
        Bindings view = HiddenContextBindings.of(context.getBindings().asImmutable());

        Assertions.assertInstanceOf(HiddenContextBindings.class, view);
        Assertions.assertNull(view.getBinding("ruleContext"));
        Assertions.assertEquals("v", view.getValue("x", String.class));
        Assertions.assertThrows(UnsupportedOperationException.class, () -> view.bind("y", String.class, "w"));
    }
}
