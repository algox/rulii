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
package org.rulii.rule;

import org.rulii.bind.match.MatchByTypeMatchingStrategy;
import org.rulii.bind.match.ParameterMatch;
import org.rulii.model.action.Action;
import org.rulii.context.RuleContext;
import org.rulii.lib.spring.util.Assert;
import org.rulii.validation.RuleViolation;
import org.rulii.validation.RuleViolationBuilder;
import org.rulii.validation.RuleViolations;
import org.rulii.validation.Severity;

import java.util.List;

public class ValidationRuleBuilder<T> extends LambdaBasedRuleBuilder<T> {

    private String errorCode;
    private Severity severity;
    private String errorMessage;
    private String defaultMessage;

    public ValidationRuleBuilder(String name, String description, String errorCode) {
        super(name, description);
        errorCode(errorCode);
    }

    public ValidationRuleBuilder<T> errorCode(String errorCode) {
        Assert.notNull(errorCode, "errorCode cannot be null.");
        this.errorCode = errorCode;
        return this;
    }

    public ValidationRuleBuilder<T> severity(Severity severity) {
        Assert.notNull(severity, "severity cannot be null.");
        this.severity = severity;
        return this;
    }

    public ValidationRuleBuilder<T> errorMessage(String errorMessage) {
        Assert.notNull(errorMessage, "errorMessage cannot be null.");
        this.errorMessage = errorMessage;
        return this;
    }

    public ValidationRuleBuilder<T> defaultMessage(String defaultMessage) {
        this.defaultMessage = defaultMessage;
        return this;
    }

    @Override
    public Rule build() {
        super.otherwise(Action.builder().with((RuleContext ruleContext, RuleViolations ruleViolations) -> {
            if (getCondition() == null) return;

            List<ParameterMatch> matches = ruleContext.match(getCondition().getDefinition());
            List<Object> values = ruleContext.resolve(matches, getCondition().getDefinition());

            RuleViolationBuilder builder = RuleViolation.builder()
                    .with(getName())
                    .errorCode(errorCode)
                    .severity(severity)
                    .errorMessage(errorMessage)
                    .defaultMessage(defaultMessage);

            if (matches != null) {
                int size = matches.size();
                int valuesSize = values.size();
                for (int i = 0; i < size; i++) {
                    builder.param(matches.get(i).getDefinition().getName(), i < valuesSize ? values.get(i) : null);
                }
            }

            ruleViolations.add(builder.build(ruleContext.getMessageResolver(), ruleContext.getMessageFormatter(), ruleContext.getLocale()));
        })
                    .param(0)
                        .matchUsing(MatchByTypeMatchingStrategy.class)
                    .build()
                    .param(1)
                        .matchUsing(MatchByTypeMatchingStrategy.class)
                    .build()
                .build());

        return super.build();
    }
}
