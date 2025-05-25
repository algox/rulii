[Rulii Maven Central]:http://search.maven.org/#artifactdetails|org.rulii|rulii|1.1.0|
[Apache 2.0 License]:https://opensource.org/licenses/Apache-2.0

# _Rulii_
**Rule your code** <br/><sub> _100% Java_ &middot; _Easy to learn_ &middot; _Declarative and Functional models_ &middot; _Zero dependencies_ &middot; _Spring support_ </sub>

---

[![License](https://img.shields.io/badge/license-Apache%202.0-orange.svg)][Apache 2.0 License]
[![Maven Central Version](https://img.shields.io/maven-central/v/org.rulii/rulii)][Rulii Maven Central]
[![Javadoc](https://javadoc.io/badge2/org.rulii/rulii/1.1.0/javadoc.svg)](https://javadoc.io/doc/org.rulii/rulii/1.1.0)
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
 * **[Spring support](https://github.com/algox/rulii-spring)**


## Getting started
_Add the dependency_

Maven
```xml
<dependency>
    <groupId>org.rulii</groupId>
    <artifactId>rulii</artifactId>
    <version>1.1.0</version>
</dependency>
```

Grade
```groovy
compile 'org.rulii:rulii:1.1.0'
```

## Writing Rules

#### Declaratively

Let's write a simple Validation Rule. Given two non-null dates (fromDate, toDate), let's validate that fromDate is before toDate. 

```java
@Rule
@Description("This Rule will validate that the from date is before the to date.")
public class ConsistentDateRule {

    public ConsistentDateRule() {
        super();
    }

    @PreCondition // Don't run the rule if we have null values
    public boolean check(LocalDate fromDate, LocalDate toDate) {
        return fromDate != null && toDate != null;
    }

    @Given // Condition
    public boolean isValid(LocalDate fromDate, LocalDate toDate) {
        return fromDate.isBefore(toDate);
    }

    @Otherwise() // Else Action
    public void otherwise(LocalDate fromDate, LocalDate toDate, RuleViolations violations) {
        violations.add(RuleViolation.builder().build("consistentDateRule", "errorCode.100",
                "fromDate [" + fromDate + "] should be before toDate [" + toDate + "]"));
    }
}

// Create the Rule instance
Rule rule = Rule.builder().build(ConsistentDateRule.class);

```

#### Functionally

```java

Rule rule = Rule.builder()
        .name("consistentDateRule")
        .description("This Rule will validate that the from date is before the to date.")
        .preCondition(condition((LocalDate fromDate, LocalDate toDate) -> toDate != null && fromDate != null))
        .given(condition((LocalDate fromDate, LocalDate toDate) -> fromDate.isBefore(toDate)))
        .otherwise(action((LocalDate fromDate, LocalDate toDate, RuleViolations violations) -> {
            violations.add(RuleViolation.builder().build("consistentDateRule", "errorCode.100",
                    "fromDate [" + fromDate + "] should be before toDate [" + toDate + "]"));
        }))
        .build();
```

**Run the Rule**
```java
// Create your bindings
Bindings bindings = Bindings.builder().standard();
bindings.bind("fromDate", LocalDate.of(1980, Month.JANUARY, 1));
bindings.bind("toDate", LocalDate.now());
bindings.bind("violations", new RuleViolations());

// Run the Rule
RuleResult result = rule.run(bindings);

if (result.status().isPass()) {
    // Rule passed   
} else {
    // Rule failed    
}

```

**That's it! You have written your first Rule.**

## Writing RuleSets

```java

RuleSet<RuleViolations> ruleSet = RuleSet.builder()
        .with("testRuleSet")
        .rule(new AlphaNumericValidationRule("a"))
        .rule(new NotEmptyValidationRule("a"))
        .rule(new NotNullValidationRule("b"))
        .rule(new NumericValidationRule("b"))
        .rule(new UpperCaseValidationRule("c"))
        .rule(Rule.builder().build(ConsistentDateRule.class))
        .resultExtractor(function((RuleViolations violations) -> violations))
        .build();

// Create your bindings
Bindings bindings = Bindings.builder().standard();
bindings.bind("a", "aaa");
bindings.bind("b", 123);
bindings.bind("c", "ABC");
bindings.bind("flag", true);
bindings.bind("fromDate", LocalDate.of(1980, Month.JANUARY, 1));
bindings.bind("toDate", LocalDate.now());
bindings.bind("violations", new RuleViolations());

//Run the RuleSet
RuleViolations violations = ruleSet.run(bindings);

// Found errors
if (violations.hasErrors()) {
    throw new ValidationException(violations);
}

```
**Now you are ready to Rule your code!**
