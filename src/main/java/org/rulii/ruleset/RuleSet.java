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

import org.rulii.context.RuleContext;
import org.rulii.model.Runnable;
import org.rulii.model.action.Action;
import org.rulii.model.condition.Condition;
import org.rulii.model.function.Function;
import org.rulii.model.Definable;
import org.rulii.model.Identifiable;
import org.rulii.model.ScopeDefining;
import org.rulii.model.UnrulyException;
import org.rulii.rule.Rule;

import java.util.List;

/**
 * RuleSet is a logical grouping of Rules.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public interface RuleSet<T> extends Runnable<T>, Identifiable, Iterable<Rule>, Definable<RuleSetDefinition>, ScopeDefining {

    static RuleSetBuilderBuilder builder() {
        return RuleSetBuilderBuilder.getInstance();
    }

    T run(RuleContext context) throws UnrulyException;

    /**
     * Ruleset name.
     *
     * @return name.
     */
    String getName();

    /**
     * RuleSet description.
     *
     * @return description.
     */
    String getDescription();

    @Override
    RuleSetDefinition getDefinition();

    /**
     * Returns the Condition (if one exists) to be met before the execution of the Rules.
     *
     * @return pre-check before execution of the Rules.
     */
    Condition getPreCondition();

    Action getInitializer();

    /**
     * Returns the Condition that determines when execution should stop.
     *
     * @return stopping condition.
     */
    Condition getStopCondition();

    Action getFinalizer();

    Function<T> getResultExtractor();

    /**
     * Size of this RuleSet (ie : number of Rules in this RuleSet)
     *
     * @return number of Rules in this RuleSet.
     */
    int size();

    Rule get(String name);

    Rule get(int index);

    List<Rule> getRules();

    default boolean isEmpty() {
        return getRules() == null || getRules().isEmpty();
    }

}

