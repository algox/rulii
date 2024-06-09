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
package org.rulii.validation.rules.email;

import org.rulii.annotation.Description;
import org.rulii.annotation.Rule;
import org.rulii.context.RuleContext;
import org.rulii.lib.apache.validation.EmailValidator;
import org.rulii.validation.BindingSupplier;
import org.rulii.validation.BindingValidationRule;
import org.rulii.validation.Severity;
import org.rulii.validation.ValidationRuleException;

import java.util.Arrays;
import java.util.List;

/**
 * Validation Rule to make sure the the value must match an email regex format.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
@Rule
@Description("Value must match an email regex pattern.")
public class EmailValidationRule extends BindingValidationRule {

    private static final List<Class<?>> SUPPORTED_TYPES = Arrays.asList(CharSequence.class);

    public static final String ERROR_CODE       = "rulii.validation.rules.EmailValidationRule.errorCode";
    public static final String DEFAULT_MESSAGE  = "Value not a valid email address. Given {0}.";

    private final boolean allowLocal;
    private final boolean allowTopLevelDomain;
    private final EmailValidator validator;

    public EmailValidationRule(String bindingName, boolean allowLocal, boolean allowTopLevelDomain) {
        this(bindingName, ERROR_CODE, Severity.ERROR, null, allowLocal, allowTopLevelDomain);
    }

    public EmailValidationRule(String bindingName, String errorCode, Severity severity, String errorMessage,
                               boolean allowLocal, boolean allowTopLevelDomain) {
        super(bindingName, errorCode, severity, errorMessage, DEFAULT_MESSAGE);
        this.allowLocal = allowLocal;
        this.allowTopLevelDomain = allowTopLevelDomain;
        this.validator = EmailValidator.getInstance(allowLocal, allowTopLevelDomain);
    }

    public EmailValidationRule(BindingSupplier bindingSupplier, String errorCode, Severity severity,
                               String errorMessage, boolean allowLocal, boolean allowTopLevelDomain) {
        super(bindingSupplier, errorCode, severity, errorMessage, DEFAULT_MESSAGE);
        this.allowLocal = allowLocal;
        this.allowTopLevelDomain = allowTopLevelDomain;
        this.validator = EmailValidator.getInstance(allowLocal, allowTopLevelDomain);
    }

    @Override
    protected boolean isValid(RuleContext ruleContext, Object value) {
        if (value == null) return true;

        if (!(value instanceof CharSequence))
            throw new ValidationRuleException("EmailValidationRule only applies to CharSequences."
                    + "Supplied Class [" + value.getClass() + "] value [" + value + "]");

        return validator.isValid(value.toString());
    }

    public boolean isAllowLocal() {
        return allowLocal;
    }

    public boolean isAllowTopLevelDomain() {
        return allowTopLevelDomain;
    }

    @Override
    public List<Class<?>> getSupportedTypes() {
        return SUPPORTED_TYPES;
    }

    @Override
    public String toString() {
        return "EmailValidationRule{" +
                "allowLocal=" + allowLocal +
                ", allowTopLevelDomain=" + allowTopLevelDomain +
                '}';
    }
}
