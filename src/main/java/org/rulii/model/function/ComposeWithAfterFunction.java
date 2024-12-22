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
package org.rulii.model.function;

import org.rulii.bind.NamedScope;
import org.rulii.context.RuleContext;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.UnrulyException;

import java.util.UUID;

/**
 * Represents a composed function that applies a "after" function to the result
 * of the main function.
 *
 * @param <V> the type of the result of the main function
 * @param <T> the type of the result of the "after" function
 *
 * @author Max Arulananthan
 * @since 1.0
 *
 */
public class ComposeWithAfterFunction<V, T> implements Function<T> {

    private final Function<V> mainFunction;
    private final String resultBindingName;
    private final Function<T> after;

    public ComposeWithAfterFunction(Function<V> mainFunction, String resultBindingName, Function<T> after) {
        super();
        Assert.notNull(mainFunction, "mainFunction cannot be null.");
        Assert.hasText(resultBindingName, "resultBindingName cannot be null/empty.");
        Assert.notNull(after, "after cannot be null.");
        this.mainFunction = mainFunction;
        this.resultBindingName = resultBindingName;
        this.after = after;
    }

    /**
     * Runs the ComposeWithAfterFunction by executing the main function, binding the result to a named scope,
     * and applying the after function to the RuleContext.
     *
     * @param ruleContext the RuleContext object containing the necessary information for executing the function
     * @return the result of the after function applied to the RuleContext
     * @throws UnrulyException if an error occurs during execution
     */
    @Override
    public T run(RuleContext ruleContext) throws UnrulyException {
        NamedScope scope = ruleContext.getBindings().addScope(getName() + "-" + UUID.randomUUID());

        try {
            V value = mainFunction.apply(ruleContext);
            scope.getBindings().bind(resultBindingName, value);
            return after.apply(ruleContext);
        } finally {
          ruleContext.getBindings().removeScope(scope);
        }
    }

    @Override
    public String getName() {
        return "anonymous-compose-after-function";
    }
}
