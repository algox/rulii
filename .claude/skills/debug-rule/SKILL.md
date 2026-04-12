---
name: debug-rule
description: Diagnostic guide for troubleshooting rulii rules that aren't firing, returning unexpected PASS/FAIL/SKIP, or throwing exceptions — covers bindings, types, scope, tracing, and validation violations
user-invocable: true
---

# Debugging a Rule in rulii

Use this guide when a rule is not behaving as expected: wrong result, unexpected SKIP, missing violation, or an exception.

---

## Step 1 — Identify the result status

Every `rule.run(ctx)` returns a `RuleResult`. Check it first:

```java
RuleResult result = rule.run(ctx);
System.out.println(result.getStatus()); // PASS, FAIL, or SKIP
```

| Status | What happened |
|--------|--------------|
| `PASS` | preCondition ✓, condition ✓, then-actions ran |
| `FAIL` | preCondition ✓, condition ✗, otherwise ran |
| `SKIP` | preCondition returned `false` — rule was bypassed entirely |

---

## Step 2 — Diagnose SKIP

SKIP means the **preCondition** returned false. For `ValidationRule` subclasses (all 34 built-ins) the preCondition is `checkType()`.

**Common cause: type mismatch.**
```java
// Rule expects CharSequence, but binding holds a Boolean
bindings.bind("value", Boolean.class, true);
// → checkType() returns false → SKIP (no violation, no otherwise call)
```

**Fix:** Ensure the binding type matches `getSupportedTypes()` for the rule:
```java
// Check supported types
System.out.println(rule.getSupportedTypes());  // e.g. [class java.lang.CharSequence]

// Fix the binding type
bindings.bind("value", String.class, "hello");
```

**Custom rules:** If you have a `@PreCondition` method, add a print or debugger breakpoint there.

---

## Step 3 — Diagnose FAIL (unexpected)

Condition returned `false`. Verify what value is actually being seen:

```java
// 1. Print the binding value directly
System.out.println(bindings.getValue("fieldName"));

// 2. Test the condition in isolation
boolean result = rule.getCondition().isTrue(fieldName -> actualValue);
System.out.println("Condition result: " + result);

// 3. For ValidationRules — check isValid() directly
// (cast to the concrete type)
NotNullValidationRule vr = ...; // reference before wrapping in Rule.builder()
System.out.println(vr.isValid(ctx, bindings.getValue("fieldName")));
```

**Null value:** Most rules return `false` from `isValid(null)` → FAIL. Check for nulls:
```java
Object val = bindings.getValue("fieldName");
System.out.println("Value is: " + val + " / type: " + (val == null ? "null" : val.getClass()));
```

---

## Step 4 — Diagnose missing binding (`UnrulyException`)

If you see `UnrulyException` wrapping `NoSuchBindingException`:

```
org.rulii.model.UnrulyException: ...
Caused by: org.rulii.bind.NoSuchBindingException: No binding found for name [fieldName]
```

**Checklist:**
1. Is the binding name spelled exactly right? Bindings are case-sensitive.
2. Is it in the right scope? Check the active scope:
   ```java
   System.out.println(ctx.getBindings().getNames());
   ```
3. Was the binding added to the right `Bindings` instance? (Scoped vs standard)
4. For `Validators.binding("name")` — is the string the same as the binding name?

---

## Step 5 — Diagnose missing violation (ValidationRule ran but no violation recorded)

**Cause A:** `RuleViolations` binding is not in the context.
```java
// ValidationRule.otherwise() REQUIRES a ruleViolations binding
// Add it manually, or use .validating() on the RuleSet
bindings.bind("ruleViolations", RuleViolations.class, new RuleViolations());
```

**Cause B:** The rule SKIPped (type mismatch) — see Step 2.

**Cause C:** The rule PASSed — the value actually satisfied the condition.

**Inspect violations after run:**
```java
RuleViolations violations = bindings.getValue("ruleViolations", RuleViolations.class);
violations.getViolations().forEach(v ->
    System.out.println(v.getErrorCode() + ": " + v.getMessage()));
```

---

## Step 6 — Enable tracing

Add a listener to the `RuleContext` to trace every lifecycle event:

```java
RuleContext ctx = RuleContext.builder()
        .tracer(Tracer.builder()
                .listener(new RuleListener() {
                    @Override
                    public void onRulePreConditionCheck(Rule rule, Condition c, boolean result) {
                        System.out.printf("[%s] preCondition = %b%n", rule.getName(), result);
                    }
                    @Override
                    public void onRuleConditionCheck(Rule rule, Condition c, boolean result) {
                        System.out.printf("[%s] condition = %b%n", rule.getName(), result);
                    }
                    @Override
                    public void onRuleEnd(Rule rule, RuleResult result) {
                        System.out.printf("[%s] result = %s%n", rule.getName(), result.getStatus());
                    }
                    @Override
                    public void onRuleError(Rule rule, Exception e) {
                        System.out.printf("[%s] ERROR: %s%n", rule.getName(), e.getMessage());
                    }
                })
                .build())
        .build(bindings);
```

---

## Step 7 — Inspect the rule definition

Print what the framework thinks the rule looks like:

```java
RuleDefinition def = rule.getDefinition();
System.out.println("Name:        " + def.getName());
System.out.println("Condition:   " + def.getConditionDefinition());
System.out.println("PreCond:     " + def.getPreConditionDefinition());
System.out.println("Parameters:  " + def.getConditionDefinition().getParameterDefinitions());
```

Parameter names come from the **LocalVariableTable** in bytecode. If names show as `arg0`, `arg1`, the class was compiled without debug info — check that `maven-compiler-plugin` does not have `<debuglevel>none</debuglevel>`.

---

## Decision Tree

```
Rule result is unexpected
│
├─ SKIP?
│   └─ preCondition returned false
│       ├─ ValidationRule → type mismatch (checkType) → fix binding type
│       └─ Custom rule    → debug @PreCondition method
│
├─ FAIL (expected PASS)?
│   ├─ Check the actual binding value — null? wrong value?
│   ├─ Test condition in isolation: rule.getCondition().isTrue(x -> val)
│   └─ Multi-binding rule? Confirm ALL bindings are present and correct
│
├─ PASS (expected FAIL)?
│   └─ Condition logic may be inverted — test isValid() directly
│
├─ No violation recorded?
│   ├─ Rule SKIPped? → see SKIP branch
│   ├─ ruleViolations binding missing? → add it or use .validating()
│   └─ Rule PASSed? → value satisfied the condition
│
└─ UnrulyException?
    ├─ Caused by NoSuchBindingException → binding name wrong or missing
    └─ Other cause → enable tracing to find exact failure point
```
