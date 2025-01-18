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
package org.rulii.test.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.lib.spring.core.ParameterNameDiscoverer;
import org.rulii.model.Runnable;
import org.rulii.model.action.Action;
import org.rulii.model.function.TriFunction;
import org.rulii.model.function.UnaryFunction;
import org.rulii.util.reflect.LambdaUtils;

import java.io.Serial;
import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.math.BigDecimal;

/**
 * Test cases related to LambdaUtils.
 *
 * @author Max Arulananthan
 */
public final class LambdaUtilsTest {

    public LambdaUtilsTest() {
        super();
    }

    @Test
    public void test1() {
        UnaryFunction<Boolean, Integer> rule1 = (Integer x) -> x > 10;

        UnaryFunction<Boolean, Integer> rule2 = new UnaryFunction<Boolean, Integer>() {
            @Override
            public Boolean apply(Integer arg) {
                return null;
            }
        };

        Assertions.assertTrue(LambdaUtils.isLambda(rule1));
        Assertions.assertFalse(LambdaUtils.isLambda(rule2));

        SerializedLambda lambda = LambdaUtils.getSerializedLambda(rule1);
        Class<?> implementationClass = LambdaUtils.getImplementationClass(lambda);
        Method implementationMethod = LambdaUtils.getImplementationMethod(lambda, implementationClass);

        Assertions.assertEquals(implementationMethod.getName(), lambda.getImplMethodName());
        Assertions.assertEquals(implementationMethod.getReturnType(), Boolean.class);
        Assertions.assertEquals(implementationMethod.getParameterTypes()[0], Integer.class);
    }

    @Test
    public void test2() {
        TriFunction<Boolean, Integer, String, BigDecimal> rule3 = (Integer xxx, String value, BigDecimal salary) -> xxx > 10 && salary != null;
        SerializedLambda lambda = LambdaUtils.getSerializedLambda(rule3);
        Class<?> implementationClass = LambdaUtils.getImplementationClass(lambda);
        Method implementationMethod = LambdaUtils.getImplementationMethod(lambda, implementationClass);

        ParameterNameDiscoverer discoverer = ParameterNameDiscoverer.create();
        String[] names = discoverer.getParameterNames(implementationMethod);

        Assertions.assertEquals(3, names.length);
        Assertions.assertEquals("xxx", names[0]);
        Assertions.assertEquals("value", names[1]);
        Assertions.assertEquals("salary", names[2]);
    }

    @Test
    public void test3() throws NoSuchMethodException {

        TriFunction<Boolean, Integer, String, BigDecimal> rule3 = new TriFunction<Boolean, Integer, String, BigDecimal>() {
            @Override
            public Boolean apply(Integer xxx, String value, BigDecimal salary) {
                return false;
            }
        };

        Method implementationMethod = rule3.getClass().getDeclaredMethod("apply", Integer.class, String.class, BigDecimal.class);
        ParameterNameDiscoverer discoverer = ParameterNameDiscoverer.create();
        String[] names = discoverer.getParameterNames(implementationMethod);

        Assertions.assertEquals(3, names.length);
        Assertions.assertEquals("xxx", names[0]);
        Assertions.assertEquals("value", names[1]);
        Assertions.assertEquals("salary", names[2]);
    }

    @Test
    public void testIsLambdaWithActualLambda() {
        //Initialize a lambda
        Runnable<Void> lambda = (ctx) -> {
            System.out.println("This is a test lambda.");
            return null;
        };
        //Test LambdaUtils#isLambda with the lambda
        Assertions.assertTrue(LambdaUtils.isLambda(lambda), "LambdaUtils failed to recognize a valid lambda.");
    }

    @Test
    public void testIsLambdaWithNonLambda() {
        //Initialize a non-lambda Serializable object
        Serializable nonLambda = new Serializable() {
            @Serial
            private static final long serialVersionUID = 1L;
        };

        //Test LambdaUtils#isLambda with the non-lambda object
        Assertions.assertFalse(LambdaUtils.isLambda(nonLambda), "LambdaUtils recognized a non-lambda object as a lambda.");
    }

    @Test
    void testIsLambdaWithNull() {
        //Test LambdaUtils#isLambda with null input
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> LambdaUtils.isLambda(null));
        Assertions.assertEquals("target cannot be null.", exception.getMessage());
    }
}
