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
package org.rulii.bind.match;

import org.rulii.bind.BindingException;
import org.rulii.bind.Bindings;
import org.rulii.convert.ConversionException;
import org.rulii.convert.Converter;
import org.rulii.convert.ConverterRegistry;
import org.rulii.lib.apache.commons.logging.Log;
import org.rulii.lib.apache.commons.logging.LogFactory;
import org.rulii.lib.apache.reflect.TypeUtils;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.MethodDefinition;
import org.rulii.model.ParameterDefinition;
import org.rulii.model.UnrulyException;
import org.rulii.util.reflect.ObjectFactory;
import org.rulii.util.reflect.ReflectionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Default Parameter Resolver implementation.
 *
 * Matching: Here is how we find matches(bindings) for the given parameter.
 *  - if the parameter specifies its own matching strategy we use it
 *  - if not we use the given matching strategy
 *  - if a single match or none are found we are done.
 *  - if more than 1 is found we check to see if there is a primary defined.
 *  - if there are none then we throw an error.
 *
 * @author Max Arulananthan
 * @since 1.0
 * @see ParameterResolver
 */
public class DefaultParameterResolver implements ParameterResolver {

    private static final Log logger = LogFactory.getLog(DefaultParameterResolver.class);

    private final boolean autoConvert;

    public DefaultParameterResolver() {
        this(true);
    }

    public DefaultParameterResolver(boolean autoConvert) {
        super();
        this.autoConvert = autoConvert;
    }

    @Override
    public List<ParameterMatch> match(MethodDefinition definition, Bindings bindings,
                                              BindingMatchingStrategy matchingStrategy,
                                              ObjectFactory objectFactory) throws BindingException {
        if (definition == null) return Collections.emptyList();

        ParameterMatch[] result = new ParameterMatch[definition.getParameterDefinitions().size()];
        int index = 0;

        for (ParameterDefinition parameterDefinition : definition.getParameterDefinitions()) {
            BindingMatchingStrategy matcher = matchingStrategy;

            result[index] = null;

            // See if the parameter is overriding the matching strategy to be used.
            if (parameterDefinition.isMatchSpecified()) {
                matcher = objectFactory.createBindingMatchingStrategy(parameterDefinition.getMatchUsing());
            }

            // Find all the matching bindings
            List<BindingMatch<Object>> matches = matcher.match(bindings, parameterDefinition.getName(),
                    parameterDefinition.getUnderlyingType(), parameterDefinition.containsGenericInfo());
            int matchesCount = matches.size();

            if (matchesCount == 0) {
                result[index] = new ParameterMatch(parameterDefinition, null);
            } else if (matchesCount == 1) {
                ParameterMatch match = new ParameterMatch(parameterDefinition, matches.get(0));
                match.setDescription("Matched using [" + matcher.getDescription() + "]");
                result[index] = match;
            } else {
                // More than one match found; let's see if there is a primary candidate
                BindingMatch<Object> primaryBinding = null;

                for (BindingMatch<Object> m : matches) {
                    // Primary candidate found
                    if (m.getBinding().isPrimary()) {
                        primaryBinding = m;
                        break;
                    }
                }

                if (primaryBinding != null) {
                    result[index] = new ParameterMatch(parameterDefinition, primaryBinding);
                } else {
                    // Too many matches found; cannot proceed.
                    throw new BindingException("Multiple matches found for ("+ parameterDefinition.getTypeAndName()
                            + "). Perhaps specify a primary Binding? ",
                            definition, parameterDefinition, matchingStrategy, matches);
                }
            }

            index++;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Parameter Matches [" + Arrays.toString(result) + "] for method [" + definition.getMethodParameterSignature()  +"]");
        }

        return Collections.unmodifiableList(Arrays.asList(result));
    }

    @Override
    public List<Object> resolve(List<ParameterMatch> matches, MethodDefinition definition, Bindings bindings,
                            BindingMatchingStrategy matchingStrategy, ConverterRegistry registry,
                            ObjectFactory objectFactory) throws BindingException {
        if (matches == null) return Collections.emptyList();

        Object[] result = new Object[matches.size()];

        for (int i = 0; i < result.length; i++) {
            // Make sure matches are passed
            if (matches.get(i) == null) throw new UnrulyException("Invalid state. You cannot have a null match");
            // Strict checks that the binding exists; non-strict lets the consumer deal with the consequences.
            result[i] = getValue(matches.get(i), definition, matchingStrategy, registry, objectFactory);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Parameter Resolved Values [" + Arrays.toString(result) + "] for method [" + definition.getMethodParameterSignature()  +"]");
        }

        return Collections.unmodifiableList(Arrays.asList(result));
    }

    /**
     * Get the match value.
     *
     * @param match binding match.
     * @param definition method definition.
     * @param matchingStrategy strategy used.
     * @param registry converter registry.
     * @param objectFactory object factory.
     * @return match value.
     */
    protected Object getValue(ParameterMatch match, MethodDefinition definition,
                              BindingMatchingStrategy matchingStrategy,
                              ConverterRegistry registry,
                              ObjectFactory objectFactory) {
        Assert.notNull(match, "match cannot be null.");
        Assert.notNull(definition, "definition cannot be null.");
        Assert.notNull(matchingStrategy, "matchingStrategy cannot be null.");
        Assert.notNull(registry, "registry cannot be null.");
        Assert.notNull(objectFactory, "objectFactory cannot be null.");

        // Binding was not found; use the default
        if (!match.isMatched()) {
            if (match.getDefinition().isOptionalType()) return Optional.empty();
            // Return the default value if one is provided
            return deriveDefaultValue(match, definition, matchingStrategy, registry, objectFactory);
        }

        // Looks like they want the Binding itself
        if (match.getDefinition().isBindingType()) {
            return match.getBinding();
        }

        // Looks like they want a Optional<Binding>
        if (match.getDefinition().isOptionalType() && ReflectionUtils.isBinding(match.getDefinition().getUnderlyingType())) {
            return Optional.of(match.getBinding());
        }

        Object result = match.getBinding().getValue();

        // Perhaps we need to convert value?
        if (result != null && isAutoConvert()) {
            result = autoConvert(result, match, registry);
        }

        return match.getDefinition().isOptionalType() ? Optional.of(result) : result;
    }

    /**
     * Retrieves the default value from text.
     *
     * @param match parameter match.
     * @param definition method definition.
     * @param matchingStrategy strategy used.
     * @param registry converter registry.
     * @param objectFactory object factory.
     * @return default value.
     */
    protected Object deriveDefaultValue(ParameterMatch match, MethodDefinition definition,
                                             BindingMatchingStrategy matchingStrategy,
                                             ConverterRegistry registry,
                                             ObjectFactory objectFactory) {
        Assert.notNull(match, "match cannot be null.");
        Assert.notNull(definition, "definition cannot be null.");
        Assert.notNull(matchingStrategy, "matchingStrategy cannot be null.");
        Assert.notNull(registry, "registry cannot be null.");
        Assert.notNull(objectFactory, "objectFactory cannot be null.");

        if (!match.getDefinition().hasDefaultValue()) {
            Object defaultValue = ReflectionUtils.getDefaultValue(match.getDefinition().getType());

            if (defaultValue != null) {
                match.setDescription("Using primitive default");
            }

            return defaultValue;
        }

        Converter<String, ?> converter = registry.find(String.class, match.getDefinition().getType());

        if (converter == null) {
            throw new BindingException("Cannot find a converter that will convert default value ["
                    + match.getDefinition().getDefaultValueText() + "] to type ["
                    + match.getDefinition().getType() + "]. Add Converter to registry and try again.", definition, match.getDefinition(),
                    matchingStrategy, null);
        }

        match.setDescription("Using default value " + match.getDefinition().getDefaultValueText());
        return match.getDefinition().getDefaultValue(converter);
    }

    /**
     * Convert the value if needed.
     *
     * @param result original value.
     * @param match match.
     * @param registry converter registry.
     * @return converted value.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Object autoConvert(Object result, ParameterMatch match, ConverterRegistry registry) {
        Assert.notNull(match, "match cannot be null.");
        Assert.notNull(registry, "registry cannot be null.");

        // We don't have a binding move on
        if (match.getBinding() == null) return result;
        // We don't have a definition move on
        if (match.getDefinition() == null) return result;

        if (!TypeUtils.isAssignable(match.getDefinition().getType(), match.getBinding().getType())) {
            // Find a converter to convert to desired type
            Converter converter = registry.find(match.getBinding().getType(), match.getDefinition().getType());
            // Found a converter, let's try and convert it
            if (converter != null) {
                try {
                    result = converter.convert(result, match.getDefinition().getType());
                    match.setDescription("Using auto-convert from " + match.getBinding().getType());
                } catch (ConversionException e) {
                    // Could not convert move on
                }
            }
        }

        return result;
    }

    @Override
    public boolean isAutoConvert() {
        return autoConvert;
    }
}
