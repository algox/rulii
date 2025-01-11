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
package org.rulii.model.function;

import org.rulii.bind.BindingDeclaration;
import org.rulii.bind.Bindings;
import org.rulii.bind.ReservedBindings;
import org.rulii.context.RuleContext;
import org.rulii.model.Runnable;
import org.rulii.model.UnrulyException;

/**
 * Represents a function that accepts argument(s) and produces a result.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
@FunctionalInterface
public interface Function<T> extends Runnable<T> {

    static FunctionBuilderBuilder builder() {
        return FunctionBuilderBuilder.getInstance();
    }

    /**
     * Derives all the arguments and executes this Function.
     *
     * @param ruleContext Rule Context.
     * @return result of the function.
     * @throws UnrulyException thrown if there are any errors during the Function execution.
     */
    default T apply(RuleContext ruleContext) throws UnrulyException {
        return run(ruleContext);
    }

    /**
     * Applies the Bindings to the Function and returns the result.
     *
     * @param bindings the Bindings to be applied to the Function
     * @return the result of applying the Bindings to the Function
     * @throws UnrulyException if there are any errors during the execution of the Function
     */
    default T apply(Bindings bindings) throws UnrulyException {
        return run(bindings);
    }

    /**
     * Apply the given binding declarations to the function and return the result.
     *
     * @param params an array of BindingDeclaration objects representing the binding declarations to be applied
     * @return the result of applying the binding declarations to the function
     * @throws UnrulyException if there are any errors during the execution of the function
     */
    default T apply(BindingDeclaration<?>...params) throws UnrulyException {
        return run(params);
    }

    /**
     * Composes this function with a before function. The before function is applied to the input and then the main function is executed.
     *
     * @param <B> the type of the input before function accepts
     * @param before the before function to apply
     * @return a new function that represents the composition of the before and main functions
     */
    default <B> Function<T> compose(Function<B> before) {
        return new ComposeWithBeforeFunction<B, T>(before, ReservedBindings.METHOD_RESULT.getName(), this);
    }

    /**
     * Composes this function with a before function. The before function is applied to the input and then the main function is executed.
     *
     * @param <B> the type of the input before function accepts
     * @param before the before function to apply
     * @param resultBindingName the name of the binding to store the result of the before function's application
     * @return a new function that represents the composition of the before and main functions
     */
    default <B> Function<T> compose(Function<B> before, String resultBindingName) {
        return new ComposeWithBeforeFunction<B, T>(before, resultBindingName, this);
    }

    /**
     * Composes this function with another function, applying the "after" function to the result of this function.
     *
     * @param <V>   the type of the result of the "after" function
     * @param after the function to apply to the result of this function
     * @return a new function that represents the composition of this function and the "after" function
     */
    default <V> Function<V> andThen(Function<V> after) {
        return new ComposeWithAfterFunction<T, V>(this, ReservedBindings.METHOD_RESULT.getName(), after);
    }

    /**
     * Composes this function with another function, applying the "after" function to the result of this function.
     *
     * @param <V>                 the type of the result of the "after" function
     * @param after               the function to apply to the result of this function
     * @param resultBindingName   the name of the binding to store the result of the "after" function's application
     * @return a new function that represents the composition of this function and the "after" function
     */
    default <V> Function<V> andThen(Function<V> after, String resultBindingName) {
        return new ComposeWithAfterFunction<T, V>(this, resultBindingName, after);
    }

    @Override
    default String getName() {
        return "anonymous-function";
    }
}
