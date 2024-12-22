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
package org.rulii.model.condition;

import org.rulii.annotation.Function;
import org.rulii.config.RuliiSystem;
import org.rulii.model.RunnableBuilder;
import org.rulii.model.action.Action;
import org.rulii.model.function.*;
import org.rulii.lib.spring.core.BridgeMethodResolver;
import org.rulii.lib.spring.core.annotation.AnnotationUtils;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.MethodDefinition;
import org.rulii.model.SourceDefinition;
import org.rulii.model.UnrulyException;
import org.rulii.util.reflect.ObjectFactory;
import org.rulii.util.reflect.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * Convenient Builder class to creating new class based or lambda based Condition.
 *
 * @author Max Arulananthan
 * @since 1.0
 * @see Action
 */
public final class ConditionBuilderBuilder {

    private static final Predicate<Method> FILTER =
            m -> ReflectionUtils.isAnnotated(m, Function.class)
                    && Modifier.isPublic(m.getModifiers()) && !m.isBridge();

    private static final ConditionBuilderBuilder instance = new ConditionBuilderBuilder();

    private ConditionBuilderBuilder() {
        super();
    }

    public static ConditionBuilderBuilder getInstance() {
        return instance;
    }

    private ConditionBuilder with(Object target, MethodDefinition definition) {
        return new ConditionBuilder(target, definition);
    }

    public List<Condition> build(Class<?> clazz) {
        Assert.notNull(clazz, "clazz cannot be null.");
        ObjectFactory objectFactory = RuliiSystem.getInstance().getObjectFactory();
        return build(clazz, objectFactory);
    }

    public List<Condition> build(Class<?> clazz, ObjectFactory factory) {
        return build(factory.createCondition(clazz));
    }

    /**
     * Introspect the given object for methods that are annotated with @Condition Annotation and build corresponding conditions for them.
     *
     * @param target target object.
     * @return array of condition inside the input class.
     */
    public List<Condition> build(Object target) {
        return build(target, org.rulii.annotation.Condition.class);
    }

    /**
     * Introspect the given object for methods that are annotated with the required Annotation and build corresponding conditions for them.
     *
     * @param target target object
     * @param annotationClass desired Condition marker.            .
     * @return array of conditions inside the input class.
     */
    public List<Condition> build(Object target, Class<? extends Annotation> annotationClass) {
        Assert.notNull(annotationClass, "annotationClass cannot be null.");
        Class<?> clazz = target.getClass();
        Method[] candidates = ReflectionUtils.getMethodsWithAnnotation(clazz, annotationClass);

        for (Method candidate : candidates) {
            if (!(candidate.getReturnType().equals(boolean.class))) {
                throw new UnrulyException("Condition" + annotationClass.getSimpleName() + " must return a boolean. "
                        + clazz.getSimpleName() + " method " + candidate
                        + "] returns a [" + candidate.getReturnType() + "]");
            }

        }

        Condition[] result = new Condition[candidates.length];

        for (int i = 0; i < candidates.length; i++) {
            String name = extractName(candidates[0]);
            Method candidate = BridgeMethodResolver.findBridgedMethod(candidates[0]);
            ConditionBuilder builder = with(target, MethodDefinition.load(candidate, true, SourceDefinition.build()));
            if (name != null) builder.name(name);
            result[i] = builder.build();
        }

        return Collections.unmodifiableList(Arrays.asList(result));
    }

    private ConditionBuilder withCondition(Object target) {
        Method[] candidates = ReflectionUtils.getMethods(target.getClass(), FILTER);

        if (candidates == null || candidates.length == 0) {
            throw new UnrulyException("Condition method not found on class [" + target.getClass() + "]");
        }

        // Too many Actions declared
        if (candidates.length > 1) {
            throw new UnrulyException("Too many condition methods found on class [" + target.getClass() + "]. Candidates ["
                    + Arrays.toString(candidates) + "]");
        }

        String name = extractName(candidates[0]);
        Method candidate = BridgeMethodResolver.findBridgedMethod(candidates[0]);
        RunnableBuilder.MethodInfo methodInfo = RunnableBuilder.load(target, candidate);

        if (!boolean.class.equals(methodInfo.getDefinition().getReturnType()) &&
                !Boolean.class.equals(methodInfo.getDefinition().getReturnType())) {
            throw new UnrulyException("Conditions must return a boolean [" + methodInfo.getDefinition().getMethod() + "]");
        }

        ConditionBuilder result = new ConditionBuilder(methodInfo.getTarget(), methodInfo.getDefinition());

        if (name != null) result.name(name);

        return result;
    }

    /**
     * Creates a new condition builder with no arguments.
     *
     * @param condition desired condition.
     * @return new ConditionBuilder with no arguments.
     */
    public ConditionBuilder with(NoArgFunction<Boolean> condition) {
        return withCondition(condition);
    }

    /**
     * Creates a new condition builder with one argument.
     *
     * @param condition desired condition.
     * @param <A> generic type of the first parameter.
     * @return new ActionBuilder with one argument.
     */
    public <A> ConditionBuilder with(UnaryFunction<Boolean, A> condition) {
        return withCondition(condition);
    }

    /**
     * Creates a new condition builder with two argument.
     *
     * @param condition desired condition.
     * @param <A> generic type of the first parameter.
     * @param <B> generic type of the second parameter.
     * @return new ActionBuilder with two arguments.
     */
    public <A, B> ConditionBuilder with(BiFunction<Boolean, A, B> condition) {
        return withCondition(condition);
    }

    /**
     * Creates a new condition builder with three argument.
     *
     * @param condition desired condition.
     * @param <A> generic type of the first parameter.
     * @param <B> generic type of the second parameter.
     * @param <C> generic type of the third parameter.
     * @return new ActionBuilder with three arguments.
     */
    public <A, B, C> ConditionBuilder with(TriFunction<Boolean, A, B, C> condition) {
        return withCondition(condition);
    }

    /**
     * Creates a new condition builder with four argument.
     *
     * @param condition desired action.
     * @param <A> generic type of the first parameter.
     * @param <B> generic type of the second parameter.
     * @param <C> generic type of the third parameter.
     * @param <D> generic type of the fourth parameter.
     * @return new ActionBuilder with four arguments.
     */
    public <A, B, C, D> ConditionBuilder with(QuadFunction<Boolean, A, B, C, D> condition) {
        return withCondition(condition);
    }

    /**
     * Creates a new condition builder with five argument.
     *
     * @param condition desired action.
     * @param <A> generic type of the first parameter.
     * @param <B> generic type of the second parameter.
     * @param <C> generic type of the third parameter.
     * @param <D> generic type of the fourth parameter.
     * @param <E> generic type of the fifth parameter.
     * @return new ActionBuilder with five arguments.
     */
    public <A, B, C, D, E> ConditionBuilder with(QuinFunction<Boolean, A, B, C, D, E> condition) {
        return withCondition(condition);
    }

    /**
     * Creates a new condition builder with six argument.
     *
     * @param condition desired action.
     * @param <A> generic type of the first parameter.
     * @param <B> generic type of the second parameter.
     * @param <C> generic type of the third parameter.
     * @param <D> generic type of the fourth parameter.
     * @param <E> generic type of the fifth parameter.
     * @param <F> generic type of the sixth parameter.
     * @return new ActionBuilder with six arguments.
     */
    public <A, B, C, D, E, F> ConditionBuilder with(SexFunction<Boolean, A, B, C, D, E, F> condition) {
        return withCondition(condition);
    }

    /**
     * Creates a new condition builder with seven argument.
     *
     * @param condition desired condition.
     * @param <A> generic type of the first parameter.
     * @param <B> generic type of the second parameter.
     * @param <C> generic type of the third parameter.
     * @param <D> generic type of the fourth parameter.
     * @param <E> generic type of the fifth parameter.
     * @param <F> generic type of the sixth parameter.
     * @param <G> generic type of the seventh parameter.
     * @return new ActionBuilder with seven arguments.
     */
    public <A, B, C, D, E, F, G> ConditionBuilder with(SeptFunction<Boolean, A, B, C, D, E, F, G> condition) {
        return withCondition(condition);
    }

    /**
     * Creates a new condition builder with eight argument.
     *
     * @param condition desired condition.
     * @param <A> generic type of the first parameter.
     * @param <B> generic type of the second parameter.
     * @param <C> generic type of the third parameter.
     * @param <D> generic type of the fourth parameter.
     * @param <E> generic type of the fifth parameter.
     * @param <F> generic type of the sixth parameter.
     * @param <G> generic type of the seventh parameter.
     * @param <H> generic type of the eighth parameter.
     * @return new ActionBuilder with eight arguments.
     */
    public <A, B, C, D, E, F, G, H> ConditionBuilder with(OctFunction<Boolean, A, B, C, D, E, F, G, H> condition) {
        return withCondition(condition);
    }

    /**
     * Creates a new condition with nine argument.
     *
     * @param condition desired condition.
     * @param <A> generic type of the first parameter.
     * @param <B> generic type of the second parameter.
     * @param <C> generic type of the third parameter.
     * @param <D> generic type of the fourth parameter.
     * @param <E> generic type of the fifth parameter.
     * @param <F> generic type of the sixth parameter.
     * @param <G> generic type of the seventh parameter.
     * @param <H> generic type of the eighth parameter.
     * @param <I> generic type of the ninth parameter.
     * @return new ActionBuilder with nine arguments.
     */
    public <A, B, C, D, E, F, G, H, I> ConditionBuilder with(NovFunction<Boolean, A, B, C, D, E, F, G, H, I> condition) {
        return withCondition(condition);
    }

    /**
     * Creates a new condition builder with ten argument.
     *
     * @param condition desired condition.
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
     * @return new ActionBuilder with ten arguments.
     */
    public <A, B, C, D, E, F, G, H, I, J> ConditionBuilder with(DecFunction<Boolean, A, B, C, D, E, F, G, H, I, J> condition) {
        return withCondition(condition);
    }

    private static String extractName(Method method) {
        Assert.notNull(method, "method cannot be null.");
        org.rulii.annotation.Condition condition = AnnotationUtils.getAnnotation(method, org.rulii.annotation.Condition.class);
        if (condition == null) return method.getName();
        return org.rulii.annotation.Condition.NOT_APPLICABLE.equals(condition.name()) ? method.getName() : condition.name();
    }
}
