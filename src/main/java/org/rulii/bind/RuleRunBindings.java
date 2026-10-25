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
package org.rulii.bind;

import org.rulii.lib.spring.util.Assert;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The bindings a rule run sees when the rule asked for {@link LocalBindings}: a view over the caller's live
 * {@link ScopedBindings} with one virtual entry on top, {@code localBindings}, and nothing else of its own.
 * Every bind, scope push and pop, listener and fork goes to the caller's stack, so a rule's actions still bind
 * their outputs where the caller reads them; only the lookup of {@code localBindings} is answered here.
 *
 * <p>The entry answers to its name, and to its exact type {@link LocalBindings}; a lookup by the type
 * {@link Bindings} never returns it, so a {@code Bindings} parameter keeps resolving to the caller's stack.
 * A view over a view (a rule run from within a rule's action) answers with its own entry: the innermost wins.
 *
 * <p>Created by {@code RuleContext.withLocalBindings()} for one run and dropped with it.
 *
 * @author Max Arulananthan
 * @since 2.2
 */
public final class RuleRunBindings implements ScopedBindings {

    private final ScopedBindings delegate;
    private final Binding<LocalBindings> local;

    public RuleRunBindings(ScopedBindings delegate, LocalBindings localBindings) {
        super();
        Assert.notNull(delegate, "delegate cannot be null.");
        Assert.notNull(localBindings, "localBindings cannot be null.");
        this.delegate = delegate;
        // Already immutable: the Binding never changes, so an immutable copy of the stack hands out this very
        // Binding instead of copying it per condition. The LocalBindings it holds stays live (it is never made
        // immutable, see LocalBindings#asImmutable).
        this.local = new ImmutableBinding<>(Binding.builder().with(ReservedBindings.LOCAL_BINDINGS.getName())
                .type(LocalBindings.class)
                .isFinal(true)
                .value(localBindings)
                .build());
    }

    /**
     * The caller's bindings this view sits on.
     *
     * @return the delegate.
     */
    public ScopedBindings getDelegate() {
        return delegate;
    }

    /**
     * The local bindings of this run.
     *
     * @return the local bindings.
     */
    public LocalBindings getLocalBindings() {
        return local.getValue();
    }

    private boolean isLocal(String name) {
        return local.getName().equals(name);
    }

    private boolean isLocal(Type type) {
        return LocalBindings.class.equals(type);
    }

    // --- lookups: the one place this view answers for itself ----------------------------------------

    @Override
    @SuppressWarnings("unchecked")
    public <T> Binding<T> getBinding(String name) {
        Assert.notNull(name, "name cannot be null.");
        return isLocal(name) ? (Binding<T>) local : delegate.getBinding(name);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Binding<T> getBinding(String name, Type type) {
        Assert.notNull(name, "name cannot be null.");
        Assert.notNull(type, "type cannot be null.");
        if (isLocal(name)) return local.isAssignable(type) ? (Binding<T>) local : null;
        return delegate.getBinding(name, type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<Binding<T>> getBindings(Type type) {
        Assert.notNull(type, "type cannot be null.");
        return isLocal(type) ? List.of((Binding<T>) local) : delegate.getBindings(type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<Binding<T>> getAllBindings(Type type) {
        Assert.notNull(type, "type cannot be null.");
        return isLocal(type) ? List.of((Binding<T>) local) : delegate.getAllBindings(type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<Binding<T>> getAllBindings(String name) {
        Assert.notNull(name, "name cannot be null.");
        return isLocal(name) ? List.of((Binding<T>) local) : delegate.getAllBindings(name);
    }

    // --- views that include the entry ----------------------------------------------------------------

    @Override
    public int size() {
        return delegate.size() + 1;
    }

    @Override
    public Set<String> getNames() {
        Set<String> result = new LinkedHashSet<>(delegate.getNames());
        result.add(local.getName());
        return Collections.unmodifiableSet(result);
    }

    @Override
    public Iterator<Binding<?>> iterator() {
        List<Binding<?>> result = new ArrayList<>();
        for (Binding<?> binding : delegate) {
            result.add(binding);
        }
        result.add(local);
        return result.iterator();
    }

    /**
     * The values, caller's first; a snapshot, unlike the live map of a {@link DefaultScopedBindings}.
     */
    @Override
    public Map<String, Object> asMap() {
        Map<String, Object> result = new LinkedHashMap<>(delegate.asMap());
        result.put(local.getName(), local.getValue());
        return Collections.unmodifiableMap(result);
    }

    @Override
    public String prettyPrint(String prefix) {
        return delegate.prettyPrint(prefix) + prefix + local.getName() + " = " + local.getValue();
    }

    // --- everything else is the caller's -------------------------------------------------------------

    @Override
    public <T> void bind(Binding<T> binding) {
        delegate.bind(binding);
    }

    @Override
    public void addBindingListener(BindingListener listener) {
        delegate.addBindingListener(listener);
    }

    @Override
    public boolean removeBindingListener(BindingListener listener) {
        return delegate.removeBindingListener(listener);
    }

    @Override
    public Bindings getCurrentBindings() {
        return delegate.getCurrentBindings();
    }

    @Override
    public NamedScope getCurrentScope() {
        return delegate.getCurrentScope();
    }

    @Override
    public NamedScope getParentScope() {
        return delegate.getParentScope();
    }

    @Override
    public NamedScope getRootScope() {
        return delegate.getRootScope();
    }

    @Override
    public NamedScope getGlobalScope() {
        return delegate.getGlobalScope();
    }

    @Override
    public NamedScope addScope() {
        return delegate.addScope();
    }

    @Override
    public NamedScope addScope(String name) {
        return delegate.addScope(name);
    }

    @Override
    public NamedScope addScope(String name, Bindings bindings) {
        return delegate.addScope(name, bindings);
    }

    @Override
    public NamedScope getScope(String name) {
        return delegate.getScope(name);
    }

    @Override
    public Bindings getScopeBindings(String name) {
        return delegate.getScopeBindings(name);
    }

    @Override
    public String getScopeName(Bindings bindings) {
        return delegate.getScopeName(bindings);
    }

    @Override
    public NamedScope removeScope() {
        return delegate.removeScope();
    }

    @Override
    public NamedScope removeScope(String name) {
        return delegate.removeScope(name);
    }

    @Override
    public NamedScope removeScope(Bindings target) {
        return delegate.removeScope(target);
    }

    @Override
    public void removeScope(NamedScope scope) {
        delegate.removeScope(scope);
    }

    @Override
    public int getScopeSize() {
        return delegate.getScopeSize();
    }

    /**
     * Forks the caller's bindings. The local bindings belong to this run on this thread and are not carried
     * into the fork.
     */
    @Override
    public ScopedBindings fork() {
        return delegate.fork();
    }

    @Override
    public String toString() {
        return "RuleRunBindings(" + delegate + ")";
    }
}
