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
package org.rulii.test.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.bind.BindingException;
import org.rulii.bind.Bindings;
import org.rulii.bind.match.BindingMatchingStrategy;
import org.rulii.bind.match.ParameterMatch;
import org.rulii.bind.match.ParameterResolver;
import org.rulii.context.RuleContext;
import org.rulii.convert.ConverterRegistry;
import org.rulii.model.ArgumentList;
import org.rulii.model.MethodDefinition;
import org.rulii.model.UnrulyException;
import org.rulii.model.action.Action;
import org.rulii.model.condition.Condition;
import org.rulii.util.reflect.ObjectFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Tests for the list the resolved arguments of a method call travel in.
 *
 * @author Max Arulananthan
 */
public class ArgumentListTest {

    public ArgumentListTest() {
        super();
    }

    @Test
    public void valuesAreKeptInOrderAndMayBeNull() {
        List<Object> list = ArgumentList.wrap(new Object[] {"a", null, 3});

        Assertions.assertEquals(3, list.size());
        Assertions.assertEquals("a", list.get(0));
        Assertions.assertNull(list.get(1));
        Assertions.assertEquals(Integer.valueOf(3), list.get(2));
        // It is a list like any other to whoever reads it.
        Assertions.assertEquals(Arrays.asList("a", null, 3), list);
        Assertions.assertEquals(Arrays.asList("a", null, 3).hashCode(), list.hashCode());
        Assertions.assertEquals("[a, null, 3]", list.toString());
        Assertions.assertTrue(list.contains(null));
        Assertions.assertTrue(ArgumentList.wrap(new Object[0]).isEmpty());
    }

    @Test
    public void cannotBeChanged() {
        List<Object> list = ArgumentList.wrap(new Object[] {"a", "b"});

        Assertions.assertThrows(UnsupportedOperationException.class, () -> list.add("c"));
        Assertions.assertThrows(UnsupportedOperationException.class, () -> list.set(0, "x"));
        Assertions.assertThrows(UnsupportedOperationException.class, () -> list.remove(0));
        Assertions.assertThrows(UnsupportedOperationException.class, () -> list.clear());
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            java.util.Iterator<Object> iterator = list.iterator();
            iterator.next();
            iterator.remove();
        });
        Assertions.assertEquals(Arrays.asList("a", "b"), list);
    }

    @Test
    public void toArrayIsACopy() {
        List<Object> list = ArgumentList.wrap(new String[] {"a", "b"});

        Object[] copy = list.toArray();
        Assertions.assertEquals(Object[].class, copy.getClass());
        copy[0] = "changed";
        Assertions.assertEquals("a", list.get(0));
    }

    @Test
    public void resolverHandsOutItsValuesInAnArgumentList() {
        Condition condition = Condition.builder().with((Integer a, String b) -> a > 0).build();
        RuleContext ruleContext = RuleContext.builder().build(Bindings.builder().standard(a -> 5, b -> "x"));

        List<ParameterMatch> matches = ruleContext.getParameterResolver().match(condition.getDefinition(),
                ruleContext.getBindings(), ruleContext.getMatchingStrategy(), ruleContext.getObjectFactory());
        List<Object> values = ruleContext.getParameterResolver().resolve(matches, condition.getDefinition(),
                ruleContext.getBindings(), ruleContext.getMatchingStrategy(), ruleContext.getConverterRegistry(),
                ruleContext.getObjectFactory());

        Assertions.assertTrue(values instanceof ArgumentList);
        Assertions.assertEquals(Arrays.asList(5, "x"), values);
        Assertions.assertThrows(UnsupportedOperationException.class, () -> values.set(0, 1));
    }

    @Test
    public void argumentsFromAnyOtherListStillReachTheMethod() {
        // A resolver of one's own may hand out any kind of list.
        ParameterResolver standard = ParameterResolver.builder().build();
        ParameterResolver plainLists = new ParameterResolver() {
            @Override
            public List<ParameterMatch> match(MethodDefinition definition, Bindings bindings,
                                              BindingMatchingStrategy matchingStrategy,
                                              ObjectFactory objectFactory) throws BindingException {
                return standard.match(definition, bindings, matchingStrategy, objectFactory);
            }

            @Override
            public List<Object> resolve(List<ParameterMatch> matches, MethodDefinition definition, Bindings bindings,
                                        BindingMatchingStrategy matchingStrategy, ConverterRegistry registry,
                                        ObjectFactory objectFactory) throws BindingException {
                return new ArrayList<>(standard.resolve(matches, definition, bindings, matchingStrategy, registry, objectFactory));
            }

            @Override
            public boolean isAutoConvert() {
                return standard.isAutoConvert();
            }
        };

        RuleContext ruleContext = RuleContext.builder()
                .with(Bindings.builder().standard(a -> 5, b -> "x"))
                .paramResolver(plainLists)
                .build();

        Condition condition = Condition.builder().with((Integer a, String b) -> a == 5 && "x".equals(b)).build();
        Assertions.assertTrue(condition.isTrue(ruleContext));

        List<Object> seen = new ArrayList<>();
        Action action = Action.builder().with((Integer a, String b) -> { seen.add(a); seen.add(b); }).build();
        action.run(ruleContext);
        Assertions.assertEquals(Arrays.asList(5, "x"), seen);
    }

    @Test
    public void conditionStillGetsReadOnlyArgumentsAndActionLiveOnes() {
        RuleContext ruleContext = RuleContext.builder().build(Bindings.builder().standard(a -> 5));

        // The condition is handed a read-only copy of its Bindings argument...
        Condition condition = Condition.builder().with((Bindings bindings, Integer a) -> {
            bindings.bind("fromCondition", a);
            return true;
        }).build();
        Assertions.assertThrows(UnrulyException.class, () -> condition.isTrue(ruleContext));
        Assertions.assertFalse(ruleContext.getBindings().contains("fromCondition"));

        // ...the action the live one.
        Action action = Action.builder().with((Bindings bindings, Integer a) -> { bindings.bind("fromAction", a); }).build();
        action.run(ruleContext);
        Assertions.assertEquals(Integer.valueOf(5), ruleContext.getBindings().getValue("fromAction"));
    }
}
