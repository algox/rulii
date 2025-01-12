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

import org.rulii.bind.load.BindingLoader;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.Immutator;
import org.rulii.util.TypeReference;

import java.lang.reflect.Type;
import java.util.*;

/**
 * The Bindings Container.
 *
 * @author Max Arulananthan
 * @since 1.0
 * @see Binding
 * @see BindingBuilder
 */
public interface Bindings extends Iterable<Binding<?>>, Immutator<Bindings> {

    /**
     * Bindings builder.
     *
     * @return builder.
     */
    static BindingsBuilder builder() {
        return BindingsBuilder.getInstance();
    }

    /**
     * Declares a new Binding given a name, type and an initial value.
     *
     * @param name name of the Binding.
     * @param initialValue initial value of the Binding.
     * @param <T> generic type of the Binding.
     * @return Binding.
     * @throws BindingAlreadyExistsException thrown if the Binding already exists.
     * @throws InvalidBindingException thrown if we cannot set initial value.
     * @see Binding
     */
    default <T> Binding<T> bind(String name, T initialValue) throws BindingAlreadyExistsException, InvalidBindingException {
        Binding<T> result = Binding.builder().build(name, initialValue);
        bind(result);
        return result;
    }

    /**
     * Declares a new Binding given a name, type and an initial value.
     *
     * @param name name of the Binding.
     * @param type type of the Binding.
     * @param <T> generic type of the Binding.
     * @return Binding.
     * @throws BindingAlreadyExistsException thrown if the Binding already exists.
     * @see Binding
     */
    default <T> Binding<T> bind(String name, Type type) throws BindingAlreadyExistsException {
        Binding<T> result = Binding.builder().build(name, type);
        bind(result);
        return result;
    }

    /**
     * Declares a new Binding given a name, type and an initial value.
     *
     * @param name name of the Binding.
     * @param type type reference of the Binding.
     * @param <T> generic type of the Binding.
     * @return Binding.
     * @throws BindingAlreadyExistsException thrown if the Binding already exists.
     * @see Binding
     */
    default <T> Binding<T> bind(String name, TypeReference<T> type) throws BindingAlreadyExistsException {
        Binding<T> result = Binding.builder().build(name, type);
        bind(result);
        return result;
    }

    /**
     * Declares a new Binding given a name, type and an initial value.
     *
     * @param name name of the Binding.
     * @param type type of the Binding.
     * @param initialValue initial value of the Binding.
     * @param <T> generic type of the Binding.
     * @return Binding.
     * @throws BindingAlreadyExistsException thrown if the Binding already exists.
     * @throws InvalidBindingException thrown if we cannot set initial value.
     * @see Binding
     */
    default <T> Binding<T> bind(String name, Type type, T initialValue) throws BindingAlreadyExistsException, InvalidBindingException {
        Binding<T> result = Binding.builder().with(name, type).value(initialValue).build();
        bind(result);
        return result;
    }

    /**
     * Declares a new Binding given a name, type and an initial value.
     *
     * @param name name of the Binding.
     * @param type type reference of the Binding.
     * @param initialValue initial value of the Binding.
     * @param <T> generic type of the Binding.
     * @return Binding.
     * @throws BindingAlreadyExistsException thrown if the Binding already exists.
     * @throws InvalidBindingException thrown if we cannot set initial value.
     * @see Binding
     */
    default <T> Binding<T> bind(String name, TypeReference<T> type, T initialValue) throws BindingAlreadyExistsException, InvalidBindingException {
        Binding<T> result = Binding.builder().with(name, type).value(initialValue).build();
        bind(result);
        return result;
    }

    /**
     * Creates a new Binding using a BindingDeclaration. The type of the Binding will be the type of the value.
     * In case the value is null then the type is Object.class. Note that generics are not available and hence the
     * type that is declared will NOT have any generic type.
     *
     * @param declaration declaration details.
     * @return Binding.
     * @throws BindingAlreadyExistsException thrown if the Binding already exists.
     * @throws InvalidBindingException thrown if we cannot set initial value.
     */
    default <T> Binding<T> bind(BindingDeclaration<T> declaration) throws BindingAlreadyExistsException, InvalidBindingException {
        Assert.notNull(declaration, "declaration cannot be null.");
        Binding<T> result = Binding.builder().with(declaration).build();
        bind(result);
        return result;
    }

    /**
     * Creates Bindings and adds them all.
     *
     * @param declarations binding declarations.
     * @return resulting Bindings.
     * @throws BindingAlreadyExistsException thrown if the Binding already exists.
     * @throws InvalidBindingException thrown if we cannot set initial value.
     */
    default List<Binding<?>> bind(BindingDeclaration<?>...declarations) throws BindingAlreadyExistsException, InvalidBindingException {
        Assert.notNullArray(declarations, "declarations cannot be null");
        Binding<?>[] result = new Binding[declarations.length];

        for (int i = 0; i < declarations.length; i++) {
            result[i] = bind(declarations[i]);
        }

        return List.of(result);
    }

    /**
     * Binds the given Binding into this set of Bindings. Follows the same rules as adding a new Binding with name, type, etc.
     *
     * @param binding existing Binding.
     * @param <T> generic type of the Binding.
     * @throws BindingAlreadyExistsException thrown if the Binding already exists.
     */
    <T> void bind(Binding<T> binding) throws BindingAlreadyExistsException;

    /**
     * Loads a collection of Bindings from the given value object.
     *
     * @param loader BindingLoader to use (see: PropertyBindingLoader, FieldBindingLoader, MapBindingLoader)
     * @param container container object (Bean, Map, etc)
     * @param <T> generic type of the Value object.
     */
    default <T> void load(BindingLoader<T> loader, T container) {
        Assert.notNull(loader, "loader cannot be null.");
        Assert.notNull(container, "container cannot be null.");
        loader.load(this, container);
    }

    /**
     * Loads a collection of Bindings from the properties in the given Bean.
     *
     * @param container container object (Bean)
     * @param <T> generic type of the Value object.
     */
    default <T> void loadProperties(T container) {
        Assert.notNull(container, "container cannot be null.");
        load(BindingLoader.builder().propertyLoaderBuilder().build(), container);
    }

    /**
     * Loads a collection of Bindings from the fields in the given Object.
     *
     * @param container container object.
     * @param <T> generic type of the Value object.
     */
    default <T> void loadFields(T container) {
        Assert.notNull(container, "container cannot be null.");
        load(BindingLoader.builder().fieldLoaderBuilder().build(), container);
    }

    /**
     * Loads a collection of Bindings from the given Map.
     *
     * @param container container object(Map).
     */
    default void loadMap(Map<String, Object> container) {
        Assert.notNull(container, "container cannot be null.");
        load(BindingLoader.builder().mapLoaderBuilder().build(), container);
    }

    /**
     * Retrieves the number of Bindings.
     *
     * @return number of Bindings.
     */
    int size();

    /**
     * Determines whether there are any Bindings.
     *
     * @return true if its empty; false otherwise.
     */
    default boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Retrieves the Binding identified by the given name.
     *
     * @param name name of the Binding.
     * @param <T> generic type of the Binding.
     * @return Binding if found; null otherwise.
     */
    <T> Binding<T> getBinding(String name);

    /**
     * Retrieves an optional binding for the specified name.
     *
     * @param name the name of the binding.
     * @return an {@code Optional} containing the binding if present, otherwise an empty {@code Optional}
     */
    default <T> Optional<Binding<T>> getOptionalBinding(String name) {
        Binding<T> result = getBinding(name);
        return Optional.of(result);
    }

    /**
     * Retrieves the Binding identified by the given name and type.
     *
     * @param name name of the Binding.
     * @param type type of the Binding.
     * @param <T> generic type of the Binding.
     * @return Binding if found; null otherwise.
     */
    <T> Binding<T> getBinding(String name, Type type);

    /**
     * Retrieves an optional binding for the specified name and type.
     *
     * @param name the name of the binding
     * @param type the type of the binding
     * @return an {@code Optional} containing the binding if present, otherwise an empty {@code Optional}
     */
    default <T> Optional<Binding<T>> getOptionalBinding(String name, Type type) {
        Binding<T> result = getBinding(name, type);
        return Optional.of(result);
    }

    /**
     * Determines if a Binding with given name exists.
     *
     * @param name name of the Binding.
     * @return true if Binding exists; false otherwise.
     */
    default boolean contains(String name) {
        return getBinding(name) != null;
    }

    /**
     * Determines if the Binding with given name and types exists.
     *
     * @param name name of the Binding.
     * @param type class type of the Binding.
     * @param <T> generic type of the Binding.
     * @return true if Binding exists; false otherwise.
     */
    default <T> boolean contains(String name, Class<T> type) {
        return getBinding(name, type) != null;
    }

    /**
     * Determines if the Binding with given name and types exists.
     *
     * @param name name of the Binding.
     * @param type generic type of the Binding.
     * @param <T> generic type of the Binding.
     * @return true if Binding exists; false otherwise.
     */
    @SuppressWarnings("SimplifiableConditionalExpression")
    default <T> boolean contains(String name, TypeReference<T> type) {
        Binding<?> result = getBinding(name);
        return result != null
                ? result.getType().equals(type.getType()) ? true : false
                : false;
    }

    /**
     * Retrieves all the Bindings of the given type.
     *
     * @param type desired type.
     * @param <T> generic type of the Binding.
     * @return all matching Bindings.
     */
    <T> List<Binding<T>> getBindings(Type type);

    /**
     * Retrieves the value of the Binding with the given name.
     *
     * @param name name of the Binding.
     * @param <T> generic type of the Binding.
     * @return value if Binding is found.
     * @throws NoSuchBindingException if Binding is not found.
     */
    default <T> T getValue(String name) {
        Binding<T> result = getBinding(name);
        // Could not find Binding
        if (result == null) throw new NoSuchBindingException(name);
        return result.getValue();
    }

    /**
     *
     * Retrieves the optional value associated with the specified name.
     *
     * @param name the name of the binding to retrieve
     * @return an Optional containing the value associated with the specified name, or an empty Optional if no value is found
     */
    default <T> Optional<T> getOptionalValue(String name) {
        try {
            Binding<T> result = getBinding(name);
            return Optional.of(result.getValue());
        } catch (NoSuchBindingException e) {
            return Optional.empty();
        }
    }

    /**
     * Retrieves the value of the Binding with the given name.
     *
     * @param name name of the Binding.
     * @param type required type.
     * @param <T> generic type of the Binding.
     * @return value if Binding is found.
     * @throws NoSuchBindingException if Binding is not found.
     */
    default <T> T getValue(String name, Class<T> type) {
        Binding<T> result = getBinding(name, type);
        // Could not find Binding
        if (result == null) throw new NoSuchBindingException(name);
        return result.getValue();
    }

    /**
     * Retrieves the optional value of a specified type associated with the given name.
     *
     * @param name the name of the value to be retrieved
     * @param type the class representing the type of the value to be retrieved
     * @return an Optional containing the value if found, otherwise an empty Optional
     */
    default <T> Optional<T> getOptionalValue(String name, Class<T> type) {
        try {
            Binding<T> result = getBinding(name, type);
            return Optional.of(result.getValue());
        } catch (NoSuchBindingException e) {
            return Optional.empty();
        }
    }

    /**
     * Sets the value of Binding with the given name.
     *
     * @param name name of the Binding.
     * @param value desired new value.
     * @param <T> generic type of the Binding.
     * @throws NoSuchBindingException if Binding is not found.
     * @throws InvalidBindingException unable to edit the value.
     */
    default <T> void setValue(String name, T value) throws NoSuchBindingException, InvalidBindingException {
        Binding<T> result = getBinding(name);
        // Could not find Binding
        if (result == null) throw new NoSuchBindingException(name);
        result.setValue(value);
    }

    /**
     * Sets the value of Binding using the given BindingDeclaration.
     *
     * @param declaration declaration details.
     * @throws NoSuchBindingException if Binding is not found.
     * @throws InvalidBindingException unable to edit the value.
     */
    default void setValue(BindingDeclaration<?> declaration) throws NoSuchBindingException, InvalidBindingException {
        setValue(declaration.name(), declaration.value());
    }

    /**
     * Creates Bindings and adds them all.
     *
     * @param declarations binding declarations.
     */
    default void setValues(BindingDeclaration<?>...declarations) throws NoSuchBindingException, InvalidBindingException {
        Assert.notNullArray(declarations, "declarations cannot be null");
        Arrays.stream(declarations).forEach(this::setValue);
    }


    /**
     * Adds a listener to be notified when a binding occurs.
     *
     * @param listener the binding listener to add
     */
    void addBindingListener(BindingListener listener);

    /**
     * Removes the specified BindingListener from the list of listeners. Once removed,
     * the listener will no longer receive notifications when the binding is added.
     *
     * @param listener The BindingListener to be removed.
     * @return true if the listener was successfully removed, false otherwise.
     */
    boolean removeBindingListener(BindingListener listener);

    /**
     * Retrieves all the Binding Names (ie. keys).
     *
     * @return all the used keys.
     */
    Set<String> getNames();

    /**
     * Retrieves the Binding values as an Unmodifiable Map.
     *
     * @return unmodifiable Map of the Binding values.
     */
    Map<String, ?> asMap();

    /**
     * Returns back a immutable version of this Bindings.
     *
     * @return immutable version of this.
     */
    default ImmutableBindings asImmutableBindings() {
        return new ImmutableBindings(this);
    }

    @Override
    default Bindings asImmutable() {
        return new ImmutableBindings(this);
    }

    /**
     * Beautified version of the Bindings.
     *
     * @param prefix for spacing.
     * @return textual version of the Bindings.
     */
    String prettyPrint(String prefix);
}
