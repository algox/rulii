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
     * The async step runs on a {@link org.rulii.context.RuleContext#fork() fork} of the calling
     * flow's context, taken when {@code asyncRun} executes. The fork shares every scope the caller
     * had at that moment, by reference: values set on those bindings by either side are seen by
     * both. Everything structural is private to the task: the bindings and scopes it adds are never
     * seen by the caller, and the scopes the caller adds or removes afterwards do not affect the
     * task. A rule set or nested flow, which pushes scopes of its own, is therefore safe to run
     * here. This is the default mode.
     */
    INHERITED,

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
