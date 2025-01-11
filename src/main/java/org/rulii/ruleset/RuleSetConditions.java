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
package org.rulii.ruleset;

import org.rulii.bind.match.MatchByTypeMatchingStrategy;
import org.rulii.context.RuleContext;
import org.rulii.model.condition.Condition;
import org.rulii.rule.Rule;

/**
 * Represents a utility class that provides various conditions commonly used in RuleSet implementations.
 * This class contains static methods to create different types of conditions, such as stopping conditions based on pass count, fail count, or skip count,
 * as well as conditions where all rules must pass, one rule must pass, none can pass, or only one can pass.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public final class RuleSetConditions {

    private RuleSetConditions() {
        super();
    }

    /**
     * Creates a Condition that stops when the passed count exceeds a specified threshold.
     *
     * @param count the threshold value for the number of passed conditions to stop
     * @return the Condition instance that stops the execution when the passed count reaches or exceeds the given threshold
     */
    public static Condition stopOnPassCount(int count) {
        return Condition.builder().with((RuleSetExecutionStatus ruleSetResult) ->
                ruleSetResult.getPassed().size() >= count
        )
                .param(0)
                    .matchUsing(MatchByTypeMatchingStrategy.class)
                .build()
                .build();
    }

    /**
     * Returns a Condition instance that stops the execution when the number of passed conditions reaches or exceeds one.
     *
     * @return the Condition instance that stops the execution when the passed count reaches or exceeds one
     */
    public static Condition stopWhenOnePasses() {
        return stopOnPassCount(1);
    }

    /**
     * Creates a Condition that stops when the number of failed conditions reached or exceeds a specified count threshold.
     *
     * @param count the threshold value for the number of failed conditions to trigger the stop
     * @return the Condition instance representing the behavior to stop when the failed count exceeds the given threshold
     */
    public static Condition stopOnFailCount(int count) {
        return Condition.builder().with((RuleSetExecutionStatus ruleSetResult) ->
                        ruleSetResult.getFailed().size() >= count
                )
                .param(0)
                    .matchUsing(MatchByTypeMatchingStrategy.class)
                .build()
                .build();
    }

    /**
     * Returns a Condition instance that stops the execution when at least one condition fails.
     *
     * @return the Condition instance that stops the execution when at least one condition fails
     */
    public static Condition stopWhenOneFails() {
       return stopOnFailCount(1);
    }

    /**
     * Creates a Condition that stops the execution when the number of skipped items exceeds a specified count threshold.
     *
     * @param count the threshold value for the number of skipped items to trigger the stop
     * @return the Condition instance that stops the execution when the skipped count reaches or exceeds the given threshold
     */
    public static Condition stopOnSkipCount(int count) {
        return Condition.builder().with((RuleSetExecutionStatus ruleSetResult) ->
                        ruleSetResult.getSkipped().size() >= count
                )
                .param(0)
                    .matchUsing(MatchByTypeMatchingStrategy.class)
                .build()
                .build();
    }

    /**
     * Creates a Condition that stops the execution when at least one condition fails or when the number of skipped items exceeds a specified count threshold.
     * This Condition is a combination of stopping on the failure of one condition and stopping when skipped items reach a certain count.
     *
     * @return the Condition instance representing the behavior to stop when at least one condition fails or skipped items exceed the given threshold
     */
    public static Condition stopWhenOneFailsOrSKipped() {
        return stopOnFailCount(1).or(stopOnSkipCount(1));
    }

    /**
     * Creates a Condition that checks if all rules in the RuleSet pass.
     *
     * @return the Condition instance that checks if all rules pass
     */
    public static Condition allMustPass() {
        return Condition.builder().with((RuleContext ruleContext, RuleSet<?> ruleSet) -> {
                    boolean result = true;

                    for (Rule rule : ruleSet) {
                        if (!rule.isTrue(ruleContext)) {
                            result = false;
                            break;
                        }
                    }

                    return result;
                })
                .param(0)
                    .matchUsing(MatchByTypeMatchingStrategy.class)
                .build()
                .param(1)
                    .matchUsing(MatchByTypeMatchingStrategy.class)
                .build()
                .build();
    }

    /**
     * Creates a Condition that checks if at least one rule in the RuleSet passes.
     *
     * @return the Condition instance representing the logic to determine if at least one rule passes
     */
    public static Condition oneMustPass() {
        return Condition.builder().with((RuleContext ruleContext, RuleSet<?> ruleSet) -> {
                    boolean result = false;

                    for (Rule rule : ruleSet) {
                        if (rule.isTrue(ruleContext)) {
                            result = true;
                            break;
                        }
                    }

                    return result;
                })
                .param(0)
                    .matchUsing(MatchByTypeMatchingStrategy.class)
                .build()
                .param(1)
                    .matchUsing(MatchByTypeMatchingStrategy.class)
                .build()
                .build();
    }

    /**
     * Returns a Condition instance that checks if none of the rules in the RuleSet can pass.
     *
     * @return the Condition instance that checks if none of the rules can pass
     */
    public static Condition noneCanPass() {
        return Condition.builder().with((RuleContext ruleContext, RuleSet<?> ruleSet) -> {
                    int count = 0;

                    for (Rule rule : ruleSet) {
                        if (rule.isTrue(ruleContext)) {
                            count++;
                            break;
                        }
                    }

                    return count == 0;
                })
                .param(0)
                    .matchUsing(MatchByTypeMatchingStrategy.class)
                .build()
                .param(1)
                    .matchUsing(MatchByTypeMatchingStrategy.class)
                .build()
                .build();
    }

    /**
     * Creates a Condition that allows only one rule to pass in the RuleSet.
     *
     * @return the Condition instance that evaluates to true if only one rule passes in the RuleSet
     */
    public static Condition onlyOneCanPass() {
        return Condition.builder().with((RuleContext ruleContext, RuleSet<?> ruleSet) -> {
                    int count = 0;

                    for (Rule rule : ruleSet) {
                        if (rule.isTrue(ruleContext)) {
                            count++;
                            break;
                        }
                    }

                    return count == 1;
                })
                .param(0)
                    .matchUsing(MatchByTypeMatchingStrategy.class)
                .build()
                .param(1)
                    .matchUsing(MatchByTypeMatchingStrategy.class)
                .build()
                .build();
    }
}
