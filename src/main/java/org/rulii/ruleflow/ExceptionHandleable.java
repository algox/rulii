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
 * Capability interface for constructs that support a step-level exception handler.
 *
 * <p>Implemented by {@link RunConstruct} — which backs both {@code run()}/{@code apply()} and
 * {@code execute()} steps. Configured via {@link RunSpec#onException} or
 * {@link ExecuteSpec#onException} within the step's spec consumer.
 *
 * @author Max Arulananthan
 * @since 2.0
 */
interface ExceptionHandleable {

    /**
     * Attaches a step-level exception handler. Replaces any previously set handler.
     *
     * @param exceptionHandler the handler; must not be null.
     */
    void setExceptionHandler(RuleFlowExceptionHandler exceptionHandler);
}
