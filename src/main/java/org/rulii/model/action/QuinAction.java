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

import org.rulii.annotation.Action;

import java.io.Serializable;

/**
 * Functional Action taking in five parameters.
 *
 * @param <A> generic type of the 1st parameter.
 * @param <B> generic type of the 2nd parameter.
 * @param <C> generic type of the 3rd parameter.
 * @param <D> generic type of the 4th parameter.
 * @param <E> generic type of the 5th parameter.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
@FunctionalInterface
public interface QuinAction<A, B, C, D, E> extends Serializable {

    /**
     * Action logic taking in five args.
     *
     * @param arg0 1st arg.
     * @param arg1 2nd arg.
     * @param arg2 3rd arg.
     * @param arg3 4th arg.
     * @param arg4 5th arg.
     */
    @Action
    void run(A arg0, B arg1, C arg2, D arg3, E arg4);
}
