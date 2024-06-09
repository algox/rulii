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

import org.rulii.lib.spring.util.Assert;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

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
        this.template = template;
        this.placeholders = Collections.unmodifiableList(placeholders);
        Collections.sort(placeholders);
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

        placeholders.stream().forEach(p -> {
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
        Queue<Placeholder> queue = new ArrayBlockingQueue<>(placeholders.size(), false, placeholders);

        for (int i = 0; i < template.length(); i++) {

            if (!queue.isEmpty() && queue.peek().getStartPosition() == i) {
                Placeholder match = queue.poll();
                ParameterInfo parameter = matchMap.get(match.getName());

                if (parameter != null) {
                    result.append(match.getMessageFormatText(parameter.getIndex()));
                } else {
                    result.append("[").append(match.getName()).append(" not found]");
                }

                i = match.getEndPosition() - 1;
            } else {
                result.append(template.charAt(i));
            }
        }

        return result.toString();
    }
}
