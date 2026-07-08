/*
 * This software is licensed under the Apache 2 license, quoted below.
 *
 * Copyright (c) 1999-2026, Algorithmx Inc.
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
 * Accumulates the {@link RuleResult}s produced while a {@link RuleSet} runs, in the order the
 * rules were executed, along with whether the RuleSet's pre-condition passed.
 * <p>
 * An instance of this class is created fresh for each RuleSet execution (see
 * {@link RuleSetExecutionStrategyTemplate#createRuleSetScope}) and is also the default result type
 * returned by {@link RuleSet#run(org.rulii.context.RuleContext)} when no
 * {@code resultExtractor} is configured on the builder.
 * <p>
 * The {@code isAllXxx}/{@code isAnyXxx} family of methods are queries over the accumulated results:
 * {@code isAllXxx()} is a universal check (vacuously {@code true} when no results have been recorded
 * yet) and {@code isAnyXxx()} is an existential check (always {@code false} when no results have been
 * recorded).
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class RuleSetExecutionStatus implements Iterable<RuleResult> {
    private final List<RuleResult> statuses = Collections.synchronizedList(new LinkedList<>());
    private boolean preConditionCheck = false;

    /**
     * Creates a new, empty RuleSetExecutionStatus with no recorded results and
     * {@link #isPreConditionCheck()} defaulting to {@code false}.
     */
    public RuleSetExecutionStatus() {
        super();
    }

    /**
     * Records the result of executing a single Rule in the RuleSet.
     *
     * @param result the RuleResult to record; must not be null.
     */
    public void add(RuleResult result) {
        Assert.notNull(result, "result cannot be null.");
        statuses.add(result);
    }

    /**
     * Returns the most recently recorded RuleResult.
     *
     * @return the last RuleResult added via {@link #add(RuleResult)}, or {@code null} if no
     * results have been recorded yet.
     */
    public RuleResult getLastResult() {
        int size = size();
        return size > 0 ? statuses.get(size - 1) : null;
    }

    /**
     * Returns all recorded results for the Rule with the given name.
     *
     * @param ruleName name of the Rule to look up; must not be null.
     * @return an unmodifiable list of matching RuleResults, in execution order; empty if none match.
     */
    public List<RuleResult> getRuleResult(String ruleName) {
        Assert.notNull(ruleName, "ruleName cannot be null.");
        return getRuleResults(r -> ruleName.equals(r.rule().getName()));
    }

    /**
     * Returns all recorded results with status {@link RuleExecutionStatus#PASS}.
     *
     * @return an unmodifiable list of matching RuleResults, in execution order; empty if none match.
     */
    public List<RuleResult> getPassed() {
        return getRuleResults(RuleExecutionStatus.PASS);
    }

    /**
     * Returns all recorded results with status {@link RuleExecutionStatus#PASS} or
     * {@link RuleExecutionStatus#SKIPPED}.
     *
     * @return an unmodifiable list of matching RuleResults, in execution order; empty if none match.
     */
    public List<RuleResult> getPassedOrSkipped() {
        return getRuleResults(RuleExecutionStatus.PASS, RuleExecutionStatus.SKIPPED);
    }

    /**
     * Returns all recorded results with status {@link RuleExecutionStatus#FAIL}.
     *
     * @return an unmodifiable list of matching RuleResults, in execution order; empty if none match.
     */
    public List<RuleResult> getFailed() {
        return getRuleResults(RuleExecutionStatus.FAIL);
    }

    /**
     * Returns all recorded results with status {@link RuleExecutionStatus#FAIL} or
     * {@link RuleExecutionStatus#SKIPPED}.
     *
     * @return an unmodifiable list of matching RuleResults, in execution order; empty if none match.
     */
    public List<RuleResult> getFailedOrSkipped() {
        return getRuleResults(RuleExecutionStatus.FAIL, RuleExecutionStatus.SKIPPED);
    }

    /**
     * Returns all recorded results with status {@link RuleExecutionStatus#SKIPPED}.
     *
     * @return an unmodifiable list of matching RuleResults, in execution order; empty if none match.
     */
    public List<RuleResult> getSkipped() {
        return getRuleResults(RuleExecutionStatus.SKIPPED);
    }

    /**
     * Returns the RuleResult recorded at the given index, in execution order.
     *
     * @param index zero-based index of the result to retrieve.
     * @return the RuleResult at the given index.
     * @throws IndexOutOfBoundsException if the index is out of range.
     */
    public RuleResult get(int index) {
        return statuses.get(index);
    }

    /**
     * Determines whether every recorded result has status {@link RuleExecutionStatus#PASS}.
     *
     * @return {@code true} if all recorded results passed, or if no results have been recorded yet;
     * {@code false} otherwise.
     */
    public boolean isAllPass() {
        return isTrue(r -> r.status().isPass());
    }

    /**
     * Determines whether every recorded result has status {@link RuleExecutionStatus#PASS} or
     * {@link RuleExecutionStatus#SKIPPED}.
     *
     * @return {@code true} if all recorded results passed or were skipped, or if no results have
     * been recorded yet; {@code false} otherwise.
     */
    public boolean isAllPassOrSkip() {
        return isTrue(r -> r.status().isPass() || r.status().isSkipped());
    }

    /**
     * Determines whether at least one recorded result has status {@link RuleExecutionStatus#PASS}.
     *
     * @return {@code true} if any recorded result passed; {@code false} if none did, or if no
     * results have been recorded yet.
     */
    public boolean isAnyPass() {
        return !isTrue(r -> !r.status().isPass());
    }

    /**
     * Determines whether at least one recorded result has status {@link RuleExecutionStatus#SKIPPED}.
     *
     * @return {@code true} if any recorded result was skipped; {@code false} if none were, or if no
     * results have been recorded yet.
     */
    public boolean isAnySkip() {
        return !isTrue(r -> !r.status().isSkipped());
    }

    /**
     * Determines whether every recorded result has status {@link RuleExecutionStatus#SKIPPED}.
     *
     * @return {@code true} if all recorded results were skipped, or if no results have been
     * recorded yet; {@code false} otherwise.
     */
    public boolean isAllSkip() {
        return isTrue(r -> r.status().isSkipped());
    }

    /**
     * Determines whether every recorded result has status {@link RuleExecutionStatus#FAIL}.
     *
     * @return {@code true} if all recorded results failed, or if no results have been recorded yet;
     * {@code false} otherwise.
     */
    public boolean isAllFail() {
        return isTrue(r -> r.status().isFail());
    }

    /**
     * Determines whether at least one recorded result has status {@link RuleExecutionStatus#FAIL}.
     *
     * @return {@code true} if any recorded result failed; {@code false} if none did, or if no
     * results have been recorded yet.
     */
    public boolean isAnyFail() {
        return !isTrue(r -> !r.status().isFail());
    }

    /**
     * Determines whether every recorded result has status {@link RuleExecutionStatus#FAIL} or
     * {@link RuleExecutionStatus#SKIPPED}.
     *
     * @return {@code true} if all recorded results failed or were skipped, or if no results have
     * been recorded yet; {@code false} otherwise.
     */
    public boolean isAllFailOrSkip() {
        return isTrue(r -> r.status().isFail() || r.status().isSkipped());
    }

    /**
     * Determines whether every recorded result's status is one of the given statuses.
     *
     * @param statuses the statuses to test against; a {@code null} or empty array matches nothing,
     * so this returns {@code true} only if no results have been recorded yet.
     * @return {@code true} if every recorded result's status is contained in {@code statuses}, or
     * if no results have been recorded yet; {@code false} otherwise.
     */
    public boolean isTrue(RuleExecutionStatus...statuses) {
        Set<RuleExecutionStatus> values = statuses != null ? new HashSet<>(Arrays.asList(statuses)) : new HashSet<>();
        return isTrue(r -> values.contains(r.status()));
    }

    /**
     * Determines whether the given predicate holds for every recorded result. Short-circuits and
     * returns {@code false} on the first result the predicate rejects.
     *
     * @param predicate the condition to test each recorded RuleResult against.
     * @return {@code true} if the predicate holds for every recorded result, or if no results have
     * been recorded yet (vacuous truth); {@code false} otherwise.
     */
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

    /**
     * Returns all recorded results whose status is one of the given statuses.
     *
     * @param statuses the statuses to filter by; a {@code null} or empty array matches nothing.
     * @return an unmodifiable list of matching RuleResults, in execution order; empty if none match.
     */
    public List<RuleResult> getRuleResults(RuleExecutionStatus...statuses) {
        Set<RuleExecutionStatus> values = statuses != null ? new HashSet<>(Arrays.asList(statuses)) : new HashSet<>();
        return getRuleResults(r -> values.contains(r.status()));
    }

    /**
     * Returns all recorded results that satisfy the given predicate.
     *
     * @param predicate the condition to filter recorded RuleResults by.
     * @return an unmodifiable list of matching RuleResults, in execution order; empty if none match.
     */
    public List<RuleResult> getRuleResults(Predicate<RuleResult> predicate) {
        List<RuleResult> result = new ArrayList<>();

        for (RuleResult ruleResult : statuses) {
            if (predicate.test(ruleResult)) {
                result.add(ruleResult);
            }
        }

        return Collections.unmodifiableList(result);
    }

    /**
     * Returns an iterator over the recorded results, in execution order.
     *
     * @return an Iterator over the recorded RuleResults.
     */
    @Override
    public Iterator<RuleResult> iterator() {
        return statuses.iterator();
    }

    /**
     * Returns the number of results recorded so far.
     *
     * @return the count of recorded RuleResults.
     */
    public int size() {
        return statuses.size();
    }

    /**
     * Determines whether the RuleSet's pre-condition passed.
     *
     * @return {@code true} if the pre-condition passed (or the RuleSet had no pre-condition);
     * {@code false} if the pre-condition was checked and failed.
     */
    public boolean isPreConditionCheck() {
        return preConditionCheck;
    }

    /**
     * Records the outcome of the RuleSet's pre-condition check. Package-private: set once by the
     * execution strategy driving this RuleSet's run.
     *
     * @param preConditionCheck the outcome of the pre-condition check.
     */
    void setPreConditionCheck(boolean preConditionCheck) {
        this.preConditionCheck = preConditionCheck;
    }

    @Override
    public String toString() {
        return "pre = " + preConditionCheck + "; " + statuses;
    }
}
