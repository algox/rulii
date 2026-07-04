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
package org.rulii.model.function;

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
 * Default Function implementation.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class DefaultFunction<T> extends AbstractRunnable implements Function<T> {

    /**
     * Ctor taking meta-information and the target object.
     *
     * @param target action target.
     * @param name name of the function.
     * @param methodDefinition meta info.
     */
    public DefaultFunction(Object target, String name, MethodDefinition methodDefinition) {
        super(methodDefinition, MethodExecutor.build(methodDefinition.getMethod()), target,
                name != null ? name : "anonymous-function", methodDefinition.getDescription());
    }

    @Override
    @SuppressWarnings("unchecked")
    public T run(RuleContext ruleContext) throws UnrulyException {
        Assert.notNull(ruleContext, "context cannot be null.");

        List<ParameterMatch> matches = null;
        List<Object> values = null;

        try {
            // match the parameters with bindings
            matches = ruleContext.getParameterResolver().match(getDefinition(), ruleContext.getBindings(),
                    ruleContext.getMatchingStrategy(), ruleContext.getObjectFactory());
            // resolve parameter values
            values = ruleContext.getParameterResolver().resolve(matches, getDefinition(), ruleContext.getBindings(),
                    ruleContext.getMatchingStrategy(), ruleContext.getConverterRegistry(), ruleContext.getObjectFactory());
            // run the function
            return (T) run(matches, values);
        } catch (UnrulyException e) {
            throw e;
        } catch (Exception e) {
            throw new UnrulyException("Error trying to run Function : " + RuleUtils.getSignature(this, matches, values), e);
        }
    }

    /**
     * Functions cannot change state.
     * @return false;
     */
    @Override
    protected final boolean couldChangeState() {
        return false;
    }
}