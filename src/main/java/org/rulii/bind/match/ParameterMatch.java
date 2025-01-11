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

import org.rulii.bind.Binding;
import org.rulii.model.ParameterDefinition;
import org.rulii.lib.spring.util.Assert;

/**
 * Stores the parameter with its corresponding matched binding(if one is found) for a given match.
 *
 * @author Max Arulananthan.
 * @since 1.0
*/
public class ParameterMatch {

    private final ParameterDefinition definition;
    private final BindingMatch<Object> bindingMatch;
    private String description;

    public ParameterMatch(ParameterDefinition definition, BindingMatch<Object> bindingMatch) {
        super();
        Assert.notNull(definition, "definition cannot be null.");
        this.definition = definition;
        this.bindingMatch = bindingMatch;
        this.description = bindingMatch != null ? ("using " + bindingMatch.getStrategyUsed().getSimpleName()) : null;
    }

    public ParameterDefinition getDefinition() {
        return definition;
    }

    public BindingMatch<Object> getBindingMatch() {
        return bindingMatch;
    }

    public Binding<Object> getBinding() {
        return bindingMatch != null ? bindingMatch.getBinding() : null;
    }

    public boolean isMatched() {
        return bindingMatch != null && bindingMatch.getBinding() != null;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "ParameterMatch{" +
                "definition=" + definition +
                ", bindingMatch=" + bindingMatch +
                ", description='" + description + '\'' +
                '}';
    }
}
