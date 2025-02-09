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

/**
 * Bindings builder.
 *
 * @author Max Arulananthan
 * @since 1.0
 * @see Bindings
 *
 */
public final class BindingsBuilder {

    private static final BindingsBuilder instance = new BindingsBuilder();

    private BindingsBuilder() {
        super();
    }

    public static BindingsBuilder getInstance() {
        return instance;
    }

    /**
     * Create Default Bindings.
     *
     * @return Default Bindings.
     */
    public Bindings standard() {
        return new DefaultBindings();
    }

    /**
     * Create Default Bindings with some initial Bindings.
     *
     * @param bindings initial bindings.
     * @return Default Bindings.
     */
    public Bindings standard(BindingDeclaration<?>...bindings) {
        Bindings result = standard();
        if (bindings != null) result.bind(bindings);
        return result;
    }

    /**
     * Create Scoped Bindings.
     *
     * @return Scoped Bindings.
     */
    public ScopedBindings scoped() {
        return new DefaultScopedBindings(ScopedBindings.ROOT_SCOPE);
    }

    /**
     * Create Scoped Bindings with initial Bindings.
     *
     * @param bindings initial bindings.
     * @return Scoped Bindings.
     */
    public ScopedBindings scoped(BindingDeclaration<?>...bindings) {
        ScopedBindings result = new DefaultScopedBindings(ScopedBindings.ROOT_SCOPE);
        result.bind(bindings);
        return result;
    }

    /**
     * Create Scoped Bindings with the given name.
     *
     * @param name bindings name.
     * @return Scoped Bindings.
     */
    public ScopedBindings scoped(String name) {
        return new DefaultScopedBindings(name);
    }

    /**
     * Create Scoped Bindings with the given name.
     *
     * @param name bindings name.
     * @param bindings initial bindings.
     * @return Scoped Bindings.
     */
    public ScopedBindings scoped(String name, Bindings bindings) {
        return new DefaultScopedBindings(name, bindings);
    }

    /**
     * Create Scoped Bindings with the given name and set of initial declarations.
     *
     * @param name bindings name.
     * @param bindings initial bindings.
     * @return Scoped Bindings.
     */
    public ScopedBindings scoped(String name, BindingDeclaration<?>...bindings) {
        ScopedBindings result = new DefaultScopedBindings(name);
        result.bind(bindings);
        return result;
    }

    /**
     * Creates an immutable Bindings with initial bindings.
     *
     * @param bindings initial bindings.
     * @return immutable bindings.
     */
    public ImmutableBindings immutable(Bindings bindings) {
        return (ImmutableBindings) bindings.asImmutable();
    }

    /**
     * Creates an immutable Bindings with initial bindings.
     *
     * @param bindings initial bindings.
     * @return immutable bindings.
     */
    public ImmutableBindings immutable(BindingDeclaration<?>...bindings) {
        return (ImmutableBindings) standard(bindings).asImmutable();
    }

    /**
     * Creates an immutable Bindings with initial bindings.
     *
     * @param bindings initial bindings.
     * @return immutable bindings.
     */
    public ImmutableScopedBindings immutable(ScopedBindings bindings) {
        return bindings.asImmutable();
    }
}
