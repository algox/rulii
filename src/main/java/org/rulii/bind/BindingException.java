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
package org.rulii.bind;

import org.rulii.bind.match.BindingMatch;
import org.rulii.bind.match.BindingMatchingStrategy;
import org.rulii.model.UnrulyException;
import org.rulii.model.MethodDefinition;
import org.rulii.model.ParameterDefinition;
import org.rulii.lib.spring.util.Assert;
import org.rulii.util.RuleUtils;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Binding Exception containing all the parameters avail during the attempted Bind.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class BindingException extends UnrulyException {

    private final MethodDefinition methodDefinition;
    private final ParameterDefinition parameterDefinition;
    private final BindingMatchingStrategy matchingStrategy;
    private final Collection<BindingMatch<Object>> matches;

    /**
     * Creates a BindException with the following parameters.
     *
     * @param message basic message (first line).
     * @param methodDefinition method details.
     * @param parameterDefinition parameter details.
     * @param matchingStrategy matching strategy being used.
     * @param matches bind candidates.
     */
    public BindingException(String message, MethodDefinition methodDefinition, ParameterDefinition parameterDefinition,
                            BindingMatchingStrategy matchingStrategy, Collection<BindingMatch<Object>> matches) {
        super(generateMessage(message, methodDefinition, matchingStrategy, matches));
        Assert.notNull(methodDefinition, "methodDefinition cannot be null");
        Assert.notNull(matchingStrategy, "matchingStrategy cannot be null");
        this.methodDefinition = methodDefinition;
        this.parameterDefinition = parameterDefinition;
        this.matchingStrategy = matchingStrategy;
        this.matches = matches;
    }

    public MethodDefinition getMethodDefinition() {
        return methodDefinition;
    }

    public ParameterDefinition getParameterDefinition() {
        return parameterDefinition;
    }

    public BindingMatchingStrategy getMatchingStrategy() {
        return matchingStrategy;
    }

    public Collection<BindingMatch<Object>> getMatches() {
        return matches;
    }

    private static String generateMessage(String message, MethodDefinition methodDefinition,
                                          BindingMatchingStrategy matchingStrategy,
                                          Collection<BindingMatch<Object>> matches) {
        Assert.notNull(methodDefinition, "definition cannot be null.");
        Assert.notNull(matchingStrategy, "matchingStrategy cannot be null.");
        return message + System.lineSeparator()
                + RuleUtils.TAB + "Method : "  + methodDefinition.getSignature() + System.lineSeparator()
                + RuleUtils.TAB + "Matched Candidates (using " + matchingStrategy.getClass().getSimpleName()
                + ") : {" + matchesText(matches) + "}";
    }

    private static String matchesText(Collection<BindingMatch<Object>> matches) {
        if (matches == null || matches.size() == 0) return "";
        return matches.stream()
                .map(m -> m.getBinding().getTypeAndName() + " (using " + m.getStrategyUsed().getSimpleName() + ")")
                .collect(Collectors.joining(", "));
    }
}
