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
package org.rulii.ruleflow;

import org.rulii.lib.spring.util.Assert;
import org.rulii.model.Definition;
import org.rulii.model.ExpressionInfo;
import org.rulii.model.InputParameter;
import org.rulii.model.SourceDefinition;
import org.rulii.ruleflow.info.CommandInfo;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

/**
 * Metadata describing a {@link RuleFlow} pipeline: its identity, input parameters, result type
 * and, since 2.1, the full structure of its commands, global exception handler, finalizer and
 * result extractor. Nothing here runs anything.
 *
 * @author Max Arulananthan
 * @since 2.0
 */
public final class RuleFlowDefinition implements Definition {

    private final String name;
    private final String description;
    private final SourceDefinition sourceDefinition;
    private final Type resultType;
    private final List<InputParameter<?>> inputParameters;
    private final List<CommandInfo> commands;
    private final CommandInfo.Handler globalHandler;
    private final ExpressionInfo finalizer;
    private final ExpressionInfo returning;

    /**
     * Creates a definition.
     *
     * @param name             flow name; must not be empty.
     * @param description      flow description; may be null.
     * @param sourceDefinition where the flow was built; must not be null.
     * @param resultType       type of the result, or null when the flow returns its rule context.
     * @param inputParameters  declared input parameters; must not be null.
     * @param commands         info of the top-level commands in order; must not be null.
     * @param globalHandler    flow-level exception handler; null when none.
     * @param finalizer        the finalizer action; null when none.
     * @param returning        the result extractor; null when the flow returns its rule context.
     */
    public RuleFlowDefinition(String name, String description, SourceDefinition sourceDefinition, Type resultType,
                              List<InputParameter<?>> inputParameters, List<CommandInfo> commands,
                              CommandInfo.Handler globalHandler, ExpressionInfo finalizer, ExpressionInfo returning) {
        super();
        Assert.hasText(name, "name cannot be empty/null.");
        Assert.notNull(sourceDefinition, "sourceDefinition cannot be null.");
        Assert.notNull(inputParameters, "inputParameters cannot be null.");
        Assert.notNull(commands, "commands cannot be null.");
        this.name = name;
        this.description = description;
        this.sourceDefinition = sourceDefinition;
        this.resultType = resultType;
        this.inputParameters = Collections.unmodifiableList(inputParameters);
        this.commands = Collections.unmodifiableList(commands);
        this.globalHandler = globalHandler;
        this.finalizer = finalizer;
        this.returning = returning;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public SourceDefinition getSource() {
        return sourceDefinition;
    }

    /**
     * The type of the flow's result.
     *
     * @return the type given to {@code returning(Class, Function)}, else the extractor's declared
     * return type, else null when the flow returns its rule context.
     */
    public Type getResultType() {
        return resultType;
    }

    /**
     * Number of top-level commands.
     *
     * @return command count.
     */
    public int getCommandCount() {
        return commands.size();
    }

    public List<InputParameter<?>> getInputParameters() {
        return inputParameters;
    }

    /**
     * The top-level commands in order, each describing itself (and its body, for containers).
     *
     * @return unmodifiable list; never null.
     * @since 2.1
     */
    public List<CommandInfo> getCommands() {
        return commands;
    }

    /**
     * The flow-level exception handler.
     *
     * @return handler info, or null when there is none.
     * @since 2.1
     */
    public CommandInfo.Handler getGlobalHandler() {
        return globalHandler;
    }

    /**
     * The finalizer action.
     *
     * @return expression info, or null when there is none.
     * @since 2.1
     */
    public ExpressionInfo getFinalizer() {
        return finalizer;
    }

    /**
     * The result extractor.
     *
     * @return expression info, or null when the flow returns its rule context.
     * @since 2.1
     */
    public ExpressionInfo getReturning() {
        return returning;
    }

    @Override
    public String toString() {
        return "RuleFlowDefinition{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", resultType=" + resultType +
                ", commandCount=" + commands.size() +
                ", inputParameters=" + inputParameters +
                ", globalHandler=" + globalHandler +
                ", finalizer=" + finalizer +
                ", returning=" + returning +
                '}';
    }
}
