/*
 * This software is licensed under the Apache 2 license, quoted below.
 *
 * Copyright (c) 1999-2026, Algorithmx Inc.
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
    // Strategy that produced the match, when the caller says; only read when a description is asked for.
    private final BindingMatchingStrategy matchedUsing;
    private String description;
    // False until the description has been built or set: one that was set, even to null, is never rebuilt.
    private boolean described;

    public ParameterMatch(ParameterDefinition definition, BindingMatch<Object> bindingMatch) {
        this(definition, bindingMatch, null);
    }

    /**
     * Creates a ParameterMatch that remembers which strategy produced it.
     *
     * @param definition the parameter definition.
     * @param bindingMatch the matched binding, or null if none matched.
     * @param matchedUsing the strategy that produced the match; may be null.
     */
    public ParameterMatch(ParameterDefinition definition, BindingMatch<Object> bindingMatch,
                          BindingMatchingStrategy matchedUsing) {
        super();
        Assert.notNull(definition, "definition cannot be null.");
        this.definition = definition;
        this.bindingMatch = bindingMatch;
        this.matchedUsing = matchedUsing;
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
        // Built on demand: the text is only read for diagnostics, so the hot path never pays for it.
        if (!described) {
            if (matchedUsing != null) {
                description = "Matched using [" + matchedUsing.getDescription() + "]";
            } else if (bindingMatch != null) {
                description = "using " + bindingMatch.getStrategyUsed().getSimpleName();
            }
            described = true;
        }
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        this.described = true;
    }

    @Override
    public String toString() {
        return "ParameterMatch{" +
                "definition=" + definition +
                ", bindingMatch=" + bindingMatch +
                ", description='" + getDescription() + '\'' +
                '}';
    }
}
