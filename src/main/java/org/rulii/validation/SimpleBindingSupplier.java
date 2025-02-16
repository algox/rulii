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
import org.rulii.lib.apache.StringUtils;
import org.rulii.lib.spring.util.Assert;

/**
 * A simple implementation of the {@link BindingSupplier} interface. It provides a way to retrieve
 * the name and parent name associated with a rule context.
 *
 * @author Max Arulananthan
 * @since 1.0
 *
 */
public final class SimpleBindingSupplier implements BindingSupplier {

    private final String bindingName;
    private final String parentBindingName;

    /**
     * Creates a new instance of SimpleBindingSupplier with the given binding name.
     *
     * @param bindingName The name of the binding. Cannot be null or empty.
     */
    public SimpleBindingSupplier(String bindingName) {
        this(bindingName, null);
    }

    /**
     * A simple implementation of the {@link BindingSupplier} interface.
     * It provides a way to retrieve the name and parent name associated with a rule context.
     * @param bindingName The name of the binding. Cannot be null or empty.
     * @param parentBindingName The name of the parent binding.
     */
    public SimpleBindingSupplier(String bindingName, String parentBindingName) {
        super();
        Assert.isTrue(StringUtils.isNotEmpty(bindingName), "bindingName must have text.");
        this.bindingName = bindingName;
        this.parentBindingName = parentBindingName;
    }

    @Override
    public String getName(Bindings bindings) {
        return bindingName;
    }

    @Override
    public String getParentName(Bindings bindings) {
        return parentBindingName;
    }

    @Override
    public String toString() {
        return "SimpleBindingSupplier{" +
                "bindingName='" + bindingName + '\'' +
                ", parentBindingName='" + parentBindingName + '\'' +
                '}';
    }
}
