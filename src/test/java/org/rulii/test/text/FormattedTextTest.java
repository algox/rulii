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
package org.rulii.test.text;

import org.rulii.text.FormattedText;
import org.rulii.text.FormattedTextParser;
import org.junit.Assert;
import org.junit.Test;

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
       Assert.assertTrue(formattedText.getTemplate().equals("This is a test of with no place holders"));
       Assert.assertTrue(formattedText.getPlaceholderSize() == 0);
    }

    @Test
    public void testMessageFormattedText2() {
        FormattedText formattedText = FormattedTextParser.parse("This is a test ${a} ${b}");
        Assert.assertTrue(formattedText.getPlaceholderSize() == 2);
        Assert.assertTrue(formattedText.getFirstPlaceholder("a").getStartPosition() == 15);
        Assert.assertTrue(formattedText.getFirstPlaceholder("a").getEndPosition() == 19);
        Assert.assertTrue(formattedText.getFirstPlaceholder("a").getOptions().size() == 0);
        Assert.assertTrue(formattedText.getFirstPlaceholder("b").getStartPosition() == 20);
        Assert.assertTrue(formattedText.getFirstPlaceholder("b").getEndPosition() == 24);
        Assert.assertTrue(formattedText.getFirstPlaceholder("b").getOptions().size() == 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMessageFormattedText3() {
        FormattedTextParser.parse(null);
    }

    @Test
    public void testMessageFormattedText4() {
        FormattedText formattedText = FormattedTextParser.parse("This is a test {0} {1}");
        Assert.assertTrue(formattedText.getPlaceholderSize() == 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMessageFormattedText5() {
        FormattedTextParser.parse("This is a test ${} ${}");
    }

    @Test
    public void testMessageFormattedText6() {
        FormattedText formattedText = FormattedTextParser.parse("This is a test ${ a    } ${  b  }");
        Assert.assertTrue(formattedText.getPlaceholderSize() == 2);
        Assert.assertTrue(formattedText.getFirstPlaceholder("a").getStartPosition() == 15);
        Assert.assertTrue(formattedText.getFirstPlaceholder("a").getEndPosition() == 24);
        Assert.assertTrue(formattedText.getFirstPlaceholder("b").getStartPosition() == 25);
        Assert.assertTrue(formattedText.getFirstPlaceholder("b").getEndPosition() == 33);
    }

    @Test
    public void testMessageFormattedText7() {
        FormattedText formattedText = FormattedTextParser.parse("This is a test ${ a     ${  b  }");
        Assert.assertTrue(formattedText.getPlaceholderSize() == 1);
    }

    @Test
    public void testMessageFormattedText8() {
        FormattedText formattedText = FormattedTextParser.parse("This is a test ${a, number, integer}    ${b,date, long} ${c} ${value, number}");
        Assert.assertTrue(formattedText.getPlaceholderSize() == 4);
        Assert.assertTrue(formattedText.getFirstPlaceholder("a").getOptions().size() == 2);
        Assert.assertTrue(formattedText.getFirstPlaceholder("b").getOptions().size() == 2);
        Assert.assertTrue(formattedText.getFirstPlaceholder("c").getOptions().size() == 0);
        Assert.assertTrue(formattedText.getFirstPlaceholder("value").getOptions().size() == 1);
    }

    @Test
    public void testMessageFormattedText9() {
        FormattedText formattedText = FormattedTextParser.parse("${a} ${b} ${c} ${a} ${b}");
        Assert.assertTrue(formattedText.getPlaceholderSize() == 5);
    }
}
