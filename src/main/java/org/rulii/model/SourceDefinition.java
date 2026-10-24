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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
    private final String fieldName;
    private final String fileName;
    private final Integer lineNumber;

    private SourceDefinition() {
        super();
        this.className = "n/a";
        this.methodName = "n/a";
        this.fieldName = null;
        this.fileName = "n/a";
        this.lineNumber = null;
    }

    public SourceDefinition(String className, String methodName, String fileName, Integer lineNumber) {
        this(className, methodName, null, fileName, lineNumber);
    }

    /**
     * Full constructor. A source names at most one member: a method or a field.
     *
     * @param className  declaring class; null for a non-Java source.
     * @param methodName declaring method; null when the source is not a method.
     * @param fieldName  declaring field; null when the source is not a field.
     * @param fileName   source file; null when unknown.
     * @param lineNumber 1-based line; null when unknown.
     * @since 2.2
     */
    public SourceDefinition(String className, String methodName, String fieldName, String fileName, Integer lineNumber) {
        super();
        this.className = className;
        this.methodName = methodName;
        this.fieldName = fieldName;
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

    /**
     * A source that is a field: a constraint declared on a bean field, for example.
     *
     * @param field the field; must not be null.
     * @return source definition with the declaring class name and the field name.
     * @since 2.2
     */
    public static SourceDefinition forField(Field field) {
        if (field == null) throw new IllegalArgumentException("field cannot be null.");
        return new SourceDefinition(field.getDeclaringClass().getName(), null, field.getName(), null, null);
    }

    /**
     * A source that is a method: a constraint declared on a getter, for example.
     *
     * @param method the method; must not be null.
     * @return source definition with the declaring class name and the method name.
     * @since 2.2
     */
    public static SourceDefinition forMethod(Method method) {
        if (method == null) throw new IllegalArgumentException("method cannot be null.");
        return new SourceDefinition(method.getDeclaringClass().getName(), method.getName(), null, null, null);
    }

    /**
     * A source that is a constructor: a constraint declared on a constructor or one of its parameters,
     * for example. The method name recorded is the constructor's simple class name, as the
     * specification names constructor nodes.
     *
     * @param constructor the constructor; must not be null.
     * @return source definition with the declaring class name and the class's simple name as the method name.
     * @since 2.2
     */
    public static SourceDefinition forConstructor(Constructor<?> constructor) {
        if (constructor == null) throw new IllegalArgumentException("constructor cannot be null.");
        return new SourceDefinition(constructor.getDeclaringClass().getName(), constructor.getDeclaringClass().getSimpleName(), null, null, null);
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

    /**
     * Name of the declaring field, when the source is a field.
     *
     * @return field name; null when the source is not a field.
     * @since 2.2
     */
    public String getFieldName() {
        return fieldName;
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
                ", fieldName='" + fieldName + '\'' +
                ", fileName='" + fileName + '\'' +
                ", lineNumber=" + lineNumber +
                '}';
    }
}
