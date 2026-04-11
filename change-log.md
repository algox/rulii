# Changelog

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
