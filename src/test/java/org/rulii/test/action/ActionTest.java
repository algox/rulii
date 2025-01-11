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
package org.rulii.test.action;

import org.rulii.annotation.Order;
import org.rulii.bind.Binding;
import org.rulii.bind.Bindings;
import org.rulii.bind.InvalidBindingException;
import org.rulii.context.RuleContext;
import org.rulii.model.ParameterMismatchException;
import org.rulii.model.action.Action;
import org.rulii.model.UnrulyException;
import org.junit.Assert;
import org.junit.Test;

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
 */
public class ActionTest {

    public ActionTest() {
        super();
    }

    @Test
    public void test1() {
        Action action = Action.builder().with((String arg1, String arg2) -> {
            Assert.assertNull(arg1);
            Assert.assertNull(arg2);
        }).build();
        action.run();
    }

    @Test
    public void test2() {
        Action action = Action.builder().with((String arg1, String arg2) -> {
            Assert.assertEquals("a", arg1);
            Assert.assertEquals("b", arg2);
        }).build();

        action.run(arg1 -> "a", arg2 -> "b");
    }

    @Test(expected = UnrulyException.class)
    public void test3() {
        Action action = Action.builder().with((String arg1, String arg2) -> {}).build();
        action.run(arg1 -> 123, arg2 -> 12);
    }

    @Test
    public void test4() {
        Action action = action((String var1, Optional<String> var2, Optional<BigDecimal> var3) -> {
            Assert.assertEquals(var1, "a");
            Assert.assertEquals(var2, Optional.empty());
            Assert.assertEquals(var3.get(), new BigDecimal("10.00"));
        });

        action.run(RuleContext.builder().with(var1 -> "a", var3 -> new BigDecimal("10.00")).build());
    }

    @Test
    public void test5() {
        Action action = action((Byte var1, Boolean var2, Character var3, Double var4, Float var5, Integer var6, Long var7, Short var8) -> {
            Assert.assertTrue(var1 == 2);
            Assert.assertEquals(var2, true);
            Assert.assertTrue(var3 == 'c');
            Assert.assertTrue(var4 == 100.100);
            Assert.assertTrue(var5 == 10.10f);
            Assert.assertTrue(var6 == 10);
            Assert.assertTrue(var7 == 10000788);
            Assert.assertTrue(var8 == 200);
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

    @Test(expected = UnrulyException.class)
    public void test7() {
        List<Action> actions = Action.builder().build(new ActionContainer());

        actions.get(3).run(arg1 -> 123, arg2 -> 12);
    }

    @Test
    public void test8() {
        List<Action> actions = Action.builder().build(new ActionContainer());
        // Test Order
        Assert.assertTrue(actions.get(0).getName().equals("action4"));
        Assert.assertTrue(actions.get(1).getName().equals("action3"));
        Assert.assertTrue(actions.get(2).getName().equals("action2"));
        Assert.assertTrue(actions.get(3).getName().equals("action1"));
    }

    @Test(expected = UnrulyException.class)
    public void test9() {
        List<Action> actions = Action.builder().build(new ActionContainer());
        actions.get(4).run();
    }

    @Test
    public void test10() {
        List<Action> actions = Action.builder().build(new ActionContainer());
        Bindings bindings = Bindings.builder().standard(var9 -> new BigDecimal("0.00"));
        actions.get(4).run(bindings);
        Assert.assertEquals(bindings.getValue("var9"), new BigDecimal("400.00"));
    }

    @Test(expected = UnrulyException.class)
    public void test11() {
        List<Action> actions = Action.builder().build(new ActionContainer());
        Bindings bindings = Bindings.builder().standard();
        bindings.bind(Binding.builder().constant("arg9", new BigDecimal("0.00")));
        actions.get(4).run(bindings);
    }

    @Test
    public void test12() {
        List<Action> actions = Action.builder().build(new ActionContainer());
        Bindings bindings = Bindings.builder().standard();
        bindings.bind(Binding.builder().finalBinding("arg9", new BigDecimal("0.00")));
        actions.get(4).run(bindings);
    }

    @Test(expected = UnrulyException.class)
    public void test13() {
        List<Action> actions = Action.builder().build(new ActionContainer());
        Bindings bindings = Bindings.builder().standard();
        bindings.bind(Binding.builder().finalConstant("arg9", new BigDecimal("0.00")));
        actions.get(4).run(bindings);
    }

    @Test
    public void test14() {
        List<Action> actions = Action.builder().build(new ActionContainer());
        Bindings bindings = Bindings.builder().standard();
        bindings.bind(Binding.builder().with ("arg9", BigDecimal.class, new BigDecimal("0.00")).build());
        actions.get(4).run(bindings);
        Assert.assertEquals(new BigDecimal("400.00"), bindings.getValue("arg9"));
    }

    @Test
    public void test15() {
        List<Action> actions = Action.builder().build(new ActionContainer());
        Bindings bindings = Bindings.builder().standard();
        Binding binding1 = bindings.bind(arg1 -> (byte) 111);
        Binding binding2 = bindings.bind(arg2 -> false);
        Binding binding3 = bindings.bind(arg3 -> 'c');
        Binding binding4 = bindings.bind(arg4 -> 1200.05);
        Binding binding5 = bindings.bind(arg5 -> 1000.05f);
        Binding binding6 = bindings.bind(arg6 -> 12000);
        Binding binding7 = bindings.bind(arg7 -> 120000000l);
        Binding binding8 = bindings.bind(arg8 -> (short) 1024);
        Binding binding9 = bindings.bind(arg9 -> new Person("Kobe", "Bryant"));

        actions.get(5).run(bindings);

        Assert.assertEquals((byte) binding1.getValue(), (byte) 100);
        Assert.assertEquals(binding2.getValue(), true);
        Assert.assertEquals((char) binding3.getValue(), 'z');
        Assert.assertTrue((double) binding4.getValue() == 1500.5555);
        Assert.assertTrue((float) binding5.getValue() == 10.11f);
        Assert.assertEquals((int) binding6.getValue(), 34895795);
        Assert.assertEquals((long) binding7.getValue(), 348934980395l);
        Assert.assertEquals((short) binding8.getValue(), (short) 2398);
        Assert.assertEquals(binding9.getValue(), new Person("Micahel", "Jordan"));
    }

    private static class ActionContainer {

        public ActionContainer() {
            super();
        }

        @org.rulii.annotation.Action
        @Order(4)
        public void action1(String arg1, String arg2) {
            Assert.assertNull(arg1);
            Assert.assertNull(arg2);
        }

        @org.rulii.annotation.Action
        @Order(3)
        public void action2(String arg1, String arg2, Integer arg3) {
            Assert.assertEquals("a", arg1);
            Assert.assertEquals("b", arg2);
        }

        @org.rulii.annotation.Action
        @Order(2)
        public void action3(int var1, Optional<String> var2, Optional<BigDecimal> var3) {
            Assert.assertEquals(var1, 0);
            Assert.assertEquals(var2, Optional.empty());
            Assert.assertEquals(var3.get(), new BigDecimal("10.00"));
        }

        @org.rulii.annotation.Action
        @Order(1)
        public void action4(byte var1, boolean var2, char var3, double var4, float var5, int var6, long var7, short var8) {
            Assert.assertTrue(var1 == 0);
            Assert.assertEquals(var2, false);
            Assert.assertTrue(var4 == 0);
            Assert.assertTrue(var5 == 0f);
            Assert.assertTrue(var6 == 0);
            Assert.assertTrue(var7 == 0l);
            Assert.assertTrue(var8 == 0);
        }

        @org.rulii.annotation.Action
        @Order(5)
        public void action5(Binding<BigDecimal> var9) {
            var9.setValue(new BigDecimal("400.00"));
        }

        @org.rulii.annotation.Action
        public void action6(Binding<Byte> arg1, Binding<Boolean> arg2, Binding<Character> arg3, Binding<Double> arg4,
                            Binding<Float> arg5, Binding<Integer> arg6, Binding<Long> arg7, Binding<Short> arg8, Binding<Person> arg9) {
            Assert.assertNotNull(arg1);
            Assert.assertNotNull(arg2);
            Assert.assertNotNull(arg3);
            Assert.assertNotNull(arg4);
            Assert.assertNotNull(arg5);
            Assert.assertNotNull(arg6);
            Assert.assertNotNull(arg7);
            Assert.assertNotNull(arg8);
            Assert.assertNotNull(arg9);

            Assert.assertEquals((byte) arg1.getValue(), (byte) 111);
            Assert.assertEquals(arg2.getValue(), false);
            Assert.assertEquals((char) arg3.getValue(), 'c');
            Assert.assertTrue(arg4.getValue() == 1200.05);
            Assert.assertTrue(arg5.getValue() == 1000.05f);
            Assert.assertEquals((int) arg6.getValue(), 12000);
            Assert.assertEquals((long) arg7.getValue(), 120000000l);
            Assert.assertEquals((short) arg8.getValue(), (short) 1024);
            Assert.assertEquals(arg9.getValue(), new Person("Kobe", "Bryant"));

            arg1.setValue((byte) 100);
            arg2.setValue(true);
            arg3.setValue('z');
            arg4.setValue(1500.5555);
            arg5.setValue(10.11f);
            arg6.setValue(34895795);
            arg7.setValue(348934980395l);
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
