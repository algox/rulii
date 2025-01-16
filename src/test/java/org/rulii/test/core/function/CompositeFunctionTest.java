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
import org.rulii.bind.Bindings;
import org.rulii.model.UnrulyException;
import org.rulii.model.function.Function;
import org.rulii.model.function.Functions;

/**
 * The CompositeFunctionTest class contains unit tests for composing functions using the Function interface.
 *
 * @author Max Arulananthan
 * @since 1.0
 *
 */
public class CompositeFunctionTest {

    public CompositeFunctionTest() {
        super();
    }

    @Test
    public void testCompose1() throws UnrulyException {
        Function<Integer> addOne = Functions.function((Integer x) -> x + 1);
        Function<Integer> multiplyByTwo = Functions.function((Integer value) -> value * 2);

        Bindings bindings = Bindings.builder().standard();
        bindings.bind("x", 5);

        Function<Integer> composedFunc = multiplyByTwo.compose(addOne, "value");
        Integer result = composedFunc.apply(bindings);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(12, result);
    }

    @Test
    public void testCompose2() throws UnrulyException {
        Function<Integer> multiplyByTwo = Functions.function((Integer x) -> x * 2);
        Function<Integer> addOne = Functions.function((Integer value) -> value + 1);

        Bindings bindings = Bindings.builder().standard();
        bindings.bind("x", 5);

        Function<Integer> composedFunc = multiplyByTwo.andThen(addOne, "value");
        Integer result = composedFunc.apply(bindings);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(11, result);
    }
}
