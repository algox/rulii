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
public class WhenCommand extends ContainerCommand implements ScopeDefining {

    private final Condition condition;
    private List<RuleFlowCommand> otherwiseBody = Collections.emptyList();

    public WhenCommand(Condition condition) {
        super();
        Assert.notNull(condition, "condition cannot be null.");
        this.condition = condition;
    }

    /**
     * Injects the otherwise-branch commands. Called by the builder after the otherwise
     * Consumer body completes.
     *
     * @param commands the otherwise-branch commands; must not be null.
     */
    public void setOtherwiseBody(List<RuleFlowCommand> commands) {
        Assert.notNull(commands, "commands cannot be null.");
        this.otherwiseBody = Collections.unmodifiableList(commands);
    }

    @Override
    public void execute(RuleFlowExecutionContext ctx) {
        List<RuleFlowCommand> branch = condition.isTrue(ctx.getRuleContext()) ? getBody() : otherwiseBody;

        for (RuleFlowCommand cmd : branch) {
            cmd.execute(ctx);
        }
    }

}
