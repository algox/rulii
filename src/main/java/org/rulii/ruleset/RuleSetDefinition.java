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
package org.rulii.ruleset;

import org.rulii.model.Definition;
import org.rulii.model.MethodDefinition;
import org.rulii.model.SourceDefinition;
import org.rulii.rule.RuleDefinition;
import org.rulii.lib.spring.util.Assert;
import org.rulii.util.RuleUtils;

import java.util.Arrays;

public final class RuleSetDefinition implements Definition {

    // Name of the RuleSet
    private String name;
    // Description of the RuleSet
    private final String description;
    private final SourceDefinition sourceDefinition;
    // PreCondition method details
    private final MethodDefinition preConditionDefinition;
    // StopAction method details
    private final MethodDefinition stopActionDefinition;
    private final RuleDefinition[] definitions;

    public RuleSetDefinition(String name, String description,
                             SourceDefinition sourceDefinition,
                             MethodDefinition preConditionDefinition,
                             MethodDefinition stopActionDefinition,
                             RuleDefinition...definitions) {
        super();
        setName(name);
        this.description = description;
        this.sourceDefinition = sourceDefinition;
        this.preConditionDefinition = preConditionDefinition;
        this.stopActionDefinition = stopActionDefinition;
        this.definitions = definitions;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public SourceDefinition getSource() {
        return sourceDefinition;
    }

    public MethodDefinition getPreConditionDefinition() {
        return preConditionDefinition;
    }

    public MethodDefinition getStopActionDefinition() {
        return stopActionDefinition;
    }

    public RuleDefinition[] getDefinitions() {
        return definitions;
    }

    void setName(String name) {
        Assert.isTrue(RuleUtils.isValidName(name), "RuleSet name must match ["
                + RuleUtils.NAME_REGEX + "] Given [" + name + "]");
        this.name = name;
    }

    @Override
    public String toString() {
        return "RuleSetDefinition{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", source=" + sourceDefinition +
                ", preConditionDefinition=" + preConditionDefinition +
                ", stopActionDefinition=" + stopActionDefinition +
                ", definitions=" + Arrays.toString(definitions) +
                '}';
    }
}
