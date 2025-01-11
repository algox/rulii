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
package org.rulii.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * The {@code NumberComparator} class provides static methods for comparing numbers of different types.
 *
 * This class contains methods for comparing whole numbers and decimal numbers.
 * It also includes a method for determining the signum of a number.
 *
 * Example usage:
 * <pre>
 *     NumberComparator.compare(5, 10); // returns -1
 *     NumberComparator.compare(3.14, BigDecimal.valueOf(3.14)); // returns 0
 *     NumberComparator.signum(-7); // returns -1
 * </pre>
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public final class NumberComparator {

    private static final Map<Class<?>, WholeNumberComparator<?>> numberComparators = new HashMap<>();
    private static final Map<Class<?>, DecimalComparator<?>> decimalComparators = new HashMap<>();
    private static final Map<Class<?>, NumberSignum<?>> signumComparators = new HashMap<>();

    static {
        numberComparators.put(Byte.class, (WholeNumberComparator<Byte>) NumberComparator::compareByte);
        numberComparators.put(byte.class, (WholeNumberComparator<Byte>) NumberComparator::compareByte);
        numberComparators.put(BigDecimal.class, (WholeNumberComparator<BigDecimal>) NumberComparator::compareBigDecimal);
        numberComparators.put(BigInteger.class, (WholeNumberComparator<BigInteger>) NumberComparator::compareBigInteger);
        numberComparators.put(Double.class, (WholeNumberComparator<Double>) NumberComparator::compareDouble);
        numberComparators.put(double.class, (WholeNumberComparator<Double>) NumberComparator::compareDouble);
        numberComparators.put(Float.class, (WholeNumberComparator<Float>) NumberComparator::compareFloat);
        numberComparators.put(float.class, (WholeNumberComparator<Float>) NumberComparator::compareFloat);
        numberComparators.put(Integer.class, (WholeNumberComparator<Integer>) NumberComparator::compareInteger);
        numberComparators.put(int.class, (WholeNumberComparator<Integer>) NumberComparator::compareInteger);
        numberComparators.put(Long.class, (WholeNumberComparator<Long>) NumberComparator::compareLong);
        numberComparators.put(long.class, (WholeNumberComparator<Long>) NumberComparator::compareLong);
        numberComparators.put(Short.class, (WholeNumberComparator<Short>) NumberComparator::compareShort);
        numberComparators.put(short.class, (WholeNumberComparator<Short>) NumberComparator::compareShort);

        decimalComparators.put(Byte.class, (DecimalComparator<Byte>) NumberComparator::compareByte);
        decimalComparators.put(byte.class, (DecimalComparator<Byte>) NumberComparator::compareByte);
        decimalComparators.put(BigDecimal.class, (DecimalComparator<BigDecimal>) NumberComparator::compareBigDecimal);
        decimalComparators.put(BigInteger.class, (DecimalComparator<BigInteger>) NumberComparator::compareBigInteger);
        decimalComparators.put(Double.class, (DecimalComparator<Double>) NumberComparator::compareDouble);
        decimalComparators.put(double.class, (DecimalComparator<Double>) NumberComparator::compareDouble);
        decimalComparators.put(Float.class, (DecimalComparator<Float>) NumberComparator::compareFloat);
        decimalComparators.put(float.class, (DecimalComparator<Float>) NumberComparator::compareFloat);
        decimalComparators.put(Integer.class, (DecimalComparator<Integer>) NumberComparator::compareInteger);
        decimalComparators.put(int.class, (DecimalComparator<Integer>) NumberComparator::compareInteger);
        decimalComparators.put(Long.class, (DecimalComparator<Long>) NumberComparator::compareLong);
        decimalComparators.put(long.class, (DecimalComparator<Long>) NumberComparator::compareLong);
        decimalComparators.put(Short.class, (DecimalComparator<Short>) NumberComparator::compareShort);
        decimalComparators.put(short.class, (DecimalComparator<Short>) NumberComparator::compareShort);

        signumComparators.put(Byte.class, (NumberSignum<Byte>) NumberComparator::signum);
        signumComparators.put(byte.class, (NumberSignum<Byte>) NumberComparator::signum);
        signumComparators.put(BigDecimal.class, (NumberSignum<BigDecimal>) NumberComparator::signum);
        signumComparators.put(BigInteger.class, (NumberSignum<BigInteger>) NumberComparator::signum);
        signumComparators.put(Double.class, (NumberSignum<Double>) NumberComparator::signum);
        signumComparators.put(double.class, (NumberSignum<Double>) NumberComparator::signum);
        signumComparators.put(Float.class, (NumberSignum<Float>) NumberComparator::signum);
        signumComparators.put(float.class, (NumberSignum<Float>) NumberComparator::signum);
        signumComparators.put(Integer.class, (NumberSignum<Integer>) NumberComparator::signum);
        signumComparators.put(int.class, (NumberSignum<Integer>) NumberComparator::signum);
        signumComparators.put(Long.class, (NumberSignum<Long>) NumberComparator::signum);
        signumComparators.put(long.class, (NumberSignum<Long>) NumberComparator::signum);
        signumComparators.put(Short.class, (NumberSignum<Short>) NumberComparator::signum);
        signumComparators.put(short.class, (NumberSignum<Short>) NumberComparator::signum);
    }

    private NumberComparator() {
        super();
    }

    /**
     * Compares a given Number object with a long value.
     *
     * @param number the Number object to compare
     * @param value  the long value to compare with
     * @return an Integer representing the result of the comparison
     *         Returns null if the number is NaN
     *         Returns -1 if the number is negative infinity
     *         Returns 1 if the number is positive infinity
     *         Returns the result of Double.compare(number, value) otherwise
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Integer compare(Number number, long value) {
        WholeNumberComparator comparator = numberComparators.get(number.getClass());
        return comparator != null
                ? comparator.compare(number, value)
                : compareDouble(number.doubleValue(), value);
    }

    /**
     * Compares a given Number object with a BigDecimal value.
     *
     * @param number the Number object to compare
     * @param value  the BigDecimal value to compare with
     * @return an Integer representing the result of the comparison
     * Returns null if the number is NaN
     * Returns -1 if the number is negative infinity
     * Returns 1 if the number is positive infinity
     * Returns the result of BigDecimal.valueOf(number).compareTo(value) otherwise
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Integer compare(Number number, BigDecimal value) {
        DecimalComparator comparator = decimalComparators.get(number.getClass());
        return comparator != null
                ? comparator.compare(number, value)
                : compareDouble(number.doubleValue(), value);
    }

    /**
     * Returns the signum function of the specified number. The signum function
     * returns -1 if the number is negative, 0 if the number is zero, and 1 if the
     * number is positive.
     *
     * @param number the number to compute the signum function for
     * @return -1 if the number is negative,
     *         0 if the number is zero,
     *         1 if the number is positive
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Integer signum(Number number) {
        NumberSignum comparator = signumComparators.get(number.getClass());
        return comparator != null
                ? comparator.signum(number)
                : Long.signum(number.longValue());
    }

    /**
     * Compares a given long number with a BigDecimal value.
     *
     * @param number the long number to compare
     * @param value  the BigDecimal value to compare with
     * @return an int representing the result of the comparison
     */
    public static int compare(long number, BigDecimal value) {
        return BigDecimal.valueOf(number).compareTo(value);
    }

    /**
     * Compares a Byte object with a long value.
     *
     * @param number the Byte object to compare
     * @param value the long value to compare with
     * @return the value 0 if the Byte is equal to the long value; a value less than 0 if the Byte is less than the long value;
     *         a value greater than 0 if the Byte is greater than the long value
     */
    public static int compareByte(Byte number, long value) {
        return Long.compare(number, value);
    }

    /**
     * Compares a Byte number with a BigDecimal value.
     *
     * @param number the Byte number to be compared
     * @param value the BigDecimal value to be compared with
     * @return the result of the comparison, 0 if numbers are equal, -1 if the Byte number is smaller, 1 if the Byte number is bigger
     */
    public static int compareByte(Byte number, BigDecimal value) {
        return new BigDecimal(number).compareTo(value);
    }

    /**
     * Compares a given BigDecimal object with a long value.
     *
     * @param number the BigDecimal object to compare
     * @param value the long value to compare with
     * @return an int representing the result of the comparison
     *         Returns a negative value if the number is less than the value
     *         Returns 0 if the number is equal to the value
     *         Returns a positive value if the number is greater than the value
     */
    public static int compareBigDecimal(BigDecimal number, long value) {
        return number.compareTo(BigDecimal.valueOf(value));
    }

    /**
     * Compares two BigDecimal objects and returns the result of the comparison.
     *
     * @param number the first BigDecimal object to compare
     * @param value  the second BigDecimal object to compare
     * @return an int representing the result of the comparison
     *         Returns a negative value if the first BigDecimal is less than the second BigDecimal
     *         Returns 0 if the first BigDecimal is equal to the second BigDecimal
     *         Returns a positive value if the first BigDecimal is greater than the second BigDecimal
     */
    public static int compareBigDecimal(BigDecimal number, BigDecimal value) {
        return number.compareTo(value);
    }

    /**
     * Compares a BigInteger object with a long value.
     *
     * @param number the BigInteger object to compare
     * @param value  the long value to compare with
     * @return an int representing the result of the comparison
     *         Returns a negative value if the number is less than the value
     *         Returns 0 if the number is equal to the value
     *         Returns a positive value if the number is greater than the value
     */
    public static int compareBigInteger(BigInteger number, long value) {
        return number.compareTo(BigInteger.valueOf(value));
    }

    /**
     * Compares a given BigInteger object with a BigDecimal value.
     *
     * @param number the BigInteger object to compare
     * @param value  the BigDecimal value to compare with
     * @return an int representing the result of the comparison
     */
    public static int compareBigInteger(BigInteger number, BigDecimal value) {
        return new BigDecimal(number).compareTo(value);
    }

    /**
     * Compares a given Double number with a long value.
     *
     * @param number the Double number to compare
     * @param value  the long value to compare with
     * @return an Integer representing the result of the comparison
     *         Returns null if the number is NaN
     *         Returns -1 if the number is negative infinity
     *         Returns 1 if the number is positive infinity
     *         Returns the result of Double.compare(number, value) otherwise
     */
    public static Integer compareDouble(Double number, long value) {
        if (number.isNaN()) return null;
        if (number == Double.NEGATIVE_INFINITY) return -1;
        if (number == Double.POSITIVE_INFINITY) return 1;
        return Double.compare(number, value);
    }

    /**
     * Compares a given Double number with a BigDecimal value.
     *
     * @param number the Double number to compare
     * @param value  the BigDecimal value to compare with
     * @return an Integer representing the result of the comparison
     *         Returns null if the number is NaN
     *         Returns -1 if the number is negative infinity
     *         Returns 1 if the number is positive infinity
     *         Returns the result of BigDecimal.valueOf(number).compareTo(value) otherwise
     */
    public static Integer compareDouble(Double number, BigDecimal value) {
        if (number.isNaN()) return null;
        if (number == Double.NEGATIVE_INFINITY) return -1;
        if (number == Double.POSITIVE_INFINITY) return 1;
        return BigDecimal.valueOf(number).compareTo(value);
    }

    /**
     * Compares a given Float number with a long value.
     *
     * @param number the Float number to compare
     * @param value  the long value to compare with
     * @return an Integer representing the result of the comparison
     *         Returns null if the number is NaN
     *         Returns -1 if the number is negative infinity
     *         Returns 1 if the number is positive infinity
     *         Returns the result of Float.compare(number, value) otherwise
     */
    public static Integer compareFloat(Float number, long value) {
        if (number.isNaN()) return null;
        if (number == Float.NEGATIVE_INFINITY) return -1;
        if (number == Float.POSITIVE_INFINITY) return 1;
        return Float.compare(number, value);
    }

    /**
     * Compares a given Float number with a BigDecimal value.
     *
     * @param number the Float number to compare
     * @param value  the BigDecimal value to compare with
     * @return an Integer representing the result of the comparison
     * Returns null if the number is NaN
     * Returns -1 if the number is negative infinity
     * Returns 1 if the number is positive infinity
     * Returns the result of BigDecimal.valueOf(number).compareTo(value) otherwise
     */
    public static Integer compareFloat(Float number, BigDecimal value) {
        if (number.isNaN()) return null;
        if (number == Float.NEGATIVE_INFINITY) return -1;
        if (number == Float.POSITIVE_INFINITY) return 1;
        return BigDecimal.valueOf(number).compareTo(value);
    }

    /**
     * Compares an integer with a given long value.
     *
     * @param number the integer value to compare
     * @param value  the long value to compare with
     * @return an int representing the result of the comparison
     *         Returns a negative value if the number is less than the value
     *         Returns 0 if the number is equal to the value
     *         Returns a positive value if the number is greater than the value
     */
    public static int compareInteger(int number, long value) {
        return Long.compare(number, value);
    }

    /**
     * Compares an integer with a given BigDecimal value.
     *
     * @param number the integer value to compare
     * @param value  the BigDecimal value to compare with
     * @return an Integer representing the result of the comparison
     * Returns null if the number is NaN
     * Returns -1 if the number is negative infinity
     * Returns 1 if the number is positive infinity
     * Returns the result of BigDecimal.valueOf(number).compareTo(value) otherwise
     */
    public static int compareInteger(int number, BigDecimal value) {
        return compare(number, value);
    }

    /**
     * Compares two long values.
     *
     * @param number the first long value to compare
     * @param value the second long value to compare
     * @return the value 0 if number1 is equal to number2; a value less than 0 if number1 is less than number2;
     *         a value greater than 0 if number1 is greater than number2
     */
    public static int compareLong(long number, long value) {
        return Long.compare(number, value);
    }

    /**
     * Compares a given long number with a BigDecimal value.
     *
     * @param number the long number to compare
     * @param value  the BigDecimal value to compare with
     * @return an int representing the result of the comparison
     */
    public static int compareLong(long number, BigDecimal value) {
        return compare(number, value);
    }

    /**
     * Compares a given short number with a long value.
     *
     * @param number the short number to compare
     * @param value  the long value to compare with
     * @return an int representing the result of the comparison
     *         Returns a negative value if the number is less than the value
     *         Returns 0 if the number is equal to the value
     *         Returns a positive value if the number is greater than the value
     */
    public static int compareShort(short number, long value) {
        return Long.compare(number, value);
    }

    /**
     * Compares a short number to a BigDecimal value.
     *
     * @param number The short number to compare.
     * @param value The BigDecimal value to compare.
     * @return 0 if the short number is equal to the BigDecimal value, a value less than 0 if the short number is smaller
     * than the BigDecimal value, or a value greater than 0 if the short number is larger than the BigDecimal value.
     */
    public static int compareShort(short number, BigDecimal value) {
        return compare(number, value);
    }

    /**
     * Returns the signum function of the specified Byte value.
     *
     * @param number the Byte value whose signum is to be computed
     * @return -1 if the value is less than zero, 0 if the value is equal to zero, or 1 if the value is greater than zero
     */
    public static int signum(Byte number) {
        return number.compareTo((byte) 0);
    }

    /**
     * Returns the signum function of the given BigInteger.
     *
     * The signum function returns -1 if the number is negative, 0 if the number is zero,
     * and 1 if the number is positive.
     *
     * @param number the BigInteger for which the signum function is to be computed
     * @return -1 if the number is negative, 0 if the number is zero, and 1 if the number is positive
     */
    public static int signum(BigInteger number) {
        return number.signum();
    }

    /**
     * Returns the signum function of the specified BigDecimal.
     *
     * @param number the BigDecimal to calculate the signum function for
     * @return -1 if the specified BigDecimal is negative, 0 if the specified BigDecimal is zero,
     *          or 1 if the specified BigDecimal is positive.
     */
    public static int signum(BigDecimal number) {
        return number.signum();
    }

    /**
     * Returns the signum function of the given float number.
     *
     * @param number the number to find the signum of
     * @return -1 if the number is negative, 0 if the number is zero, 1 if the number is positive,
     *         or null if the number is NaN
     */
    public static Integer signum(Float number) {
        if (number.isNaN()) return null;
        if (number == Float.NEGATIVE_INFINITY) return -1;
        if (number == Float.POSITIVE_INFINITY) return 1;
        return number.compareTo(0F);
    }

    /**
     * Calculates the signum of a given number.
     *
     * @param number The number to calculate the signum for.
     * @return Returns the signum of the given number as an Integer. The return value can be one of the following:
     *         <ul>
     *             <li>{@code -1} if the number is negative</li>
     *             <li>{@code 0} if the number is zero</li>
     *             <li>{@code 1} if the number is positive</li>
     *             <li>{@code null} if the number is a NaN (Not-a-Number)</li>
     *         </ul>
     */
    public static Integer signum(Double number) {
        if (number.isNaN()) return null;
        if (number == Double.NEGATIVE_INFINITY) return -1;
        if (number == Double.POSITIVE_INFINITY) return 1;
        return number.compareTo(0D);
    }

    /**
     * Returns the signum function of the specified integer.
     *
     * @param number the integer value whose signum is to be computed
     * @return the signum function of the specified integer; returns -1 if the specified value is negative,
     *         0 if the specified value is zero, or 1 if the specified value is positive
     */
    public static int signum(Integer number) {
        return Integer.signum(number);
    }

    /**
     * Returns the signum function of the specified long value.
     *
     * @param number the value whose signum is to be computed
     * @return -1 if the specified value is negative, 0 if the specified value is zero, 1 if the specified value is positive
     */
    public static int signum(Long number) {
        return Long.signum( number );
    }

    /**
     * Returns the signum function of the specified Short value.
     *
     * @param number the Short value whose signum is to be computed.
     * @return -1 if the specified value is negative, 0 if the specified value is zero, or 1 if the
     *         specified value is positive.
     */
    public static int signum(Short number) {
        return number.compareTo((short) 0);
    }

    /**
     * This interface represents a comparator for comparing a value of type T with a whole number represented as a Long.
     * The comparator returns an Integer value based on the comparison result of the two values.
     *
     * @param <T> the type of the value being compared
     */
    private interface WholeNumberComparator<T> {
        Integer compare(T value1, Long value2);
    }

    /**
     * Interface for comparing decimal values.
     *
     * @param <T> the type of value to compare with BigDecimal
     */
    private interface DecimalComparator<T> {
        Integer compare(T value1, BigDecimal value2);
    }

    /**
     * Represents an interface for obtaining the signum of a numerical value.
     *
     * @param <T> the type of the numerical value.
     */
    private interface NumberSignum<T> {
        Integer signum(T value);
    }
}
