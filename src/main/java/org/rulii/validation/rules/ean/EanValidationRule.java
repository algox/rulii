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
package org.rulii.validation.rules.ean;

import org.rulii.annotation.Description;
import org.rulii.annotation.Rule;
import org.rulii.context.RuleContext;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.function.Function;
import org.rulii.validation.RuleViolationBuilder;
import org.rulii.validation.Severity;
import org.rulii.validation.ValidationRuleException;
import org.rulii.validation.ValueValidationRule;
import org.rulii.validation.rules.modcheck.ModCheck;

import java.util.ArrayList;
import java.util.List;

/**
 * Validation Rule to make sure a character sequence is an EAN barcode number: 13 digits (or 8 for
 * EAN-8) whose last digit is the modulo 10 check digit with weights 3 and 1.
 *
 * @author Max Arulananthan
 * @since 2.2
 */
@Rule
@Description("Value must be an EAN barcode number.")
public class EanValidationRule extends ValueValidationRule {

    /**
     * The EAN format.
     */
    public enum Type {
        /** Thirteen digits. */
        EAN13(13),
        /** Eight digits. */
        EAN8(8);

        private final int length;

        Type(int length) {
            this.length = length;
        }

        public int getLength() {
            return length;
        }
    }

    public static final List<Class<?>> SUPPORTED_TYPES = List.of(CharSequence.class);

    public static final String ERROR_CODE       = "eanValidationRule.errorCode";
    public static final String DEFAULT_MESSAGE  = "Value {0} is not a valid {1} barcode.";

    private final Type type;

    /**
     * Creates a new builder for this validation rule.
     *
     * @param function the function that supplies the value to validate
     * @return a new {@link EanValidationRuleBuilder}
     */
    public static EanValidationRuleBuilder builder(Function<?> function) {
        return new EanValidationRuleBuilder(function);
    }

    EanValidationRule(Function<?> valueFunction, String errorCode, Severity severity, String errorMessage, String valueName, Type type) {
        super(valueFunction, errorCode, severity, errorMessage, DEFAULT_MESSAGE, valueName);
        Assert.notNull(type, "type cannot be null.");
        this.type = type;
    }

    @Override
    protected boolean isValid(RuleContext ruleContext, Object value) {
        if (value == null) return false;

        if (!(value instanceof CharSequence text))
            throw new ValidationRuleException("EanValidationRule only applies to CharSequences."
                    + "Supplied Class [" + value.getClass() + "] value [" + value + "]");

        if (text.length() != type.getLength()) return false;

        List<Integer> digits = new ArrayList<>(text.length());

        for (int i = 0; i < text.length(); i++) {
            int digit = Character.digit(text.charAt(i), 10);
            if (digit < 0) return false;
            digits.add(digit);
        }

        int check = digits.remove(digits.size() - 1);
        return check == ModCheck.mod10CheckDigit(digits, 3, 1);
    }

    @Override
    protected void customizeViolation(RuleContext ruleContext, RuleViolationBuilder builder) {
        builder.param("type", type);
    }

    @Override
    public List<Class<?>> getSupportedTypes() {
        return SUPPORTED_TYPES;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String describe() {
        return getValueName() + " must be a valid " + type + " barcode";
    }

    @Override
    public String toString() {
        return "EanValidationRule{type=" + type + '}';
    }
}
