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
package org.rulii.test.ruleflow;

import org.junit.jupiter.api.Test;
import org.rulii.bind.Bindings;
import org.rulii.context.RuleContext;
import org.rulii.model.UnrulyException;
import org.rulii.ruleflow.RuleFlow;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.rulii.model.action.Actions.action;
import static org.rulii.model.function.Functions.function;

/**
 * The default value of a flow's optional input parameter is bound in the flow's own scope: the
 * flow sees it, the caller never does, and it is evaluated again on every run.
 */
public class RuleFlowInputDefaultsTest {

    public RuleFlowInputDefaultsTest() {
        super();
    }

    @Test
    public void defaultIsSeenByTheFlowAndDoesNotOutliveTheRun() {
        RuleFlow<Integer> flow = RuleFlow.builder()
                .name("discountFlow")
                .param("discount", Integer.class, function(() -> 5))
                .returning(function((Integer discount) -> discount))
                .build();
        Bindings bindings = Bindings.builder().standard();
        RuleContext ruleContext = RuleContext.builder().build(bindings);
        int scopes = ruleContext.getBindings().getScopeSize();

        assertEquals(Integer.valueOf(5), flow.run(ruleContext));
        assertFalse(ruleContext.getBindings().contains("discount"));
        assertFalse(bindings.contains("discount"));
        assertEquals(scopes, ruleContext.getBindings().getScopeSize());
    }

    @Test
    public void defaultIsEvaluatedOnEveryRunInTheSameContext() {
        AtomicInteger calls = new AtomicInteger();
        RuleFlow<Integer> flow = RuleFlow.builder()
                .name("counterFlow")
                .param("ticket", Integer.class, function(() -> calls.incrementAndGet()))
                .returning(function((Integer ticket) -> ticket))
                .build();
        RuleContext ruleContext = RuleContext.builder().build();

        assertEquals(Integer.valueOf(1), flow.run(ruleContext));
        assertEquals(Integer.valueOf(2), flow.run(ruleContext));
        assertEquals(2, calls.get());
    }

    @Test
    public void nullBoundByTheCallerGetsTheDefaultInsideAndStaysNullOutside() {
        RuleFlow<Integer> flow = RuleFlow.builder()
                .name("nullFlow")
                .param("discount", Integer.class, function(() -> 5))
                .returning(function((Integer discount) -> discount))
                .build();
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("discount", Integer.class, null);
        RuleContext ruleContext = RuleContext.builder().build(bindings);

        assertEquals(Integer.valueOf(5), flow.run(ruleContext));
        assertTrue(bindings.contains("discount"));
        assertNull(bindings.getValue("discount"));
    }

    @Test
    public void suppliedValueWinsOverTheDefault() {
        AtomicInteger calls = new AtomicInteger();
        RuleFlow<Integer> flow = RuleFlow.builder()
                .name("suppliedFlow")
                .param("discount", Integer.class, function(() -> calls.incrementAndGet()))
                .returning(function((Integer discount) -> discount))
                .build();

        assertEquals(Integer.valueOf(15), flow.run(discount -> 15));
        assertEquals(0, calls.get());
    }

    @Test
    public void aDefaultCanReadTheDefaultsDeclaredBeforeIt() {
        RuleFlow<Integer> flow = RuleFlow.builder()
                .name("chainFlow")
                .param("base", Integer.class, function(() -> 2))
                .param("scaled", Integer.class, function((Integer base) -> base * 10))
                .returning(function((Integer scaled) -> scaled))
                .build();

        assertEquals(Integer.valueOf(20), flow.run());
        assertEquals(Integer.valueOf(30), flow.run(base -> 3));
    }

    @Test
    public void failingDefaultPropagatesAndLeavesNoScopeBehind() {
        RuleFlow<?> flow = RuleFlow.builder()
                .name("failingDefaultFlow")
                .param("discount", Integer.class, function((RuleContext c) -> { throw new IllegalStateException("no discount"); }))
                .build();
        RuleContext ruleContext = RuleContext.builder().build();
        int scopes = ruleContext.getBindings().getScopeSize();

        UnrulyException thrown = assertThrows(UnrulyException.class, () -> flow.run(ruleContext));
        assertTrue(thrown.getMessage().startsWith("RuleFlow [failingDefaultFlow] could not evaluate the default value of input parameter [discount]"));
        assertTrue(causedBy(thrown, IllegalStateException.class, "no discount"));
        assertEquals(scopes, ruleContext.getBindings().getScopeSize());
        assertFalse(ruleContext.getBindings().contains("discount"));
    }

    @Test
    public void missingRequiredInputStillFailsBeforeAnyDefaultIsEvaluated() {
        AtomicInteger calls = new AtomicInteger();
        RuleFlow<?> flow = RuleFlow.builder()
                .name("requiredFlow")
                .param("discount", Integer.class, function(() -> calls.incrementAndGet()))
                .param("orderId", String.class)
                .build();

        assertThrows(UnrulyException.class, () -> flow.run());
        assertEquals(0, calls.get());
    }

    @Test
    public void nestedFlowSeesTheOuterFlowsDefault() {
        List<Integer> seen = new ArrayList<>();
        RuleFlow<?> inner = RuleFlow.builder()
                .name("innerFlow")
                .param("discount", Integer.class, function(() -> 99))
                .execute(action((Integer discount) -> seen.add(discount)))
                .build();
        RuleFlow<?> outer = RuleFlow.builder()
                .name("outerFlow")
                .param("discount", Integer.class, function(() -> 5))
                .run(inner)
                .build();
        RuleContext ruleContext = RuleContext.builder().build();

        outer.run(ruleContext);

        assertEquals(List.of(5), seen);
        assertFalse(ruleContext.getBindings().contains("discount"));
    }

    @Test
    public void asyncRunKeepsTheDefaultInsideTheFork() throws Exception {
        RuleFlow<Integer> flow = RuleFlow.builder()
                .name("asyncDefaultFlow")
                .param("discount", Integer.class, function(() -> 5))
                .returning(function((Integer discount) -> discount))
                .build();
        Bindings bindings = Bindings.builder().standard();
        RuleContext ruleContext = RuleContext.builder().build(bindings);

        assertEquals(Integer.valueOf(5), flow.runAsync(ruleContext).get());
        assertFalse(ruleContext.getBindings().contains("discount"));
        assertFalse(bindings.contains("discount"));
    }

    private static boolean causedBy(Throwable thrown, Class<? extends Throwable> type, String message) {
        for (Throwable cause = thrown.getCause(); cause != null; cause = cause.getCause()) {
            if (type.isInstance(cause) && message.equals(cause.getMessage())) return true;
        }

        return false;
    }
}
