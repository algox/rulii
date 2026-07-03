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

/**
 * Controls how the {@link org.rulii.context.RuleContext} is provided to an async step launched
 * via {@link RuleFlowBuilderTemplate#asyncRun}.
 *
 * @author Max Arulananthan
 * @since 2.0
 */
public enum AsyncContextMode {

    /**
     * The calling flow's {@link org.rulii.context.RuleContext} is shared directly with the async
     * step. Both the caller and the async task operate on the same bindings concurrently. This
     * is the default mode and is appropriate for fire-and-forget tasks that do not write bindings.
     */
    SHARED,

    /**
     * A new {@link org.rulii.context.RuleContext} is created for the async step, backed by an
     * immutable snapshot of the caller's current bindings. The async task can read all values
     * visible at the time {@code asyncRun} executes, but cannot mutate the caller's bindings.
     */
    IMMUTABLE,

    /**
     * A user-supplied {@link org.rulii.context.RuleContext} is used for the async step. The async
     * task operates on a completely independent context with its own bindings and services.
     */
    CUSTOM
}
