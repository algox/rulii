/*
 * This software is licensed under the Apache 2 license, quoted below.
 *
 * Copyright (c) 1999-2024, Algorithmx Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.rulii.rule;

import org.rulii.annotation.Rule;
import org.rulii.annotation.*;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.UnrulyException;
import org.rulii.model.action.Action;
import org.rulii.model.condition.Condition;
import org.rulii.util.Ordered;

import java.util.List;

/**
 * ClassBasedRuleBuilder is a generic class that extends AbstractRuleBuilder and is used to build rule objects based on a Rule class and a target object.
 * It provides methods to retrieve information about the Rule class such as name, description, and order.
 * It also loads the Rule class by setting the necessary properties like name, description, order, pre-conditions, conditions, then-actions, and otherwise actions.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class ClassBasedRuleBuilder<T> extends AbstractRuleBuilder<T> {

    /**
     * Constructor for the ClassBasedRuleBuilder class. Initializes a new instance with the specified inline flag set to false.
     */
    protected ClassBasedRuleBuilder() {
        super(false);
    }

    /**
     * Constructor for the ClassBasedRuleBuilder class. Initializes a new instance with the specified inline flag set to false.
     *
     * @param ruleClass the desired Rule class to be loaded. Must not be null.
     * @param target the rule implementation object. Must not be null.
     */
    protected ClassBasedRuleBuilder(Class<T> ruleClass, T target) {
        super(false);
        load(ruleClass, target);
    }

    /**
     * Creates a ClassBasedRuleBuilder instance with the specified ruleClass and target object.
     *
     * @param <T> the type of the ruleClass and target object
     * @param ruleClass the desired Rule class to be loaded. Must not be null.
     * @param target the rule implementation object. Must not be null.
     * @return a new ClassBasedRuleBuilder instance
     */
    public static <T> ClassBasedRuleBuilder<T> with(Class<T> ruleClass, T target) {
        return new ClassBasedRuleBuilder<>(ruleClass, target);
    }

    /**
     * Retrieves the name of the Rule based on the Rule annotation on the provided class.
     *
     * @param <T> the type of the Rule class
     * @param ruleClass the Class object representing the Rule class
     * @return the name of the Rule as specified in the annotation, or the simple class name if no annotation is present
     */
    public static <T> String getRuleName(Class<T> ruleClass) {
        // Try and locate the Rule annotation on the class
        Rule rule = ruleClass.getAnnotation(Rule.class);

        return rule == null ? ruleClass.getSimpleName() :
                Rule.NOT_APPLICABLE.equals(rule.name())
                        ? ruleClass.getSimpleName()
                        : rule.name();
    }

    /**
     * Retrieves the detailed description of a rule based on the Description annotation on the provided class.
     *
     * @param <T>        the type of the ruleClass
     * @param ruleClass   the Class object representing the rule class to retrieve the description from
     * @return the detailed description specified in the Description annotation, or null if no annotation is present
     */
    public static <T> String getRuleDescription(Class<T> ruleClass) {
        Description descriptionAnnotation = ruleClass.getAnnotation(Description.class);
        return descriptionAnnotation != null ? descriptionAnnotation.value() : null;
    }

    /**
     * Retrieves the rule order value specified in the Order annotation of the provided Rule class.
     *
     * @param <T>       the type of the Rule class
     * @param ruleClass the Class object representing the Rule class
     * @return the rule order value as specified in the annotation, or Ordered.LOWEST_PRECEDENCE if no annotation is present
     */
    public static <T> Integer getRuleOrder(Class<T> ruleClass) {
        Order orderAnnotation = ruleClass.getAnnotation(Order.class);
        return orderAnnotation != null ? orderAnnotation.value() : Ordered.LOWEST_PRECEDENCE;
    }

    /**
     * Loads the given Rule class. The Rule class must be annotated with @Rule and must define a single "given" method
     * which returns a boolean. The when method can take a arbitrary number of arguments.
     *
     * @param ruleClass desired Rule class.
     * @param target rule implementation.
     */
    protected void load(Class<T> ruleClass, T target) {
        Assert.notNull(ruleClass, "ruleClass cannot be null.");
        Assert.notNull(target, "target cannot be null.");
        ruleClass(ruleClass);
        target(target);
        name(getRuleName(ruleClass));
        description(getRuleDescription(ruleClass));
        order(getRuleOrder(ruleClass));
        loadPreCondition(ruleClass, target);
        loadCondition(ruleClass, target);
        loadThenActions(target);
        loadOtherwiseAction(target);
    }

    /**
     * Loads and processes the pre-condition method of the specified rule class against the target object.
     *
     * @param ruleClass the Class representing the rule class with the pre-condition method
     * @param target the target object to process the pre-condition against
     */
    protected void loadPreCondition(Class<T> ruleClass, Object target) {
        List<Condition> preConditions = Condition.builder().build(target, PreCondition.class);

        if (preConditions.isEmpty()) return;

        if (preConditions.size() > 1) {
            // Too many matches
            throw new UnrulyException(ruleClass.getSimpleName() + " class has too many " +
                    " PreCondition methods. " + "There can be at most one method (Annotated with @PreCondition)."
                    + "Currently there are [" + preConditions.size()
                    + "] candidates [" + preConditions + "]");
        }

        // Load Pre-Condition
        if (preConditions.get(0) != null) pre(preConditions.get(0));
    }

    /**
     * Introspects the given object for methods that are annotated with the required Annotation and builds corresponding conditions for them.
     *
     * @param ruleClass the Rule class to load conditions for
     * @param target the target object to build conditions from
     */
    protected void loadCondition(Class<T> ruleClass, Object target) {
        List<Condition> givenConditions = Condition.builder().build(target, Given.class);

        if (givenConditions.isEmpty()) return;

        if (givenConditions.size() > 1) {
            // Too many matches
            throw new UnrulyException(ruleClass.getSimpleName() + " class has too many " +
                    " Given methods. " + "There can be at most one method (Annotated with @Given)."
                    + "Currently there are [" + givenConditions.size()
                    + "] candidates [" + givenConditions + "]");
        }

        // Set Given-Condition
        if (givenConditions.get(0) != null) given(givenConditions.get(0));
    }

    /**
     * Loads all the @Then actions in the given class. A method is considered an Action if takes arbitrary number
     * of arguments and returns nothing (ie: void) and the method is annotated with @ActionConsumer.
     *
     * @param target rule target.
     */
    protected void loadThenActions(Object target) {
        List<Action> thenActions = Action.builder().build(target, Then.class);

        if (thenActions.isEmpty()) return;

        // Load Then-Actions
        for (Action thenAction : thenActions) {
            then(thenAction);
        }
    }

    /**
     * Loads the @Otherwise action in the given class. A method is considered an Action if takes arbitrary number
     * of arguments and returns nothing (ie: void) and the method is annotated with @Otherwise.
     *
     * @param target rule target.
     */
    protected void loadOtherwiseAction(Object target) {
        List<Action> otherwiseActions = Action.builder().build(target, Otherwise.class);

        if (otherwiseActions.isEmpty()) return;
        if (otherwiseActions.size() > 1) throw new UnrulyException(getRuleClass() + " has too many "
                + "@Otherwise action methods. " + "There can be at most 1."
                + " methods (Annotated with @Otherwise"
                + "). Currently there are [" + otherwiseActions.size()
                + "] candidates [" + otherwiseActions + "]");

        // Load Otherwise-Action
        if (otherwiseActions.get(0) != null) otherwise(otherwiseActions.get(0));
    }
}
