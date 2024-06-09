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
package org.rulii.util.reflect;

/**
 * MethodResolverBuilder is a class that provides a way to build instances of MethodResolver.
 * It follows the Singleton design pattern, with a private constructor and a public static method getInstance() to retrieve the instance.
 * It also has a build() method that returns a new instance of DefaultMethodResolver.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public final class MethodResolverBuilder {

    private static final MethodResolverBuilder instance = new MethodResolverBuilder();

    private MethodResolverBuilder() {
        super();
    }

    /**
     * Returns the singleton instance of MethodResolverBuilder.
     *
     * @return The singleton instance of MethodResolverBuilder.
     */
    public static MethodResolverBuilder getInstance() {
        return instance;
    }

    /**
     * Builds a MethodResolver instance.
     *
     * @return A MethodResolver instance.
     */
    public MethodResolver build() {
        return new DefaultMethodResolver();
    }
}
