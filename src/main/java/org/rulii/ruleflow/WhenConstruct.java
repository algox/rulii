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

import org.rulii.lib.spring.util.Assert;
import org.rulii.model.condition.Condition;
import org.rulii.ruleflow.command.RuleFlowCommand;
import org.rulii.ruleflow.command.WhenCommand;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder-time accumulator for a {@code when/otherwise} conditional block.
 *
 * <p>Commands added while {@code inOtherwise} is false go into the then-branch;
 * after {@link #switchToOtherwise()} they go into the otherwise-branch.
 *
 * @author Max Arulananthan
 * @since 2.0
 */
class WhenConstruct extends FlowConstruct {

    private final Condition condition;
    private final List<RuleFlowCommand> otherwiseCommands = new ArrayList<>();
    private boolean inOtherwise = false;

    WhenConstruct(Condition condition) {
        super();
        Assert.notNull(condition, "condition cannot be null.");
        this.condition = condition;
    }

    /**
     * Redirects subsequent {@link #commands()} calls to the otherwise-branch list.
     * Called by the builder between the then-Consumer and the otherwise-Consumer.
     */
    void switchToOtherwise() {
        inOtherwise = true;
    }

    @Override
    List<RuleFlowCommand> commands() {
        return inOtherwise ? otherwiseCommands : getCommands();
    }

    @Override
    protected RuleFlowCommand buildCommand() {
        WhenCommand cmd = new WhenCommand(condition);
        cmd.setBody(new ArrayList<>(getCommands()));
        cmd.setOtherwiseBody(new ArrayList<>(otherwiseCommands));
        return cmd;
    }
}