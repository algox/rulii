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

import java.util.*;

/**
 * A {@link MessageResolver} implementation that resolves message codes to messages
 * using a composite of multiple resource bundles.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class CompositeResourceBundleMessageResolver implements MessageResolver {

    private final Set<String> baseNames = new LinkedHashSet<>();

    /**
     * A {@link MessageResolver} implementation that resolves message codes to messages
     * using a composite of multiple resource bundles.
     *
     * @param baseNames a list of base names for the resource bundles to be used for message resolution
     */
    public CompositeResourceBundleMessageResolver(List<String> baseNames) {
        super();
        if (baseNames != null) this.baseNames.addAll(baseNames);
    }

    /**
     * A {@link MessageResolver} implementation that resolves message codes to messages
     * using a composite of multiple resource bundles.
     */
    public CompositeResourceBundleMessageResolver(String...baseNames) {
        this(baseNames != null ? Arrays.asList(baseNames) : null);
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
    @Override
    public String resolve(Locale locale, String code, String defaultMessage) {
        if (code == null) return defaultMessage;

        String result = null;

        for (String baseName : baseNames) {
            try {
                ResourceBundle bundle = ResourceBundle.getBundle(baseName, locale != null ? locale : Locale.getDefault());

                if (bundle.containsKey(code)) {
                    result = bundle.getString(code);
                    break;
                }
            } catch (MissingResourceException e) {}
        }

        if (result == null) result = defaultMessage;

        return result;
    }

    @Override
    public String toString() {
        return "CompositeResourceBundleMessageResolver{" +
                "baseNames=" + baseNames +
                '}';
    }
}
