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

import org.rulii.context.RuleContext;
import org.rulii.lib.spring.util.Assert;
import org.rulii.text.MessageFormatter;
import org.rulii.text.MessageResolver;
import org.rulii.text.ParameterInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * The RuleViolationBuilder class is used to build RuleViolation objects.
 * It provides methods to set various attributes of the RuleViolation such as error code, error message,
 * severity, default message, and parameters.
 * The build() method is used to create the RuleViolation object with the specified attributes.
 * The RuleViolationBuilder is immutable and thread-safe.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class RuleViolationBuilder {

    private final String ruleName;
    private String errorCode = null;
    private Severity severity = null;
    private String errorMessage = null;
    private String defaultMessage = null;
    private final List<ParameterInfo> params = new ArrayList<>();

    RuleViolationBuilder(String ruleName) {
        super();
        Assert.notNull(ruleName, "ruleName cannot be null.");
        this.ruleName = ruleName;
    }

    /**
     * Sets the error code for the RuleViolationBuilder.
     *
     * @param errorCode The error code to set.
     * @return The RuleViolationBuilder instance.
     */
    public RuleViolationBuilder errorCode(String errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    /**
     * Sets the error message for the RuleViolationBuilder.
     *
     * @param errorMessage The error message to set.
     * @return The RuleViolationBuilder instance.
     */
    public RuleViolationBuilder errorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    /**
     * Sets the default error message for the RuleViolationBuilder.
     *
     * @param defaultMessage The default error message to set.
     * @return The RuleViolationBuilder instance.
     */
    public RuleViolationBuilder defaultMessage(String defaultMessage) {
        this.defaultMessage = defaultMessage;
        return this;
    }

    /**
     * Sets the severity for the RuleViolationBuilder.
     *
     * @param severity The severity to set.
     * @return The RuleViolationBuilder instance.
     */
    public RuleViolationBuilder severity(Severity severity) {
        this.severity = severity;
        return this;
    }

    /**
     * Adds a parameter to the RuleViolationBuilder.
     *
     * @param name  The name of the parameter.
     * @param value The value of the parameter.
     * @return The RuleViolationBuilder instance.
     */
    public RuleViolationBuilder param(String name, Object value) {
        params.add(new ParameterInfo(params.size(), name, value));
        return this;
    }

    /**
     * Clears the parameters stored in the RuleViolationBuilder.
     *
     * @return The RuleViolationBuilder instance.
     */
    public RuleViolationBuilder clearParams() {
        this.params.clear();
        return this;
    }

    /**
     * Builds a RuleViolation object using the given RuleContext.
     *
     * @param context the RuleContext object containing the required information
     * @return a RuleViolation object
     */
    public RuleViolation build(RuleContext context) {
        return build(context.getMessageResolver(), context.getMessageFormatter() , context.getLocale());
    }

    /**
     * Builds a RuleViolation object using the given MessageResolver, MessageFormatter, and Locale.
     * The error message is resolved using the provided MessageResolver and formatted using the provided MessageFormatter.
     * The resulting RuleViolation object is created with the resolved and formatted error message, and the other parameters of the RuleViolationBuilder instance.
     * The built RuleViolation object is returned.
     *
     * @param messageResolver the MessageResolver used to resolve the error message
     * @param messageFormatter the MessageFormatter used to format the error message
     * @param locale the Locale used for message resolution and formatting
     * @return a RuleViolation object
     */
    public RuleViolation build(MessageResolver messageResolver, MessageFormatter messageFormatter, Locale locale) {
        String message = resolveErrorMessage(messageResolver, locale);
        RuleViolation result = new RuleViolation(ruleName, errorCode, severity,
                message != null
                        ? messageFormatter != null
                            ? messageFormatter.format(locale, message, params.toArray(new ParameterInfo[params.size()]))
                            : message
                        : null);

        addRuleParameters(result, params);
        return result;
    }

    /**
     * Resolves the error message using the provided MessageResolver and Locale.
     *
     * @param messageResolver the MessageResolver used to resolve the error message
     * @param locale the Locale used for message resolution
     * @return the resolved error message, or null if no message is found
     */
    private String resolveErrorMessage(MessageResolver messageResolver, Locale locale) {
        if (errorMessage != null) return errorMessage;
        return messageResolver != null ? messageResolver.resolve(locale, errorCode, defaultMessage) : null;
    }

    /**
     * Adds the rule parameters to a given RuleViolation object.
     *
     * @param error The RuleViolation object to which the parameters will be added.
     * @param parameters The list of ParameterInfo objects containing the parameter name and value.
     */
    private void addRuleParameters(RuleViolation error, List<ParameterInfo> parameters) {
        parameters.stream()
                .filter(Objects::nonNull)
                .forEach(m -> error.param(m.getName(),
                        m.getValue() != null
                                ? m.getValue().toString()
                                : null));
    }
}
