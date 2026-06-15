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

import org.rulii.model.UnrulyException;

/**
 * Throwable used to implement early exit via {@code returning()}.
 *
 * <p>Thrown by {@code ReturningCommand} and propagates naturally through all nesting levels.
 * Caught exactly once in {@link RulingOrder#run(org.rulii.context.RuleContext)}.
 *
 * @author Max Arulananthan
 * @since 2.0
 */
public class RuleFlowReturn extends UnrulyException {

    private final Object result;

    public RuleFlowReturn(Object result) {
        super();
        this.result = result;
    }

    public Object getResult() {
        return result;
    }
}
