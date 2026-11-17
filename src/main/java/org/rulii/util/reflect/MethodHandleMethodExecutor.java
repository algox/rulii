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
package org.rulii.util.reflect;

import org.rulii.lib.spring.util.Assert;
import org.rulii.model.UnrulyException;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The MethodHandleMethodExecutor class is an implementation of the MethodExecutor interface that
 * uses MethodHandle to execute a method on a target object.
 *
 * <p>The handle is adapted once, when the executor is created, to take every argument and return its result as
 * an {@code Object}; a call is then one exact invocation of it. The conversions are the ones
 * {@link MethodHandle#invokeWithArguments(Object...)} applies (arguments are cast and unboxed on the way in, the
 * result is boxed on the way out), so a wrong argument still fails with a {@link ClassCastException} and a
 * {@code null} for a primitive with a {@link NullPointerException}.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class MethodHandleMethodExecutor implements MethodExecutor {

    private static final Object[] NO_ARGS = new Object[0];
    // Up to this many arguments are handed to the handle one by one; a longer list goes as one array.
    private static final int MAX_DIRECT_ARGS = 4;

    // One adapted handle per method, shared by every executor of that method: all lambdas of one shape run through
    // the same interface method, and a rule class may be instantiated many times. Kept with the declaring class,
    // so it is released when the class is.
    private static final ClassValue<Map<Method, MethodHandle>> HANDLES = new ClassValue<>() {
        @Override
        protected Map<Method, MethodHandle> computeValue(Class<?> type) {
            return new ConcurrentHashMap<>();
        }
    };

    private final Method method;
    private final int parameterCount;
    private final boolean staticMethod;
    // The method's handle with every parameter and the result as Object: (Object target, Object arg, ...) -> Object,
    // without the target for a static method. With more than MAX_DIRECT_ARGS parameters the arguments are one
    // array instead: (Object target, Object[] args) -> Object.
    private final MethodHandle handle;

    /**
     * The MethodHandleMethodExecutor class is an implementation of the MethodExecutor interface that
     * uses MethodHandle to execute a method on a target object.
     */
    public MethodHandleMethodExecutor(Method method) throws IllegalAccessException {
        super();
        Assert.notNull(method, "method cannot be null.");
        this.method = method;
        this.parameterCount = method.getParameterCount();
        this.staticMethod = Modifier.isStatic(method.getModifiers());
        this.handle = adapt(method);
    }

    private static MethodHandle adapt(Method method) throws IllegalAccessException {
        Map<Method, MethodHandle> handles = HANDLES.get(method.getDeclaringClass());
        MethodHandle result = handles.get(method);

        if (result == null) {
            MethodHandle methodHandle = ReflectionUtils.getMethodHandle(method);
            // Everything as Object, converted exactly as MethodHandle.invokeWithArguments would.
            result = methodHandle.asType(methodHandle.type().generic());

            if (method.getParameterCount() > MAX_DIRECT_ARGS) {
                result = result.asSpreader(Object[].class, method.getParameterCount());
            }

            MethodHandle existing = handles.putIfAbsent(method, result);
            if (existing != null) result = existing;
        }

        return result;
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
        // Null args are legal for zero-param methods (e.g. via the List overload of
        // MethodExecutor.execute) - ReflectiveMethodExecutor accepts them, so this strategy must too.
        if (userArgs == null) userArgs = NO_ARGS;

        if (parameterCount != userArgs.length) {
            throw new UnrulyException("Invalid number of args passed to Method call [" + method()
                    + "] required [" + parameterCount + "]");
        }

        // The target is not passed to a static method
        return (T) (staticMethod ? executeStatic(userArgs) : executeInstance(target, userArgs));
    }

    private Object executeInstance(Object target, Object[] args) throws Throwable {
        switch (parameterCount) {
            case 0: return (Object) handle.invokeExact(target);
            case 1: return (Object) handle.invokeExact(target, args[0]);
            case 2: return (Object) handle.invokeExact(target, args[0], args[1]);
            case 3: return (Object) handle.invokeExact(target, args[0], args[1], args[2]);
            case 4: return (Object) handle.invokeExact(target, args[0], args[1], args[2], args[3]);
            default: return (Object) handle.invokeExact(target, args);
        }
    }

    private Object executeStatic(Object[] args) throws Throwable {
        switch (parameterCount) {
            case 0: return (Object) handle.invokeExact();
            case 1: return (Object) handle.invokeExact(args[0]);
            case 2: return (Object) handle.invokeExact(args[0], args[1]);
            case 3: return (Object) handle.invokeExact(args[0], args[1], args[2]);
            case 4: return (Object) handle.invokeExact(args[0], args[1], args[2], args[3]);
            default: return (Object) handle.invokeExact(args);
        }
    }

    @Override
    public final Method method() {
        return method;
    }
}
