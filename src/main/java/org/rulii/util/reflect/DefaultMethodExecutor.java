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

import java.lang.reflect.Method;

/**
 * The DefaultMethodExecutor class is an implementation of the MethodExecutor interface.
 * It is responsible for executing a target method using a given delegate MethodExecutor.
 *
 * By default, it uses the MethodHandleMethodExecutor if possible, otherwise it falls back to the ReflectiveMethodExecutor.
 *
 * The DefaultMethodExecutor class has the following public methods:
 *  - execute(Object target, Object... userArgs): Executes the target method and returns the result.
 *  - getMethod(): Returns the method that will be executed.
 *
 * The DefaultMethodExecutor class also overrides the toString() method to provide a string representation of the object.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class DefaultMethodExecutor implements MethodExecutor {

    private final Method method;
    private MethodExecutor delegate;

    /**
     * The DefaultMethodExecutor class is an implementation of the MethodExecutor interface.
     * It is responsible for executing a target method using a given delegate MethodExecutor.
     *
     * By default, it uses the MethodHandleMethodExecutor if possible, otherwise it falls back to the ReflectiveMethodExecutor.
     *
     * The DefaultMethodExecutor class has the following public methods:
     *  - execute(Object target, Object... userArgs): Executes the target method and returns the result.
     *  - getMethod(): Returns the method that will be executed.
     *
     * The DefaultMethodExecutor class also overrides the toString() method to provide a string representation of the object.
     */
    public DefaultMethodExecutor(Method method) {
        super();
        Assert.notNull(method, "method cannot be null.");
        this.method = method;
        ReflectionUtils.makeAccessible(method);
        try {
            this.delegate = new MethodHandleMethodExecutor(method);
        } catch (Exception e) {
            this.delegate = new ReflectiveMethodExecutor(method);
        }
    }

    /**
     * Executes the target method using a delegate MethodExecutor.
     *
     * @param target the target object on which the method will be executed.
     * @param userArgs the arguments to be passed to the method.
     * @param <T> the generic type of the result.
     * @return the result of the method execution.
     * @throws Throwable if any error occurs during method execution.
     */
    @Override
    public <T> T execute(Object target, Object...userArgs) throws Throwable {
        return delegate.execute(target, userArgs);
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public String toString() {
        return "DefaultMethodExecutor{" +
                "method=" + method +
                ", delegate=" + delegate +
                '}';
    }
}
