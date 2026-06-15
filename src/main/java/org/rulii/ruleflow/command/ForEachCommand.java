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

import org.rulii.bind.NamedScope;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.ScopeDefining;
import org.rulii.model.UnrulyException;
import org.rulii.model.condition.Condition;
import org.rulii.model.function.Function;
import org.rulii.ruleflow.RuleFlowExecutionContext;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Pipeline command that iterates a collection and executes the body commands for each element.
 *
 * <p>Each iteration runs in its own scope. Two bindings are automatically added to that
 * scope before the body executes: the current element (bound under {@link #elementBindingName})
 * and its 0-based position (bound as {@code "index"}).
 *
 * <p>Iteration stops early if the optional {@link #stopCondition} evaluates to {@code true}
 * after an element's body completes.
 *
 * @author Max Arulananthan
 * @since 2.0
 */
public class ForEachCommand implements RuleFlowCommand, ScopeDefining, CompositeCommand {

    public static final String INDEX_BINDING_NAME = "index";

    private final Function<?> listSource;
    private final List<RuleFlowCommand> bodyCommands;
    private final String elementBindingName;
    private final Condition stopCondition;

    public ForEachCommand(Function<?> listSource, List<RuleFlowCommand> bodyCommands,
                          String elementBindingName, Condition stopCondition) {
        super();
        Assert.notNull(listSource, "listSource cannot be null.");
        Assert.notNull(bodyCommands, "bodyCommands cannot be null.");
        Assert.hasText(elementBindingName, "elementBindingName cannot be empty/null.");
        this.listSource = listSource;
        this.bodyCommands = Collections.unmodifiableList(bodyCommands);
        this.elementBindingName = elementBindingName;
        this.stopCondition = stopCondition;
    }

    @Override
    public void execute(RuleFlowExecutionContext ctx) {
        Object list = listSource.apply(ctx.getRuleContext());

        if (list != null && !(list instanceof Collection)) throw new UnrulyException("listSource must return a Collection.");
        Collection<?> items = (Collection<?>) list;
        if (items == null || items.isEmpty()) return;

        int index = 0;
        for (Object item : items) {
            NamedScope scope = ctx.getRuleContext().getBindings().addScope(getScopeName(index));

            try {
                ctx.getRuleContext().getBindings().bind(elementBindingName, item);
                ctx.getRuleContext().getBindings().bind(INDEX_BINDING_NAME, index);

                for (RuleFlowCommand cmd : bodyCommands) {
                    cmd.execute(ctx);
                }

                if (stopCondition != null && stopCondition.isTrue(ctx.getRuleContext())) break;

            } finally {
                ctx.getRuleContext().getBindings().removeScope(scope);
            }

            index++;
        }
    }

    @Override
    public List<List<RuleFlowCommand>> getBlocks() {
        return List.of(bodyCommands);
    }

    protected String getScopeName(int index) {
        return "forEach-scope-" + UUID.randomUUID().toString().substring(0, 8) + "-" + index;
    }
}
