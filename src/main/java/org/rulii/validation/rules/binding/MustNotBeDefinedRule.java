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
package org.rulii.validation.rules.binding;

import org.rulii.annotation.*;
import org.rulii.bind.match.MatchByTypeMatchingStrategy;
import org.rulii.context.RuleContext;
import org.rulii.validation.*;

/**
 * Validation Rule to make sure the given BindingName is NOT defined.
 *
 * @author Max Arulananthan
 * @since 1.0
 *
 */
@Rule
@Description("Binding Name must NOT exist.")
public class MustNotBeDefinedRule extends ValidationRule {

    public static final String ERROR_CODE      = "mustNotBeDefinedRule.errorCode";
    public static final String DEFAULT_MESSAGE = "Binding {0} must not be defined.";

    private final String bindingName;

    public MustNotBeDefinedRule(String bindingName) {
        this(bindingName, ERROR_CODE, Severity.ERROR, null);
    }

    public MustNotBeDefinedRule(String bindingName, String errorCode, Severity severity, String errorMessage) {
        super(errorCode, severity, errorMessage, DEFAULT_MESSAGE);
        this.bindingName = bindingName;
    }

    @Given
    public boolean isValid(@Param(matchUsing = MatchByTypeMatchingStrategy.class) RuleContext ruleContext) {
        return !ruleContext.getBindings().contains(bindingName);
    }

    @Otherwise
    public void otherwise(@Param(matchUsing = MatchByTypeMatchingStrategy.class) RuleContext ruleContext,
                          @Param(matchUsing = MatchByTypeMatchingStrategy.class) RuleViolations ruleViolations) {
        RuleViolationBuilder builder = RuleViolation.builder()
                .with(this)
                .param("bindingName", bindingName);
        ruleViolations.add(builder.build(ruleContext));
    }

    public String getBindingName() {
        return bindingName;
    }

    @Override
    public String toString() {
        return "MustNotBeDefinedRule{" +
                "bindingName=" + getBindingName() +
                '}';
    }
}