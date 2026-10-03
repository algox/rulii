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
package org.rulii.test.core;

import org.junit.jupiter.api.Test;
import org.rulii.model.MethodDefinition;
import org.rulii.model.ParameterDefinition;
import org.rulii.model.ReturnTypeDefinition;
import org.rulii.model.action.Action;
import org.rulii.model.condition.Condition;
import org.rulii.model.function.Function;
import org.rulii.rule.Rule;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Guards against the static definition caches growing without bound.
 *
 * <p>{@code Class#getDeclaredMethods()} hands back a fresh {@code Method} copy on every call, so a
 * cache keyed by {@code Method} identity never hit for a lambda and gained one entry per build.
 * The caches now key by {@code Method} value; building the same lambda repeatedly must not add
 * entries. A warm-up loop runs first so that the assertion checks growth only, regardless of what
 * other tests in the same JVM have already cached.
 *
 * @author Max Arulananthan
 * @since 2.1
 */
public class MethodDefinitionCacheTest {

    private static final int WARM_UP = 10;
    private static final int ITERATIONS = 1000;

    public MethodDefinitionCacheTest() {
        super();
    }

    // Each build goes through a helper so that the warm-up and the measured loop hit the same
    // lambda call site; two call sites would be two synthetic methods and two legitimate entries.

    @Test
    public void sameLambdaBuiltRepeatedlyDoesNotGrowCaches() throws Exception {
        for (int i = 0; i < WARM_UP; i++) {
            buildCondition();
        }

        Sizes before = Sizes.capture();

        for (int i = 0; i < ITERATIONS; i++) {
            buildCondition();
        }

        before.assertUnchanged();
    }

    @Test
    public void capturingLambdaBuiltRepeatedlyDoesNotGrowCaches() throws Exception {
        for (int i = 0; i < WARM_UP; i++) {
            buildCapturingFunction(i);
        }

        Sizes before = Sizes.capture();

        for (int i = 0; i < ITERATIONS; i++) {
            buildCapturingFunction(i);
        }

        before.assertUnchanged();
    }

    @Test
    public void lambdaRuleBuiltRepeatedlyDoesNotGrowCaches() throws Exception {
        for (int i = 0; i < WARM_UP; i++) {
            buildRule(i);
        }

        Sizes before = Sizes.capture();

        for (int i = 0; i < ITERATIONS; i++) {
            buildRule(i);
        }

        before.assertUnchanged();
    }

    private static void buildCondition() {
        Condition.builder().with((Integer age) -> age >= 18).build();
    }

    private static void buildCapturingFunction(int limit) {
        Function.builder().with((Integer x) -> x + limit).build();
    }

    private static void buildRule(int i) {
        Rule.builder().name("rule" + i)
                .given(Condition.builder().with((Integer a) -> a > 0).build())
                .then(Action.builder().with((Integer a) -> {}).build())
                .build();
    }

    private record Sizes(int methods, int parameters, int returnTypes) {

        static Sizes capture() throws Exception {
            return new Sizes(cacheSize(MethodDefinition.class), cacheSize(ParameterDefinition.class),
                    cacheSize(ReturnTypeDefinition.class));
        }

        void assertUnchanged() throws Exception {
            Sizes now = capture();
            assertEquals(methods, now.methods, "MethodDefinition.CACHE grew");
            assertEquals(parameters, now.parameters, "ParameterDefinition.CACHE grew");
            assertEquals(returnTypes, now.returnTypes, "ReturnTypeDefinition.CACHE grew");
        }

        private static int cacheSize(Class<?> c) throws Exception {
            Field field = c.getDeclaredField("CACHE");
            field.setAccessible(true);
            return ((Map<?, ?>) field.get(null)).size();
        }
    }
}
