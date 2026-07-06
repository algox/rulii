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

import org.rulii.lib.spring.util.Assert;

import java.util.*;

/**
 * Represents a formatted text with placeholders.
 */
public class FormattedText {

    private final String template;
    private final List<Placeholder> placeholders;

    /**
     * Represents a formatted text with placeholders.
     */
    public FormattedText(String template, List<Placeholder> placeholders) {
        super();
        Assert.notNull(template, "template cannot be null.");
        Assert.notNull(placeholders, "placeholders cannot be null.");
        this.template = template;
        List<Placeholder> copy = new ArrayList<>(placeholders);
        Collections.sort(copy);
        this.placeholders = Collections.unmodifiableList(copy);
    }

    public String getTemplate() {
        return template;
    }

    /**
     * Retrieves the first placeholder with the given name from the list of placeholders.
     *
     * @param name the name of the placeholder to retrieve
     * @return the first placeholder with the given name, or null if no placeholder with the given name exists
     */
    public Placeholder getFirstPlaceholder(String name) {
        Placeholder result = null;

       for (Placeholder placeholder : placeholders) {
           if (placeholder.getName().equals(name)) {
               result = placeholder;
               break;
           }
       }

        return result;
    }

    /**
     * Retrieves all placeholders with the given name from the list of placeholders.
     *
     * @param name the name of the placeholder to retrieve
     * @return a list of placeholders with the given name, or an empty list if no placeholder with the given name exists
     */
    public List<Placeholder> getPlaceholder(String name) {
        List<Placeholder> result = new ArrayList<>();

        placeholders.forEach(p -> {
            if (p.getName().equals(name)) result.add(p);
        });

        return result;
    }

    public int getPlaceholderSize() {
        return placeholders.size();
    }

    public boolean hasPlaceholders() {
        return !placeholders.isEmpty();
    }

    /**
     * Replaces placeholders in the template with the corresponding values from the parameters.
     *
     * @param parameters the ParameterInfo objects containing the values to replace the placeholders
     * @return the formatted string with replaced placeholders
     */
    public String replaceWithIndex(ParameterInfo...parameters) {

        if (placeholders == null || placeholders.isEmpty()) return template;
        if (parameters == null || parameters.length == 0) return template;

        Map<String, ParameterInfo> matchMap = new HashMap<>();

        for (ParameterInfo parameter : parameters) {
            matchMap.put(parameter.getName(), parameter);
        }

        StringBuilder result = new StringBuilder();
        int lastEnd = 0;

        // placeholders is sorted by start position (see constructor) and matches are
        // non-overlapping, so a single forward pass copying the gaps between them suffices.
        for (Placeholder placeholder : placeholders) {
            result.append(template, lastEnd, placeholder.getStartPosition());
            ParameterInfo parameter = matchMap.get(placeholder.getName());

            if (parameter != null) {
                result.append(placeholder.getMessageFormatText(parameter.getIndex()));
            } else {
                result.append("[").append(placeholder.getName()).append(" not found]");
            }

            lastEnd = placeholder.getEndPosition();
        }

        result.append(template, lastEnd, template.length());

        return result.toString();
    }
}
