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

import org.rulii.bind.load.BindingLoader;
import org.rulii.lib.spring.util.Assert;
import org.rulii.util.TypeReference;

import java.lang.reflect.Type;
import java.util.*;

/**
 * Immutable version of the Bindings. All functions that create Bindings will be disabled.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class ImmutableBindings implements Bindings, Map<String, Object> {

    private final Bindings target;

    ImmutableBindings(Bindings target) {
        super();
        Assert.notNull(target, "target cannot be null.");
        this.target = target;
    }

    @Override
    public int size() {
        return getTarget().size();
    }

    @Override
    public boolean contains(String name) {
        return getTarget().contains(name);
    }

    @Override
    public <T> boolean contains(String name, Class<T> type) {
        return getTarget().contains(name, type);
    }

    @Override
    public <T> boolean contains(String name, TypeReference<T> type) {
        return getTarget().contains(name, type);
    }

    @Override
    public <T> Binding<T> getBinding(String name) {
        Binding<T> result = getTarget().getBinding(name);
        return result != null ? result.asImmutable() : null;
    }

    @Override
    public <T> T getValue(String name) {
        return getTarget().getValue(name);
    }

    @Override
    public <T> T setValue(String name, T value) {
        throw new UnsupportedOperationException("Binding [" + name + "] is immutable. It cannot be edited.");
    }

    @Override
    public <T> T setValueOrBind(String name, T value) throws NoSuchBindingException, InvalidBindingException {
        throw new UnsupportedOperationException("Binding [" + name + "] is immutable. It cannot be edited.");
    }

    @Override
    public <T> Binding<T> getBinding(String name, Type type) {
        Binding<T> result = getTarget().getBinding(name, type);
        return result != null ? result.asImmutable() : null;
    }

    @Override
    public <T> List<Binding<T>> getBindings(Type type) {
        return convertToImmutableList(getTarget().getBindings(type));
    }

    private <T> List<Binding<T>> convertToImmutableList(List<Binding<T>> original) {
        if (original == null) return null;
        List<Binding<T>> result = new LinkedList<>();

        for (Binding<T> entry : original) {
            result.add(entry.asImmutable());
        }

        return result;
    }

    @Override
    public Set<String> getNames() {
        return target.getNames();
    }

    @Override
    public Iterator<Binding<?>> iterator() {
        return getTarget().iterator();
    }

    protected Bindings getTarget() {
        return target;
    }

    @Override
    public final <T> void bind(Binding<T> binding) throws BindingAlreadyExistsException {
        throw new UnsupportedOperationException("Bindings are immutable.");
    }

    @Override
    public final List<Binding<?>> bind(BindingDeclaration<?>...declarations) {
        throw new UnsupportedOperationException("Bindings are immutable.");
    }

    @Override
    public final <T> Binding<T> bind(BindingDeclaration<T> declaration) {
        throw new UnsupportedOperationException("Bindings are immutable.");
    }

    @Override
    public final <T> Binding<T> bind(String name, T initialValue) {
        throw new UnsupportedOperationException("Bindings are immutable.");
    }

    @Override
    public final <T> Binding<T> bind(String name, Type type) {
        throw new UnsupportedOperationException("Bindings are immutable.");
    }

    @Override
    public final <T> Binding<T> bind(String name, TypeReference<T> type) {
        throw new UnsupportedOperationException("Bindings are immutable.");
    }

    @Override
    public final <T> Binding<T> bind(String name, Type type, T initialValue) {
        throw new UnsupportedOperationException("Bindings are immutable.");
    }

    @Override
    public final <T> Binding<T> bind(String name, TypeReference<T> type, T initialValue) {
        throw new UnsupportedOperationException("Bindings are immutable.");
    }

    @Override
    public final <T> void load(BindingLoader<T> loader, T value) {
        throw new UnsupportedOperationException("Bindings are immutable.");
    }

    @Override
    public ImmutableBindings asImmutable() {
        return this;
    }

    @Override
    public void addBindingListener(BindingListener listener) {
        throw new UnsupportedOperationException("Bindings are immutable.");
    }

    @Override
    public boolean removeBindingListener(BindingListener listener) {
        throw new UnsupportedOperationException("Bindings are immutable.");
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
        return size() == 0;
    }

    @Override
    public Set<String> keySet() {
        Set<String> result = new LinkedHashSet<>();

        for (Binding<?> binding : this) {
            result.add(binding.getName());
        }

        return result;
    }

    @Override
    public Collection<Object> values() {
        Set<Object> result = new LinkedHashSet<>();

        for (Binding<?> binding : this) {
            result.add(binding.getValue());
        }

        return result;
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        Set<Entry<String, Object>> result = new LinkedHashSet<>();

        for (Binding<?> binding : this) {
            result.add(new AbstractMap.SimpleEntry<>(binding.getName(), binding.getValue()));
        }

        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!Bindings.class.isAssignableFrom(o.getClass())) return false;
        return target.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(target);
    }

    @Override
    public String toString() {
        return target.toString();
    }

    @Override
    public String prettyPrint(String prefix) {
        return getTarget().prettyPrint(prefix);
    }
}
