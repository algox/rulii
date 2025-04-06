/*
 * This software is licensed under the Apache 2 license, quoted below.
 *
 * Copyright (c) 1999-2025, Algorithmx Inc.
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
package org.rulii.validation.rules.in;

import org.rulii.annotation.Description;
import org.rulii.annotation.Rule;
import org.rulii.context.RuleContext;
import org.rulii.lib.spring.util.Assert;
import org.rulii.validation.BindingSupplier;
import org.rulii.validation.BindingValidationRule;
import org.rulii.validation.RuleViolationBuilder;
import org.rulii.validation.Severity;

import java.util.Collection;
import java.util.List;

/**
 * Validation Rule to make sure the value is in the given collection.
 *
 * @author Max Arulananthan
 * @since 1.0
 *
 */
@Rule
@Description("Value must be in the given collection.")
public class InValidationRule extends BindingValidationRule {

    public static List<Class<?>> SUPPORTED_TYPES    = List.of(Object.class);

    public static final String ERROR_CODE       = "inValidationRule.errorCode";
    public static final String DEFAULT_MESSAGE  = "Value {0} not found in one of the given values {1}.";

    private final Collection<?> values;

    public InValidationRule(String bindingName, Collection<?> values) {
        this(bindingName, ERROR_CODE, Severity.ERROR, null, values);
    }

    public InValidationRule(String bindingName, String errorCode, Severity severity, String errorMessage, Collection<?> values) {
        super(bindingName, errorCode, severity, errorMessage, DEFAULT_MESSAGE);
        Assert.notNull(values, "values cannot be null.");
        this.values = values;
    }

    public InValidationRule(BindingSupplier bindingSupplier, String errorCode, Severity severity, String errorMessage, Collection<?> values) {
        super(bindingSupplier, errorCode, severity, errorMessage, DEFAULT_MESSAGE);
        Assert.notNull(values, "values cannot be null.");
        this.values = values;
    }

    @Override
    protected boolean isValid(RuleContext ruleContext, Object value) {
        return values.contains(value);
    }

    @Override
    protected void customizeViolation(RuleContext ruleContext, RuleViolationBuilder builder) {
        builder.param("values", values.toString());
    }

    @Override
    public List<Class<?>> getSupportedTypes() {
        return SUPPORTED_TYPES;
    }

    public Collection<?> getValues() {
        return values;
    }

    @Override
    public String toString() {
        return "InValidationRule{" +
                "values=" + values +
                '}';
    }
}
