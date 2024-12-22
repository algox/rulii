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
package org.rulii.model.action;

/**
 * Static Action builder. Convenient way to build actions using static methods.
 * Make sure to use a static import
 * example: import static org.algorithmx.rulii.lang.action.Actions.*;
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public final class Actions {

    private static final Action EMPTY_ACTION = action(() -> {});

    private Actions() {
        super();
    }

    /**
     * As the name suggestion, this creates an Action that does nothing.
     *
     * @return do nothing action.
     */
    public static Action emptyAction() {
        return EMPTY_ACTION;
    }

    /**
     * Creates a new action with no arguments.
     *
     * @param action desired action.
     * @return new action with no arguments.
     */
    public static Action action(NoArgAction action) {
        return Action.builder().with(action).build();
    }

    /**
     * Creates a new action with one argument.
     *
     * @param action desired action.
     * @param <A> generic type of the first parameter.
     * @return new Action with one argument.
     */
    public static <A> Action action(UnaryAction<A> action) {
        return Action.builder().with(action).build();
    }

    /**
     * Creates a new action with two arguments.
     *
     * @param action desired action.
     * @param <A> generic type of the first parameter.
     * @param <B> generic type of the second parameter.
     * @return new Action with two arguments.
     */
    public static <A, B> Action action(BiAction<A, B> action) {
        return Action.builder().with(action).build();
    }

    /**
     * Creates a new action with three arguments.
     *
     * @param action desired action.
     * @param <A> generic type of the first parameter.
     * @param <B> generic type of the second parameter.
     * @param <C> generic type of the third parameter.
     * @return new action with three arguments.
     */
    public static <A, B, C> Action action(TriAction<A, B, C> action) {
        return Action.builder().with(action).build();
    }

    /**
     * Creates a new action with four arguments.
     *
     * @param action desired action.
     * @param <A> generic type of the first parameter.
     * @param <B> generic type of the second parameter.
     * @param <C> generic type of the third parameter.
     * @param <D> generic type of the fourth parameter.
     * @return new action with four arguments.
     */
    public static <A, B, C, D> Action action(QuadAction<A, B, C, D> action) {
        return Action.builder().with(action).build();
    }

    /**
     * Creates a new action with five arguments.
     *
     * @param action desired action.
     * @param <A> generic type of the first parameter.
     * @param <B> generic type of the second parameter.
     * @param <C> generic type of the third parameter.
     * @param <D> generic type of the fourth parameter.
     * @param <E> generic type of the fifth parameter.
     * @return new action with five arguments.
     */
    public static <A, B, C, D, E> Action action(QuinAction<A, B, C, D, E> action) {
        return Action.builder().with(action).build();
    }

    /**
     * Creates a new action with six arguments.
     *
     * @param action desired action.
     * @param <A> generic type of the first parameter.
     * @param <B> generic type of the second parameter.
     * @param <C> generic type of the third parameter.
     * @param <D> generic type of the fourth parameter.
     * @param <E> generic type of the fifth parameter.
     * @param <F> generic type of the sixth parameter.
     * @return new action with six arguments.
     */
    public static <A, B, C, D, E, F> Action action(SexAction<A, B, C, D, E, F> action) {
        return Action.builder().with(action).build();
    }

    /**
     * Creates a new action with seven arguments.
     *
     * @param action desired action.
     * @param <A> generic type of the first parameter.
     * @param <B> generic type of the second parameter.
     * @param <C> generic type of the third parameter.
     * @param <D> generic type of the fourth parameter.
     * @param <E> generic type of the fifth parameter.
     * @param <F> generic type of the sixth parameter.
     * @param <G> generic type of the seventh parameter.
     * @return new action with seven arguments.
     */
    public static <A, B, C, D, E, F, G> Action action(SeptAction<A, B, C, D, E, F, G> action) {
        return Action.builder().with(action).build();
    }

    /**
     * Creates a new action with eight arguments.
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
     * @return new action with eight arguments.
     */
    public static <A, B, C, D, E, F, G, H> Action action(OctAction<A, B, C, D, E, F, G, H> action) {
        return Action.builder().with(action).build();
    }

    /**
     * Creates a new action with nine arguments.
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
     * @return new action with nine arguments.
     */
    public static <A, B, C, D, E, F, G, H, I> Action action(NovAction<A, B, C, D, E, F, G, H, I> action) {
        return Action.builder().with(action).build();
    }

    /**
     * Creates a new action with ten arguments.
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
     * @return new action with ten arguments.
     */
    public static <A, B, C, D, E, F, G, H, I, J> Action action(DecAction<A, B, C, D, E, F, G, H, I, J> action) {
        return Action.builder().with(action).build();
    }
}
