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
package org.rulii.test.context;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.bind.Binding;
import org.rulii.bind.Bindings;
import org.rulii.bind.ReservedBindings;
import org.rulii.bind.ScopedBindings;
import org.rulii.context.RuleContext;
import org.rulii.rule.Rule;

import java.util.ArrayList;
import java.util.List;

import static org.rulii.model.action.Actions.action;

/**
 * {@link RuleContext#fork()}: a new context over forked bindings whose reserved pair resolves to the fork.
 *
 * @author Max Arulananthan
 * @since 2.2
 */
public class RuleContextForkTest {

    public RuleContextForkTest() {
        super();
    }

    @Test
    public void forkIsANewContextWithTheSameServices() {
        RuleContext parent = RuleContext.builder().build(a -> 1);
        parent.getBindings().addScope("flow");
        parent.getBindings().bind("b", 2);

        RuleContext fork = parent.fork();

        Assertions.assertNotEquals(parent.getId(), fork.getId());
        Assertions.assertNotSame(parent.getBindings(), fork.getBindings());
        Assertions.assertSame(parent.getMatchingStrategy(), fork.getMatchingStrategy());
        Assertions.assertSame(parent.getParameterResolver(), fork.getParameterResolver());
        Assertions.assertSame(parent.getMessageResolver(), fork.getMessageResolver());
        Assertions.assertSame(parent.getMessageFormatter(), fork.getMessageFormatter());
        Assertions.assertSame(parent.getObjectFactory(), fork.getObjectFactory());
        Assertions.assertSame(parent.getTracer(), fork.getTracer());
        Assertions.assertSame(parent.getConverterRegistry(), fork.getConverterRegistry());
        Assertions.assertSame(parent.getClock(), fork.getClock());
        Assertions.assertSame(parent.getExecutorService(), fork.getExecutorService());
        Assertions.assertSame(parent.getLocale(), fork.getLocale());
        Assertions.assertEquals(parent.getRuleRegistry(), fork.getRuleRegistry());

        Assertions.assertEquals(1, (Integer) fork.getBindings().getValue("a"));
        Assertions.assertEquals(2, (Integer) fork.getBindings().getValue("b"));
        Assertions.assertEquals(parent.getBindings().getScopeSize() + 1, fork.getBindings().getScopeSize());
    }

    @Test
    public void reservedPairResolvesToTheForkInsideTheFork() {
        RuleContext parent = RuleContext.builder().build(a -> 1);
        RuleContext fork = parent.fork();
        ScopedBindings forked = fork.getBindings();

        // by name
        Assertions.assertSame(fork, forked.getValue(ReservedBindings.RULE_CONTEXT.getName()));
        Assertions.assertSame(forked, forked.getValue(ReservedBindings.BINDINGS.getName()));
        // by type: the lookup stops at the fork scope, so there is exactly one candidate
        List<Binding<RuleContext>> contexts = forked.getBindings(RuleContext.class);
        Assertions.assertEquals(1, contexts.size());
        Assertions.assertSame(fork, contexts.get(0).getValue());
        List<Binding<Bindings>> bindings = forked.getBindings(Bindings.class);
        Assertions.assertEquals(1, bindings.size());
        Assertions.assertSame(forked, bindings.get(0).getValue());
        // the parent's pair is not inherited: across all the fork's scopes there is exactly one of each
        Assertions.assertEquals(1, forked.getAllBindings(RuleContext.class).size());
        Assertions.assertEquals(1, forked.getAllBindings(Bindings.class).size());
        Assertions.assertEquals(1, forked.getAllBindings(ReservedBindings.RULE_CONTEXT.getName()).size());
        Assertions.assertNotSame(parent.getBindings().getRootScope(), forked.getRootScope(), "the root was substituted");
        Assertions.assertEquals(parent.getBindings().getRootScope().getName(), forked.getRootScope().getName());
        // and the parent still has its own
        Assertions.assertSame(parent, parent.getBindings().getValue(ReservedBindings.RULE_CONTEXT.getName()));
        Assertions.assertSame(parent.getBindings(), parent.getBindings().getValue(ReservedBindings.BINDINGS.getName()));
        // the fork's pair is final, like the root's
        Assertions.assertTrue(forked.getBinding(ReservedBindings.RULE_CONTEXT.getName()).isFinal());
    }

    @Test
    public void ruleRunInTheForkReceivesTheFork() {
        RuleContext parent = RuleContext.builder().build(a -> 1);
        RuleContext fork = parent.fork();
        List<RuleContext> contexts = new ArrayList<>();
        List<Bindings> bindings = new ArrayList<>();

        Rule rule = Rule.builder().name("forkReader")
                .then(action((RuleContext ruleContext, Bindings bindings1) -> {
                    contexts.add(ruleContext);
                    bindings.add(bindings1);
                }))
                .build();

        rule.run(fork);

        Assertions.assertEquals(1, contexts.size());
        Assertions.assertSame(fork, contexts.get(0));
        Assertions.assertSame(fork.getBindings(), bindings.get(0));
    }

    @Test
    public void valuesAreSharedAndStructureIsNot() {
        RuleContext parent = RuleContext.builder().build(a -> 1);
        RuleContext fork = parent.fork();

        fork.getBindings().setValue("a", 5);
        Assertions.assertEquals(5, (Integer) parent.getBindings().getValue("a"));

        fork.getBindings().bind("x", "y");
        fork.getBindings().addScope("task");
        Assertions.assertFalse(parent.getBindings().contains("x"));
        Assertions.assertNull(parent.getBindings().getScope("task"));
    }

    @Test
    public void forkOfAForkHasOnePairToo() {
        RuleContext parent = RuleContext.builder().build(a -> 1);
        RuleContext fork = parent.fork();
        fork.getBindings().bind("x", "y");
        RuleContext grandChild = fork.fork();
        ScopedBindings bindings = grandChild.getBindings();

        // the first fork's own scope held its pair beside "x": the pair is gone, "x" is still shared
        Assertions.assertEquals(1, bindings.getAllBindings(RuleContext.class).size());
        Assertions.assertSame(grandChild, bindings.getValue(ReservedBindings.RULE_CONTEXT.getName()));
        Assertions.assertSame(bindings, bindings.getValue(ReservedBindings.BINDINGS.getName()));
        Assertions.assertEquals("y", bindings.getValue("x"));
        bindings.setValue("x", "z");
        Assertions.assertEquals("z", fork.getBindings().getValue("x"));
        Assertions.assertSame(fork, fork.getBindings().getValue(ReservedBindings.RULE_CONTEXT.getName()));

        List<RuleContext> contexts = new ArrayList<>();
        Rule.builder().name("grandChildReader").then(action((RuleContext ruleContext) -> contexts.add(ruleContext))).build().run(grandChild);
        Assertions.assertEquals(List.of(grandChild), contexts);
    }

    @Test
    public void immutableContextCannotFork() {
        RuleContext parent = RuleContext.builder().build(a -> 1);
        Assertions.assertThrows(UnsupportedOperationException.class, () -> parent.asImmutable().fork());
    }
}
