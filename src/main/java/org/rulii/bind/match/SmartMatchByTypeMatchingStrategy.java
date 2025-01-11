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

import org.rulii.bind.Bindings;
import org.rulii.lib.apache.commons.logging.Log;
import org.rulii.lib.apache.commons.logging.LogFactory;
import org.rulii.lib.spring.util.Assert;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * BindingMatchingStrategy that matches Bindings by the given Type (for parameters that contain generic info).
 * For Lambda parameters (parameter without generics) it will only match if the parameter is not generic.
 *
 * @author Max Arulananthan
 * @since 1.0
 * @see BindingMatchingStrategy
 */
public class SmartMatchByTypeMatchingStrategy implements BindingMatchingStrategy {

    private static final Log logger = LogFactory.getLog(SmartMatchByTypeMatchingStrategy.class);

    private final MatchByTypeMatchingStrategy typeMatchingStrategy = new MatchByTypeMatchingStrategy();

    public SmartMatchByTypeMatchingStrategy() {
        super();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<BindingMatch<T>> match(Bindings bindings, String name, Type type, boolean containsGenericInfo) {
        Assert.notNull(bindings, "bindings cannot be bull");
        Assert.notNull(type, "type cannot be bull");

        // We don't have any generic info and are trying to match a parameterized type
        if (!containsGenericInfo && (type instanceof  ParameterizedType || Object.class.equals(type))) return new ArrayList<>();

        List<BindingMatch<T>> result = typeMatchingStrategy.match(bindings, name, type, containsGenericInfo);

        if (logger.isDebugEnabled()) {
            logger.debug("Name [" + name + "] Type [" + type + "] Matches [" + result + "]");
        }

        return result;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
