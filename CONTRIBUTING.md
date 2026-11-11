# Contributing to rulii

Thanks for taking the time. rulii has one maintainer, so the fastest way to get something in is to make it easy to
review.

## Before you start

- **A question about using rulii** goes in [Discussions](https://github.com/algox/rulii/discussions), under Q&A.
- **A bug** goes in an [issue](https://github.com/algox/rulii/issues). Include the rulii version, the smallest rule
  that shows it, how you ran it, what you expected and what happened.
- **A feature** starts as an issue or an Ideas discussion, before any code. It saves you writing something that
  doesn't fit.
- **A security problem** is reported privately. See [SECURITY.md](SECURITY.md).

A typo, a broken link or a missing test doesn't need an issue. Send the pull request.

## Building

You need JDK 17 or later and Maven.

```bash
git clone https://github.com/algox/rulii.git
cd rulii
mvn test
```

That runs the whole suite, over 1,600 tests. It must pass before a pull request is reviewed.

## Sending a pull request

1. Fork the repository and branch from `develop`.
2. Make the change, with tests.
3. Add a line to `change-log.md` under the unreleased version, if a user would notice the change.
4. Open the pull request against `develop`, and say what it changes and why.

Keep a pull request to one thing. A fix and a refactoring are two pull requests.

## What the code looks like

- **Licence header.** Every source file, main and test, starts with the Apache 2.0 licence block. Copy it from any
  existing file.
- **Explicit `super()`.** Every constructor calls `super();`, even when it does nothing.
- **Aligned constants.** `ERROR_CODE` and `DEFAULT_MESSAGE` are padded so the `=` signs line up:

  ```java
  public static final String ERROR_CODE       = "myRule.errorCode";
  public static final String DEFAULT_MESSAGE  = "Value must satisfy X.";
  ```

- **No new dependencies.** The core jar depends on nothing, and that stays. Anything optional is a `provided`
  dependency and is only used when it's on the classpath.
- **Leave `org.rulii.lib` alone.** It's vendored code from other projects, kept as close to the originals as possible.
- **Javadoc on public API.** A new public class or method says what it does.
- **Java 17.** No language features or APIs newer than that.

## Tests

Tests are JUnit 5 and live under `src/test/java/org/rulii/test`, in the package that matches what they test. A bug
fix comes with a test that fails without it. A new rule, validator or builder method comes with tests for the passing
case, the failing case and the skipped case.

If you use Claude Code, the repository has skills in `.claude/skills` that know the conventions: `/write-test`,
`/new-rule`, `/new-validation-rule` and the rest.

## AI-assisted contributions

They're fine. You answer for what you send, the same as I do: read it, run it, and be ready to explain it in review.

## The other libraries

Each has its own repository, and the same rules apply there:

- [rulii-spring](https://github.com/algox/rulii-spring), the Spring Boot integration
- [rulii-bean-validation](https://github.com/algox/rulii-bean-validation), Jakarta Bean Validation on the engine
- [rulii-explorer](https://github.com/algox/rulii-explorer), the viewer
- [rulii-samples](https://github.com/algox/rulii-samples), runnable examples

## Licence

rulii is under the [Apache License 2.0](LICENSE). A contribution you send is under the same licence, as section 5 of
the licence says. There's no contributor agreement to sign.
