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
package org.rulii.test.bind.load;

import org.rulii.bind.Bindings;
import org.rulii.bind.load.BindingLoader;
import org.rulii.bind.load.FieldBindingLoader;
import org.rulii.bind.load.MapBindingLoader;
import org.rulii.bind.load.PropertyBindingLoader;
import org.junit.Assert;
import org.junit.Test;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Tests for BindingLoader.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class BindingLoaderTest {

    public BindingLoaderTest() {
        super();
    }

    @Test
    public void fieldLoaderTest() {
        Athlete jordan = new Athlete("Michael", "Jordan", 23, new BigDecimal("100000000"));
        Bindings bindings = Bindings.builder().standard();
        bindings.load(new FieldBindingLoader<>(), jordan);
        Assert.assertTrue(bindings.getValue("firstName").equals("Michael"));
        Assert.assertTrue(bindings.getValue("lastName").equals("Jordan"));
        Assert.assertTrue(bindings.getValue("age").equals(23));
        Assert.assertTrue(bindings.getValue("salary").equals(new BigDecimal("100000000")));
        Assert.assertFalse(bindings.contains("serialVersionUID"));

        bindings = Bindings.builder().standard();
        bindings.loadFields(jordan);
        Assert.assertTrue(bindings.getValue("firstName").equals("Michael"));
        Assert.assertTrue(bindings.getValue("lastName").equals("Jordan"));
        Assert.assertTrue(bindings.getValue("age").equals(23));
        Assert.assertTrue(bindings.getValue("salary").equals(new BigDecimal("100000000")));
        Assert.assertFalse(bindings.contains("serialVersionUID"));
    }

    @Test
    public void propertyLoaderTest() {
        Athlete jordan = new Athlete("Michael", "Jordan", 23, new BigDecimal("100000000"));
        Bindings bindings = Bindings.builder().standard();
        bindings.load(new PropertyBindingLoader<>(), jordan);
        Assert.assertTrue(bindings.getValue("firstName").equals("Michael"));
        Assert.assertTrue(bindings.getValue("lastName").equals("Jordan"));
        Assert.assertTrue(bindings.getValue("age").equals(23));
        Assert.assertTrue(bindings.getValue("salary").equals(new BigDecimal("100000000")));
        Assert.assertFalse(bindings.contains("team"));

        bindings = Bindings.builder().standard();
        bindings.loadProperties(jordan);
        Assert.assertTrue(bindings.getValue("firstName").equals("Michael"));
        Assert.assertTrue(bindings.getValue("lastName").equals("Jordan"));
        Assert.assertTrue(bindings.getValue("age").equals(23));
        Assert.assertTrue(bindings.getValue("salary").equals(new BigDecimal("100000000")));
        Assert.assertFalse(bindings.contains("team"));
    }

    @Test
    public void mapLoaderTest() {
        Map<String, Object> map = new HashMap<>();

        map.put("firstName", "Michael");
        map.put("lastName", "Jordan");
        map.put("age", 23);
        map.put("salary", new BigDecimal("100000000"));

        Bindings bindings = Bindings.builder().standard();
        bindings.load(new MapBindingLoader(), map);

        Assert.assertTrue(bindings.getValue("firstName").equals("Michael"));
        Assert.assertTrue(bindings.getValue("lastName").equals("Jordan"));
        Assert.assertTrue(bindings.getValue("age").equals(23));
        Assert.assertTrue(bindings.getValue("salary").equals(new BigDecimal("100000000")));

        bindings = Bindings.builder().standard();
        bindings.loadMap(map);

        Assert.assertTrue(bindings.getValue("firstName").equals("Michael"));
        Assert.assertTrue(bindings.getValue("lastName").equals("Jordan"));
        Assert.assertTrue(bindings.getValue("age").equals(23));
        Assert.assertTrue(bindings.getValue("salary").equals(new BigDecimal("100000000")));
    }

    @Test
    public void fieldLoaderTest2() {
        Athlete jordan = new Athlete("Michael", "Jordan", 23, new BigDecimal("100000000"));
        Bindings bindings = Bindings.builder().standard();
        BindingLoader loader = BindingLoader.builder().fieldLoaderBuilder()
                .ignored("firstName")
                .build();

        bindings.load(loader, jordan);
        Assert.assertFalse(bindings.contains("firstName"));
        Assert.assertTrue(bindings.getValue("lastName").equals("Jordan"));
        Assert.assertTrue(bindings.getValue("age").equals(23));
        Assert.assertTrue(bindings.getValue("salary").equals(new BigDecimal("100000000")));

        bindings = Bindings.builder().standard();
        loader = BindingLoader.builder().fieldLoaderBuilder()
                .include("firstName")
                .build();
        bindings.load(loader, jordan);
        Assert.assertTrue(bindings.getValue("firstName").equals("Michael"));
    }

    @Test
    public void fieldLoaderTest3() {
        Athlete jordan = new Athlete("Michael", "Jordan", 23, new BigDecimal("100000000"));
        Bindings bindings = Bindings.builder().standard();
        BindingLoader loader = BindingLoader.builder().fieldLoaderBuilder()
                .include("firstName")
                .nameGenerator((Field f) -> f.getName() + "x")
                .build();
        bindings.load(loader, jordan);
        Assert.assertTrue(bindings.getValue("firstNamex").equals("Michael"));
        Assert.assertFalse(bindings.contains("lastName"));
        Assert.assertFalse(bindings.contains("lastNamex"));
    }

    @Test
    public void propertyLoaderTest2() {
        Athlete jordan = new Athlete("Michael", "Jordan", 23, new BigDecimal("100000000"));
        Bindings bindings = Bindings.builder().standard();
        BindingLoader loader = BindingLoader.builder().propertyLoaderBuilder()
                .ignored("firstName")
                .build();

        bindings.load(loader, jordan);
        Assert.assertFalse(bindings.contains("firstName"));
        Assert.assertTrue(bindings.getValue("lastName").equals("Jordan"));
        Assert.assertTrue(bindings.getValue("age").equals(23));
        Assert.assertTrue(bindings.getValue("salary").equals(new BigDecimal("100000000")));

        bindings = Bindings.builder().standard();
        loader = BindingLoader.builder().propertyLoaderBuilder()
                .include("firstName")
                .build();
        bindings.load(loader, jordan);
        Assert.assertTrue(bindings.getValue("firstName").equals("Michael"));
    }

    @Test
    public void propertyLoaderTest3() {
        Athlete jordan = new Athlete("Michael", "Jordan", 23, new BigDecimal("100000000"));
        Bindings bindings = Bindings.builder().standard();
        BindingLoader loader = BindingLoader.builder().propertyLoaderBuilder()
                .include("firstName")
                .nameGenerator((PropertyDescriptor f) -> f.getName() + "x")
                .build();
        bindings.load(loader, jordan);
        Assert.assertTrue(bindings.getValue("firstNamex").equals("Michael"));
        Assert.assertFalse(bindings.contains("lastName"));
        Assert.assertFalse(bindings.contains("lastNamex"));
    }

    @Test
    public void mapLoaderTest2() {
        Map<String, Object> jordan = new HashMap<>();
        jordan.put("firstName", "Michael");
        jordan.put("lastName", "Jordan");
        jordan.put("age", 23);
        jordan.put("salary", new BigDecimal("100000000"));

        Bindings bindings = Bindings.builder().standard();
        BindingLoader loader = BindingLoader.builder().mapLoaderBuilder()
                .ignored("firstName")
                .build();

        bindings.load(loader, jordan);
        Assert.assertFalse(bindings.contains("firstName"));
        Assert.assertTrue(bindings.getValue("lastName").equals("Jordan"));
        Assert.assertTrue(bindings.getValue("age").equals(23));
        Assert.assertTrue(bindings.getValue("salary").equals(new BigDecimal("100000000")));

        bindings = Bindings.builder().standard();
        loader = BindingLoader.builder().mapLoaderBuilder()
                .include("firstName")
                .build();
        bindings.load(loader, jordan);
        Assert.assertTrue(bindings.getValue("firstName").equals("Michael"));
    }

    @Test
    public void mapLoaderTest3() {
        Map<String, Object> jordan = new HashMap<>();
        jordan.put("firstName", "Michael");
        jordan.put("lastName", "Jordan");
        jordan.put("age", 23);
        jordan.put("salary", new BigDecimal("100000000"));

        Bindings bindings = Bindings.builder().standard();
        BindingLoader loader = BindingLoader.builder().mapLoaderBuilder()
                .include("firstName")
                .nameGenerator((String k) -> k + "x")
                .build();
        bindings.load(loader, jordan);
        Assert.assertTrue(bindings.getValue("firstNamex").equals("Michael"));
        Assert.assertFalse(bindings.contains("lastName"));
        Assert.assertFalse(bindings.contains("lastNamex"));
    }

    public static class Person {

        private static final long serialVersionUID = 0l;
        private String firstName;
        private String lastName;
        private int age;

        public Person(String firstName, String lastName, int age) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.age = age;
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

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }

    public static class Athlete extends Person {
        private BigDecimal salary;
        private String team;

        public Athlete(String firstName, String lastName, int age, BigDecimal salary) {
            super(firstName, lastName, age);
            this.salary = salary;
        }

        public BigDecimal getSalary() {
            return salary;
        }

        public void setSalary(BigDecimal salary) {
            this.salary = salary;
        }

        public void setTeam(String team) {
            this.team = team;
        }
    }
}
