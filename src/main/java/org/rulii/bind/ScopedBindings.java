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
import java.util.Map;

/**
 * Bindings with Scopes. Allows the user to add/remove scopes around the Bindings. Each Binding is tied to a
 * Scope and Binding is removed once the Scope is removed. Binding is treated much like a Local variable on a method stack.
 *
 * @author Max Arulananthan
 * @since 1.0
 * @see Binding
 * @see Bindings
 */
public interface ScopedBindings extends Bindings {

    String ROOT_SCOPE   = "root-scope";
    String GLOBAL_SCOPE = "global-scope";

    /**
     * Returns the current working scope bindings.
     *
     * @return working scope bindings.
     */
    Bindings getCurrentBindings();

    /**
     * Retrieves the current named scope.
     *
     * @return the current named scope.
     */
    NamedScope getCurrentScope();

    /**
     * Retrieves the parent of the current scope.
     *
     * @return parent scope; if current scope is the root scope then null.
     */
    NamedScope getParentScope();

    /**
     * Returns the root scope.
     *
     * @return root scope.
     */
    NamedScope getRootScope();

    /**
     * Global Binding Scope is just below the Root Scope in the stack.
     *
     * @return global scope.
     */
    NamedScope getGlobalScope();

    /**
     * Creates a new scope with a generated name.
     *
     * @return added scope.
     */
    NamedScope addScope();

    /**
     * Creates new scope and pushes it to the top of Stack.
     *
     * @param name name of the scope.
     * @return the newly created scope.
     */
    NamedScope addScope(String name);

    /**
     * Creates a new scope with the given name and initial Bindings.
     *
     * @param name scope name.
     * @param bindings initial bindings.
     */
    NamedScope addScope(String name, Bindings bindings);

    /**
     * Retrieves the named scope with the given name.
     *
     * @param name the name of the scope
     * @return the named scope if found, otherwise null
     */
    NamedScope getScope(String name);

    /**
     * Retrieves scope with the desired name.
     *
     * @param name scope name.
     * @return scope if found; null otherwise.
     */
    Bindings getScopeBindings(String name);

    /**
     * Determines whether a scope exists with the given name.
     *
     * @param name scope name
     * @return true if scope exists; false otherwise.
     */
    default boolean containsScope(String name) {
        return getScope(name) != null;
    }

    /**
     * Retrieves the scope name given the actual bindings instance.
     *
     * @param bindings bindings.
     * @return scope name if found; null otherwise.
     */
    String getScopeName(Bindings bindings);

    /**
     * Pops the working Bindings off the Stack.
     *
     * @return the removed Bindings.
     */
    NamedScope removeScope();

    /**
     * Removes the scope with the given name.
     *
     * @param name scope name.
     * @return removed Bindings if a match was found; null otherwise.
     */
    NamedScope removeScope(String name);

    /**
     * Pops the working Bindings off the Stack till we match our desired target.
     *
     * @param target removes all scopes including target and above.
     * @return the removed Scope.
     */
    NamedScope removeScope(Bindings target);

    /**
     * Pops the working Scope off the Stack till we match our desired target.
     *
     * @param scope removes all scopes including target and above.
     */
    void removeScope(NamedScope scope);

    /**
     * Binds the given Binding into the current scope. Follows the same rules as adding a new Binding with name, type, etc.
     *
     * @param binding existing Binding.
     * @param <T> generic type of the Binding.
     * @throws BindingAlreadyExistsException thrown if the Binding already exists.
     */
    @Override
    <T> void bind(Binding<T> binding);

    /**
     * Retrieves the Binding identified by the given name. The search starts with working scope and goes back the Stack
     * until the initial scope. The search stops once a match is found.
     *
     * @param name name of the Binding.
     * @param <T> generic type of the Binding.
     * @return Binding if found; null otherwise.
     */
    @Override
    <T> Binding<T> getBinding(String name);

    /**
     * Retrieves the Binding identified by the given name and type. The search starts with working scope and goes back the Stack
     * until the initial scope. The search stops once a match is found.
     *
     * @param name name of the Binding.
     * @param type type of the Binding.
     * @param <T> generic type of the Binding.
     * @return Binding if found; null otherwise.
     */
    @Override
    <T> Binding<T> getBinding(String name, Type type);

    /**
     * Retrieves all the Bindings of the given type. The search starts with working scope and goes back the Stack
     * until a match is found.
     *
     * @param type desired type.
     * @param <T> generic type of the Binding.
     * @return all matching Bindings.
     */
    @Override
    <T> List<Binding<T>> getBindings(Type type);

    /**
     * Retrieves all the Bindings of the given type. The search starts with working scope and goes back the Stack
     * until the initial scope.
     *
     * @param type desired type.
     * @param <T> generic type of the Binding.
     * @return all matching Bindings.
     */
    <T> List<Binding<T>> getAllBindings(Type type);

    /**
     * Retrieves all the Bindings with the given name. The search starts with working scope and goes back the Stack
     * until the initial scope.
     *
     * @param name desired name.
     * @param <T> generic type of the Binding.
     * @return all matching Bindings.
     */
    <T> List<Binding<T>> getAllBindings(String name);

    /**
     * Retrieves the Binding values as an Unmodifiable Map. The retrieval starts with working scope and goes back the Stack
     * until the initial scope.
     *
     * @return unmodifiable Map of the Binding values.
     */
    @Override
    Map<String, ?> asMap();

    /**
     * Retrieves the number of Bindings in all the scopes. All Bindings are accounted for (does not account for unique names).
     * Use uniqueSize() for unique count.
     *
     * @return total number of Bindings (in all Scopes).
     */
    @Override
    int size();

    /**
     * Number of scopes.
     *
     * @return number of total scopes.
     */
    int getScopeSize();

    /**
     * Retrieves the number of unique (by name) Bindings in all the scopes.
     *
     * @return unique Bindings count.
     */
    default int uniqueSize() {
        return asMap().size();
    }

    /**
     * Returns an immutable version of these Bindings.
     *
     * @return immutable version of this.
     */
    @Override
    default ImmutableScopedBindings asImmutable() {
        return new ImmutableScopedBindings(this);
    }
}
