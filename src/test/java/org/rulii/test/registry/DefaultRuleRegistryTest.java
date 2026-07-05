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
package org.rulii.test.registry;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.context.RuleContext;
import org.rulii.model.Runnable;
import org.rulii.registry.AlreadyRegisteredException;
import org.rulii.registry.DefaultRuleRegistry;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Tests for DefaultRuleRegistry.
 *
 * @author Max Arulananthan
 */
public class DefaultRuleRegistryTest {

    public DefaultRuleRegistryTest() {
        super();
    }

    private static Runnable<Object> namedRunnable(String name) {
        return new Runnable<>() {
            @Override
            public Object run(RuleContext ruleContext) {
                return null;
            }

            @Override
            public String getName() {
                return name;
            }
        };
    }

    @Test
    public void testRegister_duplicateName_exceptionCarriesExistingNotNewRunnable() {
        DefaultRuleRegistry registry = new DefaultRuleRegistry();
        Runnable<Object> first = namedRunnable("dup");
        Runnable<Object> second = namedRunnable("dup");

        registry.register(first);
        AlreadyRegisteredException ex = Assertions.assertThrows(AlreadyRegisteredException.class,
                () -> registry.register(second));

        Assertions.assertSame(first, ex.getExistingValue());
    }

    @Test
    public void testRegister_blankName_throwsIllegalArgumentException() {
        DefaultRuleRegistry registry = new DefaultRuleRegistry();
        Runnable<Object> blankNamed = namedRunnable("   ");

        Assertions.assertThrows(IllegalArgumentException.class, () -> registry.register(blankNamed));
        Assertions.assertEquals(0, registry.getCount());
    }

    @Test
    public void testConcurrentRegister_sameName_onlyOneWinner_loserThrowsWithCorrectExisting() throws Exception {
        int iterations = 200;
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            for (int i = 0; i < iterations; i++) {
                String name = "concurrent-rule-" + i;
                DefaultRuleRegistry registry = new DefaultRuleRegistry();
                Runnable<Object> a = namedRunnable(name);
                Runnable<Object> b = namedRunnable(name);
                CyclicBarrier barrier = new CyclicBarrier(2);
                AtomicInteger successCount = new AtomicInteger();
                AtomicReference<AlreadyRegisteredException> caught = new AtomicReference<>();

                Callable<Void> taskA = () -> {
                    barrier.await();
                    try {
                        registry.register(a);
                        successCount.incrementAndGet();
                    } catch (AlreadyRegisteredException e) {
                        caught.set(e);
                    }
                    return null;
                };
                Callable<Void> taskB = () -> {
                    barrier.await();
                    try {
                        registry.register(b);
                        successCount.incrementAndGet();
                    } catch (AlreadyRegisteredException e) {
                        caught.set(e);
                    }
                    return null;
                };

                List<Future<Void>> futures = executor.invokeAll(List.of(taskA, taskB));
                for (Future<Void> future : futures) future.get();

                Assertions.assertEquals(1, successCount.get(),
                        "iteration " + i + ": exactly one of the two concurrent registrations must win");
                Assertions.assertEquals(1, registry.getCount());

                AlreadyRegisteredException loserException = caught.get();
                Assertions.assertNotNull(loserException,
                        "iteration " + i + ": the losing registration must throw AlreadyRegisteredException");
                Runnable<?> winner = registry.get(name);
                Assertions.assertSame(winner, loserException.getExistingValue());
            }
        } finally {
            executor.shutdown();
        }
    }
}
