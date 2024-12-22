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

import org.rulii.annotation.*;
import org.rulii.annotation.Rule;
import org.rulii.model.action.Action;
import org.rulii.model.condition.Condition;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.UnrulyException;
import org.rulii.util.Ordered;

import java.util.List;

/**
 * Builder class for all Class based Rule(s).
 *
 * @author Max Arulananthan.
 * @since 1.0
 */
public class ClassBasedRuleBuilder<T> extends AbstractRuleBuilder<T> {

    private ClassBasedRuleBuilder(Class<T> ruleClass, T target) {
        super(false);
        load(ruleClass, target);
    }

    public static <T> ClassBasedRuleBuilder<T> with(Class<T> ruleClass, T target) {
        return new ClassBasedRuleBuilder(ruleClass, target);
    }

    public static <T> String getRuleName(Class<T> ruleClass) {
        // Try and locate the Rule annotation on the class
        Rule rule = ruleClass.getAnnotation(Rule.class);

        String ruleName = rule == null ? ruleClass.getSimpleName() :
                Rule.NOT_APPLICABLE.equals(rule.name())
                        ? ruleClass.getSimpleName()
                        : rule.name();

        return ruleName;
    }

    public static <T> String getRuleDescription(Class<T> ruleClass) {
        Description descriptionAnnotation = ruleClass.getAnnotation(Description.class);
        return descriptionAnnotation != null ? descriptionAnnotation.value() : null;
    }

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

    protected void loadPreCondition(Class<T> ruleClass, Object target) {
        List<Condition> preConditions = Condition.builder().build(target, PreCondition.class);

        if (preConditions == null || preConditions.size() == 0) return;

        if (preConditions.size() > 1) {
            // Too many matches
            throw new UnrulyException(ruleClass.getSimpleName() + " class has too many " +
                    " PreCondition methods. " + "There can be at most one method (Annotated with @PreCondition)."
                    + "Currently there are [" + preConditions.size()
                    + "] candidates [" + preConditions + "]");
        }

        // Load Pre-Condition
        if (preConditions.size() == 1 && preConditions.get(0) != null) {
            pre(preConditions.get(0));
        }
    }

    protected void loadCondition(Class<T> ruleClass, Object target) {
        List<Condition> givenConditions = Condition.builder().build(target, Given.class);

        if (givenConditions.size() > 1) {
            // Too many matches
            throw new UnrulyException(ruleClass.getSimpleName() + " class has too many " +
                    " Given methods. " + "There can be at most one method (Annotated with @Given)."
                    + "Currently there are [" + givenConditions.size()
                    + "] candidates [" + givenConditions + "]");
        }

        // Set Given-Condition
        if (givenConditions.size() == 1 && givenConditions.get(0) != null) {
            given(givenConditions.get(0));
        }
    }

    /**
     * Loads all the @Then actions in the given class. A method is considered an Action if takes arbitrary number
     * of arguments and returns nothing (ie: void) and the method is annotated with @ActionConsumer.
     *
     * @param target rule target.
     */
    protected void loadThenActions(Object target) {
        List<Action> thenActions = Action.builder().build(target, Then.class);

        if (thenActions == null) return;

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

        if (otherwiseActions == null) return;
        if (otherwiseActions.size() > 1) throw new UnrulyException(getRuleDefinition().getRuleClass() + " has too many "
                + "@Otherwise action methods. " + "There can be at most 1."
                + " methods (Annotated with @Otherwise"
                + "). Currently there are [" + otherwiseActions.size()
                + "] candidates [" + otherwiseActions + "]");

        // Load Otherwise-Action
        if (otherwiseActions.size() == 1) {
            otherwise(otherwiseActions.get(0));
        }
    }
}
