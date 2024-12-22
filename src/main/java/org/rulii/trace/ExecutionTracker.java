/*
 * This software is licensed under the Apache 2 license, quoted below.
 *
 * Copyright (c) 1999-2021, Algorithmx Inc.
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

import org.rulii.model.Runnable;
import org.rulii.model.action.Action;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.Definable;
import org.rulii.model.Definition;
import org.rulii.model.SourceDefinition;
import org.rulii.util.RuleUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ExecutionTracker implements ExecutionListener {

    private final List<ExecutionItem> events = new LinkedList<>();

    public ExecutionTracker() {
        super();
    }

    public <T extends Runnable<?>> void onEvent(ExecutionEvent<T> event) {
        Assert.notNull(event, "event cannot be null.");

        SourceDefinition source = null;

        if (event.getData() instanceof Definable) {
            Definition definition = ((Definable<?>) event.getData()).getDefinition();
            source = definition.getSource();
        }

        events.add(new ExecutionItem(createSignature(event), source));
    }

    public StackTraceElement[] getStackTrace() {
        StackTraceElement[] result = new StackTraceElement[events.size()];
        int size = events.size();

        for (int i = 0; i < size; i++) {
            result[i] = events.get(size - 1 - i).toStackTraceElement();
        }

        return result;
    }

    public void clear() {
        this.events.clear();
    }

    private <T extends Runnable<?>> String createSignature(ExecutionEvent<T> event) {
        StringBuilder result = new StringBuilder(event.getData().getDescription());

        if (event.getParameters() != null && !event.getParameters().isEmpty()) {
            int size = event.getParameters().size();
            int index = 0;

            result.append(" args [");
            for (Map.Entry<String, Object> entry : event.getParameters().entrySet()) {
                result.append(entry.getKey()).append(" = ").append(RuleUtils.getSummaryTextValue(entry.getValue()));
                if (index < size - 1) result.append(", ");
                index++;
            }
            result.append("]");
        }

        if (event.getEventType().isPost() && !Action.class.isAssignableFrom(event.getData().getClass())) {
            result.append(" result [").append(RuleUtils.getSummaryTextValue(event.getResult())).append("]");
        }

        return result.toString();
    }

    private static class ExecutionItem {
        private final String className;
        private final String methodName;
        private final String signature;
        private final String fileName;
        private final Integer lineNumber;

        public ExecutionItem(String signature, SourceDefinition source) {
            this(source != null ? source.getClassName() : null, signature, source != null ? source.getMethodName() : null,
                    source != null ? source.getFileName() : null, source != null ? source.getLineNumber() : null);
        }

        public ExecutionItem(String className, String methodName, String signature, String fileName, Integer lineNumber) {
            super();
            Assert.notNull(signature, "signature cannot be null.");
            this.className = className;
            this.methodName = methodName;
            this.signature = signature;
            this.fileName = fileName;
            this.lineNumber = lineNumber;
        }

        public String getClassName() {
            return className;
        }

        public String getMethodName() {
            return methodName;
        }

        public String getSignature() {
            return signature;
        }

        public String getFileName() {
            return fileName;
        }

        public Integer getLineNumber() {
            return lineNumber;
        }

        public StackTraceElement toStackTraceElement() {
            return new StackTraceElement(className, methodName, fileName, lineNumber);
        }
    }
}
