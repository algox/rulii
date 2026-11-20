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
package org.rulii.test.validation;

import org.junit.jupiter.api.Test;
import org.rulii.bind.Binding;
import org.rulii.bind.Bindings;
import org.rulii.bind.NoSuchBindingException;
import org.rulii.context.RuleContext;
import org.rulii.model.Definable;
import org.rulii.model.Immutator;
import org.rulii.model.MethodDefinition;
import org.rulii.model.ParameterDefinitionEditor;
import org.rulii.model.UnrulyException;
import org.rulii.model.function.Function;
import org.rulii.rule.Rule;
import org.rulii.rule.RuleResult;
import org.rulii.validation.BindingFunction;
import org.rulii.validation.BindingLookupFunction;
import org.rulii.validation.ConstantFunction;
import org.rulii.validation.RuleViolation;
import org.rulii.validation.RuleViolations;
import org.rulii.validation.Severity;
import org.rulii.validation.ValueValidationRule;
import org.rulii.validation.rules.Validators;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.rulii.model.function.Functions.function;
import static org.rulii.validation.rules.Validators.*;

/**
 * Tests for the {@link Validators} factory class, specifically covering the three
 * value-source strategies: {@code binding(name)}, {@code value(constant)}, and
 * custom {@code Functions.function(lambda)}.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class ValidatorsTests {

    public ValidatorsTests() {
        super();
    }

    // -----------------------------------------------------------------------
    // binding(String name) factory
    // -----------------------------------------------------------------------

    @Test
    public void bindingReturnsBingFunction() {
        Function<?> fn = binding("age");
        assertInstanceOf(BindingFunction.class, fn);
        assertEquals("age", ((BindingFunction<?>) fn).getBindingName());
    }

    @Test
    public void bindingIsALookupFunction() {
        // binding(name) is the dedicated lookup; a BindingFunction of one's own is not.
        assertInstanceOf(BindingLookupFunction.class, binding("age"));
        Function<?> own = new BindingFunction<>("age", function((RuleContext ruleContext) -> 21));
        assertFalse(own instanceof BindingLookupFunction);
    }

    @Test
    public void bindingLookupDescribesItself() {
        Function<?> target = ((BindingFunction<?>) binding("age")).getTargetFunction();

        assertInstanceOf(Definable.class, target);
        assertEquals("binding", target.getName());
        assertEquals("binding(age)", target.getDescription());

        MethodDefinition definition = target.getDefinition();
        assertNotNull(definition);
        assertEquals("binding", definition.getName());
        assertEquals("binding(age)", definition.getDescription());
        assertEquals(Object.class, definition.getReturnType());
        assertEquals(1, definition.getParameterDefinitions().size());
        assertEquals("ruleContext", definition.getParameterDefinition(0).getName());
        assertEquals(RuleContext.class, definition.getParameterDefinition(0).getType());
        // Declared here, and every lookup has a definition of its own.
        assertEquals(ValidatorsTests.class.getName(), definition.getSource().getClassName());
        assertNotSame(definition, ((BindingFunction<?>) binding("age")).getTargetFunction().getDefinition());
        assertSame(definition, target.getExpression().method());
    }

    @Test
    public void bindingLookupIsTheValueFunctionOfTheRule() {
        // The builder keeps the target of a BindingFunction: for binding(name) that is the lookup itself.
        Rule rule = min(binding("age"), 18L).build();
        ValueValidationRule target = rule.getTarget();
        assertEquals("binding", target.getValueFunction().getName());
        assertEquals("age", target.getValueName());
        assertTrue(rule.run(age -> 21).status().isPass());
    }

    @Test
    public void bindingLookupNeedsAContext() {
        assertThrows(IllegalArgumentException.class, () -> binding("age").run((RuleContext) null));
    }

    @Test
    public void bindingAndValueFunctionsCanBeWrittenAndReadBack() throws Exception {
        RuleContext ruleContext = RuleContext.builder().build(Bindings.builder().standard(age -> 21));

        Function<?> lookup = writeAndRead(binding("age"));
        assertInstanceOf(BindingLookupFunction.class, lookup);
        assertEquals("age", ((BindingFunction<?>) lookup).getBindingName());
        assertEquals(Integer.valueOf(21), lookup.run(ruleContext));
        Function<?> target = ((BindingFunction<?>) lookup).getTargetFunction();
        assertEquals("binding(age)", target.getDescription());
        assertNotNull(target.getDefinition());

        Function<?> constant = writeAndRead(value("hello"));
        assertInstanceOf(ConstantFunction.class, constant);
        assertEquals("hello", constant.run(ruleContext));
        assertEquals("value", constant.getName());
        assertNotNull(constant.getDefinition());
    }

    @Test
    public void bindingAndValueFunctionsPrintTheirDescription() {
        assertEquals("binding(age)", ((BindingFunction<?>) binding("age")).getTargetFunction().toString());
        assertEquals("constant value", value("s3cret").toString());
    }

    /** A value of one's own that can be written through: every read-only version is one step further away. */
    private static final class Depth implements Immutator<Depth> {

        private final int level;

        private Depth(int level) {
            super();
            this.level = level;
        }

        @Override
        public Depth asImmutable() {
            return new Depth(level + 1);
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Depth && ((Depth) o).level == level;
        }

        @Override
        public int hashCode() {
            return level;
        }

        @Override
        public String toString() {
            return "Depth(" + level + ")";
        }
    }

    @Test
    public void bindingLookupMakesAValueReadOnlyExactlyOnce() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("x", Depth.class, new Depth(0));
        RuleContext ruleContext = RuleContext.builder().build(bindings);

        // Once, whether the context it is handed is the live one or a read-only one.
        assertEquals(1, ((Depth) binding("x").run(ruleContext)).level);
        assertEquals(1, ((Depth) binding("x").run(ruleContext.asImmutable())).level);

        // So the pre-condition, the condition and the otherwise action of a rule all see the same value.
        assertTrue(in(binding("x"), List.of(new Depth(1))).build().run(ruleContext).status().isPass());
        RuleViolations errors = new RuleViolations();
        bindings.bind("ruleViolations", RuleViolations.class, errors);
        assertTrue(in(binding("x"), List.of(new Depth(2))).build().run(ruleContext).status().isFail());
        assertEquals("Depth(1)", String.valueOf(errors.getViolations().get(0).getParameters().get("x")));
    }

    @Test
    public void bindingLookupReportsAFailingReadTheWayAFunctionDoes() {
        AtomicBoolean failing = new AtomicBoolean();
        Supplier<String> supplier = () -> {
            if (failing.get()) throw new IllegalStateException("no value today");
            return "fine";
        };
        Bindings bindings = Bindings.builder().standard();
        bindings.bind(Binding.builder().with("x").type(String.class).value(supplier).build());
        RuleContext ruleContext = RuleContext.builder().build(bindings);
        assertEquals("fine", binding("x").run(ruleContext));

        failing.set(true);
        UnrulyException e = assertThrows(UnrulyException.class, () -> binding("x").run(ruleContext));
        assertInstanceOf(IllegalStateException.class, e.getCause());
        assertTrue(e.getMessage().startsWith("Error trying to execute ["));
        // A binding that is not there is still reported as that.
        assertThrows(NoSuchBindingException.class, () -> binding("nope").run(ruleContext));
    }

    @Test
    public void valueFunctionsHaveParameterDefinitionsOfTheirOwn() {
        // Editing the definition of one function leaves every other alone.
        MethodDefinition first = ((BindingFunction<?>) binding("a")).getTargetFunction().getDefinition();
        MethodDefinition second = ((BindingFunction<?>) binding("b")).getTargetFunction().getDefinition();
        assertNotSame(first.getParameterDefinition(0), second.getParameterDefinition(0));
        ParameterDefinitionEditor.with(first.getParameterDefinition(0), first).description("edited");
        assertNull(second.getParameterDefinition(0).getDescription());
        assertNull(((BindingFunction<?>) binding("c")).getTargetFunction().getDefinition().getParameterDefinition(0).getDescription());

        assertNotSame(value(1).getDefinition().getParameterDefinition(0), value(2).getDefinition().getParameterDefinition(0));
    }

    @Test
    public void bindingLookupReadsTheContextItIsRunOnWhenBuiltOverAnother() {
        // A context built over the bindings of another carries that context's reserved pair in its global scope;
        // the pair is hidden there, so the lookup reads the stack of the context it is run on.
        RuleContext inner = RuleContext.builder().build(Bindings.builder().standard(x -> "inner-x"));
        RuleContext outer = RuleContext.builder().build(inner.getBindings());
        outer.getBindings().addScope("outer-scope");
        outer.getBindings().bind("x", String.class, "outer-x");
        outer.getBindings().bind("y", String.class, "outer-y");

        assertEquals("outer-x", binding("x").run(outer));
        assertEquals("outer-y", binding("y").run(outer));
        // A rule's own methods are handed the same context.
        assertTrue(Validators.assertEquals(binding("x"), "outer-x").build().run(outer).status().isPass());
        // The first context is untouched.
        assertEquals("inner-x", binding("x").run(inner));
        assertThrows(NoSuchBindingException.class, () -> binding("y").run(inner));
    }

    @Test
    public void bindingLookupDoesNotSeeTheLocalBindingsOfARun() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("x", Depth.class, new Depth(0));
        RuleContext ruleContext = RuleContext.builder().build(bindings);

        assertThrows(NoSuchBindingException.class, () -> binding("localBindings").run(ruleContext.withLocalBindings()));
        // Read-only once, also through the per-run view of a read-only context.
        assertEquals(1, ((Depth) binding("x").run(ruleContext.withLocalBindings())).level);
        assertEquals(1, ((Depth) binding("x").run(ruleContext.asImmutable().withLocalBindings())).level);
        assertEquals(1, ((Depth) binding("x").run(ruleContext.withLocalBindings().asImmutable())).level);
    }

    private static Function<?> writeAndRead(Function<?> function) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(function);
        }
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            return (Function<?>) in.readObject();
        }
    }

    @Test
    public void bindingFunctionReadsTheBinding() {
        Function<?> fn = binding("age");
        RuleContext ruleContext = RuleContext.builder().build(Bindings.builder().standard(age -> 21));

        Object value = fn.run(ruleContext);
        assertEquals(Integer.valueOf(21), value);
        // The same on the read-only context a condition runs with.
        Object readOnlyValue = fn.run(ruleContext.asImmutable());
        assertEquals(Integer.valueOf(21), readOnlyValue);
        // No such binding.
        assertThrows(NoSuchBindingException.class, () -> binding("nope").run(ruleContext));
    }

    @Test
    public void bindingFunctionHandsOutBindingsReadOnly() {
        // A function only ever sees a read-only context: a value that could be written through comes back read-only.
        Bindings inner = Bindings.builder().standard(x -> 1);
        Bindings outer = Bindings.builder().standard();
        outer.bind("nested", Bindings.class, inner);
        RuleContext ruleContext = RuleContext.builder().build(outer);

        Object value = binding("nested").run(ruleContext);
        assertInstanceOf(Bindings.class, value);
        assertThrows(RuntimeException.class, () -> ((Bindings) value).bind("y", 2));
        assertFalse(inner.contains("y"));
    }

    @Test
    public void bindingFunctionOfOnesOwnIsStillRun() {
        AtomicInteger calls = new AtomicInteger();
        Function<Integer> counting = function((RuleContext ruleContext) -> {
            calls.incrementAndGet();
            return 10;
        });

        Rule rule = min(new BindingFunction<>("amount", counting), 5L).build();
        assertTrue(rule.run().status().isPass());
        // Once for the type check, once for the condition.
        assertEquals(2, calls.get());

        // And a violation is still keyed by the binding name.
        RuleViolations errors = new RuleViolations();
        Rule failing = min(new BindingFunction<>("amount", counting), 50L).build();
        assertTrue(failing.run(ruleViolations -> errors).status().isFail());
        assertTrue(errors.getViolations().get(0).getParameters().containsKey("amount"));
    }

    @Test
    public void bindingNamePropagatedToViolationParam() {
        Rule rule = notNull(binding("username")).build();
        RuleViolations errors = new RuleViolations();

        // null value → violation; the param key must equal the binding name
        RuleResult result = rule.run(ruleViolations -> errors, username -> (Object) null);
        assertTrue(result.status().isFail());
        assertEquals(1, errors.getViolations().size());
        RuleViolation v = errors.getViolations().get(0);
        assertTrue(v.getParameters().containsKey("username"),
                "Violation param should be keyed by binding name 'username'");
    }

    @Test
    public void bindingMissingFromContextIsSkipped() {
        // No binding provided → NoSuchBindingException propagates → UnrulyException
        Rule rule = notNull(binding("missingField")).build();
        assertThrows(UnrulyException.class, () -> rule.run());
    }

    @Test
    public void bindingResolvedAtRuntime() {
        Rule rule = notNull(binding("email")).build();

        // First run: value present and non-null → PASS
        RuleResult pass = rule.run(email -> "user@example.com");
        assertTrue(pass.status().isPass());

        // Second run: value present but null → FAIL
        RuleViolations errors = new RuleViolations();
        RuleResult fail = rule.run(ruleViolations -> errors, email -> (Object) null);
        assertTrue(fail.status().isFail());
    }

    @Test
    public void bindingWithMinRule() {
        Rule rule = min(binding("age"), 18L).build();

        RuleResult pass = rule.run(age -> 21);
        assertTrue(pass.status().isPass());

        RuleViolations errors = new RuleViolations();
        RuleResult fail = rule.run(ruleViolations -> errors, age -> 16);
        assertTrue(fail.status().isFail());
        assertTrue(errors.hasErrors());
        RuleViolation v = errors.getViolations().get(0);
        assertTrue(v.getParameters().containsKey("age"),
                "Violation param should be keyed by binding name 'age'");
    }

    @Test
    public void bindingWithMaxRule() {
        Rule rule = max(binding("score"), 100L).build();

        assertTrue(rule.run(score -> 99).status().isPass());
        assertTrue(rule.run(score -> 100).status().isPass());

        RuleViolations errors = new RuleViolations();
        assertTrue(rule.run(ruleViolations -> errors, score -> 101).status().isFail());
        assertFalse(errors.getViolations().isEmpty());
    }

    @Test
    public void bindingWithNotBlankRule() {
        Rule rule = notBlank(binding("name")).build();

        assertTrue(rule.run(name -> "Alice").status().isPass());

        RuleViolations errors = new RuleViolations();
        assertTrue(rule.run(ruleViolations -> errors, name -> "  ").status().isFail());
        assertTrue(errors.hasErrors());
    }

    @Test
    public void bindingWithAlphaRule() {
        Rule rule = alpha(binding("code")).build();

        assertTrue(rule.run(code -> "ABC").status().isPass());

        RuleViolations errors = new RuleViolations();
        assertTrue(rule.run(ruleViolations -> errors, code -> "AB1").status().isFail());
        RuleViolation v = errors.getViolations().get(0);
        assertTrue(v.getParameters().containsKey("code"));
    }

    @Test
    public void bindingWithPositiveRule() {
        Rule rule = positive(binding("amount")).build();

        assertTrue(rule.run(amount -> 5).status().isPass());

        RuleViolations errors = new RuleViolations();
        assertTrue(rule.run(ruleViolations -> errors, amount -> -1).status().isFail());
    }

    @Test
    public void bindingWithNegativeRule() {
        Rule rule = negative(binding("delta")).build();

        assertTrue(rule.run(delta -> -3).status().isPass());

        RuleViolations errors = new RuleViolations();
        assertTrue(rule.run(ruleViolations -> errors, delta -> 0).status().isFail());
    }

    @Test
    public void bindingWithSizeRule() {
        Rule rule = size(binding("tags"), 1, 3).build();

        assertTrue(rule.run(tags -> List.of("a", "b")).status().isPass());

        RuleViolations errors = new RuleViolations();
        assertTrue(rule.run(ruleViolations -> errors, tags -> List.of("a", "b", "c", "d")).status().isFail());
    }

    @Test
    public void bindingWithInRule() {
        Rule rule = in(binding("status"), Arrays.asList("ACTIVE", "INACTIVE")).build();

        assertTrue(rule.run(status -> "ACTIVE").status().isPass());

        RuleViolations errors = new RuleViolations();
        assertTrue(rule.run(ruleViolations -> errors, status -> "PENDING").status().isFail());
    }

    @Test
    public void bindingWithPatternRule() {
        Rule rule = pattern(binding("zip"), "\\d{5}").build();

        assertTrue(rule.run(zip -> "12345").status().isPass());

        RuleViolations errors = new RuleViolations();
        assertTrue(rule.run(ruleViolations -> errors, zip -> "1234").status().isFail());
    }

    @Test
    public void bindingWithEmailRule() {
        Rule rule = email(binding("address")).build();

        assertTrue(rule.run(address -> "test@example.com").status().isPass());

        RuleViolations errors = new RuleViolations();
        assertTrue(rule.run(ruleViolations -> errors, address -> "not-an-email").status().isFail());
    }

    @Test
    public void bindingWithDecimalMinMax() {
        Rule minRule = decimalMin(binding("price"), new BigDecimal("0.01")).build();
        Rule maxRule = decimalMax(binding("price"), new BigDecimal("999.99")).build();

        assertTrue(minRule.run(price -> new BigDecimal("1.00")).status().isPass());
        assertTrue(maxRule.run(price -> new BigDecimal("999.99")).status().isPass());

        RuleViolations errors = new RuleViolations();
        assertTrue(minRule.run(ruleViolations -> errors, price -> new BigDecimal("0.00")).status().isFail());
        assertTrue(errors.hasErrors());
    }

    @Test
    public void bindingWithEqualsRule() {
        Rule rule = Validators.assertEquals(binding("role"), "ADMIN").build();

        assertTrue(rule.run(role -> "ADMIN").status().isPass());

        RuleViolations errors = new RuleViolations();
        assertTrue(rule.run(ruleViolations -> errors, role -> "USER").status().isFail());
    }

    @Test
    public void bindingWithAssertNotEqualsRule() {
        Rule rule = assertNotEquals(binding("status"), "BANNED").build();

        assertTrue(rule.run(status -> "ACTIVE").status().isPass());

        RuleViolations errors = new RuleViolations();
        assertTrue(rule.run(ruleViolations -> errors, status -> "BANNED").status().isFail());
    }

    @Test
    public void bindingWithAssertTrueRule() {
        Rule rule = Validators.assertTrue(binding("enabled")).build();

        assertTrue(rule.run(enabled -> Boolean.TRUE).status().isPass());

        RuleViolations errors = new RuleViolations();
        assertTrue(rule.run(ruleViolations -> errors, enabled -> Boolean.FALSE).status().isFail());
    }

    @Test
    public void bindingWithAssertFalseRule() {
        Rule rule = Validators.assertFalse(binding("locked")).build();

        assertTrue(rule.run(locked -> Boolean.FALSE).status().isPass());

        RuleViolations errors = new RuleViolations();
        assertTrue(rule.run(ruleViolations -> errors, locked -> Boolean.TRUE).status().isFail());
    }

    @Test
    public void bindingWithPastRule() {
        Rule rule = past(binding("birthDate")).build();

        assertTrue(rule.run(birthDate -> LocalDate.now().minusDays(1)).status().isPass());

        RuleViolations errors = new RuleViolations();
        assertTrue(rule.run(ruleViolations -> errors, birthDate -> LocalDate.now().plusDays(1)).status().isFail());
    }

    @Test
    public void bindingWithFutureRule() {
        Rule rule = future(binding("expiryDate")).build();

        assertTrue(rule.run(expiryDate -> LocalDate.now().plusDays(1)).status().isPass());

        RuleViolations errors = new RuleViolations();
        assertTrue(rule.run(ruleViolations -> errors, expiryDate -> LocalDate.now().minusDays(1)).status().isFail());
    }

    @Test
    public void bindingWithStartsWithRule() {
        Rule rule = startsWith(binding("prefix"), "http://", "https://").build();

        assertTrue(rule.run(prefix -> "https://example.com").status().isPass());

        RuleViolations errors = new RuleViolations();
        assertTrue(rule.run(ruleViolations -> errors, prefix -> "ftp://example.com").status().isFail());
    }

    @Test
    public void bindingWithEndsWithRule() {
        Rule rule = endsWith(binding("fileName"), ".pdf", ".docx").build();

        assertTrue(rule.run(fileName -> "report.pdf").status().isPass());

        RuleViolations errors = new RuleViolations();
        assertTrue(rule.run(ruleViolations -> errors, fileName -> "report.txt").status().isFail());
    }

    @Test
    public void bindingWithUpperCaseRule() {
        Rule rule = upperCase(binding("acronym")).build();

        assertTrue(rule.run(acronym -> "NASA").status().isPass());

        RuleViolations errors = new RuleViolations();
        assertTrue(rule.run(ruleViolations -> errors, acronym -> "nasa").status().isFail());
    }

    @Test
    public void bindingWithLowerCaseRule() {
        Rule rule = lowerCase(binding("slug")).build();

        assertTrue(rule.run(slug -> "helloworld").status().isPass());

        RuleViolations errors = new RuleViolations();
        assertTrue(rule.run(ruleViolations -> errors, slug -> "HelloWorld").status().isFail());
    }

    @Test
    public void bindingWithNullRule() {
        Rule rule = isNull(binding("deletedAt")).build();

        assertTrue(rule.run(deletedAt -> (Object) null).status().isPass());

        RuleViolations errors = new RuleViolations();
        assertTrue(rule.run(ruleViolations -> errors, deletedAt -> "2026-01-01").status().isFail());
    }

    @Test
    public void bindingWithNegativeOrZeroRule() {
        Rule rule = negativeOrZero(binding("balance")).build();

        assertTrue(rule.run(balance -> 0).status().isPass());
        assertTrue(rule.run(balance -> -5).status().isPass());

        RuleViolations errors = new RuleViolations();
        assertTrue(rule.run(ruleViolations -> errors, balance -> 1).status().isFail());
    }

    @Test
    public void bindingWithPositiveOrZeroRule() {
        Rule rule = positiveOrZero(binding("quantity")).build();

        assertTrue(rule.run(quantity -> 0).status().isPass());
        assertTrue(rule.run(quantity -> 10).status().isPass());

        RuleViolations errors = new RuleViolations();
        assertTrue(rule.run(ruleViolations -> errors, quantity -> -1).status().isFail());
    }

    @Test
    public void bindingWithDigitsRule() {
        Rule rule = digits(binding("taxId"), 3, 2).build();

        assertTrue(rule.run(taxId -> new BigDecimal("123.45")).status().isPass());

        RuleViolations errors = new RuleViolations();
        assertTrue(rule.run(ruleViolations -> errors, taxId -> new BigDecimal("1234.56")).status().isFail());
    }

    // -----------------------------------------------------------------------
    // value(Object constant) factory
    // -----------------------------------------------------------------------

    @Test
    public void valueFactoryReturnsConstant() {
        // value() wraps in a plain Function (not BindingFunction)
        Function<?> fn = value("hello");
        assertFalse(fn instanceof BindingFunction,
                "value() should not produce a BindingFunction");
    }

    @Test
    public void valueIsAConstantFunction() {
        assertInstanceOf(ConstantFunction.class, value("hello"));

        RuleContext ruleContext = RuleContext.builder().build(Bindings.builder().standard());
        assertEquals("hello", value("hello").run(ruleContext));
        // The same on the read-only context a condition runs with.
        assertEquals("hello", value("hello").run(ruleContext.asImmutable()));
        assertNull(value(null).run(ruleContext));
        assertThrows(IllegalArgumentException.class, () -> value("hello").run((RuleContext) null));
    }

    @Test
    public void valueHandsOutTheConstantItself() {
        // The constant is returned as it is, also one that could be written through.
        Bindings constant = Bindings.builder().standard(x -> 1);
        RuleContext ruleContext = RuleContext.builder().build(Bindings.builder().standard());

        assertSame(constant, value(constant).run(ruleContext));
        assertSame(constant, value(constant).run(ruleContext.asImmutable()));
    }

    @Test
    public void valueDescribesItselfWithoutTheValue() {
        Function<?> fn = value("s3cret");

        assertInstanceOf(Definable.class, fn);
        assertEquals("value", fn.getName());
        assertEquals("constant value", fn.getDescription());

        MethodDefinition definition = fn.getDefinition();
        assertNotNull(definition);
        assertEquals("value", definition.getName());
        assertEquals("constant value", definition.getDescription());
        assertEquals(Object.class, definition.getReturnType());
        assertEquals(1, definition.getParameterDefinitions().size());
        assertEquals("ruleContext", definition.getParameterDefinition(0).getName());
        assertEquals(RuleContext.class, definition.getParameterDefinition(0).getType());
        assertEquals(ValidatorsTests.class.getName(), definition.getSource().getClassName());
        assertNotSame(definition, value("s3cret").getDefinition());
        assertSame(definition, fn.getExpression().method());
        // The value is often the data under validation: it stays out of what the function says about itself.
        assertFalse(definition.toString().contains("s3cret"));
    }

    @Test
    public void valueIsTheValueFunctionOfTheRule() {
        Function<?> fn = value(25L);
        Rule rule = min(fn, 18L).build();
        ValueValidationRule target = rule.getTarget();
        assertSame(fn, target.getValueFunction());
        assertEquals("value", target.getValueName());
    }

    @Test
    public void valueWithNotNullPassesNoBindingsNeeded() {
        // value() provides the constant; no binding required in context
        Rule rule = notNull(value("hello")).build();
        RuleResult result = rule.run(); // completely empty context
        assertTrue(result.status().isPass());
    }

    @Test
    public void valueNullFailsWithNoBindingsNeeded() {
        Rule rule = notNull(value(null)).build();
        RuleViolations errors = new RuleViolations();
        RuleResult result = rule.run(ruleViolations -> errors);
        assertTrue(result.status().isFail());
        assertTrue(errors.hasErrors());
    }

    @Test
    public void valueDefaultParamNameIsValue() {
        Rule rule = notNull(value(null)).build();
        RuleViolations errors = new RuleViolations();
        rule.run(ruleViolations -> errors);
        RuleViolation v = errors.getViolations().get(0);
        assertTrue(v.getParameters().containsKey("value"),
                "value() should use 'value' as the default param name in violations");
    }

    @Test
    public void valueWithMinRule() {
        Rule rule = min(value(25L), 18L).build();
        assertTrue(rule.run().status().isPass());

        Rule failRule = min(value(10L), 18L).build();
        RuleViolations errors = new RuleViolations();
        assertTrue(failRule.run(ruleViolations -> errors).status().isFail());
    }

    @Test
    public void valueWithNotBlankRule() {
        Rule passRule = notBlank(value("hello")).build();
        assertTrue(passRule.run().status().isPass());

        Rule failRule = notBlank(value("")).build();
        RuleViolations errors = new RuleViolations();
        assertTrue(failRule.run(ruleViolations -> errors).status().isFail());
    }

    @Test
    public void valueWithAlphaNumericRule() {
        Rule passRule = alphaNumeric(value("abc123")).build();
        assertTrue(passRule.run().status().isPass());

        Rule failRule = alphaNumeric(value("abc!")).build();
        RuleViolations errors = new RuleViolations();
        assertTrue(failRule.run(ruleViolations -> errors).status().isFail());
    }

    @Test
    public void valueWithSizeRule() {
        Rule passRule = size(value(List.of("a", "b")), 1, 3).build();
        assertTrue(passRule.run().status().isPass());

        Rule failRule = size(value(List.of()), 1, 3).build();
        RuleViolations errors = new RuleViolations();
        assertTrue(failRule.run(ruleViolations -> errors).status().isFail());
    }

    @Test
    public void valueWithEqualsRule() {
        Rule passRule = Validators.assertEquals(value("ADMIN"), "ADMIN").build();
        assertTrue(passRule.run().status().isPass());

        Rule failRule = Validators.assertEquals(value("USER"), "ADMIN").build();
        RuleViolations errors = new RuleViolations();
        assertTrue(failRule.run(ruleViolations -> errors).status().isFail());
    }

    @Test
    public void valueWithInRule() {
        Rule passRule = in(value("RED"), Arrays.asList("RED", "GREEN", "BLUE")).build();
        assertTrue(passRule.run().status().isPass());

        Rule failRule = in(value("YELLOW"), Arrays.asList("RED", "GREEN", "BLUE")).build();
        RuleViolations errors = new RuleViolations();
        assertTrue(failRule.run(ruleViolations -> errors).status().isFail());
    }

    @Test
    public void valueWithPatternRule() {
        Rule passRule = pattern(value("ABC-123"), "[A-Z]{3}-\\d{3}").build();
        assertTrue(passRule.run().status().isPass());

        Rule failRule = pattern(value("abc-123"), "[A-Z]{3}-\\d{3}").build();
        RuleViolations errors = new RuleViolations();
        assertTrue(failRule.run(ruleViolations -> errors).status().isFail());
    }

    @Test
    public void valueWithDecimalRule() {
        Rule passRule = decimal(value("3.14")).build();
        assertTrue(passRule.run().status().isPass());

        Rule failRule = decimal(value("notANumber")).build();
        RuleViolations errors = new RuleViolations();
        assertTrue(failRule.run(ruleViolations -> errors).status().isFail());
    }

    @Test
    public void valueWithNullIsPassedAsConstant() {
        // A value(null) evaluates to null; notNull should fail
        Rule rule = notNull(value(null)).build();
        RuleViolations errors = new RuleViolations();
        assertTrue(rule.run(ruleViolations -> errors).status().isFail());
    }

    // -----------------------------------------------------------------------
    // Custom Functions.function(lambda) factory
    // -----------------------------------------------------------------------

    @Test
    public void customFunctionNotBingFunction() {
        Function<?> fn = function(() -> "computed");
        assertFalse(fn instanceof BindingFunction,
                "Functions.function() should not produce a BindingFunction");
    }

    @Test
    public void customFunctionWithNotNullPass() {
        String computed = "dynamic value";
        Rule rule = notNull(function(() -> computed)).build();
        assertTrue(rule.run().status().isPass());
    }

    @Test
    public void customFunctionWithNotNullFail() {
        Rule rule = notNull(function(() -> (Object) null)).build();
        RuleViolations errors = new RuleViolations();
        assertTrue(rule.run(ruleViolations -> errors).status().isFail());
    }

    @Test
    public void customFunctionDefaultParamNameIsValue() {
        Rule rule = notNull(function(() -> (Object) null)).build();
        RuleViolations errors = new RuleViolations();
        rule.run(ruleViolations -> errors);
        RuleViolation v = errors.getViolations().get(0);
        assertTrue(v.getParameters().containsKey("value"),
                "Functions.function() should use 'value' as the default param name in violations");
    }

    @Test
    public void customFunctionWithAlpha() {
        Rule passRule = alpha(function(() -> "hello")).build();
        assertTrue(passRule.run().status().isPass());

        Rule failRule = alpha(function(() -> "hello1")).build();
        RuleViolations errors = new RuleViolations();
        assertTrue(failRule.run(ruleViolations -> errors).status().isFail());
    }

    @Test
    public void customFunctionWithMin() {
        Rule rule = min(function(() -> 20), 18L).build();
        assertTrue(rule.run().status().isPass());

        Rule failRule = min(function(() -> 15), 18L).build();
        RuleViolations errors = new RuleViolations();
        assertTrue(failRule.run(ruleViolations -> errors).status().isFail());
    }

    @Test
    public void customFunctionWithDynamicComputation() {
        int[] counter = {0};
        // lambda increments each call — proves the function is called at run-time
        Rule rule = min(function(() -> ++counter[0]), 1L).build();

        assertTrue(rule.run().status().isPass()); // counter = 1,2 → passes min(1)
        assertTrue(rule.run().status().isPass()); // counter = 3,4 → still passes
        // function is called twice per run: once in checkType, once in isValid
        assertEquals(4, counter[0]);
    }

    @Test
    public void customFunctionWithPositive() {
        Rule passRule = positive(function(() -> 42)).build();
        assertTrue(passRule.run().status().isPass());

        Rule failRule = positive(function(() -> -1)).build();
        RuleViolations errors = new RuleViolations();
        assertTrue(failRule.run(ruleViolations -> errors).status().isFail());
    }

    @Test
    public void customFunctionWithEmailRule() {
        Rule passRule = email(function(() -> "test@domain.com")).build();
        assertTrue(passRule.run().status().isPass());

        Rule failRule = email(function(() -> "bad-email")).build();
        RuleViolations errors = new RuleViolations();
        assertTrue(failRule.run(ruleViolations -> errors).status().isFail());
    }

    // -----------------------------------------------------------------------
    // Mixed scenarios and builder fluent API
    // -----------------------------------------------------------------------

    @Test
    public void builderFluentApiWithBinding() {
        Rule rule = notNull(binding("token"))
                .errorCode("token.required")
                .severity(Severity.FATAL)
                .message("Token must not be null")
                .build();

        RuleViolations errors = new RuleViolations();
        RuleResult result = rule.run(ruleViolations -> errors, token -> (Object) null);
        assertTrue(result.status().isFail());

        RuleViolation v = errors.getViolations().get(0);
        assertEquals("token.required", v.getErrorCode());
        assertEquals(Severity.FATAL, v.getSeverity());
        assertEquals("Token must not be null", v.getErrorMessage());
        assertTrue(v.getParameters().containsKey("token"));
    }

    @Test
    public void builderFluentApiWithValue() {
        Rule rule = min(value(5L), 10L)
                .errorCode("min.violated")
                .severity(Severity.WARNING)
                .message("Value too small")
                .build();

        RuleViolations errors = new RuleViolations();
        rule.run(ruleViolations -> errors);
        RuleViolation v = errors.getViolations().get(0);
        assertEquals("min.violated", v.getErrorCode());
        assertEquals(Severity.WARNING, v.getSeverity());
        assertEquals("Value too small", v.getErrorMessage());
    }

    @Test
    public void builderFluentApiWithFunction() {
        Rule rule = notBlank(function(() -> ""))
                .errorCode("blank.error")
                .severity(Severity.ERROR)
                .message("Must not be blank")
                .build();

        RuleViolations errors = new RuleViolations();
        rule.run(ruleViolations -> errors);
        RuleViolation v = errors.getViolations().get(0);
        assertEquals("blank.error", v.getErrorCode());
        assertEquals(Severity.ERROR, v.getSeverity());
        assertEquals("Must not be blank", v.getErrorMessage());
    }

    @Test
    public void bindingNullValueFailsForNonNullableRules() {
        // Rules that treat null as invalid — alpha fails on null
        Rule alphaRule = alpha(binding("optional")).build();
        RuleViolations errors = new RuleViolations();
        RuleResult result = alphaRule.run(ruleViolations -> errors, optional -> (Object) null);
        assertTrue(result.status().isFail());
    }

    @Test
    public void valueNullFailsNonNullableRules() {
        // alpha treats null as invalid
        Rule rule = alpha(value(null)).build();
        RuleViolations errors = new RuleViolations();
        assertTrue(rule.run(ruleViolations -> errors).status().isFail());
    }

    @Test
    public void bindingWrongTypeIsSkipped() {
        // min() supports Number and CharSequence; Boolean is neither → SKIP
        Rule rule = min(binding("value"), 10L).build();
        RuleResult result = rule.run(value -> Boolean.TRUE);
        assertTrue(result.status().isSkipped());
    }

    @Test
    public void valueWrongTypeIsSkipped() {
        // alpha() supports CharSequence; Integer is not CharSequence → SKIP
        Rule rule = alpha(value(42)).build();
        RuleResult result = rule.run();
        assertTrue(result.status().isSkipped());
    }

    @Test
    public void multipleBindingRulesIndependent() {
        Rule nameRule  = notBlank(binding("name")).build();
        Rule emailRule = email(binding("email")).build();
        Rule ageRule   = min(binding("age"), 18L).build();

        assertTrue(nameRule.run(name -> "Alice").status().isPass());
        assertTrue(emailRule.run(email -> "alice@example.com").status().isPass());
        assertTrue(ageRule.run(age -> 20).status().isPass());

        RuleViolations errors = new RuleViolations();
        assertTrue(nameRule.run(ruleViolations -> errors, name -> "").status().isFail());
        assertTrue(emailRule.run(ruleViolations -> errors, email -> "not-valid").status().isFail());
        assertTrue(ageRule.run(ruleViolations -> errors, age -> 16).status().isFail());
        assertEquals(3, errors.getViolations().size());
    }
}
