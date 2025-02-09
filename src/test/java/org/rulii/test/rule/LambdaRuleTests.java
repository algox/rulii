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
package org.rulii.test.rule;

import org.junit.jupiter.api.Test;
import org.rulii.bind.Binding;
import org.rulii.bind.Bindings;
import org.rulii.bind.match.MatchByTypeMatchingStrategy;
import org.rulii.context.RuleContext;
import org.rulii.model.UnrulyException;
import org.rulii.model.condition.Condition;
import org.rulii.model.condition.Conditions;
import org.rulii.rule.Rule;
import org.rulii.rule.RuleExecutionStatus;
import org.rulii.rule.RuleResult;
import org.rulii.util.TypeReference;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.rulii.model.action.Actions.action;
import static org.rulii.model.condition.Conditions.condition;

/**
 * RuleTests class for testing Lambda Rule functionality.
 * Contains multiple test methods for different scenarios.
 *
 * @author Max Arulananthan
 * @since 1.0
 *
 */
public class LambdaRuleTests {

    public LambdaRuleTests() {
        super();
    }

    @Test
    public void test1() {
        Rule rule = Rule.builder().name("TestRule1")
                .preCondition(condition(() -> true))
                .build();
        assertNotNull(rule);
        assertNotNull(rule.getPreCondition());
        assertNotNull(rule.getCondition());
        assertEquals(rule.getCondition(), Conditions.TRUE());
        assertTrue(rule.getPreCondition().isTrue());
        assertTrue(rule.getCondition().isTrue());
    }

    @Test
    public void test2() {
        Rule rule = Rule.builder().name("TestRule2")
                .preCondition(condition(() -> false))
                .build();
        assertNotNull(rule);
        assertNotNull(rule.getPreCondition());
        assertNotNull(rule.getCondition());
        assertEquals(rule.getCondition(), Conditions.TRUE());
        assertFalse(rule.getPreCondition().isTrue());
        assertTrue(rule.getCondition().isTrue());
    }

    @Test
    public void test3() {
        Rule rule = Rule.builder().name("TestRule3")
                .preCondition(condition((String name, List<Integer> values) -> {
                    assertNotNull(name);
                    assertNotNull(values);
                    assertFalse(values.isEmpty());
                    return true;
                }))
                .given(condition((List<Integer> values) -> {
                    assertFalse(values.isEmpty());
                    return true;
                }))
                .build();
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("name", "value");
        bindings.bind("values", List.of(1,2,3));
        rule.run(bindings);
    }

    @Test
    public void test4() {
        Rule rule = Rule.builder().name("TestRule4")
                .preCondition(condition((Optional<String> name, Optional<List<Integer>> values, String someValue, Integer someNumber) -> {
                    assertTrue(name.isPresent());
                    assertTrue(values.isPresent());
                    assertFalse(values.get().isEmpty());
                    return false;
                }))
                .build();
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("name", "value");
        bindings.bind("values", List.of(1,2,3));
        RuleResult result = rule.run(bindings);
        assertEquals(result.status(), RuleExecutionStatus.SKIPPED);
    }

    @Test
    public void test5() {
        Rule rule = Rule.builder().name("TestRule5")
                .preCondition(condition((Optional<String> name, Optional<List<Integer>> values, String someValue, Integer someNumber) -> {
                    assertFalse(name.isPresent());
                    assertFalse(values.isPresent());
                    assertNull(someValue);
                    assertNull(someNumber);
                    return true;
                }))
                .build();
        Bindings bindings = Bindings.builder().standard();
        RuleResult result = rule.run(bindings);
        assertEquals(result.status(), RuleExecutionStatus.PASS);
    }

    @Test
    public void test6() {
        Condition c = Condition.builder().with((String someValue, Integer someNumber) -> {
            assertEquals("xxx", someValue);
            assertEquals(123, someNumber);
            return true;
        })
                .param(0).defaultValueText("xxx").build()
                .param(1).defaultValueText("123").build()
                .build();
        Rule rule = Rule.builder().name("TestRule6")
                .preCondition(c)
                .build();
        Bindings bindings = Bindings.builder().standard();
        RuleResult result = rule.run(bindings);
        assertEquals(result.status(), RuleExecutionStatus.PASS);
    }

    @Test
    public void test7() {
        Condition c = Condition.builder().with((String someValue, Integer someNumber) -> {
                    assertEquals("xxx", someValue);
                    assertEquals(123, someNumber);
                    return true;
                })
                .param(0).name("text").build()
                .param(1).name("num").build()
                .build();
        Rule rule = Rule.builder().name("TestRule7")
                .preCondition(c)
                .build();
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("text", "xxx");
        bindings.bind("num", 123);
        bindings.bind("someValue", "yyy");
        bindings.bind("someNumber", 321);
        RuleResult result = rule.run(bindings);
        assertEquals(result.status(), RuleExecutionStatus.PASS);
    }

    @Test
    public void test8() {
        Condition c = Condition.builder().with((String someValue) -> {
                    assertEquals("xxx", someValue);
                    return true;
                })
                .param(0).matchUsing(MatchByTypeMatchingStrategy.class).build()
                .build();
        Rule rule = Rule.builder().name("TestRule8")
                .preCondition(c)
                .build();

        Bindings bindings = Bindings.builder().standard();
        bindings.bind("text", "xxx");
        bindings.bind("someText", "yyy");
        bindings.bind("num", 123);
        assertThrowsExactly(UnrulyException.class, () -> {
            rule.run(bindings);
        });
    }

    @Test
    public void test9() {
        Condition c = Condition.builder().with((Binding<String> someValue) -> {
                    assertNotNull(someValue);
                    assertEquals("xxx", someValue.getValue());
                    return true;
                })
                .param(0).type(new TypeReference<Binding<String>>() {}).build()
                .build();

        Rule rule = Rule.builder().name("TestRule9")
                .preCondition(c)
                .build();
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("text", "xxx");
        RuleResult result = rule.run(bindings);
        assertEquals(result.status(), RuleExecutionStatus.PASS);
    }

    @Test
    public void test10() {
        Condition c = Condition.builder().with((Binding<String> someValue) -> {
                    assertNotNull(someValue);
                    assertEquals("xxx", someValue.getValue());
                    return true;
                })
                .param(0).type(new TypeReference<Binding<String>>() {}).build()
                .build();

        Rule rule = Rule.builder().name("TestRule10")
                .preCondition(c)
                .build();
        RuleResult result = rule.run(text -> "xxx");
        assertEquals(result.status(), RuleExecutionStatus.PASS);
    }

    @Test
    public void test11() {
        Condition c = Condition.builder().with((Binding<String> someValue) -> {
                    assertNotNull(someValue);
                    assertEquals("xxx", someValue.getValue());
                    return true;
                })
                .param(0).type(new TypeReference<Binding<String>>() {}).build()
                .build();

        Rule rule = Rule.builder().name("TestRule11")
                .preCondition(c)
                .build();
        RuleContext context = RuleContext.builder().build(text -> "xxx");
        RuleResult result = rule.run(context);
        assertEquals(result.status(), RuleExecutionStatus.PASS);
    }

    @Test
    public void test12() {
        Condition c = Condition.builder().with((Binding<String> someValue) -> {
                    assertNotNull(someValue);
                    assertEquals("xxx", someValue.getValue());
                    someValue.setValue("yyy");
                    return true;
                })
                .param(0).type(new TypeReference<Binding<String>>() {}).build()
                .build();

        Rule rule = Rule.builder().name("TestRule12")
                .preCondition(c)
                .build();
        RuleContext context = RuleContext.builder().build(text -> "xxx");
        assertThrowsExactly(UnrulyException.class, () -> {
            rule.run(context);
        });
    }

    @Test
    public void test13() {
        Condition cond = Condition.builder().with((Binding<String> a, String b, Binding<Integer> c) -> {
                    assertNull(a);
                    assertNull(b);
                    assertNull(c);
                    return true;
                })
                .param(0).type(new TypeReference<Binding<String>>() {}).build()
                .build();

        Rule rule = Rule.builder().name("TestRule13")
                .preCondition(cond)
                .build();
        RuleResult result = rule.run();
        assertEquals(result.status(), RuleExecutionStatus.PASS);
    }

    @Test
    public void test14() {
        Rule rule = Rule.builder().name("TestRule2")
                .preCondition(condition(() -> false))
                .build();
        RuleResult result = rule.run();
        assertEquals(result.status(), RuleExecutionStatus.SKIPPED);
    }

    @Test
    public void test15() {
        Rule rule = Rule.builder().name("TestRule15")
                .preCondition(condition((Boolean flag) -> flag))
                .build();
        RuleResult result = rule.run(flag -> true);
        assertEquals(result.status(), RuleExecutionStatus.PASS);
    }

    @Test
    public void test16() {
        Rule rule = Rule.builder().name("TestRule16")
                .preCondition(condition((Boolean flag) -> flag))
                .build();
        RuleResult result = rule.run(flag -> false);
        assertEquals(result.status(), RuleExecutionStatus.SKIPPED);
    }

    @Test
    public void test17() {
        Rule rule = Rule.builder().name("TestRule17")
                .preCondition(condition((Boolean flag) -> {
                    throw new IllegalArgumentException();
                }))
                .build();
        assertThrowsExactly(UnrulyException.class, () -> {
            rule.run(flag -> false);
        });
    }

    @Test
    public void test19() {
        Rule rule = Rule.builder().name("TestRule19")
                .given(condition(() -> true))
                .build();
        assertNull(rule.getPreCondition());
        assertNotNull(rule.getCondition());
        RuleResult result = rule.run();
        assertEquals(result.status(), RuleExecutionStatus.PASS);
    }

    @Test
    public void test20() {
        Rule rule = Rule.builder().name("TestRule20")
                .preCondition(condition((Boolean flag) -> flag))
                .given(condition(() -> true))
                .build();
        RuleResult result = rule.run(flag -> true);
        assertEquals(result.status(), RuleExecutionStatus.PASS);
        result = rule.run(flag -> false);
        assertEquals(result.status(), RuleExecutionStatus.SKIPPED);
    }

    @Test
    public void test21() {
        Rule rule = Rule.builder().name("TestRule21")
                .preCondition(condition((Boolean flag) -> {
                    throw new IllegalArgumentException();
                }))
                .given(condition(() -> true))
                .build();
        assertThrowsExactly(UnrulyException.class, () -> {
            rule.run(flag -> true);
        });
    }

    @Test
    public void test22() {
        Rule rule = Rule.builder().name("TestRule22")
                .given(condition((String someText, Map<String, List<Integer>> someMap) -> {
                    assertNotNull(someText);
                    assertNotNull(someMap);
                    return true;
                }))
                .build();
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("someText", "abc");
        bindings.bind("someMap", new HashMap<>());
        RuleResult result = rule.run(bindings);
        assertEquals(result.status(), RuleExecutionStatus.PASS);
    }

    @Test
    public void test23() {
        Rule rule = Rule.builder().name("TestRule23")
                .given(condition((String someText, Map<String, List<Integer>> someMap) -> {
                    assertNotNull(someText);
                    assertNotNull(someMap);
                    return true;
                }))
                .build();
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("someText", "abc");
        bindings.bind("map", new TypeReference<Map<String, List<Integer>>>(){}.getType(), new HashMap<>());
        RuleResult result = rule.run(bindings);
        assertEquals(result.status(), RuleExecutionStatus.PASS);
    }

    @Test
    public void test24() {
        Rule rule = Rule.builder().name("TestRule24")
                .given(condition((String someText, Map<String, List<Integer>> someMap) -> {
                    assertNotNull(someText);
                    assertNotNull(someMap);
                    return true;
                }))
                .build();
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("someText", "abc");
        bindings.bind("someMap", new ArrayList<>());
        assertThrowsExactly(UnrulyException.class, () -> {
            rule.run(bindings);
        });
    }

    @Test
    public void test25() {
        Condition cond = Condition.builder().with((Float someFloat, Optional<Map<String, List<Integer>>> someMap, Binding<Float> bindingFloat) -> {
                    assertEquals(120.00f, someFloat);
                    assertNotNull(someMap);
                    assertNotNull(bindingFloat);
                    assertTrue(someMap.isPresent());
                     assertEquals(120.00f, bindingFloat.getValue());
                    return true;
                })
                .param(2).type(new TypeReference<Binding<Float>>() {}).build()
                .build();

        Rule rule = Rule.builder().name("TestRule25")
                .given(cond)
                .build();
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("someFloat", 120.00f);
        bindings.bind("someMap", new HashMap<String, List<Integer>>());
        RuleResult result = rule.run(bindings);
        assertEquals(result.status(), RuleExecutionStatus.PASS);
    }

    @Test
    public void test26() {
        Rule rule = Rule.builder().name("TestRule26")
                .preCondition(condition((RuleContext ruleContext, Bindings ruleBindings) -> {
                    assertNotNull(ruleBindings);
                    assertNotNull(ruleContext);
                    return true;
                }))
                .given(condition((RuleContext ruleContext, Bindings ruleBindings) -> {
                    assertNotNull(ruleContext);
                    assertNotNull(ruleBindings);
                    return true;
                }))
                .build();
        Bindings bindings = Bindings.builder().standard();
        RuleResult result = rule.run(bindings);
        assertEquals(result.status(), RuleExecutionStatus.PASS);
    }

    @Test
    public void test27() {
        Rule rule = Rule.builder().name("TestRule27")
                .preCondition(condition((Bindings bindings) -> {
                    bindings.bind("a", Integer.class);
                    return true;
                }))
                .build();
        Bindings bindings = Bindings.builder().standard();
        assertThrowsExactly(UnrulyException.class, () -> {
            rule.run(bindings);
        });
    }

    @Test
    public void test28() {
        Rule rule = Rule.builder().name("TestRule28")
                .preCondition(condition((RuleContext ctx) -> {
                    ctx.getBindings().bind("a", Integer.class);
                    return true;
                }))
                .build();
        Bindings bindings = Bindings.builder().standard();
        assertThrowsExactly(UnrulyException.class, () -> {
            rule.run(bindings);
        });
    }

    @Test
    public void test29() {
        Rule rule = Rule.builder().name("TestRule29")
                .given(condition((Bindings bindings) -> {
                    bindings.bind("a", Integer.class);
                    return true;
                }))
                .build();
        Bindings bindings = Bindings.builder().standard();
        assertThrowsExactly(UnrulyException.class, () -> {
            rule.run(bindings);
        });
    }

    @Test
    public void test30() {
        Rule rule = Rule.builder().name("TestRule30")
                .given(condition((RuleContext ctx) -> {
                    ctx.getBindings().bind("a", Integer.class);
                    return true;
                }))
                .build();
        Bindings bindings = Bindings.builder().standard();
        assertThrowsExactly(UnrulyException.class, () -> {
            rule.run(bindings);
        });
    }

    @Test
    public void test31() {
        Rule rule = Rule.builder().name("TestRule31")
                .preCondition(condition(() -> true))
                .given(condition(() -> true))
                .then(action((Binding<Boolean> flag) -> {
                    assertNotNull(flag);
                    flag.setValue(true);
                }))
                .build();
        Bindings bindings = Bindings.builder().standard();
        Binding<Boolean> flag = bindings.bind("flag", false);
        RuleResult result = rule.run(bindings);
        assertEquals(result.status(), RuleExecutionStatus.PASS);
        assertEquals(true, flag.getValue());
    }

    @Test
    public void test32() {
        Rule rule = Rule.builder().name("TestRule32")
                .then(action((Binding<Boolean> flag) -> {
                    assertNotNull(flag);
                    flag.setValue(true);
                }))
                .build();
        Bindings bindings = Bindings.builder().standard();
        Binding<Boolean> flag = bindings.bind("flag", false);
        RuleResult result = rule.run(bindings);
        assertEquals(result.status(), RuleExecutionStatus.PASS);
        assertEquals(true, flag.getValue());
    }

    @Test
    public void test33() {
        Rule rule = Rule.builder().name("TestRule33")
                .given(condition((Boolean flag) -> flag))
                .then(action((Bindings bindings) -> {
                    assertNotNull(bindings);
                    bindings.bind("someValue", 100);
                }))
                .build();
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("flag", true);
        RuleResult result = rule.run(bindings);
        assertEquals(result.status(), RuleExecutionStatus.PASS);
        assertEquals(100, (int) bindings.getValue("someValue"));
    }

    @Test
    public void test34() {
        Rule rule = Rule.builder().name("TestRule34")
                .given(condition((Boolean flag) -> flag))
                .then(action((Bindings bindings) -> {
                    assertNotNull(bindings);
                    bindings.bind("someValue", 100);
                }))
                .build();
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("flag", false);
        RuleResult result = rule.run(bindings);
        assertEquals(result.status(), RuleExecutionStatus.FAIL);
        assertFalse(bindings.contains("someValue"));
    }

    @Test
    public void test35() {
        Rule rule = Rule.builder().name("TestRule35")
                .given(condition((Boolean flag) -> flag))
                .then(action((Binding<Integer> someValue) -> {
                    assertNotNull(someValue);
                    someValue.setValue(someValue.getValue() + 100);
                }))
                .then(action((Binding<Integer> someValue) -> {
                    assertNotNull(someValue);
                    someValue.setValue(someValue.getValue() + 100);
                }))
                .build();
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("flag", true);
        bindings.bind("someValue", 0);
        RuleResult result = rule.run(bindings);
        assertEquals(result.status(), RuleExecutionStatus.PASS);
        assertEquals(200, (int) bindings.getValue("someValue"));
    }

    @Test
    public void test36() {
        Rule rule = Rule.builder().name("TestRule36")
                .given(condition((Boolean flag) -> flag))
                .then(action((Binding<String> someValue) -> {
                    assertNotNull(someValue);
                    someValue.setValue(someValue.getValue() + "a");
                }))
                .then(action((Binding<String> someValue) -> {
                    assertNotNull(someValue);
                    someValue.setValue(someValue.getValue() + "b");
                }))
                .then(action((Binding<String> someValue) -> {
                    assertNotNull(someValue);
                    someValue.setValue(someValue.getValue() + "c");
                }))
                .build();
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("flag", true);
        bindings.bind("someValue", "");
        RuleResult result = rule.run(bindings);
        assertEquals(result.status(), RuleExecutionStatus.PASS);
        assertEquals("abc", bindings.getValue("someValue"));
    }

    @Test
    public void test38() {
        Rule rule = Rule.builder().name("TestRule38")
                .given(condition((Boolean flag) -> flag))
                .then(action(() -> {
                    throw new IllegalArgumentException();
                })).build();
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("flag", true);
        assertThrowsExactly(UnrulyException.class, () -> {
            rule.run(bindings);
        });
    }

    @Test
    public void test41() {
        Rule rule = Rule.builder().name("TestRule41")
                .given(condition((Boolean flag) -> flag))
                .then(action((Binding<String> someValue) -> {
                    assertNotNull(someValue);
                    someValue.setValue(someValue.getValue() + "zzz");
                }))
                .otherwise(action((Binding<String> someValue) -> {
                    assertNotNull(someValue);
                    someValue.setValue(someValue.getValue() + "yyy");
                })).build();
        assertNotNull(rule.getOtherwiseAction());
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("flag", true);
        bindings.bind("someValue", "");
        RuleResult result = rule.run(bindings);
        assertEquals(result.status(), RuleExecutionStatus.PASS);
        assertEquals("zzz", bindings.getValue("someValue"));
    }

    @Test
    public void test42() {
        Rule rule = Rule.builder().name("TestRule41")
                .given(condition((Boolean flag) -> flag))
                .then(action((Binding<String> someValue) -> {
                    assertNotNull(someValue);
                    someValue.setValue(someValue.getValue() + "zzz");
                }))
                .otherwise(action((Binding<String> someValue) -> {
                    assertNotNull(someValue);
                    someValue.setValue(someValue.getValue() + "yyy");
                })).build();
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("flag", false);
        bindings.bind("someValue", "");
        RuleResult result = rule.run(bindings);
        assertEquals(result.status(), RuleExecutionStatus.FAIL);
        assertEquals("yyy", bindings.getValue("someValue"));
    }

    @Test
    public void test44() {
        Rule rule = Rule.builder().name("TestRule44")
                .given(condition((Boolean flag) -> flag))
                .then(action((Bindings bindings) -> {
                    assertNotNull(bindings);
                    bindings.bind("newBinding", 321);
                }))
                .otherwise(action((RuleContext ctx, Bindings bindings) -> {
                    assertNotNull(ctx);
                    assertNotNull(bindings);
                    bindings.bind("newBinding", 123);
                })).build();
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("flag", false);
        RuleResult result = rule.run(bindings);
        assertEquals(result.status(), RuleExecutionStatus.FAIL);
        assertEquals(123, (int) bindings.getValue("newBinding"));
    }

    @Test
    public void test45() {
        Rule rule = Rule.builder().name("TestRule44")
                .given(condition((Boolean flag) -> flag))
                .then(action((Bindings bindings) -> {
                    assertNotNull(bindings);
                    bindings.bind("newBinding", 321);
                }))
                .otherwise(action((RuleContext ctx, Bindings bindings) -> {
                    assertNotNull(ctx);
                    assertNotNull(bindings);
                    bindings.bind("newBinding", 123);
                })).build();
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("flag", true);
        RuleResult result = rule.run(bindings);
        assertEquals(result.status(), RuleExecutionStatus.PASS);
        assertEquals(321, (int) bindings.getValue("newBinding"));
    }
}
