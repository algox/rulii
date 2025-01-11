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

import org.rulii.lib.spring.util.Assert;
import org.rulii.rule.RuleExecutionStatus;
import org.rulii.rule.RuleResult;

import java.util.*;
import java.util.function.Predicate;

/**
 * Represents the execution status of a rule set.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class RuleSetExecutionStatus implements Iterable<RuleResult> {
    private final List<RuleResult> statuses = Collections.synchronizedList(new LinkedList<>());
    private boolean preConditionCheck = false;

    public RuleSetExecutionStatus() {
        super();
    }

    public void add(RuleResult result) {
        Assert.notNull(result, "result cannot be null.");
        statuses.add(result);
    }

    public RuleResult getLastResult() {
        int size = size();
        return size > 0 ? statuses.get(size - 1) : null;
    }

    public List<RuleResult> getRuleResult(String ruleName) {
        Assert.notNull(ruleName, "ruleName cannot be null.");
        return getRuleResults(r -> ruleName.equals(r.rule().getName()));
    }

    public List<RuleResult> getPassed() {
        return getRuleResults(RuleExecutionStatus.PASS);
    }

    public List<RuleResult> getPassedOrSkipped() {
        return getRuleResults(RuleExecutionStatus.PASS, RuleExecutionStatus.SKIPPED);
    }

    public List<RuleResult> getFailed() {
        return getRuleResults(RuleExecutionStatus.FAIL);
    }

    public List<RuleResult> getFailedOrSkipped() {
        return getRuleResults(RuleExecutionStatus.FAIL, RuleExecutionStatus.SKIPPED);
    }

    public List<RuleResult> getSkipped() {
        return getRuleResults(RuleExecutionStatus.SKIPPED);
    }

    public RuleResult get(int index) {
        return statuses.get(index);
    }

    public boolean isAllPass() {
        return isTrue(r -> r.status().isPass());
    }

    public boolean isAllPassOrSkip() {
        return isTrue(r -> r.status().isPass() || r.status().isSkipped());
    }

    public boolean isAnyPass() {
        return isTrue(r -> !r.status().isPass());
    }

    public boolean isAnySkip() {
        return isTrue(r -> !r.status().isSkipped());
    }

    public boolean isAllSkip() {
        return isTrue(r -> r.status().isSkipped());
    }

    public boolean isAllFail() {
        return isTrue(r -> r.status().isFail());
    }

    public boolean isAnyFail() {
        return isTrue(r -> !r.status().isFail());
    }

    public boolean isAllFailOrSkip() {
        return isTrue(r -> r.status().isFail() || r.status().isSkipped());
    }

    public boolean isTrue(RuleExecutionStatus...statuses) {
        Set<RuleExecutionStatus> values = statuses != null ? new HashSet<>(Arrays.asList(statuses)) : new HashSet<>();
        return isTrue(r -> values.contains(r.status()));
    }

    public boolean isTrue(Predicate<RuleResult> predicate) {
        boolean result = true;

        for (RuleResult ruleResult : statuses) {
            if (!predicate.test(ruleResult)) {
                result = false;
                break;
            }
        }

        return result;
    }

    public List<RuleResult> getRuleResults(RuleExecutionStatus...statuses) {
        Set<RuleExecutionStatus> values = statuses != null ? new HashSet<>(Arrays.asList(statuses)) : new HashSet<>();
        return getRuleResults(r -> values.contains(r.status()));
    }

    public List<RuleResult> getRuleResults(Predicate<RuleResult> predicate) {
        List<RuleResult> result = new ArrayList<>();

        for (RuleResult ruleResult : statuses) {
            if (predicate.test(ruleResult)) {
                result.add(ruleResult);
            }
        }

        return Collections.unmodifiableList(result);
    }

    @Override
    public Iterator<RuleResult> iterator() {
        return statuses.iterator();
    }

    public int size() {
        return statuses.size();
    }

    public boolean isPreConditionCheck() {
        return preConditionCheck;
    }

    void setPreConditionCheck(boolean preConditionCheck) {
        this.preConditionCheck = preConditionCheck;
    }

    @Override
    public String toString() {
        return "pre = " + preConditionCheck + "; " + statuses;
    }
}
