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
package org.rulii.lang.function;

import org.rulii.bind.NamedScope;
import org.rulii.context.RuleContext;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.UnrulyException;

import java.util.UUID;

/**
 * Represents a function that applies a before function to the input and then executes a main function.
 *
 * @param <V> the type of the input before function accepts
 * @param <T> the type of the result the main function produces
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class ComposeWithBeforeFunction<V, T> implements Function<T> {

    private final Function<V> before;
    private final String resultBindingName;
    private final Function<T> mainFunction;

    public ComposeWithBeforeFunction(Function<V> before, String resultBindingName, Function<T> mainFunction) {
        super();
        Assert.notNull(before, "before cannot be null.");
        Assert.hasText(resultBindingName, "resultBindingName cannot be null/empty.");
        Assert.notNull(mainFunction, "mainFunction cannot be null.");
        this.before = before;
        this.resultBindingName = resultBindingName;
        this.mainFunction = mainFunction;
    }

    /**
     * Executes a before function, binds the result to a named scope, and then evaluates a main function.
     *
     * @param ruleContext The RuleContext containing the state required for rule execution.
     * @return The result of the main function.
     * @throws UnrulyException If an exception occurs during rule execution.
     */
    @Override
    public T run(RuleContext ruleContext) throws UnrulyException {
        V value = before.apply(ruleContext);
        NamedScope scope = ruleContext.getBindings().addScope(getName() + "-" + UUID.randomUUID());

        try {
            scope.getBindings().bind(resultBindingName, value);
            return mainFunction.apply(ruleContext);
        } finally {
          ruleContext.getBindings().removeScope(scope);
        }
    }

    @Override
    public String getName() {
        return "anonymous-compose-before-function";
    }
}
