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

import org.rulii.model.Runnable;
import org.rulii.model.Definable;
import org.rulii.model.MethodDefinition;

/**
 * Represents an operation that accepts input arguments performs an action and returns no result.
 *
 * @author Max Arulananthan
 * @since 1.0
 * @see Runnable
 */
@FunctionalInterface
public interface Action extends Runnable<Void> {

    /**
     * Builder class to help create a new Action.
     *
     * @return builder.
     */
    static ActionBuilderBuilder builder() {
        return ActionBuilderBuilder.getInstance();
    }

    @SuppressWarnings("unchecked")
    default MethodDefinition getDefinition() {
        return this instanceof Definable ? ((Definable<MethodDefinition>) this).getDefinition() : null;
    }

    /**
     * Chaining Actions together. The given action will be run before this action.
     *
     * @param action before action.
     * @return chained action.
     */
    default Action andBefore(Action action) {
        return new ChainedAction(this, action, false);
    }

    /**
     * Chaining Actions together. The given action will be run if this action completes without any errors.
     *
     * @param nextAction next action.
     * @return chained action.
     */
    default Action andThen(Action nextAction) {
        return new ChainedAction(this, nextAction, true);
    }

    @Override
    default String getName() {
        return "anonymous-action";
    }
}
