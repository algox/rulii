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
package org.rulii.test.bind.match;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.bind.Binding;
import org.rulii.bind.Bindings;
import org.rulii.bind.ScopedBindings;
import org.rulii.bind.match.BindingMatchingStrategyType;
import org.rulii.bind.match.ParameterMatch;
import org.rulii.bind.match.ParameterResolver;
import org.rulii.convert.ConverterRegistry;
import org.rulii.model.MethodDefinition;
import org.rulii.model.action.Action;
import org.rulii.util.reflect.ObjectFactory;

import java.math.BigDecimal;
import java.util.List;

public class GenericParameterResolverTest<T extends CharSequence> {

    public GenericParameterResolverTest() {
        super();
    }

    @Test
    public void test1() {
        Action action = Action.builder()
                .with((Binding<BigDecimal> bind, T match) -> {}).build();

        ParameterResolver resolver = ParameterResolver.builder().build();
        MethodDefinition definition = action.getDefinition();

        ScopedBindings bindings = Bindings.builder().scoped();

        Binding<BigDecimal> var2 = bindings.bind(bind -> new BigDecimal("1000.01"));
        bindings.addScope();
        bindings.bind(a -> "genericMatch");

        List<ParameterMatch> matches = resolver.match(definition, bindings,
                BindingMatchingStrategyType.MATCH_BY_NAME_THEN_BY_TYPE.getStrategy(),
                ObjectFactory.builder().build());
        List<Object> values = resolver.resolve(matches, definition, bindings,
                BindingMatchingStrategyType.MATCH_BY_NAME_THEN_BY_TYPE.getStrategy(),
                ConverterRegistry.builder().build(), ObjectFactory.builder().build());

        Assertions.assertEquals(var2, values.get(0));
        Assertions.assertEquals("genericMatch", values.get(1));
    }
}
