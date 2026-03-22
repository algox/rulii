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
