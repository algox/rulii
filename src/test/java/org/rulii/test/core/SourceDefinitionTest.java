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
}
