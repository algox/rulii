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
package org.rulii.model.condition;

import org.rulii.model.RunnableBuilder;
import org.rulii.model.MethodDefinition;

/**
 * Builder class used to defaultObjectFactory Conditions.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class ConditionBuilder extends RunnableBuilder<ConditionBuilder, Condition> {

    public static ConditionBuilderBuilder builder() {
        return ConditionBuilderBuilder.getInstance();
    }

    private String name;

    ConditionBuilder(Object target, MethodDefinition definition) {
        super(target, definition);
    }

    /**
     * Provide a name for the Action.
     *
     * @param name name of the Action.
     * @return ActionBuilder for fluency.
     */
    public ConditionBuilder name(String name) {
        getDefinition().setName(name);
        this.name = name;
        return this;
    }

    /**
     * Provide a description for the Action.
     *
     * @param description description of the Action.
     * @return ActionBuilder for fluency.
     */
    public ConditionBuilder description(String description) {
        getDefinition().setDescription(description);
        return this;
    }

    public String getName() {
        return name;
    }

    /**
     * Builds the Condition based on the set properties.
     *
     * @return a new Condition.
     */
    @Override
    public Condition build() {
        getDefinition().createParameterNameIndex();
        getDefinition().validate();
        return new DefaultCondition(getTarget(), getName(), getDefinition());
    }
}
