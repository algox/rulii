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

import java.util.*;

/**
 * Container for all Rule Violations.
 *
 * @author Max Arulananthan
 * @since 1.0
 *
 */
public class RuleViolations implements Iterable<RuleViolation> {

    private final List<RuleViolation> errors = Collections.synchronizedList(new ArrayList<>());

    public RuleViolations() {
        super();
    }

    /**
     * Adds a new violation.
     *
     * @param error error to be added.
     */
    public void add(RuleViolation error) {
        Assert.notNull(error, "error cannot be null.");
        errors.add(error);
    }

    /**
     * Adds multiple RuleViolation objects to the container.
     *
     * @param errors the RuleViolation objects to be added to the container
     */
    public void addAll(RuleViolation...errors) {
        Assert.notNull(errors, "error cannot be null.");
        this.errors.addAll(Arrays.asList(errors));
    }

    /**
     * Adds all the RuleViolation objects from the specified collection to this container.
     *
     * @param errors the collection of RuleViolation objects to be added
     * @throws IllegalArgumentException if the collection is null
     */
    public void addAll(Collection<RuleViolation> errors) {
        Assert.notNull(errors, "error cannot be null.");
        this.errors.addAll(errors);
    }

    /**
     * Adds a new violation with the given name and error code.
     *
     * @param ruleName rule name.
     * @param errorCode error code.
     * @return newly created violation.
     */
    public RuleViolation add(String ruleName, String errorCode) {
        return add(ruleName, errorCode, Severity.ERROR, null);
    }

    /**
     * Adds a new violation with the given name, error code and error message.
     *
     * @param ruleName rule name.
     * @param errorCode error code.
     * @param severity severity of the error.
     * @param errorMessage error message.
     * @return newly created violation.
     */
    public RuleViolation add(String ruleName, String errorCode, Severity severity, String errorMessage) {
        RuleViolation result = new RuleViolation(ruleName, errorCode, severity, errorMessage, null);
        errors.add(result);
        return result;
    }

    /**
     * Checks if the RuleViolations container is empty.
     *
     * @return true if the container is empty, false otherwise.
     */
    public boolean isEmpty() {
        return errors.isEmpty();
    }

    /**
     * Determines if this container has any associated errors (errors of severity FATAL or ERROR).
     *
     * @return true if this container has any errors (errors of severity FATAL or ERROR); false otherwise.
     */
    public boolean hasSevereErrors() {
        return errors.stream().filter(e -> e.getSeverity() == Severity.FATAL || e.getSeverity() == Severity.ERROR).count() > 0;
    }

    /**
     * Determines if this container has any associated errors.
     *
     * @return true if this container has any errors; false otherwise.
     */
    public boolean hasErrors() {
        return getErrorCount(Severity.ERROR) > 0;
    }

    /**
     * Returns the number of errors with the desired severity.
     *
     * @param severity desired severity.
     * @return number of errors with the desired severity.
     */
    public long getErrorCount(Severity severity) {
        return errors.stream().filter(e -> e.getSeverity() == severity).count();
    }

    /**
     * Determines if there are any FATAL errors in this container.
     *
     * @return true if there are any FATAL errors; false otherwise.
     */
    public boolean hasFatalErrors() {
        return getErrorCount(Severity.FATAL) > 0;
    }

    /**
     * Determines if there are any WARNINGS in this container.
     *
     * @return true if there are any WARNINGS; false otherwise.
     */
    public boolean hasWarnings() {
        return getErrorCount(Severity.WARNING) > 0;
    }

    /**
     * Determines if there are any INFO messages in this container.
     *
     * @return true if there are any INFO messages; false otherwise.
     */
    public boolean hasInfoMessages() {
        return getErrorCount(Severity.INFO) > 0;
    }

    /**
     * Retrieves the RuleViolation object at the specified index.
     *
     * @param index the index of the RuleViolation object to retrieve
     * @return the RuleViolation object at the specified index
     */
    public RuleViolation getViolation(int index) {
        return errors.get(index);
    }

    /**
     * Returns all the associated violation.
     *
     * @return Rule Violations.
     */
    public List<RuleViolation> getViolations() {
        if (errors.isEmpty()) return Collections.emptyList();
        return Collections.unmodifiableList(errors);
    }

    /**
     * Returns the number of errors in this container.
     *
     * @return size of the container.
     */
    public int size() {
        return errors.size();
    }

    @Override
    public Iterator<RuleViolation> iterator() {
        return errors.iterator();
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("Violations [");
        for (int i = 0; i < errors.size(); i++) {
            result.append(errors.get(i).toString()).append("]");
            if (i < errors.size() - 1) result.append(System.lineSeparator());
        }
        return result.toString();
    }
}
