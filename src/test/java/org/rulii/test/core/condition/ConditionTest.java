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
package org.rulii.test.core.condition;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.bind.Binding;
import org.rulii.context.RuleContext;
import org.rulii.model.UnrulyException;
import org.rulii.model.condition.Condition;
import org.rulii.model.function.TriFunction;
import org.rulii.util.TypeReference;

import java.math.BigDecimal;
import java.util.*;

import static org.rulii.model.condition.Conditions.condition;

/**
 * Tests related to the ConditionBuilder.
 *
 * @author Max Arulananthan.
 */
public class ConditionTest {

    public ConditionTest() {
        super();
    }

    @Test
    public void testCondition0() {
        Condition condition = Condition.builder()
                .with(() -> true)
                .build();
        Assertions.assertTrue(condition.isTrue());
    }

    @Test
    public void testCondition2() {
        Condition condition = Condition.builder()
                .with((Integer a, String x) -> a > 55)
                .build();
        Assertions.assertTrue(condition.isTrue(a -> 56, x -> "abc"));
    }

    @Test
    public void testCondition3() {
        Condition condition = Condition.builder()
                .with((Integer a, Date date, String x) -> a != null)
                .build();
        Assertions.assertTrue(condition.isTrue(a -> 10, date -> new Date(), x -> "abc"));
    }

    @Test
    public void testCondition4() {
        Condition condition = Condition.builder()
                .with((Integer a, Date date, String x, List<String> values) -> a > 35 && x.equals("x")
                        && values.get(0).equals("one"))
                .param(3)
                    .type(new TypeReference<List<String>>(){}.getType())
                    .build()
                .build();
        List<String> strings = new ArrayList<>();
        strings.add("one");
        Assertions.assertTrue(condition.isTrue(a -> 36, date -> new Date(), x -> "x", values -> strings));
    }

    @Test
    public void testCondition5() {
        Condition condition = Condition.builder()
                .with((Integer a, Date date, String x, List<String> values, Long longValue) -> a > 35 && x.equals("x")
                        && values.get(0).equals("one"))
                .param("values")
                    .type(new TypeReference<List<String>>(){}.getType())
                    .build()
                .build();
        List<String> strings = new ArrayList<>();
        strings.add("one");
        Assertions.assertTrue(condition.isTrue(a -> 36, date -> new Date(), x -> "x", values -> strings, longValue -> 0L));
    }

    @Test
    public void testCondition6() {
        Condition condition = Condition.builder()
                .with((Integer a, Date date, String x, List<String> values, Long longValue, String b) -> a > 35 && x.equals("x"))
                .build();
        Assertions.assertTrue(condition.isTrue(a -> 36, date -> new Date(), x -> "x", values -> new ArrayList<>(), longValue -> 0L, b -> "b"));
    }

    @Test
    public void testCondition7() {
        Condition condition = Condition.builder()
                .with((Integer a, Date date, String x, List<String> values, Long longValue, String b, BigDecimal big)
                        -> big.compareTo(BigDecimal.ZERO) > 0)
                .build();
        Assertions.assertTrue(condition.isTrue(a -> 36, date -> new Date(), x -> "x", values -> new ArrayList<>(), longValue -> 0L, b -> "b", big -> new BigDecimal("100")));
    }

    @Test
    public void testCondition8() {
        Condition condition = Condition.builder()
                .with((Integer a, Date date, String x, List<String> values, Long longValue, String b,
                            BigDecimal big, List<Integer> numbers) -> {
                    return numbers != null && numbers.size() > 5;
                })
                .build();
        List<Integer> numberValues = new ArrayList<>();
        numberValues.add(1);
        numberValues.add(2);
        numberValues.add(3);
        numberValues.add(4);
        numberValues.add(5);
        numberValues.add(6);
        Assertions.assertTrue(condition.isTrue(a -> 36, date -> new Date(), x -> "x", values -> new ArrayList<>(),
                longValue -> 0L, b -> "b", big -> new BigDecimal("100"), numbers -> numberValues));
    }

    @Test
    public void testCondition9() {
        Condition condition = Condition.builder()
                .with((Integer a, Date date, String x, List<String> values, Long longValue, String b,
                            BigDecimal big, List<Integer> numbers, Map<String, String> map)
                        -> map.containsKey("abcde")
                )
                .description("some description")
                .build();
        Assertions.assertEquals("some description", condition.getDefinition().getDescription());
    }

    @Test
    public void testCondition10() {
        Condition condition = Condition.builder()
                .with((Integer a, Date date, String x, List<String> values, Long longValue, String b,
                            BigDecimal big, List<Integer> numbers, Map<String, String> map, Integer z)
                        -> map.containsKey("abcde")
                )
                .description("some description")
                .build();
        List<Integer> numberValues = new ArrayList<>();
        numberValues.add(1);
        numberValues.add(2);
        numberValues.add(3);
        numberValues.add(4);
        numberValues.add(5);
        numberValues.add(6);
        Map<String, String> mapValues = new HashMap<>();
        mapValues.put("abcde", "xxx");

        Assertions.assertTrue(condition.isTrue(a -> 36, date -> new Date(), x -> "x", values -> new ArrayList<>(),
                longValue -> 0L, b -> "b", big -> new BigDecimal("100"), numbers -> numberValues, map -> mapValues, z -> 10));
    }

    @Test
    public void testCondition11() {
        Assertions.assertThrows(UnrulyException.class, () -> {
            Condition condition = condition((Binding<Integer> arg1) -> {
                arg1.setValue(200);
                return true;
            });

            condition.run(arg1 -> 100);
        });
    }

    @Test
    public void testCondition12() {
        Assertions.assertThrows(UnrulyException.class, () -> {
            Condition condition = condition((RuleContext ctx) -> {
                ctx.getBindings().addScope();
                return true;
            });

            condition.run(arg1 -> 100);
        });
    }

    @Test
    public void testCondition13() {
        Assertions.assertThrows(UnrulyException.class, () -> {
            Condition condition = condition((Binding<RuleContext> ruleContext) -> {
                ruleContext.getValue().getBindings().addScope();
                return true;
            });

            condition.run();
        });
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void testCondition14() {
        Assertions.assertThrows(UnrulyException.class, () -> {
            Condition condition = condition((Optional<RuleContext> ruleContext) -> {
                ruleContext.get().getBindings().addScope();
                return true;
            });

            condition.run();
        });
    }

    @Test
    public void testConditionConsumer() {
        Condition condition = condition((Integer a) -> a > 10);
        Assertions.assertTrue(condition.isTrue(a -> 13));
    }

    @Test
    public void testInnerClass() {
        Condition condition = Condition.builder()
                .with(new TriFunction<Boolean, String, Integer, Map<String, Integer>>() {
                    @Override
                    public Boolean apply(String a, Integer b, Map<String, Integer> map) {
                        return true;
                    }
                })
                .build();
        Assertions.assertEquals(3, condition.getDefinition().getParameterDefinitions().size());
        Assertions.assertEquals("map", condition.getDefinition().getParameterDefinitions().get(2).getName());
        Assertions.assertEquals(condition.getDefinition().getParameterDefinitions().get(2)
                .getType(), new TypeReference<Map<String, Integer>>() {
        }.getType());
        Assertions.assertTrue(condition.isTrue(a -> "aa", b -> 12, map -> new HashMap<>()));
    }
}
