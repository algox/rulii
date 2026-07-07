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
package org.rulii.rule;

import org.rulii.context.RuleContext;
import org.rulii.model.Definable;
import org.rulii.model.Identifiable;
import org.rulii.model.Runnable;
import org.rulii.model.UnrulyException;
import org.rulii.model.action.Action;
import org.rulii.model.condition.Condition;

import java.util.List;

/**
 * Rule class encapsulates all the properties/methods of a Rule within the framework. A Rule consists of two parts
 * a Condition and a list of associated Actions. You can think of it as a If (Condition).. then Action(s).
 *
 * The Condition is stateless and should be able to execute many times without any side effects (idempotent).
 * The Action(s) can be stateful.
 *
 * A Rule Condition can be tested via :
 * the isPass(..), isFail(...) and test() methods. Those methods must be given the arguments the rule requires. These
 * methods are solely there for the purpose of executing them manually to test the Rule. You should use the RuleEngine
 * to automate the process of checking the Condition and running the associated Actions.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public interface Rule extends Runnable<RuleResult>, Identifiable, Definable<RuleDefinition> {

    static RuleBuilder builder() {
        return RuleBuilder.getInstance();
    }

    /**
     * Executes the Rule against the given RuleContext.
     *
     * The Pre-Condition (if any) is checked first; if it fails, the Rule is skipped entirely (status
     * SKIPPED) and neither the Actions nor the Otherwise action are executed. Otherwise, the given
     * Condition is checked: if it passes, the Rule's Actions are executed (status PASS); if it fails,
     * the Otherwise action is executed instead, if one exists (status FAIL). If an error occurs at any
     * stage, the Rule's status is ERROR and the error is (re)thrown as an UnrulyException.
     *
     * @param ruleContext used to derive the parameters required for this Rule.
     * @return execution status of the rule.
     * @throws UnrulyException thrown if there are any runtime errors during the execution.
     */
    @Override
    RuleResult run(RuleContext ruleContext) throws UnrulyException;

    default boolean isTrue(RuleContext ruleContext) {
        return RuleExecutionStrategy.build().isTrue(this, ruleContext);
    }

    /**
     * Rule Pre-Condition.
     *
     * @return Rule Condition.
     */
    Condition getPreCondition();

    /**
     * Rule Condition.
     *
     * @return Rule Condition.
     */
    Condition getCondition();

    /**
     * Any associated Actions.
     *
     * @return associated actions.
     */
    List<Action> getActions();

    @Override
    RuleDefinition getDefinition();

    /**
     * Otherwise Action.
     *
     * @return otherwise Action for this Rule.
     */
    Action getOtherwiseAction();

}
