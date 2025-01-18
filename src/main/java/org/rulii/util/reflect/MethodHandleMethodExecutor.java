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
package org.rulii.util.reflect;

import org.rulii.lib.spring.util.Assert;
import org.rulii.model.UnrulyException;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * The MethodHandleMethodExecutor class is an implementation of the MethodExecutor interface that
 * uses MethodHandle to execute a method on a target object.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class MethodHandleMethodExecutor implements MethodExecutor {

    private final Method method;
    private final MethodHandle methodHandle;

    /**
     * The MethodHandleMethodExecutor class is an implementation of the MethodExecutor interface that
     * uses MethodHandle to execute a method on a target object.
     */
    public MethodHandleMethodExecutor(Method method) throws IllegalAccessException {
        super();
        Assert.notNull(method, "method cannot be null.");
        this.methodHandle = ReflectionUtils.getMethodHandle(method);
        this.method = method;
    }

    /**
     * Executes a method with the given target and arguments.
     *
     * @param target    the target object on which the method will be executed
     * @param userArgs  the arguments to be passed to the method
     * @param <T>       the generic type of the return value
     * @return the result of the method execution
     * @throws Throwable if an error occurs during execution
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T execute(Object target, Object... userArgs) throws Throwable {
        if (method.getParameterCount() != (userArgs == null ? 0 : userArgs.length)) {
            throw new UnrulyException("Invalid number of args passed to Method call [" + method()
                    + "] required [" + method.getParameterCount() + "]");
        }

        boolean staticMethod = Modifier.isStatic(method().getModifiers());
        int index = 0;

        // Do no include target if it's a static method call
        Object[] args = new Object[staticMethod
                ? method.getParameterCount()
                : method.getParameterCount() + 1];

        if (!staticMethod) args[index++] = target;

        for (Object userArg : userArgs) {
            args[index++] = userArg;
        }

        // Execute the method with the derived parameters
        return (T) methodHandle.invokeWithArguments(args);
    }

    @Override
    public final Method method() {
        return method;
    }
}
