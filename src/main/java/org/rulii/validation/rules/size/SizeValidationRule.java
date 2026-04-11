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
package org.rulii.validation.rules.size;

import org.rulii.annotation.Description;
import org.rulii.annotation.Rule;
import org.rulii.context.RuleContext;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.function.Function;
import org.rulii.validation.RuleViolationBuilder;
import org.rulii.validation.Severity;
import org.rulii.validation.ValidationRuleException;
import org.rulii.validation.ValueValidationRule;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Validation Rule to make sure the size is between the given min and max values.
 *
 * @author Max Arulananthan
 * @since 1.0
 *
 */
@Rule
@Description("Size is between the given min and max values.")
public class SizeValidationRule extends ValueValidationRule {

    public static List<Class<?>> SUPPORTED_TYPES    = List.of(boolean[].class, byte[].class, char[].class, double[].class, float[].class,
            int[].class, long[].class, short[].class, Object[].class, Collection.class, Map.class, CharSequence.class);

    public static final String ERROR_CODE       = "sizeValidationRule.errorCode";
    public static final String DEFAULT_MESSAGE  = "Value {0} must be between {1} and {2}.";

    private final int min;
    private final int max;

    /**
     * Creates a new builder for this validation rule.
     *
     * @param function the function that supplies the value to validate
     * @param min      the minimum size (inclusive)
     * @param max      the maximum size (inclusive)
     * @return a new {@link SizeValidationRuleBuilder}
     */
    public static SizeValidationRuleBuilder builder(Function<?> function, int min, int max) {
        return new SizeValidationRuleBuilder(function, min, max);
    }

    SizeValidationRule(Function<?> valueFunction, String errorCode, Severity severity,
                       String errorMessage, String valueName, int min, int max) {
        super(valueFunction, errorCode, severity, errorMessage, DEFAULT_MESSAGE, valueName);
        Assert.isTrue(min >= 0, "min >= 0");
        Assert.isTrue(max >= 0, "max >= 0");
        Assert.isTrue(max >= min, "max >= min");
        this.min = min;
        this.max = max;
    }

    @Override
    public boolean isSupported(Class<?> type) {
        return Collection.class.isAssignableFrom(type) || CharSequence.class.isAssignableFrom(type)
                || Map.class.isAssignableFrom(type) || type.isArray();
    }

    @Override
    protected boolean isValid(RuleContext ruleContext, Object value) {
        if (value == null) return false;

        Integer size = null;

        if (value instanceof CharSequence) size = getSize((CharSequence) value);
        if (value instanceof Collection) size = getSize((Collection<?>) value);
        if (value instanceof Map) size = getSize((Map<?,?>) value);
        if (value.getClass().isArray()) size = getSize(value);

        if (size == null) {
            throw new ValidationRuleException("SizeValidationRule only applies to Collections/Maps/CharSequences and Arrays."
                    + "Supplied Class [" + value.getClass() + "]");
        }

        return size >= min && size <= max;
    }

    @Override
    protected void customizeViolation(RuleContext ruleContext, RuleViolationBuilder builder) {
        builder
                .param("min", min)
                .param("max", max);
    }

    private int getSize(Object value) {
        return Array.getLength(value);
    }

    private int getSize(Collection<?> collection) {
        return collection.size();
    }

    private int getSize(Map<?,?> map) {
        return map.size();
    }

    private int getSize(CharSequence sequence) {
        return sequence.length();
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    @Override
    public List<Class<?>> getSupportedTypes() {
        return SUPPORTED_TYPES;
    }

    @Override
    public String toString() {
        return "SizeValidationRule{" +
                "min=" + min +
                ", max=" + max +
                '}';
    }
}
