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
import org.rulii.text.MessageFormatter;
import org.rulii.text.ParameterInfo;

import java.util.Locale;

public class MessageFormatterTest {

    public MessageFormatterTest() {
        super();
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
}
