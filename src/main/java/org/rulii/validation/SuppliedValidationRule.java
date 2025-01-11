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
package org.rulii.validation;

import org.rulii.annotation.Given;
import org.rulii.annotation.Otherwise;
import org.rulii.annotation.Param;
import org.rulii.annotation.Rule;
import org.rulii.bind.match.MatchByTypeMatchingStrategy;
import org.rulii.bind.match.ParameterMatch;
import org.rulii.context.RuleContext;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.condition.Condition;
import org.rulii.util.RuleUtils;

import java.util.List;
import java.util.Map;

/**
 * The SuppliedValidationRule class represents a validation rule that can be used to perform validation based on a supplied Condition.
 * It extends the ValidationRule abstract class and implements methods for checking validity and handling failed validation cases.
 *
 * This class requires a Condition to be supplied during instantiation, and it implements the isValid method to define the validation logic.
 * In case the validation fails, the otherwise method is invoked to handle the failed validation case.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
@Rule
public class SuppliedValidationRule extends ValidationRule {

    private final Condition condition;

    public SuppliedValidationRule(Condition condition, String errorCode, Severity severity, String errorMessage, String defaultMessage) {
        super(errorCode, severity, errorMessage, defaultMessage);
        Assert.notNull(condition, "condition must not be null.");
        this.condition = condition;
    }

    @Given
    public boolean isValid(@Param(matchUsing = MatchByTypeMatchingStrategy.class) RuleContext ruleContext) {
        return condition.isTrue(ruleContext);
    }

    @Otherwise
    public void otherwise(@Param(matchUsing = MatchByTypeMatchingStrategy.class) RuleContext ruleContext,
                          @Param(matchUsing = MatchByTypeMatchingStrategy.class) RuleViolations ruleViolations) {
        List<ParameterMatch> matches = ruleContext.getParameterResolver().match(condition.getDefinition(),
                ruleContext.getBindings(), ruleContext.getMatchingStrategy(), ruleContext.getObjectFactory());
        List<Object> values = ruleContext.getParameterResolver().resolve(matches, condition.getDefinition(),
                ruleContext.getBindings(), ruleContext.getMatchingStrategy(), ruleContext.getConverterRegistry(),
                ruleContext.getObjectFactory());
        Map<String, Object> params = RuleUtils.convert(matches, values);
        RuleViolationBuilder builder = createRuleViolationBuilder();

        for (Map.Entry<String, Object> entry : params.entrySet()) {
            builder.param(entry.getKey(), entry.getValue());
        }

        ruleViolations.add(builder.build(ruleContext.getMessageResolver(), ruleContext.getMessageFormatter(), ruleContext.getLocale()));
    }

}
