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
package org.rulii.model.action;

import org.rulii.model.RunnableBuilder;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.MethodDefinition;

/**
 * Builder class for Actions.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public final class ActionBuilder extends RunnableBuilder<ActionBuilder, Action> {

    public static ActionBuilderBuilder builder() {
        return ActionBuilderBuilder.getInstance();
    }

    private String name;

    ActionBuilder(Object target, MethodDefinition definition) {
        super(target, definition);
    }

    /**
     * Provide a name for the action.
     *
     * @param name action name.
     * @return ActionBuilder for fluency.
     */
    public ActionBuilder name(String name) {
        Assert.notNull(name, "name cannot be null.");
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
    public ActionBuilder description(String description) {
        getDefinition().setDescription(description);
        return this;
    }

    /**
     * Builds the Action based on the set properties.
     *
     * @return a new Action.
     */
    @Override
    public Action build() {
        getDefinition().createParameterNameIndex();
        getDefinition().validate();
        return new DefaultAction(getTarget(), name, getDefinition());
    }

}
