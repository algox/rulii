---
name: write-test
description: Guide for writing JUnit 5 tests in the rulii project — test structure, known JUnit 5.12.1 ambiguities, PASS/FAIL/SKIP patterns, ValidationRule test patterns, and missing-binding test patterns
user-invocable: true
---

# Writing Tests in rulii

Use this guide when writing JUnit 5 tests for rules, validation rules, RuleSets, bindings, or any other rulii component.

Tests live in `src/test/java/org/rulii/test/`.

---

## 1. Standard test class structure

```java
package org.rulii.test.<package>;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.bind.Bindings;
import org.rulii.context.RuleContext;
import org.rulii.model.UnrulyException;
import org.rulii.rule.Rule;
import org.rulii.validation.RuleViolations;

import static org.rulii.model.condition.Conditions.condition;
import static org.rulii.model.action.Actions.action;
import static org.rulii.validation.rules.Validators.binding;

public class MyFeatureTests {

    @Test
    public void testSomething() {
        // arrange
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("age", Integer.class, 25);
        RuleContext ctx = RuleContext.builder().build(bindings);

        // act
        Rule rule = Rule.builder()
                .name("AgeRule")
                .given(condition((Integer age) -> age >= 18))
                .build();
        var result = rule.run(ctx);

        // assert
        Assertions.assertEquals(org.rulii.rule.RuleExecutionStatus.PASS, result.getStatus());
    }
}
```

---

## 2. JUnit 5.12.1 — Known Ambiguity Issues

These cause **compile errors**, not runtime errors. Fix them as shown.

### 2a. `assertEquals` with int literal + Object + message

```java
// AMBIGUOUS — new overloads assertEquals(float, Float, String) / (double, Double, String)
assertEquals(5, someObject, "message");

// FIX — cast to Number and extract int
Assertions.assertEquals(5, ((Number) someObject).intValue(), "message");

// Or use the explicit class prefix without static import
Assertions.assertEquals(5, someObject);  // only ambiguous when 3rd String arg present
```

### 2b. `assertDoesNotThrow` with method reference returning `Void`

```java
// AMBIGUOUS — Void return creates overload conflict
assertDoesNotThrow(action::run);

// FIX — use block lambda
assertDoesNotThrow(() -> { action.run(); });
```

### 2c. `Validators` method name clash with JUnit static imports

`Validators.assertEquals`, `Validators.assertFalse`, `Validators.assertTrue`, `Validators.assertNotEquals`
clash with JUnit's static imports.

```java
// Option 1 — explicit class prefix (most readable)
import static org.rulii.validation.rules.Validators.*;
// Then call:
Validators.assertEquals(...)   // not assertEquals() — would resolve to JUnit

// Option 2 — wildcard Validators import, explicit JUnit imports
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.rulii.validation.rules.Validators.*;
// JUnit explicit imports take precedence over Validators wildcard
```

---

## 3. Testing PASS / FAIL / SKIP

### PASS
```java
Bindings bindings = Bindings.builder().standard();
bindings.bind("value", String.class, "hello");
bindings.bind("ruleViolations", RuleViolations.class, new RuleViolations());
RuleContext ctx = RuleContext.builder().build(bindings);

Rule rule = NotNullValidationRule.builder(binding("value")).build();
var result = rule.run(ctx);

Assertions.assertEquals(RuleExecutionStatus.PASS, result.getStatus());
Assertions.assertTrue(bindings.getValue("ruleViolations", RuleViolations.class).isEmpty());
```

### FAIL (violation recorded)
```java
Bindings bindings = Bindings.builder().standard();
bindings.bind("value", String.class, null);                         // null → FAIL
bindings.bind("ruleViolations", RuleViolations.class, new RuleViolations());
RuleContext ctx = RuleContext.builder().build(bindings);

Rule rule = NotNullValidationRule.builder(binding("value")).build();
rule.run(ctx);

RuleViolations violations = bindings.getValue("ruleViolations", RuleViolations.class);
Assertions.assertFalse(violations.isEmpty());
Assertions.assertEquals(NotNullValidationRule.ERROR_CODE, violations.getViolation(0).getErrorCode());
```

### SKIP (type mismatch — no violation, no otherwise)
```java
// Pass a Boolean where the rule expects CharSequence → checkType() returns false → SKIP
Bindings bindings = Bindings.builder().standard();
bindings.bind("value", Boolean.class, true);
bindings.bind("ruleViolations", RuleViolations.class, new RuleViolations());
RuleContext ctx = RuleContext.builder().build(bindings);

Rule rule = NotEmptyValidationRule.builder(binding("value")).build();
var result = rule.run(ctx);

Assertions.assertEquals(RuleExecutionStatus.SKIP, result.getStatus());
Assertions.assertTrue(bindings.getValue("ruleViolations", RuleViolations.class).isEmpty());
```

> **Prefer type-mismatch SKIP over missing-binding** when testing SKIP inside a RuleSet.
> Missing bindings throw `UnrulyException`, which is harder to isolate.

---

## 4. Testing missing bindings

A missing binding propagates `NoSuchBindingException` → wrapped in `UnrulyException`:

```java
Bindings bindings = Bindings.builder().standard();
// "value" binding is intentionally absent
RuleContext ctx = RuleContext.builder().build(bindings);

Rule rule = NotNullValidationRule.builder(binding("value")).build();

// Assert UnrulyException — NOT NoSuchBindingException
Assertions.assertThrows(UnrulyException.class, () -> rule.run(ctx));
```

---

## 5. Testing null values (FAIL, not SKIP)

Most rules return `false` from `isValid(null)` → FAIL (not SKIP — null passes `checkType()`).
`RuleViolations` must be present or `otherwise()` throws:

```java
Bindings bindings = Bindings.builder().standard();
bindings.bind("value", String.class, null);
bindings.bind("ruleViolations", RuleViolations.class, new RuleViolations());  // required
RuleContext ctx = RuleContext.builder().build(bindings);

Rule rule = NotEmptyValidationRule.builder(binding("value")).build();
rule.run(ctx);

Assertions.assertFalse(bindings.getValue("ruleViolations", RuleViolations.class).isEmpty());
```

---

## 6. Testing a ValidationException from a validating RuleSet

```java
RuleSet<?> ruleSet = RuleSet.builder()
        .with("MyValidation")
        .param("email", String.class)
        .rule(NotNullValidationRule.builder(binding("email")).build())
        .rule(EmailValidationRule.builder(binding("email")).build())
        .validating()
        .build();

ValidationException ex = Assertions.assertThrows(
        ValidationException.class,
        () -> ruleSet.run(email -> "not-an-email")
);

Assertions.assertEquals(1, ex.getViolations().size());
Assertions.assertEquals(EmailValidationRule.ERROR_CODE, ex.getViolations().getViolation(0).getErrorCode());
```

---

## 7. Testing conditions in isolation

```java
Condition condition = Condition.builder().build((Integer age) -> age >= 18);

// Direct lambda test — no RuleContext needed
Assertions.assertTrue(condition.isTrue(age -> 25));
Assertions.assertFalse(condition.isTrue(age -> 16));
```

---

## 8. Quick reference — common imports

```java
// Assertions
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

// Core builders
import org.rulii.bind.Bindings;
import org.rulii.context.RuleContext;
import org.rulii.rule.Rule;
import org.rulii.rule.RuleExecutionStatus;
import org.rulii.ruleset.RuleSet;

// Validation
import org.rulii.validation.RuleViolations;
import org.rulii.validation.ValidationException;
import org.rulii.model.UnrulyException;

// Static helpers
import static org.rulii.model.condition.Conditions.condition;
import static org.rulii.model.action.Actions.action;
import static org.rulii.validation.rules.Validators.binding;
import static org.rulii.validation.rules.Validators.value;
```
