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
package org.rulii.validation;

import org.rulii.context.RuleContext;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.UnrulyException;
import org.rulii.model.function.Function;

/**
 * A {@link Function} wrapper that associates a binding name with a delegate function.
 * This class is used by validation rule builder classes to extract the binding name so it can
 * be used as the parameter key in rule violations.
 *
 * @param <T> the return type of the wrapped function
 * @author Max Arulananthan
 * @since 1.2
 */
public class BindingFunction<T> implements Function<T> {
    private final String bindingName;
    private final Function<T> targetFunction;

    /**
     * Creates a new {@code BindingFunction} that associates the given binding name with the target function.
     *
     * @param bindingName    the name of the binding; used as the parameter key in rule violations
     * @param targetFunction the underlying function that performs the actual value retrieval
     */
    public BindingFunction(String bindingName, Function<T> targetFunction) {
        super();
        Assert.hasText(bindingName, "bindingName cannot be null or empty");
        Assert.notNull(targetFunction, "targetFunction cannot be null");
        this.bindingName = bindingName;
        this.targetFunction = targetFunction;
    }

    @Override
    public T run(RuleContext ruleContext) throws UnrulyException {
        return targetFunction.run(ruleContext);
    }

    /**
     * Returns the underlying function that this wrapper delegates to.
     *
     * @return the target {@link Function}
     */
    public Function<T> getTargetFunction() {
        return targetFunction;
    }

    /**
     * Returns the binding name associated with this function.
     *
     * @return the binding name used as the parameter key in rule violations
     */
    public String getBindingName() {
        return bindingName;
    }
}
