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
package org.rulii.lang.function;

public final class Functions {

    private Functions() {
        super();
    }

    /**
     * Creates a new function with no arguments.
     *
     * @param function desired action.
     * @param <T> generic return type of the function.
     * @return new FunctionBuilder with no arguments.
     */
    public static <T> Function<T> function(NoArgFunction<T> function) {
        FunctionBuilder<T> builder = Function.builder().with(function);
        return builder.build();
    }

    /**
     * Creates a new function with one argument.
     *
     * @param function desired function.
     * @param <A> generic type of the first parameter.
     * @param <T> generic return type of the function.
     * @return new function with one argument.
     */
    public static <T, A> Function<T> function(UnaryFunction<T, A> function) {
        FunctionBuilder<T> builder = Function.builder().with(function);
        return builder.build();
    }

    /**
     * Creates a new function with two arguments.
     *
     * @param function desired function.
     * @param <A> generic type of the first parameter.
     * @param <B> generic type of the second parameter.
     * @param <T> generic return type of the function.
     * @return new function with two arguments.
     */
    public static <T, A, B> Function<T> function(BiFunction<T, A, B> function) {
        FunctionBuilder<T> builder = Function.builder().with(function);
        return builder.build();
    }

    /**
     * Creates a new function with three arguments.
     *
     * @param function desired function.
     * @param <A> generic type of the first parameter.
     * @param <B> generic type of the second parameter.
     * @param <C> generic type of the third parameter.
     * @param <T> generic return type of the function.
     * @return new function with three arguments.
     */
    public static <T, A, B, C> Function function(TriFunction<T, A, B, C> function) {
        FunctionBuilder<T> builder = Function.builder().with(function);
        return builder.build();
    }

    /**
     * Creates a new function with four arguments.
     *
     * @param function desired function.
     * @param <A> generic type of the first parameter.
     * @param <B> generic type of the second parameter.
     * @param <C> generic type of the third parameter.
     * @param <D> generic type of the fourth parameter.
     * @param <T> generic return type of the function.
     * @return new function with four arguments.
     */
    public static <T, A, B, C, D> Function<T> function(QuadFunction<T, A, B, C, D> function) {
        FunctionBuilder<T> builder = Function.builder().with(function);
        return builder.build();
    }

    /**
     * Creates a new function with five arguments.
     *
     * @param function desired function.
     * @param <A> generic type of the first parameter.
     * @param <B> generic type of the second parameter.
     * @param <C> generic type of the third parameter.
     * @param <D> generic type of the fourth parameter.
     * @param <E> generic type of the fifth parameter.
     * @param <T> generic return type of the function.
     * @return new Function with five arguments.
     */
    public static <T, A, B, C, D, E> Function<T> function(QuinFunction<T, A, B, C, D, E> function) {
        FunctionBuilder<T> builder = Function.builder().with(function);
        return builder.build();
    }

    /**
     * Creates a new function with six arguments.
     *
     * @param function desired function.
     * @param <A> generic type of the first parameter.
     * @param <B> generic type of the second parameter.
     * @param <C> generic type of the third parameter.
     * @param <D> generic type of the fourth parameter.
     * @param <E> generic type of the fifth parameter.
     * @param <F> generic type of the sixth parameter.
     * @param <T> generic return type of the function.
     * @return new Function with six arguments.
     */
    public static <T, A, B, C, D, E, F> Function<T> function(SexFunction<T, A, B, C, D, E, F> function) {
        FunctionBuilder<T> builder = Function.builder().with(function);
        return builder.build();
    }

    /**
     * Creates a new function with seven arguments.
     *
     * @param function desired function.
     * @param <A> generic type of the first parameter.
     * @param <B> generic type of the second parameter.
     * @param <C> generic type of the third parameter.
     * @param <D> generic type of the fourth parameter.
     * @param <E> generic type of the fifth parameter.
     * @param <F> generic type of the sixth parameter.
     * @param <G> generic type of the seventh parameter.
     * @param <T> generic return type of the function.
     * @return new Function with seven arguments.
     */
    public static <T, A, B, C, D, E, F, G> Function<T> function(SeptFunction<T, A, B, C, D, E, F, G> function) {
        FunctionBuilder<T> builder = Function.builder().with(function);
        return builder.build();
    }

    /**
     * Creates a new function with eight arguments.
     *
     * @param function desired function.
     * @param <A> generic type of the first parameter.
     * @param <B> generic type of the second parameter.
     * @param <C> generic type of the third parameter.
     * @param <D> generic type of the fourth parameter.
     * @param <E> generic type of the fifth parameter.
     * @param <F> generic type of the sixth parameter.
     * @param <G> generic type of the seventh parameter.
     * @param <H> generic type of the eighth parameter.
     * @param <T> generic return type of the function.
     * @return new Function with eight arguments.
     */
    public static <T, A, B, C, D, E, F, G, H> Function<T> function(OctFunction<T, A, B, C, D, E, F, G, H> function) {
        FunctionBuilder<T> builder = Function.builder().with(function);
        return builder.build();
    }

    /**
     * Creates a new function with nine arguments.
     *
     * @param function desired function.
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
     * @return new Function with nine arguments.
     */
    public static <T, A, B, C, D, E, F, G, H, I> Function<T> function(NovFunction<T, A, B, C, D, E, F, G, H, I> function) {
        FunctionBuilder<T> builder = Function.builder().with(function);
        return builder.build();
    }

    /**
     * Creates a new function builder with ten arguments.
     *
     * @param function desired function.
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
    public static <T, A, B, C, D, E, F, G, H, I, J> Function<T> function(DecFunction<T, A, B, C, D, E, F, G, H, I, J> function) {
        FunctionBuilder<T> builder = Function.builder().with(function);
        return builder.build();
    }

}
