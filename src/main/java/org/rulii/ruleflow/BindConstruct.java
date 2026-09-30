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

import org.rulii.bind.ScopedBindings;
import org.rulii.lib.spring.util.Assert;
import org.rulii.ruleflow.command.BindCommand;
import org.rulii.ruleflow.command.RuleFlowCommand;
import org.rulii.ruleflow.info.CommandInfo;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Build-time construct for a {@code bind()} step.
 *
 * <p>Produces an immutable {@link BindCommand}. No capability interfaces are currently
 * implemented; to add decorator support in the future, implement {@link Bindable},
 * {@link Parameterizable}, or {@link ExceptionHandleable} as appropriate.
 *
 * @author Max Arulananthan
 * @since 2.0
 */
class BindConstruct extends CommandConstruct {

    private final Consumer<ScopedBindings> binder;
    private final Supplier<CommandInfo.Bind> info;

    BindConstruct(Consumer<ScopedBindings> binder, Supplier<CommandInfo.Bind> info) {
        super();
        Assert.notNull(binder, "binder cannot be null.");
        Assert.notNull(info, "info cannot be null.");
        this.binder = binder;
        this.info = info;
    }

    @Override
    protected RuleFlowCommand buildCommand() {
        return new BindCommand(binder, info);
    }
}
