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
import org.rulii.model.ScopeDefining;

/**
 * Pipeline command that pushes a named binding scope onto the scope stack.
 *
 * <p>The scope persists until the matching {@link EndScopeCommand} executes.
 * Bindings created within the scope are discarded when the scope is removed.
 *
 * @author Max Arulananthan
 * @since 2.0
 */
public class ScopeCommand implements RuleFlowCommand, ScopeDefining {

    private final String scopeName;

    public ScopeCommand(String scopeName) {
        super();
        this.scopeName = scopeName;
    }

    /**
     * Returns the name of the scope to push, or {@code null} for an auto-generated name.
     *
     * @return scope name; may be null.
     */
    public String getScopeName() {
        return scopeName;
    }

    @Override
    public void execute(RuleFlowExecutionContext ctx) {
        if (scopeName != null) {
            ctx.getRuleContext().getBindings().addScope(scopeName);
        } else {
            ctx.getRuleContext().getBindings().addScope();
        }
    }
}
