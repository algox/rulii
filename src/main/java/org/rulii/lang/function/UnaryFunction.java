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

import org.rulii.annotation.Function;

import java.io.Serializable;

/**
 * Function taking in a single parameter.
 *
 * @param <A> generic type of the parameter.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
@FunctionalInterface
public interface UnaryFunction<T, A> extends Serializable {

    /**
     * Action logic taking in a single arg.
     *
     * @param arg arg value.
     * @return result of the function.
     */
    @Function
    T apply(A arg);
}