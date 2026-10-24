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
package org.rulii.test.script.el;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.bind.Bindings;
import org.rulii.context.RuleContext;
import org.rulii.model.condition.Condition;
import org.rulii.model.function.Function;
import org.rulii.script.BuildScriptException;
import org.rulii.script.EvaluationException;
import org.rulii.script.Script;
import org.rulii.script.ScriptProcessorFactory;
import org.rulii.script.ScriptProcessorManager;
import org.rulii.script.el.ElScript;
import org.rulii.script.el.ElScriptProcessorFactory;

import java.util.List;
import java.util.Map;

/**
 * Jakarta Expression Language as a rulii scripting language: bindings reachable through the root
 * object and as bare identifiers, the standard resolvers, compile and evaluation errors.
 */
public class ElScriptTest {

    private static final String LANG = ElScriptProcessorFactory.LANGUAGE_NAME;

    public ElScriptTest() {
        super();
    }

    private static RuleContext context() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("age", int.class, 20);
        bindings.bind("name", String.class, "rulii");
        bindings.bind("person", Person.class, new Person("Ada", 36));
        bindings.bind("tags", List.class, List.of("a", "b"));
        bindings.bind("limits", Map.class, Map.of("max", 5));
        bindings.bind("empty", String.class, "reserved");
        bindings.bind("nothing", String.class, null);
        return RuleContext.builder().with(bindings).build();
    }

    @Test
    public void factoryIsRegisteredAndAvailable() {
        ScriptProcessorFactory factory = ScriptProcessorManager.getInstance().getScriptProcessorFactory(LANG);

        Assertions.assertInstanceOf(ElScriptProcessorFactory.class, factory);
        Assertions.assertTrue(factory.isAvailable());
        Assertions.assertEquals("ctx", factory.getBindingsName());
        Assertions.assertSame(factory, ScriptProcessorManager.getInstance().getScriptProcessorFactory("jel"));
    }

    @Test
    public void bindingsAreReachableThroughTheRootObjectAndBareNames() {
        Condition root = Condition.builder().build(Script.builder().build(LANG, "${ctx.age >= 18}"));
        Condition bare = Condition.builder().build(Script.builder().build(LANG, "${age >= 18}"));

        Assertions.assertTrue(root.isTrue(context()));
        Assertions.assertTrue(bare.isTrue(context()));
        Assertions.assertEquals(21, ((Number) Function.builder().build(Script.builder().build(LANG, "${age + 1}")).apply(context())).intValue(), "EL arithmetic yields a Long");
    }

    @Test
    public void standardResolversApply() {
        Assertions.assertEquals("Ada", Function.builder().build(Script.builder().build(LANG, "${person.name}")).apply(context()));
        Assertions.assertEquals("b", Function.builder().build(Script.builder().build(LANG, "${tags[1]}")).apply(context()));
        Assertions.assertEquals(5L, ((Number) Function.builder().build(Script.builder().build(LANG, "${limits.max}")).apply(context())).longValue());
        Assertions.assertEquals(5, Function.builder().build(Script.builder().build(LANG, "${name.length()}")).apply(context()));
        Assertions.assertEquals("rulii!", Function.builder().build(Script.builder().build(LANG, "${name += '!'}")).apply(context()));
        Assertions.assertEquals("yes", Function.builder().build(Script.builder().build(LANG, "${empty nothing ? 'yes' : 'no'}")).apply(context()));
    }

    @Test
    public void reservedWordBindingsNeedTheRootObjectInBracketForm() {
        Assertions.assertThrows(BuildScriptException.class, () -> Script.builder().build(LANG, "${ctx.empty}"), "a reserved word is no identifier, even after a dot");
        Assertions.assertEquals("reserved", Function.builder().build(Script.builder().build(LANG, "${ctx['empty']}")).apply(context()));
        Assertions.assertEquals("ctx", Function.builder().build(Script.builder().build(LANG, "${ctx.name == 'rulii' ? 'ctx' : 'other'}")).apply(context()));
    }

    @Test
    public void literalTextAndMixedTemplatesWork() {
        Assertions.assertEquals("Hello Ada, 36", Function.builder().build(Script.builder().build(LANG, "Hello ${person.name}, ${person.age}")).apply(context()));
    }

    @Test
    public void compileAndEvaluationErrorsAreReported() {
        BuildScriptException compile = Assertions.assertThrows(BuildScriptException.class, () -> Script.builder().build(LANG, "${age >"));
        Assertions.assertEquals("${age >", compile.getScript());

        Script<Object> unknown = Script.builder().build(LANG, "${nobody.name}");
        Assertions.assertInstanceOf(ElScript.class, unknown);
        EvaluationException evaluation = Assertions.assertThrows(EvaluationException.class, () -> unknown.run(context()));
        Assertions.assertEquals("${nobody.name}", evaluation.getScript());

        Assertions.assertThrows(EvaluationException.class, () -> Script.builder().build(LANG, "${age = 3}").run(context()));
    }

    @Test
    public void compiledScriptIsSharedAcrossContexts() {
        Script<Object> script = Script.builder().build(LANG, "${age}");

        Assertions.assertEquals(20, script.run(context()));
        Bindings other = Bindings.builder().standard();
        other.bind("age", int.class, 7);
        Assertions.assertEquals(7, script.run(RuleContext.builder().with(other).build()));
    }

    public static class Person {

        private final String name;
        private final int age;

        public Person(String name, int age) {
            super();
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }
    }
}
