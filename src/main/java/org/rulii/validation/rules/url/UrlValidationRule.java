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
package org.rulii.validation.rules.url;

import org.rulii.annotation.Description;
import org.rulii.annotation.Rule;
import org.rulii.context.RuleContext;
import org.rulii.lib.apache.validation.UrlValidator;
import org.rulii.validation.BindingValidationRule;
import org.rulii.validation.Severity;
import org.rulii.validation.ValidationRuleException;

import java.util.List;

/**
 * Validation Rule to make sure the value must match a Url regex format.
 *
 * @author Max Arulananthan
 * @since 1.0
 *
 */
@Rule
@Description("Value must match a Url regex pattern.")
public class UrlValidationRule extends BindingValidationRule {

    public static List<Class<?>> SUPPORTED_TYPES    = List.of(CharSequence.class);

    public static final String ERROR_CODE       = "urlValidationRule.errorCode";
    public static final String DEFAULT_MESSAGE  = "Value {0} is not a valid Url";

    private final UrlValidator validator;

    public UrlValidationRule(String bindingName) {
        this(bindingName, ERROR_CODE, Severity.ERROR, null);
    }

    public UrlValidationRule(String bindingName, String errorCode, Severity severity, String errorMessage) {
        super(bindingName, errorCode, severity, errorMessage, DEFAULT_MESSAGE);
        this.validator = new UrlValidator();
    }

    @Override
    protected boolean isValid(RuleContext ruleContext, Object value) {

        if (value == null) return true;

        if (!(value instanceof CharSequence))
            throw new ValidationRuleException("UrlValidationRule only applies to CharSequences(eg: Strings)."
                    + "Supplied Class [" + value.getClass() + "] value [" + value + "]");

        return validator.isValid(value.toString());
    }

    @Override
    public List<Class<?>> getSupportedTypes() {
        return SUPPORTED_TYPES;
    }

    @Override
    public String toString() {
        return "UrlValidationRule";
    }
}
