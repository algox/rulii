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
package org.rulii.validation.rules.max;

import org.rulii.annotation.Description;
import org.rulii.annotation.Rule;
import org.rulii.context.RuleContext;
import org.rulii.lib.spring.util.Assert;
import org.rulii.util.NumberComparator;
import org.rulii.validation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Validation Rule to make sure the value is less the desired Max.
 *
 * @author Max Arulananthan
 * @since 1.0
 *
 */
@Rule
@Description("Value is less than the desired Max.")
public class DecimalMaxValidationRule extends BindingValidationRule {

    public static List<Class<?>> SUPPORTED_TYPES    = List.of(Number.class, CharSequence.class);

    public static final String ERROR_CODE       = "decimalMaxValidationRule.errorCode";
    public static final String DEFAULT_MESSAGE  = "Value {0} must not be less than or equal to {1}.";

    private final BigDecimal max;
    private final boolean inclusive;

    /**
     * Constructs a new DecimalMaxValidationRule with the specified parameters.
     *
     * @param bindingName the name of the binding
     * @param max the maximum value allowed in the validation
     * @param inclusive true if the maximum value is inclusive, false otherwise
     */
    public DecimalMaxValidationRule(String bindingName, BigDecimal max, boolean inclusive) {
        this(bindingName, ERROR_CODE, Severity.ERROR, null, max, inclusive);
    }

    /**
     * Constructs a new DecimalMaxValidationRule with the specified parameters.
     *
     * @param bindingName the name of the binding
     * @param errorCode the error code associated with the validation rule
     * @param max the maximum value allowed in the validation
     * @param inclusive true if the maximum value is inclusive, false otherwise
     */
    public DecimalMaxValidationRule(String bindingName, String errorCode, BigDecimal max, boolean inclusive) {
        this(bindingName, errorCode, Severity.ERROR, null, max, inclusive);
    }

    /**
     * Initializes a DecimalMaxValidationRule with the specified parameters.
     *
     * @param bindingName   the name of the binding
     * @param errorCode     the error code associated with the validation rule
     * @param severity      the severity of the error
     * @param errorMessage  the error message to be displayed in case of validation failure
     * @param max           the maximum value allowed in the validation
     * @param inclusive     true if the maximum value is inclusive, false otherwise
     */
    public DecimalMaxValidationRule(String bindingName, String errorCode, Severity severity,
                                    String errorMessage, BigDecimal max, boolean inclusive) {
        super(bindingName, errorCode, severity, errorMessage, DEFAULT_MESSAGE);
        Assert.notNull(max, "max cannot be null.");
        this.max = max;
        this.inclusive = inclusive;
    }

    /**
     * Represents a validation rule for validating decimal values against a maximum threshold.
     *
     * @param bindingSupplier  The supplier of bindings for rule evaluation. Must not be null.
     * @param errorCode        The error code associated with the validation rule.
     * @param severity         The severity of the error.
     * @param errorMessage     The error message to be displayed if the validation fails.
     * @param max             The maximum value allowed in the validation. Must not be null.
     * @param inclusive        True if the maximum value is inclusive, false otherwise.
     */
    public DecimalMaxValidationRule(BindingSupplier bindingSupplier, String errorCode, Severity severity,
                                    String errorMessage, BigDecimal max, boolean inclusive) {
        super(bindingSupplier, errorCode, severity, errorMessage, DEFAULT_MESSAGE);
        Assert.notNull(max, "max cannot be null.");
        this.max = max;
        this.inclusive = inclusive;
    }

    @Override
    protected boolean isValid(RuleContext ruleContext, Object value) {

        if (value == null) return true;

        Number number = null;

        if (value instanceof Number) number = (Number) value;
        if (value instanceof CharSequence) {
            try {
                number = new BigDecimal(value.toString());
            } catch (NumberFormatException e) {
                return false;
            }
        }

        if (number == null)
            throw new ValidationRuleException("DecimalMaxValidationRule only applies to Numbers/CharSequences."
                    + "Supplied Class [" + value.getClass() + "] value [" + value + "]");

        Integer result = NumberComparator.compare(number, max);
        return result == null || (isInclusive() ? result <= 0 : result < 0);
    }

    @Override
    protected void customizeViolation(RuleContext ruleContext, RuleViolationBuilder builder) {
        builder.param("max", max);
    }

    public BigDecimal getMax() {
        return max;
    }

    public boolean isInclusive() {
        return inclusive;
    }

    @Override
    public List<Class<?>> getSupportedTypes() {
        return SUPPORTED_TYPES;
    }

    @Override
    public String toString() {
        return "DecimalMaxValidationRule{" +
                "max=" + max +
                ", inclusive=" + inclusive +
                '}';
    }
}
