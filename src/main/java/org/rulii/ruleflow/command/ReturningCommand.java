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

import org.rulii.ruleflow.RuleFlowExecutionContext;
import org.rulii.ruleflow.RuleFlowReturn;
import org.rulii.model.function.Function;

/**
 * Pipeline command that terminates the flow and returns a result.
 *
 * <p>Throws a {@link RuleFlowReturn} which propagates through all nesting levels and is
 * caught exactly once in {@code RulingOrder.run()}. When {@code resultExtractor} is
 * {@code null} the flow returns the current {@link org.rulii.context.RuleContext}.
 *
 * @author Max Arulananthan
 * @since 2.0
 */
public class ReturningCommand implements RuleFlowCommand {

    private final Function<?> resultExtractor;

    public ReturningCommand(Function<?> resultExtractor) {
        super();
        this.resultExtractor = resultExtractor;
    }

    @Override
    public void execute(RuleFlowExecutionContext ctx) {
        Object result = resultExtractor != null
                ? resultExtractor.apply(ctx.getRuleContext())
                : ctx.getRuleContext();
        throw new RuleFlowReturn(result);
    }
}
