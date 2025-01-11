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
package org.rulii.model.function;

import org.rulii.lib.spring.core.BridgeMethodResolver;
import org.rulii.lib.spring.core.annotation.AnnotationUtils;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.MethodDefinition;
import org.rulii.model.RunnableBuilder;
import org.rulii.model.SourceDefinition;
import org.rulii.model.UnrulyException;
import org.rulii.util.reflect.ObjectFactory;
import org.rulii.util.reflect.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.function.Predicate;

/**
 * Convenient Builder class to creating new class based or lambda based Function.
 *
 * @author Max Arulananthan
 * @since 1.0
 * @see Function
 */
public final class FunctionBuilderBuilder {

    private static final FunctionBuilderBuilder instance = new FunctionBuilderBuilder();

    private static final Predicate<Method> FILTER = m -> ReflectionUtils
            .isAnnotated(m, org.rulii.annotation.Function.class)
            && Modifier.isPublic(m.getModifiers()) && !m.isBridge();

    public static FunctionBuilderBuilder builder() {
        return getInstance();
    }

    private FunctionBuilderBuilder() {
        super();
    }

    public static FunctionBuilderBuilder getInstance() {
        return instance;
    }

    public Function[] build(Class<?> clazz) {
        Assert.notNull(clazz, "clazz cannot be null.");
        ObjectFactory objectFactory = ObjectFactory.builder().build();
        return build(clazz, objectFactory);
    }

    public Function[] build(Class<?> clazz, ObjectFactory factory) {
        return build(factory.createFunction(clazz));
    }

    /**
     * Introspect the given object for methods that are annotated with @Function Annotation and build corresponding functions for them.
     *
     * @param target target object.
     * @return array of function inside the input class.
     */
    public Function[] build(Object target) {
        return build(target, org.rulii.annotation.Function.class);
    }

    private <T> FunctionBuilder<T> with(Object target, MethodDefinition definition) {
        return new FunctionBuilder(target, definition);
    }

    /**
     * Introspect the given object for methods that are annotated with the required Annotation and build corresponding functions for them.
     *
     * @param target target object
     * @param annotationClass desired Function marker.            .
     * @return array of functions inside the input class.
     */
    public Function[] build(Object target, Class<? extends Annotation> annotationClass) {
        Assert.notNull(annotationClass, "annotationClass cannot be null.");
        Class<?> clazz = target.getClass();
        Method[] candidates = ReflectionUtils.getMethodsWithAnnotation(clazz, annotationClass);

        Function[] result = new Function[candidates.length];

        for (int i = 0; i < candidates.length; i++) {
            String name = extractName(candidates[0]);
            Method candidate = BridgeMethodResolver.findBridgedMethod(candidates[0]);
            FunctionBuilder builder = with(target, MethodDefinition.load(candidate, true, SourceDefinition.build()));
            if (name != null) builder.name(name);
            result[i] = builder.build();
        }

        return result;
    }

    private static <T> FunctionBuilder<T> withFunction(Object target) {
        Method[] candidates = ReflectionUtils.getMethods(target.getClass(), FILTER);

        if (candidates == null || candidates.length == 0) {
            throw new UnrulyException("Function method not found on class [" + target.getClass() + "]");
        }

        // Too many Actions declared
        if (candidates.length > 1) {
            throw new UnrulyException("Too many function methods found on class [" + target.getClass() + "]. Candidates ["
                    + Arrays.toString(candidates) + "]");
        }

        String name = extractName(candidates[0]);
        Method candidate = BridgeMethodResolver.findBridgedMethod(candidates[0]);
        RunnableBuilder.MethodInfo methodInfo = RunnableBuilder.load(target, candidate);

        FunctionBuilder result = (FunctionBuilder<T>) new FunctionBuilder(methodInfo.getTarget(), methodInfo.getDefinition());

        if (name != null) result.name(name);

        return result;
    }

    /**
     * Creates a new function builder with no arguments.
     *
     * @param function desired action.
     * @param <T> generic return type of the function.
     * @return new FunctionBuilder with no arguments.
     */
    public <T> FunctionBuilder<T> with(NoArgFunction<T> function) {
        return withFunction(function);
    }

    /**
     * Creates a new function builder with one argument.
     *
     * @param function desired action.
     * @param <A> generic type of the first parameter.
     * @param <T> generic return type of the function.
     * @return new FunctionBuilder with one arguments.
     */
    public <T, A> FunctionBuilder<T> with(UnaryFunction<T, A> function) {
        return withFunction(function);
    }

    /**
     * Creates a new function builder with two argument.
     *
     * @param function desired action.
     * @param <A> generic type of the first parameter.
     * @param <B> generic type of the second parameter.
     * @param <T> generic return type of the function.
     * @return new FunctionBuilder with two arguments.
     */
    public <T, A, B> FunctionBuilder<T> with(BiFunction<T, A, B> function) {
        return withFunction(function);
    }

    /**
     * Creates a new function builder with three argument.
     *
     * @param function desired action.
     * @param <A> generic type of the first parameter.
     * @param <B> generic type of the second parameter.
     * @param <C> generic type of the third parameter.
     * @param <T> generic return type of the function.
     * @return new FunctionBuilder with three arguments.
     */
    public <T, A, B, C> FunctionBuilder<T> with(TriFunction<T, A, B, C> function) {
        return withFunction(function);
    }

    /**
     * Creates a new function builder with four argument.
     *
     * @param function desired action.
     * @param <A> generic type of the first parameter.
     * @param <B> generic type of the second parameter.
     * @param <C> generic type of the third parameter.
     * @param <D> generic type of the fourth parameter.
     * @param <T> generic return type of the function.
     * @return new FunctionBuilder with four arguments.
     */
    public <T, A, B, C, D> FunctionBuilder<T> with(QuadFunction<T, A, B, C, D> function) {
        return withFunction(function);
    }

    /**
     * Creates a new function builder with five argument.
     *
     * @param function desired action.
     * @param <A> generic type of the first parameter.
     * @param <B> generic type of the second parameter.
     * @param <C> generic type of the third parameter.
     * @param <D> generic type of the fourth parameter.
     * @param <E> generic type of the fifth parameter.
     * @param <T> generic return type of the function.
     * @return new FunctionBuilder with five arguments.
     */
    public <T, A, B, C, D, E> FunctionBuilder<T> with(QuinFunction<T, A, B, C, D, E> function) {
        return withFunction(function);
    }

    /**
     * Creates a new function builder with six argument.
     *
     * @param function desired action.
     * @param <A> generic type of the first parameter.
     * @param <B> generic type of the second parameter.
     * @param <C> generic type of the third parameter.
     * @param <D> generic type of the fourth parameter.
     * @param <E> generic type of the fifth parameter.
     * @param <F> generic type of the sixth parameter.
     * @param <T> generic return type of the function.
     * @return new FunctionBuilder with six arguments.
     */
    public <T, A, B, C, D, E, F> FunctionBuilder<T> with(SexFunction<T, A, B, C, D, E, F> function) {
        return withFunction(function);
    }

    /**
     * Creates a new function builder with seven argument.
     *
     * @param function desired action.
     * @param <A> generic type of the first parameter.
     * @param <B> generic type of the second parameter.
     * @param <C> generic type of the third parameter.
     * @param <D> generic type of the fourth parameter.
     * @param <E> generic type of the fifth parameter.
     * @param <F> generic type of the sixth parameter.
     * @param <G> generic type of the seventh parameter.
     * @param <T> generic return type of the function.
     * @return new FunctionBuilder with seven arguments.
     */
    public <T, A, B, C, D, E, F, G> FunctionBuilder<T> with(SeptFunction<T, A, B, C, D, E, F, G> function) {
        return withFunction(function);
    }

    /**
     * Creates a new function builder with eight argument.
     *
     * @param function desired action.
     * @param <A> generic type of the first parameter.
     * @param <B> generic type of the second parameter.
     * @param <C> generic type of the third parameter.
     * @param <D> generic type of the fourth parameter.
     * @param <E> generic type of the fifth parameter.
     * @param <F> generic type of the sixth parameter.
     * @param <G> generic type of the seventh parameter.
     * @param <H> generic type of the eighth parameter.
     * @param <T> generic return type of the function.
     * @return new FunctionBuilder with eight arguments.
     */
    public <T, A, B, C, D, E, F, G, H> FunctionBuilder<T> with(OctFunction<T, A, B, C, D, E, F, G, H> function) {
        return withFunction(function);
    }

    /**
     * Creates a new function builder with nine argument.
     *
     * @param function action action.
     * @param <A> generic type of the first parameter.
     * @param <B> generic type of the second parameter.
     * @param <C> generic type of the third parameter.
     * @param <D> generic type of the fourth parameter.
     * @param <E> generic type of the fifth parameter.
     * @param <F> generic type of the sixth parameter.
     * @param <G> generic type of the seventh parameter.
     * @param <H> generic type of the eighth parameter.
     * @param <I> generic type of the ninth parameter.
     * @param <T> generic return type of the function.
     * @return new FunctionBuilder with nine arguments.
     */
    public <T, A, B, C, D, E, F, G, H, I> FunctionBuilder<T> with(NovFunction<T, A, B, C, D, E, F, G, H, I> function) {
        return withFunction(function);
    }

    /**
     * Creates a new function builder with ten argument.
     *
     * @param function desired action.
     * @param <A> generic type of the first parameter.
     * @param <B> generic type of the second parameter.
     * @param <C> generic type of the third parameter.
     * @param <D> generic type of the fourth parameter.
     * @param <E> generic type of the fifth parameter.
     * @param <F> generic type of the sixth parameter.
     * @param <G> generic type of the seventh parameter.
     * @param <H> generic type of the eighth parameter.
     * @param <I> generic type of the ninth parameter.
     * @param <J> generic type of the ninth parameter.
     * @param <T> generic return type of the function.
     * @return new FunctionBuilder with ten arguments.
     */
    public <T, A, B, C, D, E, F, G, H, I, J> FunctionBuilder<T> with(DecFunction<T, A, B, C, D, E, F, G, H, I, J> function) {
        return withFunction(function);
    }

    private static String extractName(Method method) {
        org.rulii.annotation.Function function = AnnotationUtils.getAnnotation(method, org.rulii.annotation.Function.class);
        if (function == null) return method.getName();
        return org.rulii.annotation.Function.NOT_APPLICABLE.equals(function.name()) ? method.getName() : function.name();
    }
}
