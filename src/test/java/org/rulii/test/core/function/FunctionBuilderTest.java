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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.model.Definable;
import org.rulii.model.MethodDefinition;
import org.rulii.model.function.Function;

import java.math.BigDecimal;

public class FunctionBuilderTest {

    public FunctionBuilderTest() {
        super();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testNoArg() {
        Function<Boolean> function = Function.builder()
                .with(() -> true)
                .name("function0")
                .build();

        Assertions.assertEquals("function0", ((Definable<MethodDefinition>) function).getDefinition().getName());
        Assertions.assertEquals(0, ((Definable<MethodDefinition>) function).getDefinition().getParameterDefinitions().size());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void test2Args() {
        Function<BigDecimal> function = Function.builder()
                .with((String x, BigDecimal value) -> value)
                .build();

        Assertions.assertEquals(2, ((Definable<MethodDefinition>) function).getDefinition().getParameterDefinitions().size());
        Assertions.assertEquals("value", ((Definable<MethodDefinition>) function).getDefinition().getParameterDefinitions().get(1).getName());
        Assertions.assertEquals(((Definable<MethodDefinition>) function).getDefinition().getParameterDefinitions().get(1).getType(), BigDecimal.class);

        function.apply(x -> "123", value -> new BigDecimal("10.00"));
    }
}
