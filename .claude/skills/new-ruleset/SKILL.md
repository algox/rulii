---
name: new-ruleset
description: Guide for creating a RuleSet in rulii — covers builder API, lifecycle hooks, input params, stop conditions, validating mode, async execution, and error handling
user-invocable: true
---

# Creating a RuleSet in rulii

Use this guide when the user asks to create, design, or explain a RuleSet.

## 1. Minimal RuleSet

```java
RuleSet<?> ruleSet = RuleSet.builder()
        .with("MyRuleSet")
        .rule(Rule.builder().name("Rule1").given(condition((Integer age) -> age >= 18)).build())
        .rule(Rule.builder().name("Rule2").given(condition((String email) -> email.contains("@"))).build())
        .build();

// Run with Bindings
Bindings bindings = Bindings.builder().standard();
bindings.bind(age -> 21);
bindings.bind(email -> "user@example.com");
RuleContext ctx = RuleContext.builder().build(bindings);
ruleSet.run(ctx);

// Or run with inline BindingDeclaration lambdas (parameter name becomes binding name)
ruleSet.run(age -> 21, email -> "user@example.com");
```

---

## 2. Builder API

```
RuleSet.builder()
  .with(name)                     // required — name must match [A-Za-z][A-Za-z0-9_]*
  .with(name, description)        // optional description
  .description(...)               // set/override description after .with()
  .param(name, Type.class)        // declare required input (validated before run)
  .param(name, Type.class, false) // optional input
  .param(name, Type.class, defaultValueFunction) // optional with default
  .preCondition(condition)        // skip entire RuleSet if false
  .initializer(action)            // runs before any rule
  .rule(rule)                     // add a Rule or ValidationRule
  .rules(rule1, rule2, ...)       // add multiple at once
  .rule(index, rule)              // insert at position
  .stopCondition(condition)       // halt mid-execution when true
  .finalizer(action)              // always runs after all rules
  .resultExtractor(function)      // customize what run() returns
  .errorHandler(function)         // handle exceptions from lifecycle methods
  .validating()                   // shortcut: adds RuleViolations param + throwing finalizer
  .build()
```

---

## 3. Lifecycle execution order

```
preCondition?  →  (false = skip entire RuleSet)
initializer?
  for each Rule:
    Rule.run(ctx)
    stopCondition?  →  (true = halt loop)
finalizer?
resultExtractor  →  (returns result to caller)
```

If any lifecycle step throws, the **errorHandler** is invoked instead of propagating directly.

---

## 4. Input Parameters

Declare expected bindings explicitly — the framework validates they exist before running:

```java
RuleSet<?> ruleSet = RuleSet.builder()
        .with("UserValidation")
        .param("name", String.class)               // required
        .param("age", Integer.class)               // required
        .param("email", String.class, false)       // optional, may be absent
        .param("role", String.class,               // optional with computed default
               Functions.function(() -> "GUEST"))
        .rule(...)
        .build();

// Pass values as BindingDeclaration lambdas — no Bindings object needed
ruleSet.run(name -> "Alice", age -> 30, email -> "alice@example.com");
```

---

## 5. Stop Conditions — `RuleSetConditions`

Use built-in stop conditions to control short-circuit behaviour:

```java
import org.rulii.ruleset.RuleSetConditions;

// Stop after first rule fails
.stopCondition(RuleSetConditions.stopWhenOneFails())

// Stop after first rule passes
.stopCondition(RuleSetConditions.stopWhenOnePasses())

// Stop after first fail OR first skip
.stopCondition(RuleSetConditions.stopWhenOneFailsOrSkipped())

// Stop after N passes / fails / skips
.stopCondition(RuleSetConditions.stopOnPassCount(2))
.stopCondition(RuleSetConditions.stopOnFailCount(3))
.stopCondition(RuleSetConditions.stopOnSkipCount(1))
```

Use `RuleSetConditions` as **preConditions** too:

```java
// Only run the RuleSet if all contained rules would pass
.preCondition(RuleSetConditions.allMustPass())

// Exactly one rule must pass (mutual exclusion)
.preCondition(RuleSetConditions.onlyOneCanPass())

// At least one must pass
.preCondition(RuleSetConditions.oneMustPass())

// None can pass
.preCondition(RuleSetConditions.noneCanPass())
```

---

## 6. Validation RuleSet — `.validating()`

The `.validating()` shortcut is the standard pattern for field-level validation:
- Adds a `ruleViolations` param with a `RuleViolations` default
- Installs a finalizer that throws `ValidationException` if any severe violations exist

```java
RuleSet<?> ruleSet = RuleSet.builder()
        .with("UserValidation")
        .param("name", String.class)
        .param("age", Integer.class)
        .rule(NotNullValidationRule.builder(binding("name")).build())
        .rule(MinValidationRule.builder(binding("age"), 18L).message("Must be 18+").build())
        .validating()    // <-- wires up RuleViolations + throwing finalizer
        .build();

try {
    ruleSet.run(name -> null, age -> 15);
} catch (ValidationException e) {
    List<RuleViolation> violations = e.getViolations().getViolations();
}
```

> **Note**: `ValidationRule` instances added via `.rule(validationRule)` automatically wrap in a `Rule`
> and write violations to the `ruleViolations` binding. Type mismatches → SKIP (no violation written).

---

## 7. Custom Result Extractor

By default `run()` returns the `RuleSetExecutionStatus` (pass/fail/skip lists). Override to return domain data:

```java
RuleSet<String> ruleSet = RuleSet.<String>builder()
        .with("ClassifyRuleSet")
        .rule(...)
        .resultExtractor(Functions.function(
                (RuleSetExecutionStatus status) -> status.getPassed().isEmpty() ? "REJECTED" : "APPROVED"
        ))
        .build();

String decision = ruleSet.run(ctx);
```

---

## 8. Error Handling

Exceptions from **lifecycle methods** (initializer, finalizer) — not from rule conditions — trigger the error handler.

### Default behaviour
- `ValidationException` root cause → rethrown as-is
- Everything else → wrapped in `UnrulyException`

### Custom error handler
```java
.errorHandler(Functions.function((RuleContext ctx, RuleSet<?> rs, RuleSetExecutionStatus status, Exception ex) -> {
    log.error("RuleSet [{}] failed: {}", rs.getName(), ex.getMessage());
    return null;   // swallow; return a value; or rethrow
}))
```

The handler can receive any subset of `(RuleContext, RuleSet<?>, RuleSetExecutionStatus, Exception)` —
the framework matches by type, so you only declare the parameters you need:

```java
// Minimal — just the exception
.errorHandler(Functions.function((Exception ex) -> {
    log.warn("Handled: {}", ex.getMessage());
    return "fallback";
}))
```

The exception is also accessible as the `"ex"` binding inside the handler's `RuleContext`.

---

## 9. Async Execution

```java
// Fire and forget
CompletableFuture<RuleSetExecutionStatus> future = ruleSet.runAsync(ctx);

// With timeout
CompletableFuture<RuleSetExecutionStatus> future = ruleSet.runAsync(ctx, 5, TimeUnit.SECONDS);

future.thenAccept(status -> System.out.println("Passed: " + status.getPassed().size()));
```

---

## 10. Copy / Mutate an existing RuleSet

```java
// Build from an existing RuleSet — copies all settings
RuleSetBuilder copy = RuleSet.builder().with(existingRuleSet);

// Add or replace rules, then rebuild
copy.rule(newRule).build();
```

---

## Common Mistakes

| Mistake | Fix |
|---------|-----|
| Condition returning `false` triggers error handler | It does NOT — only unchecked exceptions from lifecycle methods do |
| Forgetting `.validating()` when using `ValidationRule` | Without it, violations are collected but never thrown |
| Passing a `Bindings` object as a single lambda arg | Pass individual `BindingDeclaration` lambdas: `run(age -> 21, name -> "x")` |
| Wrong-type binding for a `ValidationRule` | Type mismatch → SKIP (no violation). Use correct type or the rule silently skips |
| Missing binding for a `ValidationRule` | `NoSuchBindingException` → wrapped in `UnrulyException`. Use `param()` to declare required inputs |
