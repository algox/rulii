[Rulii Maven Central]:http://search.maven.org/#artifactdetails|org.rulii|rulii|1.0.0|
[Apache 2.0 License]:https://opensource.org/licenses/Apache-2.0

# _Rulii_
**Rule your code** <br/><sub> _100% Java_ &middot; _Easy to learn_ &middot; _zero dependencies_ &middot; _Simple DSL_ </sub>

---

[![License](https://img.shields.io/badge/license-Apache%202.0-orange.svg)][Apache 2.0 License]
[![Maven Central Version](https://img.shields.io/maven-central/v/org.rulii/rulii)][Rulii Maven Central]
[![Javadoc](https://javadoc.io/badge2/org.rulii/rulii/1.0.0/javadoc.svg)](https://javadoc.io/doc/org.rulii/rulii/1.0.0)
![Build](https://github.com/algox/rulii/actions/workflows/maven.yml/badge.svg)

## What is it?

_Rulii_ is a Rule Engine which organizes business logic through a set of rules, each consisting of a condition and a set of action(s). 
The engine runs rules on data, identifying rules whose conditions match and then executing the corresponding actions.
This is an alternative computational model, based on production rules that define conditions and actions, akin to "if-then" statements. 
The engine evaluates these rules in an order it deems appropriate, allowing flexible decision-making based on conditions.

This model promotes a clear separation between business logic and data, making it easier to adapt when business rules change and simplifying the testing of those changes. 
It is particularly helpful in scenarios like data validation, qualification processes, or calculations, where actions are triggered based on the truth or falsity of specific conditions. 


## Features

 * Abstractions help define and implement business rules in Java in a structured and maintainable way, making it easier to manage and apply them across your system.
 * You can define rules either by creating classes or by using lambdas in a functional style, depending on your preference and the complexity of the rules.
 * You can organize related rules into a RuleSet, which encourages reuse and better management of rules.
 * Rules and RuleSets are stateless, making them highly efficient and performant.
 * Lightweight with no external dependencies.


## Getting started
_Add the dependency_

Maven
```xml
<dependency>
    <groupId>org.rulii</groupId>
    <artifactId>rulii</artifactId>
    <version>1.0.0</version>
</dependency>
```

Grade
```groovy
compile 'org.rulii:rulii:1.0.0'
```

## Writing Rules

#### Declaratively

```java
@Rule
@Description("Hello world Rule!")
public class HelloWorldRule {

    public HelloWorldRule() {
        super();
    }

    @Given
    public boolean shouldSayHi(boolean flag) {
        return flag;
    }

    @Then
    public void sayHi() {
        System.out.println("Hello World!");
    }
}

// Create the Rule instance
Rule rule = Rule.builder().build(HelloWorldRule.class);

// Create your bindings
Bindings bindings = Bindings.builder().standard();
// We need one binding "flag"
bindings.bind("flag", true);

// Run the Rule
RuleResult result = rule.run(bindings);
```

#### Functionally

```java

Rule rule = Rule.builder()
                .name("HelloWorldRule")
                .description("Hello world Rule!")
                .given(condition((Boolean flag) -> flag))
                .then(action(() -> System.out.println("Hello World!")))
                .build();

// Create your bindings
Bindings bindings = Bindings.builder().standard();
// We need one binding "flag"
bindings.bind("flag", true);

// Run the Rule
RuleResult result = rule.run(bindings);
```

**That's it! You have written your first Rule!**

## Writing RuleSets

```java

RuleSet<RuleSetExecutionStatus> ruleSet = RuleSet.builder().with("TestRuleSet")
                .validationRule(new AlphaNumericValidationRule("a"))
                .validationRule(new NotEmptyValidationRule("a"))
                .validationRule(new NotNullValidationRule("b"))
                .validationRule(new NumericValidationRule("b"))
                .validationRule(new UpperCaseValidationRule("c"))
                .rule(Rule.builder().build(HelloWorldRule.class))
                .build();

// Create your bindings
Bindings bindings = Bindings.builder().standard();
bindings.bind("a", "aaa");
bindings.bind("b", 123);
bindings.bind("c", "ABC");
bindings.bind("flag", true);

//Run the RuleSet
RuleSetExecutionStatus result = ruleSet.run(bindings);

```
