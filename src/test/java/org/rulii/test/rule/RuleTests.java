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
import org.rulii.context.RuleContext;
import org.rulii.model.UnrulyException;
import org.rulii.model.condition.Conditions;
import org.rulii.rule.Rule;
import org.rulii.rule.RuleExecutionStatus;
import org.rulii.rule.RuleResult;
import org.rulii.util.TypeReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.rulii.test.rule.TestRules.*;

/**
 * RuleTests class for testing Rule functionality.
 * Contains multiple test methods for different scenarios.
 *
 * @author Max Arulananthan
 * @since 1.0
 *
 */
public class RuleTests {

    public RuleTests() {
        super();
    }

    @Test
    public void test1() {
        Rule rule = Rule.builder().build(TestRule1.class);
        assertNotNull(rule);
        assertNotNull(rule.getPreCondition());
        assertNotNull(rule.getCondition());
        assertEquals(rule.getCondition(), Conditions.TRUE());
        assertTrue(rule.getPreCondition().isTrue());
        assertTrue(rule.getCondition().isTrue());
    }

    @Test
    public void test2() {
        Rule rule = Rule.builder().build(TestRule2.class);
        assertNotNull(rule);
        assertNotNull(rule.getPreCondition());
        assertNotNull(rule.getCondition());
        assertEquals(rule.getCondition(), Conditions.TRUE());
        assertFalse(rule.getPreCondition().isTrue());
        assertTrue(rule.getCondition().isTrue());
    }

    @Test
    public void test3() {
        Rule rule = Rule.builder().build(TestRule3.class);
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("name", "value");
        bindings.bind("values", List.of(1,2,3));
        rule.run(bindings);
    }

    @Test
    public void test4() {
        Rule rule = Rule.builder().build(TestRule4.class);
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("name", "value");
        bindings.bind("values", List.of(1,2,3));
        RuleResult result = rule.run(bindings);
        assertEquals(result.status(), RuleExecutionStatus.SKIPPED);
    }

    @Test
    public void test5() {
        Rule rule = Rule.builder().build(TestRule5.class);
        Bindings bindings = Bindings.builder().standard();
        RuleResult result = rule.run(bindings);
        assertEquals(result.status(), RuleExecutionStatus.PASS);
    }

    @Test
    public void test6() {
        Rule rule = Rule.builder().build(TestRule6.class);
        Bindings bindings = Bindings.builder().standard();
        RuleResult result = rule.run(bindings);
        assertEquals(result.status(), RuleExecutionStatus.PASS);
    }

    @Test
    public void test7() {
        Rule rule = Rule.builder().build(TestRule7.class);
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
        Rule rule = Rule.builder().build(TestRule8.class);
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
        Rule rule = Rule.builder().build(TestRule9.class);
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("text", "xxx");
        RuleResult result = rule.run(bindings);
        assertEquals(result.status(), RuleExecutionStatus.PASS);
    }

    @Test
    public void test10() {
        Rule rule = Rule.builder().build(TestRule9.class);
        RuleResult result = rule.run(text -> "xxx");
        assertEquals(result.status(), RuleExecutionStatus.PASS);
    }

    @Test
    public void test11() {
        Rule rule = Rule.builder().build(TestRule9.class);
        RuleContext context = RuleContext.builder().build(text -> "xxx");
        RuleResult result = rule.run(context);
        assertEquals(result.status(), RuleExecutionStatus.PASS);
    }

    @Test
    public void test12() {
        Rule rule = Rule.builder().build(TestRule10.class);
        RuleContext context = RuleContext.builder().build(text -> "xxx");
        assertThrowsExactly(UnrulyException.class, () -> {
            rule.run(context);
        });
    }

    @Test
    public void test13() {
        Rule rule = Rule.builder().build(TestRule11.class);
        RuleResult result = rule.run();
        assertEquals(result.status(), RuleExecutionStatus.PASS);
    }

    @Test
    public void test14() {
        Rule rule = Rule.builder().build(TestRule2.class);
        RuleResult result = rule.run();
        assertEquals(result.status(), RuleExecutionStatus.SKIPPED);
    }

    @Test
    public void test15() {
        Rule rule = Rule.builder().build(TestRule12.class);
        RuleResult result = rule.run(flag -> true);
        assertEquals(result.status(), RuleExecutionStatus.PASS);
    }

    @Test
    public void test16() {
        Rule rule = Rule.builder().build(TestRule12.class);
        RuleResult result = rule.run(flag -> false);
        assertEquals(result.status(), RuleExecutionStatus.SKIPPED);
    }

    @Test
    public void test17() {
        Rule rule = Rule.builder().build(TestRule13.class);
        assertThrowsExactly(UnrulyException.class, () -> {
            rule.run(flag -> false);
        });
    }

    @Test
    public void test18() {
        Rule rule = Rule.builder().build(TestRule14.class);
        assertThrowsExactly(UnrulyException.class, () -> {
            rule.run(flag -> true);
        });
    }

    @Test
    public void test19() {
        Rule rule = Rule.builder().build(TestRule15.class);
        assertNull(rule.getPreCondition());
        assertNotNull(rule.getCondition());
        RuleResult result = rule.run();
        assertEquals(result.status(), RuleExecutionStatus.PASS);
    }

    @Test
    public void test20() {
        Rule rule = Rule.builder().build(TestRule16.class);
        RuleResult result = rule.run(flag -> true);
        assertEquals(result.status(), RuleExecutionStatus.PASS);
        result = rule.run(flag -> false);
        assertEquals(result.status(), RuleExecutionStatus.SKIPPED);
    }

    @Test
    public void test21() {
        Rule rule = Rule.builder().build(TestRule17.class);
        assertThrowsExactly(UnrulyException.class, () -> {
            rule.run(flag -> true);
        });
    }

    @Test
    public void test22() {
        Rule rule = Rule.builder().build(TestRule18.class);
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("someText", "abc");
        bindings.bind("someMap", new HashMap<>());
        RuleResult result = rule.run(bindings);
        assertEquals(result.status(), RuleExecutionStatus.PASS);
    }

    @Test
    public void test23() {
        Rule rule = Rule.builder().build(TestRule18.class);
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("someText", "abc");
        bindings.bind("map", new TypeReference<Map<String, List<Integer>>>(){}.getType(), new HashMap<>());
        RuleResult result = rule.run(bindings);
        assertEquals(result.status(), RuleExecutionStatus.PASS);
    }

    @Test
    public void test24() {
        Rule rule = Rule.builder().build(TestRule18.class);
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("someText", "abc");
        bindings.bind("someMap", new ArrayList<>());
        assertThrowsExactly(UnrulyException.class, () -> {
            rule.run(bindings);
        });
    }

    @Test
    public void test25() {
        Rule rule = Rule.builder().build(TestRule19.class);
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("someFloat", 120.00f);
        bindings.bind("someMap", new HashMap<String, List<Integer>>());
        RuleResult result = rule.run(bindings);
        assertEquals(result.status(), RuleExecutionStatus.PASS);
    }

    @Test
    public void test26() {
        Rule rule = Rule.builder().build(TestRule20.class);
        Bindings bindings = Bindings.builder().standard();
        RuleResult result = rule.run(bindings);
        assertEquals(result.status(), RuleExecutionStatus.PASS);
    }

    @Test
    public void test27() {
        Rule rule = Rule.builder().build(TestRule21.class);
        Bindings bindings = Bindings.builder().standard();
        assertThrowsExactly(UnrulyException.class, () -> {
            rule.run(bindings);
        });
    }

    @Test
    public void test28() {
        Rule rule = Rule.builder().build(TestRule22.class);
        Bindings bindings = Bindings.builder().standard();
        assertThrowsExactly(UnrulyException.class, () -> {
            rule.run(bindings);
        });
    }

    @Test
    public void test29() {
        Rule rule = Rule.builder().build(TestRule23.class);
        Bindings bindings = Bindings.builder().standard();
        assertThrowsExactly(UnrulyException.class, () -> {
            rule.run(bindings);
        });
    }

    @Test
    public void test30() {
        Rule rule = Rule.builder().build(TestRule24.class);
        Bindings bindings = Bindings.builder().standard();
        assertThrowsExactly(UnrulyException.class, () -> {
            rule.run(bindings);
        });
    }

    @Test
    public void test31() {
        Rule rule = Rule.builder().build(TestRule25.class);
        Bindings bindings = Bindings.builder().standard();
        Binding<Boolean> flag = bindings.bind("flag", false);
        RuleResult result = rule.run(bindings);
        assertEquals(result.status(), RuleExecutionStatus.PASS);
        assertEquals(true, flag.getValue());
    }

    @Test
    public void test32() {
        Rule rule = Rule.builder().build(TestRule26.class);
        Bindings bindings = Bindings.builder().standard();
        Binding<Boolean> flag = bindings.bind("flag", false);
        RuleResult result = rule.run(bindings);
        assertEquals(result.status(), RuleExecutionStatus.PASS);
        assertEquals(true, flag.getValue());
    }

    @Test
    public void test33() {
        Rule rule = Rule.builder().build(TestRule27.class);
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("flag", true);
        RuleResult result = rule.run(bindings);
        assertEquals(result.status(), RuleExecutionStatus.PASS);
        assertEquals(100, (int) bindings.getValue("someValue"));
    }

    @Test
    public void test34() {
        Rule rule = Rule.builder().build(TestRule27.class);
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("flag", false);
        RuleResult result = rule.run(bindings);
        assertEquals(result.status(), RuleExecutionStatus.FAIL);
        assertFalse(bindings.contains("someValue"));
    }

    @Test
    public void test35() {
        Rule rule = Rule.builder().build(TestRule29.class);
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("flag", true);
        bindings.bind("someValue", 0);
        RuleResult result = rule.run(bindings);
        assertEquals(result.status(), RuleExecutionStatus.PASS);
        assertEquals(200, (int) bindings.getValue("someValue"));
    }

    @Test
    public void test36() {
        Rule rule = Rule.builder().build(TestRule30.class);
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("flag", true);
        bindings.bind("someValue", "");
        RuleResult result = rule.run(bindings);
        assertEquals(result.status(), RuleExecutionStatus.PASS);
        assertEquals("abc", bindings.getValue("someValue"));
    }

    @Test
    public void test37() {
        Rule rule = Rule.builder().build(TestRule31.class);
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("flag", true);
        bindings.bind("someValue", "");
        RuleResult result = rule.run(bindings);
        assertEquals(result.status(), RuleExecutionStatus.PASS);
        assertEquals("xyz", bindings.getValue("someValue"));
    }

    @Test
    public void test38() {
        Rule rule = Rule.builder().build(TestRule32.class);
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("flag", true);
        assertThrowsExactly(UnrulyException.class, () -> {
            rule.run(bindings);
        });
    }

    @Test
    public void test39() {
        Rule rule = Rule.builder().build(TestRule33.class);
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("flag", true);
        bindings.bind("someValue", "");
        assertThrowsExactly(UnrulyException.class, () -> {
            rule.run(bindings);
        });
    }

    @Test
    public void test40() {
        Rule rule = Rule.builder().build(TestRule34.class);
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("flag", true);
        bindings.bind("someValue", "");
        RuleResult result = rule.run(bindings);
        assertEquals(result.status(), RuleExecutionStatus.PASS);
        assertEquals("zzz123", bindings.getValue("someValue"));
    }

    @Test
    public void test41() {
        Rule rule = Rule.builder().build(TestRule35.class);
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
        Rule rule = Rule.builder().build(TestRule35.class);
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("flag", false);
        bindings.bind("someValue", "");
        RuleResult result = rule.run(bindings);
        assertEquals(result.status(), RuleExecutionStatus.FAIL);
        assertEquals("yyy", bindings.getValue("someValue"));
    }

    @Test
    public void test43() {
        assertThrowsExactly(UnrulyException.class, () -> {
            Rule.builder().build(TestRule36.class);
        });
    }

    @Test
    public void test44() {
        Rule rule = Rule.builder().build(TestRule37.class);
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("flag", false);
        RuleResult result = rule.run(bindings);
        assertEquals(result.status(), RuleExecutionStatus.FAIL);
        assertEquals(123, (int) bindings.getValue("newBinding"));
    }

    @Test
    public void test45() {
        Rule rule = Rule.builder().build(TestRule37.class);
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("flag", true);
        RuleResult result = rule.run(bindings);
        assertEquals(result.status(), RuleExecutionStatus.PASS);
        assertEquals(321, (int) bindings.getValue("newBinding"));
    }
}



