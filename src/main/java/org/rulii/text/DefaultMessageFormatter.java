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

import java.text.MessageFormat;
import java.util.Locale;

/**
 * The DefaultMessageFormatter class is an implementation of the MessageFormatter interface.
 * It provides methods to format messages with placeholders using the specified locale and parameters.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class DefaultMessageFormatter implements MessageFormatter {

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
        FormattedText formattedText = FormattedTextParser.parse(message);
        String template = formattedText.hasPlaceholders() ? formattedText.replaceWithIndex(parameters) : message;
        return format(locale, template, createArguments(parameters));
    }

    /**
     * Creates an array of objects from the specified ParameterInfo array.
     * The values of the ParameterInfo objects are used as the elements of the resulting array.
     *
     * @param parameters the ParameterInfo array to create arguments from
     * @return the array of objects created from the ParameterInfo values
     */
    private Object[] createArguments(ParameterInfo...parameters) {
        Object[] result = new Object[parameters != null ? parameters.length : 0];

        if (parameters == null || parameters.length == 0) return result;

        for (int i = 0; i < parameters.length; i++) {
            result[i] = parameters[i].getValue();
        }

        return result;
    }

    @Override
    public String toString() {
        return "DefaultMessageFormatter{}";
    }
}
