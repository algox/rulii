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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

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
        Assertions.assertThrows(ConversionException.class, () -> {
            ConverterRegistry registry = ConverterRegistry.builder().build();
            Converter<String, BigDecimal> converter = registry.find(String.class, BigDecimal.class);
            Assertions.assertNotNull(converter);
            Assertions.assertTrue(converter.canConvert(String.class, BigDecimal.class));
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
        Assertions.assertThrows(ConversionException.class, () -> {
            ConverterRegistry registry = ConverterRegistry.builder().build();
            Converter<String, BigInteger> converter = registry.find(String.class, BigInteger.class);
            Assertions.assertNotNull(converter);
            Assertions.assertTrue(converter.canConvert(String.class, BigInteger.class));
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
        Assertions.assertThrows(ConversionException.class, () -> {
            ConverterRegistry registry = ConverterRegistry.builder().build();
            Converter<String, Byte> converter = registry.find(String.class, Byte.class);
            Assertions.assertNotNull(converter);
            Assertions.assertTrue(converter.canConvert(String.class, Byte.class));
            byte b = 123;
            Assertions.assertEquals((byte) converter.convert("123", Byte.class), b);
            converter.convert("xxxx", Byte.class);
        });
    }

    @Test
    public void stringToDoubleTest() {
        Assertions.assertThrows(ConversionException.class, () -> {
            ConverterRegistry registry = ConverterRegistry.builder().build();
            Converter<String, Double> converter = registry.find(String.class, Double.class);
            Assertions.assertNotNull(converter);
            Assertions.assertTrue(converter.canConvert(String.class, Double.class));
            Assertions.assertEquals(123.45555, converter.convert("123.45555", Double.class));
            converter.convert("xxxx", Double.class);
        });
    }

    @Test
    public void stringToFloatTest() {
        Assertions.assertThrows(ConversionException.class, () -> {
            ConverterRegistry registry = ConverterRegistry.builder().build();
            Converter<String, Float> converter = registry.find(String.class, Float.class);
            Assertions.assertNotNull(converter);
            Assertions.assertTrue(converter.canConvert(String.class, Float.class));
            Assertions.assertEquals(123.455f, converter.convert("123.455", Float.class));
            converter.convert("xxxx", Float.class);
        });
    }

    @Test
    public void stringToIntegerTest() {
        Assertions.assertThrows(ConversionException.class, () -> {
            ConverterRegistry registry = ConverterRegistry.builder().build();
            Converter<String, Integer> converter = registry.find(String.class, Integer.class);
            Assertions.assertNotNull(converter);
            Assertions.assertTrue(converter.canConvert(String.class, Integer.class));
            Assertions.assertEquals(123455, (int) converter.convert("123455", Integer.class));
            Assertions.assertEquals(123456, (int) converter.convert("123455.50", Integer.class));
            converter.convert("xxxx", Integer.class);
        });
    }

    @Test
    public void stringToLongTest() {
        Assertions.assertThrows(ConversionException.class, () -> {
        ConverterRegistry registry = ConverterRegistry.builder().build();
        Converter<String, Long> converter = registry.find(String.class, Long.class);
        Assertions.assertNotNull(converter);
        Assertions.assertTrue(converter.canConvert(String.class, Long.class));
        Assertions.assertEquals(1234553892374L, (long) converter.convert("1234553892374", Long.class));
        Assertions.assertEquals(1234553892375L, (long) converter.convert("1234553892374.50", Long.class));
        converter.convert("xxxx", Long.class);
        });
    }

    @Test
    public void stringToShortTest() {
        Assertions.assertThrows(ConversionException.class, () -> {
            ConverterRegistry registry = ConverterRegistry.builder().build();
            Converter<String, Short> converter = registry.find(String.class, Short.class);
            Assertions.assertNotNull(converter);
            Assertions.assertTrue(converter.canConvert(String.class, Short.class));
            Assertions.assertEquals(64001, (short) converter.convert("64001", Short.class));
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
        Assertions.assertThrows(ConversionException.class, () -> {
            ConverterRegistry registry = ConverterRegistry.builder().build();
            Converter<String, DAYS> converter = registry.find(String.class, DAYS.class);
            Assertions.assertNotNull(converter);
            Assertions.assertTrue(converter.canConvert(String.class, DAYS.class));
            Assertions.assertEquals(converter.convert("TUESDAY", DAYS.class), DAYS.TUESDAY);
            converter.convert("xxxx", DAYS.class);
        });
    }

    @Test
    public void stringToLocalDateTest() {
        Assertions.assertThrows(ConversionException.class, () -> {
            ConverterRegistry registry = ConverterRegistry.builder().build();
            Converter<String, LocalDate> converter = registry.find(String.class, LocalDate.class);
            Assertions.assertNotNull(converter);
            Assertions.assertTrue(converter.canConvert(String.class, LocalDate.class));
            Assertions.assertEquals(converter.convert(LocalDate.now().toString(), LocalDate.class), LocalDate.now());
            converter.convert("xxxx", LocalDate.class);
        });
    }

    @Test
    public void stringToLocalDateTimeTest() {
        Assertions.assertThrows(ConversionException.class, () -> {
            ConverterRegistry registry = ConverterRegistry.builder().build();
            Converter<String, LocalDateTime> converter = registry.find(String.class, LocalDateTime.class);
            Assertions.assertNotNull(converter);
            Assertions.assertTrue(converter.canConvert(String.class, LocalDateTime.class));
            LocalDateTime time = LocalDateTime.now();
            Assertions.assertEquals(converter.convert(time.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), LocalDateTime.class), time);
            converter.convert("xxxx", LocalDateTime.class);
        });
    }

    @Test
    public void stringToDateTest() throws ParseException {
        Assertions.assertThrows(ConversionException.class, () -> {
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
            converter.convert("xxxx", Date.class);
        });
    }

    @Test
    public void converterNotFound() {
        ConverterRegistry registry = ConverterRegistry.builder().build();
        Converter<String, Thread> converter = registry.find(String.class, Thread.class);
        Assertions.assertNull(converter);
    }

    private enum DAYS {
        MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
    }
}
