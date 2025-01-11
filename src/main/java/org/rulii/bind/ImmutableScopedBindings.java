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
package org.rulii.bind;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Immutable version of the ScopedBindings. All functions that create Bindings/Scopes will be disabled.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class ImmutableScopedBindings extends ImmutableBindings implements ScopedBindings {

    ImmutableScopedBindings(ScopedBindings bindings) {
        super(bindings);
    }

    @Override
    protected ScopedBindings getTarget() {
        return (ScopedBindings) super.getTarget();
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
}
