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

import java.util.List;

/**
 * A {@link RuleFlowCommand} that contains one or more ordered blocks of nested commands.
 *
 * <p>Composite commands ({@code when}, {@code forEach}, {@code onException}) hold their
 * branch commands in blocks. The execution strategy uses {@link #getBlocks()} to recurse
 * into nested commands for scope management and build-time structural validation.
 *
 * <p>Block layout by command type:
 * <ul>
 *   <li>{@code WhenCommand} — {@code [thenCommands, otherwiseCommands]}</li>
 *   <li>{@code ForEachCommand} — {@code [bodyCommands]}</li>
 *   <li>{@code OnExceptionCommand} — {@code [handlerCommands]}</li>
 * </ul>
 *
 * @author Max Arulananthan
 * @since 2.0
 */
public interface CompositeCommand extends RuleFlowCommand {

    /**
     * Returns the ordered list of command blocks owned by this composite command.
     *
     * @return immutable list of blocks; never null, never empty.
     */
    List<List<RuleFlowCommand>> getBlocks();
}
