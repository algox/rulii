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
package org.rulii.validation;

import org.rulii.bind.Binding;
import org.rulii.context.RuleContext;

/**
 * Represents a supplier of bindings for rule evaluation.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
@FunctionalInterface
public interface BindingSupplier {

    /**
     * Retrieves the name for the given rule context.
     *
     * @param context The rule context to get the name from. Cannot be null.
     * @return The name associated with the rule context.
     */
    String getName(RuleContext context);

    /**
     * Retrieves the name of the parent from the given rule context.
     *
     * @param context The rule context to get the parent name from. Cannot be null.
     * @return The parent name associated with the rule context.
     */
    default String getParentName(RuleContext context) {
        return null;
    }

    /**
     * Retrieves the value associated with the given rule context.
     *
     * @param context The rule context to retrieve the value from. Cannot be null.
     * @return The value associated with the rule context, or null if no value is found.
     */
    default Object getValue(RuleContext context) {
        Binding<?> binding = context.getBindings().getBinding(getName(context));
        return binding != null ? binding.getValue() : null;
    }
}
