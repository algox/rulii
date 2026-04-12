---
name: new-rule
description: Guide for creating a Rule in rulii — lambda-based and class-based styles, conditions, actions, otherwise, preCondition, and running rules manually
user-invocable: true
---

# Creating a Rule in rulii

Use this guide when the user asks to create, compose, or explain a Rule.

A Rule is an **if / then / otherwise** construct: a Condition that is tested, one or more Actions run when it passes, and an optional otherwise Action run when it fails.

---

## 1. Minimal Rule (lambda-based)

```java
Rule rule = Rule.builder()
        .name("AgeCheck")
        .given(condition((Integer age) -> age >= 18))
        .build();
```

- `Rule.builder()` returns the singleton `RuleBuilder`.
- `.name(String)` is required for lambda-based rules.
- `.given(Condition)` sets the main condition. A rule with no condition defaults to always-true (with a warning).

---

## 2. Full builder API

```
Rule.builder()
  .name("RuleName")             // required for lambda rules; must match [A-Za-z][A-Za-z0-9_]*
  .name("RuleName", "desc")     // name + description shorthand
  .description("...")           // optional description
  .preCondition(condition)      // skip this rule entirely if false (no then/otherwise called)
  .given(condition)             // main condition — required
  .then(action)                 // action(s) when condition is true (chainable)
  .then(action2)
  .otherwise(action)            // action when condition is false
  .build()
```

---

## 3. Condition and Action factories

Import the static helpers for concise lambda syntax:

```java
import static org.rulii.model.condition.Conditions.condition;
import static org.rulii.model.action.Actions.action;
```

**Condition examples:**
```java
// Single binding resolved by parameter name
condition((Integer age) -> age >= 18)

// Multiple bindings
condition((String firstName, String lastName) -> firstName != null && lastName != null)

// Always true / false
Conditions.TRUE()
Conditions.FALSE()

// Combine with .and() / .or() / .negate()
condition((Integer age) -> age >= 18).and(condition((String email) -> email.contains("@")))
```

**Action examples:**
```java
// Mutation via Binding<T>
action((Binding<Integer> count) -> count.setValue(count.getValue() + 1))

// Side effect with a plain value
action((String name) -> System.out.println("Hello " + name))

// Access the full Bindings
action((Bindings bindings) -> bindings.bind(result -> "done"))

// No-op
Actions.EMPTY_ACTION()
```

---

## 4. PreCondition vs. main Condition

| | PreCondition | Condition |
|--|--|--|
| **Fails → result** | SKIP (no then/otherwise) | otherwise action runs (if set) |
| **Typical use** | Guard — wrong type, missing context | Business logic |
| **Class-based** | `@PreCondition` annotation | `@Given` annotation |

```java
Rule rule = Rule.builder()
        .name("NumericCheck")
        .preCondition(condition((Object value) -> value instanceof String))  // SKIP if not a String
        .given(condition((String value) -> value.matches("\\d+")))
        .otherwise(action((RuleViolations rv) -> rv.add(...)))
        .build();
```

---

## 5. Class-based Rules

Annotate a plain class — rulii discovers `@Given`, `@Then`, `@Otherwise`, `@PreCondition` by annotation:

```java
@org.rulii.annotation.Rule
@Description("User must be of legal age")
public class AgeCheckRule {

    @PreCondition
    public boolean isApplicable(Object value) {
        return value instanceof Integer;
    }

    @Given
    public boolean isValid(Integer age) {
        return age >= 18;
    }

    @Then
    public void onPass(Bindings bindings) {
        // runs when condition is true
    }

    @Otherwise
    public void onFail(RuleViolations violations) {
        // runs when condition is false
    }
}

// Build from class
Rule rule = Rule.builder().build(AgeCheckRule.class);

// Build from instance (useful when constructor arguments are needed)
Rule rule = Rule.builder().build(new AgeCheckRule());
```

---

## 6. Running a Rule

**Via RuleContext (normal path — used in RuleSets):**
```java
Bindings bindings = Bindings.builder().standard();
bindings.bind(age -> 25);
RuleContext ctx = RuleContext.builder().build(bindings);

RuleResult result = rule.run(ctx);
// result.getStatus() → PASS, FAIL, or SKIP
```

**Manually testing the condition (no actions fired):**
```java
// Pass BindingDeclaration lambdas — parameter name becomes binding name
boolean passes = rule.getCondition().isTrue(age -> 25);
boolean passes = rule.getCondition().isTrue(age -> 25, email -> "a@b.com");

// Or with a RuleContext
boolean passes = rule.isTrue(ctx);
```

**Shorthand run with inline bindings:**
```java
rule.run(age -> 25, name -> "Alice");
```

---

## 7. RuleResult status values

| Status | Meaning |
|--------|---------|
| `PASS` | preCondition passed, condition returned `true`, then-actions ran |
| `FAIL` | preCondition passed, condition returned `false`, otherwise ran |
| `SKIP` | preCondition returned `false` — rule was bypassed entirely |

---

## 8. Adding tracing

```java
RuleContext ctx = RuleContext.builder()
        .tracer(Tracer.builder()
                .listener(new RuliiListener() {
                    @Override
                    public void onRuleConditionCheck(Rule rule, Condition condition, boolean result) {
                        System.out.println(rule.getName() + " condition = " + result);
                    }
                })
                .build())
        .build(bindings);
```

---

## Common Mistakes

| Mistake | Fix |
|---------|-----|
| No `.name()` on a lambda rule | Required — the framework throws `IllegalArgumentException` |
| Calling `.run(bindings)` instead of `.run(ctx)` | Wrap bindings: `RuleContext.builder().build(bindings)`, then `rule.run(ctx)` |
| Expecting `otherwise` to fire on SKIP | It doesn't — only fires when condition is `false`, not when preCondition is `false` |
| Passing a `Bindings` container as a single lambda to `isTrue()` | Pass individual `BindingDeclaration` lambdas: `isTrue(age -> 25, name -> "x")` |
