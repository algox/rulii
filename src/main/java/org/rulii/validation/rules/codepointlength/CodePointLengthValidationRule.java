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
package org.rulii.validation.rules.codepointlength;

import org.rulii.annotation.Description;
import org.rulii.annotation.Rule;
import org.rulii.context.RuleContext;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.function.Function;
import org.rulii.validation.RuleViolationBuilder;
import org.rulii.validation.Severity;
import org.rulii.validation.ValidationRuleException;
import org.rulii.validation.ValueValidationRule;

import java.text.Normalizer;
import java.util.List;

/**
 * Validation Rule to make sure the number of Unicode code points of a character sequence is within
 * bounds (both inclusive). Unlike a length check it counts supplementary characters once. The value
 * may be normalized to a {@link Normalizer.Form} before counting.
 *
 * @author Max Arulananthan
 * @since 2.2
 */
@Rule
@Description("Value's code point length must be within the given bounds.")
public class CodePointLengthValidationRule extends ValueValidationRule {

    public static final List<Class<?>> SUPPORTED_TYPES = List.of(CharSequence.class);

    public static final String ERROR_CODE       = "codePointLengthValidationRule.errorCode";
    public static final String DEFAULT_MESSAGE  = "Value {0} must be between {1} and {2} code points long.";

    private final int min;
    private final int max;
    private final Normalizer.Form normalization;

    /**
     * Creates a new builder for this validation rule.
     *
     * @param function the function that supplies the value to validate
     * @param min the minimum number of code points (inclusive)
     * @param max the maximum number of code points (inclusive)
     * @return a new {@link CodePointLengthValidationRuleBuilder}
     */
    public static CodePointLengthValidationRuleBuilder builder(Function<?> function, int min, int max) {
        return new CodePointLengthValidationRuleBuilder(function, min, max);
    }

    CodePointLengthValidationRule(Function<?> valueFunction, String errorCode, Severity severity, String errorMessage,
                                  String valueName, int min, int max, Normalizer.Form normalization) {
        super(valueFunction, errorCode, severity, errorMessage, DEFAULT_MESSAGE, valueName);
        Assert.isTrue(min >= 0, "min must be >= 0.");
        Assert.isTrue(max >= min, "max must be >= min.");
        this.min = min;
        this.max = max;
        this.normalization = normalization;
    }

    @Override
    protected boolean isValid(RuleContext ruleContext, Object value) {
        if (value == null) return false;

        if (!(value instanceof CharSequence text))
            throw new ValidationRuleException("CodePointLengthValidationRule only applies to CharSequences."
                    + "Supplied Class [" + value.getClass() + "] value [" + value + "]");

        CharSequence counted = normalization != null && text.length() > 0 ? Normalizer.normalize(text, normalization) : text;
        int length = Character.codePointCount(counted, 0, counted.length());

        return length >= min && length <= max;
    }

    @Override
    protected void customizeViolation(RuleContext ruleContext, RuleViolationBuilder builder) {
        builder.param("min", min).param("max", max);
    }

    @Override
    public List<Class<?>> getSupportedTypes() {
        return SUPPORTED_TYPES;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    /**
     * The normalization applied before counting.
     *
     * @return the form; null when the value is counted as is.
     */
    public Normalizer.Form getNormalization() {
        return normalization;
    }

    @Override
    public String toString() {
        return "CodePointLengthValidationRule{min=" + min + ", max=" + max + ", normalization=" + normalization + '}';
    }
}
