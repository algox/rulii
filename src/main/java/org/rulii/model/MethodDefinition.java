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
package org.rulii.model;

import org.rulii.annotation.Description;
import org.rulii.lib.spring.util.Assert;
import org.rulii.util.reflect.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Defines a method is to be isPass dynamically (such as "when" and "then")
 *
 * The definition stores the reflective method and its associated parameter definitions.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public final class MethodDefinition implements Definition {

    private static final Map<Method, MethodDefinition> CACHE = Collections.synchronizedMap(new IdentityHashMap<>());

    private final Method method;
    // Determines whether this method parameters contain generic info
    private final boolean containsGenericInfo;
    private final SourceDefinition sourceDefinition;
    private final List<ParameterDefinition> parameterDefinitions;
    private final ReturnTypeDefinition returnTypeDefinition;
    private final Map<String, ParameterDefinition> paramNameMap = new HashMap<>();

    // Name of the action
    private String name;
    // Description of the Action
    private String description;
    // Return type of the method
    private Type returnType;

    public MethodDefinition(Method method, boolean containsGenericInfo, String description,
                            SourceDefinition sourceDefinition,
                            ReturnTypeDefinition returnTypeDefinition,
                            List<ParameterDefinition> parameterDefinitions) {
        super();
        Assert.notNull(method, "method cannot be null");
        this.method = method;
        this.name = method.getName();
        this.containsGenericInfo = containsGenericInfo;
        this.description = description;
        this.returnType = method.getGenericReturnType();
        this.sourceDefinition = sourceDefinition;
        this.returnTypeDefinition = returnTypeDefinition;
        this.parameterDefinitions = parameterDefinitions != null
                ? Collections.unmodifiableList(parameterDefinitions)
                : Collections.emptyList();
        createParameterNameIndex();
        validate();
    }

    public void validate() {
        HashSet<Integer> indexRange = IntStream.range(0, parameterDefinitions.size()).collect(HashSet::new, Set::add, Set::addAll);

        for (ParameterDefinition definition : parameterDefinitions) {
            if (!paramNameMap.containsKey(definition.getName())) {
                throw new UnrulyException("Could not find parameter name [" + definition.getName()
                        + "] in index [" + paramNameMap + "]. Was the index created?");
            }
            indexRange.remove(definition.getIndex());
        }

        if (!indexRange.isEmpty()) {
            throw new UnrulyException("Parameter indexes should be contiguous. Invalid index [" + indexRange + "]");
        }
    }

    /**
     * Loads all the associated method definition that match the given predicate.
     *
     * @param c container class.
     * @param predicate matcher.
     * @param sourceDefinition source details.
     * @return all matching MethodDefinitions
     */
    public static List<MethodDefinition> load(Class<?> c, Predicate<Method> predicate, SourceDefinition sourceDefinition) {
        Method[] matches = ReflectionUtils.getMethods(c, predicate);
        List<MethodDefinition> result = new ArrayList<>(matches.length);
        Arrays.stream(matches).forEach(m -> result.add(load(m, true, sourceDefinition)));
        return Collections.unmodifiableList(result);
    }

    public static MethodDefinition load(Method method, boolean containsGenericInfo, SourceDefinition sourceDefinition) {
        Assert.notNull(method, "method cannot be null.");
        return CACHE.computeIfAbsent(method, m -> loadInternal(m, containsGenericInfo, sourceDefinition));
    }

    private static MethodDefinition loadInternal(Method method, boolean containsGenericInfo, SourceDefinition sourceDefinition) {
        Assert.notNull(method, "method cannot be null");
        Description descriptionAnnotation = method.getAnnotation(Description.class);
        return new MethodDefinition(method, containsGenericInfo,
                descriptionAnnotation != null ? descriptionAnnotation.value() : null, sourceDefinition,
                ReturnTypeDefinition.load(method, sourceDefinition),
                ParameterDefinition.load(method, containsGenericInfo, sourceDefinition));
    }

    /**
     * Reflective method behind the Method Definition.
     *
     * @return reflective method.
     */
    public Method getMethod() {
        return method;
    }

    public ReturnTypeDefinition getReturnTypeDefinition() {
        return returnTypeDefinition;
    }

    /**
     * All the parameter definitions for this method definition.
     *
     * @return parameter meta information.
     */
    public List<ParameterDefinition> getParameterDefinitions() {
        return parameterDefinitions;
    }

    public int getParameterCount() {
        return parameterDefinitions.size();
    }

    /**
     * Parameter Definition at the given index.
     *
     * @param index parameter index.
     * @return Parameter Definition.
     */
    public ParameterDefinition getParameterDefinition(int index) {

        if (getParameterDefinitions().isEmpty()) {
            throw new UnrulyException("There are no args found in this function. [" + method + "]");
        }

        if (index < 0 || index >= getParameterDefinitions().size()) {
            throw new UnrulyException("Invalid parameter index [" + index + "] it must be between [0, "
                    + getParameterDefinitions().size() + "] method [" + method + "]");
        }

        return getParameterDefinitions().get(index);
    }

    /**
     * Parameter Definition with the given name.
     *
     * @param name parameter name.
     * @return Parameter Definition.
     */
    public ParameterDefinition getParameterDefinition(String name) {
        return paramNameMap.get(name);
    }

    /**
     * Retrieves the name of this definition.
     *
     * @return name of the method or overridden value.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Determines whether this method parameters contain generic info.
     * @return true if the method parameters contain generic info; false otherwise.
     */
    public boolean containsGenericInfo() {
        return containsGenericInfo;
    }

    /**
     * Description of this method.
     *
     * @return method description.
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * Overrides the name of the method.
     *
     * @param name new value/
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the description of this method.
     *
     * @param description method description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Return type of this method.
     *
     * @return method return type.
     */
    public Type getReturnType() {
        return returnType;
    }

    /**
     * Overridden method return type with the given value.
     *
     * @param returnType new value.
     */
    public void setReturnType(Type returnType) {
        Assert.notNull(returnType, "returnType cannot be null.");
        this.returnType = returnType;
    }

    @Override
    public SourceDefinition getSource() {
        return sourceDefinition;
    }

    /**
     * Determines whether the method is static.
     *
     * @return return true if it's a static method; false otherwise.
     */
    public boolean isStatic() {
        return Modifier.isStatic(method.getModifiers());
    }

    /**
     * Returns a simplified version of the method signature.
     *
     * @return simplified method signature.
     */
    public String getSignature() {
        return getMethod().getDeclaringClass().getSimpleName() + "."
                + getName() + "(" + getMethodParameterSignature() + ")";
    }

    public String getMethodParameterSignature() {
        return getParameterDefinitions().stream()
                .map(p -> p.getTypeName() + " " + p.getName())
                .collect(Collectors.joining(", "));
    }

    /**
     * Creates a lookup between the parameter and it's name.
     */
    public void createParameterNameIndex() {
        if (parameterDefinitions == null || parameterDefinitions.isEmpty()) return;
        paramNameMap.clear();
        // Create the param name map
        parameterDefinitions.forEach(param -> {
            if (paramNameMap.containsKey(param.getName())) throw new UnrulyException("Parameter [" + param.getName() + "] already exists.");
            paramNameMap.put(param.getName(), param);
        });
    }

    @Override
    public String toString() {
        return "MethodDefinition{" +
                "method=" + method +
                ", source=" + sourceDefinition +
                ", parameterDefinitions=" + parameterDefinitions +
                ", returnTypeDefinition=" + returnTypeDefinition +
                ", paramNameMap=" + paramNameMap +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", returnType=" + returnType +
                '}';
    }
}
