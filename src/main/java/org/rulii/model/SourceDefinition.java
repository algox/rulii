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
package org.rulii.model;

/**
 * Details about of the source of the implementation.
 * ie: class/line/method details of a Definable Object (Rule/Action/Condition etc)
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class SourceDefinition {

    private final String className;
    private final String methodName;
    private final String fileName;
    private final Integer lineNumber;

    private SourceDefinition() {
        super();
        this.className = "n/a";
        this.methodName = "n/a";
        this.fileName = "n/a";
        this.lineNumber = null;
    }

    public SourceDefinition(String className, String methodName, String fileName, Integer lineNumber) {
        super();
        this.className = className;
        this.methodName = methodName;
        this.fileName = fileName;
        this.lineNumber = lineNumber;
    }

    /**
     * Builds a source definition using a stacktrace.
     *
     * @return source defintion.
     */
    public static SourceDefinition build() {
        StackTraceElement element = findElement((new Exception()).getStackTrace());

        return element != null
                ? new SourceDefinition(element.getClassName(), element.getMethodName(), element.getFileName(), element.getLineNumber())
                : new SourceDefinition();
    }

    private static StackTraceElement findElement(StackTraceElement[] elements ) {
        StackTraceElement result = null;

        for (StackTraceElement element : elements) {
            if (element.getClassName().startsWith("java")
                    || (element.getClassName().startsWith("org.algorithmx.")
                    && !element.getClassName().startsWith("org.algorithmx.rulii.test"))) {
                continue;
            }

            result = element;
            break;
        }

        return result;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getFileName() {
        return fileName;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    @Override
    public String toString() {
        return "SourceDefinition{" +
                "className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", fileName='" + fileName + '\'' +
                ", lineNumber=" + lineNumber +
                '}';
    }
}
