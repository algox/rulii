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
import org.rulii.bind.BindingDeclaration;
import org.rulii.bind.Bindings;
import org.rulii.model.function.Function;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.rulii.model.function.Functions.function;

/**
 * Tests for the {@code run(Object params)} dispatch method on {@link org.rulii.model.Runnable}.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class RunnableTest {

    public RunnableTest() {
        super();
    }

    public static class Order {
        private final String orderId;
        private final int amount;

        Order(String orderId, int amount) {
            super();
            this.orderId = orderId;
            this.amount = amount;
        }

        public String getOrderId() { return orderId; }
        public int getAmount() { return amount; }
    }

    @Test
    public void testRunWithPojo_bindsJavaBeanProperties() {
        Function<String> f = function((String orderId) -> orderId);
        String result = f.run(new Order("ORD-001", 100));
        assertEquals("ORD-001", result);
    }

    @Test
    public void testRunWithPojo_multipleProperties() {
        Function<Integer> f = function((String orderId, Integer amount) -> amount);
        Integer result = f.run(new Order("ORD-002", 250));
        assertEquals(250, result);
    }

    @Test
    public void testRunWithMap_bindsMapEntries() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("orderId", "ORD-003");
        Function<String> f = function((String orderId) -> orderId);
        String result = f.run(params);
        assertEquals("ORD-003", result);
    }

    @Test
    public void testRunWithMap_multipleEntries() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("name", "Alice");
        params.put("age", 30);
        Function<Integer> f = function((Integer age) -> age);
        Integer result = f.run(params);
        assertEquals(30, result);
    }

    @Test
    public void testRunWithBindingDeclaration_delegatesCorrectly() {
        Function<Integer> f = function((Integer age) -> age);
        BindingDeclaration<?> d = age -> 42;
        Integer result = f.run(d);
        assertEquals(42, result);
    }

    @Test
    public void testRunWithBindings_delegatesCorrectly() {
        Function<String> f = function((String name) -> name);
        Bindings bindings = Bindings.builder().standard();
        bindings.bind(name -> "Bob");
        String result = f.run(bindings);
        assertEquals("Bob", result);
    }

    @Test
    public void testRunWithNull_throwsIllegalArgumentException() {
        Function<String> f = function((String name) -> name);
        assertThrows(IllegalArgumentException.class, () -> f.run((Object) null));
    }
}
