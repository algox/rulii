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
package org.rulii.bind;

import org.rulii.lib.spring.util.Assert;
import org.rulii.model.Immutator;

/**
 * The `NamedScope` class represents a named scope along with its associated bindings.
 * A scope is a container for variables and functions that can be accessed within a specific context.
 * This class provides methods to retrieve the name and bindings of a scope.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class NamedScope implements Immutator<NamedScope> {
    private final String name;
    private final Bindings bindings;

    /**
     * Creates a new NamedScope with the given name and bindings.
     * A NamedScope represents a named scope along with its associated bindings.
     * A scope is a container for variables and functions that can be accessed within a specific context.
     *
     * @param name     the name of the scope (cannot be null)
     * @param bindings the bindings for the scope (cannot be null)
     */
    public NamedScope(String name, Bindings bindings) {
        Assert.notNull(name, "name cannot be null");
        Assert.notNull(bindings, "bindings cannot be null");
        this.name = name;
        this.bindings = bindings;
    }

    public String getName() {
        return name;
    }

    public Bindings getBindings() {
        return bindings;
    }

    @Override
    public NamedScope asImmutable() {
        return new NamedScope(name, bindings.asImmutableBindings());
    }

    @Override
    public String toString() {
        return "NamedScope{" +
                "name='" + name + '\'' +
                ", bindings=" + bindings +
                '}';
    }
}
