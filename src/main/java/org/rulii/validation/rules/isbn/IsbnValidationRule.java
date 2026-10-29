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
package org.rulii.validation.rules.isbn;

import org.rulii.annotation.Description;
import org.rulii.annotation.Rule;
import org.rulii.context.RuleContext;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.function.Function;
import org.rulii.validation.RuleViolationBuilder;
import org.rulii.validation.Severity;
import org.rulii.validation.ValidationRuleException;
import org.rulii.validation.ValueValidationRule;

import java.util.List;

/**
 * Validation Rule to make sure a character sequence is an ISBN: ten characters whose weighted sum
 * (weights 10 down to 1, {@code X} standing for ten in the last position) is a multiple of eleven, or
 * thirteen digits whose weighted sum (weights 1 and 3 alternating) is a multiple of ten. Separators and
 * any other character that is not a digit or {@code X} are ignored, so {@code 978-0-306-40615-7} is
 * accepted.
 *
 * @author Max Arulananthan
 * @since 2.2
 */
@Rule
@Description("Value must be an ISBN.")
public class IsbnValidationRule extends ValueValidationRule {

    /**
     * The ISBN format.
     */
    public enum Type {
        /** Ten characters. */
        ISBN_10,
        /** Thirteen digits. */
        ISBN_13,
        /** Either, decided by the length. */
        ANY
    }

    public static final List<Class<?>> SUPPORTED_TYPES = List.of(CharSequence.class);

    public static final String ERROR_CODE       = "isbnValidationRule.errorCode";
    public static final String DEFAULT_MESSAGE  = "Value {0} is not a valid ISBN.";

    private final Type type;

    /**
     * Creates a new builder for this validation rule.
     *
     * @param function the function that supplies the value to validate
     * @return a new {@link IsbnValidationRuleBuilder}
     */
    public static IsbnValidationRuleBuilder builder(Function<?> function) {
        return new IsbnValidationRuleBuilder(function);
    }

    IsbnValidationRule(Function<?> valueFunction, String errorCode, Severity severity, String errorMessage, String valueName, Type type) {
        super(valueFunction, errorCode, severity, errorMessage, DEFAULT_MESSAGE, valueName);
        Assert.notNull(type, "type cannot be null.");
        this.type = type;
    }

    @Override
    protected boolean isValid(RuleContext ruleContext, Object value) {
        if (value == null) return false;

        if (!(value instanceof CharSequence text))
            throw new ValidationRuleException("IsbnValidationRule only applies to CharSequences."
                    + "Supplied Class [" + value.getClass() + "] value [" + value + "]");

        String digits = strip(text);

        return switch (type) {
            case ISBN_10 -> isbn10(digits);
            case ISBN_13 -> isbn13(digits);
            case ANY -> digits.length() == 10 ? isbn10(digits) : isbn13(digits);
        };
    }

    private static String strip(CharSequence text) {
        StringBuilder result = new StringBuilder(text.length());

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c >= '0' && c <= '9') {
                result.append(c);
            } else if (c == 'X' || c == 'x') {
                result.append('X');
            }
        }

        return result.toString();
    }

    private static boolean isbn10(String digits) {
        if (digits.length() != 10) return false;

        int sum = 0;

        for (int i = 0; i < 10; i++) {
            char c = digits.charAt(i);
            int digit;

            if (c == 'X') {
                if (i != 9) return false;
                digit = 10;
            } else {
                digit = c - '0';
            }

            sum += digit * (10 - i);
        }

        return sum % 11 == 0;
    }

    private static boolean isbn13(String digits) {
        if (digits.length() != 13) return false;

        int sum = 0;

        for (int i = 0; i < 13; i++) {
            char c = digits.charAt(i);
            if (c == 'X') return false;
            sum += (c - '0') * (i % 2 == 0 ? 1 : 3);
        }

        return sum % 10 == 0;
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
        return getValueName() + " must be a valid " + type + " number";
    }

    @Override
    public String toString() {
        return "IsbnValidationRule{type=" + type + '}';
    }
}
