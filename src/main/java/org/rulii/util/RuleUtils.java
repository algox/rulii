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
package org.rulii.util;

import org.rulii.bind.match.ParameterMatch;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.MethodDefinition;
import org.rulii.model.Runnable;
import org.rulii.model.UnrulyException;
import org.rulii.rule.Rule;
import org.rulii.rule.RuleDefinition;
import org.rulii.ruleset.RuleSet;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Utility class to provide convenience methods for Rules.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public final class RuleUtils {

    public static final String TAB              = "\t";
    public static final String NAME_REGEX       = "[a-zA-Z$_][a-zA-Z0-9$_]*";
    private static final Pattern NAME_PATTERN   = Pattern.compile(NAME_REGEX);
    private static final int MAX_SUMMARY_SIZE   = 80;

    private RuleUtils() {
        super();
    }

    /**
     * Successful result to a Rule execution.
     *
     * @return true
     */
    public static boolean PASS() {
        return true;
    }

    /**
     * Unsuccessful result to a Rule execution.
     *
     * @return false
     */
    public static boolean FAIL() {
        return false;
    }

    /**
     * Determines whether the given name is "valid" Name. It needs to follow the following regex ^[a-zA-Z][a-zA-Z0-9]*?$
     *
     * @param name desired Rule Name.
     * @return true if the name is valid; false otherwise.
     */
    public static boolean isValidName(String name) {
        return name != null && !name.trim().isEmpty() && NAME_PATTERN.matcher(name).matches();
    }

    /**
     * Validates a given name. It throws an exception if the name is invalid.
     *
     * @param name the name to be validated
     * @throws UnrulyException if the name is invalid
     */
    public static void validateName(String name) {
        if (!isValidName(name)) throw new UnrulyException("Invalid name [" + name
                + "], it must not be empty and follow regex [" + NAME_REGEX + "]");
    }

    /**
     * Merges the first rule with the rest of the Rules.
     *
     * @param rule new rule.
     * @param others other rules.
     * @return merged array of rules.
     */
    public static Rule[] merge(Rule rule, Rule[] others) {
        Assert.isTrue(others != null && others.length > 0,
                "others cannot be null and must have at least 1 element");
        Rule[] result = new Rule[others.length + 1];
        result[0] = rule;
        System.arraycopy(others, 0, result, 1, others.length);
        return result;
    }

    /**
     * Creates a Predicate that filters Rules based on their package names.
     *
     * @param packageNames the package names to filter by
     * @return the Predicate used to filter Rules
     */
    public static Predicate<Rule> createPackageRuleFilter(String...packageNames) {
        return r -> {
            if (packageNames == null || packageNames.length == 0) return true;
            Set<String> names = new HashSet<>(Arrays.asList(packageNames));
            if (r.getTarget() == null) return false;
            return names.contains(r.getTarget().getClass().getPackage().getName());
        };
    }

    public static String getSignature(Runnable<?> data, List<ParameterMatch> matches, List<?> values) {
        StringBuilder result = new StringBuilder(data.getDescription());
        Map<String, Object> parameters = convert(matches, values);

        if (!parameters.isEmpty()) {
            int size = parameters.size();
            int index = 0;

            result.append(" args [");

            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                result.append(entry.getKey()).append(" = ").append(RuleUtils.getSummaryTextValue(entry.getValue()));
                if (index < size - 1) result.append(", ");
                index++;
            }

            result.append("]");
        }

        return result.toString();
    }

    private static Map<String, Object> convert(List<ParameterMatch> matches, List<?> values) {
        if (matches == null) return new LinkedHashMap<>();
        if (matches.isEmpty()) return new LinkedHashMap<>();
        if (matches.size() != values.size()) throw new UnrulyException("Size mismatch [" + matches.size() + "] [" + values.size() + "]");

        Map<String, Object> result = new LinkedHashMap<>();
        int size = matches.size();

        for (int i = 0; i < size; i++) {
            result.put(matches.get(i).getDefinition().getName(), values.get(i));
        }

        return Collections.unmodifiableMap(result);
    }

    /**
     * Returns the description of a RuleSet.
     *
     * @param ruleSet the RuleSet for which to get the description
     * @param prefix the prefix to use for the description
     * @return the description of the RuleSet
     */
    public static String getRuleSetDescription(RuleSet ruleSet, String prefix) {
        return prefix + "RuleSet Name    : " + ruleSet.getName() + System.lineSeparator() +
                prefix + "Description     : " + ruleSet.getDescription() + System.lineSeparator() +
                prefix + "PreCondition    : " + (ruleSet.getPreCondition() != null) + System.lineSeparator() +
                prefix + "StopCondition    : " + (ruleSet.getStopCondition() != null) + System.lineSeparator() +
                prefix + "Number of Rules : " + ruleSet.size() + System.lineSeparator();
    }

    /**
     * Retrieves the description of a Rule.
     *
     * @param ruleDefinition    The RuleDefinition object representing the rule.
     * @param methodDefinition  The MethodDefinition object representing the method.
     * @param prefix            The prefix to use for the description.
     * @return The description of the Rule.
     */
    public static String getRuleDescription(RuleDefinition ruleDefinition, MethodDefinition methodDefinition,
                                            String prefix) {
        StringBuilder result = new StringBuilder();
        result.append(prefix).append("Rule Name   : ").append(ruleDefinition.getName()).append(System.lineSeparator());
        result.append(prefix).append("Rule Class  : ").append(ruleDefinition.getRuleClass().getSimpleName()).append(System.lineSeparator());

        if (ruleDefinition.getDescription() != null) {
            result.append(prefix).append("Description : ").append(ruleDefinition.getDescription()).append(System.lineSeparator());
        }

        result.append(prefix).append("Method      : ").append(methodDefinition.getSignature()).append(System.lineSeparator());
        return result.toString();
    }

    /**
     * Retrieves the description of a method.
     *
     * @param methodDefinition The MethodDefinition object representing the method.
     * @param matches          The array of ParameterMatch objects representing parameter matches.
     * @param values           The array of values representing the actual parameter values.
     * @param prefix           The prefix to use for the description.
     * @return The description of the method.
     */
    public static String getMethodDescription(MethodDefinition methodDefinition, ParameterMatch[] matches,
                                              Object[] values, String prefix) {
        StringBuilder result = new StringBuilder();
        result.append(prefix).append("Method : ").append(methodDefinition.getSignature()).append(System.lineSeparator());

        if (!methodDefinition.getParameterDefinitions().isEmpty()) {
            result.append(prefix).append("Parameter Matches :");
            result.append(System.lineSeparator());
            result.append(getArgumentDescriptions(methodDefinition, matches, values, (prefix + TAB)));
        }

        return result.toString();
    }

    /**
     * Returns the argument descriptions for a given method.
     *
     * @param methodDefinition The MethodDefinition object representing the method.
     * @param matches          The array of ParameterMatch objects representing parameter matches.
     * @param values           The array of values representing the actual parameter values.
     * @param prefix           The prefix to use for the description.
     * @return The argument descriptions for the method.
     */
    public static String getArgumentDescriptions(MethodDefinition methodDefinition, ParameterMatch[] matches,
                                                 Object[] values, String prefix) {
        if (matches == null && values == null) return "";

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < methodDefinition.getParameterDefinitions().size(); i++) {
            result.append(prefix).append("Parameter(index = ").append(i).append(") :");

            result.append(" (").append(methodDefinition.getParameterDefinition(i).getTypeName()).append(" ")
                    .append(methodDefinition.getParameterDefinition(i).getName()).append(" = ");

            if (values != null && i < values.length && values[i] != null) {
                result.append(getSummaryTextValue(values[i], 80)).append(")");
            } else {
                result.append("null)");
            }

            if (matches != null && i < matches.length && matches[i] != null) {
                if (matches[i].getBinding() != null) {
                    result.append(System.lineSeparator());
                    result.append(prefix);
                    result.append("Matched Binding      : (").append(matches[i].getBinding().getTypeAndName()).append(")");

                    boolean mismatch = matches[i].getBinding().isAssignable(methodDefinition.getParameterDefinition(i).getType());

                    if (!mismatch) result.append(" ! Types Incompatible Expected [")
                            .append(methodDefinition.getParameterDefinition(i).getTypeName()).append("]")
                            .append(" Given [")
                            .append(matches[i].getBinding().getTypeName()).append("]");
                }
            }

            if (i < methodDefinition.getParameterDefinitions().size() - 1) result.append(System.lineSeparator());
        }

        return result.toString();
    }

    /**
     * Returns a string consisting of indentation tabs based on the count.
     * Each tab is represented by the TAB constant defined in the RuleUtils class.
     *
     * @param count the number of indentation tabs to generate
     * @return the string of indentation tabs
     */
    public static String getTabs(int count) {
        return TAB.repeat(Math.max(0, count));
    }

    /**
     * Returns a summary text value of an object. If the object is null, it returns "null".
     * If the string representation of the object is longer than the maximum length specified,
     * it returns a truncated version of the string with "..." appended. Otherwise, it returns
     * the string representation of the object.
     *
     * @param value the object to get the summary text value of
     * @return the summary text value of the object
     */
    public static String getSummaryTextValue(Object value) {
        return getSummaryTextValue(value, MAX_SUMMARY_SIZE);
    }

    /**
     * Returns a summary text value of an object. If the object is null, it returns "null".
     * If the string representation of the object is longer than the maximum length specified,
     * it returns a truncated version of the string with "..." appended. Otherwise, it returns
     * the string representation of the object.
     *
     * @param value the object to get the summary text value of
     * @param max the maximum length of the summary text value
     * @return the summary text value of the object
     */
    public static String getSummaryTextValue(Object value, int max) {
        if (value == null) return "null";
        String result = value.toString();
        if (result == null) return "null";
        return result.length() > max ? (result.substring(0, max) + "...") : result;
    }
}
