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
import org.rulii.lib.spring.util.Assert;

/**
 * Per-{@code run()} execution state for a {@link RuleFlow} pipeline.
 *
 * <p>Created once at the start of each {@code run()} call and discarded when it
 * completes. Holds the active {@link RuleContext} and the owning {@link RuleFlow}.
 * The flow-level global exception handler is accessed directly via
 * {@link RuleFlow#getGlobalHandler()}.
 *
 * @author Max Arulananthan
 * @since 2.0
 */
public class RuleFlowExecutionContext {

    private final RuleContext ruleContext;
    private final RuleFlow<?> ruleFlow;

    RuleFlowExecutionContext(RuleContext ruleContext, RuleFlow<?> ruleFlow) {
        super();
        Assert.notNull(ruleContext, "ruleContext cannot be null.");
        Assert.notNull(ruleFlow, "ruleFlow cannot be null.");
        this.ruleContext = ruleContext;
        this.ruleFlow = ruleFlow;
    }

    /**
     * Returns the {@link RuleContext} for this execution.
     *
     * @return rule context; never null.
     */
    public RuleContext getRuleContext() {
        return ruleContext;
    }

    /**
     * Returns the {@link RuleFlow} being executed.
     *
     * @return rule flow; never null.
     */
    public RuleFlow<?> getRuleFlow() {
        return ruleFlow;
    }

    /**
     * Derives a new execution context bound to a different {@link RuleContext}, keeping the
     * same owning {@link RuleFlow}. Used by async continuations (e.g. {@code asyncRun}'s
     * {@code thenRun}) that must run their body against the resolved context of the async
     * step (which may differ from the caller's context under IMMUTABLE/CUSTOM modes) rather
     * than constructing a whole new {@code RuleFlowExecutionContext} from scratch.
     *
     * @param ruleContext the context the derived execution should run against; must not be null.
     * @return a new execution context; never null.
     */
    public RuleFlowExecutionContext withRuleContext(RuleContext ruleContext) {
        return new RuleFlowExecutionContext(ruleContext, this.ruleFlow);
    }
}
