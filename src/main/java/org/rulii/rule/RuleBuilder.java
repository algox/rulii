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
import org.rulii.util.reflect.ObjectFactory;

/**
 * Builder class for creating Rule instances.
 *
 * @author Max Arulananthan.
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

    public <T> ClassBasedRuleBuilder<T> with(Class<T> ruleClass) {
        Assert.notNull(ruleClass, "ruleClass cannot be null.");
        ObjectFactory objectFactory = ObjectFactory.builder().build();
        return with(ruleClass, objectFactory);
    }

    public <T> Rule build(Class<T> ruleClass) {
        return with(ruleClass).build();
    }

    @SuppressWarnings("unchecked")
    public <T> ClassBasedRuleBuilder<T> with(T ruleTarget) {
        Assert.notNull(ruleTarget, "ruleTargetCannot be null");
        return ClassBasedRuleBuilder.with((Class<T>) ruleTarget.getClass(), ruleTarget);
    }

    public <T> Rule build(T ruleTarget) {
        return with(ruleTarget).build();
    }

    public <T> ClassBasedRuleBuilder<T> with(Class<T> ruleClass, ObjectFactory objectFactory) {
        Assert.notNull(ruleClass, "ruleClass cannot be null.");
        Assert.notNull(objectFactory, "objectFactory cannot be null.");
        return ClassBasedRuleBuilder.with(ruleClass, objectFactory.createRule(ruleClass));
    }

    public <T> Rule build(Class<T> ruleClass, ObjectFactory objectFactory) {
        return with(ruleClass, objectFactory).build();
    }

    public LambdaBasedRuleBuilder<?> name(String ruleName) {
        return new LambdaBasedRuleBuilder(ruleName, null);
    }

    public <T> LambdaBasedRuleBuilder<T> name(String ruleName, String description) {
        return new LambdaBasedRuleBuilder<T>(ruleName, description);
    }

    public ValidationRuleBuilder<?> validationRule(String name, String errorCode) {
        return validationRule(name, null, errorCode);
    }

    public ValidationRuleBuilder<?> validationRule(String name, String description, String errorCode) {
        return new ValidationRuleBuilder<>(name, description, errorCode);
    }
}
