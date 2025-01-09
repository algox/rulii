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
package org.rulii.ruleset;

public final class RuleSetBuilderBuilder {

    private static final RuleSetBuilderBuilder instance = new RuleSetBuilderBuilder();

    private RuleSetBuilderBuilder() {
        super();
    }

    public static RuleSetBuilderBuilder getInstance() {
        return instance;
    }

    public static <T> RuleSetBuilder<T> with(String name) {
        return new RuleSetBuilder<T>(name);
    }

    public static <T> RuleSetBuilder<T> with(String name, String description) {
        return new RuleSetBuilder<T>(name, description);
    }
}
