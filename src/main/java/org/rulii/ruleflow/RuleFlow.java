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
import org.rulii.model.AsyncRunnable;
import org.rulii.model.Definable;
import org.rulii.model.InputParameter;
import org.rulii.model.Runnable;
import org.rulii.model.ScopeDefining;
import org.rulii.model.action.Action;

import java.util.List;

/**
 * A fluent pipeline of executable steps.
 *
 * <p>A {@code RuleFlow} is built with {@link RuleFlowBuilder} and executed by calling
 * {@link #run(RuleContext)} or one of the {@code runAsync} variants.
 * {@code Serializable} and {@code Identifiable} are inherited via {@link Runnable}.
 *
 * @param <T> the result type produced when the flow completes.
 *
 * @author Max Arulananthan
 * @since 2.0
 */
public interface RuleFlow<T> extends Runnable<T>, AsyncRunnable<T>, Definable<RuleFlowDefinition>, ScopeDefining {

    /**
     * Entry point for the fluent flow-building DSL.
     *
     * @return a new {@link RuleFlowBuilder}; never null.
     */
    static RuleFlowBuilder builder() {
        return RuleFlowBuilderBuilder.getInstance().build();
    }

    /**
     * Returns the declared input parameters for this flow.
     *
     * @return immutable list; never null, may be empty.
     */
    List<InputParameter<?>> getInputParameters();

    /**
     * Returns the finalizer action, or {@code null} if none was registered.
     *
     * @return finalizer action; may be null.
     */
    Action getFinalizer();

}
