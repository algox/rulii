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

import org.rulii.bind.match.ParameterMatch;
import org.rulii.context.RuleContext;
import org.rulii.model.AbstractRunnable;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.MethodDefinition;
import org.rulii.model.UnrulyException;
import org.rulii.util.RuleUtils;
import org.rulii.util.reflect.MethodExecutor;

import java.util.List;

/**
 * Default Condition implementation.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class DefaultCondition extends AbstractRunnable implements Condition {

    public DefaultCondition(Object target, String name, MethodDefinition methodDefinition) {
        super(methodDefinition, MethodExecutor.build(methodDefinition.getMethod()), target,
                name != null ? name : "condition", methodDefinition.getDescription());
    }

    public Boolean run(RuleContext ruleContext) throws UnrulyException {
        Assert.notNull(ruleContext, "ruleContext cannot be null.");

        List<ParameterMatch> matches = null;
        List<Object> values = null;

        try {
            // match the parameters with bindings
            matches = ruleContext.match(getDefinition());
            // resolve parameter values
            values = ruleContext.resolve(matches, getDefinition());
            // run the condition
            Object result = run(matches, values);
            // Check the result
            if (result == null) throw new UnrulyException("Condition excepts a boolean return type. Actual [null]");
            if (!(result instanceof Boolean)) throw new UnrulyException("Condition expects a boolean return type. " +
                    "Actual [" + result.getClass().getSimpleName() + "]");
            // audit post
            return (Boolean) result;
        } catch (Exception e) {
            throw new UnrulyException("Error trying to run Condition: " + RuleUtils.getSignature(this, matches, values), e);
        }
    }

    /**
     * Conditions cannot change state.
     * @return false;
     */
    @Override
    protected final boolean couldChangeState() {
        return false;
    }
}
