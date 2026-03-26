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
package org.rulii.script;

import org.rulii.lib.spring.util.Assert;

/**
 * Describes a single named, typed parameter that a {@link Script} expects to receive at evaluation time.
 *
 * <p>Script parameters allow the framework to validate or adapt bindings before passing them into the
 * script engine. Each parameter carries a {@link #getName() name} — used to look up the corresponding
 * value in the rule bindings — and a {@link #getType() type} that indicates the expected Java type.
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
     * Creates a new {@code ScriptParameter}.
     *
     * @param name the parameter name used to look up the value in rule bindings; must not be null or empty.
     * @param type the expected Java type of the parameter value; must not be null.
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
     * Returns the parameter name, which is used to look up the corresponding value in the rule bindings.
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
