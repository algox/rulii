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
package org.rulii.validation.rules.fileexists;

import org.rulii.annotation.Description;
import org.rulii.annotation.Rule;
import org.rulii.context.RuleContext;
import org.rulii.model.UnrulyException;
import org.rulii.validation.BindingSupplier;
import org.rulii.validation.BindingValidationRule;
import org.rulii.validation.Severity;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Validation Rule to make sure the file exists.
 *
 * @author Max Arulananthan
 * @since 1.0
 *
 */
@Rule
@Description("File must exist.")
public class FileExistsValidationRule extends BindingValidationRule {

    public static List<Class<?>> SUPPORTED_TYPES    = List.of(CharSequence.class);

    public static final String ERROR_CODE       = "fileExistsValidationRule.errorCode";
    public static final String DEFAULT_MESSAGE  = "File {0} does not exist.";

    /**
     * Creates a FileExistsValidationRule with the specified binding name.
     *
     * @param bindingName the name of the binding for this rule
     */
    public FileExistsValidationRule(String bindingName) {
        this(bindingName, ERROR_CODE, Severity.ERROR, null);
    }

    /**
     * Creates a FileExistsValidationRule with the specified binding name and error code. The severity is set to ERROR by default.
     *
     * @param bindingName the name of the binding for this rule
     * @param errorCode the error code associated with this rule
     */
    public FileExistsValidationRule(String bindingName, String errorCode) {
        this(bindingName, errorCode, Severity.ERROR, null);
    }

    /**
     * Creates a FileExistsValidationRule with the specified parameters.
     *
     * @param bindingName the name of the binding for this rule
     * @param errorCode the error code associated with this rule
     * @param severity the severity of the error
     * @param errorMessage the error message to be displayed if the validation rule fails
     */
    public FileExistsValidationRule(String bindingName, String errorCode, Severity severity, String errorMessage) {
        super(bindingName, errorCode, severity, errorMessage, DEFAULT_MESSAGE);
    }

    /**
     * Represents a validation rule used to ensure that a file exists.
     *
     * @param bindingSupplier The supplier of bindings for rule evaluation. Must not be null.
     * @param errorCode The error code associated with the validation rule.
     * @param severity The severity of the error.
     * @param errorMessage The error message that will be displayed if the validation rule fails.
     */
    public FileExistsValidationRule(BindingSupplier bindingSupplier, String errorCode, Severity severity,
                                    String errorMessage) {
        super(bindingSupplier, errorCode, severity, errorMessage, DEFAULT_MESSAGE);
    }

    @Override
    protected boolean isValid(RuleContext ruleContext, Object value) {
        if (value == null) return true;

        if (!(value instanceof CharSequence))
            throw new UnrulyException("FileExistsValidationRule only applies to CharSequences."
                    + "Supplied Class [" + value.getClass() + "] value [" + value + "]");

        String fileName = value.toString();
        Path path = Paths.get(fileName);
        return Files.exists(path);
    }

    @Override
    public List<Class<?>> getSupportedTypes() {
        return SUPPORTED_TYPES;
    }

    @Override
    public String toString() {
        return "FileExistsValidationRule";
    }
}
