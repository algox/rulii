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

import org.rulii.context.RuleContext;
import org.rulii.ruleflow.command.OnExceptionCommand;
import org.rulii.lib.spring.util.Assert;

/**
 * Per-{@code run()} execution state for a {@link RuleFlow} pipeline.
 *
 * <p>Created once at the start of each {@code run()} call and discarded when it
 * completes. Holds the active {@link RuleContext}, the owning {@link RuleFlow},
 * and the optional global exception handler.
 *
 * @author Max Arulananthan
 * @since 2.0
 */
public class RuleFlowExecutionContext {

    private final RuleContext ruleContext;
    private final RuleFlow<?> ruleFlow;
    private OnExceptionCommand globalHandler;

    RuleFlowExecutionContext(RuleContext ruleContext, RuleFlow<?> ruleFlow) {
        super();
        Assert.notNull(ruleContext, "ruleContext cannot be null.");
        Assert.notNull(ruleFlow, "ruleFlow cannot be null.");
        this.ruleContext = ruleContext;
        this.ruleFlow = ruleFlow;
    }

    public RuleContext getRuleContext() {
        return ruleContext;
    }

    public RuleFlow<?> getRuleFlow() {
        return ruleFlow;
    }

    public OnExceptionCommand getGlobalHandler() {
        return globalHandler;
    }

    void setGlobalHandler(OnExceptionCommand globalHandler) {
        this.globalHandler = globalHandler;
    }
}
