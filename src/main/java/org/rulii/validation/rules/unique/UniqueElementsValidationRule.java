/*
 * This software is licensed under the Apache 2 license, quoted below.
 *
 * Copyright (c) 1999-2026, Algorithmx Inc.
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
package org.rulii.validation.rules.unique;

import org.rulii.annotation.Description;
import org.rulii.annotation.Rule;
import org.rulii.context.RuleContext;
import org.rulii.model.function.Function;
import org.rulii.validation.Severity;
import org.rulii.validation.ValidationRuleException;
import org.rulii.validation.ValueValidationRule;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Validation Rule to make sure a collection contains no element twice. Two elements are the same
 * when they are equal ({@link Object#equals(Object)}); a null element counts as an element.
 *
 * @author Max Arulananthan
 * @since 2.2
 */
@Rule
@Description("Collection must only contain unique elements.")
public class UniqueElementsValidationRule extends ValueValidationRule {

    public static final List<Class<?>> SUPPORTED_TYPES = List.of(Collection.class);

    public static final String ERROR_CODE       = "uniqueElementsValidationRule.errorCode";
    public static final String DEFAULT_MESSAGE  = "Value {0} must only contain unique elements.";

    /**
     * Creates a new builder for this validation rule.
     *
     * @param function the function that supplies the collection to validate
     * @return a new {@link UniqueElementsValidationRuleBuilder}
     */
    public static UniqueElementsValidationRuleBuilder builder(Function<?> function) {
        return new UniqueElementsValidationRuleBuilder(function);
    }

    UniqueElementsValidationRule(Function<?> valueFunction, String errorCode, Severity severity, String errorMessage, String valueName) {
        super(valueFunction, errorCode, severity, errorMessage, DEFAULT_MESSAGE, valueName);
    }

    @Override
    protected boolean isValid(RuleContext ruleContext, Object value) {
        if (value == null) return false;

        if (!(value instanceof Collection<?> collection))
            throw new ValidationRuleException("UniqueElementsValidationRule only applies to Collections."
                    + "Supplied Class [" + value.getClass() + "] value [" + value + "]");

        Set<Object> seen = new HashSet<>(Math.max(16, collection.size() * 2));

        for (Object element : collection) {
            if (!seen.add(element)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public List<Class<?>> getSupportedTypes() {
        return SUPPORTED_TYPES;
    }

    @Override
    public String toString() {
        return "UniqueElementsValidationRule";
    }
}
