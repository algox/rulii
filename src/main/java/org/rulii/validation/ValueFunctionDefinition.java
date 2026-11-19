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
package org.rulii.validation;

import org.rulii.model.MethodDefinition;
import org.rulii.model.ParameterDefinition;
import org.rulii.model.SourceDefinition;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds the definition of a value function that is a class of its own ({@link BindingLookupFunction},
 * {@link ConstantFunction}) rather than a function built over a lambda.
 *
 * @author Max Arulananthan
 * @since 2.3
 */
final class ValueFunctionDefinition {

    private ValueFunctionDefinition() {
        super();
    }

    /**
     * Creates a definition over the given method for one function: it records where that function was declared,
     * and its parameter definitions are its own, so editing them leaves every other function alone.
     *
     * @param method      the method the function runs.
     * @param name        the name of the function.
     * @param description the description of the function.
     * @return a new definition.
     */
    static MethodDefinition of(Method method, String name, String description) {
        SourceDefinition source = SourceDefinition.build();
        // The loaded definition is cached and shared: take what does not change from it, copy what can be edited.
        MethodDefinition shared = MethodDefinition.load(method, true, source);
        List<ParameterDefinition> parameters = new ArrayList<>(shared.getParameterCount());

        for (ParameterDefinition parameter : shared.getParameterDefinitions()) {
            parameters.add(parameter.copy());
        }

        MethodDefinition result = new MethodDefinition(method, true, description, source,
                shared.getReturnTypeDefinition(), parameters);
        result.setName(name);
        return result;
    }
}
