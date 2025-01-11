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
package org.rulii.test.core.function;

import org.rulii.model.function.Function;
import org.junit.Assert;
import org.junit.Test;
import org.rulii.model.Definable;
import org.rulii.model.MethodDefinition;

import java.math.BigDecimal;

public class FunctionBuilderTest {

    public FunctionBuilderTest() {
        super();
    }

    @Test
    public void testNoArg() {
        Function<Boolean> function = Function.builder()
                .with(() -> true)
                .name("function0")
                .build();

        Assert.assertTrue(((Definable<MethodDefinition>) function).getDefinition().getName().equals("function0"));
        Assert.assertTrue(((Definable<MethodDefinition>) function).getDefinition().getParameterDefinitions().size() == 0);
    }

    @Test
    public void test2Args() {
        Function<BigDecimal> function = Function.builder()
                .with((String x, BigDecimal value) -> value)
                .build();

        Assert.assertTrue(((Definable<MethodDefinition>) function).getDefinition().getParameterDefinitions().size() == 2);
        Assert.assertTrue(((Definable<MethodDefinition>) function).getDefinition().getParameterDefinitions().get(1).getName().equals("value"));
        Assert.assertTrue(((Definable<MethodDefinition>) function).getDefinition().getParameterDefinitions().get(1).getType().equals(BigDecimal.class));

        function.apply(x -> "123", value -> new BigDecimal("10.00"));
    }
}
