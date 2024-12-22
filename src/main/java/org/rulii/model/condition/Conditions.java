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

import org.rulii.model.function.*;

/**
 * Static Condition builder. Convenient way to build conditions using static methods.
 * Make sure to use a static import
 * example: import static org.algorithmx.rulii.lang.condition.Conditions.*;
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public final class Conditions {

    private static final Condition TRUE = Condition.builder().with(() -> true).build();
    private static final Condition FALSE = Condition.builder().with(() -> false).build();

    private Conditions() {
        super();
    }

    /**
     * Always true condition.
     *
     * @return always true condition.
     */
    public static Condition TRUE() {
        return TRUE;
    }

    /**
     * Always false condition.
     *
     * @return always false condition.
     */
    public static Condition FALSE() {
        return FALSE;
    }

    /**
     * Creates a new condition with no arguments.
     *
     * @param function desired action.
     * @return new condition with no arguments.
     */
    public static Condition condition(NoArgFunction<Boolean> function) {
        return Condition.builder().with(function).build();
    }

    /**
     * Creates a new condition with one argument.
     *
     * @param function desired action.
     * @param <A> generic type of the first parameter.
     * @return new Condition representing the passed in function.
     */
    public static <A> Condition condition(UnaryFunction<Boolean, A> function) {
        return Condition.builder().with(function).build();
    }

    /**
     * Creates a new condition with two arguments.
     *
     * @param function desired action.
     * @param <A> generic type of the first parameter.
     * @param <B> generic type of the second parameter.
     * @return new condition with two arguments.
     */
    public static <A, B> Condition condition(BiFunction<Boolean, A, B> function) {
        return Condition.builder().with(function).build();
    }

    /**
     * Creates a new condition with three arguments.
     *
     * @param function desired action.
     * @param <A> generic type of the first parameter.
     * @param <B> generic type of the second parameter.
     * @param <C> generic type of the third parameter.
     * @return new condition with three arguments.
     */
    public static <A, B, C> Condition condition(TriFunction<Boolean, A, B, C> function) {
        return Condition.builder().with(function).build();
    }

    /**
     * Creates a new condition builder with four arguments.
     *
     * @param function desired action.
     * @param <A> generic type of the first parameter.
     * @param <B> generic type of the second parameter.
     * @param <C> generic type of the third parameter.
     * @param <D> generic type of the fourth parameter.
     * @return new condition with four arguments.
     */
    public static <A, B, C, D> Condition condition(QuadFunction<Boolean, A, B, C, D> function) {
        return Condition.builder().with(function).build();
    }

    /**
     * Creates a new condition builder with five arguments.
     *
     * @param function desired action.
     * @param <A> generic type of the first parameter.
     * @param <B> generic type of the second parameter.
     * @param <C> generic type of the third parameter.
     * @param <D> generic type of the fourth parameter.
     * @param <E> generic type of the fifth parameter.
     * @return new condition with five arguments.
     */
    public static <A, B, C, D, E> Condition condition(QuinFunction<Boolean, A, B, C, D, E> function) {
        return Condition.builder().with(function).build();
    }

    /**
     * Creates a new condition with six arguments.
     *
     * @param function desired action.
     * @param <A> generic type of the first parameter.
     * @param <B> generic type of the second parameter.
     * @param <C> generic type of the third parameter.
     * @param <D> generic type of the fourth parameter.
     * @param <E> generic type of the fifth parameter.
     * @param <F> generic type of the sixth parameter.
     * @return new condition with six arguments.
     */
    public static <A, B, C, D, E, F> Condition condition(SexFunction<Boolean, A, B, C, D, E, F> function) {
        return Condition.builder().with(function).build();
    }

    /**
     * Creates a new condition with seven arguments.
     *
     * @param function desired action.
     * @param <A> generic type of the first parameter.
     * @param <B> generic type of the second parameter.
     * @param <C> generic type of the third parameter.
     * @param <D> generic type of the fourth parameter.
     * @param <E> generic type of the fifth parameter.
     * @param <F> generic type of the sixth parameter.
     * @param <G> generic type of the seventh parameter.
     * @return new condition with seven arguments.
     */
    public static <A, B, C, D, E, F, G> Condition condition(SeptFunction<Boolean, A, B, C, D, E, F, G> function) {
        return Condition.builder().with(function).build();
    }

    /**
     * Creates a new condition with eight arguments.
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
     * @return new condition with eight arguments.
     */
    public static <A, B, C, D, E, F, G, H> Condition condition(OctFunction<Boolean, A, B, C, D, E, F, G, H> function) {
        return Condition.builder().with(function).build();
    }

    /**
     * Creates a condition with nine arguments.
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
     * @return new condition with nine arguments.
     */
    public static <A, B, C, D, E, F, G, H, I> Condition condition(NovFunction<Boolean, A, B, C, D, E, F, G, H, I> function) {
        return Condition.builder().with(function).build();
    }

    /**
     * Creates a new condition with ten arguments.
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
     * @return new condition with ten arguments.
     */
    public static <A, B, C, D, E, F, G, H, I, J> Condition condition(DecFunction<Boolean, A, B, C, D, E, F, G, H, I, J> function) {
        return Condition.builder().with(function).build();
    }
}
