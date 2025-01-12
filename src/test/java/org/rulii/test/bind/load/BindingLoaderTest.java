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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.bind.Bindings;
import org.rulii.bind.load.*;

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
        Assertions.assertEquals("Michael", bindings.getValue("firstName"));
        Assertions.assertEquals("Jordan", bindings.getValue("lastName"));
        Assertions.assertEquals(23, (int) bindings.getValue("age"));
        Assertions.assertEquals(bindings.getValue("salary"), new BigDecimal("100000000"));
        Assertions.assertFalse(bindings.contains("serialVersionUID"));

        bindings = Bindings.builder().standard();
        bindings.loadFields(jordan);
        Assertions.assertEquals("Michael", bindings.getValue("firstName"));
        Assertions.assertEquals("Jordan", bindings.getValue("lastName"));
        Assertions.assertEquals(23, (int) bindings.getValue("age"));
        Assertions.assertEquals(bindings.getValue("salary"), new BigDecimal("100000000"));
        Assertions.assertFalse(bindings.contains("serialVersionUID"));
    }

    @Test
    public void propertyLoaderTest() {
        Athlete jordan = new Athlete("Michael", "Jordan", 23, new BigDecimal("100000000"));
        Bindings bindings = Bindings.builder().standard();
        bindings.load(new PropertyBindingLoader<>(), jordan);
        Assertions.assertEquals("Michael", bindings.getValue("firstName"));
        Assertions.assertEquals("Jordan", bindings.getValue("lastName"));
        Assertions.assertEquals(23, (int) bindings.getValue("age"));
        Assertions.assertEquals(bindings.getValue("salary"), new BigDecimal("100000000"));
        Assertions.assertNull(bindings.getValue("team"));

        bindings = Bindings.builder().standard();
        bindings.loadProperties(jordan);
        Assertions.assertEquals("Michael", bindings.getValue("firstName"));
        Assertions.assertEquals("Jordan", bindings.getValue("lastName"));
        Assertions.assertEquals(23, (int) bindings.getValue("age"));
        Assertions.assertEquals(bindings.getValue("salary"), new BigDecimal("100000000"));
        Assertions.assertNull(bindings.getValue("team"));
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

        Assertions.assertEquals("Michael", bindings.getValue("firstName"));
        Assertions.assertEquals("Jordan", bindings.getValue("lastName"));
        Assertions.assertEquals(23, (int) bindings.getValue("age"));
        Assertions.assertEquals(bindings.getValue("salary"), new BigDecimal("100000000"));

        bindings = Bindings.builder().standard();
        bindings.loadMap(map);

        Assertions.assertEquals("Michael", bindings.getValue("firstName"));
        Assertions.assertEquals("Jordan", bindings.getValue("lastName"));
        Assertions.assertEquals(23, (int) bindings.getValue("age"));
        Assertions.assertEquals(bindings.getValue("salary"), new BigDecimal("100000000"));
    }

    @Test
    public void fieldLoaderTest2() {
        Athlete jordan = new Athlete("Michael", "Jordan", 23, new BigDecimal("100000000"));
        Bindings bindings = Bindings.builder().standard();
        BindingLoaderBuilder<Field, Athlete> builder = BindingLoader.builder().fieldLoaderBuilder();
        BindingLoader<Athlete> loader = builder
                .ignored("firstName")
                .build();

        bindings.load(loader, jordan);
        Assertions.assertFalse(bindings.contains("firstName"));
        Assertions.assertEquals("Jordan", bindings.getValue("lastName"));
        Assertions.assertEquals(23, (int) bindings.getValue("age"));
        Assertions.assertEquals(bindings.getValue("salary"), new BigDecimal("100000000"));

        bindings = Bindings.builder().standard();
        builder = BindingLoader.builder().fieldLoaderBuilder();
        loader = builder
                .include("firstName")
                .build();
        bindings.load(loader, jordan);
        Assertions.assertEquals("Michael", bindings.getValue("firstName"));
    }

    @Test
    public void fieldLoaderTest3() {
        Athlete jordan = new Athlete("Michael", "Jordan", 23, new BigDecimal("100000000"));
        Bindings bindings = Bindings.builder().standard();
        BindingLoaderBuilder<Field, Athlete> builder = BindingLoader.builder().fieldLoaderBuilder();
        BindingLoader<Athlete> loader = builder
                .include("firstName")
                .nameGenerator((Field f) -> f.getName() + "x")
                .build();
        bindings.load(loader, jordan);
        Assertions.assertEquals("Michael", bindings.getValue("firstNamex"));
        Assertions.assertFalse(bindings.contains("lastName"));
        Assertions.assertFalse(bindings.contains("lastNamex"));
    }

    @Test
    public void propertyLoaderTest2() {
        Athlete jordan = new Athlete("Michael", "Jordan", 23, new BigDecimal("100000000"));
        Bindings bindings = Bindings.builder().standard();
        BindingLoaderBuilder<PropertyDescriptor, Athlete> builder = BindingLoader.builder().propertyLoaderBuilder();
        BindingLoader<Athlete> loader = builder
                .ignored("firstName")
                .build();

        bindings.load(loader, jordan);
        Assertions.assertFalse(bindings.contains("firstName"));
        Assertions.assertEquals("Jordan", bindings.getValue("lastName"));
        Assertions.assertEquals(23, (int) bindings.getValue("age"));
        Assertions.assertEquals(bindings.getValue("salary"), new BigDecimal("100000000"));

        bindings = Bindings.builder().standard();
        builder = BindingLoader.builder().propertyLoaderBuilder();
        loader = builder
                .include("firstName")
                .build();
        bindings.load(loader, jordan);
        Assertions.assertEquals("Michael", bindings.getValue("firstName"));
    }

    @Test
    public void propertyLoaderTest3() {
        Athlete jordan = new Athlete("Michael", "Jordan", 23, new BigDecimal("100000000"));
        Bindings bindings = Bindings.builder().standard();
        BindingLoaderBuilder<PropertyDescriptor, Athlete> builder = BindingLoader.builder().propertyLoaderBuilder();
        BindingLoader<Athlete> loader = builder
                .include("firstName")
                .nameGenerator((PropertyDescriptor f) -> f.getName() + "x")
                .build();
        bindings.load(loader, jordan);
        Assertions.assertEquals("Michael", bindings.getValue("firstNamex"));
        Assertions.assertFalse(bindings.contains("lastName"));
        Assertions.assertFalse(bindings.contains("lastNamex"));
    }

    @Test
    public void mapLoaderTest2() {
        Map<String, Object> jordan = new HashMap<>();
        jordan.put("firstName", "Michael");
        jordan.put("lastName", "Jordan");
        jordan.put("age", 23);
        jordan.put("salary", new BigDecimal("100000000"));

        Bindings bindings = Bindings.builder().standard();
        BindingLoaderBuilder<String, Map<String, Object>> builder = BindingLoader.builder().mapLoaderBuilder();
        BindingLoader<Map<String, Object>> loader = builder
                .ignored("firstName")
                .build();

        bindings.load(loader, jordan);
        Assertions.assertFalse(bindings.contains("firstName"));
        Assertions.assertEquals("Jordan", bindings.getValue("lastName"));
        Assertions.assertEquals(23, (int) bindings.getValue("age"));
        Assertions.assertEquals(bindings.getValue("salary"), new BigDecimal("100000000"));

        bindings = Bindings.builder().standard();
        loader = BindingLoader.builder().mapLoaderBuilder()
                .include("firstName")
                .build();
        bindings.load(loader, jordan);
        Assertions.assertEquals("Michael", bindings.getValue("firstName"));
    }

    @Test
    public void mapLoaderTest3() {
        Map<String, Object> jordan = new HashMap<>();
        jordan.put("firstName", "Michael");
        jordan.put("lastName", "Jordan");
        jordan.put("age", 23);
        jordan.put("salary", new BigDecimal("100000000"));

        Bindings bindings = Bindings.builder().standard();
        BindingLoaderBuilder<String, Map<String, Object>> builder = BindingLoader.builder().mapLoaderBuilder();
        BindingLoader<Map<String, Object>> loader = builder
                .include("firstName")
                .nameGenerator((String k) -> k + "x")
                .build();
        bindings.load(loader, jordan);
        Assertions.assertEquals("Michael", bindings.getValue("firstNamex"));
        Assertions.assertFalse(bindings.contains("lastName"));
        Assertions.assertFalse(bindings.contains("lastNamex"));
    }

    public static class Person {

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

        public String getTeam() {
            return team;
        }
    }
}
