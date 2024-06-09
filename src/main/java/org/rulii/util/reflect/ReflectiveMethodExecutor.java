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
package org.rulii.util.reflect;

import org.rulii.lib.spring.util.Assert;
import org.rulii.model.UnrulyException;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * The ReflectiveMethodExecutor class is an implementation of the MethodExecutor interface that
 * uses reflection to execute a method on a target object.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class ReflectiveMethodExecutor implements MethodExecutor {

    private final Method method;

    /**
     * The ReflectiveMethodExecutor class is an implementation of the MethodExecutor interface that
     * uses reflection to execute a method on a target object.
     *
     * @param method The method to be executed.reflectiveMethodExecutor
     * @throws IllegalArgumentException If the method parameter is null.
     */
    public ReflectiveMethodExecutor(Method method) {
        super();
        Assert.notNull(method, "method cannot be null.");
        this.method = method;
    }

    /**
     * Executes the method with the given parameters.
     *
     * @param target     The target object on which the method will be executed.
     * @param userArgs   The arguments to be passed to the method. Can be null.
     * @param <T>        The generic type of the result.
     * @return The result of the method execution.
     * @throws UnrulyException if the number of arguments passed is invalid or if there is an error during execution.
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T execute(Object target, Object... userArgs) {
        if (method.getParameterCount() != (userArgs == null ? 0 : userArgs.length)) {
            throw new UnrulyException("Invalid number of args passed to Method call [" + getMethod()
                    + "] required [" + method.getParameterCount() + "]");
        }

        boolean staticMethod = Modifier.isStatic(getMethod().getModifiers());

        try {
            // Execute the method with the given parameters
            return (T) method.invoke(staticMethod ? null : target, userArgs);
        } catch (Throwable e) {
            // Something went wrong with the execution
            throw new UnrulyException("Unexpected error trying to execute [" + getMethod()
                    + "] with arguments " + Arrays.toString(userArgs), e);
        }
    }

    @Override
    public final Method getMethod() {
        return method;
    }
}
