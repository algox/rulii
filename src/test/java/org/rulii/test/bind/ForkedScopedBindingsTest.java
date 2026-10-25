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
import org.rulii.bind.BindingAlreadyExistsException;
import org.rulii.bind.BindingListener;
import org.rulii.bind.Bindings;
import org.rulii.bind.CannotRemoveRootScopeException;
import org.rulii.bind.NamedScope;
import org.rulii.bind.NoSuchBindingsException;
import org.rulii.bind.NoSuchScopeException;
import org.rulii.bind.PromiscuousBinder;
import org.rulii.bind.ReservedBindings;
import org.rulii.bind.ScopedBindings;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link ScopedBindings#fork()}: inherited scopes shared by reference, everything structural private to the fork.
 *
 * @author Max Arulananthan
 * @since 2.2
 */
public class ForkedScopedBindingsTest {

    public ForkedScopedBindingsTest() {
        super();
    }

    /** Parent with "a" in the root scope and "b" in a scope named "inner" (the current scope). */
    private static ScopedBindings parent() {
        ScopedBindings parent = Bindings.builder().scoped();
        parent.bind("a", 1);
        parent.addScope("inner");
        parent.bind("b", 2);
        return parent;
    }

    @Test
    public void forkSharesTheInheritedScopesByReference() {
        ScopedBindings parent = parent();
        NamedScope inner = parent.getCurrentScope();

        ScopedBindings fork = parent.fork();

        Assertions.assertEquals(parent.getScopeSize() + 1, fork.getScopeSize());
        Assertions.assertSame(inner, fork.getScope("inner"));
        Assertions.assertSame(parent.getRootScope(), fork.getRootScope());
        Assertions.assertTrue(fork.getCurrentScope().getName().startsWith("fork-scope-"));
        Assertions.assertSame(inner, fork.getParentScope());
        Assertions.assertEquals(1, (Integer) fork.getValue("a"));
        Assertions.assertEquals(2, (Integer) fork.getValue("b"));
        Assertions.assertEquals(parent.size(), fork.size());
    }

    @Test
    public void valuesAreSharedBothWays() {
        ScopedBindings parent = parent();
        ScopedBindings fork = parent.fork();

        fork.setValue("a", 10);
        Assertions.assertEquals(10, (Integer) parent.getValue("a"));

        parent.setValue("b", 20);
        Assertions.assertEquals(20, (Integer) fork.getValue("b"));

        // A Binding the parent adds into a shared scope after the fork is visible too: the scope is shared.
        parent.bind("c", 3);
        Assertions.assertEquals(3, (Integer) fork.getValue("c"));
    }

    @Test
    public void forkBindingsArePrivate() {
        ScopedBindings parent = parent();
        ScopedBindings fork = parent.fork();

        fork.bind("x", "y");

        Assertions.assertTrue(fork.contains("x"));
        Assertions.assertFalse(parent.contains("x"));
        Assertions.assertSame(fork.getCurrentBindings().getBinding("x"), fork.getBinding("x"));
        Assertions.assertEquals(parent.size() + 1, fork.size());
    }

    @Test
    public void scopesTheParentAddsAfterTheForkAreInvisible() {
        ScopedBindings parent = parent();
        ScopedBindings fork = parent.fork();

        parent.addScope("later");
        parent.bind("d", 4);

        Assertions.assertFalse(fork.contains("d"));
        Assertions.assertNull(fork.getScope("later"));
        Assertions.assertTrue(parent.contains("d"));
    }

    @Test
    public void scopesTheParentRemovesAfterTheForkStayVisible() {
        ScopedBindings parent = parent();
        ScopedBindings fork = parent.fork();

        parent.removeScope();

        Assertions.assertNull(parent.getScope("inner"));
        Assertions.assertFalse(parent.contains("b"));
        Assertions.assertNotNull(fork.getScope("inner"));
        Assertions.assertEquals(2, (Integer) fork.getValue("b"));
    }

    @Test
    public void forkScopesArePrivate() {
        ScopedBindings parent = parent();
        ScopedBindings fork = parent.fork();
        int parentSize = parent.getScopeSize();
        int forkSize = fork.getScopeSize();

        NamedScope child = fork.addScope("child");
        fork.bind("e", 5);

        Assertions.assertNull(parent.getScope("child"));
        Assertions.assertFalse(parent.contains("e"));
        Assertions.assertEquals(parentSize, parent.getScopeSize());
        Assertions.assertSame(child, fork.getCurrentScope());

        fork.removeScope(child);
        Assertions.assertEquals(forkSize, fork.getScopeSize());
        Assertions.assertFalse(fork.contains("e"));
    }

    @Test
    public void forkCannotPopBelowItsFirstOwnScope() {
        ScopedBindings parent = parent();
        ScopedBindings fork = parent.fork();

        Assertions.assertThrows(CannotRemoveRootScopeException.class, fork::removeScope);
        // The parent's scopes are untouched by the attempt.
        Assertions.assertNotNull(parent.getScope("inner"));
        Assertions.assertEquals(parent.getScopeSize() + 1, fork.getScopeSize());
    }

    @Test
    public void removingAnInheritedScopeThroughTheForkFailsCleanly() {
        ScopedBindings parent = parent();
        NamedScope inner = parent.getCurrentScope();
        ScopedBindings fork = parent.fork();
        fork.addScope("child");
        int forkSize = fork.getScopeSize();

        Assertions.assertThrows(NoSuchScopeException.class, () -> fork.removeScope(inner));
        Assertions.assertThrows(NoSuchBindingsException.class, () -> fork.removeScope(inner.getBindings()));
        Assertions.assertThrows(NoSuchBindingsException.class, () -> fork.removeScope("inner"));
        // Nothing was popped on the way to the failure.
        Assertions.assertEquals(forkSize, fork.getScopeSize());
        Assertions.assertNotNull(fork.getScope("child"));
        Assertions.assertNotNull(parent.getScope("inner"));
    }

    @Test
    public void listenersAttachOnlyToOwnedScopes() {
        ScopedBindings parent = parent();
        RecordingListener before = new RecordingListener();
        parent.addBindingListener(before);
        ScopedBindings fork = parent.fork();
        RecordingListener forkListener = new RecordingListener();
        fork.addBindingListener(forkListener);
        RecordingListener after = new RecordingListener();
        parent.addBindingListener(after);

        // The parent's listeners present at the fork are copied: they see the fork's scope changes.
        fork.addScope("child");
        Assertions.assertEquals(List.of("child"), before.scopesAdded);
        Assertions.assertEquals(List.of("child"), forkListener.scopesAdded);
        Assertions.assertEquals(List.of(), after.scopesAdded);

        // The fork's own listener hears binds in the fork, not binds the parent makes into shared scopes.
        fork.bind("x", 1);
        parent.bind("y", 2);
        Assertions.assertEquals(List.of("x"), forkListener.bound);
        Assertions.assertEquals(List.of("y"), after.bound);
        Assertions.assertEquals(List.of("x", "y"), before.bound);

        // Removing the fork's listener removes it from the fork's scopes only; the parent never had it.
        Assertions.assertTrue(fork.removeBindingListener(forkListener));
        Assertions.assertFalse(parent.removeBindingListener(forkListener));
        fork.bind("z", 3);
        Assertions.assertEquals(List.of("x"), forkListener.bound);
    }

    @Test
    public void forkOfAFork() {
        ScopedBindings parent = parent();
        ScopedBindings fork = parent.fork();
        fork.bind("x", "y");

        ScopedBindings grandChild = fork.fork();
        grandChild.bind("w", "v");

        Assertions.assertEquals(fork.getScopeSize() + 1, grandChild.getScopeSize());
        Assertions.assertEquals("y", grandChild.getValue("x"));
        Assertions.assertEquals(1, (Integer) grandChild.getValue("a"));
        Assertions.assertFalse(fork.contains("w"));
        Assertions.assertFalse(parent.contains("w"));
        Assertions.assertNotEquals(fork.getCurrentScope().getName(), grandChild.getCurrentScope().getName());
        Assertions.assertThrows(CannotRemoveRootScopeException.class, grandChild::removeScope);
    }

    @Test
    public void reservedPairIsNotInheritedButItsNeighboursAre() {
        ScopedBindings parent = Bindings.builder().scoped();
        PromiscuousBinder root = (PromiscuousBinder) parent.getRootScope().getBindings();
        root.promiscuousBind(Binding.builder().with(ReservedBindings.RULE_CONTEXT.getName()).type(Object.class).isFinal(true).value("parentContext").build());
        root.promiscuousBind(Binding.builder().with(ReservedBindings.BINDINGS.getName()).type(Bindings.class).isFinal(true).value(parent).build());
        parent.bind("a", 1);
        parent.addScope("inner");
        parent.bind("b", 2);

        ScopedBindings fork = parent.fork();

        Assertions.assertFalse(fork.contains(ReservedBindings.RULE_CONTEXT.getName()));
        Assertions.assertFalse(fork.contains(ReservedBindings.BINDINGS.getName()));
        Assertions.assertTrue(parent.contains(ReservedBindings.RULE_CONTEXT.getName()));
        // the root was substituted (same name), the other scope is the very same object
        Assertions.assertNotSame(parent.getRootScope(), fork.getRootScope());
        Assertions.assertEquals(ScopedBindings.ROOT_SCOPE, fork.getRootScope().getName());
        Assertions.assertSame(parent.getScope("inner"), fork.getScope("inner"));
        // "a" sat beside the pair: still there, and still the same Binding, so its value is shared
        Assertions.assertSame(parent.getBinding("a"), fork.getBinding("a"));
        fork.setValue("a", 10);
        Assertions.assertEquals(10, (Integer) parent.getValue("a"));
        Assertions.assertEquals(2, (Integer) fork.getValue("b"));
    }

    @Test
    public void immutableBindingsCannotFork() {
        ScopedBindings parent = parent();
        Assertions.assertThrows(UnsupportedOperationException.class, () -> parent.asImmutable().fork());
    }

    @Test
    public void viewsIncludeTheInheritedScopes() {
        ScopedBindings parent = parent();
        ScopedBindings fork = parent.fork();
        fork.bind("x", "y");

        Assertions.assertTrue(fork.asMap().containsKey("a"));
        Assertions.assertTrue(fork.asMap().containsKey("x"));
        Assertions.assertTrue(fork.getNames().containsAll(List.of("a", "b", "x")));
        Assertions.assertEquals(3, fork.uniqueSize());
        Assertions.assertTrue(fork.prettyPrint("").contains("Scope (index = 2)"));
        Assertions.assertEquals(parent.uniqueSize() + 1, fork.uniqueSize());
    }

    @Test
    public void finalInheritedBindingBlocksARebindInTheFork() {
        ScopedBindings parent = parent();
        parent.bind(Binding.builder().with("f").type(Integer.class).isFinal(true).value(1).build());
        ScopedBindings fork = parent.fork();

        Assertions.assertThrows(BindingAlreadyExistsException.class, () -> fork.bind("f", 2));
        // A non-final inherited name can be shadowed in the fork without touching the parent.
        fork.bind("a", 100);
        Assertions.assertEquals(100, (Integer) fork.getValue("a"));
        Assertions.assertEquals(1, (Integer) parent.getValue("a"));
    }

    @Test
    public void typeLookupStopsAtTheFirstScopeWithAMatch() {
        ScopedBindings parent = parent();
        ScopedBindings fork = parent.fork();
        fork.bind("x", "y");

        List<Binding<String>> strings = fork.getBindings(String.class);
        Assertions.assertEquals(1, strings.size());
        Assertions.assertEquals("x", strings.get(0).getName());
        // Integers live in the inherited scopes: "a" in the root, "b" in inner. getBindings stops at inner.
        List<Binding<Integer>> integers = fork.getBindings(Integer.class);
        Assertions.assertEquals(1, integers.size());
        Assertions.assertEquals("b", integers.get(0).getName());
        Assertions.assertEquals(2, fork.getAllBindings(Integer.class).size());
    }

    private static final class RecordingListener implements BindingListener {

        final List<String> bound = new ArrayList<>();
        final List<String> scopesAdded = new ArrayList<>();

        @Override
        public void onBind(Binding<?> binding) {
            bound.add(binding.getName());
        }

        @Override
        public void onScopeAdd(NamedScope scope) {
            scopesAdded.add(scope.getName());
        }
    }
}
