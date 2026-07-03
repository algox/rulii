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

import org.rulii.context.RuleContextBuilder;
import org.rulii.lib.spring.util.Assert;
import org.rulii.ruleflow.command.ContextCommand;
import org.rulii.ruleflow.command.RuleFlowCommand;

import java.util.function.Consumer;

/**
 * Build-time construct for a {@code context()} step.
 *
 * <p>Seals into an immutable {@link ContextCommand}.
 *
 * @author Max Arulananthan
 * @since 2.0
 */
class ContextConstruct extends CommandConstruct {

    private final Consumer<RuleContextBuilder> configurator;

    ContextConstruct(Consumer<RuleContextBuilder> configurator) {
        super();
        Assert.notNull(configurator, "configurator cannot be null.");
        this.configurator = configurator;
    }

    @Override
    protected RuleFlowCommand buildCommand() {
        return new ContextCommand(configurator);
    }
}
