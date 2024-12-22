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
package org.rulii.model.action;

import org.rulii.bind.match.ParameterMatch;
import org.rulii.context.RuleContext;
import org.rulii.model.AbstractRunnable;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.MethodDefinition;
import org.rulii.model.UnrulyException;
import org.rulii.util.reflect.MethodExecutor;

import java.util.List;

/**
 * Default Action implementation.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class DefaultAction extends AbstractRunnable implements Action {

    /**
     * Ctor taking meta information and the target object.
     *
     * @param target action target.
     * @param name name of the action.
     * @param methodDefinition meta info.
     *
     */
    public DefaultAction(Object target, String name, MethodDefinition methodDefinition) {
        super(methodDefinition, MethodExecutor.build(methodDefinition.getMethod()), target,
                name != null ? name : "action", methodDefinition.getDescription());
    }

    @Override
    public Void run(RuleContext ruleContext) throws UnrulyException {
        Assert.notNull(ruleContext, "context cannot be null.");

        try {
            // match the parameters with bindings
            List<ParameterMatch> matches = ruleContext.match(getDefinition());
            // resolve parameter values
            List<Object> values = ruleContext.resolve(matches, getDefinition());
            // run the action
            run(matches, values);
            return null;
        } catch (UnrulyException e) {
            if (!e.isFilled()) e.fillStack(e.getStackTrace(), ruleContext.getExecutionContext().getStackTrace());
            throw e;
        }
    }

    /**
     * Actions could change state.
     * @return true;
     */
    protected final boolean couldChangeState() {
        return true;
    }
}