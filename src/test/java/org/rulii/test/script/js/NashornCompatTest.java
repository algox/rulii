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
package org.rulii.test.script.js;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.bind.Bindings;
import org.rulii.context.RuleContext;
import org.rulii.model.UnrulyException;
import org.rulii.model.condition.Condition;
import org.rulii.model.function.Function;
import org.rulii.script.Script;
import org.rulii.script.graaljs.GraalJsScriptProcessorFactory;

/**
 * Tests for the Nashorn compatibility mode enabled on the shared GraalJS engine
 * ({@code js.nashorn-compat} + {@code allowExperimentalOptions}).
 *
 * <p>Covers bean-style property access on host objects, confirms the explicit ES2022
 * setting still wins over the ES5 default that compatibility mode would imply, and pins
 * down the behaviour of the {@code exit()}/{@code quit()} globals the mode introduces.
 */
public class NashornCompatTest {

    public static class Person {
        private final String name;
        private final int age;
        private final Long id;
        private boolean active;

        public Person(String name, int age, Long id, boolean active) {
            super();
            this.name = name;
            this.age = age;
            this.id = id;
            this.active = active;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }

        public Long getId() {
            return id;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }
    }

    private RuleContext contextWith(Bindings bindings) {
        return RuleContext.builder()
                .with(bindings)
                .build();
    }

    private RuleContext personContext(Person person) {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("person", Person.class, person);
        return contextWith(bindings);
    }

    private Script<Boolean> condition(String text) {
        return Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME, text);
    }

    // -----------------------------------------------------------------------
    // Bean-style property access
    // -----------------------------------------------------------------------

    @Test
    public void testBeanGetterAccessibleAsProperty() {
        RuleContext ctx = personContext(new Person("Max", 42, 7L, true));
        Condition condition = Condition.builder().build(condition("ctx.person.name === 'Max'"));
        Assertions.assertTrue(condition.isTrue(ctx));
    }

    @Test
    public void testBeanGetterExplicitCallStillWorks() {
        RuleContext ctx = personContext(new Person("Max", 42, 7L, true));
        Condition condition = Condition.builder().build(condition("ctx.person.getName() === 'Max'"));
        Assertions.assertTrue(condition.isTrue(ctx));
    }

    @Test
    public void testBeanPrimitiveGetterParticipatesInArithmetic() {
        RuleContext ctx = personContext(new Person("Max", 42, 7L, true));
        Function<Integer> function = Function.builder().build(
                Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME, "ctx.person.age + 1"));
        Assertions.assertEquals(43, ((Number) function.apply(ctx)).intValue());
    }

    @Test
    public void testBeanBooleanIsGetterAccessibleAsProperty() {
        RuleContext ctx = personContext(new Person("Max", 42, 7L, true));
        Condition condition = Condition.builder().build(condition("ctx.person.active === true"));
        Assertions.assertTrue(condition.isTrue(ctx));
    }

    @Test
    public void testBeanBoxedLongGetterIsANumber() {
        RuleContext ctx = personContext(new Person("Max", 42, 7L, true));
        Condition condition = Condition.builder().build(
                condition("typeof ctx.person.id === 'number' && ctx.person.id === 7"));
        Assertions.assertTrue(condition.isTrue(ctx));
    }

    @Test
    public void testBeanSetterInvokedOnAssignment() {
        Person person = new Person("Max", 42, 7L, true);
        RuleContext ctx = personContext(person);
        Condition condition = Condition.builder().build(condition("ctx.person.active = false; true"));
        Assertions.assertTrue(condition.isTrue(ctx));
        Assertions.assertFalse(person.isActive());
    }

    @Test
    public void testBindingKeysTakePrecedenceOverBeanProperties() {
        // DefaultBindings is itself a Map and exposes isEmpty()/size(); a binding with one of
        // those names must still resolve to the binding value, not the bean property.
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("empty", String.class, "binding");
        bindings.bind("size", String.class, "binding");
        RuleContext ctx = contextWith(bindings);
        Condition condition = Condition.builder().build(
                condition("ctx.empty === 'binding' && ctx.size === 'binding'"));
        Assertions.assertTrue(condition.isTrue(ctx));
    }

    // -----------------------------------------------------------------------
    // ES2022 still in force (nashorn-compat alone would imply ES5)
    // -----------------------------------------------------------------------

    @Test
    public void testOptionalChainingAndNullishCoalescingStillAvailable() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("age", Integer.class, 42);
        RuleContext ctx = contextWith(bindings);
        Condition condition = Condition.builder().build(
                condition("(ctx.age?.toFixed(1) ?? 'none') === '42.0' && (ctx.missing?.x ?? 'none') === 'none'"));
        Assertions.assertTrue(condition.isTrue(ctx));
    }

    @Test
    public void testArrayAtAndPrivateClassFieldsStillAvailable() {
        RuleContext ctx = contextWith(Bindings.builder().standard());
        Condition condition = Condition.builder().build(
                condition("class A { #x = 1; get x() { return this.#x; } }; [1, 2, 3].at(-1) === 3 && new A().x === 1"));
        Assertions.assertTrue(condition.isTrue(ctx));
    }

    // -----------------------------------------------------------------------
    // exit()/quit() globals introduced by the mode
    // -----------------------------------------------------------------------

    @Test
    public void testQuitTerminatesScriptNotJvm() {
        RuleContext ctx = contextWith(Bindings.builder().standard());
        Condition condition = Condition.builder().build(condition("quit(1); true"));
        Assertions.assertThrows(UnrulyException.class, () -> condition.isTrue(ctx));
        // Reaching this line proves the JVM was not terminated.
        Assertions.assertTrue(Condition.builder().build(condition("true")).isTrue(ctx));
    }

    @Test
    public void testExitTerminatesScriptNotJvm() {
        RuleContext ctx = contextWith(Bindings.builder().standard());
        Condition condition = Condition.builder().build(condition("exit(2); true"));
        Assertions.assertThrows(UnrulyException.class, () -> condition.isTrue(ctx));
        Assertions.assertTrue(Condition.builder().build(condition("true")).isTrue(ctx));
    }
}
