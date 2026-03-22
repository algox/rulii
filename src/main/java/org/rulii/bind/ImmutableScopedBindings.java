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
import java.util.*;

/**
 * Immutable version of the ScopedBindings. All functions that create Bindings/Scopes will be disabled.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class ImmutableScopedBindings implements ScopedBindings, Map<String, Object> {

    private final ScopedBindings target;

    ImmutableScopedBindings(ScopedBindings bindings) {
        super();
        this.target = bindings;
    }

    protected ScopedBindings getTarget() {
        return target;
    }

    @Override
    public <T> void bind(Binding<T> binding) {
        throw new UnsupportedOperationException("Bindings are immutable.");
    }

    @Override
    public <T> Binding<T> getBinding(String name) {
        return getTarget().getBinding(name);
    }

    @Override
    public <T> Binding<T> getBinding(String name, Type type) {
        return getTarget().getBinding(name, type);
    }

    @Override
    public <T> List<Binding<T>> getBindings(Type type) {
        return getTarget().getBindings(type);
    }

    @Override
    public int size() {
        return getTarget().size();
    }

    @Override
    public void addBindingListener(BindingListener listener) {
        getTarget().addBindingListener(listener);
    }

    @Override
    public boolean removeBindingListener(BindingListener listener) {
        return getTarget().removeBindingListener(listener);
    }

    @Override
    public Set<String> getNames() {
        return getTarget().getNames();
    }

    @Override
    public Iterator<Binding<?>> iterator() {
        return getTarget().iterator();
    }

    @Override
    public Bindings getCurrentBindings() {
        return getTarget().getCurrentBindings();
    }

    @Override
    public NamedScope getCurrentScope() {
        return getTarget().getCurrentScope();
    }

    @Override
    public NamedScope addScope() {
        throw new UnsupportedOperationException("Bindings are immutable.");
    }

    @Override
    public NamedScope addScope(String name) {
        throw new UnsupportedOperationException("Bindings are immutable.");
    }

    @Override
    public NamedScope addScope(String name, Bindings bindings) {
        throw new UnsupportedOperationException("Bindings are immutable.");
    }

    @Override
    public NamedScope getScope(String name) {
        return getTarget().getScope(name);
    }

    @Override
    public Bindings getScopeBindings(String name) {
        return getTarget().getScopeBindings(name);
    }

    @Override
    public String getScopeName(Bindings bindings) {
        return getTarget().getScopeName(bindings);
    }

    @Override
    public NamedScope removeScope(String name) {
        throw new UnsupportedOperationException("Bindings are immutable.");
    }

    @Override
    public NamedScope removeScope() {
        throw new UnsupportedOperationException("Bindings are immutable.");
    }

    @Override
    public NamedScope removeScope(Bindings target) { throw new UnsupportedOperationException("Bindings are immutable."); }

    @Override
    public void removeScope(NamedScope scope) {
        throw new UnsupportedOperationException("Bindings are immutable.");
    }

    @Override
    public NamedScope getParentScope() {
        return getTarget().getParentScope();
    }

    @Override
    public int getScopeSize() {
        return getTarget().getScopeSize();
    }

    @Override
    public <T> List<Binding<T>> getAllBindings(Type type) {
        return getTarget().getAllBindings(type);
    }

    @Override
    public <T> List<Binding<T>> getAllBindings(String name) {
        return getTarget().getAllBindings(name);
    }

    @Override
    public String toString() {
        return getTarget().toString();
    }

    @Override
    public NamedScope getRootScope() {
        return getTarget().getRootScope();
    }

    @Override
    public NamedScope getGlobalScope() {
        return getTarget().getGlobalScope();
    }

    @Override
    public String prettyPrint(String prefix) {
        return getTarget().prettyPrint(prefix);
    }

    @Override
    public ImmutableScopedBindings asImmutable() {
        return this;
    }

    @Override
    public Map<String, Object> asMap() {
        return this;
    }

    @Override
    public boolean containsKey(Object key) {
        Assert.notNull(key, "key cannot be null.");
        return contains(key.toString());
    }

    @Override
    public boolean containsValue(Object value) {
        for (Binding<?> binding : this) {
            if (Objects.equals(binding.getValue(), value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object get(Object key) {
        Assert.notNull(key, "key cannot be null.");
        Binding<Object> result = getBinding(key.toString());
        return result != null ? result.getValue() : null;
    }

    @Override
    public Object put(String key, Object value) {
        throw new UnsupportedOperationException("Bindings are immutable.");
    }

    @Override
    public Object remove(Object key) {
        throw new UnsupportedOperationException("Bindings does not support removal of values. Use setValue() instead.");
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        throw new UnsupportedOperationException("Bindings are immutable.");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Bindings cannot be cleared.");
    }

    @Override
    public boolean isEmpty() {
        return getTarget().isEmpty();
    }

    @Override
    public Set<String> keySet() {
        Map<String, Binding<?>> result = new LinkedHashMap<>();

        for (Binding<?> binding : this) {
            result.putIfAbsent(binding.getName(), binding);
        }

        return result.keySet();
    }

    @Override
    public Collection<Object> values() {
        Map<String, Object> result = new LinkedHashMap<>();

        for (Binding<?> binding : this) {
            result.putIfAbsent(binding.getName(), binding.getValue());
        }

        return result.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        Map<String, Entry<String, Object>> result = new LinkedHashMap<>();

        for (Binding<?> binding : this) {
            result.putIfAbsent(binding.getName(), new AbstractMap.SimpleEntry<>(binding.getName(), binding.getValue()));
        }

        return new LinkedHashSet<>(result.values());
    }

    @Override
    public int uniqueSize() {
        return keySet().size();
    }
}
