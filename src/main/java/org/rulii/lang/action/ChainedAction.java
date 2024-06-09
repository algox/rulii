/*
 * This software is licensed under the Apache 2 license, quoted below.
 *
 * Copyright (c) 1999-2024, Algorithmx Inc.
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

package org.rulii.lang.action;

import org.rulii.context.RuleContext;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.UnrulyException;

/**
 * The ChainedAction class represents an action that chains two other actions together.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class ChainedAction implements Action {

    private final Action mainAction;
    private final Action beforeAfter;
    private final boolean after;

    public ChainedAction(Action mainAction, Action beforeAfter, boolean after) {
        super();
        Assert.notNull(mainAction, "mainAction cannot be null.");
        Assert.notNull(beforeAfter, "beforeAfter cannot be null.");
        this.mainAction = mainAction;
        this.beforeAfter = beforeAfter;
        this.after = after;
    }

    /**
     * Runs a chained action, which consists of a main action followed by an optional before-after action.
     *
     * @param ruleContext the rule context used for execution
     * @return null
     * @throws UnrulyException if there are errors during execution
     */
    @Override
    public Void run(RuleContext ruleContext) throws UnrulyException {
        if (!after) beforeAfter.run(ruleContext);
        mainAction.run(ruleContext);
        if (after) beforeAfter.run(ruleContext);
        return null;
    }

    @Override
    public String getName() {
        return "anonymous-chained-action(" + (after ? "after" : "before");
    }
}
