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

/**
 * Error severities are defined as follows:
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public enum Severity {

    // A severe error. Should not proceed any further.
    FATAL,
    // A severe error. Action must be taken to fix it.
    ERROR,
    // Action must be taken at some time to prevent this situation.
    WARNING,
    // Informative message.
    INFO
}
