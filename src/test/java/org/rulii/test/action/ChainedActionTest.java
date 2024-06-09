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
package org.rulii.test.action;

import org.rulii.bind.Binding;
import org.rulii.bind.Bindings;
import org.rulii.bind.InvalidBindingException;
import org.rulii.context.RuleContext;
import org.rulii.lang.ParameterMismatchException;
import org.rulii.lang.action.Action;
import org.rulii.model.UnrulyException;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

import static org.rulii.lang.action.Actions.action;
import static org.rulii.lang.condition.Conditions.condition;
/**
 * CompositeAction related tests.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class ChainedActionTest {

    public ChainedActionTest() {
        super();
    }

    @Test
    public void test1() {
        Action action = Action.builder().with((Binding<String> arg1) -> {
                    Assert.assertEquals("a", arg1.getValue());
                    arg1.setValue("b");
                }).build()
                .andThen(Action.builder().with((Binding<String> arg1) -> {
                    Assert.assertEquals("b", arg1.getValue());
                    arg1.setValue("c");
                }).build())
                .andThen(Action.builder().with((Binding<String> arg1) -> {
                    Assert.assertEquals("c", arg1.getValue());
                    arg1.setValue("d");
                }).build())
                .andThen(action((Binding<String> arg1) -> {
                    Assert.assertEquals("d", arg1.getValue());
                    arg1.setValue("e");
                }));

        Bindings bindings = Bindings.builder().standard(arg1 -> "a");
        action.run(bindings);
        Assert.assertEquals("e", bindings.getValue("arg1"));
    }

    @Test
    public void test2() {
        Action action = Action.builder().with((Binding<String> arg1) -> {
                    Assert.assertEquals("a", arg1.getValue());
                    arg1.setValue("b");
                }).build()
                .andThen(Action.builder().with((Binding<String> arg1) -> {
                            Assert.assertEquals("b", arg1.getValue());
                            arg1.setValue("c");
                        }).build().andThen(Action.builder().with((Binding<String> arg1) -> {
                            Assert.assertEquals("c", arg1.getValue());
                            arg1.setValue("d");
                        }).build().andThen(action((Binding<String> arg1) -> {
                            Assert.assertEquals("d", arg1.getValue());
                            arg1.setValue("e");
                        })))
                        );

        Bindings bindings = Bindings.builder().standard(arg1 -> "a");
        action.run(bindings);
        Assert.assertEquals("e", bindings.getValue("arg1"));
    }

    @Test
    public void test3() {
        Action action = Action.builder().with((Binding<Integer> arg1) -> {
                    Assert.assertEquals(0, (int) arg1.getValue());
                    arg1.setValue(200);
                }).build()
                .andThen(action((Binding<Integer> arg2) -> arg2.setValue(1)))
                .andThen(action((Binding<Integer> arg3) -> arg3.setValue(1)))
                .andThen(action((Binding<Integer> arg4) -> arg4.setValue(1)));

        Bindings bindings = Bindings.builder().standard(arg1 -> 0, arg2 -> 0, arg3 -> 0, arg4 -> 0);
        action.run(bindings);

        Assert.assertEquals(200, (int) bindings.getValue("arg1"));
        Assert.assertEquals(1, (int) bindings.getValue("arg2"));
        Assert.assertEquals(1, (int) bindings.getValue("arg3"));
        Assert.assertEquals(1, (int) bindings.getValue("arg4"));
    }

    @Test
    public void test4() {
        Action action = action((Binding<Integer> arg1) -> {
            Assert.assertEquals(0, (int) arg1.getValue());
            arg1.setValue(200);
        })
                .andThen(action((Binding<Integer> arg2) -> arg2.setValue(1)))
                .andThen(action((Binding<Integer> arg3) -> arg3.setValue(1)))
                .andThen(action((Binding<Integer> arg4) -> arg4.setValue(1)));

        Bindings bindings = Bindings.builder().standard(arg1 -> 0, arg2 -> 0, arg3 -> 0, arg4 -> 0);
        action.run(bindings);

        Assert.assertEquals(200, (int) bindings.getValue("arg1"));
        Assert.assertEquals(1, (int) bindings.getValue("arg2"));
        Assert.assertEquals(1, (int) bindings.getValue("arg3"));
        Assert.assertEquals(1, (int) bindings.getValue("arg4"));
    }

    @Test
    public void test5() {
        Action action = action((Binding<Integer> arg1) -> {
            Assert.assertEquals(0, (int) arg1.getValue());
            arg1.setValue(200);
        }).andThen(action((Binding<Integer> arg1) -> {
            Assert.assertEquals(200, (int) arg1.getValue());
            arg1.setValue(300);
        }));

        Bindings bindings = Bindings.builder().standard(arg1 -> 0);
        action.run(bindings);

        Assert.assertEquals(300, (int) bindings.getValue("arg1"));
    }

    @Test(expected = UnrulyException.class)
    public void test6() {
        Action action = action((Binding<Integer> arg1) -> {
            arg1.getValue();
        });

        Bindings bindings = Bindings.builder().standard();
        action.run(bindings);
    }

    @Test(expected = UnrulyException.class)
    public void test7() {
        Action action = action((Binding<Integer> arg1) -> {
            arg1.getValue();
        }).andThen(action((Integer arg1) -> Assert.assertEquals(200, (int) arg1)));

        Bindings bindings = Bindings.builder().standard();
        action.run(bindings);
    }

    @Test
    public void test8() {
        Action action = action((Bindings bindings, BigDecimal arg2) -> {
            bindings.bind("arg1", 100);
        }).andBefore(action((RuleContext ruleContext) -> {
            ruleContext.getBindings().bind("arg2", new BigDecimal("100.00"));
        }));

        Bindings bindings = Bindings.builder().standard();
        action.run(bindings);

        Assert.assertEquals(100, (int) bindings.getValue("arg1"));
    }
}
