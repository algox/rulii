# Changelog

## [2.1.0] (unreleased)

### Introspection: rules, rule sets and rule flows describe themselves

Everything a rule, rule set or rule flow is made of can now be read off the built object without running it. This is the foundation for rulii-explorer.

#### Expressions (`org.rulii.model.ExpressionInfo`)
- `Condition`, `Action` and `Function` gain `getExpression()`, returning an `ExpressionInfo` alongside the existing `getDefinition()`. Kinds: `SCRIPT` (language + source text), `COMPILED` (the `MethodDefinition`, or null when not introspectable) and `COMPOSITE` (operator + operands, for `and`/`or`/`xor`/`not`, chained actions and composed functions).
- Script-backed runnables are now `ScriptCondition`, `ScriptAction` and `ScriptFunction` (subclasses of the defaults) and keep their `Script`.
- `Script.getSourceText()` returns the text as written, before `${...}` placeholder resolution; `getScript()` is still the resolved text the compiler saw. `ScriptBuilder` stamps the source text after compilation, so `ScriptCompiler` implementations are unchanged.
- `ChainedAction`, `ComposeWithBeforeFunction` and `ComposeWithAfterFunction` expose their parts (including the result binding name).

#### Rule flow commands (`org.rulii.ruleflow.info.CommandInfo`)
- `RuleFlowCommand.getInfo()` describes a command: `Run`, `Apply`, `Execute`, `AsyncRun`, `Await`, `Bind`, `When`, `ForEach`, `Scope`, `Exit` or `Custom`. Container bodies, handlers and continuations are included. A custom command can override it to report real structure.
- `Bind` records the names and types a step binds, never the values. `BindCommand` gains a constructor taking a `Supplier<CommandInfo.Bind>` for commands built outside the builder. Builder subclasses get a protected `bindWith(binder, info)` hook for bind steps the built-in overloads can't describe.
- `WhenCommand`, `ForEachCommand` and `ReturningCommand` expose their condition, source, stop condition and result extractor. `RuleFlowExceptionHandler.getBody()` is public and `getInfo()` returns a `Handler`.

#### Definitions
- `RuleFlowDefinition` carries the command tree (`getCommands()`), the global handler, and the finalizer and returning expressions. `getContextLabel()` names the context configurator (`context(Consumer, String label)` sets the label; the class name is the default; null without one). `getResultType()` is the explicit type from the new `returning(Class<T>, Function<T>)` overload, else the extractor's declared return type, else `Object`.
- `RuleSetDefinition` carries the input parameters and the error handler. `isValidating()` says whether `validating()` was called. `getStopConditionDefinition()` replaces the misnamed `getStopActionDefinition()`.
- `InputParameter` gains a `description`; the rule set and rule flow builders gain `param(name, type, required, description)` and `param(name, type, defaultValue, description)`.
- `SourceDefinition.build()` now records the caller's location. It used to skip the pre-2.0 `org.algorithmx.` package instead of `org.rulii.`, so every location pointed at `SourceDefinition.build` itself. Rulii frames are now recognised by code location (the rulii jar) rather than by package prefix, so application code in `org.rulii.*` packages is recorded too.
- `SuppliedValidationRule.getCondition()` exposes the supplied condition.
- `ValidationRuleBuilder.build()` no longer drops the description set on the builder.
- Builders accept an explicit source: `source(SourceDefinition)` on the rule, validation rule, value validation rule, rule set and rule flow builders, for loaders that know where an artifact was declared (rulii-spring records the XML file and line). `SourceDefinition.forFile(fileName, line)` and `forClass(type)` create them. Class-based rules now record their rule class as the source instead of the code that called the builder.

#### Registry and validation
- `RuleRegistry.getNames()` returns the keys every runnable can be looked up by. `getRuleFlows()` now has a real default derived from it instead of returning an empty list.
- `ValueValidationRule.getValueName()` is public.

### Scripting (GraalJS)
- **Polyglot context leak fixed.** GraalJS backs every `ScriptEngine.createBindings()` call with a polyglot `Context` that is reclaimed only by an explicit close. `JSR223ScriptProcessor` created one per evaluation and never closed it, and `GraalJsScriptProcessorFactory.getScriptCompiler()` left one more behind per compiled script. Under sustained evaluation this exhausted the heap. The processor now releases the context after each evaluation (`releaseContext`) whenever the result does not depend on it (`isContextReleasable`). So that it almost always can, the GraalJS host access policy (`GraalJsHostAccess.DETACHING`) now detaches JavaScript values as they cross into Java: arrays, `Set`s and iterables become `ArrayList`s, object literals and `Map`s become `LinkedHashMap`s, `Date`s become `Instant`s. This applies to evaluation results, to values a script writes into the bindings (`ctx.out = [1, 2]`) and to arguments passed to Java methods and setters whose parameter is typed `Object`, `List` or `Collection` (a `Map`-typed parameter still receives a live proxy); previously all of these were live proxies. Functions cannot be detached, so a script returning one keeps its context open. The new `GraalJsScriptProcessor` asks GraalJS whether a result is host or guest; the generic processor's default releases only for `null`, `String`, `Number`, `Boolean` and `Character` results. The new `GraalJsScriptCompiler` closes its throwaway engine after compiling. The factory now returns these two classes; both remain `JSR223ScriptProcessor` / `ScriptCompiler` instances. Behaviour change to note: a value written into the bindings is a Java copy, so reading it back within the same script gives a Java `List`/`Map` (`ctx.out.add(4)`, not `ctx.out.push(4)`).
- **Factory aliases.** `ScriptProcessorFactory.getAliases()` (default: none) lets a factory declare alternate names; `ScriptProcessorManager` registers them too, without displacing a factory explicitly registered under that name. `GraalJsScriptProcessorFactory` reports every name GraalJS itself registers with JSR-223 (`JS`, `JavaScript`, `javascript`, `ECMAScript`, `graal.js`, `GraalJS`, ...), taken from `GraalJSEngineFactory.getNames()`, so all of them now resolve to the configured factory. Previously anything but `js` fell through to the generic JSR-223 wrapper around a bare GraalJS engine, which had none of the factory's configuration and leaked a context per compiled script. `JSR223ScriptProcessorFactory` reports its engine's names likewise, and the manager's JSR-223 fall-back registers them, so every spelling of an engine's name shares one wrapper.
- **Nashorn compatibility mode on by default.** The shared GraalJS engine sets `js.nashorn-compat=true` (with `allowExperimentalOptions(true)`, as GraalJS flags the option experimental). Scripts can read Java bean properties directly: `ctx.person.name` instead of `ctx.person.getName()`, `ctx.person.active` for `isActive()`, and assignment calls the setter. Previously such reads silently evaluated to `null`. The explicit ES2022 setting still applies. The mode also enables the Nashorn `Java.*` helpers, `JavaImporter`, and the `exit()` / `quit()` globals, which end the script with an exception rather than the JVM.

### Model
- **Definition cache leak fixed.** `MethodDefinition`, `ParameterDefinition` and `ReturnTypeDefinition` cached their results by `Method` identity, but reflection returns a fresh `Method` object on every lookup, so every condition, action, function or rule built from a lambda (or any other non-class functional object) added entries that were never released. Under sustained building this grew the heap without bound. The caches now key by `Method` value (plus the `containsGenericInfo` flag, which the old key ignored on a hit) and are bounded by the number of distinct methods. Class-based rules were unaffected, as they already resolved to the same `Method` instances.

### Breaking changes
- `RuleRegistry.getNames()` is abstract: custom registries must implement it.
- `ContainerCommand.getBody()` is public (was protected): subclasses that override it must widen it too.
- The `RuleFlowDefinition` and `RuleSetDefinition` constructors take the new fields. Only the builders call them.
- `RuleSetDefinition.getStopActionDefinition()` is deprecated for removal; use `getStopConditionDefinition()`.

## [2.0.0]

### New Feature: RuleFlow (`org.rulii.ruleflow`)

A fluent, pipeline-style orchestration API for composing Rules, RuleSets, and other RuleFlows into a single executable flow — reducing the boilerplate of wiring bindings, scopes, conditionals, and error handling by hand.

#### Builder API
```java
RuleFlow<Boolean> flow = RuleFlow.builder()
    .name("myFlow")
    .bind(x -> 42)
    .run(rule, spec -> spec.as("result").with(y -> 10)
                           .onException(UnrulyException.class, b -> b.bind(ok -> false)))
    .when(condition, b -> b.run(rule2))
    .onException(Exception.class, b -> b.bind(handled -> true))
    .<Boolean>returning(function((Boolean handled) -> handled))
    .build();
```
- `RuleFlow.builder()` — entry point; all pipeline methods are fluent (CRTP self-type, no casting)
- **Steps**: `run(...)` / `apply(...)` / `execute(...)` — run a Rule, RuleSet, RuleFlow, Condition, Action, or Function, or look one up from the `RuleRegistry` by name or class
- **Step configuration** via `RunSpec` / `ExecuteSpec` Consumer lambdas:
  - `.as(bindingName)` / `.as(scopeName, bindingName)` — bind the step result
  - `.with(BindingDeclaration...)` — step-scoped parameters
  - `.onException(type, handlerBody)` — step-level exception handler
- **Containers** (Consumer-body): `when(condition, body)` / `when(condition, body, otherwiseBody)`, `forEach(listFn, "item", body)`, `scope("name", body)`
- **Other commands**: `bind(...)`, `exit(...)`, `returning(...)`, `context(...)` (must be the first step; validated eagerly)
- `ContainerCommand` is a public extension point for custom container commands

#### Exception handling
- `RuleFlowExceptionHandler` — step-level (`RunSpec.onException`) and flow-level/global (`builder.onException(...)`) handlers
- Handler body receives the exception bound as `"ex"`; a matched handler swallows the exception and execution continues with the next step
- Step handler is checked first, then the global handler; unmatched exceptions propagate

#### Async execution
- `asyncRun(...)` — launch a Rule / RuleSet / RuleFlow / registry lookup asynchronously; result bound as a `CompletableFuture<T>` via `.as(name)`
- `await(name)`, `awaitAll(names...)`, `awaitAny(names...)` — blocking waits with optional timeout (default 30s)
- `AsyncContextMode` — SHARED (default), IMMUTABLE (`withImmutableBindings()`), or CUSTOM (`withContext(ctx)`) context for the async task
- `AsyncRunSpec.thenRun(resultBindingName, body)` — continuation chained onto the async completion, without blocking the flow
- `AsyncRunSpec.onException(type, handlerBody)` — fires as soon as the async task (or its continuation) fails, whether or not the future is ever awaited
- New `org.rulii.model.AsyncRunnable` interface — `runAsync(RuleContext)` support for Rule / RuleSet / RuleFlow

#### Integration
- `RuleRegistry` — new `getRuleFlow(String)` and `getRuleFlows()` methods; RuleFlows are registrable like Rules and RuleSets
- Tracing — `RuleFlowListener` events fired through the `Tracer` for flow/command start, completion, and errors

---

### Correctness & Hardening Pass (all packages)

A systematic multi-angle code review was run over every main package; the notable, user-visible fixes are listed per package below. Test suite grew from 1039 to 1274 tests.

#### `org.rulii.model`
- `MethodDefinition` / `ParameterDefinition` no longer share a mutable cache across instances (could corrupt definitions under concurrent builds)
- `DefaultFunction` now executes through `AbstractRunnable.run()` like Condition/Action — consistent tracing and parameter-mutation semantics
- Composite conditions (`and` / `or`) now short-circuit
- `NotCondition` preserves the original exception type instead of re-wrapping
- `ChainedAction.getName()` typo fixed; `RunnableBuilder.loadLambda()` no longer silently swallows load failures

#### `org.rulii.bind`
- `ImmutableScopedBindings` now actually enforces immutability (including via `iterator()` and supplied bindings)
- `RuleContextBuilder` no longer crashes on shadowed scopes; `RuleContext.builder().with(existingContext)` no longer duplicates reserved bindings (`BindingException: Multiple matches found`)
- `ScopedBindings.get()` vs `entrySet()` shadowing behavior reconciled
- `DelegatingBinding` no longer bypasses type checks; `DefaultParameterResolver` no longer swallows `ConversionException`
- `contains(TypeReference)` / `contains(Class)` now agree; `SuppliedBinding` double-checked locking made thread-safe

#### `org.rulii.context`
- `RuleContext.asImmutable()` no longer leaks mutable binding values, and gets a fresh id/creation time
- Internal executor upgraded from an unbounded, never-shutdown pool to a bounded `ThreadPoolExecutor` with `CallerRunsPolicy` and a JVM shutdown hook
- `RuleContextOptions` gained `getTracer()`; `getScriptProcessor()` initialization race fixed

#### `org.rulii.convert`
- Numeric text converters no longer misparse leading-zero strings as octal (`"010"` → 10, not 8)
- `TextToBooleanConverter` no longer silently returns `false` for unrecognized input
- `TextToDateConverter` detects `+HHmm` zone offsets; `TextToCharsetConverter` handles `IllegalCharsetNameException`; UUID/Currency converters preserve the original exception cause; blank UUID input converts to `null`
- `ParameterDefinition.getDefaultValue()` no longer serves a stale cached value when called with a different `Converter`
- `ConverterRegistry.register()` override contract fixed

#### `org.rulii.registry`
- `register()` check-then-act race fixed; duplicate-name exception now reports the actual conflicting entry
- `getRulesInPackage()` no longer NPEs on classes without a package; empty-string names rejected

#### `org.rulii.script`
- `ScriptProcessorManager` made a true singleton (was static state behind a public constructor) with initialization race fixed
- Janino `ASTMutator` no longer silently corrupts bindings on nested field writes
- GraalJS processor caches the `Engine` instead of building a script engine per execution
- `JITScript` evaluator visibility race fixed; `autoReturn()` no longer mis-scans text blocks; `JSR223ScriptProcessorFactory` respects its `languageName` override; `ServiceLoader` discovery isolates per-provider failures

#### `org.rulii.text`
- Format patterns containing literal commas (e.g. `{0, number, #,##0.00}`) no longer break the parser
- `ParameterInfo.equals()` Integer reference-equality bug fixed
- `MessageResolver` missing-key behavior reconciled to the documented "returns null" contract
- Placeholder values are now placed by declared index; defensive copies added; template-parse caching added to `DefaultMessageFormatter`

#### `org.rulii.trace`
- A throwing listener can no longer alter a rule's outcome or abort execution — per-listener exceptions are now isolated across all execution strategies
- Listener collection is now thread-safe (`CopyOnWriteArraySet`); `removeListener(RuliiListener)` now returns `boolean`

#### `org.rulii.util`
- `ReflectionUtils.getPostConstructMethods()` operator-precedence bug fixed (could match wrong methods)
- `DefaultMethodResolver` now checks all parameters of multi-parameter methods, not just the first
- Static caches on the hot build path made thread-safe; `DefaultObjectFactory` no longer leaks cached instances across factory instances and now supports package-private no-arg constructors
- Exception wrapping reconciled between `MethodHandleMethodExecutor` and `ReflectiveMethodExecutor`; `RunnableComparator` and `TypeReference.equals()` contract violations fixed

#### `org.rulii.annotation`
- Annotation scanning no longer produces duplicate matches for inherited/overridden methods (`findMethods()` dedup)
- New `AnnotationConstants` class centralizes annotation default constants
- **Removed**: `Param.NoOpBindingMatchingStrategy` (unused)

#### `org.rulii.rule`
- `RuleExecutionStatus` gained **`ERROR`** (and `isError()`)
- `Rule.isTrue()` now goes through `RuleExecutionStrategy.isTrue()` — no longer bypasses the tracer
- `RuleDefinition.equals()` / `hashCode()` fixed for composite-condition NPEs and lambda-rule identity collisions
- `RulingClass` defensively copies then-actions; exception wrapping reduced from three layers to two
- **Removed**: dead `RuleUtils.validateName()`

#### `org.rulii.validation`
- Numeric validation rules (`max`, `min`, `decimalMax`, `decimalMin`, `positive`, `positiveOrZero`, `negative`, `negativeOrZero`) now coerce string values via `BigDecimal` instead of `Long` — decimal strings like `"10.5"` no longer falsely FAIL; coercion consolidated into `ValueValidationRule.toNumber()`
- `RuleViolations.hasErrors()` documented: counts `Severity.ERROR` only (by design)

#### `org.rulii.ruleset`
- `validating()` and `finalizer()` no longer silently overwrite each other — actions are now composed
- `RuleSetExecutionStatus.isAnyPass()` / `isAnySkip()` / `isAnyFail()` were logically inverted — fixed
- A finalizer exception no longer masks an already-propagating original exception
- Async execution documented: concurrent runs share one mutable `RuleContext` scope stack — supply separate contexts for parallel runs

---

### Integration-Driven Additions & Fixes

Additions and fixes driven by the rulii-spring 2.0 integration work:

#### API additions
- `BindingDeclaration.of(String name, T value)` — explicit-name factory for callers whose binding names are only known at runtime (configuration, XML); complements the lambda parameter-name form
- `ScriptProcessorManager.setScriptTextResolver(UnaryOperator<String>)` / `clearScriptTextResolver(expected)` / `resolveScriptText(String)` — a script-text pre-processing hook applied in `ScriptBuilderBuilder` before compilation (identity by default); integrations use it for e.g. Spring `${property:default}` placeholder resolution
- `DefaultConverterRegistry.registerDefaults()` made public — build with `registerDefaults=false`, register higher-precedence converters, then append the built-ins as fallbacks (`find()` is first-match)
- `RuleFlowBuilderTemplate.run(...)` / `asyncRun(...)` string-lookup parameter renamed `registryName` → `nameInRegistry`, matching the command internals

#### RuleFlow fixes
- `AsyncRunCommand` now binds the caller-visible future handle **before** launching the task — binding after launch could land the future inside the async child's transient scope (shared bindings) and vanish with it, making `await` fail with `NoSuchBindingException`
- `RunCommand` — a step's `as(...)` result no longer binds into the transient `with(...)` param scope (where it was destroyed with the scope, making `as()` + `with()` mutually exclusive in practice); the result destination is now captured before the param scope is pushed
- `DefaultConverterRegistry` no longer registers `TextToUrlConverter` twice

---

## [1.2.0]

### New Feature: Scripting Support (`org.rulii.script`)

#### Core Scripting API
- `Script<T>` — new interface (extends `Runnable<T>`) representing an executable script; exposes `getLanguageName()`, `getScript()`, `getScriptParameters()`
- `AbstractScript<T>` — base implementation storing language name, script text, and parameters
- `DefaultScript<T>` — concrete `Script` implementation
- `ScriptBuilder` / `ScriptBuilderBuilder` — fluent builder: `Script.builder().with(language, script).param(...).build()`
- `ScriptParameter` — typed, named parameter descriptor for a script
- `ScriptOptions` — configuration holder (e.g. `bindingsName`, defaults to `"ctx"`)

#### Processor Registry
- `ScriptProcessor` — interface: `getLanguageName()` + `evaluate(Script, RuleContext)`
- `ScriptProcessorRegistry` — interface: `register`, `deregister`, `getScriptProcessor(languageName)`
- `DefaultScriptProcessorRegistry` — `HashMap`-backed implementation; when `autoIncludeJSR223=true`, falls back to auto-discovering a JSR-223 engine by language name and caching it on first use

#### JSR-223 Bridge
- `JSR223ScriptProcessor` — evaluates scripts via `javax.script.ScriptEngine`; if the engine implements `Compilable`, compiles and caches scripts in a `WeakHashMap` for reuse; exposes the `RuleContext` bindings into the script scope via `bindingsName`

#### Exceptions
- `EvaluationException` — thrown when script evaluation fails at runtime
- `BuildScriptException` — thrown when script compilation fails

---

### `RuleContext` — ScriptProcessorRegistry Integration
- `RuleContext` now holds a `ScriptProcessorRegistry`
- `RuleContextBuilder`, `RuleContextOptions`, and `StandardRuleContextOptions` updated to wire it in at construction time

---

### `Bindings` API Improvements
- `setValue(String, T)` — return type changed from `void` to `T` (returns the previous value)
- `setValue(BindingDeclaration<T>)` — return type changed from `void` to `T`
- New `setValueOrBind(String, T)` — sets the value if the binding exists, otherwise creates a new binding; returns the previous value or `null`
- `asMap()` — return type tightened from `Map<String, ?>` to `Map<String, Object>`

---

### `Bindings` Now Implement `java.util.Map<String, Object>`

Applies to `DefaultBindings`, `DefaultScopedBindings`, `ImmutableBindings`, and `ImmutableScopedBindings`:
- `asMap()` now returns `this` directly instead of creating a copy
- Full `Map` contract implemented: `containsKey`, `containsValue`, `get`, `put`, `remove` (throws `UnsupportedOperationException`), `putAll`, `clear` (throws), `isEmpty`, `keySet`, `values`, `entrySet`
- `DefaultScopedBindings.iterator()` changed from `HashSet` to `LinkedHashSet` — insertion order is now preserved

---

### `Condition` / `Action` / `Function` Builders
- New `build(Script<T>)` overloads on `ConditionBuilderBuilder`, `ActionBuilderBuilder`, and `FunctionBuilderBuilder` — wraps a `Script` into a Condition, Action, or Function

---

### Validation Rules Refactoring (`org.rulii.validation`)

#### `ValueValidationRule`
- New abstract base class for all value-based validation rules
- Accepts a `Function<?>` (instead of a hard-coded binding name) to supply the value at execution time
- `@PreCondition checkType(RuleContext)` — returns `false` (result: **SKIP**) when the binding is absent (`NoSuchBindingException`) or the value type is not supported; does not throw

#### `ValueValidationRuleBuilder<T, V>`
- New abstract fluent builder base for all `ValueValidationRule` subclasses
- Fluent methods: `.errorCode(String)`, `.severity(Severity)`, `.message(String)`, `.valueName(String)`, `.name(String)`, `.description(String)`
- `.build()` — produces a fully configured `Rule` wrapping the validation rule
- When `valueFunction` is a `BindingFunction`, the binding name is automatically extracted and used as the `valueName` in rule violations

#### `BindingFunction<T>`
- New `Function<T>` wrapper that associates a **binding name** with a delegate function
- Used by `ValueValidationRuleBuilder` to auto-extract the parameter key for rule violations
- Constructed via `Validators.binding(String name)`

---

### `Validators` Static Factory (`org.rulii.validation.rules.Validators`)

New entry-point class with 34 static factory methods — one per built-in validation rule. Each method returns the rule's concrete builder for further customization before calling `.build()`.

#### Value supplier helpers
- `Validators.binding(String name)` — returns a `BindingFunction` that looks up the named binding from `RuleContext` at execution time; binding name is propagated as the violation parameter key
- `Validators.value(Object val)` — returns a constant `Function` that always produces the given value

#### Available validators
| Method | Rule | Description |
|---|---|---|
| `alpha(fn)` | `AlphaValidationRule` | Value must contain only alphabetic characters |
| `alphaNumeric(fn)` | `AlphaNumericValidationRule` | Value must contain only alphanumeric characters |
| `ascii(fn)` | `AsciiValidationRule` | Value must contain only ASCII characters |
| `assertTrue(fn)` | `AssertTrueValidationRule` | Value must be `true` |
| `assertFalse(fn)` | `AssertFalseValidationRule` | Value must be `false` |
| `assertEquals(fn, expected)` | `AssertEqualsValidationRule` | Value must equal the expected value |
| `assertNotEquals(fn, unexpected)` | `AssertNotEqualsValidationRule` | Value must not equal the given value |
| `blank(fn)` | `BlankValidationRule` | Value must be blank (null or whitespace-only) |
| `decimal(fn)` | `DecimalValidationRule` | Value must be a valid decimal number |
| `digits(fn, maxInt, maxFrac)` | `DigitsValidationRule` | Value must have at most `maxInt` integer digits and `maxFrac` fractional digits |
| `email(fn)` | `EmailValidationRule` | Value must be a valid e-mail address |
| `endsWith(fn, suffixes...)` | `EndsWithValidationRule` | Value must end with one of the given suffixes |
| `fileExists(fn)` | `FileExistsValidationRule` | Value must be a path to an existing file |
| `future(fn)` | `FutureValidationRule` | Date/time value must be in the future |
| `futureOrPresent(fn)` | `FutureOrPresentValidationRule` | Date/time value must be in the future or present |
| `in(fn, collection)` | `InValidationRule` | Value must be contained in the given collection |
| `lowerCase(fn)` | `LowerCaseValidationRule` | Value must be all lower-case |
| `max(fn, max)` | `MaxValidationRule` | Numeric value must be ≤ `max` |
| `decimalMax(fn, max)` | `DecimalMaxValidationRule` | Decimal value must be ≤ `max` |
| `min(fn, min)` | `MinValidationRule` | Numeric value must be ≥ `min` |
| `decimalMin(fn, min)` | `DecimalMinValidationRule` | Decimal value must be ≥ `min` |
| `negative(fn)` | `NegativeValidationRule` | Value must be strictly negative |
| `negativeOrZero(fn)` | `NegativeOrZeroValidationRule` | Value must be negative or zero |
| `notBlank(fn)` | `NotBlankValidationRule` | Value must not be blank |
| `notEmpty(fn)` | `NotEmptyValidationRule` | Collection/String/Array must not be empty |
| `notNull(fn)` | `NotNullValidationRule` | Value must not be null |
| `isNull(fn)` | `NullValidationRule` | Value must be null |
| `numeric(fn)` | `NumericValidationRule` | Value must be a numeric string |
| `past(fn)` | `PastValidationRule` | Date/time value must be in the past |
| `pastOrPresent(fn)` | `PastOrPresentValidationRule` | Date/time value must be in the past or present |
| `pattern(fn, regex)` | `PatternValidationRule` | Value must match the given regex |
| `positive(fn)` | `PositiveValidationRule` | Value must be strictly positive |
| `positiveOrZero(fn)` | `PositiveOrZeroValidationRule` | Value must be positive or zero |
| `size(fn, min, max)` | `SizeValidationRule` | Size of collection/String/Array must be within `[min, max]` |
| `startsWith(fn, prefixes...)` | `StartsWithValidationRule` | Value must start with one of the given prefixes |
| `upperCase(fn)` | `UpperCaseValidationRule` | Value must be all upper-case |
| `url(fn)` | `UrlValidationRule` | Value must be a valid URL |
