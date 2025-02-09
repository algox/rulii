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
package org.rulii.test.rule;

import org.rulii.annotation.*;
import org.rulii.bind.Binding;
import org.rulii.bind.Bindings;
import org.rulii.bind.match.MatchByTypeMatchingStrategy;
import org.rulii.context.RuleContext;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test rules and their conditions.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public final class TestRules {

    private TestRules() {
        super();
    }

    @Rule
    public static class TestRule1 {

        public TestRule1() {
            super();
        }

        @PreCondition
        public boolean preCheck() {
            return true;
        }
    }

    @Rule
    public static class TestRule2 {

        public TestRule2() {
            super();
        }

        @PreCondition
        public boolean preCheck() {
            return false;
        }
    }


    @Rule
    public static class TestRule3 {

        public TestRule3() {
            super();
        }

        @PreCondition
        public boolean preCheck(String name, List<Integer> values) {
            assertNotNull(name);
            assertNotNull(values);
            assertFalse(values.isEmpty());
            return true;
        }

        @Given
        public boolean when(List<Integer> values) {
            assertFalse(values.isEmpty());
            return true;
        }
    }

    @Rule
    public static class TestRule4 {

        public TestRule4() {
            super();
        }

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        @PreCondition
        public boolean preCheck(Optional<String> name, Optional<List<Integer>> values) {
            assertTrue(name.isPresent());
            assertTrue(values.isPresent());
            assertFalse(values.get().isEmpty());
            return false;
        }
    }

    @Rule
    public static class TestRule5 {

        public TestRule5() {
            super();
        }

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        @PreCondition
        public boolean preCheck(Optional<String> name, Optional<List<Integer>> values, String someValue, int someNumber) {
            assertFalse(name.isPresent());
            assertFalse(values.isPresent());
            assertNull(someValue);
            assertEquals(0, someNumber);
            return true;
        }
    }

    @Rule
    public static class TestRule6 {

        public TestRule6() {
            super();
        }

        @PreCondition
        public boolean preCheck(@Param(defaultValue = "xxx") String someValue, @Param(name = "num", defaultValue = "123") int someNumber) {
            assertEquals("xxx", someValue);
            assertEquals(123, someNumber);
            return true;
        }
    }

    @Rule
    public static class TestRule7 {

        public TestRule7() {
            super();
        }

        @PreCondition
        public boolean preCheck(@Param(name = "text") String someValue, @Param("num") int someNumber) {
            assertEquals("xxx", someValue);
            assertEquals(123, someNumber);
            return true;
        }
    }

    @Rule
    public static class TestRule8 {

        public TestRule8() {
            super();
        }

        @PreCondition
        public boolean preCheck(@Param(matchUsing = MatchByTypeMatchingStrategy.class) String someValue) {
            assertEquals("xxx", someValue);
            return true;
        }
    }

    @Rule
    public static class TestRule9 {

        public TestRule9() {
            super();
        }

        @PreCondition
        public boolean preCheck(Binding<String> someValue) {
            assertNotNull(someValue);
            assertEquals("xxx", someValue.getValue());
            return true;
        }
    }

    @Rule
    public static class TestRule10 {

        public TestRule10() {
            super();
        }

        @PreCondition
        public boolean preCheck(Binding<String> someValue) {
            assertNotNull(someValue);
            assertEquals("xxx", someValue.getValue());
            someValue.setValue("yyy");
            return true;
        }
    }

    @Rule
    public static class TestRule11 {

        public TestRule11() {
            super();
        }

        @PreCondition
        public boolean preCheck(Binding<String> a, String b, Binding<Integer> c) {
            assertNull(a);
            assertNull(b);
            assertNull(c);
            return true;
        }
    }

    @Rule("Rule12")
    public static class TestRule12 {

        public TestRule12() {
            super();
        }

        @PreCondition
        public boolean isApplicable(boolean flag) {
            return flag;
        }
    }

    @Rule(name = "Rule13")
    public static class TestRule13 {

        public TestRule13() {
            super();
        }

        @PreCondition
        public boolean isApplicable(boolean flag) {
            throw new IllegalArgumentException("Invalid flag");
        }
    }

    @Rule()
    public static class TestRule14 {

        public TestRule14() {
            super();
        }

        @PreCondition
        public boolean isApplicable(boolean flag) throws CheckedException {
            throw new CheckedException("Checked Exception.");
        }
    }

    public static class CheckedException extends Exception {

        public CheckedException(String message) {
            super(message);
        }
    }

    @Rule()
    public static class TestRule15 {

        public TestRule15() {
            super();
        }

        @Given
        public boolean isValid() {
            return true;
        }
    }

    @Rule()
    public static class TestRule16 {

        public TestRule16() {
            super();
        }

        @PreCondition
        public boolean isApplicable(boolean flag) {
            return flag;
        }

        @Given
        public boolean isValid() {
            return true;
        }
    }

    @Rule()
    public static class TestRule17 {

        public TestRule17() {
            super();
        }

        @PreCondition
        public boolean isApplicable(boolean flag) {
            throw new IllegalArgumentException();
        }

        @Given
        public boolean when() {
            return true;
        }
    }

    @Rule()
    public static class TestRule18 {

        public TestRule18() {
            super();
        }

        @Given
        public boolean when(String someText, Map<String, List<Integer>> someMap) {
            assertNotNull(someText);
            assertNotNull(someMap);
            return true;
        }
    }

    @Rule()
    public static class TestRule19 {

        public TestRule19() {
            super();
        }

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        @Given
        public boolean when(Float someFloat, Optional<Map<String, List<Integer>>> someMap, Binding<Float> bindingFloat) {
            assertEquals(120.00f, someFloat);
            assertNotNull(someMap);
            assertNotNull(bindingFloat);
            assertTrue(someMap.isPresent());
            assertEquals(120.00f, bindingFloat.getValue());
            return true;
        }
    }

    @Rule()
    public static class TestRule20 {

        public TestRule20() {
            super();
        }

        @PreCondition
        private boolean preCondition(Bindings bindings, RuleContext ctx) {
            assertNotNull(bindings);
            assertNotNull(ctx);
            return true;
        }

        @Given
        public boolean when(RuleContext ruleContext, Bindings ruleBindings) {
            assertNotNull(ruleContext);
            assertNotNull(ruleBindings);
            return true;
        }
    }

    @Rule()
    public static class TestRule21 {

        public TestRule21() {
            super();
        }

        @PreCondition
        private boolean preCondition(Bindings bindings) {
            bindings.bind("a", Integer.class);
            return true;
        }
    }

    @Rule()
    public static class TestRule22 {

        public TestRule22() {
            super();
        }

        @PreCondition
        private boolean preCondition(RuleContext ctx) {
            ctx.getBindings().bind("a", Integer.class);
            return true;
        }
    }

    @Rule()
    public static class TestRule23 {

        public TestRule23() {
            super();
        }

        @Given
        private boolean when(Bindings bindings) {
            bindings.bind("a", Integer.class);
            return true;
        }
    }

    @Rule()
    public static class TestRule24 {

        public TestRule24() {
            super();
        }

        @Given
        private boolean when(RuleContext ctx) {
            ctx.getBindings().bind("a", Integer.class);
            return true;
        }
    }

    @Rule()
    public static class TestRule25 {

        public TestRule25() {
            super();
        }

        @PreCondition
        public boolean preCheck() {
            return true;
        }

        @Given
        private boolean when() {
            return true;
        }

        @Then
        public void action(Binding<Boolean> check) {
            assertNotNull(check);
            check.setValue(true);
        }
    }

    @Rule()
    public static class TestRule26 {

        public TestRule26() {
            super();
        }

        @Then
        public void action(Binding<Boolean> check) {
            assertNotNull(check);
            check.setValue(true);
        }
    }

    @Rule()
    public static class TestRule27 {

        public TestRule27() {
            super();
        }

        @Given
        public boolean when(boolean flag) {
            return flag;
        }

        @Then
        public void action(Bindings bindings) {
            assertNotNull(bindings);
            bindings.bind("someValue", 100);
        }
    }

    @Rule()
    public static class TestRule28 {

        public TestRule28() {
            super();
        }

        @Given
        public boolean when(boolean flag) {
            return flag;
        }

        @Then
        public void action(Bindings bindings) {
            assertNotNull(bindings);
            bindings.bind("someValue", 100);
        }
    }

    @Rule()
    public static class TestRule29 {

        public TestRule29() {
            super();
        }

        @Given
        public boolean when(boolean flag) {
            return flag;
        }

        @Then
        public void action1(Binding<Integer> someValue) {
            assertNotNull(someValue);
            someValue.setValue(someValue.getValue() + 100);
        }

        @Then
        public void action2(Binding<Integer> someValue) {
            assertNotNull(someValue);
            someValue.setValue(someValue.getValue() + 100);
        }
    }

    @Rule()
    public static class TestRule30 {

        public TestRule30() {
            super();
        }

        @Given
        public boolean when(boolean flag) {
            return flag;
        }

        @Then
        @Order(3)
        public void action3(Binding<String> someValue) {
            assertNotNull(someValue);
            someValue.setValue(someValue.getValue() + "c");
        }

        @Then
        @Order(2)
        public void action2(Binding<String> someValue) {
            assertNotNull(someValue);
            someValue.setValue(someValue.getValue() + "b");
        }

        @Then
        @Order(1)
        public void action1(Binding<String> someValue) {
            assertNotNull(someValue);
            someValue.setValue(someValue.getValue() + "a");
        }
    }

    @Rule()
    public static class TestRule31 {

        public TestRule31() {
            super();
        }

        @Given
        public boolean when(boolean flag) {
            return flag;
        }

        @Then
        @Order(2)
        public void action2(Binding<String> someValue) {
            assertNotNull(someValue);
            someValue.setValue(someValue.getValue() + "y");
        }

        @Then
        @Order(1)
        public void action1(Binding<String> someValue) {
            assertNotNull(someValue);
            someValue.setValue(someValue.getValue() + "x");
        }

        @Then
        @Order(3)
        public void action3(Binding<String> someValue) {
            assertNotNull(someValue);
            someValue.setValue(someValue.getValue() + "z");
        }
    }

    @Rule()
    public static class TestRule32 {

        public TestRule32() {
            super();
        }

        @Given
        public boolean when(boolean flag) {
            return flag;
        }

        @Then
        public void action(RuleContext ctx) {
            throw new IllegalArgumentException("Test");
        }
    }

    @Rule()
    public static class TestRule33 {

        public TestRule33() {
            super();
        }

        @Given
        public boolean when(boolean flag) {
            return flag;
        }

        @Then
        @Order(1)
        public void action1(Binding<String> someValue) {
            assertNotNull(someValue);
            someValue.setValue(someValue.getValue() + "zzz");
        }

        @Then
        public void action(RuleContext ctx) throws CheckedException {
            throw new CheckedException("Testing");
        }
    }

    @Rule()
    public static class TestRule34 {

        public TestRule34() {
            super();
        }

        @Given
        public boolean when(boolean flag) {
            return flag;
        }

        @Then
        @Order(1)
        public void action1(Binding<String> someValue) {
            assertNotNull(someValue);
            someValue.setValue(someValue.getValue() + "zzz");
        }

        @Then
        public void action2(Binding<String> someValue) {
            assertNotNull(someValue);
            someValue.setValue(someValue.getValue() + "123");
        }
    }

    @Rule()
    public static class TestRule35 {

        public TestRule35() {
            super();
        }

        @Given
        public boolean when(boolean flag) {
            return flag;
        }

        @Then
        public void then(Binding<String> someValue) {
            assertNotNull(someValue);
            someValue.setValue(someValue.getValue() + "zzz");
        }

        @Otherwise
        public void otherwise(Binding<String> someValue) {
            assertNotNull(someValue);
            someValue.setValue(someValue.getValue() + "yyy");
        }
    }

    @Rule()
    public static class TestRule36 {

        public TestRule36() {
            super();
        }

        @Given
        public boolean when(boolean flag) {
            return flag;
        }

        @Then
        public void then(Binding<String> someValue) {
            assertNotNull(someValue);
            someValue.setValue(someValue.getValue() + "zzz");
        }

        @Otherwise
        public void otherwise1(Binding<String> someValue) {
            assertNotNull(someValue);
            someValue.setValue(someValue.getValue() + "111");
        }

        @Otherwise
        public void otherwise2(Binding<String> someValue) {
            assertNotNull(someValue);
            someValue.setValue(someValue.getValue() + "222");
        }
    }

    @Rule()
    public static class TestRule37 {

        public TestRule37() {
            super();
        }

        @Given
        public boolean when(boolean flag) {
            return flag;
        }

        @Then
        public void then(Bindings bindings) {
            assertNotNull(bindings);
            bindings.bind("newBinding", 321);
        }

        @Otherwise
        public void otherwise(RuleContext ctx, Bindings bindings) {
            assertNotNull(ctx);
            assertNotNull(bindings);
            bindings.bind("newBinding", 123);
        }
    }
}
