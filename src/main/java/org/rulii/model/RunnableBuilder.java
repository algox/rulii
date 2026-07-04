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

import org.rulii.lib.apache.commons.logging.Log;
import org.rulii.lib.apache.commons.logging.LogFactory;
import org.rulii.lib.spring.util.Assert;
import org.rulii.util.reflect.LambdaUtils;
import org.rulii.util.reflect.MethodResolver;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Parent builder class for Lambda based Runnable(s).
 *
 * @author Max Arulananthan.
 * @since 1.0
 *
 */
public abstract class RunnableBuilder<T extends RunnableBuilder<?,?>, R extends Runnable<?>> {

    private static final Log LOGGER = LogFactory.getLog(RunnableBuilder.class);
    private static final MethodResolver METHOD_RESOLVER = MethodResolver.builder().build();

    private final Object target;
    private final MethodDefinition definition;

    protected RunnableBuilder(Object target, MethodDefinition definition) {
        super();
        Assert.notNull(definition, "actionMethod cannot be null.");
        this.target = target;
        // Defensive copy: definition may be a shared instance from MethodDefinition's cache
        // (see MethodDefinition#load); customizing this builder (name/description/param edits)
        // must not mutate that shared, cached original out from under unrelated Runnables built
        // from the same underlying reflective Method.
        this.definition = definition.copy();
    }

    /**
     * Builds a new instance of the desired runnable.
     *
     * @return new runnable.
     */
    public abstract R build();

    /**
     * Loads the given target.
     *
     * @param target target object.
     * @param targetMethod target method.
     *
     * @return matched method.
     */
    public static MethodInfo load(Object target, Method targetMethod) {
        Assert.notNull(target, "function cannot be null.");
        Assert.notNull(targetMethod, "targetMethod cannot be null.");

        SerializedLambda serializedLambda = LambdaUtils.getSafeSerializedLambda(target);

        if (serializedLambda != null) {
            return loadLambda(target, targetMethod, serializedLambda);
        }

        // See if we need to find the implementation (so we have can access the parameter names);
        Method implementationMethod = getImplementationMethod(target.getClass(), targetMethod);
        // If this happens we have a bug in getImplementationMethod().
        if (implementationMethod == null) throw new UnrulyException("Unable to find implementation method for [" + targetMethod + "]");

        return new MethodInfo(target, MethodDefinition.load(implementationMethod, true, SourceDefinition.build()));
    }

    /**
     * Edit parameter with the given index.
     *
     * @param index parameter index.
     * @return new parameter editor.
     */
    @SuppressWarnings("unchecked")
    public ParameterDefinitionEditor<T> param(int index) {
        return ParameterDefinitionEditor.with(getDefinition().getParameterDefinition(index), this);
    }

    /**
     * Edit parameter with the given name.
     *
     * @param name parameter name.
     * @return new parameter editor.
     */
    @SuppressWarnings("unchecked")
    public ParameterDefinitionEditor<T> param(String name) {
        ParameterDefinition definition = getDefinition().getParameterDefinition(name);

        if (definition == null) {
            throw new UnrulyException("No such parameter found [" + name + "] in method [" + getDefinition().getMethod() + "]");
        }

        return ParameterDefinitionEditor.with(definition, this);
    }

    /**
     * Loads the given functional lambda.
     *
     * @param function target function.
     * @param functionMethod implementation method.
     * @param serializedLambda serialized function.
     * @return matched method.
     */
    protected static MethodInfo loadLambda(Object function, Method functionMethod, SerializedLambda serializedLambda) {
        Assert.notNull(functionMethod, "functionMethod cannot be null.");
        Assert.notNull(serializedLambda, "serializedLambda cannot be null.");
        MethodDefinition methodDefinition = null;

        try {
            Class<?> implementationClass = LambdaUtils.getImplementationClass(serializedLambda);
            Method implementationMethod = LambdaUtils.getImplementationMethod(serializedLambda, implementationClass);
            MethodDefinition implementationMethodDefinition = MethodDefinition.load(implementationMethod, true, SourceDefinition.build());

            ParameterDefinition[] parameterDefinitions = new ParameterDefinition[functionMethod.getParameterCount()];
            int delta = implementationMethod.getParameterCount() - functionMethod.getParameterCount();

            for (int i = delta; i < implementationMethod.getParameterCount(); i++) {
                int index = i - delta;
                parameterDefinitions[index] = ParameterDefinition.copy(implementationMethodDefinition.getParameterDefinition(i), index, false);
            }

            methodDefinition = new MethodDefinition(functionMethod, false,
                    implementationMethodDefinition.getDescription(), SourceDefinition.build(),
                    implementationMethodDefinition.getReturnTypeDefinition(),
                    Arrays.asList(parameterDefinitions));
            methodDefinition.setReturnType(implementationMethod.getGenericReturnType());

        } catch (Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Unable to resolve lambda implementation method for [" + functionMethod
                        + "]; falling back to functional-interface parameter names.", e);
            }
        }

        if (methodDefinition == null) {
            methodDefinition = MethodDefinition.load(functionMethod, true, SourceDefinition.build());
        }

        return new MethodInfo(function, methodDefinition);
    }

    public Object getTarget() {
        return target;
    }

    public MethodDefinition getDefinition() {
        return definition;
    }

    private static Method getImplementationMethod(Class<?> c, Method candidate) {
        return METHOD_RESOLVER.getImplementationMethod(c, candidate);
    }

    public record MethodInfo(Object target, MethodDefinition definition) {}
}
