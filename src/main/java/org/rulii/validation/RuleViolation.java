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

import org.rulii.lib.spring.util.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Class containing all the details of a Rule Violation. It contains Rule Name, error code, severity, error message and all
 * relevant parameters.
 *
 * @author Max Arulananthan
 * @since 1.0
 *
 */
public class RuleViolation {

    private final String ruleName;
    private final String errorCode;
    private final Severity severity;
    private final String errorMessage;
    private final Map<String, String> params;

    /**
     * Creates a new RuleViolation with the given rule name, error code, severity, and error message.
     *
     * @param ruleName the name of the rule
     * @param errorCode the error code associated with the rule
     * @param severity the severity of the error (null defaults to Severity.ERROR)
     * @param errorMessage the error message (can be null)
     * @param params violation parameters.
     */
    public RuleViolation(String ruleName, String errorCode, Severity severity, String errorMessage, Map<String, String> params) {
        super();
        Assert.notNull(ruleName, "ruleName cannot be null.");
        Assert.hasText(errorCode, "errorCode cannot be null/empty.");
        this.ruleName = ruleName;
        this.errorCode = errorCode;
        this.severity = severity == null ? Severity.ERROR : severity;
        this.errorMessage = errorMessage;
        this.params = Collections.unmodifiableMap(params != null ? params : new HashMap<>());
    }

    /**
     * Returns a new instance of RuleViolationBuilderBuilder.
     *
     * @return a new instance of RuleViolationBuilderBuilder
     */
    public static RuleViolationBuilderBuilder builder() {
        return RuleViolationBuilderBuilder.getInstance();
    }

    /**
     * Name of the rule.
     *
     * @return rule name.
     */
    public String getRuleName() {
        return ruleName;
    }

    /**
     * Associated Error code.
     *
     * @return error code.
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Severity of this error.
     *
     * @return error severity.
     */
    public Severity getSeverity() {
        return severity;
    }

    /**
     * Optional error message.
     *
     * @return error message.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Associated rule parameters.
     *
     * @return rule parameters.
     */
    public Map<String, String> getParameters() {
        return params;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RuleViolation that = (RuleViolation) o;
        return ruleName.equals(that.ruleName) &&
                errorCode.equals(that.errorCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ruleName, errorCode);
    }

    @Override
    public String toString() {
        return "RuleViolation{" +
                "ruleName='" + ruleName + '\'' +
                ", errorCode='" + errorCode + '\'' +
                ", severity=" + severity +
                ", errorMessage='" + errorMessage + '\'' +
                ", params=" + params +
                '}';
    }
}
