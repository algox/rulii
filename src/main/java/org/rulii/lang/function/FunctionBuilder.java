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
package org.rulii.lang.function;

import org.rulii.lang.RunnableBuilder;
import org.rulii.model.MethodDefinition;
import org.rulii.model.ParameterDefinition;
import org.rulii.model.ParameterDefinitionEditor;
import org.rulii.model.UnrulyException;

/**
 * Builder class for Functions.
 *
 * @author Max Arulananthan
 * @since 1.0
 *
 */
public class FunctionBuilder<T> extends RunnableBuilder<FunctionBuilder<T>, Function> {

    private String name;

    FunctionBuilder(Object function, MethodDefinition definition) {
        super(function, definition);
    }

    /**
     * Provide a name for the Action.
     *
     * @param name name of the Action.
     * @return ActionBuilder for fluency.
     */
    public FunctionBuilder<T> name(String name) {
        getDefinition().setName(name);
        this.name = name;
        return this;
    }

    /**
     * Provide a description for the Function.
     *
     * @param description description of the Action.
     * @return ActionBuilder for fluency.
     */
    public FunctionBuilder<T> description(String description) {
        getDefinition().setDescription(description);
        return this;
    }

    public ParameterDefinitionEditor<FunctionBuilder<T>> param(int index) {
        return ParameterDefinitionEditor.with(getDefinition().getParameterDefinition(index), this);
    }

    public ParameterDefinitionEditor<FunctionBuilder<T>> param(String name) {
        ParameterDefinition definition = getDefinition().getParameterDefinition(name);

        if (definition == null) {
            throw new UnrulyException("No such parameter found [" + name + "] in method [" + getDefinition().getMethod() + "]");
        }

        return ParameterDefinitionEditor.with(definition, this);
    }

    public String getName() {
        return name;
    }

    /**
     * Builds the Action based on the set properties.
     *
     * @return a new Action.
     */
    @Override
    public Function<T> build() {
        getDefinition().createParameterNameIndex();
        getDefinition().validate();
        return new DefaultFunction(getTarget(), getName(), getDefinition());
    }
}
