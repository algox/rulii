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
import org.rulii.lib.spring.util.Assert;
import org.rulii.ruleflow.RuleFlowExecutionContext;
import org.rulii.ruleflow.info.CommandInfo;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Pipeline command that adds one or more bindings to the current scope.
 *
 * <p>The actual binding strategy (declarations, name/value pair, POJO properties, etc.) is
 * captured as a {@link Consumer} at build time by {@link org.rulii.ruleflow.RuleFlowBuilderTemplate}.
 *
 * @author Max Arulananthan
 * @since 2.0
 */
public class BindCommand implements RuleFlowCommand {

    private final Consumer<ScopedBindings> binder;
    private final Supplier<CommandInfo.Bind> info;

    /**
     * Creates a bind command whose structure is unknown; {@link #getInfo()} reports
     * {@link CommandInfo.Custom}.
     *
     * @param binder the binding logic; must not be null.
     */
    public BindCommand(Consumer<ScopedBindings> binder) {
        this(binder, null);
    }

    /**
     * Creates a bind command that can describe itself.
     *
     * @param binder the binding logic; must not be null.
     * @param info   supplies the description on demand; may be null when unknown.
     * @since 2.1
     */
    public BindCommand(Consumer<ScopedBindings> binder, Supplier<CommandInfo.Bind> info) {
        super();
        Assert.notNull(binder, "binder cannot be null.");
        this.binder = binder;
        this.info = info;
    }

    @Override
    public CommandInfo getInfo() {
        CommandInfo.Bind result = info != null ? info.get() : null;
        return result != null ? result : RuleFlowCommand.super.getInfo();
    }

    @Override
    public void execute(RuleFlowExecutionContext ctx) {
        binder.accept(ctx.getRuleContext().getBindings());
    }
}
