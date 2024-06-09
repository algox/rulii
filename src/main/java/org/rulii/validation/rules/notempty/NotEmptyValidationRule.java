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
package org.rulii.validation.rules.notempty;

import org.rulii.annotation.Description;
import org.rulii.annotation.Rule;
import org.rulii.context.RuleContext;
import org.rulii.validation.BindingSupplier;
import org.rulii.validation.BindingValidationRule;
import org.rulii.validation.Severity;
import org.rulii.validation.ValidationRuleException;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Validation Rule to make sure the value is not empty (ie. arrays/collections/maps/strings have at least one item).
 *
 * @author Max Arulananthan
 * @since 1.0
 */
@Rule
@Description("Value must not be empty.")
public class NotEmptyValidationRule extends BindingValidationRule {

    public static List<Class<?>> SUPPORTED_TYPES    = Arrays.asList(boolean[].class, byte[].class, char[].class, double[].class, float[].class,
            int[].class, long[].class, short[].class, Object[].class, Collection.class, Map.class, CharSequence.class);

    public static final String ERROR_CODE       = "rulii.validation.rules.NotEmptyValidationRule.errorCode";
    public static final String DEFAULT_MESSAGE  = "Value cannot be empty.";

    public NotEmptyValidationRule(String bindingName) {
        this(bindingName, ERROR_CODE, Severity.ERROR, null);
    }

    public NotEmptyValidationRule(String bindingName, String errorCode, Severity severity, String errorMessage) {
        super(bindingName, errorCode, severity, errorMessage, DEFAULT_MESSAGE);
    }

    public NotEmptyValidationRule(BindingSupplier bindingSupplier, String errorCode, Severity severity, String errorMessage) {
        super(bindingSupplier, errorCode, severity, errorMessage, DEFAULT_MESSAGE);
    }

    @Override
    protected boolean isValid(RuleContext ruleContext, Object value) {
        if (value == null) return true;

        if (value instanceof Collection) return isValid((Collection) value);
        if (value instanceof CharSequence) return isValid((CharSequence) value);
        if (value instanceof Map) return isValid((Map) value);
        if (value.getClass().isArray()) return isValidArray(value);

        throw new ValidationRuleException("NotEmptyValidationRule only applies to Collections/Maps/Arrays and CharSequences."
                + "Supplied Class [" + value.getClass() + "]");
    }

    @Override
    public List<Class<?>> getSupportedTypes() {
        return SUPPORTED_TYPES;
    }

    private boolean isValidArray(Object value) {
        int length = Array.getLength(value);
        return length > 0;
    }

    private boolean isValid(Collection collection) {
        int length = collection.size();
        return length > 0;
    }

    private boolean isValid(Map map) {
        int length = map.size();
        return length > 0;
    }

    private boolean isValid(CharSequence sequence) {
        int length = sequence.length();
        return length > 0;
    }

    @Override
    public String toString() {
        return "NotEmptyValidationRule";
    }
}
