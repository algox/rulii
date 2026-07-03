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

import org.rulii.ruleflow.command.RuleFlowCommand;

import java.util.ArrayList;
import java.util.List;

/**
 * Internal builder-time accumulator for a bounded block of pipeline commands.
 *
 * <p>Each {@code FlowConstruct} sits on the state-machine stack inside
 * {@link RuleFlowBuilderTemplate} while a Consumer body is executing. Commands added during
 * that body are collected via {@link #commands()}. When the body returns, the builder calls
 * {@link CommandConstruct#seal()}, which applies any registered decorators and then delegates
 * to the subclass' {@link #buildCommand()} to produce the corresponding runtime
 * {@link RuleFlowCommand}.
 *
 * @author Max Arulananthan
 * @since 2.0
 */
abstract class FlowConstruct extends CommandConstruct {

    private final List<RuleFlowCommand> commands = new ArrayList<>();

    FlowConstruct() {
        super();
    }

    /**
     * Returns the active write-target list. Overridden by {@link WhenConstruct} to switch
     * between the then-branch and otherwise-branch based on its internal state.
     *
     * @return the list that new commands are appended to.
     */
    List<RuleFlowCommand> commands() {
        return commands;
    }

    /**
     * Returns the primary (then-branch or only) command list for this construct.
     *
     * @return mutable list; never null.
     */
    protected List<RuleFlowCommand> getCommands() {
        return commands;
    }
}
