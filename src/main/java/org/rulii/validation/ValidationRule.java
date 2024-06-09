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
package org.rulii.validation;

import org.rulii.model.UnrulyException;
import org.rulii.rule.RuleDefinition;
import org.rulii.rule.RuleDefinitionAware;

/**
 * This abstract class represents a validation rule that can be used to perform validation on a value or object.
 * It can be extended to create custom validation rules for specific use cases.
 * <p>
 * The ValidationRule class provides methods to get information about the rule, such as the error code, severity, error message, and default message.
 * It also provides a method to set the rule definition associated with the rule.
 * <p>
 * To use this class, extend it and implement the `isValid` method to define the validation logic specific to the rule.
 * <p>
 * Example Usage:
 * ```
 * public class CustomValidationRule extends ValidationRule {
 * <p>
 *    public CustomValidationRule() {
 *        super("my_error_code", Severity.WARNING, "Default message");
 *    }
 * <p>
 *    public boolean isValid(RuleContext ruleContext, Object value) {
 *        // Perform custom validation logic here
 *        return true;
 *    }
 * }
 * ```
 * <p>
 * @see RuleDefinitionAware
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public abstract class ValidationRule implements RuleDefinitionAware {

    private final String errorCode;
    private final Severity severity;
    private final String errorMessage;
    private final String defaultMessage;
    private RuleDefinition ruleDefinition;

    /**
     * Creates a new ValidationRule with the given parameters.
     *
     * @param errorCode      the error code associated with the rule
     * @param severity       the severity of the rule
     * @param defaultMessage the default error message to be used if no specific error message is provided
     */
    protected ValidationRule(String errorCode, Severity severity, String defaultMessage) {
        this(errorCode, severity, null, defaultMessage);
    }

    /**
     * Creates a new ValidationRule with the given parameters.
     *
     * @param errorCode      the error code associated with the rule
     * @param severity       the severity of the rule
     * @param errorMessage   the error message to be displayed if the validation rule fails
     * @param defaultMessage the default error message to be used if no specific error message is provided
     */
    protected ValidationRule(String errorCode, Severity severity, String errorMessage, String defaultMessage) {
        super();
        this.errorCode = errorCode;
        this.severity = severity;
        this.errorMessage = errorMessage;
        this.defaultMessage = defaultMessage;
    }

    public String getName() {
        return ruleDefinition != null ? ruleDefinition.getName() : getClass().getSimpleName();
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    public RuleDefinition getRuleDefinition() {
        return ruleDefinition;
    }

    @Override
    public void setRuleDefinition(RuleDefinition ruleDefinition) {
        if (this.ruleDefinition != null) throw new UnrulyException("ruleDefinition cannot be modified.");
        this.ruleDefinition = ruleDefinition;
    }

    /**
     * Creates a new RuleViolationBuilder instance.
     *
     * @return The created RuleViolationBuilder instance.
     */
    protected RuleViolationBuilder createRuleViolationBuilder() {
        return RuleViolation.builder().with(this);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "errorCode='" + errorCode + '\'' +
                ", severity=" + severity +
                ", errorMessage='" + errorMessage + '\'' +
                ", defaultMessage='" + defaultMessage + '\'' +
                ", ruleDefinition=" + ruleDefinition +
                '}';
    }
}
