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
package org.rulii.model;

import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;

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
     * A source that is a file rather than Java code: an XML rule file, for example.
     *
     * @param fileName   the file, in whatever form the loader knows it ({@code classpath:rules/order.xml}); must not be empty.
     * @param lineNumber 1-based line of the declaring element; null when unknown.
     * @return source definition with no class or method.
     * @since 2.1
     */
    public static SourceDefinition forFile(String fileName, Integer lineNumber) {
        if (fileName == null || fileName.isBlank()) throw new IllegalArgumentException("fileName cannot be empty/null.");
        return new SourceDefinition(null, null, fileName, lineNumber);
    }

    /**
     * A source that is a class: the rule class of a class-based rule.
     *
     * @param type the class; must not be null.
     * @return source definition with the class name only.
     * @since 2.1
     */
    public static SourceDefinition forClass(Class<?> type) {
        if (type == null) throw new IllegalArgumentException("type cannot be null.");
        return new SourceDefinition(type.getName(), null, null, null);
    }

    private static final StackWalker WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
    private static final URL RULII_LOCATION = locationOf(SourceDefinition.class);

    /**
     * Builds a source definition from the call stack: the nearest frame that is neither the JDK
     * nor rulii itself. Rulii is recognised by code location (the jar or classes directory this
     * class was loaded from), so application code in any package, including {@code org.rulii.*}
     * packages such as the explorer, is recorded correctly.
     *
     * @return source definition; a placeholder when no such frame exists.
     */
    public static SourceDefinition build() {
        return WALKER.walk(frames -> frames.filter(frame -> !isInternal(frame.getDeclaringClass())).findFirst())
                .map(frame -> new SourceDefinition(frame.getClassName(), frame.getMethodName(), frame.getFileName(), frame.getLineNumber()))
                .orElseGet(SourceDefinition::new);
    }

    private static boolean isInternal(Class<?> type) {
        String name = type.getName();
        if (name.startsWith("java.") || name.startsWith("jdk.") || name.startsWith("sun.")) return true;
        URL location = locationOf(type);
        return location != null && location.equals(RULII_LOCATION);
    }

    private static URL locationOf(Class<?> type) {
        try {
            ProtectionDomain domain = type.getProtectionDomain();
            CodeSource source = domain != null ? domain.getCodeSource() : null;
            return source != null ? source.getLocation() : null;
        } catch (SecurityException e) {
            return null;
        }
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
