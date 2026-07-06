/*
 * This software is licensed under the Apache 2 license, quoted below.
 *
 * Copyright (c) 1999-2026, Algorithmx Inc.
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

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The DefaultMessageFormatter class is an implementation of the MessageFormatter interface.
 * It provides methods to format messages with placeholders using the specified locale and parameters.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class DefaultMessageFormatter implements MessageFormatter {

    // Message templates are fixed, author-supplied strings (validation-rule DEFAULT_MESSAGE
    // constants, resource-bundle entries, or literal errorMessage(...) calls) - never built by
    // interpolating runtime data into the template text itself - so caching by template string
    // is bounded by the number of distinct templates an application defines, not by call volume.
    private final Map<String, FormattedText> cache = new ConcurrentHashMap<>();

    public DefaultMessageFormatter() {
        super();
    }

    public String format(Locale locale, String message, Object...args) {
        MessageFormat format = new MessageFormat(message, locale);
        return format.format(args);
    }

    /**
     * Formats the given message with placeholders using the specified locale and parameters.
     *
     * @param locale     the locale to use for formatting the message
     * @param message    the message string containing placeholders
     * @param parameters optional parameters to replace the placeholders
     * @return the formatted message string
     */
    @Override
    public String format(Locale locale, String message, ParameterInfo...parameters) {
        FormattedText formattedText = cache.computeIfAbsent(message, FormattedTextParser::parse);
        String template = formattedText.hasPlaceholders() ? formattedText.replaceWithIndex(parameters) : message;
        return format(locale, template, createArguments(parameters));
    }

    /**
     * Creates an array of objects from the specified ParameterInfo array.
     *
     * <p>Each value is placed at its declared {@link ParameterInfo#getIndex()} position, not its
     * position within the {@code parameters} array, so this agrees with the same
     * {@code getIndex()} value {@link FormattedText#replaceWithIndex} embeds as the MessageFormat
     * positional placeholder.
     *
     * @param parameters the ParameterInfo array to create arguments from
     * @return the array of objects created from the ParameterInfo values
     */
    private Object[] createArguments(ParameterInfo...parameters) {
        if (parameters == null || parameters.length == 0) return new Object[0];

        int maxIndex = 0;

        for (ParameterInfo parameter : parameters) {
            maxIndex = Math.max(maxIndex, parameter.getIndex());
        }

        Object[] result = new Object[maxIndex + 1];

        for (ParameterInfo parameter : parameters) {
            result[parameter.getIndex()] = parameter.getValue();
        }

        return result;
    }

    @Override
    public String toString() {
        return "DefaultMessageFormatter{}";
    }
}
