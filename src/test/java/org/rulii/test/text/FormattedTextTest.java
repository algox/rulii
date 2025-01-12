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
import org.rulii.text.FormattedText;
import org.rulii.text.FormattedTextParser;

/**
 * Test cases covering the Formatted Text.
 *
 * @author Max Arulananthan.
 */
public class FormattedTextTest {

    public FormattedTextTest() {
        super();
    }

   @Test
   public void testMessageFormattedText1() {
       FormattedText formattedText = FormattedTextParser.parse("This is a test of with no place holders");
       Assertions.assertEquals("This is a test of with no place holders", formattedText.getTemplate());
       Assertions.assertEquals(0, formattedText.getPlaceholderSize());
    }

    @Test
    public void testMessageFormattedText2() {
        FormattedText formattedText = FormattedTextParser.parse("This is a test ${a} ${b}");
        Assertions.assertEquals(2, formattedText.getPlaceholderSize());
        Assertions.assertEquals(15, (int) formattedText.getFirstPlaceholder("a").getStartPosition());
        Assertions.assertEquals(19, formattedText.getFirstPlaceholder("a").getEndPosition());
        Assertions.assertEquals(0, formattedText.getFirstPlaceholder("a").getOptions().size());
        Assertions.assertEquals(20, (int) formattedText.getFirstPlaceholder("b").getStartPosition());
        Assertions.assertEquals(24, formattedText.getFirstPlaceholder("b").getEndPosition());
        Assertions.assertEquals(0, formattedText.getFirstPlaceholder("b").getOptions().size());
    }

    @Test
    public void testMessageFormattedText3() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            FormattedTextParser.parse(null);
        });
    }

    @Test
    public void testMessageFormattedText4() {
        FormattedText formattedText = FormattedTextParser.parse("This is a test {0} {1}");
        Assertions.assertEquals(0, formattedText.getPlaceholderSize());
    }

    @Test
    public void testMessageFormattedText5() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            FormattedTextParser.parse("This is a test ${} ${}");
        });
    }

    @Test
    public void testMessageFormattedText6() {
        FormattedText formattedText = FormattedTextParser.parse("This is a test ${ a    } ${  b  }");
        Assertions.assertEquals(2, formattedText.getPlaceholderSize());
        Assertions.assertEquals(15, (int) formattedText.getFirstPlaceholder("a").getStartPosition());
        Assertions.assertEquals(24, formattedText.getFirstPlaceholder("a").getEndPosition());
        Assertions.assertEquals(25, (int) formattedText.getFirstPlaceholder("b").getStartPosition());
        Assertions.assertEquals(33, formattedText.getFirstPlaceholder("b").getEndPosition());
    }

    @Test
    public void testMessageFormattedText7() {
        FormattedText formattedText = FormattedTextParser.parse("This is a test ${ a     ${  b  }");
        Assertions.assertEquals(1, formattedText.getPlaceholderSize());
    }

    @Test
    public void testMessageFormattedText8() {
        FormattedText formattedText = FormattedTextParser.parse("This is a test ${a, number, integer}    ${b,date, long} ${c} ${value, number}");
        Assertions.assertEquals(4, formattedText.getPlaceholderSize());
        Assertions.assertEquals(2, formattedText.getFirstPlaceholder("a").getOptions().size());
        Assertions.assertEquals(2, formattedText.getFirstPlaceholder("b").getOptions().size());
        Assertions.assertEquals(0, formattedText.getFirstPlaceholder("c").getOptions().size());
        Assertions.assertEquals(1, formattedText.getFirstPlaceholder("value").getOptions().size());
    }

    @Test
    public void testMessageFormattedText9() {
        FormattedText formattedText = FormattedTextParser.parse("${a} ${b} ${c} ${a} ${b}");
        Assertions.assertEquals(5, formattedText.getPlaceholderSize());
    }
}
