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
import org.rulii.lib.spring.util.Assert;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

public class ReturnTypeDefinition implements Definition {

    private static final Map<Method, ReturnTypeDefinition> CACHE = Collections.synchronizedMap(new IdentityHashMap<>());

    private final String description;
    private final Type type;
    private final AnnotatedType annotatedType;
    private final Annotation[] annotations;
    private final SourceDefinition sourceDefinition;

    private ReturnTypeDefinition(String description, Type type, AnnotatedType annotatedType, SourceDefinition sourceDefinition,
                                Annotation...annotations) {
        super();
        this.description = description;
        this.type = type;
        this.annotatedType = annotatedType;
        this.sourceDefinition = sourceDefinition;
        this.annotations = annotations;
    }

    public static ReturnTypeDefinition load(Method method, SourceDefinition sourceDefinition) {
        Assert.notNull(method, "method cannot be null.");
        return CACHE.computeIfAbsent(method, m -> loadInternal(m, sourceDefinition));
    }

    private static ReturnTypeDefinition loadInternal(Method method, SourceDefinition sourceDefinition) {
        Description descriptionAnnotation = method.getAnnotatedReturnType().getAnnotation(Description.class);
        return new ReturnTypeDefinition(descriptionAnnotation != null ? descriptionAnnotation.value() : null,
                method.getGenericReturnType(), method.getAnnotatedReturnType(), sourceDefinition, method.getDeclaredAnnotations());
    }

    @Override
    public String getName() {
        return "return";
    }

    public String getDescription() {
        return description;
    }

    public Type getType() {
        return type;
    }

    public AnnotatedType getAnnotatedType() {
        return annotatedType;
    }

    public Annotation[] getAnnotations() {
        return annotations;
    }

    public SourceDefinition getSource() {
        return sourceDefinition;
    }
}
