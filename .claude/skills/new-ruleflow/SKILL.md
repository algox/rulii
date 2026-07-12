---
name: new-ruleflow
description: Guide for creating a RuleFlow in rulii — fluent pipeline builder covering bind, run/apply/execute steps, step specs (as/with/onException), when/forEach/scope containers, exit, async (asyncRun/await), exception handling, and custom containers
user-invocable: true
---

# Creating a RuleFlow in rulii

Use this guide when the user asks to create, design, or explain a RuleFlow (`org.rulii.ruleflow`, added in 2.0).
A RuleFlow is an ordered pipeline of steps (bindings, rules, rule sets, nested flows, functions, actions)
with control flow (`when`, `forEach`, `scope`, `exit`), exception handling, and async execution.

Common static imports used throughout:

```java
import static org.rulii.model.action.Actions.action;
import static org.rulii.model.condition.Conditions.condition;
import static org.rulii.model.function.Functions.function;
```

## 1. Minimal RuleFlow

```java
RuleFlow<Integer> flow = RuleFlow.builder()
        .name("myFlow")                     // required — [a-zA-Z$_][a-zA-Z0-9$_]*
        .bind(x -> 42)                      // BindingDeclaration lambda: param name = binding name
        .apply(function((Integer x) -> x * 2), spec -> spec.as("result"))
        .<Integer>returning(function((Integer result) -> result))
        .build();

Integer result = flow.run();               // 84
```

Without `.returning(...)`, `run()` returns the `RuleContext`:

```java
RuleFlow<RuleContext> flow = RuleFlow.builder().name("f").bind(x -> 1).build();
RuleContext ctx = flow.run();
ctx.getBindings().getValue("x");            // 1
```

---

## 2. Builder API overview

```
RuleFlow.builder()                            // returns DefaultRuleFlowBuilder
  .name(name)                                 // required
  .description(text)
  .context(configurator)                      // customize RuleContext — MUST be first step, once only
  .param(name, Type.class)                    // required input (validated at run time)
  .param(name, Type.class, false)             // optional input
  .param(name, Type.class, defaultValueFn)    // optional with default Function<T>

  // --- pipeline steps (executed in order) ---
  .bind(decl...)  .bind(name, value)  .bind(bindings)  .bind(pojoOrMap)  .bind(loader, value)
  .bindTo(scopeName, ...)                     // same overloads, into a named scope
  .run(rule | ruleSet | ruleFlow | "registryName" | RegistryClass.class)
  .run(runnable, spec -> ...)                 // RunSpec: as() / with() / onException()
  .apply(function)  .apply(function, spec -> ...)   // result usable by later steps via as()
  .execute(action)  .execute(action, spec -> ...)   // ExecuteSpec: onException() only
  .when(condition, thenBody)                  // bodies are Consumer<builder> — no end() call
  .when(condition, thenBody, otherwiseBody)
  .forEach(sourceFn, "element", body)         // binds "element" + 0-based "index" each iteration
  .forEach(sourceFn, "element", stopCondition, body)
  .scope(body)  .scope("name", body)          // bindings inside are discarded when scope ends
  .exit()  .exit(extractorFn)                 // terminate flow immediately
  .command(customRuleFlowCommand)             // inject a pre-built command

  // --- async steps ---
  .asyncRun(runnable)  .asyncRun(runnable, spec -> ...)   // AsyncRunSpec
  .await("futureName")             .await("futureName", timeout, unit)
  .awaitAll("f1", "f2")            .awaitAll(timeout, unit, "f1", "f2")
  .awaitAny("f1", "f2")            .awaitAny(timeout, unit, "f1", "f2")

  // --- flow-level settings ---
  .onException(Type.class, handlerBody)       // global exception handler
  .finalizer(action)                          // always runs, even on early exit / exception
  .returning(extractorFn)  .returning()       // result extractor (no-arg = RuleContext)
  .build()
```

---

## 3. Running a flow

```java
flow.run();                                  // no inputs
flow.run(orderId -> "A-1", amount -> 250);   // BindingDeclaration lambdas — NOT a Bindings object
flow.run(ruleContext);                       // caller-supplied context; context(...) settings layer on top

CompletableFuture<T> f = flow.runAsync(ctx); // async, optional (timeout, unit) overload
```

Input params declared via `.param(...)` are validated before the pipeline runs — a missing
required param throws `UnrulyException`.

```java
.param("orderId", String.class)                              // required
.param("extra", String.class, false)                         // optional
.param("discount", Integer.class, function((RuleContext c) -> 5))  // default applied if absent
```

---

## 4. Steps and step specs

`run()` accepts a `Rule`, `RuleSet<?>`, nested `RuleFlow<?>`, a registry name (`String`), or a
registry class (`Class<?>` — looked up in `RuleRegistry` at run time). `apply()` takes a
`Function<?>`, `execute()` an `Action`.

Step configuration lives in the spec consumer — there are no top-level `as()`/`with()` methods:

```java
.run(pricingRule, spec -> spec
        .as("price")                         // bind step result into current scope
        .as("audit", "price")                // ...or into named scope "audit"
        .with(discount -> 0.1)               // step-scoped params (exist only during the step)
        .with(pojoOrMap)                     // ...or from a POJO / Map<String,Object>
        .onException(Exception.class, b -> b.run(fallbackRule)))
```

- `RunSpec` (for `run`/`apply`): `as`, `with`, `onException`
- `ExecuteSpec` (for `execute`): `onException` only
- `AsyncRunSpec` (for `asyncRun`): see section 7

---

## 5. Control flow

Container bodies are `Consumer<builder>` lambdas — the body itself delimits the scope,
no `endWhen()`/`endForEach()` exists:

```java
// Branching
.when(condition((Integer x) -> x > 0),
      b -> b.run(positiveRule),
      b -> b.run(negativeRule))              // otherwise-branch optional

// Iteration — binds each element under "item" and the 0-based position under "index"
.forEach(function(() -> List.of(1, 2, 3)), "item",
         b -> b.run(perItemRule))

// Iteration with early stop (checked after each element)
.forEach(sourceFn, "item", condition((Integer item) -> item > 100),
         b -> b.run(perItemRule))

// Scoped bindings — discarded when the body ends
.scope("temp", b -> b
        .bind(intermediate -> compute())
        .run(usesIntermediateRule))

// Early termination — commands after a top-level exit() fail at build() as unreachable
.when(alreadyProcessed, b -> b.exit(function(() -> "cached")))
.exit(function((Integer total) -> total))    // or .exit() to return the RuleContext
```

---

## 6. Exception handling

Three levels, all with **catch-and-continue** semantics (the handler body runs, then the flow
moves to the next step). Inside every handler body the caught exception is bound as `"ex"`:

```java
// Step-level — checked first
.run(riskyRule, spec -> spec.onException(UnrulyException.class,
        b -> b.execute(action((Exception ex) -> log.warn("step failed", ex)))))

// Flow-level global — fallback for any step without a matching step handler
.onException(Exception.class, b -> b.bind(handled -> true))
```

If neither handler matches, the exception propagates out of `run()`.

**`context()` is forbidden inside handler/continuation bodies** — it throws at build time
(the body runs against an already-built context).

The `finalizer` always runs (early exit, exception, success). If the finalizer itself throws
while another exception is propagating, the finalizer failure is logged and the original
exception propagates — note `UnrulyException` disables suppressed exceptions project-wide,
so don't look for `getSuppressed()`.

---

## 7. Async pipeline

```java
.asyncRun(pricingRuleSet, spec -> spec
        .as("pricingFuture")                 // bind the CompletableFuture in the CALLING scope
        .withImmutableBindings())            // snapshot bindings; task can't mutate caller's
.asyncRun(inventoryRule, spec -> spec
        .as("inventoryFuture")
        .withContext(separateCtx))           // fully independent, user-supplied RuleContext
.awaitAll("pricingFuture", "inventoryFuture")   // default timeout 30s; explicit overloads exist
.apply(function((CompletableFuture<?> pricingFuture) ->
        pricingFuture.getNow(null)), spec -> spec.as("price"))
```

Key semantics:
- **Context modes**: default SHARED (same context — beware concurrent mutation),
  `withImmutableBindings()` = IMMUTABLE snapshot, `withContext(ctx)` = CUSTOM.
- **`await`/`awaitAll`/`awaitAny` do NOT unwrap** — the binding stays a `CompletableFuture<T>`;
  read it with `getNow(null)` / `join()` in a later step.
- Timeout → `TimeoutException` wrapped in `UnrulyException`; the future is **not** cancelled.
- `thenRun("result", body)` — non-blocking continuation on the completion thread; the resolved
  value is bound under `"result"` for the body's duration. The `as()` binding is the **chained**
  future, so a later `await` waits for the continuation too. The body does not replace the
  future's value.
- `spec.onException(...)` on an async step fires as soon as the task (or its `thenRun`
  continuation) fails — **even if nothing ever awaits it**. When handled, the future resolves
  with `null`. Without a handler, a `thenRun` failure is only observed by a later `await`
  (unobserved failures are silently dropped, like raw `CompletableFuture`).

---

## 8. Custom context and composition

```java
// context() must be the FIRST pipeline step, and may be called only once
RuleFlow.builder()
        .name("flow")
        .context(ctxBuilder -> ctxBuilder.matchUsing(MatchByTypeMatchingStrategy.class))
        .bind(...)
        ...
```

With `flow.run(existingCtx)`, the `context(...)` settings layer on top of the supplied context.

Flows compose — `run(otherFlow)` nests a flow, and rules/rulesets registered in a
`RuleRegistry` can be referenced by name or class:

```java
.run("checkoutRuleSet")          // looked up in RuleRegistry at run time
.run(FraudCheckRule.class)
```

---

## 9. Extending the builder

Custom leaf step — implement `RuleFlowCommand` and inject with `.command(cmd)`.

Custom container (retry, parallel, circuit-breaker...) — extend the builder template and use
`runContainer`:

```java
public abstract class MyBaseBuilder<SELF extends MyBaseBuilder<SELF>>
        extends RuleFlowBuilderTemplate<SELF> {

    public SELF retry(int maxAttempts, Consumer<SELF> body) {
        return runContainer(new RetryCommand(maxAttempts), body);
    }
}
```

See `src/test/java/org/rulii/test/ruleflow/CustomRuleFlowTest.java` for a working example.

---

## Common Mistakes

| Mistake | Fix |
|---------|-----|
| `context()` after another step, or called twice | It must be the first pipeline step, once only — throws immediately otherwise |
| `context()` inside an `onException`/`thenRun` body | Forbidden — throws at build time |
| Expecting `await("f")` to replace the binding with the value | Binding stays `CompletableFuture<T>` — read via `getNow(null)`/`join()` |
| Commands after a top-level `exit()` | `build()` throws `UnrulyException` — unreachable commands |
| Passing a `Bindings` object as the single arg to `flow.run(...)` | Pass individual `BindingDeclaration` lambdas: `run(x -> 1, y -> 2)`; `bind(bindings)` is only a builder step |
| Looking for `endWhen()`/`endScope()` | Containers use `Consumer<builder>` bodies — the lambda IS the scope |
| Calling `as()`/`with()` on the builder itself | Step config only exists inside the `spec ->` consumer |
| `returning(...)` without the type witness | Use `.<String>returning(function((String s) -> s))` so `build()` infers `RuleFlow<String>` |
| Expecting a handled exception to abort the flow | Handlers are catch-and-continue — execution resumes at the next step; rethrow inside the handler body to abort |
| Async step mutating shared bindings unexpectedly | Default mode is SHARED; use `withImmutableBindings()` or `withContext(ctx)` for isolation |

## Reference tests

- `src/test/java/org/rulii/test/ruleflow/RuleFlowTest.java` — 116 tests covering every construct
- `src/test/java/org/rulii/test/ruleflow/RuleFlowTracerTest.java` — tracer/listener integration
- `src/test/java/org/rulii/test/ruleflow/CustomRuleFlowTest.java` — custom container extension
