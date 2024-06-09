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
package org.rulii.validation.rules.alphnumeric;

import org.rulii.annotation.Description;
import org.rulii.annotation.Rule;
import org.rulii.context.RuleContext;
import org.rulii.lib.apache.StringUtils;
import org.rulii.validation.BindingSupplier;
import org.rulii.validation.BindingValidationRule;
import org.rulii.validation.Severity;
import org.rulii.validation.ValidationRuleException;

import java.util.Arrays;
import java.util.List;

/**
 * Validation Rule to make sure the the value only contains alphanumeric letters (or spaces).
 *
 * @author Max Arulananthan
 * @since 1.0
 */
@Rule
@Description("Value can only contain unicode alphanumeric letters/spaces.")
public class AlphaNumericValidationRule extends BindingValidationRule {

    private static final List<Class<?>> SUPPORTED_TYPES = Arrays.asList(CharSequence.class);

    public static final String ERROR_CODE       = "rulii.validation.rules.AlphaNumericValidationRule.errorCode";
    public static final String DEFAULT_MESSAGE  = "Value must only contain alphanumeric letters. Given {0}.";

    private final boolean allowSpace;

    public AlphaNumericValidationRule(String bindingName) {
        this(bindingName, ERROR_CODE, Severity.ERROR, null, true);
    }

    public AlphaNumericValidationRule(String bindingName, String errorCode, Severity severity,
                                      String errorMessage, boolean allowSpace) {
        super(bindingName, errorCode, severity, errorMessage, DEFAULT_MESSAGE);
        this.allowSpace = allowSpace;
    }

    public AlphaNumericValidationRule(BindingSupplier bindingSupplier, String errorCode, Severity severity,
                                      String errorMessage, boolean allowSpace) {
        super(bindingSupplier, errorCode, severity, errorMessage, DEFAULT_MESSAGE);
        this.allowSpace = allowSpace;
    }

    @Override
    protected boolean isValid(RuleContext ruleContext, Object value) {
        if (value == null) return true;

        if (!(value instanceof CharSequence))
            throw new ValidationRuleException("AlphaNumericValidationRule only applies to CharSequences."
                    + "Supplied Class [" + value.getClass() + "] value [" + value + "]");

        return isAllowSpace() ? StringUtils.isAlphanumericSpace((CharSequence) value) : StringUtils.isAlphanumeric((CharSequence) value);
    }

    public boolean isAllowSpace() {
        return allowSpace;
    }

    @Override
    public List<Class<?>> getSupportedTypes() {
        return SUPPORTED_TYPES;
    }

    @Override
    public String toString() {
        return "AlphaNumericValidationRule{"
                + "isAllowSpace=" + isAllowSpace()
                + "}";
    }
}
