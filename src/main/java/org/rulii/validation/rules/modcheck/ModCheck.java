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
package org.rulii.validation.rules.modcheck;

import org.rulii.lib.spring.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * The arithmetic and the digit extraction shared by the check-digit rules. A value holds a run of
 * digits and one check character: by default the digits are everything but the last character and
 * the check character is the last one; a range ({@code startIndex} to {@code endIndex}, end exclusive)
 * and an explicit {@code checkDigitIndex} narrow that. Non-digit characters among the digits either
 * fail the check or are dropped first.
 *
 * @author Max Arulananthan
 * @since 2.2
 */
public final class ModCheck {

    /**
     * Marks the end index and the check digit index as "not set".
     */
    public static final int UNSET = -1;

    private ModCheck() {
        super();
    }

    /**
     * The digits and the check character of a value.
     *
     * @param digits the digits to run the checksum over, in order.
     * @param checkCharacter the character holding the check digit.
     */
    public record Extraction(List<Integer> digits, char checkCharacter) {}

    /**
     * Extracts the digits and the check character of a value.
     *
     * @param value the value.
     * @param startIndex where the digits start (inclusive).
     * @param endIndex where the digits end (exclusive); {@link Integer#MAX_VALUE} for the end of the value.
     * @param checkDigitIndex where the check character is; {@link #UNSET} for the last character of the range.
     * @param ignoreNonDigits whether non-digit characters among the digits are dropped.
     * @return the extraction; null when the value does not have the shape (out of range, or a non-digit
     *         that is not ignored).
     */
    public static Extraction extract(CharSequence value, int startIndex, int endIndex, int checkDigitIndex, boolean ignoreNonDigits) {
        Assert.notNull(value, "value cannot be null.");
        String text = value.toString();
        int length = text.length();
        int end = endIndex == Integer.MAX_VALUE ? length : endIndex;

        if (startIndex < 0 || end > length || startIndex >= end) return null;

        String digitsText;
        char check;

        if (checkDigitIndex == UNSET) {
            // The check character is the last of the range; the digits are the rest of the range.
            check = text.charAt(end - 1);
            digitsText = text.substring(startIndex, end - 1);
        } else {
            if (checkDigitIndex < 0 || checkDigitIndex >= length) return null;
            check = text.charAt(checkDigitIndex);
            digitsText = text.substring(startIndex, end);
            if (checkDigitIndex >= startIndex && checkDigitIndex < end) {
                digitsText = text.substring(startIndex, checkDigitIndex) + text.substring(checkDigitIndex + 1, end);
            }
        }

        List<Integer> digits = new ArrayList<>(digitsText.length());

        for (int i = 0; i < digitsText.length(); i++) {
            char c = digitsText.charAt(i);
            int digit = Character.digit(c, 10);

            if (digit >= 0) {
                digits.add(digit);
            } else if (!ignoreNonDigits) {
                return null;
            }
        }

        return new Extraction(digits, check);
    }

    /**
     * The Luhn check digit of the digits: every second digit from the right is doubled, digits above
     * nine reduced by nine, and the check digit brings the sum to a multiple of ten.
     *
     * @param digits the digits without the check digit.
     * @return the check digit (0 to 9).
     */
    public static int luhnCheckDigit(List<Integer> digits) {
        Assert.notNull(digits, "digits cannot be null.");
        int sum = 0;
        boolean doubled = true;

        for (int index = digits.size() - 1; index >= 0; index--) {
            int digit = digits.get(index);

            if (doubled) {
                digit *= 2;
                if (digit > 9) digit -= 9;
            }

            sum += digit;
            doubled = !doubled;
        }

        return (10 - sum % 10) % 10;
    }

    /**
     * The modulo 10 check digit of the digits: from the right, digits are multiplied alternately by
     * the multiplier (rightmost first) and the weight, and the check digit brings the sum to a
     * multiple of ten.
     *
     * @param digits the digits without the check digit.
     * @param multiplier the multiplier of the odd positions, counted from the right.
     * @param weight the multiplier of the even positions.
     * @return the check digit (0 to 9).
     */
    public static int mod10CheckDigit(List<Integer> digits, int multiplier, int weight) {
        Assert.notNull(digits, "digits cannot be null.");
        int sum = 0;
        boolean odd = true;

        for (int index = digits.size() - 1; index >= 0; index--) {
            sum += digits.get(index) * (odd ? multiplier : weight);
            odd = !odd;
        }

        return (10 - sum % 10) % 10;
    }

    /**
     * The modulo 11 check value of the digits: from the right, digits are multiplied by a weight that
     * starts at two and grows by one per digit, starting over at two past the threshold; the result is
     * eleven minus the sum modulo eleven, so it ranges from one to eleven (ten and eleven have no
     * single digit and are represented by replacement characters).
     *
     * @param digits the digits without the check digit, in processing order (rightmost processed first).
     * @param threshold the largest weight; {@link Integer#MAX_VALUE} for no limit.
     * @return the check value (1 to 11).
     */
    public static int mod11CheckValue(List<Integer> digits, int threshold) {
        Assert.notNull(digits, "digits cannot be null.");
        int sum = 0;
        int multiplier = 2;

        for (int index = digits.size() - 1; index >= 0; index--) {
            sum += digits.get(index) * multiplier++;
            if (multiplier > threshold) multiplier = 2;
        }

        return 11 - sum % 11;
    }
}
