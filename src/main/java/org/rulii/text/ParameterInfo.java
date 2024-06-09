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

import java.util.Objects;

/**
 * The {@code ParameterInfo} class represents information about a parameter.
 * It stores the index, name, and value of a parameter.
 *
 * <p>
 * {@code ParameterInfo} objects can be compared based on their index using the {@link Comparable} interface.
 * </p>
 *
 * <p>
 * The class overrides the {@code equals()} and {@code hashCode()} methods to compare objects based on their index.
 * </p>
 *
 * <p>
 * The class also overrides the {@code toString()} method to provide a string representation that includes the index, name, and value of the parameter.
 * </p>
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class ParameterInfo implements Comparable<ParameterInfo> {

    private final Integer index;
    private final String name;
    private final Object value;


    /**
     * Represents information about a parameter.
     * @param index index of the parameter.
     * @param name name of the parameter.
     * @param value value of the parameter.
     */
    public ParameterInfo(int index, String name, Object value) {
        super();
        this.index = index;
        this.name = name;
        this.value = value;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public int compareTo(ParameterInfo o) {
        return index.compareTo(o.index);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParameterInfo parameter = (ParameterInfo) o;
        return index == parameter.index;
    }

    @Override
    public int hashCode() {
        return Objects.hash(index);
    }

    @Override
    public String toString() {
        return "Parameter{" +
                "index=" + index +
                ", name='" + name + '\'' +
                ", value=" + value +
                '}';
    }
}
