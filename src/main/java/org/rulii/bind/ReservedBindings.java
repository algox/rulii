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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Reserved Binding Names used by Rulii.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public enum ReservedBindings {

    RULE_CONTEXT("ruleContext"),
    BINDINGS("bindings"),
    RULE_SET_STATUS("ruleSetStatus"),
    RULE_VIOLATIONS ("ruleViolations"),
    ROOT_BEAN("rootBean"),
    BINDING_NAME("$bindingName"),
    METHOD_RESULT("$result"),
    THIS("$this");

    private static final Set<String> names = new HashSet<>();

    static {
        Arrays.stream(ReservedBindings.values()).forEach(r -> names.add(r.name));
    }

    private String name;

    ReservedBindings(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Collection<String> reservedBindingNames() {
        return names;
    }

    public static boolean isReserved(String name) {
        return names.contains(name);
    }

    @Override
    public String toString() {
        return "ReservedBindings{" +
                "name='" + name + '\'' +
                '}';
    }
}
