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

import org.rulii.context.RuleContext;
import org.rulii.model.UnrulyException;

/**
 * RuleExecutionStrategy interface represents the strategy for executing a rule.
 *
 * @author Max Arulananthan
 * @since 1.0
 *
 */
public interface RuleExecutionStrategy {

    /**
     * Builds and returns a new instance of RuleExecutionStrategy.
     *
     * @return a new instance of RuleExecutionStrategy
     */
    static RuleExecutionStrategy build() {
        return new DefaultRuleExecutionStrategy();
    }

    /**
     * Executes a given rule based on the provided rule and rule context.
     *
     * @param rule the rule to be executed
     * @param ruleContext the context in which the rule will be executed
     * @return the result of executing the rule
     * @throws UnrulyException if there are any errors during the execution
     */
    RuleResult run(Rule rule, RuleContext ruleContext) throws UnrulyException;
}
