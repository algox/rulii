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

/**
 * This class represents an exception that occurs during validation.
 * It is a subclass of the UnrulyException class.
 *
 * The validation exception contains a collection of rule violations that caused the validation failure.
 * It provides methods to retrieve the rule violations and check for specific types of errors.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class ValidationException extends UnrulyException {

    private final RuleViolations violations;

    /**
     * This method creates a new instance of the ValidationException class with the given RuleViolations object.
     *
     * @param violations the RuleViolations object representing the collection of rule violations that caused the validation failure
     */
    public ValidationException(RuleViolations violations) {
        super();
        this.violations = violations;
    }

    /**
     * Returns the rule violations associated with this validation exception.
     *
     * @return an instance of the RuleViolations class representing the collection of rule violations that caused the validation failure
     */
    public RuleViolations getViolations() {
        return violations;
    }
}
