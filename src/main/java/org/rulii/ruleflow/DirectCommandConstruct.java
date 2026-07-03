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
import org.rulii.ruleflow.command.RuleFlowCommand;

/**
 * Build-time construct that wraps a pre-built {@link RuleFlowCommand} for direct injection
 * into the pipeline via {@link RuleFlowBuilderTemplate#command(RuleFlowCommand)}.
 *
 * <p>No capability interfaces ({@link Bindable}, {@link Parameterizable},
 * {@link ExceptionHandleable}) are implemented — the pre-built command carries its own
 * behaviour.
 *
 * @author Max Arulananthan
 * @since 2.0
 */
class DirectCommandConstruct extends CommandConstruct {

    private final RuleFlowCommand cmd;

    DirectCommandConstruct(RuleFlowCommand cmd) {
        super();
        Assert.notNull(cmd, "cmd cannot be null.");
        this.cmd = cmd;
    }

    @Override
    protected RuleFlowCommand buildCommand() {
        return cmd;
    }
}
