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

import org.rulii.context.RuleContext;
import org.rulii.model.UnrulyException;

/**
 * A class that implements the BindingSupplier interface to provide bindings for rule evaluation.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class RuleContextBasedBindingSupplier implements BindingSupplier {

    public static final String BINDING_NAME         = "$bindingName";
    public static final String PARENT_BINDING_NAME  = "$parentBindingName";

    public RuleContextBasedBindingSupplier() {
        super();
    }

    /**
     * Retrieves the name from the provided RuleContext object.
     *
     * @param context The RuleContext object from which to retrieve the name.
     * @return The name retrieved from the RuleContext object.
     * @throws UnrulyException if the binding with the name "$bindingName" is not set in the provided RuleContext object.
     */
    @Override
    public String getName(RuleContext context) {
        if (!context.getBindings().contains(BINDING_NAME)) throw new UnrulyException("Binding [" + BINDING_NAME + "] not set.");
        return context.getBindings().getBinding(BINDING_NAME).getTextValue();
    }

    /**
     * Retrieves the name of the parent binding from the provided RuleContext object.
     *
     * @param context The RuleContext object from which to retrieve the name.
     * @return The name of the parent binding retrieved from the RuleContext object, or null if the parent binding does not exist.
     */
    @Override
    public String getParentName(RuleContext context) {
        return context.getBindings().contains(PARENT_BINDING_NAME)
                ? context.getBindings().getBinding(PARENT_BINDING_NAME).getTextValue()
                : null;
    }
}
