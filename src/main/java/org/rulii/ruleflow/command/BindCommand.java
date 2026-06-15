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

import org.rulii.bind.ScopedBindings;
import org.rulii.ruleflow.RuleFlowExecutionContext;
import org.rulii.lib.spring.util.Assert;

import java.util.function.Consumer;

/**
 * Pipeline command that adds one or more bindings to the current scope.
 *
 * <p>The actual binding strategy (declarations, existing {@code Bindings}, POJO, loader, etc.)
 * is captured as a {@link Consumer} at build time by {@code RuleFlowBuilder}.
 *
 * @author Max Arulananthan
 * @since 2.0
 */
public class BindCommand implements RuleFlowCommand {

    private final Consumer<ScopedBindings> binder;

    public BindCommand(Consumer<ScopedBindings> binder) {
        super();
        Assert.notNull(binder, "binder cannot be null.");
        this.binder = binder;
    }

    @Override
    public void execute(RuleFlowExecutionContext ctx) {
        binder.accept(ctx.getRuleContext().getBindings());
    }
}
