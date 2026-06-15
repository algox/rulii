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
 * Singleton entry point for creating {@link RuleFlowBuilder} instances.
 *
 * <p>Use {@link RuleFlow#builder()} as the public API — this class is the backing singleton.
 *
 * @author Max Arulananthan
 * @since 2.0
 */
public final class RuleFlowBuilderBuilder {

    private static final RuleFlowBuilderBuilder instance = new RuleFlowBuilderBuilder();

    private RuleFlowBuilderBuilder() {
        super();
    }

    /**
     * Returns the singleton instance.
     *
     * @return the singleton; never null.
     */
    public static RuleFlowBuilderBuilder getInstance() {
        return instance;
    }

    /**
     * Creates a fresh {@link RuleFlowBuilder}.
     *
     * @return a new builder; never null.
     */
    public RuleFlowBuilder build() {
        return new RuleFlowBuilder();
    }
}
