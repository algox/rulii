/*
 * This software is licensed under the Apache 2 license, quoted below.
 *
 * Copyright (c) 1999-2024, Algorithmx Inc.
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
package org.rulii.bind.load;

import org.rulii.lib.spring.util.Assert;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Convenient parent class for BindingLoader(s). Handles the logic of the filter and name generator.
 *
 * @param <S> container item type (Field, PropertyDescriptor etc)
 * @param <T> container type.
 *
 * @author Max Arulananthan.
 * @since 1.0
 */
public abstract class AbstractBindingLoader<S, T> implements BindingLoader<T> {

    private Predicate<S> filter;
    private Function<S, String> nameGenerator;

    /**
     * Constructor for the AbstractBindingLoader class.
     */
    protected AbstractBindingLoader() {
        super();
    }

    /**
     * Function use to change the output BindingName,
     *
     * @param nameGenerator Function that output the BindingName,
     */
    public void setNameGenerator(Function<S, String> nameGenerator) {
        Assert.notNull(nameGenerator, "nameGenerator cannot be null.");
        this.nameGenerator = nameGenerator;
    }

    /**
     * Filter to restrict which items in the container become Bindings.
     *
     * @param filter restricting Filter.
     */
    public void setFilter(Predicate<S> filter) {
        Assert.notNull(filter, "filter cannot be null.");
        this.filter = filter;
    }

    /**
     * Array of container item names that won't get added.
     *
     * @param itemNames blacklisted item itemNames.
     */
    public void setIgnoreItems(String...itemNames) {
        Assert.notNull(itemNames, "itemNames cannot be null.");
        Set<String> nameSet = Arrays.stream(itemNames).collect(Collectors.toSet());
        setIgnoreItems(nameSet);
    }

    /**
     * Set of container item names that won't get added.
     *
     * @param itemNames blacklisted item itemNames.
     */
    protected void setIgnoreItems(Set<String> itemNames) {
        Assert.notNull(itemNames, "itemNames cannot be null.");
        this.filter = this.filter.and((S item) -> !itemNames.contains(getNameGenerator().apply(item)));
    }

    /**
     * Array of item names that will get added.
     *
     * @param itemNames whitelisted item itemNames.
     */
    public void setIncludeItems(String...itemNames) {
        Assert.notNull(itemNames, "itemNames cannot be null.");
        Set<String> nameSet = Arrays.stream(itemNames).collect(Collectors.toSet());
        setIncludeItems(nameSet);
    }

    /**
     * Array of item names that will get Bindings.
     *
     * @param names whitelisted item names.
     */
    protected void setIncludeItems(Set<String> names) {
        Assert.notNull(names, "names cannot be null.");
        this.filter = this.filter.and((S item) -> names.contains(getItemName(item)));
    }

    public Predicate<S> getFilter() {
        return filter;
    }

    public Function<S, String> getNameGenerator() {
        return nameGenerator;
    }

    /**
     * Return the name of the container item (Example: field name, property name, map key).
     *
     * @param item container item.
     * @return item name.
     */
    protected abstract String getItemName(S item);
}
