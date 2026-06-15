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

import org.rulii.context.RuleContextBuilder;
import org.rulii.ruleflow.RuleFlowExecutionContext;
import org.rulii.lib.spring.util.Assert;

import java.util.function.Consumer;

/**
 * Pipeline command that carries a {@link org.rulii.context.RuleContext} configuration lambda.
 *
 * <p>The configurator is applied by the execution strategy before command iteration begins;
 * this command's {@link #execute} method is therefore a no-op at runtime. It exists in the
 * command list solely so the builder can detect and validate its position (must be first).
 *
 * @author Max Arulananthan
 * @since 2.0
 */
public class ContextCommand implements RuleFlowCommand {

    private final Consumer<RuleContextBuilder> configurator;

    public ContextCommand(Consumer<RuleContextBuilder> configurator) {
        super();
        Assert.notNull(configurator, "configurator cannot be null.");
        this.configurator = configurator;
    }

    /**
     * Returns the context configuration lambda.
     *
     * @return the configurator; never null.
     */
    public Consumer<RuleContextBuilder> getConfigurator() {
        return configurator;
    }

    @Override
    public void execute(RuleFlowExecutionContext ctx) {
        // Applied before command iteration by the execution strategy; nothing to do here.
    }
}
