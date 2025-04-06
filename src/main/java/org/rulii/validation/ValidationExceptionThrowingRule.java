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

import org.rulii.annotation.Description;
import org.rulii.annotation.Rule;
import org.rulii.annotation.Then;

/**
 * Rule to throw ValidationException if there are any violations.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
@Rule()
@Description("Rule to throw ValidationException if there are any violations.")
public class ValidationExceptionThrowingRule {

    public ValidationExceptionThrowingRule() {
        super();
    }

    @Then
    public void then(RuleViolations violations) {
        if (violations == null || violations.isEmpty()) return;
        // Check if there are any ERROR or FATAL_ERROR errors.
        if (violations.hasSevereErrors()) throw new ValidationException(violations);
    }
}
