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
package org.rulii.rule;

/**
 * Represents the possible status of rule execution: PASS, FAIL, or SKIPPED.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public enum RuleExecutionStatus {
    PASS, FAIL, SKIPPED;

    /**
     * Checks if the current RuleExecutionStatus is equal to PASS.
     *
     * @return true if the RuleExecutionStatus is PASS, false otherwise
     */
    public boolean isPass() {
        return this == PASS;
    }

    /**
     * Checks if the current RuleExecutionStatus is equal to FAIL.
     *
     * @return true if the RuleExecutionStatus is FAIL, false otherwise
     */
    public boolean isFail() {
        return this == FAIL;
    }

    /**
     * Indicates whether the current RuleExecutionStatus is equal to SKIPPED.
     *
     * @return true if the RuleExecutionStatus is SKIPPED, false otherwise
     */
    public boolean isSkipped() {
        return this == SKIPPED;
    }
}
