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
package org.rulii.rule;

import org.rulii.lib.spring.util.Assert;

/**
 * Represents the result of executing a rule.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public record RuleResult(Rule rule, RuleExecutionStatus status) {

    public RuleResult {
        Assert.notNull(rule, "rule cannot be null.");
        Assert.notNull(status, "status cannot be null.");
    }

    @Override
    public String toString() {
        return rule.getName() + " : " + status.name();
    }
}
