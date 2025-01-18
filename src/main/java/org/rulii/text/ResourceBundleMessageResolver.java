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

import org.rulii.lib.spring.util.Assert;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Implementation of the MessageResolver interface that uses a ResourceBundle to resolve message codes to messages.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class ResourceBundleMessageResolver implements MessageResolver {

    private final String baseName;

    /**
     * Creates a new ResourceBundleMessageResolver instance.
     *
     * @param baseName the base name of the resource bundle
     * @throws IllegalArgumentException if the baseName is null
     */
    public ResourceBundleMessageResolver(String baseName) {
        super();
        Assert.hasText(baseName, "baseName cannot be empty/null.");
        this.baseName = baseName;
    }

    /**
     * Resolves a message code to a message using the specified locale and code.
     * If a defaultMessage is provided, it will be used as the default message if the code is not found.
     *
     * @param locale the locale used to resolve the message
     * @param code the code of the message to resolve
     * @param defaultMessage the default message to use if the code is not found
     * @return the resolved message, or the defaultMessage if no message is found
     */
    @Override
    public String resolve(Locale locale, String code, String defaultMessage) {
        if (code == null) return defaultMessage;

        String result = defaultMessage;

        try {
            ResourceBundle bundle = ResourceBundle.getBundle(baseName, locale != null ? locale : Locale.getDefault());
            result = bundle.getString(code);
        } catch (MissingResourceException e) {
            if (defaultMessage == null) throw e;
        }

        return result;
    }
}
