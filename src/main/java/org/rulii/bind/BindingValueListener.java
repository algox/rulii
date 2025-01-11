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

import java.util.EventListener;

/**
 * Interface for listening to changes in the value of a Binding object.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public interface BindingValueListener extends EventListener {

    /**
     * Callback when the value of a Binding object is changed.
     *
     * @param binding the Binding object that has been changed.
     * @param oldValue the old value of the Binding object.
     * @param newValue the new value of the Binding object.
     */
    default void onChange(Binding<?> binding, Object oldValue, Object newValue) {}
}
