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
package org.rulii.test.text;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.text.*;

import java.util.List;
import java.util.Locale;

/**
 * Test class for MessageFormatter and FormattedTextParser.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class MessageFormatterTest {

    public MessageFormatterTest() {
        super();
    }

    @Test
    public void testParseNormalString() {
        String template = "Name: ${name}, Age: ${age}";
        FormattedText formattedText = FormattedTextParser.parse(template);
        List<Placeholder> placeholders = formattedText.getPlaceholder("name");
        Assertions.assertEquals(1, placeholders.size());
        Placeholder namePlaceholder = placeholders.get(0);
        Assertions.assertEquals("name", namePlaceholder.getName());
        Assertions.assertEquals(6, namePlaceholder.getStartPosition());
        Assertions.assertEquals(13, namePlaceholder.getEndPosition());

        placeholders = formattedText.getPlaceholder("age");
        Assertions.assertEquals(1, placeholders.size());
        Placeholder agePlaceholder = placeholders.get(0);
        Assertions.assertEquals("age", agePlaceholder.getName());
        Assertions.assertEquals(20, agePlaceholder.getStartPosition());
        Assertions.assertEquals(26, agePlaceholder.getEndPosition());
    }

    @Test
    public void testParseNonExistentPlaceholder() {
        String template = "Name: ${name}, Age: ${age}";
        FormattedText formattedText = FormattedTextParser.parse(template);
        List<Placeholder> placeholders = formattedText.getPlaceholder("non-existent");
        Assertions.assertEquals(0, placeholders.size());
    }

    @Test
    public void testParseEmptyString() {
        String template = "";
        FormattedText formattedText = FormattedTextParser.parse(template);
        Assertions.assertFalse(formattedText.hasPlaceholders());
    }

    @Test
    public void testParseNullString() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> FormattedTextParser.parse(null));
    }

    @Test
    public void testMessageFormatter1() {
        MessageFormatter formatter = MessageFormatter.builder().build();
        String formatted = formatter.format(Locale.getDefault(), "this is a test");
        Assertions.assertEquals("this is a test", formatted);
    }

    @Test
    public void testMessageFormatter2() {
        MessageFormatter formatter = MessageFormatter.builder().build();
        ParameterInfo[] args = new ParameterInfo[3];
        args[0] = new ParameterInfo(0, "a", "oh");
        args[1] = new ParameterInfo(1, "b", "hello");
        args[2] = new ParameterInfo(2, "c", "there");
        String formatted = formatter.format(Locale.getDefault(), "Test: {0} {1} {2}", args);
        Assertions.assertEquals("Test: oh hello there", formatted);
    }

    @Test
    public void testMessageFormatter3() {
        MessageFormatter formatter = MessageFormatter.builder().build();
        ParameterInfo[] args = new ParameterInfo[2];
        args[0] = new ParameterInfo(0, "a", "oh");
        args[1] = new ParameterInfo(1, "b", 123);
        String formatted = formatter.format(Locale.getDefault(), "Test: {0} {1,number, integer }", args);
        Assertions.assertEquals("Test: oh 123", formatted);
    }

    @Test
    public void testMessageFormatter4() {
        MessageFormatter formatter = MessageFormatter.builder().build();
        ParameterInfo[] args = new ParameterInfo[2];
        args[0] = new ParameterInfo(0, "a", "oh");
        args[1] = new ParameterInfo(1, "b", 123);
        String formatted = formatter.format(Locale.getDefault(), "Test: ${a}  ${b,number, integer } ${a} ${b}", args);
        Assertions.assertEquals("Test: oh  123 oh 123", formatted);
    }

    @Test
    public void testMessageFormatter5() {
        MessageFormatter formatter = MessageFormatter.builder().build();
        ParameterInfo[] args = new ParameterInfo[2];
        args[0] = new ParameterInfo(0, "a", "oh");
        args[1] = new ParameterInfo(1, "b", 123);
        String formatted = formatter.format(Locale.getDefault(), "Test: ${a}  ${b,number, integer } ${a} ${b} {0} {1}", args);
        Assertions.assertEquals("Test: oh  123 oh 123 oh 123", formatted);
    }

    @Test
    public void testMessageFormatter6() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            MessageFormatter formatter = MessageFormatter.builder().build();
            ParameterInfo[] args = new ParameterInfo[2];
            args[0] = new ParameterInfo(0, "a", "oh");
            args[1] = new ParameterInfo(1, "b", 123);
            formatter.format(Locale.getDefault(), "Test: ${a}  ${b,number, integer ${a} ${b} {0} {1}", args);
        });
    }

    @Test
    public void testFormatWithOptionalArguments() {
        MessageFormatter formatter = MessageFormatter.builder().build();
        Locale locale = new Locale("en", "US");
        String message = "Hello, {0}!";
        String arg = "World";
        String formattedMessage = formatter.format(locale, message, arg);
        // Assuming the formatter actually replaces "{0}" with "World"
        Assertions.assertEquals("Hello, World!", formattedMessage);
    }

    @Test
    public void testFormatIndexWithParameterInfo() {
        MessageFormatter formatter = MessageFormatter.builder().build();
        Locale locale = new Locale("en", "US");
        String message = "Hello, {0}!";
        ParameterInfo parameter = new ParameterInfo(0, "param", "World");  // hypothetical ParameterInfo class
        String formattedMessage = formatter.format(locale, message, parameter);
        // Assuming the formatter actually replaces "{0}" with "World"
        Assertions.assertEquals("Hello, World!", formattedMessage);
    }

    @Test
    void testFormatWithArgs() {
        MessageFormatter messageFormatter = MessageFormatter.builder().build();
        String message = "Hello, {0}! You have {1} new messages.";
        String expectedMessage = "Hello, Alice! You have 5 new messages.";
        String actualMessage = messageFormatter.format(Locale.US, message, "Alice", 5);
        Assertions.assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void testFormatWithoutArgs() {
        MessageFormatter messageFormatter = MessageFormatter.builder().build();
        String message = "Hello, World!";
        String expectedMessage = "Hello, World!";
        String actualMessage = messageFormatter.format(Locale.US, message);
        Assertions.assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void testFormatWithParameterInfo() {
        MessageFormatter messageFormatter = MessageFormatter.builder().build();
        String message = "Hello, ${name}! You have ${num} new messages.";
        String expectedMessage = "Hello, Bob! You have 7 new messages.";
        ParameterInfo p1 = new ParameterInfo(0,"name", "Bob");
        ParameterInfo p2 = new ParameterInfo(1,"num", 7);
        String actualMessage = messageFormatter.format(Locale.US, message, p1, p2);
        Assertions.assertEquals(expectedMessage, actualMessage);
    }
}
