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
package org.rulii.ruleset;

import org.rulii.bind.match.MatchByTypeMatchingStrategy;
import org.rulii.lang.condition.Condition;
import org.rulii.context.RuleContext;
import org.rulii.rule.Rule;

public final class RuleSetConditions {

    private RuleSetConditions() {
        super();
    }

    public static Condition stopOnPassCount(int count) {
        return Condition.builder().with((RuleSetExecutionStatus ruleSetResult) ->
                ruleSetResult.getPassed().size() >= count
        )
                .param(0)
                    .matchUsing(MatchByTypeMatchingStrategy.class)
                .build()
                .build();
    }

    public static Condition stopWhenOnePasses() {
        return stopOnPassCount(1);
    }

    public static Condition stopOnFailCount(int count) {
        return Condition.builder().with((RuleSetExecutionStatus ruleSetResult) ->
                        ruleSetResult.getFailed().size() >= count
                )
                .param(0)
                    .matchUsing(MatchByTypeMatchingStrategy.class)
                .build()
                .build();
    }

    public static Condition stopWhenOneFails() {
       return stopOnFailCount(1);
    }

    public static Condition stopOnSkipCount(int count) {
        return Condition.builder().with((RuleSetExecutionStatus ruleSetResult) ->
                        ruleSetResult.getSkipped().size() >= count
                )
                .param(0)
                    .matchUsing(MatchByTypeMatchingStrategy.class)
                .build()
                .build();
    }

    public static Condition stopWhenOneFailsOrSKipped() {
        return stopOnFailCount(1).or(stopOnSkipCount(1));
    }

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
