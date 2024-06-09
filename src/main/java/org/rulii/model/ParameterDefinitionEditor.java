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

import org.rulii.bind.BindingDeclaration;
import org.rulii.bind.Bindings;
import org.rulii.bind.match.BindingMatchingStrategy;
import org.rulii.lib.spring.core.annotation.AnnotationUtils;
import org.rulii.lib.spring.util.Assert;
import org.rulii.util.TypeReference;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * Editor for updating parameter values.
 *
 * @param <T> parameter type.
 *
 * @author Max Arulananthan.
 * @since 1.0
 */
public class ParameterDefinitionEditor<T> {

    private final ParameterDefinition target;
    private final T returnType;

    private ParameterDefinitionEditor(ParameterDefinition target, T returnType) {
        super();
        Assert.notNull(target, "target cannot be null.");
        Assert.notNull(returnType, "returnType cannot be null.");
        this.target = target;
        this.returnType = returnType;
    }

    public static <T> ParameterDefinitionEditor with(ParameterDefinition target, T returnType) {
        return new ParameterDefinitionEditor(target, returnType);
    }

    /**
     * Update the name of the parameter.
     *
     * @param name new parameter name.
     * @return this Editor.
     */
    public ParameterDefinitionEditor<T> name(String name) {
        target.setName(name);
        return this;
    }

    /**
     * Update the parameter type.
     *
     * @param type new type.
     * @return this Editor.
     */
    public ParameterDefinitionEditor<T> type(Type type) {
        target.setType(type);
        return this;
    }

    /**
     * Update the parameter type.
     *
     * @param typeReference new type.
     * @param <S> new parameter type.
     * @return this Editor.
     */
    public <S> ParameterDefinitionEditor<T> type(TypeReference<S> typeReference) {
        Assert.notNull(typeReference, "typeReference cannot be null.");
        target.setType(typeReference.getType());
        return this;
    }

    /**
     * Update the parameter description.
     * @param description parameter description.
     * @return this Editor.
     */
    public ParameterDefinitionEditor<T> description(String description) {
        target.setDescription(description);
        return this;
    }

    /**
     * Update the default value text.
     * @param defaultValueText parameter default text value.
     * @return this Editor.
     */
    public ParameterDefinitionEditor<T> defaultValueText(String defaultValueText) {
        target.setDefaultValueText(defaultValueText);
        return this;
    }

    /**
     * Update the matching strategy.
     * @param matchUsing parameter matching strategy.
     * @return this Editor.
     */
    public ParameterDefinitionEditor<T> matchUsing(Class<? extends BindingMatchingStrategy> matchUsing) {
        target.setMatchUsing(matchUsing);
        return this;
    }

    public ParameterDefinitionEditor<T> annotate(Class<? extends Annotation> annotationType, BindingDeclaration...bindings) {
        Bindings attributes = bindings != null ? Bindings.builder().standard(bindings) : Bindings.builder().standard();
        AnnotationUtils.synthesizeAnnotation((Map<String, Object>) attributes.asMap(), annotationType, null);
        return this;
    }

    public T build() {
        target.validate();
        return returnType;
    }
}
