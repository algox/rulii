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
 * Interface for resolving message codes to messages.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public interface MessageResolver {

    /**
     * Builds a MessageResolver instance with the specified base names.
     * The MessageResolver is responsible for resolving message codes to messages.
     *
     * @param baseNames an array of base names for the resource bundles to be used for message resolution
     * @return a new instance of MessageResolverBuilder
     */
    static MessageResolverBuilder builder(String...baseNames) {
        return new MessageResolverBuilder(baseNames);
    }

    /**
     * Resolves a message code to a message using the specified locale and code.
     * If a defaultMessage is provided, it will be used as the default message if the code is not found.
     *
     * @param locale the locale used to resolve the message
     * @param code the code of the message to resolve
     * @return the resolved message, or null if no message is found
     */
    default String resolve(Locale locale, String code) {
        return resolve(locale, code, null);
    }

    /**
     * Resolves a message code to a message using the specified locale and code.
     * If a defaultMessage is provided, it will be used as the default message if the code is not found.
     *
     * @param locale the locale used to resolve the message
     * @param code the code of the message to resolve
     * @param defaultMessage the default message to use if the code is not found
     * @return the resolved message, or null if no message is found
     */
    String resolve(Locale locale, String code, String defaultMessage);
}
