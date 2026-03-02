/*
 * This software is licensed under the Apache 2 license, quoted below.
 *
 * Copyright (c) 1999-2025, Algorithmx Inc.
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
package org.rulii.script;

import org.rulii.bind.Binding;
import org.rulii.bind.Bindings;
import org.rulii.lib.spring.util.Assert;

import java.util.*;

/**
 * A {@link javax.script.Bindings} implementation that delegates all Map operations to an
 * underlying {@link Bindings} instance.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class DelegatingBindings implements javax.script.Bindings {

    private final Bindings target;

    public DelegatingBindings(Bindings target) {
        super();
        Assert.notNull(target, "target cannot be null.");
        this.target = target;
    }

    protected Bindings getTarget() {
        return target;
    }

    @Override
    public Object put(String name, Object value) {
        Assert.hasText(name, "name cannot be null or empty.");
        Binding<Object> binding = getTarget().getBinding(name);
        Object previous = null;

        if (binding == null) {
            getTarget().bind(name, value);
        } else {
            previous = binding.getValue();
            binding.setValue(value);
        }

        return previous;
    }

    @Override
    public void putAll(Map<? extends String, ?> toMerge) {
        Assert.notNull(toMerge, "toMerge cannot be null.");

        for (Entry<? extends String, ?> entry : toMerge.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public boolean containsKey(Object key) {
        Assert.notNull(key, "key cannot be null.");
        return getTarget().contains(key.toString());
    }

    @Override
    public Object get(Object key) {
        Assert.notNull(key, "key cannot be null.");
        return getTarget().getValue(key.toString());
    }

    @Override
    public Object remove(Object key) {
        throw new UnsupportedOperationException("remove is not supported.");
    }

    @Override
    public int size() {
        return getTarget().size();
    }

    @Override
    public boolean isEmpty() {
        return getTarget().isEmpty();
    }

    @Override
    public boolean containsValue(Object value) {
        for (Binding<?> binding : getTarget()) {
            if (Objects.equals(binding.getValue(), value)) return true;
        }
        return false;
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("clear is not supported.");
    }

    @Override
    public Set<String> keySet() {
        return getTarget().getNames();
    }

    @Override
    public Collection<Object> values() {
        List<Object> result = new ArrayList<>();
        for (Binding<?> binding : getTarget()) {
            result.add(binding.getValue());
        }
        return result;
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        Set<Entry<String, Object>> result = new LinkedHashSet<>();
        for (Binding<?> binding : getTarget()) {
            result.add(new AbstractMap.SimpleImmutableEntry<>(binding.getName(), binding.getValue()));
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!Bindings.class.isAssignableFrom(o.getClass())) return false;
        Bindings other = (Bindings) o;
        return getTarget().asMap().equals(other.asMap());
    }

    @Override
    public int hashCode() {
        return Objects.hash(target);
    }

    @Override
    public String toString() {
        return "DelegatingBindings(" + getTarget() + ")";
    }
}
