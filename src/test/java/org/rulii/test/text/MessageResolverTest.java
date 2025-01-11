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

import org.rulii.text.MessageResolver;
import org.junit.Assert;
import org.junit.Test;

import java.util.Locale;
import java.util.MissingResourceException;

public class MessageResolverTest {

    public MessageResolverTest() {
        super();
    }

    @Test
    public void testMessageFormattedText1() {
        MessageResolver resolver = MessageResolver.builder("message-resolver").build();
        String template = resolver.resolve(null, "test.001");
        Assert.assertTrue("testing".equals(template));
    }

    @Test(expected = MissingResourceException.class)
    public void testMessageFormattedText2() {
        MessageResolver resolver = MessageResolver.builder("message-resolver").build();
        String notFound = resolver.resolve(null, "unknown");
        Assert.assertNull(notFound);
    }

    @Test
    public void testMessageFormattedText3() {
        MessageResolver resolver = MessageResolver.builder("message-resolver").build();
        String template = resolver.resolve(Locale.US, "test.001");
        Assert.assertTrue("testing".equals(template));
    }
}
