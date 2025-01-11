/*
 * This software is licensed under the Apache 2 license, quoted below.
 *
 * Copyright (c) 1999-2025, Algorithmx Inc.
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
package org.rulii.model.action;

import org.rulii.context.RuleContext;
import org.rulii.model.condition.Condition;
import org.rulii.lib.spring.util.Assert;


/**
 * Represents a mapping between a Condition and an Action.
 * The Condition determines whether the Action should be executed.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public final class NextActionMapping {

    private final Condition condition;
    private final Action action;


    /**
     * Ctor for NextActionMapping.
     *
     * @param condition The condition to be evaluated.
     * @param action The action to be executed.
     */
    public NextActionMapping(Condition condition, Action action) {
        super();
        Assert.notNull(action, "action cannot be null.");
        this.condition = condition;
        this.action = action;
    }

    public boolean isApplicable(RuleContext context) {
        Assert.notNull(context, "context cannot be null.");
        return condition == null || condition.isTrue(context);
    }

    public Condition getCondition() {
        return condition;
    }

    public Action getAction() {
        return action;
    }
}
