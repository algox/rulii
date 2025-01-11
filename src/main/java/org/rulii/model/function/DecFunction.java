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

import org.rulii.annotation.Function;

import java.io.Serializable;

/**
 * Function taking in two parameters.
 *
 * @param <A> generic type of the 1st parameter.
 * @param <B> generic type of the 2nd parameter.
 * @param <C> generic type of the 3rd parameter.
 * @param <D> generic type of the 4th parameter.
 * @param <E> generic type of the 5th parameter.
 * @param <F> generic type of the 6th parameter.
 * @param <G> generic type of the 7th parameter.
 * @param <H> generic type of the 8th parameter.
 * @param <I> generic type of the 9th parameter.
 * @param <J> generic type of the 10th parameter.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
@FunctionalInterface
public interface DecFunction<T, A, B, C, D, E, F, G, H, I, J> extends Serializable {

    /**
     * Action logic taking in ten args.
     *
     * @param arg0 1st arg.
     * @param arg1 2nd arg.
     * @param arg2 2nd arg.
     * @param arg3 2nd arg.
     * @param arg4 2nd arg.
     * @param arg5 2nd arg.
     * @param arg6 2nd arg.
     * @param arg7 2nd arg.
     * @param arg8 2nd arg.
     * @param arg9 2nd arg.
     * @return result of the function.
     */
    @Function
    T apply(A arg0, B arg1, C arg2, D arg3, E arg4, F arg5, G arg6, H arg7, I arg8, J arg9);
}
