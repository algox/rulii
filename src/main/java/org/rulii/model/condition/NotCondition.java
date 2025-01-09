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
package org.rulii.model.condition;

import org.rulii.context.RuleContext;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.UnrulyException;

/**
 * Not of a given condition.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class NotCondition implements Condition {

    private final Condition condition;

    public NotCondition(Condition condition) {
        super();
        Assert.notNull(condition, "condition cannot be null.");
        this.condition = condition;
    }

    public Condition getCondition() {
        return condition;
    }

    @Override
    public Boolean run(RuleContext context) throws UnrulyException {
        try {
            return !condition.isTrue(context);
        } catch (Exception e) {
            throw new UnrulyException("Unable to run Not Condition.", e);
        }
    }

    @Override
    public String getName() {
        return "!";
    }

    @Override
    public String getDescription() {
        return "!(" + condition.getDescription() + ")";
    }
}
