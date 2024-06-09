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

import org.rulii.bind.Binding;
import org.rulii.bind.Bindings;
import org.rulii.lib.apache.commons.logging.Log;
import org.rulii.lib.apache.commons.logging.LogFactory;
import org.rulii.lib.spring.util.Assert;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * BindingMatchingStrategy that matches Bindings by the given Name.
 *
 * @author Max Arulananthan
 * @since 1.0
 * @see BindingMatchingStrategy
 */
public class MatchByNameMatchingStrategy implements BindingMatchingStrategy {

    private static final Log logger = LogFactory.getLog(MatchByNameMatchingStrategy.class);

    public MatchByNameMatchingStrategy() {
        super();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<BindingMatch<T>> match(Bindings bindings, String name, Type type, boolean containsGenericInfo) {
        Assert.notNull(bindings, "bindings cannot be bull");
        Assert.hasText(name, "name cannot be bull");

        List<BindingMatch<T>> result = new LinkedList<>();
        // Look for the Binding by name
        Binding<T> binding = bindings.getBinding(name);
        // Add the Binding (if we found one)
        if (binding != null) result.add(new BindingMatch<T>(binding, getClass()));

        if (logger.isDebugEnabled()) {
            logger.debug("Name [" + name + "] Type [" + type + "] Matches [" + result + "]");
        }

        return Collections.unmodifiableList(result);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
