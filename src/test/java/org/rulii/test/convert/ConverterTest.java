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
package org.rulii.test.convert;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.convert.ConversionException;
import org.rulii.convert.Converter;
import org.rulii.convert.ConverterRegistry;
import org.rulii.convert.text.*;
import org.rulii.lib.apache.math.NumberUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Currency;
import java.util.Date;
import java.util.UUID;

/**
 * Converter tests.
 *
 * @author Max Arulananthan
 */

public class ConverterTest {

    public ConverterTest() {
        super();
    }

    @Test
    public void stringToBigDecimalTest() {
        ConverterRegistry registry = ConverterRegistry.builder().build();
        Converter<String, BigDecimal> converter = registry.find(String.class, BigDecimal.class);
        Assertions.assertNotNull(converter);
        Assertions.assertTrue(converter.canConvert(String.class, BigDecimal.class));

        Assertions.assertThrows(ConversionException.class, () -> {
            Assertions.assertEquals(converter.convert("100.004", BigDecimal.class), new BigDecimal("100.004"));
            converter.convert("xxxx", BigDecimal.class);
        });
    }

    @Test
    public void stringBuilderToBigDecimalTest() {
        ConverterRegistry registry = ConverterRegistry.builder().build();
        Converter<StringBuilder, BigDecimal> converter = registry.find(StringBuilder.class, BigDecimal.class);
        Assertions.assertNotNull(converter);
        Assertions.assertTrue(converter.canConvert(StringBuilder.class, BigDecimal.class));
        Assertions.assertEquals(converter.convert(new StringBuilder("100.004"), BigDecimal.class), new BigDecimal("100.004"));
    }

    @Test
    public void stringToBigIntegerTest() {
        ConverterRegistry registry = ConverterRegistry.builder().build();
        Converter<String, BigInteger> converter = registry.find(String.class, BigInteger.class);
        Assertions.assertNotNull(converter);
        Assertions.assertTrue(converter.canConvert(String.class, BigInteger.class));
        Assertions.assertThrows(ConversionException.class, () -> {
            Assertions.assertEquals(converter.convert("11223423435456546", BigInteger.class), new BigInteger("11223423435456546"));
            converter.convert("xxxxx", BigInteger.class);
        });
    }

    @Test
    public void stringToBooleanTest() {
        ConverterRegistry registry = ConverterRegistry.builder().build();
        Converter<String, Boolean> converter = registry.find(String.class, Boolean.class);
        Assertions.assertNotNull(converter);
        Assertions.assertTrue(converter.canConvert(String.class, Boolean.class));
        Assertions.assertEquals(true, converter.convert("y", Boolean.class));
        Assertions.assertEquals(true, converter.convert("true", Boolean.class));
        Assertions.assertEquals(false, converter.convert("false", Boolean.class));
        Assertions.assertEquals(false, converter.convert("", Boolean.class));
    }

    @Test
    public void stringToByteTest() {
        ConverterRegistry registry = ConverterRegistry.builder().build();
        Converter<String, Byte> converter = registry.find(String.class, Byte.class);
        Assertions.assertNotNull(converter);
        Assertions.assertTrue(converter.canConvert(String.class, Byte.class));
        byte b = 123;
        Assertions.assertEquals((byte) converter.convert("123", Byte.class), b);
        Assertions.assertThrows(ConversionException.class, () -> {
            converter.convert("xxxx", Byte.class);
        });
    }

    @Test
    public void stringToDoubleTest() {
        ConverterRegistry registry = ConverterRegistry.builder().build();
        Converter<String, Double> converter = registry.find(String.class, Double.class);
        Assertions.assertNotNull(converter);
        Assertions.assertTrue(converter.canConvert(String.class, Double.class));
        Assertions.assertEquals(123.45555, converter.convert("123.45555", Double.class));
        Assertions.assertThrows(ConversionException.class, () -> {
            converter.convert("xxxx", Double.class);
        });
    }

    @Test
    public void stringToFloatTest() {
        ConverterRegistry registry = ConverterRegistry.builder().build();
        Converter<String, Float> converter = registry.find(String.class, Float.class);
        Assertions.assertNotNull(converter);
        Assertions.assertTrue(converter.canConvert(String.class, Float.class));
        Assertions.assertEquals(123.455f, converter.convert("123.455", Float.class));
        Assertions.assertThrows(ConversionException.class, () -> {
            converter.convert("xxxx", Float.class);
        });
    }

    @Test
    public void stringToIntegerTest() {
        ConverterRegistry registry = ConverterRegistry.builder().build();
        Converter<String, Integer> converter = registry.find(String.class, Integer.class);
        Assertions.assertNotNull(converter);
        Assertions.assertTrue(converter.canConvert(String.class, Integer.class));
        Assertions.assertEquals(123455, (int) converter.convert("123455", Integer.class));
        Assertions.assertThrows(ConversionException.class, () -> {
            converter.convert("123455.50", Integer.class);
        });
        Assertions.assertThrows(ConversionException.class, () -> {
            converter.convert("xxxx", Integer.class);
        });
    }

    @Test
    public void stringToLongTest() {
        ConverterRegistry registry = ConverterRegistry.builder().build();
        Converter<String, Long> converter = registry.find(String.class, Long.class);
        Assertions.assertNotNull(converter);
        Assertions.assertTrue(converter.canConvert(String.class, Long.class));
        Assertions.assertEquals(1234553892374L, (long) converter.convert("1234553892374", Long.class));
        Assertions.assertThrows(ConversionException.class, () -> {
            converter.convert("1234553892374.50", Long.class);
        });
        Assertions.assertThrows(ConversionException.class, () -> {
            converter.convert("xxxx", Long.class);
        });
    }

    @Test
    public void stringToShortTest() {
        ConverterRegistry registry = ConverterRegistry.builder().build();
        Converter<String, Short> converter = registry.find(String.class, Short.class);
        Assertions.assertNotNull(converter);
        Assertions.assertTrue(converter.canConvert(String.class, Short.class));
        Assertions.assertThrows(ConversionException.class, () -> {
            converter.convert("64001", Short.class);
        });
        Assertions.assertThrows(ConversionException.class, () -> {
            converter.convert("xxxx", Short.class);
        });
    }

    @Test
    public void stringToUrlTest() throws MalformedURLException {
        ConverterRegistry registry = ConverterRegistry.builder().build();
        Converter<String, URL> converter = registry.find(String.class, URL.class);
        URL url = new URL("http://www.rulii.org");
        Assertions.assertNotNull(converter);
        Assertions.assertTrue(converter.canConvert(String.class, URL.class));
        Assertions.assertEquals(converter.convert("http://www.rulii.org", String.class), url);
    }

    @Test
    public void stringToEnumTest() {
        ConverterRegistry registry = ConverterRegistry.builder().build();
        Converter<String, DAYS> converter = registry.find(String.class, DAYS.class);
        Assertions.assertNotNull(converter);
        Assertions.assertTrue(converter.canConvert(String.class, DAYS.class));
        Assertions.assertEquals(converter.convert("TUESDAY", DAYS.class), DAYS.TUESDAY);
        Assertions.assertThrows(ConversionException.class, () -> {
            converter.convert("xxxx", DAYS.class);
        });
    }

    @Test
    public void stringToLocalDateTest() {
        ConverterRegistry registry = ConverterRegistry.builder().build();
        Converter<String, LocalDate> converter = registry.find(String.class, LocalDate.class);
        Assertions.assertNotNull(converter);
        Assertions.assertTrue(converter.canConvert(String.class, LocalDate.class));
        Assertions.assertEquals(converter.convert(LocalDate.now().toString(), LocalDate.class), LocalDate.now());
        Assertions.assertThrows(ConversionException.class, () -> {
            converter.convert("xxxx", LocalDate.class);
        });
    }

    @Test
    public void stringToLocalDateTimeTest() {
        ConverterRegistry registry = ConverterRegistry.builder().build();
        Converter<String, LocalDateTime> converter = registry.find(String.class, LocalDateTime.class);
        Assertions.assertNotNull(converter);
        Assertions.assertTrue(converter.canConvert(String.class, LocalDateTime.class));
        LocalDateTime time = LocalDateTime.now();
        Assertions.assertEquals(converter.convert(time.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), LocalDateTime.class), time);
        Assertions.assertThrows(ConversionException.class, () -> {
            converter.convert("xxxx", LocalDateTime.class);
        });
    }

    @Test
    public void stringToDateTest() throws ParseException {
        ConverterRegistry registry = ConverterRegistry.builder().build();
        Converter<String, Date> converter = registry.find(String.class, Date.class);
        Assertions.assertNotNull(converter);
        Assertions.assertTrue(converter.canConvert(String.class, Date.class));
        LocalDateTime time = LocalDateTime.now();
        Date date1 = converter.convert(LocalDate.now().toString(), Date.class);
        Date date2 = converter.convert(time.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), Date.class);
        SimpleDateFormat SIMPLE = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat ISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Assertions.assertEquals(SIMPLE.parse(LocalDate.now().toString()), date1);
        Assertions.assertEquals(ISO8601.parse(time.toString()), date2);
        Assertions.assertThrows(ConversionException.class, () -> {
            converter.convert("xxxx", Date.class);
        });
    }

    @Test
    public void converterNotFound() {
        ConverterRegistry registry = ConverterRegistry.builder().build();
        Converter<String, Thread> converter = registry.find(String.class, Thread.class);
        Assertions.assertNull(converter);
    }

    @Test
    void shouldConvertCharSequenceToBigDecimal() {
        CharSequence input = "12345.67";
        BigDecimal expectedOutput = new BigDecimal(input.toString());
        TextToBigDecimalConverter converter = new TextToBigDecimalConverter();
        BigDecimal actualOutput = converter.convert(input, BigDecimal.class);
        Assertions.assertEquals(expectedOutput, actualOutput, "Expected converted value to match expected BigDecimal");
    }

    // Test for null input
    @Test
    void shouldReturnNullForNullInput() {
        CharSequence input = null;
        TextToBigDecimalConverter converter = new TextToBigDecimalConverter();
        BigDecimal actualOutput = converter.convert(input, BigDecimal.class);
        Assertions.assertNull(actualOutput, "Expected null due to null input");
    }

    @Test
    void shouldThrowExceptionOnInvalidConversion() {
        CharSequence input = "not-a-number";
        TextToBigDecimalConverter converter = new TextToBigDecimalConverter();
        Assertions.assertThrows(ConversionException.class,
                () -> converter.convert(input, BigDecimal.class),
                "Expected ConversionException due to invalid conversion");
    }

    @Test
    void testValidStringNumber() {
        TextToBigIntegerConverter converter = new TextToBigIntegerConverter();
        CharSequence sequence = "123456789";
        BigInteger converted = converter.convert(sequence, BigInteger.class);
        Assertions.assertEquals(NumberUtils.createBigInteger(sequence.toString()), converted);
    }

    @Test
    void testInvalidNumber() {
        TextToBigIntegerConverter converter = new TextToBigIntegerConverter();
        CharSequence sequence = "ABCDE12345";
        Assertions.assertThrows(ConversionException.class, () ->converter.convert(sequence, BigInteger.class));
    }

    @Test
    void testNullInput() {
        TextToBigIntegerConverter converter = new TextToBigIntegerConverter();
        CharSequence sequence = null;
        Assertions.assertNull(converter.convert(sequence, BigInteger.class));
    }

    @Test
    public void testConvertNull() {
        TextToBooleanConverter converter = new TextToBooleanConverter();
        Assertions.assertNull(converter.convert(null, Boolean.class));
    }

    @Test
    public void testConvertYes() {
        TextToBooleanConverter converter = new TextToBooleanConverter();
        Assertions.assertTrue(converter.convert("YES", Boolean.class));
    }

    @Test
    public void testConvertNo() {
        TextToBooleanConverter converter = new TextToBooleanConverter();
        Assertions.assertFalse(converter.convert("NO", Boolean.class));
    }

    @Test
    public void testConvert1() {
        TextToBooleanConverter converter = new TextToBooleanConverter();
        Assertions.assertTrue(converter.convert("1", Boolean.class));
    }

    @Test
    public void testConvert0() {
        TextToBooleanConverter converter = new TextToBooleanConverter();
        Assertions.assertFalse(converter.convert("0", Boolean.class));
    }

    @Test
    public void testConvertNonBoolean() {
        TextToBooleanConverter converter = new TextToBooleanConverter();
        Assertions.assertFalse(converter.convert("Anything Else", Boolean.class));
    }

    @Test
    void whenConvertValidStringToByte_thenReturnByte() {
        // Initialize object of TextToByteConverter
        TextToByteConverter converter = new TextToByteConverter();
        // Initialize a string that can be converted to Byte
        CharSequence charSequence = "123";
        // Call the method convert
        Byte result = converter.convert(charSequence, Byte.class);
        // 123 in Byte will be same as the original number
        Assertions.assertEquals((byte) 123, result);
    }

    @Test
    void whenConvertInvalidStringToByte_thenThrowConversionException() {
        // Initialize object of TextToByteConverter
        TextToByteConverter converter = new TextToByteConverter();
        // Initialize a string that can't be converted to Byte
        CharSequence charSequence = "not a number";
        // Call the method convert, expect to throw a ConversionException
        Assertions.assertThrows(ConversionException.class, () -> converter.convert(charSequence, Byte.class));
    }

    @Test
    void whenConvertNullStringToByte_thenReturnNull() {
        // Initialize object of TextToByteConverter
        TextToByteConverter converter = new TextToByteConverter();
        // Call the method convert with null value, expect to return null
        Assertions.assertNull(converter.convert(null, Byte.class));
    }

    @Test
    public void testConvertWithValidCharset() {
        TextToCharsetConverter converter = new TextToCharsetConverter();
        CharSequence validCharset = "UTF-8";
        Charset result = converter.convert(validCharset, Charset.class);
        Assertions.assertEquals(StandardCharsets.UTF_8, result);
    }

    @Test
    public void testConvertWithNullCharset() {
        TextToCharsetConverter converter = new TextToCharsetConverter();
        Charset result = converter.convert(null, Charset.class);
        Assertions.assertNull(result);
    }

    @Test
    public void testConvertWithInvalidCharset() {
        TextToCharsetConverter converter = new TextToCharsetConverter();
        CharSequence invalidCharset = "INVALID-CHARSET";
        Assertions.assertThrows(ConversionException.class, () -> {
            converter.convert(invalidCharset, Charset.class);
        });
    }

    @Test
    public void shouldConvertStringCurrencyCodeToCurrencyInstance() {
        TextToCurrencyConverter converter = new TextToCurrencyConverter();
        Currency expectedCurrency = Currency.getInstance("USD");
        Currency actualCurrency = converter.convert("USD", Currency.class);
        Assertions.assertEquals(expectedCurrency, actualCurrency);
    }

    @Test
    public void shouldReturnNullWhenInputIsNull() {
        TextToCurrencyConverter converter = new TextToCurrencyConverter();
        Currency actualCurrency = converter.convert(null, Currency.class);
        Assertions.assertNull(actualCurrency);
    }

    @Test
    public void shouldThrowExceptionForInvalidCurrencyCode() {
        TextToCurrencyConverter converter = new TextToCurrencyConverter();
        Assertions.assertThrows(ConversionException.class, () -> {
            converter.convert("XYZ", Currency.class);
        });
    }

    @Test
    public void testConvertWithNull() {
        TextToDateConverter converter = new TextToDateConverter();
        Assertions.assertNull(converter.convert(null, Date.class));
    }

    @Test
    public void testConvertWithDateOnlyFormat() throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat(TextToDateConverter.DATE_ONLY_FORMAT);
        Date expectedDate = dateFormat.parse("2022-01-01");
        TextToDateConverter converter = new TextToDateConverter();
        Assertions.assertEquals(expectedDate, converter.convert("2022-01-01", Date.class));
    }

    @Test
    public void testConvertWithDateTimeFormat() throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat(TextToDateConverter.DATE_TIME_FORMAT);
        Date expectedDate = dateFormat.parse("2022-01-01T12:00:00");
        TextToDateConverter converter = new TextToDateConverter();
        Assertions.assertEquals(expectedDate, converter.convert("2022-01-01T12:00:00", Date.class));
    }

    /*@Test
    public void testConvertWithDateTimeZoneFormat() throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat(TextToDateConverter.DATE_TIME_ZONE_FORMAT);
        Date expectedDate = dateFormat.parse("2022-01-01T12:00:00+0100");
        TextToDateConverter converter = new TextToDateConverter();
        Assertions.assertEquals(expectedDate, converter.convert("2022-01-01T12:00:00+0100", Date.class));
    }*/

    @Test
    public void testConvertWithInvalidFormat() {
        TextToDateConverter converter = new TextToDateConverter();
        Assertions.assertThrows(ConversionException.class, () -> converter.convert("invalid-date", Date.class));
    }

    @Test
    public void testValidTextConversion() {
        CharSequence input = "123.45";
        Double expected = 123.45;
        TextToDoubleConverter converter = new TextToDoubleConverter();
        Assertions.assertEquals(expected, converter.convert(input, Double.class));
    }

    @Test
    public void testNullTextConversion() {
        TextToDoubleConverter converter = new TextToDoubleConverter();
        Assertions.assertNull(converter.convert(null, Double.class));
    }

    @Test
    public void testInvalidTextConversion() {
        CharSequence input = "abc.cf";
        TextToDoubleConverter converter = new TextToDoubleConverter();
        Assertions.assertThrows(ConversionException.class, () -> converter.convert(input, Double.class));
    }

    @Test
    public void testSuccessfulConversion() {
        TextToEnumConverter converter = new TextToEnumConverter();
        Assertions.assertEquals(TestEnum.A, converter.convert("A", TestEnum.class), "Expected 'A' to be converted to TestEnum.A");
        Assertions.assertEquals(TestEnum.B, converter.convert("B", TestEnum.class), "Expected 'B' to be converted to TestEnum.B");
    }

    @Test
    public void testEnumNullConversion() {
        TextToEnumConverter converter = new TextToEnumConverter();
        Assertions.assertNull(converter.convert(null, TestEnum.class), "Expected null to be converted to null");
    }

    @Test
    public void testUnsuccessfulConversion() {
        TextToEnumConverter converter = new TextToEnumConverter();
        Assertions.assertThrows(ConversionException.class, () -> converter.convert("Invalid", TestEnum.class), "ConversionException expected");
    }

    @Test
    public void testConvertValid() {
        TextToFloatConverter converter = new TextToFloatConverter();
        float result = converter.convert("123.45", Float.TYPE);
        Assertions.assertEquals(123.45F, result);
    }

    @Test
    public void testFloatConvertNull() {
        TextToFloatConverter converter = new TextToFloatConverter();
        Float result = converter.convert(null, Float.TYPE);
        Assertions.assertNull(result);
    }

    @Test
    public void testConvertInvalid() {
        TextToFloatConverter converter = new TextToFloatConverter();
        Assertions.assertThrows(ConversionException.class, () -> converter.convert("not a valid float", Float.TYPE));
    }

    @Test
    public void testConvertWithValidValue() {
        TextToIntegerConverter converter = new TextToIntegerConverter();
        CharSequence charSequence = "123";
        Integer expected = 123;
        Integer result = converter.convert(charSequence, Integer.class);
        Assertions.assertEquals(expected, result, "Failed to convert valid CharSequence to Integer");
    }

    @Test
    public void testConvertWithNullValue() {
        TextToIntegerConverter converter = new TextToIntegerConverter();
        Assertions.assertNull(converter.convert(null, Integer.class), "Expected null when input is null");
    }

    @Test
    public void testConvertWithInvalidValue() {
        TextToIntegerConverter converter = new TextToIntegerConverter();
        CharSequence charSequence = "Invalid";

        Assertions.assertThrows(ConversionException.class, () -> {
            converter.convert(charSequence, Integer.class);
        }, "Expected ConversionException for invalid CharSequence to Integer conversion");
    }

    @Test
    public void testConvert_ValidString_ShouldConvertToCorrectLocalDate() {
        TextToLocalDateConverter converter = new TextToLocalDateConverter();
        CharSequence value = "2023-12-25";
        LocalDate expectedResult = LocalDate.of(2023,12,25);
        LocalDate result = converter.convert(value, LocalDate.class);
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    public void testConvert_NullValue_ShouldReturnNull() {
        TextToLocalDateConverter converter = new TextToLocalDateConverter();
        CharSequence value = null;
        LocalDate result = converter.convert(value, LocalDate.class);
        Assertions.assertNull(result);
    }

    @Test
    public void testConvert_InvalidString_ShouldThrowConversionException() {
        TextToLocalDateConverter converter = new TextToLocalDateConverter();
        CharSequence value = "Invalid-Date";
        Assertions.assertThrows(ConversionException.class, () -> converter.convert(value, LocalDate.class));
    }

    @Test
    public void testLocalDateTimeSuccessfulConversion() {
        TextToLocalDateTimeConverter converter = new TextToLocalDateTimeConverter();
        CharSequence datetimeString = "2023-03-12T10:12:34";
        LocalDateTime expectedDateTime = LocalDateTime.parse(datetimeString);
        Assertions.assertEquals(expectedDateTime, converter.convert(datetimeString, LocalDateTime.class));
    }

    @Test
    public void testLocalDateTimeNullInput() {
        TextToLocalDateTimeConverter converter = new TextToLocalDateTimeConverter();
        Assertions.assertNull(converter.convert(null, LocalDateTime.class));
    }


    @Test
    public void testLocalDateTimeInvalidFormat() {
        TextToLocalDateTimeConverter converter = new TextToLocalDateTimeConverter();
        CharSequence datetimeString = "incorrect-datetime";
        Assertions.assertThrows(ConversionException.class, () -> converter.convert(datetimeString, LocalDateTime.class));
    }

    @Test
    public void testValidNumberConversion() {
        TextToLongConverter converter = new TextToLongConverter();
        CharSequence value = "1234567890";
        Long result = converter.convert(value, Long.class);
        Assertions.assertEquals(1234567890L, result);
    }

    @Test
    public void testNullConversion() {
        TextToLongConverter converter = new TextToLongConverter();
        CharSequence value = null;
        Long result = converter.convert(value, Long.class);
        Assertions.assertNull(result);
    }

    @Test
    public void testInvalidNumberConversion() {
        TextToLongConverter converter = new TextToLongConverter();
        CharSequence value = "abc";
        Assertions.assertThrows(ConversionException.class, () -> converter.convert(value, Long.class));
    }

    @Test
    public void testValidShortString() {
        TextToShortConverter converter = new TextToShortConverter();
        Assertions.assertEquals(Short.valueOf("123"), converter.convert("123", Short.class));
    }

    @Test
    public void testInvalidShortString() {
        TextToShortConverter converter = new TextToShortConverter();
        Assertions.assertThrows(ConversionException.class, () -> converter.convert("abc", Short.class));
    }

    @Test
    public void testMaxValueExceedShortString() {
        TextToShortConverter converter = new TextToShortConverter();
        Assertions.assertThrows(ConversionException.class, () -> converter.convert(String.valueOf(Short.MAX_VALUE + 1), Short.class));
    }

    @Test
    public void testNullCharSequence() {
        TextToShortConverter converter = new TextToShortConverter();
        Assertions.assertNull(converter.convert(null, Short.class));
    }

    @Test
    public void testValidEnumConversion() {
        ConverterRegistry registry = ConverterRegistry.builder().build();
        Converter<String, DAYS> converter = registry.find(String.class, DAYS.class);
        Assertions.assertNotNull(converter);
        Assertions.assertTrue(converter.canConvert(String.class, DAYS.class));
        Assertions.assertEquals(converter.convert("MONDAY", DAYS.class), DAYS.MONDAY);
    }

    @Test
    public void testConvert_withValidUrl() {
        TextToUrlConverter converter = new TextToUrlConverter();
        String validUrl = "http://example.com";
        try {
            URL expected = new URL(validUrl);
            URL actual = converter.convert(validUrl, URL.class);
            Assertions.assertEquals(expected, actual);
        } catch (MalformedURLException e) {
            Assertions.fail("Test failed due to invalid URL format", e);
        }
    }

    @Test
    public void testConvert_withInvalidUrl() {
        TextToUrlConverter converter = new TextToUrlConverter();
        String invalidUrl = "invalidUrl";
        Assertions.assertThrows(ConversionException.class, () -> converter.convert(invalidUrl, URL.class));
    }

    @Test
    public void testConvert_withNull() {
        TextToUrlConverter converter = new TextToUrlConverter();
        CharSequence nullValue = null;
        URL actual = converter.convert(nullValue, URL.class);
        Assertions.assertNull(actual);
    }

    @Test
    public void shouldReturnNullWhenValueIsNull() {
        TextToUUIDConverter converter = new TextToUUIDConverter();
        Assertions.assertNull(converter.convert(null, UUID.class));
    }

    @Test
    public void shouldReturnNullWhenValueIsEmpty() {
        TextToUUIDConverter converter = new TextToUUIDConverter();
        Assertions.assertNull(converter.convert("", UUID.class));
    }

    @Test
    public void shouldConvertValidStringToUUID() {
        TextToUUIDConverter converter = new TextToUUIDConverter();
        UUID expected = UUID.randomUUID();
        String uuidString = expected.toString().trim();
        Assertions.assertEquals(expected, converter.convert(uuidString, UUID.class));
    }


    @Test
    public void shouldThrowConversionExceptionWhenInvalidUUIDStringIsProvided() {
        TextToUUIDConverter converter = new TextToUUIDConverter();
        Assertions.assertThrows(ConversionException.class, () -> converter.convert("invalid uuid string", UUID.class));
    }

    private enum DAYS {
        MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
    }

    enum TestEnum {
        A, B
    }
}
