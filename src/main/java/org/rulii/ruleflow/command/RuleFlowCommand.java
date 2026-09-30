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
import org.rulii.ruleflow.info.CommandInfo;

import java.util.List;

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

    /**
     * Describes what this command does, without running it.
     *
     * <p>The built-in commands report their real structure. The default reports
     * {@link CommandInfo.Custom} with this command's class name, plus the body when this is a
     * {@link ContainerCommand}. Override it in a custom command to report real structure.
     *
     * @return command info; never null.
     * @since 2.1
     */
    default CommandInfo getInfo() {
        List<CommandInfo> body = this instanceof ContainerCommand container
                ? CommandInfo.of(container.getBody())
                : List.of();
        return new CommandInfo.Custom(getClass().getName(), body);
    }
}
