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

import org.rulii.annotation.Description;
import org.rulii.annotation.Param;
import org.rulii.bind.match.BindingMatchingStrategy;
import org.rulii.convert.Converter;
import org.rulii.lib.spring.core.annotation.AnnotationUtils;
import org.rulii.lib.spring.util.Assert;
import org.rulii.util.RuleUtils;
import org.rulii.util.reflect.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Defines a parameter within a method that is to be isPass dynamically (such as "when" and "then")
 *
 * The definition stores the parameter index, name of parameter (automatically discovered), generic type,
 * whether the parameter is required and any associated annotations.
 *
 * A parameter is deemed not required if it is annotated @Optional or it is declared with an Optional type.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public final class ParameterDefinition implements Definition {

    private static final Map<Method, List<ParameterDefinition>> CACHE = Collections.synchronizedMap(new IdentityHashMap<>());

    private int index;
    private String name;
    private String description;
    private Type type;
    private boolean containsGenericInfo;
    private final AnnotatedType annotatedType;
    private String defaultValueText;
    private Class<? extends BindingMatchingStrategy> matchUsing;
    private final List<Annotation> annotations;
    private boolean bindingType;
    private boolean optionalType;
    private Type underlyingType;
    private SourceDefinition sourceDefinition;

    private Object defaultValue = null;

    private ParameterDefinition(int index, String name, Type type, AnnotatedType annotatedType,
                                boolean containsGenericInfo,
                                String description, String defaultValueText,
                                Class<? extends BindingMatchingStrategy> matchUsing,
                                SourceDefinition sourceDefinition, List<Annotation> annotations) {
        super();
        Assert.isTrue(index >= 0, "Parameter index must be >= 0");
        setName(name);
        setType(type);
        setContainsGenericInfo(containsGenericInfo);
        setDescription(description);
        this.annotatedType = annotatedType;
        this.index = index;
        this.annotations = annotations;
        this.defaultValueText = defaultValueText;
        this.matchUsing = matchUsing;
        this.sourceDefinition = sourceDefinition;
        validate();
    }

    public static ParameterDefinition copy(ParameterDefinition original, int index, boolean containsGenericInfo) {
        return new ParameterDefinition(index, original.getName(), original.type, original.getAnnotatedType(),
                containsGenericInfo, original.getDescription(), original.getDefaultValueText(),
                original.getMatchUsing(), original.getSource(), original.annotations);
    }

    public void validate() {

        if (isBindingType() && getDefaultValueText() != null) {
            throw new IllegalArgumentException("Bindable parameters Binding<?> cannot have default values. " +
                    "For example : @Param(defaultValue = \"10\") Binding<Integer> value" + toString());
        }

        if (isOptionalType() && getDefaultValueText() != null) {
            throw new IllegalArgumentException("Optional parameters Optional<?> cannot have default values. " +
                    "For example : @Param(defaultValue = \"10\") Optional<Integer> value" + toString());
        }
    }

    public static List<ParameterDefinition> load(Method method, boolean containsGenericInfo, SourceDefinition sourceDefinition) {
        Assert.notNull(method, "method cannot be null.");
        return CACHE.computeIfAbsent(method, m -> loadInternal(m, containsGenericInfo, sourceDefinition));
    }

    /**
     * Loads the parameter definitions for the given method.
     *
     * @param method desired method
     * @param sourceDefinition source details.
     * @return all the parameter definitions for the given method.
     */
    private static List<ParameterDefinition> loadInternal(Method method, boolean containsGenericInfo, SourceDefinition sourceDefinition) {
        String[] parameterNames = ReflectionUtils.getParameterNames(method);
        Assert.isTrue(parameterNames.length == method.getParameterTypes().length,
                "parameterNames length does not match parameter types length");
        ParameterDefinition[] result = new ParameterDefinition[method.getParameterTypes().length];

        for (int i = 0; i < method.getGenericParameterTypes().length; i++) {
            Description descriptionAnnotation = method.getParameters()[i].getAnnotation(Description.class);
            Param param = AnnotationUtils.getAnnotation(method.getParameters()[i], Param.class);
            Annotation[] parameterAnnotations = method.getParameterAnnotations()[i];
            result[i] = new ParameterDefinition(i, parameterNames[i], method.getGenericParameterTypes()[i],
                    method.getAnnotatedParameterTypes()[i], containsGenericInfo,
                    descriptionAnnotation != null ? descriptionAnnotation.value() : null, getDefaultValueText(param),
                    getMatchUsing(param), sourceDefinition, parameterAnnotations  != null ? Arrays.asList(parameterAnnotations) : null);
        }

        return Collections.unmodifiableList(Arrays.asList(result));
    }

    private static String getDefaultValueText(Param param) {
        if (param == null) return null;
        return !Param.NOT_APPLICABLE.equals(param.defaultValue()) ? param.defaultValue() : null;
    }

    private static Class<? extends BindingMatchingStrategy> getMatchUsing(Param param) {
        if (param == null) return null;
        return !Param.NoOpBindingMatchingStrategy.class.equals(param.matchUsing()) ? param.matchUsing() : null;
    }

    /**
     * Returns the index of the parameter.
     *
     * @return Parameter index.
     */
    public int getIndex() {
        return index;
    }

    /**
     * Returns the name of the parameter.
     *
     * @return name of the parameter.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns the parameter description.
     *
     * @return parameter description.
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * Sets the name of the parameter.
     *
     * @param name name of the parameter.
     */
    void setName(String name) {
        Assert.isTrue(RuleUtils.isValidName(name), "Parameter name must match ["
                + RuleUtils.NAME_REGEX + "] Given [" + name + "]");
        this.name = name;
    }

    /**
     * Returns the Type of the parameter.
     *
     * @return type of the parameter.
     */
    public Type getType() {
        return type;
    }

    /**
     * Overrides the Type of the parameter.
     *
     * @param type desired type.
     */
    void setType(Type type) {
        Assert.notNull(type, "type cannot be null.");
        this.type = type;
        this.underlyingType = type;

        if (ReflectionUtils.isBinding(type)) {
            this.bindingType = true;
            this.underlyingType = ReflectionUtils.getUnderlyingBindingType(type);
        }

        if (ReflectionUtils.isOptional(type)) {
            this.optionalType = true;
            this.underlyingType = ReflectionUtils.getUnderlyingOptionalType(type);
        }

        // Assume the type was overridden correctly;
        setContainsGenericInfo(true);
    }

    /**
     * Determines if this parameter is of primitive type.
     *
     * @return true if primitive; false otherwise.
     */
    public boolean isPrimitive() {
        return type instanceof Class && ((Class<?>) type).isPrimitive();
    }

    /**
     * Returns the name of the parameter type. In case of classes it returns the simple name otherwise the full type name.
     *
     * @return name of the parameter type.
     */
    public String getTypeName() {
        if (type == null) return null;
        if (type instanceof Class) return ((Class) type).getSimpleName();
        return type.getTypeName();
    }

    /**
     * Type and Name.
     * @return Type and Name.
     */
    public String getTypeAndName() {
        return getTypeName() + " " + getName();
    }

    public AnnotatedType getAnnotatedType() {
        return annotatedType;
    }

    /**
     * Sets the description of this parameter.
     *
     * @param description description of this parameter.
     */
    void setDescription(String description) {
        this.description = description;
    }

    /**
     * Determines whether this parameter contains Generic Info.
     * @return true if the parameter contains generic info; false otherwise.
     */
    public boolean containsGenericInfo() {
        return containsGenericInfo;
    }

    void setContainsGenericInfo(boolean containsGenericInfo) {
        this.containsGenericInfo = containsGenericInfo;
    }

    /**
     * Default value text for this parameter if one is specified.
     *
     * @return default value text if specified; null otherwise.
     * @see Param
     */
    public String getDefaultValueText() {
        return defaultValueText;
    }

    void setDefaultValueText(String defaultValueText) {
        this.defaultValueText = defaultValueText;
    }

    /**
     * Determines whether a default value has been defined for this parameter.
     *
     * @return true if default is defined; false otherwise.
     */
    public boolean hasDefaultValue() {
        return defaultValueText != null;
    }

    /**
     * Returns the default value for this parameter definition.
     *
     * @param converter converter to be used.
     * @return default value if one exists; null otherwise.
     */
    public Object getDefaultValue(Converter<String, ?> converter) {
        if (defaultValue != null) return defaultValue;
        if (getDefaultValueText() == null) return null;
        Assert.notNull(converter, "converter cannot be null.");
        this.defaultValue = converter.convert(getDefaultValueText(), getType());
        return defaultValue;
    }

    /**
     * Determines whether this parameter is using custom Match. This done by specifying @Match on the parameter.
     *
     * @return true if it is using Match; false otherwise.
     * @see Param
     */
    public boolean isMatchSpecified() {
        return matchUsing != null;
    }

    /**
     * Return the strategy class to use when custom Match is specified.
     *
     * @return BindingMatchingStrategy class is one is specified; null otherwise.
     */
    public Class<? extends BindingMatchingStrategy> getMatchUsing() {
        return matchUsing;
    }

    void setMatchUsing(Class<? extends BindingMatchingStrategy> matchUsing) {
        this.matchUsing = matchUsing;
    }

    /**
     * Returns true if this is a Binding.
     *
     * @return true if this is a Binding; false otherwise.
     */
    public boolean isBindingType() {
        return bindingType;
    }

    /**
     * Returns true if this is a Optional field.
     *
     * @return true if this is a Optional field; false otherwise.
     */
    public boolean isOptionalType() {
        return optionalType;
    }

    public Type getUnderlyingType() {
        return underlyingType;
    }

    /**
     * Returns all the associated annotations for this parameter.
     *
     * @return all annotations for this parameter.
     */
    public List<Annotation> getAnnotations() {
        return annotations;
    }

    @Override
    public SourceDefinition getSource() {
        return sourceDefinition;
    }

    @Override
    public String toString() {
        return "ParameterDefinition{" +
                "index=" + index +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", type=" + type +
                ", annotatedType=" + annotatedType +
                ", defaultValueText='" + defaultValueText + '\'' +
                ", matchUsing=" + matchUsing +
                ", annotations=" + annotations +
                ", bindingType=" + bindingType +
                ", optionalType=" + optionalType +
                ", underlyingType=" + underlyingType +
                ", sourceDefinition=" + sourceDefinition +
                ", defaultValue=" + defaultValue +
                '}';
    }
}
