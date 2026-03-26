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

package org.rulii.test.script.js;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.bind.Bindings;
import org.rulii.context.RuleContext;
import org.rulii.script.Script;
import org.rulii.script.ScriptParameter;
import org.rulii.script.graaljs.GraalJsScriptProcessorFactory;

/**
 * Unit tests for ScriptBuilder / ScriptBuilderBuilder — covers Script creation,
 * metadata accessors, and parameter attachment.
 */
public class ScriptBuildTest {

    @Test
    public void testSimpleCtxTest() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("a", int.class, 10);
        bindings.bind("b", int.class, 20);
        bindings.bind("c", int.class, 0);

        RuleContext context = RuleContext.builder()
                .with(bindings)
                .build();

        Script<Integer> script = Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME, "ctx.c = ctx.a + ctx.b;");
        Integer result = script.run(context);
        Assertions.assertEquals(30, result);
        Assertions.assertEquals(30, (Integer) bindings.getValue("c"));
    }

    @Test
    public void testBuildShorthand() {
        Script<Integer> script = Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME, "1 + 2");
        Assertions.assertEquals(GraalJsScriptProcessorFactory.LANGUAGE_NAME, script.getLanguageName());
        Assertions.assertEquals("1 + 2", script.getScript());
    }

    @Test
    public void testBuildWithFluent() {
        Script<String> script = Script.builder()
                .with(GraalJsScriptProcessorFactory.LANGUAGE_NAME, "'hello'")
                .build();
        Assertions.assertEquals(GraalJsScriptProcessorFactory.LANGUAGE_NAME, script.getLanguageName());
        Assertions.assertEquals("'hello'", script.getScript());
    }

    @Test
    public void testGetScriptParametersEmptyByDefault() {
        Script<?> script = Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME, "1");
        Assertions.assertNotNull(script.getScriptParameters());
        Assertions.assertTrue(script.getScriptParameters().isEmpty());
    }

    @Test
    public void testWithSingleParameter() {
        Script<?> script = Script.builder()
                .with(GraalJsScriptProcessorFactory.LANGUAGE_NAME, "ctx.age > 18")
                .param(new ScriptParameter("age", Integer.class))
                .build();
        Assertions.assertEquals(1, script.getScriptParameters().size());
        Assertions.assertEquals("age", script.getScriptParameters().get(0).getName());
        Assertions.assertEquals(Integer.class, script.getScriptParameters().get(0).getType());
    }

    @Test
    public void testWithMultipleParameters() {
        Script<?> script = Script.builder()
                .with(GraalJsScriptProcessorFactory.LANGUAGE_NAME, "ctx.a + ctx.b")
                .param(new ScriptParameter("a", Integer.class))
                .param(new ScriptParameter("b", Integer.class))
                .build();
        Assertions.assertEquals(2, script.getScriptParameters().size());
    }

    @Test
    public void testNullLanguageThrows() {
        Assertions.assertThrows(Exception.class, () -> Script.builder().build(null, "1 + 2"));
    }

    @Test
    public void testEmptyLanguageThrows() {
        Assertions.assertThrows(Exception.class, () -> Script.builder().build("", "1 + 2"));
    }

    @Test
    public void testNullScriptTextThrows() {
        Assertions.assertThrows(Exception.class, () -> Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME, null));
    }

    @Test
    public void testEmptyScriptTextThrows() {
        Assertions.assertThrows(Exception.class, () -> Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME, ""));
    }

    @Test
    public void testToStringContainsScript() {
        Script<?> script = Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME, "ctx.x + 1");
        Assertions.assertTrue(script.toString().contains("ctx.x + 1"));
    }
}
