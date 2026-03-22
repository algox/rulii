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

import org.rulii.lib.spring.util.Assert;

/**
 * Describes a single typed parameter that a {@link Script} expects to receive
 * when it is evaluated.
 *
 * <p>A {@code ScriptParameter} carries a name (used to expose the value as a
 * variable inside the script) and a Java {@link Class} indicating the expected
 * type of that variable.  Parameters are declared on a script via
 * {@link ScriptBuilder#param(ScriptParameter)} and stored in
 * {@link AbstractScript#getScriptParameters()}.
 *
 * @author Max Arulananthan
 * @since 1.2
 * @see Script
 * @see ScriptBuilder
 */
public class ScriptParameter {

    private final String name;
    private final Class<?> type;

    /**
     * Constructs a new {@code ScriptParameter} with the given name and type.
     *
     * @param name the variable name used to expose this parameter inside the script; must not be null or empty.
     * @param type the expected Java type of this parameter; must not be null.
     * @throws IllegalArgumentException if {@code name} is null/empty or {@code type} is null.
     */
    public ScriptParameter(String name, Class<?> type) {
        super();
        Assert.hasText(name, "name cannot be empty.");
        Assert.notNull(type, "type cannot be null.");
        this.name = name;
        this.type = type;
    }

    /**
     * Returns the variable name used to expose this parameter inside the script.
     *
     * @return the parameter name; never null or empty.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the expected Java type of this parameter.
     *
     * @return the parameter type; never null.
     */
    public Class<?> getType() {
        return type;
    }

    @Override
    public String toString() {
        return "ScriptParameter{" +
                "name='" + name + '\'' +
                ", type=" + type +
                '}';
    }
}
