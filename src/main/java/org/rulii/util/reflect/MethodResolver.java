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
package org.rulii.util.reflect;

import java.lang.reflect.Method;

/**
 * MethodResolver is an interface that provides a way to resolve the implementation method for a given class and candidate method.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public interface MethodResolver {

    /**
     * Returns a MethodResolverBuilder instance.
     *
     * @return MethodResolverBuilder instance.
     */
    static MethodResolverBuilder builder() {
        return MethodResolverBuilder.getInstance();
    }

    /**
     * Retrieves the implementation method for the given class and candidate method.
     *
     * @param c The class for which the implementation method needs to be resolved. Must not be null.
     * @param candidate The candidate method for which the implementation method needs to be resolved. Must not be null.
     * @return The resolved implementation method, or null if no implementation method was found.
     */
    Method getImplementationMethod(Class<?> c, Method candidate);
}
