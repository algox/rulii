---
name: new-validation-rule
description: Guide for creating a new custom ValidationRule in rulii — rule class, builder class, annotations, supported types, isValid logic, and customizeViolation
user-invocable: true
---

# Creating a New Validation Rule in rulii

Use this guide when the user asks to create a custom validation rule that integrates with the `ValueValidationRule` framework (writes violations to `RuleViolations`, supports type checking, severity, error codes).

This guide covers creating the rule and its builder only. It does NOT add it to `Validators`.

---

## 1. File structure

Create two files in a new sub-package under `org.rulii.validation.rules.<rulename>/`:

```
src/main/java/org/rulii/validation/rules/
  mycheck/
    MyCheckValidationRule.java        ← the rule
    MyCheckValidationRuleBuilder.java ← the builder
```

---

## 2. The rule class

```java
package org.rulii.validation.rules.mycheck;

import org.rulii.annotation.Description;
import org.rulii.annotation.Rule;
import org.rulii.context.RuleContext;
import org.rulii.model.function.Function;
import org.rulii.validation.Severity;
import org.rulii.validation.ValueValidationRule;

import java.util.List;

@Rule
@Description("Value must satisfy MyCheck.")
public class MyCheckValidationRule extends ValueValidationRule {

    // Restrict to types this rule can meaningfully validate.
    // Use Object.class to accept any type.
    public static final List<Class<?>> SUPPORTED_TYPES = List.of(String.class);

    // Error code used in RuleViolation — conventionally camelCase + ".errorCode"
    public static final String ERROR_CODE      = "myCheckValidationRule.errorCode";
    public static final String DEFAULT_MESSAGE = "Value {0} failed MyCheck.";

    // Static factory — primary entry point for building this rule
    public static MyCheckValidationRuleBuilder builder(Function<?> function) {
        return new MyCheckValidationRuleBuilder(function);
    }

    // Package-private constructor — callers use builder()
    MyCheckValidationRule(Function<?> valueFunction, String errorCode, Severity severity,
                          String errorMessage, String valueName) {
        super(valueFunction, errorCode, severity, errorMessage, DEFAULT_MESSAGE, valueName);
    }

    /**
     * Core validation logic. Called only when checkType() passes (value type is in SUPPORTED_TYPES).
     *
     * @param ruleContext the active rule context
     * @param value       the resolved value (may be null — handle it)
     * @return true if valid, false to record a violation
     */
    @Override
    protected boolean isValid(RuleContext ruleContext, Object value) {
        if (value == null) return false;          // null → FAIL (standard behaviour)
        String s = (String) value;
        return s.startsWith("MY_");               // example logic — replace with real check
    }

    @Override
    public List<Class<?>> getSupportedTypes() {
        return SUPPORTED_TYPES;
    }

    @Override
    public String toString() {
        return "MyCheckValidationRule";
    }
}
```

### Key points
- `@Rule` and `@Description` are required annotations.
- Constructor is **package-private** — callers use `builder()`.
- `isValid()` is only called when `checkType()` passes, so `value` will be an instance of one of `SUPPORTED_TYPES`. However, it may still be `null` (null passes `checkType()`).
- Wrong-type values → `checkType()` returns `false` → **SKIP** (no violation, no `isValid()` call).

---

## 3. The builder class

```java
package org.rulii.validation.rules.mycheck;

import org.rulii.model.function.Function;
import org.rulii.validation.Severity;
import org.rulii.validation.ValueValidationRuleBuilder;

public class MyCheckValidationRuleBuilder
        extends ValueValidationRuleBuilder<MyCheckValidationRuleBuilder, MyCheckValidationRule> {

    public MyCheckValidationRuleBuilder(Function<?> valueFunction) {
        super(valueFunction);
        // Set defaults — can be overridden by callers before .build()
        errorCode(MyCheckValidationRule.ERROR_CODE);
        severity(Severity.ERROR);
        message(MyCheckValidationRule.DEFAULT_MESSAGE);
    }

    @Override
    protected MyCheckValidationRule createValueValidationRule() {
        return new MyCheckValidationRule(
                getValueFunction(),
                getErrorCode(),
                getSeverity(),
                getErrorMessage(),
                getValueName()
        );
    }
}
```

### Builder API (inherited from `ValueValidationRuleBuilder`)
```
MyCheckValidationRule.builder(fn)
  .errorCode("custom.code")        // override default error code
  .severity(Severity.WARNING)      // default is ERROR
  .message("Custom message")       // override default message
  .valueName("fieldLabel")         // name used in RuleViolation params (defaults to binding name)
  .name("CustomRuleName")          // override the rule's name
  .description("...")
  .build()                         // returns Rule (wraps the ValidationRule)
```

---

## 4. Usage

```java
import static org.rulii.validation.rules.Validators.binding;
import static org.rulii.validation.rules.Validators.value;

// Validate a named binding
Rule rule = MyCheckValidationRule.builder(binding("myField")).build();

// Validate a constant value
Rule rule = MyCheckValidationRule.builder(value(someObject)).build();

// Custom error message
Rule rule = MyCheckValidationRule.builder(binding("code"))
        .message("Code must start with MY_")
        .severity(Severity.WARNING)
        .build();

// Use in a RuleSet
RuleSet<?> ruleSet = RuleSet.builder()
        .with("MyValidation")
        .rule(MyCheckValidationRule.builder(binding("code")).build())
        .validating()
        .build();
```

---

## 5. Rules with extra constructor parameters

If the rule needs configuration beyond the standard fields (e.g. a min length), add them to the constructor and builder:

```java
// Rule constructor
MyCheckValidationRule(Function<?> valueFunction, String errorCode, Severity severity,
                      String errorMessage, String valueName, int minLength) {
    super(valueFunction, errorCode, severity, errorMessage, DEFAULT_MESSAGE, valueName);
    this.minLength = minLength;
}

// Builder
public class MyCheckValidationRuleBuilder
        extends ValueValidationRuleBuilder<MyCheckValidationRuleBuilder, MyCheckValidationRule> {

    private int minLength;

    public MyCheckValidationRuleBuilder(Function<?> valueFunction, int minLength) {
        super(valueFunction);
        this.minLength = minLength;
        errorCode(MyCheckValidationRule.ERROR_CODE);
        severity(Severity.ERROR);
        message(MyCheckValidationRule.DEFAULT_MESSAGE);
    }

    @Override
    protected MyCheckValidationRule createValueValidationRule() {
        return new MyCheckValidationRule(
                getValueFunction(), getErrorCode(), getSeverity(),
                getErrorMessage(), getValueName(), minLength);
    }
}

// Static factory on the rule class
public static MyCheckValidationRuleBuilder builder(Function<?> function, int minLength) {
    return new MyCheckValidationRuleBuilder(function, minLength);
}
```

---

## 6. Customising the violation (extra params in violation message)

Override `customizeViolation()` to add extra parameters that can be referenced in the message template:

```java
@Override
protected void customizeViolation(RuleContext ruleContext, RuleViolationBuilder builder) {
    builder.param("minLength", this.minLength);
    // Message template can then use {1} to reference minLength
}

// DEFAULT_MESSAGE = "Value {0} must be at least {1} characters long."
```

---

## 7. Supported types — common patterns

```java
// Accept any type (like NotNullValidationRule)
public static final List<Class<?>> SUPPORTED_TYPES = List.of(Object.class);

// Strings only
public static final List<Class<?>> SUPPORTED_TYPES = List.of(CharSequence.class);

// Numbers only
public static final List<Class<?>> SUPPORTED_TYPES = List.of(Number.class);

// Collections only
public static final List<Class<?>> SUPPORTED_TYPES = List.of(Collection.class);

// Multiple types
public static final List<Class<?>> SUPPORTED_TYPES = List.of(String.class, CharSequence.class);
```

Type matching uses `Class.isAssignableFrom()` — specifying `CharSequence.class` will accept `String`, `StringBuilder`, etc.

---

## Checklist

- [ ] Class annotated with `@Rule` and `@Description`
- [ ] Constructor is package-private
- [ ] Static `builder(Function<?> function)` factory method present
- [ ] `ERROR_CODE` and `DEFAULT_MESSAGE` constants defined
- [ ] `isValid()` handles `null` explicitly
- [ ] `getSupportedTypes()` returns the right types
- [ ] Builder extends `ValueValidationRuleBuilder<BuilderType, RuleType>`
- [ ] Builder constructor sets `errorCode`, `severity`, `message` defaults
- [ ] Builder implements `createValueValidationRule()` passing all constructor args
