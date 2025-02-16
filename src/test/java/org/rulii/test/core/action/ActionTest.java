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
package org.rulii.test.core.action;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.bind.Binding;
import org.rulii.bind.Bindings;
import org.rulii.context.RuleContext;
import org.rulii.model.UnrulyException;
import org.rulii.model.action.Action;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.rulii.model.action.Actions.action;

/**
 * Action related tests.
 *
 * @author Max Arulananthan
 * @since 1.0
 *
 */
public class ActionTest {

    public ActionTest() {
        super();
    }

    @Test
    public void test1() {
        Action action = Action.builder().with((String arg1, String arg2) -> {
            Assertions.assertNull(arg1);
            Assertions.assertNull(arg2);
        }).build();
        action.run();
    }

    @Test
    public void test2() {
        Action action = Action.builder().with((String arg1, String arg2) -> {
            Assertions.assertEquals("a", arg1);
            Assertions.assertEquals("b", arg2);
        }).build();

        action.run(arg1 -> "a", arg2 -> "b");
    }

    @Test
    public void test3() {
        Assertions.assertThrows(UnrulyException.class, () -> {
            Action action = Action.builder().with((String arg1, String arg2) -> {
            }).build();
            action.run(arg1 -> 123, arg2 -> 12);
        });
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void test4() {
        Action action = action((String var1, Optional<String> var2, Optional<BigDecimal> var3) -> {
            Assertions.assertEquals(var1, "a");
            Assertions.assertEquals(var2, Optional.empty());
            Assertions.assertEquals(var3.get(), new BigDecimal("10.00"));
        });

        action.run(RuleContext.builder().with(var1 -> "a", var3 -> new BigDecimal("10.00")).build());
    }

    @Test
    public void test5() {
        Action action = action((Byte var1, Boolean var2, Character var3, Double var4, Float var5, Integer var6, Long var7, Short var8) -> {
            Assertions.assertEquals(2, (byte) var1);
            Assertions.assertEquals(var2, true);
            Assertions.assertEquals('c', (char) var3);
            Assertions.assertEquals(100.100, var4);
            Assertions.assertEquals(10.10f, var5);
            Assertions.assertEquals(10, (int) var6);
            Assertions.assertEquals(10000788, (long) var7);
            Assertions.assertEquals(200, (short) var8);
        });


        action.run(RuleContext.builder().with(var1 -> "2", var2-> "true", var3 -> 'c', var4 -> "100.100",
                var5 -> "10.10", var6 -> "10", var7 -> "10000788", var8 -> "200").build());
    }

    @Test
    public void test6() {
        List<Action> actions = Action.builder().build(new ActionContainer());

        actions.get(0).run();
        actions.get(1).run(RuleContext.builder().with(var3 -> new BigDecimal("10.00")).build());
        actions.get(2).run(arg1 -> "a", arg2 -> "b");
        actions.get(3).run();
    }

    @Test
    public void test7() {
        Assertions.assertThrows(UnrulyException.class, () -> {
            List<Action> actions = Action.builder().build(new ActionContainer());
            actions.get(3).run(arg1 -> 123, arg2 -> 12);
        });
    }

    @Test
    public void test8() {
        List<Action> actions = Action.builder().build(new ActionContainer());
        // Test Order
        Assertions.assertEquals("action4", actions.get(0).getName());
        Assertions.assertEquals("action3", actions.get(1).getName());
        Assertions.assertEquals("action2", actions.get(2).getName());
        Assertions.assertEquals("action1", actions.get(3).getName());
    }

    @Test
    public void test9() {
        Assertions.assertThrows(UnrulyException.class, () -> {
            List<Action> actions = Action.builder().build(new ActionContainer());
            actions.get(4).run();
        });
    }

    @Test
    public void test10() {
        List<Action> actions = Action.builder().build(new ActionContainer());
        Bindings bindings = Bindings.builder().standard(var9 -> new BigDecimal("0.00"));
        actions.get(4).run(bindings);
        Assertions.assertEquals(bindings.getValue("var9"), new BigDecimal("400.00"));
    }

    @Test
    public void test11() {
        Assertions.assertThrows(UnrulyException.class, () -> {
            List<Action> actions = Action.builder().build(new ActionContainer());
            Bindings bindings = Bindings.builder().standard();
            bindings.bind(Binding.builder().constant("arg9", new BigDecimal("0.00")));
            actions.get(4).run(bindings);
        });
    }

    @Test
    public void test12() {
        List<Action> actions = Action.builder().build(new ActionContainer());
        Bindings bindings = Bindings.builder().standard();
        bindings.bind(Binding.builder().finalBinding("arg9", new BigDecimal("0.00")));
        actions.get(4).run(bindings);
    }

    @Test
    public void test13() {
        Assertions.assertThrows(UnrulyException.class, () -> {
            List<Action> actions = Action.builder().build(new ActionContainer());
            Bindings bindings = Bindings.builder().standard();
            bindings.bind(Binding.builder().finalConstant("arg9", new BigDecimal("0.00")));
            actions.get(4).run(bindings);
        });
    }

    @Test
    public void test14() {
        List<Action> actions = Action.builder().build(new ActionContainer());
        Bindings bindings = Bindings.builder().standard();
        bindings.bind(Binding.builder().with ("arg9", BigDecimal.class, new BigDecimal("0.00")).build());
        actions.get(4).run(bindings);
        Assertions.assertEquals(new BigDecimal("400.00"), bindings.getValue("arg9"));
    }

    @Test
    public void test15() {
        List<Action> actions = Action.builder().build(new ActionContainer());
        Bindings bindings = Bindings.builder().standard();
        Binding<?> binding1 = bindings.bind(arg1 -> (byte) 111);
        Binding<?> binding2 = bindings.bind(arg2 -> false);
        Binding<?> binding3 = bindings.bind(arg3 -> 'c');
        Binding<?> binding4 = bindings.bind(arg4 -> 1200.05);
        Binding<?> binding5 = bindings.bind(arg5 -> 1000.05f);
        Binding<?> binding6 = bindings.bind(arg6 -> 12000);
        Binding<?> binding7 = bindings.bind(arg7 -> 120000000L);
        Binding<?> binding8 = bindings.bind(arg8 -> (short) 1024);
        Binding<?> binding9 = bindings.bind(arg9 -> new Person("Kobe", "Bryant"));

        actions.get(5).run(bindings);

        Assertions.assertEquals((byte) binding1.getValue(), (byte) 100);
        Assertions.assertEquals(binding2.getValue(), true);
        Assertions.assertEquals((char) binding3.getValue(), 'z');
        Assertions.assertEquals(1500.5555, (double) binding4.getValue());
        Assertions.assertEquals(10.11f, (float) binding5.getValue());
        Assertions.assertEquals((int) binding6.getValue(), 34895795);
        Assertions.assertEquals((long) binding7.getValue(), 348934980395L);
        Assertions.assertEquals((short) binding8.getValue(), (short) 2398);
        Assertions.assertEquals(binding9.getValue(), new Person("Micahel", "Jordan"));
    }

    private static class ActionContainer {

        public ActionContainer() {
            super();
        }

        @org.rulii.annotation.Action(order = 4)
        public void action1(String arg1, String arg2) {
            Assertions.assertNull(arg1);
            Assertions.assertNull(arg2);
        }

        @org.rulii.annotation.Action(order = 3)
        public void action2(String arg1, String arg2, Integer arg3) {
            Assertions.assertEquals("a", arg1);
            Assertions.assertEquals("b", arg2);
        }

        @SuppressWarnings({"OptionalGetWithoutIsPresent", "OptionalUsedAsFieldOrParameterType"})
        @org.rulii.annotation.Action(order = 2)
        public void action3(int var1, Optional<String> var2, Optional<BigDecimal> var3) {
            Assertions.assertEquals(var1, 0);
            Assertions.assertEquals(var2, Optional.empty());
            Assertions.assertEquals(var3.get(), new BigDecimal("10.00"));
        }

        @org.rulii.annotation.Action(order = 1)
        public void action4(byte var1, boolean var2, char var3, double var4, float var5, int var6, long var7, short var8) {
            Assertions.assertEquals(0, var1);
            Assertions.assertFalse(var2);
            Assertions.assertEquals(0, var4);
            Assertions.assertEquals(0f, var5);
            Assertions.assertEquals(0, var6);
            Assertions.assertEquals(0L, var7);
            Assertions.assertEquals(0, var8);
        }

        @org.rulii.annotation.Action(order = 5)
        public void action5(Binding<BigDecimal> var9) {
            var9.setValue(new BigDecimal("400.00"));
        }

        @org.rulii.annotation.Action(order = 6)
        public void action6(Binding<Byte> arg1, Binding<Boolean> arg2, Binding<Character> arg3, Binding<Double> arg4,
                            Binding<Float> arg5, Binding<Integer> arg6, Binding<Long> arg7, Binding<Short> arg8, Binding<Person> arg9) {
            Assertions.assertNotNull(arg1);
            Assertions.assertNotNull(arg2);
            Assertions.assertNotNull(arg3);
            Assertions.assertNotNull(arg4);
            Assertions.assertNotNull(arg5);
            Assertions.assertNotNull(arg6);
            Assertions.assertNotNull(arg7);
            Assertions.assertNotNull(arg8);
            Assertions.assertNotNull(arg9);

            Assertions.assertEquals((byte) arg1.getValue(), (byte) 111);
            Assertions.assertEquals(arg2.getValue(), false);
            Assertions.assertEquals((char) arg3.getValue(), 'c');
            Assertions.assertEquals(1200.05, arg4.getValue());
            Assertions.assertEquals(1000.05f, arg5.getValue());
            Assertions.assertEquals((int) arg6.getValue(), 12000);
            Assertions.assertEquals((long) arg7.getValue(), 120000000L);
            Assertions.assertEquals((short) arg8.getValue(), (short) 1024);
            Assertions.assertEquals(arg9.getValue(), new Person("Kobe", "Bryant"));

            arg1.setValue((byte) 100);
            arg2.setValue(true);
            arg3.setValue('z');
            arg4.setValue(1500.5555);
            arg5.setValue(10.11f);
            arg6.setValue(34895795);
            arg7.setValue(348934980395L);
            arg8.setValue((short) 2398);
            arg9.setValue(new Person("Micahel", "Jordan"));
        }
    }

    private static class Person {
        private String firstName;
        private String lastName;

        public Person(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Person person = (Person) o;
            return Objects.equals(firstName, person.firstName) && Objects.equals(lastName, person.lastName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(firstName, lastName);
        }
    }
}
