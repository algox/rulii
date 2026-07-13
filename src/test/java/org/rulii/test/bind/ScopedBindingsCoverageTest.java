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
import org.rulii.bind.BindingsAlreadyExistsException;
import org.rulii.bind.CannotRemoveRootScopeException;
import org.rulii.bind.ImmutableBinding;
import org.rulii.bind.NamedScope;
import org.rulii.bind.NoSuchBindingsException;
import org.rulii.bind.NoSuchScopeException;
import org.rulii.bind.ScopedBindings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Coverage tests for DefaultScopedBindings and ImmutableScopedBindings —
 * scope management, listeners, map view, and immutability enforcement.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class ScopedBindingsCoverageTest {

    public ScopedBindingsCoverageTest() {
        super();
    }

    // =========================================================================
    // DefaultScopedBindings — scope management
    // =========================================================================

    @Test
    public void testGetScopeName() {
        ScopedBindings scoped = Bindings.builder().scoped();
        NamedScope scope = scoped.addScope("myScope");

        Assertions.assertEquals("myScope", scoped.getScopeName(scope.getBindings()));
        // Bindings that are not part of any scope have no name.
        Assertions.assertNull(scoped.getScopeName(Bindings.builder().standard()));
        Assertions.assertThrows(IllegalArgumentException.class, () -> scoped.getScopeName(null));
    }

    @Test
    public void testAddScope_duplicateName_throws() {
        ScopedBindings scoped = Bindings.builder().scoped();
        scoped.addScope("dupScope");

        BindingsAlreadyExistsException e = Assertions.assertThrows(BindingsAlreadyExistsException.class,
                () -> scoped.addScope("dupScope"));
        Assertions.assertEquals("dupScope", e.getName());
        Assertions.assertNotNull(e.getExistingBindings());
    }

    @Test
    public void testRemoveScope_rootScope_throws() {
        ScopedBindings scoped = Bindings.builder().scoped();
        Assertions.assertThrows(CannotRemoveRootScopeException.class, scoped::removeScope);
        Assertions.assertThrows(CannotRemoveRootScopeException.class,
                () -> scoped.removeScope(scoped.getCurrentBindings()));
    }

    @Test
    public void testRemoveScopeByName_notFound_throws() {
        ScopedBindings scoped = Bindings.builder().scoped();
        scoped.addScope("someScope");

        NoSuchBindingsException e = Assertions.assertThrows(NoSuchBindingsException.class,
                () -> scoped.removeScope("noSuchScope"));
        Assertions.assertEquals("noSuchScope", e.getName());
        Assertions.assertNull(e.getBindings());
    }

    @Test
    public void testRemoveScopeByBindings_notFound_throws() {
        ScopedBindings scoped = Bindings.builder().scoped();
        scoped.addScope("someScope");
        Bindings foreign = Bindings.builder().standard();

        NoSuchBindingsException e = Assertions.assertThrows(NoSuchBindingsException.class,
                () -> scoped.removeScope(foreign));
        Assertions.assertSame(foreign, e.getBindings());
        Assertions.assertNull(e.getName());
    }

    @Test
    public void testRemoveScopeByBindings_popsDownToTarget() {
        ScopedBindings scoped = Bindings.builder().scoped();
        NamedScope target = scoped.addScope("target");
        scoped.addScope("above1");
        scoped.addScope("above2");

        NamedScope removed = scoped.removeScope(target.getBindings());

        Assertions.assertSame(target, removed);
        // Everything above (and including) the target is gone; only root remains.
        Assertions.assertEquals(1, scoped.getScopeSize());
        Assertions.assertNull(scoped.getScope("above1"));
        Assertions.assertNull(scoped.getScope("above2"));
    }

    @Test
    public void testRemoveScopeByNamedScope() {
        ScopedBindings scoped = Bindings.builder().scoped();
        NamedScope target = scoped.addScope("target");
        scoped.addScope("above");

        scoped.removeScope(target);
        Assertions.assertEquals(1, scoped.getScopeSize());

        // A scope that was never part of this stack cannot be removed.
        ScopedBindings other = Bindings.builder().scoped();
        NamedScope foreignScope = other.addScope("foreign");
        NoSuchScopeException e = Assertions.assertThrows(NoSuchScopeException.class,
                () -> scoped.removeScope(foreignScope));
        Assertions.assertSame(foreignScope, e.getScope());
    }

    @Test
    public void testRemoveScopeByName_popsScopesAboveIt() {
        ScopedBindings scoped = Bindings.builder().scoped();
        scoped.addScope("first");
        scoped.addScope("second");

        NamedScope removed = scoped.removeScope("first");
        Assertions.assertEquals("first", removed.getName());
        Assertions.assertEquals(1, scoped.getScopeSize());
    }

    @Test
    public void testGetParentScope() {
        ScopedBindings scoped = Bindings.builder().scoped();
        // Root only — no parent.
        Assertions.assertNull(scoped.getParentScope());

        NamedScope first = scoped.addScope("first");
        scoped.addScope("second");
        // Parent of the current ("second") scope is "first".
        Assertions.assertSame(first, scoped.getParentScope());
    }

    @Test
    public void testGetGlobalScope() {
        ScopedBindings scoped = Bindings.builder().scoped();
        Assertions.assertNull(scoped.getGlobalScope());

        NamedScope global = scoped.addScope(ScopedBindings.GLOBAL_SCOPE);
        Assertions.assertSame(global, scoped.getGlobalScope());
        Assertions.assertNotNull(scoped.getRootScope());
    }

    // =========================================================================
    // DefaultScopedBindings — listeners, map view, printing
    // =========================================================================

    @Test
    public void testBindingListeners_addAndRemove() {
        ScopedBindings scoped = Bindings.builder().scoped();
        List<String> events = new ArrayList<>();

        BindingListener listener = new BindingListener() {
            @Override
            public void onBind(Binding<?> binding) {
                events.add("bind:" + binding.getName());
            }

            @Override
            public void onScopeAdd(NamedScope scope) {
                events.add("scopeAdd:" + scope.getName());
            }

            @Override
            public void onScopeRemove(NamedScope scope) {
                events.add("scopeRemove:" + scope.getName());
            }
        };

        scoped.addBindingListener(listener);
        scoped.addScope("listenerScope");
        scoped.bind(x -> 1);
        scoped.removeScope();

        Assertions.assertTrue(events.contains("scopeAdd:listenerScope"));
        Assertions.assertTrue(events.contains("bind:x"));
        Assertions.assertTrue(events.contains("scopeRemove:listenerScope"));

        // Removing an attached listener returns true; a stranger returns false.
        Assertions.assertTrue(scoped.removeBindingListener(listener));
        Assertions.assertFalse(scoped.removeBindingListener(new BindingListener() {}));
    }

    @Test
    public void testGetNamesAndUniqueSize_shadowedNameCountsOnce() {
        ScopedBindings scoped = Bindings.builder().scoped();
        scoped.bind("a", 1);
        scoped.bind("b", 2);
        scoped.addScope("inner");
        scoped.bind("a", 100);   // shadows root "a"

        Set<String> names = scoped.getNames();
        Assertions.assertEquals(Set.of("a", "b"), names);
        Assertions.assertEquals(2, scoped.uniqueSize());
        Assertions.assertEquals(3, scoped.size());
        // Innermost scope wins for shadowed names.
        Assertions.assertEquals(100, (Integer) scoped.getValue("a"));
    }

    @Test
    public void testPutAll() {
        ScopedBindings scoped = Bindings.builder().scoped();
        scoped.bind("existing", 1);

        scoped.asMap().putAll(Map.of("existing", 42, "brandNew", "hello"));

        Assertions.assertEquals(42, (Integer) scoped.getValue("existing"));
        Assertions.assertEquals("hello", scoped.getValue("brandNew"));
    }

    @Test
    public void testPrettyPrint() {
        ScopedBindings scoped = Bindings.builder().scoped();
        scoped.bind("a", 1);
        scoped.addScope("inner");
        scoped.bind("b", 2);

        String text = scoped.prettyPrint("  ");
        Assertions.assertTrue(text.contains("Scope (index = 0)"));
        Assertions.assertTrue(text.contains("Scope (index = 1)"));
    }

    // =========================================================================
    // ImmutableScopedBindings
    // =========================================================================

    private static ScopedBindings populatedScoped() {
        ScopedBindings scoped = Bindings.builder().scoped();
        scoped.bind("a", 1);
        scoped.addScope("inner");
        scoped.bind("b", 2);
        return scoped;
    }

    @Test
    public void testImmutableScoped_mutatorsThrow() {
        ScopedBindings immutable = populatedScoped().asImmutable();

        Assertions.assertThrows(UnsupportedOperationException.class, () -> immutable.bind("c", 3));
        Assertions.assertThrows(UnsupportedOperationException.class, immutable::addScope);
        Assertions.assertThrows(UnsupportedOperationException.class, () -> immutable.addScope("x"));
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> immutable.addScope("x", Bindings.builder().standard()));
        Assertions.assertThrows(UnsupportedOperationException.class, immutable::removeScope);
        Assertions.assertThrows(UnsupportedOperationException.class, () -> immutable.removeScope("inner"));
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> immutable.removeScope(immutable.getCurrentScope()));
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> immutable.removeScope(immutable.getCurrentBindings()));
        Assertions.assertThrows(UnsupportedOperationException.class, () -> immutable.asMap().put("c", 3));
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> immutable.asMap().putAll(Map.of("c", 3)));
        Assertions.assertThrows(UnsupportedOperationException.class, () -> immutable.asMap().remove("a"));
        Assertions.assertThrows(UnsupportedOperationException.class, () -> immutable.asMap().clear());
    }

    @Test
    public void testImmutableScoped_readsDelegate() {
        ScopedBindings scoped = populatedScoped();
        ScopedBindings immutable = scoped.asImmutable();

        Assertions.assertEquals(scoped.size(), immutable.size());
        Assertions.assertEquals(scoped.getNames(), immutable.getNames());
        Assertions.assertEquals(scoped.getScopeSize(), immutable.getScopeSize());
        Assertions.assertEquals(scoped.uniqueSize(), immutable.uniqueSize());
        Assertions.assertFalse(immutable.asMap().isEmpty());
        Assertions.assertSame(scoped.getCurrentScope(), immutable.getCurrentScope());
        Assertions.assertSame(scoped.getRootScope(), immutable.getRootScope());
        Assertions.assertSame(scoped.getParentScope(), immutable.getParentScope());
        Assertions.assertNull(immutable.getGlobalScope());
        Assertions.assertNotNull(immutable.getScope("inner"));
        Assertions.assertEquals("inner", immutable.getScopeName(scoped.getScope("inner").getBindings()));
        Assertions.assertEquals(scoped.prettyPrint("  "), immutable.prettyPrint("  "));
        Assertions.assertEquals(scoped.toString(), immutable.toString());
        // asImmutable() on an immutable wrapper returns itself.
        Assertions.assertSame(immutable, immutable.asImmutable());
    }

    @Test
    public void testImmutableScoped_bindingsComeBackImmutable() {
        ScopedBindings immutable = populatedScoped().asImmutable();

        Assertions.assertInstanceOf(ImmutableBinding.class, immutable.getBinding("a"));
        Assertions.assertNull(immutable.getBinding("noSuch"));
        Assertions.assertInstanceOf(ImmutableBinding.class, immutable.getBinding("a", Integer.class));
        Assertions.assertNull(immutable.getBinding("a", String.class));

        for (Binding<?> binding : immutable) {
            Assertions.assertInstanceOf(ImmutableBinding.class, binding);
        }

        List<Binding<Object>> byName = immutable.getAllBindings("a");
        Assertions.assertEquals(1, byName.size());
        Assertions.assertInstanceOf(ImmutableBinding.class, byName.get(0));

        List<Binding<Integer>> byType = immutable.getAllBindings(Integer.class);
        Assertions.assertEquals(2, byType.size());

        Bindings innerScope = immutable.getScopeBindings("inner");
        Assertions.assertNotNull(innerScope);
        Assertions.assertNull(immutable.getScopeBindings("noSuchScope"));
        Assertions.assertNotNull(immutable.getCurrentBindings());
    }

    @Test
    public void testImmutableScoped_mapView() {
        ScopedBindings immutable = populatedScoped().asImmutable();
        Map<String, Object> map = immutable.asMap();

        Assertions.assertTrue(map.containsKey("a"));
        Assertions.assertFalse(map.containsKey("zzz"));
        Assertions.assertTrue(map.containsValue(2));
        Assertions.assertEquals(1, map.get("a"));
        Assertions.assertNull(map.get("zzz"));
        Assertions.assertEquals(Set.of("a", "b"), map.keySet());
        Assertions.assertEquals(2, map.values().size());
        Assertions.assertEquals(2, map.entrySet().size());
    }

    @Test
    public void testImmutableScoped_listenerCallsDelegateToTarget() {
        ScopedBindings scoped = populatedScoped();
        ScopedBindings immutable = scoped.asImmutable();

        // Unlike ImmutableBindings (which throws), the scoped immutable wrapper delegates
        // listener registration to the live target.
        BindingListener listener = new BindingListener() {};
        immutable.addBindingListener(listener);
        Assertions.assertTrue(immutable.removeBindingListener(listener));
    }
}
