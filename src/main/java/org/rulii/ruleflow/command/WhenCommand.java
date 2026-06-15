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

import org.rulii.lib.spring.util.Assert;
import org.rulii.model.ScopeDefining;
import org.rulii.model.condition.Condition;
import org.rulii.ruleflow.RuleFlowExecutionContext;

import java.util.Collections;
import java.util.List;

/**
 * Pipeline command that evaluates a {@link Condition} and executes the matching branch.
 *
 * <p>If the condition evaluates to {@code true} the then-branch commands run; otherwise
 * the otherwise-branch commands run (which may be empty). Both branches execute within a
 * dedicated scope that is created and removed automatically.
 *
 * @author Max Arulananthan
 * @since 2.0
 */
public class WhenCommand implements RuleFlowCommand, ScopeDefining, CompositeCommand {

    private final Condition condition;
    private final List<RuleFlowCommand> thenCommands;
    private final List<RuleFlowCommand> otherwiseCommands;

    public WhenCommand(Condition condition, List<RuleFlowCommand> thenCommands,
                       List<RuleFlowCommand> otherwiseCommands) {
        super();
        Assert.notNull(condition, "condition cannot be null.");
        Assert.notNull(thenCommands, "thenCommands cannot be null.");
        Assert.notNull(otherwiseCommands, "otherwiseCommands cannot be null.");
        this.condition = condition;
        this.thenCommands = Collections.unmodifiableList(thenCommands);
        this.otherwiseCommands = Collections.unmodifiableList(otherwiseCommands);
    }

    @Override
    public void execute(RuleFlowExecutionContext ctx) {
        List<RuleFlowCommand> branch = condition.isTrue(ctx.getRuleContext()) ? thenCommands : otherwiseCommands;

        for (RuleFlowCommand cmd : branch) {
            cmd.execute(ctx);
        }
    }

    @Override
    public List<List<RuleFlowCommand>> getBlocks() {
        return List.of(thenCommands, otherwiseCommands);
    }
}
