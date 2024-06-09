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
package org.rulii.validation.rules.asssert;

import org.rulii.annotation.Description;
import org.rulii.annotation.Rule;
import org.rulii.context.RuleContext;
import org.rulii.validation.BindingSupplier;
import org.rulii.validation.BindingValidationRule;
import org.rulii.validation.Severity;
import org.rulii.validation.ValidationRuleException;

import java.util.Arrays;
import java.util.List;

/**
 * Validation Rule to make sure the value is false.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
@Rule
@Description("Value must be false.")
public class AssertFalseValidationRule extends BindingValidationRule {

    private static final List<Class<?>> SUPPORTED_TYPES = Arrays.asList(boolean.class, Boolean.class);

    public static final String ERROR_CODE       = "rulii.validation.rules.AssertFalseValidationRule.errorCode";
    public static final String DEFAULT_MESSAGE  = "Value must be false.";

    public AssertFalseValidationRule(String bindingName, String path) {
        this(bindingName, ERROR_CODE, Severity.ERROR, null);
    }

    public AssertFalseValidationRule(String bindingName, String errorCode,
                                     Severity severity, String errorMessage) {
        super(bindingName, errorCode, severity, errorMessage, DEFAULT_MESSAGE);
    }

    public AssertFalseValidationRule(BindingSupplier bindingSupplier, String errorCode,
                                     Severity severity, String errorMessage) {
        super(bindingSupplier, errorCode, severity, errorMessage, DEFAULT_MESSAGE);
    }

    @Override
    protected boolean isValid(RuleContext ruleContext, Object value) {
        if (value == null) return true;

        if (!(value instanceof Boolean)) {
            throw new ValidationRuleException("AssertFalseValidationRule only applies to a boolean."
                    + "Supplied Class [" + value.getClass() + "]");
        }

        return Boolean.FALSE.equals(value);
    }

    @Override
    public List<Class<?>> getSupportedTypes() {
        return SUPPORTED_TYPES;
    }

    @Override
    public String toString() {
        return "AssertFalseValidationRule";
    }
}
