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
package org.rulii.text;

import java.util.Locale;

/**
 * The {@code MessageFormatter} interface defines methods for formatting messages with placeholders.
 * It provides methods to format messages with {@link Locale}, {@link String} and optional arguments.
 * The interface also provides methods to format messages with {@link Locale}, {@link String} and an array of {@link ParameterInfo}.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public interface MessageFormatter {

    /**
     * Returns an instance of {@code MessageFormatterBuilder}.
     *
     * @return the instance of {@code MessageFormatterBuilder}
     */
    static MessageFormatterBuilder builder() {
        return MessageFormatterBuilder.getInstance();
    }

    /**
     * Formats a message with placeholders using the specified locale.
     *
     * @param locale   the locale to be used for formatting
     * @param message  the message to be formatted
     * @param args     the optional arguments to be inserted into the placeholders
     * @return the formatted message
     */
    String format(Locale locale, String message, Object...args);

    /**
     * Formats a message with placeholders using the specified locale and parameter information.
     *
     * @param locale     the locale to be used for formatting
     * @param message    the message to be formatted
     * @param parameters the parameter information to be inserted into the placeholders
     * @return the formatted message
     */
    String format(Locale locale, String message, ParameterInfo...parameters);
}
