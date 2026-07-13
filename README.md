[rulii Maven Central]:https://central.sonatype.com/artifact/org.rulii/rulii
[Apache 2.0 License]:https://opensource.org/licenses/Apache-2.0

# _rulii_
**A lightweight, lambda-based business rule engine for Java 17+** <br/>
<sub> _100% Java_ &middot; _Zero dependencies_ &middot; _Declarative & Functional_ &middot; _RuleFlow orchestration_ &middot; _34 built-in validators_ &middot; _Scripting support_ &middot; _Spring support_ </sub>

---

[![License](https://img.shields.io/badge/license-Apache%202.0-orange.svg)][Apache 2.0 License]
[![Maven Central Version](https://img.shields.io/maven-central/v/org.rulii/rulii)][rulii Maven Central]
[![Javadoc](https://javadoc.io/badge2/org.rulii/rulii/javadoc.svg)](https://javadoc.io/doc/org.rulii/rulii/latest)
![Build](https://github.com/algox/rulii/actions/workflows/maven.yml/badge.svg)

---

**📖 Official documentation at [rulii.com](https://rulii.com) or the mirror [here](https://rulii.netlify.app/)**

**🧪 Sample projects at [rulii-samples](https://github.com/algox/rulii-samples)**

---

## Table of Contents

- [What is it?](#what-is-it)
- [Why rulii?](#why-rulii)
- [What's New in 2.0.0](#whats-new-in-200)
- [Getting Started](#getting-started)
- [Writing Rules](#writing-rules)
- [Writing RuleSets](#writing-rulesets)
- [Writing RuleFlows](#writing-ruleflows)
- [Built-in Validators](#built-in-validators)
- [Scripting Support](#scripting-support)
- [Spring Integration](#spring-integration)
- [Claude Code Skills](#claude-code-skills)
- [Documentation](#documentation)
- [Contributing](#contributing)

---

## What is it?

_rulii_ is a rule engine that organizes business logic through a set of rules, each consisting of a condition and
one or more actions. Rules run against named, typed data (Bindings), evaluate their conditions, and trigger actions
when those conditions are met.

This promotes a clean separation between business logic and data — making rules easy to test, reuse, and change
independently of application code. It is particularly useful for data validation, qualification workflows,
pricing logic, and any scenario where decisions are driven by a configurable set of conditions.

---

## Why rulii?

| | rulii | Drools | Easy Rules |
|---|---|---|---|
| Zero dependencies | ✅ | ❌ | ✅ |
| Lambda / functional API | ✅ | ❌ | Partial |
| Declarative annotation API | ✅ | ✅ | ✅ |
| Flow orchestration (RuleFlow) | ✅ | Separate (jBPM) | ❌ |
| 34 built-in validators | ✅ | ❌ | ❌ |
| Scripting (JSR-223) | ✅ | ✅ | ❌ |
| Spring integration | ✅ | ✅ | ✅ |
| Java 17+ | ✅ | ✅ | ✅ |
| Learning curve | Low | High | Low |

rulii is designed for teams that want the power of a rule engine without the weight of a full platform.
Rules are plain Java — no proprietary DSL, no XML, no extra runtime.

---

## What's New in 2.0.0

- **RuleFlow** (`org.rulii.ruleflow`) — a new fluent orchestration API that composes Rules, RuleSets, and other
  RuleFlows into a single executable pipeline, with conditional branches (`when`), loops (`forEach`), scoped
  bindings (`scope`), step-level and global exception handlers (`onException`), and async steps
  (`asyncRun` / `await` / `awaitAll` / `awaitAny`). See [Writing RuleFlows](#writing-ruleflows).
- **`RuleExecutionStatus.ERROR`** — rule results now distinguish execution errors from PASS/FAIL/SKIPPED.
- **Correctness & hardening pass** — a systematic review of every package fixed thread-safety issues on the
  rule-build hot path, immutability leaks in `Bindings`/`RuleContext`, numeric coercion in validation rules
  (decimal strings no longer falsely fail `min`/`max`), inverted `RuleSetExecutionStatus.isAnyPass()`/`isAnySkip()`/`isAnyFail()`,
  text-converter parsing bugs (leading zeros parsed as octal), and tracer listener isolation — a throwing
  listener can no longer alter a rule's outcome.

Full details in the [changelog](change-log.md).

---

## Getting Started

**Maven**
```xml
<dependency>
    <groupId>org.rulii</groupId>
    <artifactId>rulii</artifactId>
    <version>2.0.0</version>
</dependency>
```

**Gradle**
```groovy
implementation 'org.rulii:rulii:2.0.0'
```

---

## Writing Rules

Rules can be written declaratively (annotation-based) or functionally (lambda-based). Both styles are fully supported.

**[More examples here](https://github.com/algox/rulii-samples)**

### Declaratively

```java
@Rule
@Description("Validates that fromDate is before toDate.")
public class ConsistentDateRule {

    @PreCondition
    public boolean check(LocalDate fromDate, LocalDate toDate) {
        return fromDate != null && toDate != null;
    }

    @Given
    public boolean isValid(LocalDate fromDate, LocalDate toDate) {
        return fromDate.isBefore(toDate);
    }

    @Otherwise
    public void otherwise(LocalDate fromDate, LocalDate toDate, RuleViolations violations) {
        violations.add(RuleViolation.builder().build("consistentDateRule", "errorCode.100",
                "fromDate [" + fromDate + "] must be before toDate [" + toDate + "]"));
    }
}

Rule rule = Rule.builder().build(ConsistentDateRule.class);
```

### Functionally

```java
Rule rule = Rule.builder()
        .name("consistentDateRule")
        .description("Validates that fromDate is before toDate.")
        .preCondition(condition((LocalDate fromDate, LocalDate toDate) -> fromDate != null && toDate != null))
        .given(condition((LocalDate fromDate, LocalDate toDate) -> fromDate.isBefore(toDate)))
        .otherwise(action((LocalDate fromDate, LocalDate toDate, RuleViolations violations) ->
                violations.add(RuleViolation.builder().build("consistentDateRule", "errorCode.100",
                        "fromDate [" + fromDate + "] must be before toDate [" + toDate + "]"))))
        .build();
```

### Running a Rule

```java
Bindings bindings = Bindings.builder().standard();
bindings.bind("fromDate", LocalDate.of(1980, Month.JANUARY, 1));
bindings.bind("toDate", LocalDate.now());
bindings.bind("violations", new RuleViolations());

RuleResult result = rule.run(bindings);

if (result.status().isPass()) {
    // Rule passed
} else {
    // Rule failed
}
```

---

## Writing RuleSets

Group related rules into a `RuleSet` for reuse and clean execution:

```java
import static org.rulii.validation.rules.Validators.*;

RuleSet<RuleViolations> ruleSet = RuleSet.builder()
        .with("userValidation")
        .rule(notNull(binding("username")).build())
        .rule(notBlank(binding("username")).build())
        .rule(size(binding("username"), 3, 50).build())
        .rule(email(binding("email")).build())
        .rule(min(binding("age"), 18L).message("Must be 18 or older").build())
        .rule(Rule.builder().build(ConsistentDateRule.class))
        .resultExtractor(function((RuleViolations violations) -> violations))
        .build();

Bindings bindings = Bindings.builder().standard();
bindings.bind("username", "alice");
bindings.bind("email", "alice@example.com");
bindings.bind("age", 25);
bindings.bind("fromDate", LocalDate.of(1999, Month.JANUARY, 1));
bindings.bind("toDate", LocalDate.now());
bindings.bind("violations", new RuleViolations());

RuleViolations violations = ruleSet.run(bindings);

if (violations.hasErrors()) {
    throw new ValidationException(violations);
}
```

---

## Writing RuleFlows

**New in 2.0.0** — a `RuleFlow` orchestrates Rules, RuleSets, and other RuleFlows into a single executable
pipeline. Flows support conditional branches, loops, scoped bindings, exception handling, and a returned result:

```java
RuleFlow<BigDecimal> flow = RuleFlow.builder()
        .name("orderFlow")
        .bind(price -> BigDecimal.ZERO)
        .run(userValidationRuleSet, spec -> spec.as("violations"))
        .when(condition((RuleViolations violations) -> !violations.hasErrors()), body -> body
                .run(pricingRuleSet, spec -> spec.as("price")))
        .forEach(function((Order order) -> order.getItems()), "item", body -> body
                .run(itemDiscountRule))
        .onException(UnrulyException.class, handler -> handler
                .bind(price -> BigDecimal.ZERO))
        .<BigDecimal>returning(function((BigDecimal price) -> price))
        .build();

BigDecimal price = flow.run(bindings);
```

Steps can be configured individually — bind the result to a name (`as`), pass step-scoped parameters (`with`),
or attach a step-level exception handler (`onException`):

```java
.run(rule, spec -> spec.as("result")
                       .with(threshold -> 10)
                       .onException(UnrulyException.class, b -> b.bind(result -> false)))
```

Long-running steps can run concurrently with `asyncRun` — results are bound as `CompletableFuture`s and
awaited with `await` / `awaitAll` / `awaitAny`:

```java
RuleFlow<?> flow = RuleFlow.builder()
        .name("parallelChecks")
        .asyncRun(creditCheckRuleSet, spec -> spec.as("creditFuture"))
        .asyncRun(fraudCheckRuleSet, spec -> spec.as("fraudFuture"))
        .awaitAll("creditFuture", "fraudFuture")
        .build();
```

See the [changelog](change-log.md) for the full RuleFlow feature list.

---

## Built-in Validators

rulii ships with **34 built-in validation rules** accessible via the `Validators` static factory.
Use `Validators.binding("name")` to reference a named binding, or `Validators.value(obj)` for a constant.

```java
import static org.rulii.validation.rules.Validators.*;

// String validators
alpha(binding("code")).build()
alphaNumeric(binding("username")).build()
notBlank(binding("name")).build()
size(binding("bio"), 0, 500).build()
email(binding("email")).build()
pattern(binding("zip"), "\\d{5}").build()
upperCase(binding("countryCode")).build()

// Numeric validators
min(binding("age"), 18L).message("Must be 18 or older").build()
max(binding("quantity"), 100L).build()
positive(binding("price")).build()
digits(binding("score"), 3, 2).build()   // max 3 integer digits, 2 fractional

// Date/time validators
past(binding("birthDate")).build()
future(binding("expiryDate")).build()
futureOrPresent(binding("startDate")).build()

// Collection validators
notEmpty(binding("items")).build()
size(binding("tags"), 1, 10).build()
in(binding("status"), List.of("ACTIVE", "PENDING", "CLOSED")).build()

// Null / equality validators
notNull(binding("id")).build()
assertEquals(binding("confirmPassword"), binding("password")).build()
```

Full validator reference:

| Method | Description |
|---|---|
| `alpha(fn)` | Only alphabetic characters |
| `alphaNumeric(fn)` | Only alphanumeric characters |
| `ascii(fn)` | Only ASCII characters |
| `assertTrue(fn)` | Must be `true` |
| `assertFalse(fn)` | Must be `false` |
| `assertEquals(fn, expected)` | Must equal expected value |
| `assertNotEquals(fn, unexpected)` | Must not equal value |
| `blank(fn)` | Must be blank (null or whitespace) |
| `decimal(fn)` | Must be a valid decimal number |
| `digits(fn, maxInt, maxFrac)` | Max integer and fractional digit counts |
| `email(fn)` | Must be a valid email address |
| `endsWith(fn, suffixes...)` | Must end with one of the given suffixes |
| `fileExists(fn)` | Must be a path to an existing file |
| `future(fn)` | Date/time must be in the future |
| `futureOrPresent(fn)` | Date/time must be in the future or present |
| `in(fn, collection)` | Must be contained in the collection |
| `lowerCase(fn)` | Must be all lower-case |
| `max(fn, max)` | Numeric value must be ≤ max |
| `decimalMax(fn, max)` | Decimal value must be ≤ max |
| `min(fn, min)` | Numeric value must be ≥ min |
| `decimalMin(fn, min)` | Decimal value must be ≥ min |
| `negative(fn)` | Must be strictly negative |
| `negativeOrZero(fn)` | Must be negative or zero |
| `notBlank(fn)` | Must not be blank |
| `notEmpty(fn)` | Collection/String/Array must not be empty |
| `notNull(fn)` | Must not be null |
| `isNull(fn)` | Must be null |
| `numeric(fn)` | Must be a numeric string |
| `past(fn)` | Date/time must be in the past |
| `pastOrPresent(fn)` | Date/time must be in the past or present |
| `pattern(fn, regex)` | Must match the given regex |
| `positive(fn)` | Must be strictly positive |
| `positiveOrZero(fn)` | Must be positive or zero |
| `size(fn, min, max)` | Size must be within [min, max] |
| `startsWith(fn, prefixes...)` | Must start with one of the given prefixes |
| `upperCase(fn)` | Must be all upper-case |
| `url(fn)` | Must be a valid URL |

---

## Scripting Support

As of **1.2.0**, rules can be backed by scripts via any JSR-223 compatible engine (JavaScript, Groovy, etc.).

```java
// Build a script
Script<Boolean> script = Script.builder()
        .with("js", "age >= 18")
        .param("age", Integer.class)
        .build();

// Wrap it in a Condition
Condition condition = Condition.builder().build(script);

// Run it
Bindings bindings = Bindings.builder().standard();
bindings.bind("age", 21);

RuleContext ctx = RuleContext.builder().build(bindings);
boolean result = condition.isTrue(age -> 21);  // true
```

Scripts have full access to all named bindings and integrate seamlessly with Rules and RuleSets.

---

## Spring Integration

[rulii-spring](https://github.com/algox/rulii-spring) brings rulii into the Spring ecosystem:

- Auto-configuration of rulii options
- Automatic rule discovery via `@RuleScan`
- Spring-managed beans injected directly into rules
- Externalize rule messages via `application.yaml` / `application.properties`
- Default parameter values using Spring's conversion system

**Maven**
```xml
<dependency>
    <groupId>org.rulii</groupId>
    <artifactId>rulii-spring</artifactId>
    <version>1.2.0</version>
</dependency>
```

**Gradle**
```groovy
implementation 'org.rulii:rulii-spring:1.2.0'
```

```java
@Configuration
@RuleScan(scanBasePackages = "com.example.rules")
public class RuleConfig {

    @Bean
    public RuleSet<?> validationRules(RuleRegistry ruleRegistry) {
        return RuleSet.builder()
                .with("validationRules")
                .rule(ruleRegistry.getRule(ConsistentDateRule.class))
                .build();
    }
}
```

See the [rulii-spring repository](https://github.com/algox/rulii-spring) and the
[Spring Boot sample](https://github.com/algox/rulii-samples/tree/develop/spring-boot-sample) for full details.

---

## Claude Code Skills

This project ships with [Claude Code](https://claude.ai/code) skills that assist with common development tasks.
If you have Claude Code installed, invoke any skill with its slash command from within the project directory.

| Skill | Command | What it does |
|---|---|---|
| New Rule | `/new-rule` | Step-by-step guide for creating a Rule — lambda and class-based styles, conditions, actions, preConditions, and running rules manually |
| New RuleSet | `/new-ruleset` | Full RuleSet builder API — lifecycle hooks, input params, stop conditions, `.validating()` mode, async execution, and error handling |
| New RuleFlow | `/new-ruleflow` | Full RuleFlow pipeline API — bind/run/apply/execute steps, step specs (`as`/`with`/`onException`), `when`/`forEach`/`scope` containers, early exit, async steps (`asyncRun`/`await`), exception handling, and custom containers |
| New Validation Rule | `/new-validation-rule` | Creates a custom `ValueValidationRule` with its companion builder — covers supported types, `isValid()` logic, violation customisation, and the full checklist |
| Write Test | `/write-test` | JUnit 5 test patterns for rulii — PASS/FAIL/SKIP scenarios, missing-binding cases, validation exceptions, and JUnit 5.12.1 ambiguity workarounds |
| Debug Rule | `/debug-rule` | Diagnostic guide for rules that produce the wrong result — walks through SKIP (type mismatch), missing bindings, missing violations, and how to enable tracing |

### Using the skills in your own project

Since you'll typically reference rulii as a dependency rather than working in this repo directly,
copy the skills into your own project's `.claude/skills/` directory:

```bash
cp -r <path-to-rulii>/.claude/skills/new-rule        .claude/skills/
cp -r <path-to-rulii>/.claude/skills/new-ruleset      .claude/skills/
cp -r <path-to-rulii>/.claude/skills/new-ruleflow     .claude/skills/
cp -r <path-to-rulii>/.claude/skills/new-validation-rule .claude/skills/
cp -r <path-to-rulii>/.claude/skills/write-test       .claude/skills/
cp -r <path-to-rulii>/.claude/skills/debug-rule       .claude/skills/
```

Once copied, run `claude` from your project root — skills in `.claude/skills/` are discovered automatically.

### Prerequisites

Install Claude Code:
```bash
npm install -g @anthropic-ai/claude-code
```

---

## Documentation

**[Full documentation at rulii.com](https://rulii.com)**

- [Getting Started](https://rulii.com/introduction)
- [What's New](https://rulii.com/whats-new)
- [Spring integration docs](https://rulii.com/spring/introduction)
- [Javadoc](https://javadoc.io/doc/org.rulii/rulii/latest)
- [Sample projects](https://github.com/algox/rulii-samples)

| Section | Pages |
|---|---|
| **Getting Started** | [Introduction](https://rulii.com/introduction) · [Installation](https://rulii.com/installation) · [Core Concepts](https://rulii.com/core-concepts) · [What's New](https://rulii.com/whats-new) |
| **Core** | [Bindings](https://rulii.com/bindings) · [Rules (Functional)](https://rulii.com/lambda-rules) · [Rules (Declarative)](https://rulii.com/annotation-rules) · [RuleSets](https://rulii.com/rulesets) |
| **Validation** | [Built-in Validators](https://rulii.com/validation-rules) · [RuleViolations](https://rulii.com/rule-violations) · [Custom Validation](https://rulii.com/custom-validation) |
| **Advanced** | [Condition Composition](https://rulii.com/condition-composition) · [Scripting](https://rulii.com/scripting) · [Async Execution](https://rulii.com/async-execution) · [Rule Tracing](https://rulii.com/rule-tracing) |
| **Spring** | [Spring Introduction](https://rulii.com/spring/introduction) · [Auto-Configuration](https://rulii.com/spring/auto-configuration) · [Rule Scanning](https://rulii.com/spring/rule-scanning) |

The Markdown sources for all documentation pages are in the [`docs/`](docs/) folder — see the
[documentation index](docs/README.md) for the full page list.

---

## Contributing

Contributions are welcome! Please open an issue first to discuss what you'd like to change.

1. Fork the repo
2. Create a feature branch (`git checkout -b feature/my-feature`)
3. Commit your changes
4. Push and open a pull request against `develop`

All PRs must pass the full test suite (`mvn test`) before review.

---

_Licensed under the [Apache 2.0 License]._
