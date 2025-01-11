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

package org.rulii.registry;

import org.rulii.model.Runnable;

/**
 * The MutableRegistry interface represents a registry that allows registration of Runnable objects.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public interface MutableRuleRegistry extends RuleRegistry {

    /**
     * Registers a {@link org.rulii.model.Runnable} runnable in the rule registry.
     *
     * @param runnable the runnable to register
     */
    void register(Runnable<?> runnable);
}
