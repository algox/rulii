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
import org.rulii.lib.spring.util.ClassUtils;
import org.rulii.model.UnrulyException;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

/**
 * Lambda related utilities.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public final class LambdaUtils {

    private LambdaUtils() {
        super();
    }

    /**
     * Determines if the given class is a Lambda.
     *
     * @param target target object.
     * @return true if the target class is a Lambda; false otherwise.
     */
    public static boolean isLambda(Object target) {
        Assert.notNull(target, "target cannot be null.");

        try {
            return getSafeSerializedLambda(target) != null;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    /**
     * Returns the Serialized form of the Lambda.
     *
     * @param target Lambda object
     * @return Serialized form of the given lambda; null in case of any error.
     */
    public static SerializedLambda getSafeSerializedLambda(Object target) {
        Assert.notNull(target, "target cannot be null.");

        try {
            if (!target.getClass().isSynthetic()) return null;
            if (!(target instanceof Serializable)) return null;
            return (getSerializedLambda((Serializable) target));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns the Serialized form of the Lambda.
     *
     * @param target Lambda object
     * @return Serialized form of the given lambda.
     * @throws UnrulyException if the given target object really isn't a Lambda or if we are unable to deserialize the Lambda.
     */
    public static SerializedLambda getSerializedLambda(Serializable target) {
        Assert.notNull(target, "target cannot be null.");
        Method writeReplaceMethod = getWriteReplaceMethod(target.getClass());

        try {
            // Make sure we found the method
            if (writeReplaceMethod == null) {
                throw new UnrulyException("Unable to find writeReplace method! Not a SerializedLambda?");
            }
            // Make it callable
            writeReplaceMethod.setAccessible(true);
            Object result = writeReplaceMethod.invoke(target);

            if (!(result instanceof SerializedLambda)) {
                throw new UnrulyException("writeReplaceMethod did not return a SerializedLambda ["
                        + result + "]. is this Lambda?");
            }

            return (SerializedLambda) writeReplaceMethod.invoke(target);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new UnrulyException("Unable to execute writeReplace method! [" + writeReplaceMethod + "]");
        }
    }

    /**
     * Return the Lambda implementation class.
     *
     * @param lambda serialized lambda form
     * @return Lambda implementation class.
     * @throws UnrulyException if we are unable to load the implementing Class.
     */
    public static Class<?> getImplementationClass(SerializedLambda lambda) {
        Assert.notNull(lambda, "lambda cannot be null.");
        String className = null;

        try {
            className = lambda.getImplClass().replaceAll("/", ".");
            return ClassUtils.forName(className, null);
        } catch (ClassNotFoundException e) {
            throw new UnrulyException("Unable to load the implementing Lambda class [" + className + "]");
        }
    }

    /**
     * Returns the Lambda implementation method.
     *
     * @param lambda serialized lambda form
     * @param implementingClass Lambda implementation class.
     * @return Lambda implementation method.
     * @throws UnrulyException if we are unable to locate the Lambda implementing method.
     */
    public static Method getImplementationMethod(SerializedLambda lambda, Class<?> implementingClass) {
        Assert.notNull(lambda, "lambda cannot be null.");
        Assert.notNull(implementingClass, "implementingClass cannot be null.");
        Optional<Method> result = Arrays.stream(implementingClass.getDeclaredMethods())
                .filter(method -> method.getName().equals(lambda.getImplMethodName()))
                .findFirst();

        if (result.isEmpty()) {
            throw new UnrulyException("Unable to find implementing Lambda method on class [" + implementingClass + "]");
        }

        return result.get();
    }

    /**
     * Finds the implementation method of a Lambda.
     *
     * @param target lambda object.
     * @return implementation method if one is found.
     */
    public static Method getImplementationMethod(Object target) {
        Assert.notNull(target, "target cannot be null.");
        SerializedLambda lambda = getSafeSerializedLambda(target);

        if (lambda == null) throw new UnrulyException("Target not a Lambda");
        Class<?> clazz = getImplementationClass(lambda);
        return getImplementationMethod(lambda, clazz);
    }

    /**
     * Finds the writeReplace method.
     *
     * @param c target class.
     * @return writeReplace method if found; null otherwise.
     */
    private static Method getWriteReplaceMethod(Class<?> c) {

        try {
            return c.getDeclaredMethod("writeReplace");
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
}
