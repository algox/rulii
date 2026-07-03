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
import org.rulii.ruleflow.command.ScopeCommand;

import java.util.ArrayList;

/**
 * Builder-time accumulator for a {@code scope} block.
 *
 * <p>Holds the optional scope name and the body commands accumulated while the
 * {@code scope} Consumer executes. Produces a {@link ScopeCommand} when sealed;
 * that command pushes the scope, runs the body, and pops the scope in a
 * {@code finally} block.
 *
 * @author Max Arulananthan
 * @since 2.0
 */
class ScopeConstruct extends FlowConstruct {

    private final String scopeName;

    ScopeConstruct(String scopeName) {
        super();
        this.scopeName = scopeName;
    }

    @Override
    protected RuleFlowCommand buildCommand() {
        ScopeCommand cmd = new ScopeCommand(scopeName);
        cmd.setBody(new ArrayList<>(getCommands()));
        return cmd;
    }
}
