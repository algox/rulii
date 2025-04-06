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

import org.rulii.lib.apache.StringUtils;
import org.rulii.lib.spring.util.Assert;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Represents a placeholder in a message format template.
 *
 * <p>
 * A placeholder is used to specify a parameter or formatting information
 * within a message format template. It is enclosed within curly braces
 * and can have the following optional parts:
 * <ul>
 *    <li>Parameter name: The name of the parameter to substitute in the message format template.</li>
 *    <li>Format type: The type of formatting to apply to the parameter value.</li>
 *    <li>Format style: The style of formatting to apply to the parameter value.</li>
 * </ul>
 *
 * <p>
 * The placeholder has a name, start position, end position, and a list of options.
 * The start position represents the index in the message format template where the placeholder starts,
 * and the end position represents the index where it ends.
 *
 * <p>
 * Placeholders are comparable based on their start position. This allows them to be sorted in
 * ascending order based on their appearance in the message format template.
 *
 * <p>
 * This class is immutable once constructed.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class Placeholder implements Comparable<Placeholder> {

    private final String name;
    private final Integer startPosition;
    private final int endPosition;
    private final List<String> options;

    /**
     * Constructs a Placeholder object with the given parameters.
     *
     * @param name The name of the Placeholder. Cannot be null or empty.
     * @param startPosition The starting position of the Placeholder.
     * @param endPosition The ending position of the Placeholder.
     * @param options Optional options for the Placeholder.
     *                Must follow either "${ParameterName,FormatType,FormatStyle}"
     *                or "${ParameterName,FormatType}" or "${ParameterName}" format.
     * @throws IllegalArgumentException If the Placeholder name is null or empty,
     *                                  if the end position is not greater than the start position,
     *                                  or if the options do not follow the correct format.
     */
    Placeholder(String name, int startPosition, int endPosition, String...options) {
        super();
        Assert.isTrue(!StringUtils.isBlank(name), "PlaceHolder name cannot be null/empty. " +
                "Follow : ${BindingName,option(s)}. Given [" + name + "]");
        Assert.isTrue(endPosition > startPosition, "endIndex must be greater than startIndex. " +
                "[" + startPosition + "] [" + endPosition + "]");
        Assert.isTrue(options == null || options.length < 3, "Placeholder must follow either " +
                "${ParameterName,FormatType,FormatStyle} or ${ParameterName,FormatType} or ${ParameterName} given " + name + "]");
        this.name = name;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.options =  options != null ? Collections.unmodifiableList(Arrays.asList(options)) : null;
    }

    public String getName() {
        return name;
    }

    public List<String> getOptions() {
        return options;
    }

    public Integer getStartPosition() {
        return startPosition;
    }

    public int getEndPosition() {
        return endPosition;
    }

    public boolean hasOptions() {
        return !options.isEmpty();
    }

    public String getMessageFormatText(int index) {
        return "{" + index + (!options.isEmpty() ? "," + String.join(",", options) : "") + "}";
    }

    @Override
    public int compareTo(Placeholder o) {
        return startPosition.compareTo(o.startPosition);
    }

    @Override
    public String toString() {
        return "Placeholder{" +
                ", name='" + name + '\'' +
                ", startPosition=" + startPosition +
                ", endPosition=" + endPosition +
                ", options=" + options +
                '}';
    }
}
