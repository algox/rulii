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
package org.rulii.validation.rules.digits;

import org.rulii.annotation.Description;
import org.rulii.annotation.Rule;
import org.rulii.context.RuleContext;
import org.rulii.lib.spring.util.Assert;
import org.rulii.validation.BindingSupplier;
import org.rulii.validation.BindingValidationRule;
import org.rulii.validation.RuleViolationBuilder;
import org.rulii.validation.Severity;
import org.rulii.validation.ValidationRuleException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Validation Rule to make sure the the value must be within in range of maximum number of integral digits and
 * maximum number of fractional digits.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
@Rule
@Description("Value must be within range of the maximum integral digits and maximum fraction digits.")
public class DigitsValidationRule extends BindingValidationRule {

    private static final List<Class<?>> SUPPORTED_TYPES = Arrays.asList(Number.class, CharSequence.class);

    public static final String ERROR_CODE       = "rulii.validation.rules.DigitsValidationRule.errorCode";
    public static final String DEFAULT_MESSAGE  = "Value must have at most {1} integral digits and {2} fraction digits. Given {0}.";

    private final int maxIntegerLength;
    private final int maxFractionLength;

    public DigitsValidationRule(String bindingName, int maxIntegerLength, int maxFractionLength) {
        this(bindingName, ERROR_CODE, Severity.ERROR, null, maxIntegerLength, maxFractionLength);
    }

    public DigitsValidationRule(String bindingName, String errorCode, Severity severity, String errorMessage,
                                int maxIntegerLength, int maxFractionLength) {
        super(bindingName, errorCode, severity, errorMessage, DEFAULT_MESSAGE);
        Assert.isTrue(maxFractionLength > 0, "maxFractionLength must be > 0");
        Assert.isTrue(maxIntegerLength > 0, "maxIntegerLength must be > 0");
        this.maxIntegerLength = maxIntegerLength;
        this.maxFractionLength = maxFractionLength;
    }

    public DigitsValidationRule(BindingSupplier bindingSupplier, String errorCode, Severity severity,
                                String errorMessage, int maxIntegerLength, int maxFractionLength) {
        super(bindingSupplier, errorCode, severity, errorMessage, DEFAULT_MESSAGE);
        this.maxIntegerLength = maxIntegerLength;
        this.maxFractionLength = maxFractionLength;
    }

    @Override
    protected boolean isValid(RuleContext ruleContext, Object value) {
        if (value == null) return true;

        BigDecimal number = null;

        if (value instanceof BigDecimal ) {
            number = (BigDecimal) value;
        } else if (value instanceof Number || value instanceof CharSequence){
            try {
                number = new BigDecimal(value.toString());
                number = number.stripTrailingZeros();
            } catch (NumberFormatException e) {
                return false;
            }
        }

        if (number == null) {
            throw new ValidationRuleException("DigitsValidationRule only applies to Numbers/CharSequences."
                    + "Supplied Class [" + value.getClass() + "] value [" + value + "]");
        }


        int integerLength = number.precision() - number.scale();
        int fractionLength = number.scale() < 0 ? 0 : number.scale();

        return (maxIntegerLength >= integerLength && maxFractionLength >= fractionLength);
    }

    @Override
    protected void customizeViolation(RuleContext ruleContext, RuleViolationBuilder builder) {
        builder
                .param("maxIntegerLength", maxIntegerLength)
                .param("maxFractionLength", maxFractionLength);
    }

    public int getMaxIntegerLength() {
        return maxIntegerLength;
    }

    public int getMaxFractionLength() {
        return maxFractionLength;
    }

    @Override
    public List<Class<?>> getSupportedTypes() {
        return SUPPORTED_TYPES;
    }

    @Override
    public String toString() {
        return "DigitsValidationRule{" +
                "maxIntegerLength=" + maxIntegerLength +
                ", maxFractionLength=" + maxFractionLength +
                '}';
    }
}
