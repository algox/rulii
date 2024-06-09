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
package org.rulii.text;

/**
 * The {@code MessageFormatterBuilder} class provides a builder pattern for creating instances of {@link MessageFormatter}.
 * It is a singleton class which can be accessed using the {@link #getInstance()} method.
 *
 * <p>
 * Example usage:
 * <pre>
 *     MessageFormatter formatter = MessageFormatterBuilder.getInstance().build();
 *     String formattedMessage = formatter.format(Locale.US, "Hello {0}!", "World");
 * </pre>
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public final class MessageFormatterBuilder {

    private static final MessageFormatterBuilder instance = new MessageFormatterBuilder();

    private MessageFormatterBuilder() {
        super();
    }

    /**
     * Returns an instance of {@code MessageFormatterBuilder}.
     *
     * @return the instance of {@code MessageFormatterBuilder}
     */
    public static MessageFormatterBuilder getInstance() {
        return instance;
    }

    /**
     * Builds and returns a MessageFormatter instance.
     *
     * @return the built MessageFormatter instance
     */
    public MessageFormatter build() {
        return new DefaultMessageFormatter();
    }
}
