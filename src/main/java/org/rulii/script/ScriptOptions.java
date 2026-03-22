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
 * Immutable configuration options for script evaluation.
 *
 * <p>{@code ScriptOptions} is a record that controls how scripts interact with
 * the rule execution context.  Currently it holds a single option:
 *
 * <ul>
 *   <li>{@link #bindingsName()} — the name under which the rule bindings map is
 *       exposed as a variable inside the script (default: {@code "ctx"}).</li>
 * </ul>
 *
 * <p>The pre-built {@link #DEFAULT} constant uses {@code "ctx"} as the bindings
 * variable name and is suitable for most use cases.
 *
 * @param bindingsName the name of the variable that exposes the rule bindings
 *                     inside the script; must not be null or empty.
 *
 * @author Max Arulananthan
 * @since 1.2
 * @see ScriptProcessor
 * @see org.rulii.script.jsr223.JSR223ScriptProcessor
 */
public record ScriptOptions(String bindingsName) {

    /**
     * Default script options: exposes the rule bindings under the variable name {@code "ctx"}.
     */
    public static final ScriptOptions DEFAULT = new ScriptOptions("ctx");

    /**
     * Compact canonical constructor that validates the {@code bindingsName} component.
     *
     * @throws IllegalArgumentException if {@code bindingsName} is null or empty.
     */
    public ScriptOptions {
        Assert.hasText(bindingsName, "bindingsName cannot be empty.");
    }
}
