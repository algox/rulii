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
 * The RuleRegistryBuilder class is a builder for creating a RuleRegistry object.
 * It allows registering individual Runnable instances to be included in the rule registry.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public final class RuleRegistryBuilder {

    private final DefaultRuleRegistry registry = new DefaultRuleRegistry();

    public RuleRegistryBuilder() {
        super();
    }

    /**
     * Registers a Runnable instance into the RuleRegistryBuilder.
     *
     * @param runnable the Runnable instance to register
     * @return the RuleRegistryBuilder object
     */
    public RuleRegistryBuilder register(Runnable<?> runnable) {
        registry.register(runnable);
        return this;
    }

    /**
     * Builds a RuleRegistry object.
     *
     * @return the RuleRegistry object built by this builder.
     */
    public RuleRegistry build() {
        return registry;
    }
}
