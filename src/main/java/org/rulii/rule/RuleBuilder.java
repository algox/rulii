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

import org.rulii.lib.spring.util.Assert;
import org.rulii.model.condition.Condition;
import org.rulii.util.reflect.ObjectFactory;
import org.rulii.validation.ValidationRuleBuilder;

/**
 * RuleBuilder class provides a fluent interface to build different types of rules.
 * You can create rule instances based on class type, object instance, provide rule names, descriptions, and validation errors.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public final class RuleBuilder {

    private static final RuleBuilder instance = new RuleBuilder();

    private RuleBuilder() {
        super();
    }

    public static RuleBuilder getInstance() {
        return instance;
    }

    /**
     * Sets the rule class for building a rule based on the specified class, utilizing a default object factory.
     *
     * @param <T> the type of the rule class
     * @param ruleClass the class object representing the rule
     * @return a ClassBasedRuleBuilder instance for further rule building
     */
    public <T> ClassBasedRuleBuilder<T> with(Class<T> ruleClass) {
        Assert.notNull(ruleClass, "ruleClass cannot be null.");
        ObjectFactory objectFactory = ObjectFactory.builder().build();
        return with(ruleClass, objectFactory);
    }

    /**
     * Builds a Rule object based on the specified rule class.
     *
     * @param <T> the type of the rule class
     * @param ruleClass the class object representing the rule
     * @return a Rule instance constructed based on the provided ruleClass.
     */
    public <T> Rule build(Class<T> ruleClass) {
        return with(ruleClass).build();
    }

    @SuppressWarnings("unchecked")
    public <T> ClassBasedRuleBuilder<T> with(T ruleTarget) {
        Assert.notNull(ruleTarget, "ruleTargetCannot be null");
        return ClassBasedRuleBuilder.with((Class<T>) ruleTarget.getClass(), ruleTarget);
    }

    /**
     * Builds a Rule object based on the specified ruleTarget object.
     *
     * @param ruleTarget the object to build the Rule from
     * @return the built Rule object
     */
    public <T> Rule build(T ruleTarget) {
        return with(ruleTarget).build();
    }

    /**
     * Sets the rule class for building a rule based on the specified class, utilizing a default object factory.
     *
     * @param <T> the type of the rule class
     * @param ruleClass the class object representing the rule
     * @param objectFactory the object factory used to create rules
     * @return a ClassBasedRuleBuilder instance for further rule building
     */
    public <T> ClassBasedRuleBuilder<T> with(Class<T> ruleClass, ObjectFactory objectFactory) {
        Assert.notNull(ruleClass, "ruleClass cannot be null.");
        Assert.notNull(objectFactory, "objectFactory cannot be null.");
        return ClassBasedRuleBuilder.with(ruleClass, objectFactory.createRule(ruleClass));
    }

    /**
     * Constructs and returns a Rule instance based on the specified ruleClass and objectFactory.
     *
     * @param <T> the type of the rule class
     * @param ruleClass the class object representing the rule
     * @param objectFactory the object factory used to create rules
     * @return a Rule instance constructed based on the provided ruleClass
     */
    public <T> Rule build(Class<T> ruleClass, ObjectFactory objectFactory) {
        return with(ruleClass, objectFactory).build();
    }

    /**
     * Sets the name for the rule being built.
     *
     * @param ruleName the name of the rule
     * @return a LambdaBasedRuleBuilder instance for further rule building
     */
    public LambdaBasedRuleBuilder<?> name(String ruleName) {
        return new LambdaBasedRuleBuilder<>(ruleName, null);
    }

    /**
     * Sets the name and description for the rule being built.
     *
     * @param ruleName the name of the rule
     * @param description the description of the rule
     * @return a LambdaBasedRuleBuilder instance for further rule building
     */
    public <T> LambdaBasedRuleBuilder<T> name(String ruleName, String description) {
        return new LambdaBasedRuleBuilder<T>(ruleName, description);
    }

    /**
     * Creates a new ValidationRuleBuilder with the specified name and condition.
     *
     * @param name the name of the validation rule
     * @param condition the condition function that determines the validity of the rule
     * @return a ValidationRuleBuilder instance for further rule building
     */
    public ValidationRuleBuilder validationRule(String name, Condition condition) {
        return new ValidationRuleBuilder(name, condition);
    }

    /**
     * Creates a new ValidationRuleBuilder with the specified name, error code, and condition.
     *
     * @param name the name of the validation rule
     * @param errorCode the error code associated with the rule
     * @param condition the condition function that determines the validity of the rule
     * @return a ValidationRuleBuilder instance for further rule building
     */
    public ValidationRuleBuilder validationRule(String name, String errorCode, Condition condition) {
        ValidationRuleBuilder result = new ValidationRuleBuilder(name, condition);
        result.errorCode(errorCode);
        return result;
    }
}
