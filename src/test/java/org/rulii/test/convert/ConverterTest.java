/*
 * This software is licensed under the Apache 2 license, quoted below.
 *
 * Copyright (c) 1999-2024, Algorithmx Inc.
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

import org.rulii.convert.ConversionException;
import org.rulii.convert.Converter;
import org.rulii.convert.ConverterRegistry;
import org.junit.Assert;
import org.junit.Test;

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

    @Test(expected = ConversionException.class)
    public void stringToBigDecimalTest() {
        ConverterRegistry registry = ConverterRegistry.builder().build();
        Converter<String, BigDecimal> converter = registry.find(String.class, BigDecimal.class);
        Assert.assertTrue(converter != null);
        Assert.assertTrue(converter.canConvert(String.class, BigDecimal.class));
        Assert.assertTrue(converter.convert("100.004", BigDecimal.class).equals(new BigDecimal("100.004")));
        converter.convert("xxxx", BigDecimal.class);
    }

    @Test
    public void stringBuilderToBigDecimalTest() {
        ConverterRegistry registry = ConverterRegistry.builder().build();
        Converter<StringBuilder, BigDecimal> converter = registry.find(StringBuilder.class, BigDecimal.class);
        Assert.assertTrue(converter != null);
        Assert.assertTrue(converter.canConvert(StringBuilder.class, BigDecimal.class));
        Assert.assertTrue(converter.convert(new StringBuilder("100.004"), BigDecimal.class).equals(new BigDecimal("100.004")));
    }

    @Test(expected = ConversionException.class)
    public void stringToBigIntegerTest() {
        ConverterRegistry registry = ConverterRegistry.builder().build();
        Converter<String, BigInteger> converter = registry.find(String.class, BigInteger.class);
        Assert.assertTrue(converter != null);
        Assert.assertTrue(converter.canConvert(String.class, BigInteger.class));
        Assert.assertTrue(converter.convert("11223423435456546", BigInteger.class).equals(new BigInteger("11223423435456546")));
        converter.convert("xxxxx", BigInteger.class);
    }

    @Test
    public void stringToBooleanTest() {
        ConverterRegistry registry = ConverterRegistry.builder().build();
        Converter<String, Boolean> converter = registry.find(String.class, Boolean.class);
        Assert.assertTrue(converter != null);
        Assert.assertTrue(converter.canConvert(String.class, Boolean.class));
        Assert.assertTrue(converter.convert("y", Boolean.class).equals(true));
        Assert.assertTrue(converter.convert("true", Boolean.class).equals(true));
        Assert.assertTrue(converter.convert("false", Boolean.class).equals(false));
        Assert.assertTrue(converter.convert("", Boolean.class).equals(false));
    }

    @Test(expected = ConversionException.class)
    public void stringToByteTest() {
        ConverterRegistry registry = ConverterRegistry.builder().build();
        Converter<String, Byte> converter = registry.find(String.class, Byte.class);
        Assert.assertTrue(converter != null);
        Assert.assertTrue(converter.canConvert(String.class, Byte.class));
        byte b = 123;
        Assert.assertTrue(converter.convert("123", Byte.class).equals(b));
        converter.convert("xxxx", Byte.class);
    }

    @Test(expected = ConversionException.class)
    public void stringToDoubleTest() {
        ConverterRegistry registry = ConverterRegistry.builder().build();
        Converter<String, Double> converter = registry.find(String.class, Double.class);
        Assert.assertTrue(converter != null);
        Assert.assertTrue(converter.canConvert(String.class, Double.class));
        Assert.assertTrue(converter.convert("123.45555", Double.class).equals(123.45555));
        converter.convert("xxxx", Double.class);
    }

    @Test(expected = ConversionException.class)
    public void stringToFloatTest() {
        ConverterRegistry registry = ConverterRegistry.builder().build();
        Converter<String, Float> converter = registry.find(String.class, Float.class);
        Assert.assertTrue(converter != null);
        Assert.assertTrue(converter.canConvert(String.class, Float.class));
        Assert.assertTrue(converter.convert("123.455", Float.class).equals(123.455f));
        converter.convert("xxxx", Float.class);
    }

    @Test(expected = ConversionException.class)
    public void stringToIntegerTest() {
        ConverterRegistry registry = ConverterRegistry.builder().build();
        Converter<String, Integer> converter = registry.find(String.class, Integer.class);
        Assert.assertTrue(converter != null);
        Assert.assertTrue(converter.canConvert(String.class, Integer.class));
        Assert.assertTrue(converter.convert("123455", Integer.class).equals(123455));
        Assert.assertTrue(converter.convert("123455.50", Integer.class).equals(123456));
        converter.convert("xxxx", Integer.class);
    }

    @Test(expected = ConversionException.class)
    public void stringToLongTest() {
        ConverterRegistry registry = ConverterRegistry.builder().build();
        Converter<String, Long> converter = registry.find(String.class, Long.class);
        Assert.assertTrue(converter != null);
        Assert.assertTrue(converter.canConvert(String.class, Long.class));
        Assert.assertTrue(converter.convert("1234553892374", Long.class).equals(1234553892374l));
        Assert.assertTrue(converter.convert("1234553892374.50", Long.class).equals(1234553892375l));
        converter.convert("xxxx", Long.class);
    }

    @Test(expected = ConversionException.class)
    public void stringToShortTest() {
        ConverterRegistry registry = ConverterRegistry.builder().build();
        Converter<String, Short> converter = registry.find(String.class, Short.class);
        Assert.assertTrue(converter != null);
        Assert.assertTrue(converter.canConvert(String.class, Short.class));
        Assert.assertTrue(converter.convert("64001", Short.class).equals(64001));
        converter.convert("xxxx", Short.class);
    }

    @Test
    public void stringToUrlTest() throws MalformedURLException {
        ConverterRegistry registry = ConverterRegistry.builder().build();
        Converter<String, URL> converter = registry.find(String.class, URL.class);
        URL url = new URL("http://www.rulii.org");
        Assert.assertTrue(converter != null);
        Assert.assertTrue(converter.canConvert(String.class, URL.class));
        Assert.assertTrue(converter.convert("http://www.rulii.org",String.class).equals(url));
    }

    @Test(expected = ConversionException.class)
    public void stringToEnumTest() {
        ConverterRegistry registry = ConverterRegistry.builder().build();
        Converter<String, DAYS> converter = registry.find(String.class, DAYS.class);
        Assert.assertTrue(converter != null);
        Assert.assertTrue(converter.canConvert(String.class, DAYS.class));
        Assert.assertTrue(converter.convert("TUESDAY", DAYS.class).equals(DAYS.TUESDAY));
        converter.convert("xxxx", DAYS.class);
    }

    @Test(expected = ConversionException.class)
    public void stringToLocalDateTest() {
        ConverterRegistry registry = ConverterRegistry.builder().build();
        Converter<String, LocalDate> converter = registry.find(String.class, LocalDate.class);
        Assert.assertTrue(converter != null);
        Assert.assertTrue(converter.canConvert(String.class, LocalDate.class));
        Assert.assertTrue(converter.convert(LocalDate.now().toString(), LocalDate.class).equals(LocalDate.now()));
        converter.convert("xxxx", LocalDate.class);
    }

    @Test(expected = ConversionException.class)
    public void stringToLocalDateTimeTest() {
        ConverterRegistry registry = ConverterRegistry.builder().build();
        Converter<String, LocalDateTime> converter = registry.find(String.class, LocalDateTime.class);
        Assert.assertTrue(converter != null);
        Assert.assertTrue(converter.canConvert(String.class, LocalDateTime.class));
        LocalDateTime time = LocalDateTime.now();
        Assert.assertTrue(converter.convert(time.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), LocalDateTime.class).equals(time));
        converter.convert("xxxx", LocalDateTime.class);
    }

    @Test(expected = ConversionException.class)
    public void stringToDateTest() throws ParseException {
        ConverterRegistry registry = ConverterRegistry.builder().build();
        Converter<String, Date> converter = registry.find(String.class, Date.class);
        Assert.assertTrue(converter != null);
        Assert.assertTrue(converter.canConvert(String.class, Date.class));
        LocalDateTime time = LocalDateTime.now();
        Date date1 = converter.convert(LocalDate.now().toString(), Date.class);
        Date date2 = converter.convert(time.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), Date.class);
        SimpleDateFormat SIMPLE = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat ISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Assert.assertTrue(SIMPLE.parse(LocalDate.now().toString()).equals(date1));
        Assert.assertTrue(ISO8601.parse(time.toString()).equals(date2));
        converter.convert("xxxx", Date.class);
    }

    @Test
    public void converterNotFound() {
        ConverterRegistry registry = ConverterRegistry.builder().build();
        Converter converter = registry.find(String.class, Thread.class);
        Assert.assertNull(converter);
    }

    private enum DAYS {
        MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;
    }
}
