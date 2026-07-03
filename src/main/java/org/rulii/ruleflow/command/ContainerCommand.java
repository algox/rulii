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

import org.rulii.lib.spring.util.Assert;
import org.rulii.ruleflow.RuleFlowBuilderTemplate;

import java.util.Collections;
import java.util.List;

/**
 * Base class for custom pipeline commands that enclose a body of child commands.
 *
 * <p>Extend this class to create custom container commands (e.g., {@code retry},
 * {@code parallel}) and plug them in via
 * {@link RuleFlowBuilderTemplate#runContainer(ContainerCommand, java.util.function.Consumer)}.
 *
 * <pre>{@code
 * public class RetryCommand extends ContainerCommand {
 *     private final int maxAttempts;
 *
 *     public RetryCommand(int maxAttempts) { this.maxAttempts = maxAttempts; }
 *
 *     @Override
 *     public void execute(RuleFlowExecutionContext ctx) {
 *         for (int i = 0; i < maxAttempts; i++) {
 *             try {
 *                 for (RuleFlowCommand cmd : getBody()) cmd.execute(ctx);
 *                 return;
 *             } catch (UnrulyException e) {
 *                 if (i == maxAttempts - 1) throw e;
 *             }
 *         }
 *     }
 * }
 * }</pre>
 *
 * @author Max Arulananthan
 * @since 2.0
 */
public abstract class ContainerCommand implements RuleFlowCommand {

    private List<RuleFlowCommand> body = Collections.emptyList();

    protected ContainerCommand() {
        super();
    }

    /**
     * Called by the builder to inject the body commands accumulated during the Consumer block.
     *
     * @param body the child commands; must not be null.
     */
    public final void setBody(List<RuleFlowCommand> body) {
        Assert.notNull(body, "body cannot be null.");
        this.body = Collections.unmodifiableList(body);
    }

    /**
     * Returns the body commands enclosed by this container.
     *
     * @return immutable list; never null.
     */
    protected List<RuleFlowCommand> getBody() {
        return body;
    }
}
