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

import org.rulii.lib.spring.util.Assert;
import org.rulii.lib.spring.util.ClassUtils;
import org.rulii.model.function.Function;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * The result type of a rule set or a rule flow, together with where it came from.
 *
 * <p>Resolved once, at build time, in this order:
 * <ol>
 *     <li>{@link Origin#DECLARED}: the author stated it ({@code resultType(...)},
 *     {@code resultExtractor(Class, Function)}, {@code returning(Class, Function)}).</li>
 *     <li>{@link Origin#DEFAULT}: the engine installed its own result extractor and knows what it
 *     returns (a rule set without a result extractor returns its
 *     {@link org.rulii.ruleset.RuleSetExecutionStatus}).</li>
 *     <li>{@link Origin#DERIVED}: read off the result extractor's declared return type, when that is
 *     concrete (see {@link #concreteReturnType(Function)}).</li>
 *     <li>{@link Origin#UNKNOWN}: there is a result, but its type cannot be told: a script, a generic
 *     lambda, a function without a method definition.</li>
 * </ol>
 * {@link Origin#NONE} is a rule flow that returns its {@link org.rulii.context.RuleContext} rather than a
 * value of the author's choosing.
 *
 * <p>The type is a declaration, like an input parameter's type: it is recorded and reported, never
 * enforced at run time.
 *
 * @author Max Arulananthan
 * @since 2.2
 */
public final class ResultType {

    /** Where a result type came from. */
    public enum Origin {
        /** Stated by the author. */
        DECLARED,
        /** Read off the result extractor's declared return type. */
        DERIVED,
        /** The engine's own default result extractor. */
        DEFAULT,
        /** There is a result; its type cannot be told. */
        UNKNOWN,
        /** A rule flow returning its rule context. */
        NONE
    }

    private static final ResultType UNKNOWN_RESULT = new ResultType(null, Origin.UNKNOWN);
    private static final ResultType NONE_RESULT    = new ResultType(null, Origin.NONE);

    private final Type type;
    private final Origin origin;

    private ResultType(Type type, Origin origin) {
        super();
        this.type = type;
        this.origin = origin;
    }

    /**
     * The author stated the type.
     *
     * @param type the declared type; must not be null.
     * @return a declared result type.
     */
    public static ResultType declared(Type type) {
        Assert.notNull(type, "type cannot be null.");
        return new ResultType(type, Origin.DECLARED);
    }

    /**
     * The type was read off the result extractor.
     *
     * @param type the extractor's declared return type; must not be null.
     * @return a derived result type.
     */
    public static ResultType derived(Type type) {
        Assert.notNull(type, "type cannot be null.");
        return new ResultType(type, Origin.DERIVED);
    }

    /**
     * The engine's own default result extractor, whose type the engine knows.
     *
     * @param type what the default extractor returns; must not be null.
     * @return a default result type.
     */
    public static ResultType byDefault(Type type) {
        Assert.notNull(type, "type cannot be null.");
        return new ResultType(type, Origin.DEFAULT);
    }

    /**
     * There is a result but its type cannot be told.
     *
     * @return the unknown result type.
     */
    public static ResultType unknown() {
        return UNKNOWN_RESULT;
    }

    /**
     * A rule flow that returns its rule context.
     *
     * @return the none result type.
     */
    public static ResultType none() {
        return NONE_RESULT;
    }

    /**
     * Resolves the result type: the explicit type when given, else the extractor's concrete return
     * type, else unknown.
     *
     * @param explicit  the type the author stated; may be null.
     * @param extractor the result extractor; may be null.
     * @return the resolved result type; never null.
     */
    public static ResultType resolve(Type explicit, Function<?> extractor) {
        if (explicit != null) return declared(explicit);
        return derive(extractor != null ? extractor.getDefinition() : null);
    }

    /**
     * Derives the result type from a result extractor's method definition: its return type when
     * concrete, else unknown.
     *
     * @param definition the extractor's definition; may be null.
     * @return derived or unknown; never null.
     */
    public static ResultType derive(MethodDefinition definition) {
        Type concrete = concreteReturnType(definition);
        return concrete != null ? derived(concrete) : unknown();
    }

    /**
     * The concrete return type of a function, when the engine can tell what it is: a {@link Class}
     * other than {@code Object} or {@code void}, or a {@link ParameterizedType}. A type variable, a
     * wildcard, {@code Object} (what a generic lambda and a script function report) and a function
     * without a definition all give null.
     *
     * @param function the function; may be null.
     * @return the concrete return type, or null.
     */
    public static Type concreteReturnType(Function<?> function) {
        return function != null ? concreteReturnType(function.getDefinition()) : null;
    }

    /**
     * The concrete return type of a method definition, as {@link #concreteReturnType(Function)}.
     *
     * @param definition the method definition; may be null.
     * @return the concrete return type, or null.
     */
    public static Type concreteReturnType(MethodDefinition definition) {
        if (definition == null) return null;

        Type returnType = definition.getReturnType();

        if (returnType instanceof ParameterizedType) return returnType;
        if (!(returnType instanceof Class<?> clazz)) return null;
        if (clazz == Object.class || clazz == void.class || clazz == Void.class) return null;

        return returnType;
    }

    /**
     * The type, when known.
     *
     * @return the type; null for {@link Origin#UNKNOWN} and {@link Origin#NONE}.
     */
    public Type getType() {
        return type;
    }

    /**
     * Where the type came from.
     *
     * @return the origin; never null.
     */
    public Origin getOrigin() {
        return origin;
    }

    /**
     * Whether the type is known: declared, derived or default.
     *
     * @return true when {@link #getType()} is not null.
     */
    public boolean isKnown() {
        return type != null;
    }

    /**
     * Whether a value of the candidate type can be assigned to this type. Compares raw classes (a
     * parameterized type by its raw type), boxing primitives, so {@code Number} accepts
     * {@code Integer} and {@code int}, and {@code List<String>} accepts {@code ArrayList}.
     *
     * @param candidate the type of the value; must not be null.
     * @return true when assignable; false when this type is not known.
     */
    public boolean accepts(Type candidate) {
        Assert.notNull(candidate, "candidate cannot be null.");
        if (!isKnown()) return false;

        Class<?> target = rawClass(type);
        Class<?> source = rawClass(candidate);

        if (target == null || source == null) return false;

        return ClassUtils.isAssignable(target, source);
    }

    /**
     * A short description: the simple type name and the origin, or just the origin when there is no
     * type. For example {@code RuleViolations (derived)}, {@code List<String> (declared)},
     * {@code unknown}.
     *
     * @return the description; never null.
     */
    public String getDescription() {
        if (!isKnown()) return origin.name().toLowerCase();
        return simpleName(type) + " (" + origin.name().toLowerCase() + ")";
    }

    private static Class<?> rawClass(Type type) {
        if (type instanceof Class<?> clazz) return clazz;
        if (type instanceof ParameterizedType parameterized && parameterized.getRawType() instanceof Class<?> raw) return raw;
        return null;
    }

    /**
     * The simple name of a type: {@code RuleViolations}, {@code List<String>}, {@code int}.
     *
     * @param type the type; must not be null.
     * @return the simple name; never null.
     */
    public static String simpleName(Type type) {
        Assert.notNull(type, "type cannot be null.");
        if (type instanceof Class<?> clazz) return clazz.getSimpleName();

        if (type instanceof ParameterizedType parameterized) {
            StringBuilder result = new StringBuilder(simpleName(parameterized.getRawType())).append('<');
            Type[] arguments = parameterized.getActualTypeArguments();

            for (int i = 0; i < arguments.length; i++) {
                if (i > 0) result.append(", ");
                result.append(simpleName(arguments[i]));
            }

            return result.append('>').toString();
        }

        return type.getTypeName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResultType other)) return false;
        return Objects.equals(type, other.type) && origin == other.origin;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, origin);
    }

    @Override
    public String toString() {
        return "ResultType{" +
                "type=" + type +
                ", origin=" + origin +
                '}';
    }
}
