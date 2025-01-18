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
package org.rulii.model.action;

import org.rulii.lib.spring.core.BridgeMethodResolver;
import org.rulii.lib.spring.core.annotation.AnnotationUtils;
import org.rulii.lib.spring.core.annotation.OrderUtils;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.MethodDefinition;
import org.rulii.model.RunnableBuilder;
import org.rulii.model.SourceDefinition;
import org.rulii.model.UnrulyException;
import org.rulii.util.Ordered;
import org.rulii.util.reflect.ObjectFactory;
import org.rulii.util.reflect.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/**
 * Convenient Builder class to creating new class based or lambda based Actions.
 *
 * @author Max Arulananthan
 * @since 1.0
 * @see Action
 */
public final class ActionBuilderBuilder {

    private static final ActionBuilderBuilder instance = new ActionBuilderBuilder();

    // Default Filter
    private static final Predicate<Method> FILTER =
            m -> ReflectionUtils.isAnnotated(m, org.rulii.annotation.Action.class)
                    && Modifier.isPublic(m.getModifiers()) && !m.isBridge();

    private ActionBuilderBuilder() {
        super();
    }

    public static ActionBuilderBuilder getInstance() {
        return instance;
    }

    private static ActionBuilder with(Object target, MethodDefinition definition) {
        return new ActionBuilder(target, definition);
    }

    /**
     * Builds a list of actions based on the provided class and object factory.
     *
     * @param clazz the class to build actions for
     * @return a list of actions built from the given class
     */
    public List<Action> build(Class<?> clazz) {
        Assert.notNull(clazz, "clazz cannot be null.");
        ObjectFactory objectFactory = ObjectFactory.builder().build();
        return build(clazz, objectFactory);
    }

    /**
     * Builds a list of actions based on the provided class and object factory.
     *
     * @param clazz the class to build actions for
     * @param factory the object factory used to create actions
     * @return a list of actions built from the given class
     */
    public List<Action> build(Class<?> clazz, ObjectFactory factory) {
        return build(factory.createAction(clazz));
    }

    /**
     * Introspect the given object for methods that are annotated with @Action Annotation and build corresponding actions for them.
     *
     * @param target target object.
     * @return list of actions inside the input class.
     */
    public List<Action> build(Object target) {
        return build(target, org.rulii.annotation.Action.class);
    }

    /**
     * Introspect the given object for methods that are annotated with the required Annotation and build corresponding actions for them.
     *
     * @param target target object
     * @param annotationClass desired Action marker.            .
     * @return list of actions inside the input class.
     */
    public List<Action> build(Object target, Class<? extends Annotation> annotationClass) {
        Assert.notNull(target, "target cannot be null.");
        Assert.notNull(annotationClass, "annotationClass cannot be null.");

        Class<?> clazz = target.getClass();
        Method[] candidates = ReflectionUtils.getMethodsWithAnnotation(clazz, annotationClass);

        for (Method candidate : candidates) {
            if (!(candidate.getReturnType().equals(void.class))) {
                throw new UnrulyException("Action" + annotationClass.getSimpleName() + " must return a void. "
                        + clazz.getSimpleName() + " method " + candidate
                        + "] returns a [" + candidate.getReturnType() + "]");
            }

        }

        // Sort the methods by Order
        Arrays.sort(candidates, new MethodComparator());

        Action[] result = new Action[candidates.length];

        for (int i = 0; i < candidates.length; i++) {
            // See if its bridged method
            Method candidate = BridgeMethodResolver.findBridgedMethod(candidates[i]);
            String name = extractName(candidate);
            ActionBuilder builder = with(target, MethodDefinition.load(candidate, true, SourceDefinition.build()));
            if (name != null) builder.name(name);
            result[i] = builder.build();
        }

        return List.of(result);
    }

    private static class MethodComparator implements Comparator<Method> {

        @Override
        public int compare(Method method1, Method method2) {
            Integer order1 = OrderUtils.getOrder(method1);
            Integer order2 = OrderUtils.getOrder(method2);

            if (order1 == null) order1 = Ordered.LOWEST_PRECEDENCE;
            if (order2 == null) order2 = Ordered.LOWEST_PRECEDENCE;

            return order1.compareTo(order2);
        }
    }

    /**
     * Creates a new action builder with no arguments.
     *
     * @param action desired action.
     * @return new ActionBuilder with no arguments.
     */
    public ActionBuilder with(NoArgAction action) {
        return withAction(action);
    }

    /**
     * Creates a new action builder with one argument.
     *
     * @param action desired action.
     * @param <A> generic type of the first parameter.
     * @return new ActionBuilder with one argument.
     */
    public <A> ActionBuilder with(UnaryAction<A> action) {
        return withAction(action);
    }

    /**
     * Creates a new action builder with two argument.
     *
     * @param action desired action.
     * @param <A> generic type of the first parameter.
     * @param <B> generic type of the second parameter.
     * @return new ActionBuilder with two arguments.
     */
    public <A, B> ActionBuilder with(BiAction<A, B> action) {
        return withAction(action);
    }

    /**
     * Creates a new action builder with three argument.
     *
     * @param action desired action.
     * @param <A> generic type of the first parameter.
     * @param <B> generic type of the second parameter.
     * @param <C> generic type of the third parameter.
     * @return new ActionBuilder with three arguments.
     */
    public <A, B, C> ActionBuilder with(TriAction<A, B, C> action) {
        return withAction(action);
    }

    /**
     * Creates a new action builder with four argument.
     *
     * @param action desired action.
     * @param <A> generic type of the first parameter.
     * @param <B> generic type of the second parameter.
     * @param <C> generic type of the third parameter.
     * @param <D> generic type of the fourth parameter.
     * @return new ActionBuilder with four arguments.
     */
    public <A, B, C, D> ActionBuilder with(QuadAction<A, B, C, D> action) {
        return withAction(action);
    }

    /**
     * Creates a new action builder with five argument.
     *
     * @param action desired action.
     * @param <A> generic type of the first parameter.
     * @param <B> generic type of the second parameter.
     * @param <C> generic type of the third parameter.
     * @param <D> generic type of the fourth parameter.
     * @param <E> generic type of the fifth parameter.
     * @return new ActionBuilder with five arguments.
     */
    public <A, B, C, D, E> ActionBuilder with(QuinAction<A, B, C, D, E> action) {
        return withAction(action);
    }

    /**
     * Creates a new action builder with six argument.
     *
     * @param action desired action.
     * @param <A> generic type of the first parameter.
     * @param <B> generic type of the second parameter.
     * @param <C> generic type of the third parameter.
     * @param <D> generic type of the fourth parameter.
     * @param <E> generic type of the fifth parameter.
     * @param <F> generic type of the sixth parameter.
     * @return new ActionBuilder with six arguments.
     */
    public <A, B, C, D, E, F> ActionBuilder with(SexAction<A, B, C, D, E, F> action) {
        return withAction(action);
    }

    /**
     * Creates a new action builder with seven argument.
     *
     * @param action desired action.
     * @param <A> generic type of the first parameter.
     * @param <B> generic type of the second parameter.
     * @param <C> generic type of the third parameter.
     * @param <D> generic type of the fourth parameter.
     * @param <E> generic type of the fifth parameter.
     * @param <F> generic type of the sixth parameter.
     * @param <G> generic type of the seventh parameter.
     * @return new ActionBuilder with seven arguments.
     */
    public <A, B, C, D, E, F, G> ActionBuilder with(SeptAction<A, B, C, D, E, F, G> action) {
        return withAction(action);
    }

    /**
     * Creates a new action builder with eight argument.
     *
     * @param action desired action.
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
    public <A, B, C, D, E, F, G, H> ActionBuilder with(OctAction<A, B, C, D, E, F, G, H> action) {
        return withAction(action);
    }

    /**
     * Creates a new action with nine argument.
     *
     * @param action action.
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
    public <A, B, C, D, E, F, G, H, I> ActionBuilder with(NovAction<A, B, C, D, E, F, G, H, I> action) {
        return withAction(action);
    }

    /**
     * Creates a new action builder with ten argument.
     *
     * @param action desired action.
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
    public <A, B, C, D, E, F, G, H, I, J> ActionBuilder with(DecAction<A, B, C, D, E, F, G, H, I, J> action) {
        return withAction(action);
    }

    private ActionBuilder withAction(Object target) {
        Method[] candidates = ReflectionUtils.getMethods(target.getClass(), FILTER);

        if (candidates.length == 0) {
            throw new UnrulyException("Action method not found on class [" + target.getClass() + "]");
        }

        // Too many Actions declared
        if (candidates.length > 1) {
            throw new UnrulyException("Too many action methods found on class [" + target.getClass() + "]. Candidates ["
                    + Arrays.toString(candidates) + "]");
        }

        String name = extractName(candidates[0]);
        Method candidate = BridgeMethodResolver.findBridgedMethod(candidates[0]);
        RunnableBuilder.MethodInfo methodInfo = RunnableBuilder.load(target, candidate);

        if (!void.class.equals(methodInfo.getDefinition().getReturnType())) {
            throw new UnrulyException("Actions must return a void [" + methodInfo.getDefinition().getMethod() + "]");
        }

        ActionBuilder result = new ActionBuilder(methodInfo.getTarget(), methodInfo.getDefinition());

        if (name != null) result.name(name);

        return result;
    }

    private static String extractName(Method method) {
        Assert.notNull(method, "method cannot be null.");
        org.rulii.annotation.Action action = AnnotationUtils.getAnnotation(method, org.rulii.annotation.Action.class);
        if (action == null) return method.getName();
        return org.rulii.annotation.Action.NOT_APPLICABLE.equals(action.name()) ? method.getName() : action.name();
    }
}
