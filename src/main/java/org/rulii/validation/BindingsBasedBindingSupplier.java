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
package org.rulii.validation;

import org.rulii.bind.Bindings;
import org.rulii.bind.NoSuchBindingException;
import org.rulii.bind.ReservedBindings;
import org.rulii.model.UnrulyException;

/**
 * A class that implements the BindingSupplier interface to provide bindings for rule evaluation.
 *
 * @author Max Arulananthan
 * @since 1.0
 *
 */
public class BindingsBasedBindingSupplier implements BindingSupplier {

    public BindingsBasedBindingSupplier() {
        super();
    }

    /**
     * Retrieves the name from the provided bindings.
     *
     * @param bindings The bindings to get the parent name from. Cannot be null.
     * @return The name retrieved from the bindings.
     * @throws UnrulyException if the binding with the name "$bindingName" is not set in the provided bindings.
     */
    @Override
    public String getName(Bindings bindings) {
        if (!bindings.contains(ReservedBindings.BINDING_NAME.getName())) throw new NoSuchBindingException(ReservedBindings.BINDING_NAME.getName());
        return bindings.getBinding(ReservedBindings.BINDING_NAME.getName()).getTextValue();
    }

    /**
     * Retrieves the name of the parent binding from the provided bindings.
     *
     * @param bindings The bindings to get the parent name from. Cannot be null.
     * @return The name of the parent binding retrieved from the bindings, or null if the parent binding does not exist.
     */
    @Override
    public String getParentName(Bindings bindings) {
        return bindings.contains(ReservedBindings.PARENT_BINDING_NAME.getName())
                ? bindings.getBinding(ReservedBindings.PARENT_BINDING_NAME.getName()).getTextValue()
                : null;
    }
}
