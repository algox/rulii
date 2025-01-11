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
package org.rulii.validation;

import org.rulii.lib.spring.util.Assert;
import org.rulii.model.condition.Condition;
import org.rulii.rule.ClassBasedRuleBuilder;
import org.rulii.rule.Rule;

public class ValidationRuleBuilder extends ClassBasedRuleBuilder<SuppliedValidationRule> {

    private final Condition condition;
    private String errorCode;
    private Severity severity;
    private String errorMessage;
    private String defaultMessage;

    public ValidationRuleBuilder(String name, Condition condition) {
        super();
        Assert.notNull(condition, "condition cannot be null.");
        this.condition = condition;
        name(name);
    }

    public ValidationRuleBuilder errorCode(String errorCode) {
        Assert.notNull(errorCode, "errorCode cannot be null.");
        this.errorCode = errorCode;
        return this;
    }

    public ValidationRuleBuilder severity(Severity severity) {
        Assert.notNull(severity, "severity cannot be null.");
        this.severity = severity;
        return this;
    }

    public ValidationRuleBuilder errorMessage(String errorMessage) {
        Assert.notNull(errorMessage, "errorMessage cannot be null.");
        this.errorMessage = errorMessage;
        return this;
    }

    public ValidationRuleBuilder defaultMessage(String defaultMessage) {
        this.defaultMessage = defaultMessage;
        return this;
    }

    @Override
    public Rule build() {
        load(SuppliedValidationRule.class, new SuppliedValidationRule(condition, errorCode, severity, errorMessage, defaultMessage));
        return super.build();
    }
}
