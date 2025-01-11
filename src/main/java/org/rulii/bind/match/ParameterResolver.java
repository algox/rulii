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
package org.rulii.bind.match;

import org.rulii.bind.BindingException;
import org.rulii.bind.Bindings;
import org.rulii.convert.ConverterRegistry;
import org.rulii.model.UnrulyException;
import org.rulii.model.MethodDefinition;
import org.rulii.util.reflect.ObjectFactory;

import java.util.List;

/**
 * Resolves a method's parameters from the given Bindings using a MatchingStrategy.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public interface ParameterResolver {

    /**
     * Creates ParameterResolverBuilder with auto convert turned on.
     *
     * @return a new instance of ParameterResolverBuilder.
     */
    static ParameterResolverBuilder builder() {
        return new ParameterResolverBuilder(true);
    }

    /**
     * Matches the method parameters to an array of Bindings. We use the matching strategy to resolves each parameter.
     *
     * @param definition method meta information.
     * @param bindings available bindings.
     * @param matchingStrategy matching strategy to use to resolve the bindings.
     * @param objectFactory factory that to be used to create custom BindingStrategies.
     * @return arrays of matches.
     * @throws UnrulyException if the Binding Strategy failed.
     */
    List<ParameterMatch> match(MethodDefinition definition, Bindings bindings,
                               BindingMatchingStrategy matchingStrategy,
                               ObjectFactory objectFactory) throws BindingException;

    /**
     * Resolves the parameter matches to actual values.
     *
     * @param matches parameter matches.
     * @param definition method meta information.
     * @param bindings available bindings.
     * @param matchingStrategy matching strategy to use to resolve the bindings.
     * @param registry converter registry.
     * @param objectFactory factory that to be used to create custom BindingStrategies.
     * @return resulting values.
     */
    List<Object> resolve(List<ParameterMatch> matches, MethodDefinition definition, Bindings bindings,
                     BindingMatchingStrategy matchingStrategy, ConverterRegistry registry,
                     ObjectFactory objectFactory) throws BindingException;

    /**
     * Determines whether parameter matches should be auto converted if they are not of the correct type.
     *
     * @return true if auto convert is turned on; false otherwise.
     */
    boolean isAutoConvert();
}
