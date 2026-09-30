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
package org.rulii.ruleset;

import org.rulii.lib.spring.util.Assert;
import org.rulii.model.Definition;
import org.rulii.model.InputParameter;
import org.rulii.model.MethodDefinition;
import org.rulii.model.SourceDefinition;
import org.rulii.rule.RuleDefinition;
import org.rulii.util.RuleUtils;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

/**
 * Metadata describing a {@link RuleSet}: its identity, input parameters, the methods behind its
 * lifecycle hooks (initializer, pre-condition, stop condition, finalizer, result extractor and
 * error handler) and the definitions of its rules. Nothing here runs anything.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public final class RuleSetDefinition implements Definition {

    // Name of the RuleSet
    private String name;
    // Description of the RuleSet
    private final String description;
    private final SourceDefinition sourceDefinition;
    private final List<InputParameter<?>> inputParameters;
    private final MethodDefinition initActionDefinition;
    // PreCondition method details
    private final MethodDefinition preConditionDefinition;
    // Stop condition method details
    private final MethodDefinition stopConditionDefinition;
    private final MethodDefinition finallyActionDefinition;
    private final MethodDefinition resultActionDefinition;
    private final MethodDefinition errorHandlerDefinition;
    private final List<RuleDefinition> definitions;

    /**
     * Creates a definition.
     *
     * @param name                    rule set name; must be a valid name.
     * @param description             description; may be null.
     * @param sourceDefinition        where the rule set was built.
     * @param inputParameters         declared input parameters; must not be null.
     * @param initActionDefinition    initializer method; may be null.
     * @param preConditionDefinition  pre-condition method; may be null.
     * @param stopConditionDefinition stop condition method; may be null.
     * @param finallyActionDefinition finalizer method; may be null.
     * @param resultActionDefinition  result extractor method; may be null.
     * @param errorHandlerDefinition  error handler method; may be null.
     * @param definitions             definitions of the rules in order; must not be null.
     */
    public RuleSetDefinition(String name, String description,
                             SourceDefinition sourceDefinition,
                             List<InputParameter<?>> inputParameters,
                             MethodDefinition initActionDefinition,
                             MethodDefinition preConditionDefinition,
                             MethodDefinition stopConditionDefinition,
                             MethodDefinition finallyActionDefinition,
                             MethodDefinition resultActionDefinition,
                             MethodDefinition errorHandlerDefinition,
                             List<RuleDefinition> definitions) {
        super();
        setName(name);
        Assert.notNull(inputParameters, "inputParameters cannot be null.");
        Assert.notNull(definitions, "definitions cannot be null.");
        this.description = description;
        this.sourceDefinition = sourceDefinition;
        this.inputParameters = Collections.unmodifiableList(inputParameters);
        this.initActionDefinition = initActionDefinition;
        this.preConditionDefinition = preConditionDefinition;
        this.stopConditionDefinition = stopConditionDefinition;
        this.finallyActionDefinition = finallyActionDefinition;
        this.resultActionDefinition = resultActionDefinition;
        this.errorHandlerDefinition = errorHandlerDefinition;
        this.definitions = Collections.unmodifiableList(definitions);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public Type getResultType() {
        return resultActionDefinition != null ? resultActionDefinition.getReturnType() : null;
    }

    @Override
    public SourceDefinition getSource() {
        return sourceDefinition;
    }

    /**
     * The declared input parameters.
     *
     * @return unmodifiable list; never null.
     * @since 2.1
     */
    public List<InputParameter<?>> getInputParameters() {
        return inputParameters;
    }

    public MethodDefinition getInitActionDefinition() {
        return initActionDefinition;
    }

    public MethodDefinition getPreConditionDefinition() {
        return preConditionDefinition;
    }

    /**
     * The method behind the stop condition.
     *
     * @return method definition, or null when there is no stop condition.
     * @since 2.1
     */
    public MethodDefinition getStopConditionDefinition() {
        return stopConditionDefinition;
    }

    /**
     * The method behind the stop condition.
     *
     * @return method definition, or null when there is no stop condition.
     * @deprecated since 2.1; this always held the stop <em>condition</em>. Use
     * {@link #getStopConditionDefinition()}.
     */
    @Deprecated(since = "2.1", forRemoval = true)
    public MethodDefinition getStopActionDefinition() {
        return stopConditionDefinition;
    }

    public MethodDefinition getFinallyActionDefinition() {
        return finallyActionDefinition;
    }

    public MethodDefinition getResultActionDefinition() {
        return resultActionDefinition;
    }

    /**
     * The method behind the error handler.
     *
     * @return method definition, or null when there is no error handler.
     * @since 2.1
     */
    public MethodDefinition getErrorHandlerDefinition() {
        return errorHandlerDefinition;
    }

    public List<RuleDefinition> getDefinitions() {
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
                ", sourceDefinition=" + sourceDefinition +
                ", inputParameters=" + inputParameters +
                ", initActionDefinition=" + initActionDefinition +
                ", preConditionDefinition=" + preConditionDefinition +
                ", stopConditionDefinition=" + stopConditionDefinition +
                ", finallyActionDefinition=" + finallyActionDefinition +
                ", resultActionDefinition=" + resultActionDefinition +
                ", errorHandlerDefinition=" + errorHandlerDefinition +
                ", definitions=" + definitions +
                '}';
    }
}
