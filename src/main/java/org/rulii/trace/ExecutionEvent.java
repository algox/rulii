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
package org.rulii.trace;

import org.rulii.bind.match.ParameterMatch;
import org.rulii.model.Runnable;

import java.util.*;

public class ExecutionEvent<T extends Runnable<?>> implements Comparable<ExecutionEvent<?>> {

    private final EventType eventType;
    private final T data;
    private final Object result;
    private final Date time;
    private final Map<String, Object> parameters;

    public ExecutionEvent(EventType eventType, T data, Object result, Date time, ParameterMatch[] matches, Object[] values) {
        this.eventType = eventType;
        this.data = data;
        this.result = result;
        this.time = time;
        this.parameters = convert(matches, values);
    }

    @Override
    public int compareTo(ExecutionEvent o) {
        return time.compareTo(o.time);
    }

    public boolean isError() {
        return result instanceof Exception;
    }

    public EventType getEventType() {
        return eventType;
    }

    public T getData() {
        return data;
    }

    public Date getTime() {
        return time;
    }

    public Object getResult() {
        return result;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    private static Map<String, Object> convert(ParameterMatch[] matches, Object[] values) {
        if (matches == null) return new LinkedHashMap<>();

        Map<String, Object> result = new LinkedHashMap<>();

        for (int i = 0; i < matches.length; i++) {
            result.put(matches[i].getDefinition().getName(), i < values.length ? values[i] : null);
        }

        return Collections.unmodifiableMap(result);
    }

    @Override
    public String toString() {
        return "ExecutionEvent{" +
                "eventType=" + eventType +
                ", data=" + data +
                ", result=" + result +
                ", time=" + time +
                ", parameters=" + parameters +
                '}';
    }
}
