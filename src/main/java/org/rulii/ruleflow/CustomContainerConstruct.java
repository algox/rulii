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
import org.rulii.ruleflow.command.ContainerCommand;
import org.rulii.ruleflow.command.RuleFlowCommand;

import java.util.ArrayList;

/**
 * Builder-time accumulator for a custom {@link ContainerCommand}.
 *
 * <p>Used internally by {@link RuleFlowBuilderTemplate#runContainer}.
 *
 * @author Max Arulananthan
 * @since 2.0
 */
class CustomContainerConstruct extends FlowConstruct {

    private final ContainerCommand cmd;

    CustomContainerConstruct(ContainerCommand cmd) {
        super();
        Assert.notNull(cmd, "cmd cannot be null.");
        this.cmd = cmd;
    }

    @Override
    protected RuleFlowCommand buildCommand() {
        cmd.setBody(new ArrayList<>(getCommands()));
        return cmd;
    }
}
