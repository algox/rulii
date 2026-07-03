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
import org.rulii.model.function.Function;
import org.rulii.ruleflow.command.ForEachCommand;
import org.rulii.ruleflow.command.RuleFlowCommand;

import java.util.ArrayList;

/**
 * Builder-time accumulator for a {@code forEach} iteration block.
 *
 * <p>Holds the collection source function, element binding name, optional stop condition,
 * and the body commands accumulated while the {@code forEach} Consumer executes.
 * Produces a {@link ForEachCommand} when sealed.
 *
 * @author Max Arulananthan
 * @since 2.0
 */
class ForEachConstruct extends FlowConstruct {

    private final Function<?> source;
    private final String elementName;
    private final Condition stopCondition;

    ForEachConstruct(Function<?> source, String elementName, Condition stopCondition) {
        super();
        Assert.notNull(source, "source cannot be null.");
        Assert.hasText(elementName, "elementName cannot be empty/null.");
        this.source = source;
        this.elementName = elementName;
        this.stopCondition = stopCondition;
    }

    @Override
    protected RuleFlowCommand buildCommand() {
        ForEachCommand cmd = new ForEachCommand(source, elementName, stopCondition);
        cmd.setBody(new ArrayList<>(getCommands()));
        return cmd;
    }
}