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

import java.lang.reflect.Method;
import java.util.List;

/**
 * The MethodExecutor interface defines a contract for executing methods on a target object.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public interface MethodExecutor {

    /**
     * Creates the default implementation of MethodExecutor.
     *
     * @param method target method.
     * @return default MethodExecutor implementation.
     */
    static MethodExecutor build(Method method) {
        return new DefaultMethodExecutor(method);
    }

    /**
     *  Executes the BindableMethod and returns its result.
     *
     * @param target target object.
     * @param userArgs method arguments.
     * @param <T> generic type of the result.
     * @return the result of the execution.
     * @throws Throwable thrown during execution.
     */
    default <T> T execute(Object target, List<Object> userArgs) throws Throwable {
        return execute(target, userArgs != null ? userArgs.toArray() : null);
    }


    /**
     *  Executes the BindableMethod and returns its result.
     *
     * @param target target object.
     * @param userArgs method arguments.
     * @param <T> generic type of the result.
     * @return the result of the execution.
     * @throws Throwable thrown during execution.
     */
    <T> T execute(Object target, Object...userArgs) throws Throwable;

    /**
     * Method to execute.
     *
     * @return method to execute.
     */
    Method getMethod();
}
