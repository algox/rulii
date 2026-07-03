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

import org.rulii.model.function.Function;
import org.rulii.ruleflow.command.ReturningCommand;
import org.rulii.ruleflow.command.RuleFlowCommand;

/**
 * Build-time construct for an {@code exit()} step.
 *
 * <p>Produces an immutable {@link ReturningCommand}. When {@code extractor} is {@code null}
 * the flow returns the current {@link org.rulii.context.RuleContext}; otherwise the extractor
 * function is evaluated against the live bindings to produce a typed result.
 *
 * @author Max Arulananthan
 * @since 2.0
 */
class ExitConstruct extends CommandConstruct {

    private final Function<?> extractor;

    ExitConstruct(Function<?> extractor) {
        super();
        this.extractor = extractor;
    }

    @Override
    protected RuleFlowCommand buildCommand() {
        return new ReturningCommand(extractor);
    }
}
