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
package org.rulii.ruleflow.command;

import org.rulii.ruleflow.RuleFlowExecutionContext;

/**
 * A single executable step in a {@link org.rulii.ruleflow.RuleFlow} pipeline.
 *
 * <p>Each command receives the per-{@code run()} execution context and performs
 * its work — binding variables, running rules, evaluating conditions, etc.
 * Commands are immutable and reusable across executions.
 *
 * @author Max Arulananthan
 * @since 2.0
 */
public interface RuleFlowCommand {

    /**
     * Executes this command within the given execution context.
     *
     * @param ctx the current execution context; never null.
     */
    void execute(RuleFlowExecutionContext ctx);
}
