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
[![codecov](https://codecov.io/gh/algox/rulii/branch/develop/graph/badge.svg)](https://codecov.io/gh/algox/rulii)

---

**📖 Official documentation at [rulii.com](https://rulii.com) or the mirror [here](https://rulii.netlify.app/)**

**🧪 Sample projects at [rulii-samples](https://github.com/algox/rulii-samples)**

---

## Table of Contents

- [What is it?](#what-is-it)
- [Why rulii?](#why-rulii)
- [What's New in 2.2.0](#whats-new-in-220)
- [What's New in 2.1.0](#whats-new-in-210)
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

Business rules are the code that changes most, and they usually end up buried in the business tier, where a small
change has a large blast radius. rulii gives them a place of their own.

- **Rules are first-class citizens.** Each rule is isolated, so it can be found, changed and tested on its own.
- **Plain Java.** A rule is a class or a few lambdas. The compiler checks it, the debugger steps through it and
  JUnit tests it. There is no rule language to learn and no server to run.
- **Predictable.** Rules run in the order you give them. No inference engine decides what fires next.
- **One engine for rules, flows and validation.** Rule sets, rule flows and Bean Validation constraints all run on
  the same engine.
- **Visible.** [rulii-explorer](https://github.com/algox/rulii-explorer) shows every rule, rule set and flow of a
  running application in plain English, with its flowchart.

rulii does not do inference and has no decision tables. If you need those, Drools is the right tool.

The full story, and how rulii compares with plain Java, Drools, Easy Rules and Hibernate Validator, is on the
[Why rulii](https://rulii.org/why/) page.

---

## What's New in 2.2.0

- **Result type** — a rule set's and a rule flow's definition carry a formal result type, resolved at `build()`:
  state it with `resultExtractor(Price.class, fn)` / `returning(Price.class, fn)` or `resultType(Type)`, or let
  rulii derive it from a typed lambda. A rule set without an extractor reports `RuleSetExecutionStatus`; a
  script or generic lambda reports unknown rather than `Object`. `getResolvedResultType()` says where the type
  came from (`DECLARED`, `DERIVED`, `DEFAULT`, `UNKNOWN`, `NONE`). A stated type that the extractor cannot
  produce fails the build. See [Writing RuleSets](#writing-rulesets) and [Writing RuleFlows](#writing-ruleflows).
- **Local bindings** — a `@Rule` class method that declares a `LocalBindings` parameter gets a fresh instance per
  run, shared by the pre-condition, condition and actions of that run and invisible to the caller.
- **Forked contexts** — `RuleContext.fork()` and `ScopedBindings.fork()` give concurrent work a context that
  shares the caller's scopes as they stand but keeps everything it adds private. Async flow steps and `runAsync`
  use it; `AsyncContextMode.INHERITED` is the new default and the racy `SHARED` mode is gone.
- **Jakarta Expression Language** — a scripting language registered as `jakarta-el` (`${ctx.age >= 18}`), and a
  process-wide default scripting language (`ScriptProcessorManager.setDefaultLanguage` or the
  `rulii.script.defaultLanguage` system property) for scripts that name none. See
  [Scripting Support](#scripting-support).
- **Eleven more validators** — `uuid`, `isbn`, `ean`, `luhnCheck`, `mod10Check`, `mod11Check`, `uniqueElements`,
  `normalized`, `codePointLength`, `durationMin`, `durationMax`; `url` and `email` gain pattern and part options,
  the temporal rules accept the `java.time.chrono` calendars, and every value rule can `describe()` itself
  (`code size must be between 3 and 20`). See [Built-in Validators](#built-in-validators).
- **Registry by category and tags** — `RuleRegistry.getByCategory("Pricing")` (the category and everything
  below it), `getByTags("vip")`, `getCategories()` and `getTags()`.
- **Performance** — cheaper bindings and contexts: ids and scope names no longer draw a random UUID each, the
  binding name is validated once, and the validation rules find their context by name before type.
- **For rulii-bean-validation** — `SourceDefinition` can name a field, `RuleViolation.getParameterValues()`
  keeps the raw values, and validation rule builders take extra violation parameters.

Full details in the [changelog](change-log.md).

---

## What's New in 2.1.0

- **Category and tags** — a rule, rule set or rule flow can say where it belongs and what it is about:
  `@Category("Pricing/Discounts")` and `@Tags({"vip", "discount"})` on `@Rule` classes, `category(...)` and
  `tags(...)` on every builder. One hierarchical category (`/` separates the levels), any number of tags.
  Purely descriptive: rulii never reads them when it runs; tools such as rulii-explorer group and filter by them.
  ```java
  @Rule @Category("Orders/Validation") @Tags({"orders", "dates"})
  public class ConsistentDateRule { … }

  Rule.builder().name("fraudScoreRule").category("Risk").tags("risk", "fraud")…
  ```
- **Introspection** — everything a rule, rule set or rule flow is made of can be read off the built object:
  `Condition` / `Action` / `Function` expose an `ExpressionInfo` (`getExpression()`: script language and
  text, compiled method, or composite operator and operands), `RuleFlowCommand.getInfo()` describes each flow
  step (`Run`, `When`, `ForEach`, `Scope`, `AsyncRun`, `Await`, …), `RuleFlowDefinition` carries the command
  tree, `RuleSetDefinition` its input parameters and error handler, and `SourceDefinition` records where an
  artifact was declared (the XML file and line with rulii-spring, the rule class, or the builder call site).
  `Script.getSourceText()` returns the script as written, before `${...}` placeholder resolution. This is the
  foundation for [rulii-explorer](https://github.com/algox/rulii-explorer), which browses, searches and
  visualises the rules of a running application.
- **Scripting (GraalJS)** — Java bean properties can be read directly in JavaScript (`ctx.person.name` instead
  of `ctx.person.getName()`; assignment calls the setter); a polyglot context leak under sustained evaluation
  is fixed, with JavaScript arrays and objects now crossing into Java as detached `List`s and `Map`s; every
  name GraalJS registers (`js`, `JavaScript`, `ECMAScript`, …) resolves to the configured factory; and
  `ScriptProcessorManager.unregister(factory)` removes a factory again.
- **Memory** — the definition caches behind lambda-built conditions, actions, functions and rules no longer
  grow without bound.
- **Breaking** — custom `RuleRegistry` implementations must implement `getNames()`;
  `RuleSetDefinition.getStopActionDefinition()` is deprecated for `getStopConditionDefinition()`.

Full details in the [changelog](change-log.md).

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
    <version>2.2.0</version>
</dependency>
```

**Gradle**
```groovy
implementation 'org.rulii:rulii:2.2.0'
```

**Using more than one rulii library?** Import the [rulii-bom](https://github.com/algox/rulii-bom) once and
drop the versions. It pins a matching set of rulii, rulii-spring, rulii-bean-validation and rulii-explorer:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.rulii</groupId>
            <artifactId>rulii-bom</artifactId>
            <version>2.2.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
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
        .resultExtractor(RuleViolations.class, function((RuleViolations violations) -> violations))
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

The result type is part of the rule set's definition (`getDefinition().getResultType()`), next to its input
parameters. State it with `resultExtractor(Class, Function)` or `resultType(Type)`; when you don't, it is derived
from the extractor's declared return type, a rule set without an extractor returns its `RuleSetExecutionStatus`,
and a script or generic lambda leaves it unknown. The type is a declaration, not a cast: nothing is converted at
run time, but a stated type that contradicts the extractor fails at `build()`.

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
        .returning(BigDecimal.class, function((BigDecimal price) -> price))
        .build();

BigDecimal price = flow.run(bindings);
```

As for a rule set, the result type is part of the flow's definition: `returning(Class, Function)` states it,
`resultType(Type)` states it on its own (for a script extractor, or a flow whose result comes from `exit` steps),
and `returning(Function)` derives it from the extractor. A flow without `returning` returns its `RuleContext` and
has no result type. A stated type that contradicts the extractor or an `exit` extractor fails at `build()`.

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

rulii ships with **48 built-in validation rules** accessible via the `Validators` static factory.
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
| `codePointLength(fn, min, max)` | Number of Unicode code points within [min, max] (2.2) |
| `decimal(fn)` | Must be a valid decimal number |
| `digits(fn, maxInt, maxFrac)` | Max integer and fractional digit counts |
| `durationMin(fn, min)` | `Duration` must be ≥ min (2.2) |
| `durationMax(fn, max)` | `Duration` must be ≤ max (2.2) |
| `ean(fn)` | Must be an EAN-13 or EAN-8 barcode number (2.2) |
| `email(fn)` | Must be a valid email address |
| `endsWith(fn, suffixes...)` | Must end with one of the given suffixes |
| `fileExists(fn)` | Must be a path to an existing file |
| `future(fn)` | Date/time must be in the future |
| `futureOrPresent(fn)` | Date/time must be in the future or present |
| `in(fn, collection)` | Must be contained in the collection |
| `isbn(fn)` | Must be an ISBN-10 or ISBN-13 (2.2) |
| `lowerCase(fn)` | Must be all lower-case |
| `luhnCheck(fn)` | Digits must pass the Luhn check (2.2) |
| `max(fn, max)` | Numeric value must be ≤ max |
| `decimalMax(fn, max)` | Decimal value must be ≤ max |
| `min(fn, min)` | Numeric value must be ≥ min |
| `decimalMin(fn, min)` | Decimal value must be ≥ min |
| `mod10Check(fn)` | Digits must pass a modulo 10 check (2.2) |
| `mod11Check(fn)` | Digits must pass a modulo 11 check (2.2) |
| `negative(fn)` | Must be strictly negative |
| `negativeOrZero(fn)` | Must be negative or zero |
| `normalized(fn)` | Must be in a Unicode normalization form, NFC by default (2.2) |
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
| `uniqueElements(fn)` | Collection must hold no element twice (2.2) |
| `upperCase(fn)` | Must be all upper-case |
| `url(fn)` | Must be a valid URL; `protocol`, `host`, `port` and `regexp` options since 2.2 |
| `uuid(fn)` | Must be a UUID in its canonical form (2.2) |

Every value rule can say what it checks: `Validators.size(binding("code"), 3, 20).describe()` returns
`code size must be between 3 and 20`, and a rule built without a description uses that text.

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
The bindings are exposed to the script as `ctx` (configurable through `ScriptOptions`).

With GraalJS, Java bean properties read as plain properties since **2.1.0**: `ctx.person.name` instead of
`ctx.person.getName()`, `ctx.person.active` for `isActive()`, and assignment calls the setter.
`Script.getSourceText()` returns the text as written, before any `${...}` placeholders were resolved;
`getScript()` is the text the compiler saw.

**Jakarta Expression Language** is a scripting language since **2.2.0**, registered as `jakarta-el` (alias
`jel`) when the EL API and an implementation such as Expressly or Tomcat's `tomcat-embed-el` are on the
classpath. Scripts use EL's own delimiters, and bindings are reachable through `ctx` or as bare identifiers:

```java
Script<Boolean> script = Script.builder().with("jakarta-el", "${ctx.age >= 18}").build();
```

The plain `el` name stays what rulii-spring registers under it, Spring Expression Language, so XML rules
declared with `language="el"` keep their meaning.

**Default language.** A script that names no language is compiled in the process-wide default, declared with
`ScriptProcessorManager.setDefaultLanguage("js")` or the system property `rulii.script.defaultLanguage`; with
neither set, `Script.builder().build("age >= 18")` fails naming both. `ScriptProcessorManager.getAvailableLanguages()`
lists what can be compiled right now.

---

## Spring Integration

[rulii-spring](https://github.com/algox/rulii-spring) brings rulii into the Spring ecosystem:

- Auto-configuration of rulii options
- Automatic rule discovery via `@RuleScan`
- Spring-managed beans injected directly into rules
- Externalize rule messages via `application.yaml` / `application.properties`
- Default parameter values using Spring's conversion system
- Rules, RuleSets and RuleFlows declared in XML, with SpEL, JavaScript or Java expressions, `${property:default}`
  placeholders, and `category` / `tags` attributes

[rulii-explorer](https://github.com/algox/rulii-explorer) adds a read-only Actuator endpoint and a browser UI that
browses, searches and visualises the rules, rule sets and rule flows of a running Spring Boot application.

**Maven**
```xml
<dependency>
    <groupId>org.rulii</groupId>
    <artifactId>rulii-spring</artifactId>
    <version>2.2.0</version>
</dependency>
```

**Gradle**
```groovy
implementation 'org.rulii:rulii-spring:2.2.0'
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
