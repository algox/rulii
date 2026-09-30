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
import org.rulii.model.SourceDefinition;
import org.rulii.model.action.Action;
import org.rulii.model.condition.Condition;
import org.rulii.rule.Rule;
import org.rulii.ruleflow.RuleFlow;
import org.rulii.ruleset.RuleSet;
import org.rulii.validation.rules.Validators;

import static org.rulii.model.action.Actions.action;
import static org.rulii.model.condition.Conditions.condition;

/**
 * Verifies that {@link SourceDefinition#build()} records the caller's location, not a frame
 * inside rulii itself.
 *
 * @author Max Arulananthan
 * @since 2.1
 */
public class SourceDefinitionTest {

    @Test
    public void directCallRecordsCaller() {
        SourceDefinition source = SourceDefinition.build();

        Assertions.assertEquals(SourceDefinitionTest.class.getName(), source.getClassName());
        Assertions.assertEquals("directCallRecordsCaller", source.getMethodName());
        Assertions.assertEquals("SourceDefinitionTest.java", source.getFileName());
        Assertions.assertNotNull(source.getLineNumber());
        Assertions.assertTrue(source.getLineNumber() > 0);
    }

    @Test
    public void conditionBuilderRecordsCaller() {
        Condition condition = condition((Integer x) -> x > 10);

        SourceDefinition source = condition.getDefinition().getSource();
        Assertions.assertEquals(SourceDefinitionTest.class.getName(), source.getClassName());
        Assertions.assertEquals("conditionBuilderRecordsCaller", source.getMethodName());
    }

    @Test
    public void actionBuilderRecordsCaller() {
        Action action = action((Integer x) -> {});

        SourceDefinition source = action.getDefinition().getSource();
        Assertions.assertEquals(SourceDefinitionTest.class.getName(), source.getClassName());
        Assertions.assertEquals("actionBuilderRecordsCaller", source.getMethodName());
    }

    @Test
    public void ruleBuilderRecordsCaller() {
        Rule rule = Rule.builder()
                .name("sourceRule")
                .given(condition((Integer x) -> x > 10))
                .then(action((Integer x) -> {}))
                .build();

        SourceDefinition source = rule.getDefinition().getSource();
        Assertions.assertEquals(SourceDefinitionTest.class.getName(), source.getClassName());
        Assertions.assertEquals("ruleBuilderRecordsCaller", source.getMethodName());
    }

    @Test
    public void classBasedRuleRecordsTheRuleClass() {
        Rule rule = Rule.builder().build(TestRule1.class);

        SourceDefinition source = rule.getDefinition().getSource();
        Assertions.assertEquals(TestRule1.class.getName(), source.getClassName());
        Assertions.assertNull(source.getMethodName());
        Assertions.assertNull(source.getLineNumber());
    }

    @Test
    public void validationRulesRecordCaller() {
        Rule supplied = Rule.builder().validationRule("suppliedRule", condition(() -> true)).errorCode("E1").build();
        Assertions.assertEquals(SourceDefinitionTest.class.getName(), supplied.getDefinition().getSource().getClassName());
        Assertions.assertEquals("validationRulesRecordCaller", supplied.getDefinition().getSource().getMethodName());

        Rule predefined = Validators.notNull(Validators.binding("value")).name("notNullRule").build();
        Assertions.assertEquals(SourceDefinitionTest.class.getName(), predefined.getDefinition().getSource().getClassName());
        Assertions.assertEquals("validationRulesRecordCaller", predefined.getDefinition().getSource().getMethodName());
    }

    @Test
    public void explicitSourceWins() {
        SourceDefinition xml = SourceDefinition.forFile("classpath:rules/order.xml", 42);
        Assertions.assertNull(xml.getClassName());
        Assertions.assertNull(xml.getMethodName());
        Assertions.assertEquals("classpath:rules/order.xml", xml.getFileName());
        Assertions.assertEquals(42, xml.getLineNumber());

        Rule rule = Rule.builder().name("xmlRule").given(condition(() -> true)).source(xml).build();
        Assertions.assertSame(xml, rule.getDefinition().getSource());

        Rule classBased = Rule.builder().with(TestRule1.class).source(xml).build();
        Assertions.assertSame(xml, classBased.getDefinition().getSource());

        Rule supplied = Rule.builder().validationRule("xmlValidationRule", condition(() -> true)).errorCode("E1").source(xml).build();
        Assertions.assertSame(xml, supplied.getDefinition().getSource());

        Rule predefined = Validators.notNull(Validators.binding("value")).name("xmlNotNull").source(xml).build();
        Assertions.assertSame(xml, predefined.getDefinition().getSource());

        RuleSet<?> ruleSet = RuleSet.builder().with("xmlRuleSet").source(xml).rule(rule).build();
        Assertions.assertSame(xml, ruleSet.getDefinition().getSource());

        RuleFlow<?> flow = RuleFlow.builder().name("xmlFlow").source(xml).bind("a", 1).build();
        Assertions.assertSame(xml, flow.getDefinition().getSource());

        Assertions.assertThrows(IllegalArgumentException.class, () -> SourceDefinition.forFile(" ", 1));
        Assertions.assertThrows(IllegalArgumentException.class, () -> SourceDefinition.forClass(null));
    }
}
