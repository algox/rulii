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

/**
 * Interface for listening to changes in the Binding objects and Named scopes.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public interface BindingListener extends BindingValueListener {

    /**
     * Callback method triggered when a Binding object is bound.
     *
     * @param binding the Binding object that is being bound.
     */
    default void onBind(Binding<?> binding) {}

    /**
     * Callback method triggered when a named scope is added.
     *
     * @param name the name of the scope that is being added
     */
    default void onScopeAdd(String name) {}

    /**
     * Callback method triggered when a named scope is removed.
     *
     * @param scope the named scope that is being removed
     */
    default void onScopeRemove(NamedScope scope) {}
}
