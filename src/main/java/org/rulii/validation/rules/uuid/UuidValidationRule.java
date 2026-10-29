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
package org.rulii.validation.rules.uuid;

import org.rulii.annotation.Description;
import org.rulii.annotation.Rule;
import org.rulii.context.RuleContext;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.function.Function;
import org.rulii.validation.Severity;
import org.rulii.validation.ValidationRuleException;
import org.rulii.validation.ValueValidationRule;

import java.util.Arrays;
import java.util.List;

/**
 * Validation Rule to make sure a character sequence is a UUID in its canonical textual form:
 * 36 characters, hex digits in groups of 8-4-4-4-12 separated by dashes. Options: whether the empty
 * string passes, whether the nil UUID (all zeros) passes, the accepted versions (1 to 5 by default),
 * the accepted variants (0 to 2 by default) and the letter case of the hex digits (lower case by default).
 *
 * @author Max Arulananthan
 * @since 2.2
 */
@Rule
@Description("Value must be a UUID.")
public class UuidValidationRule extends ValueValidationRule {

    /**
     * The letter case the hex digits must be in.
     */
    public enum LetterCase {
        /** Only lower case is valid. */
        LOWER_CASE,
        /** Only upper case is valid. */
        UPPER_CASE,
        /** Either case is valid. */
        INSENSITIVE
    }

    public static final List<Class<?>> SUPPORTED_TYPES = List.of(CharSequence.class);

    public static final String ERROR_CODE       = "uuidValidationRule.errorCode";
    public static final String DEFAULT_MESSAGE  = "Value {0} must be a valid UUID.";

    public static final int[] DEFAULT_VERSIONS = {1, 2, 3, 4, 5};
    public static final int[] DEFAULT_VARIANTS = {0, 1, 2};

    private static final int LENGTH = 36;
    private static final int VERSION_INDEX = 14;
    private static final int VARIANT_INDEX = 19;

    private final boolean allowEmpty;
    private final boolean allowNil;
    private final int[] versions;
    private final int[] variants;
    private final LetterCase letterCase;

    /**
     * Creates a new builder for this validation rule.
     *
     * @param function the function that supplies the value to validate
     * @return a new {@link UuidValidationRuleBuilder}
     */
    public static UuidValidationRuleBuilder builder(Function<?> function) {
        return new UuidValidationRuleBuilder(function);
    }

    UuidValidationRule(Function<?> valueFunction, String errorCode, Severity severity, String errorMessage, String valueName,
                       boolean allowEmpty, boolean allowNil, int[] versions, int[] variants, LetterCase letterCase) {
        super(valueFunction, errorCode, severity, errorMessage, DEFAULT_MESSAGE, valueName);
        Assert.notNull(versions, "versions cannot be null.");
        Assert.notNull(variants, "variants cannot be null.");
        Assert.notNull(letterCase, "letterCase cannot be null.");
        this.allowEmpty = allowEmpty;
        this.allowNil = allowNil;
        this.versions = versions.clone();
        this.variants = variants.clone();
        this.letterCase = letterCase;
    }

    @Override
    protected boolean isValid(RuleContext ruleContext, Object value) {
        if (value == null) return false;

        if (!(value instanceof CharSequence text))
            throw new ValidationRuleException("UuidValidationRule only applies to CharSequences."
                    + "Supplied Class [" + value.getClass() + "] value [" + value + "]");

        if (text.length() == 0) return allowEmpty;
        if (text.length() != LENGTH) return false;

        boolean nil = true;

        for (int i = 0; i < LENGTH; i++) {
            char c = text.charAt(i);

            if (i == 8 || i == 13 || i == 18 || i == 23) {
                if (c != '-') return false;
                continue;
            }

            if (Character.digit(c, 16) < 0 || !caseAllowed(c)) return false;
            if (c != '0') nil = false;
        }

        if (nil) return allowNil;

        int version = Character.digit(text.charAt(VERSION_INDEX), 16);
        int variant = variant(Character.digit(text.charAt(VARIANT_INDEX), 16));

        return contains(versions, version) && contains(variants, variant);
    }

    private boolean caseAllowed(char c) {
        if (!Character.isLetter(c)) return true;
        return switch (letterCase) {
            case LOWER_CASE -> Character.isLowerCase(c);
            case UPPER_CASE -> Character.isUpperCase(c);
            case INSENSITIVE -> true;
        };
    }

    /**
     * The variant encoded in the first hex digit of the fourth group, as {@link java.util.UUID#variant()}
     * reads it: 0 for NCS (0xxx), 1 for RFC 4122 (10xx), 2 for Microsoft (110x), 3 reserved (111x).
     */
    private static int variant(int digit) {
        if (digit <= 7) return 0;
        if (digit <= 11) return 1;
        if (digit <= 13) return 2;
        return 3;
    }

    private static boolean contains(int[] values, int value) {
        for (int candidate : values) {
            if (candidate == value) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<Class<?>> getSupportedTypes() {
        return SUPPORTED_TYPES;
    }

    public boolean isAllowEmpty() {
        return allowEmpty;
    }

    public boolean isAllowNil() {
        return allowNil;
    }

    public int[] getVersions() {
        return versions.clone();
    }

    public int[] getVariants() {
        return variants.clone();
    }

    public LetterCase getLetterCase() {
        return letterCase;
    }

    @Override
    public String describe() {
        return getValueName() + " must be a valid UUID";
    }

    @Override
    public String toString() {
        return "UuidValidationRule{allowEmpty=" + allowEmpty + ", allowNil=" + allowNil + ", versions=" + Arrays.toString(versions)
                + ", variants=" + Arrays.toString(variants) + ", letterCase=" + letterCase + '}';
    }
}
