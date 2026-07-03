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
package org.rulii.ruleflow;

/**
 * Capability interface for constructs whose result can be bound to a named binding.
 *
 * <p>Implemented by {@link RunConstruct}. Configured via {@link RunSpec#as(String)} or
 * {@link RunSpec#as(String, String)} within the step's spec consumer.
 *
 * @author Max Arulananthan
 * @since 2.0
 */
interface Bindable {

    /**
     * Sets the binding target for this construct's result.
     *
     * @param name      the binding name; must not be null or empty.
     * @param scopeName the scope to bind into, or {@code null} for the current scope.
     */
    void setBindingTarget(String name, String scopeName);
}
