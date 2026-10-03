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

import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.bind.Bindings;
import org.rulii.context.RuleContext;
import org.rulii.model.function.Function;
import org.rulii.script.BuildScriptException;
import org.rulii.script.EvaluationException;
import org.rulii.script.Script;
import org.rulii.script.ScriptProcessorFactory;
import org.rulii.script.graaljs.GraalJsScriptCompiler;
import org.rulii.script.graaljs.GraalJsScriptProcessor;
import org.rulii.script.graaljs.GraalJsScriptProcessorFactory;
import org.rulii.script.jsr223.JSR223Script;
import org.rulii.script.jsr223.JSR223ScriptProcessor;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tests for the release of per-evaluation polyglot contexts.
 *
 * <p>Every {@code createBindings()} on a GraalJS engine allocates a polyglot Context that is
 * reclaimed only by an explicit close. The processor must close it after each evaluation unless
 * the result is a guest object that would die with it.
 */
public class ContextReleaseTest {

    /** Records whether the processor released the context and keeps the bindings for inspection. */
    static class ObservableGraalProcessor extends GraalJsScriptProcessor {
        javax.script.Bindings lastBindings;
        boolean released;

        ObservableGraalProcessor(ScriptEngine engine) {
            super((GraalJSScriptEngine) engine, "js", "ctx");
        }

        @Override
        protected ScriptContext buildContext(RuleContext context) {
            ScriptContext result = super.buildContext(context);
            lastBindings = result.getBindings(ScriptContext.ENGINE_SCOPE);
            released = false;
            return result;
        }

        @Override
        protected void releaseContext(ScriptContext scriptContext) {
            released = true;
            super.releaseContext(scriptContext);
        }
    }

    /** Same observation points, but on the generic JSR-223 processor (conservative default policy). */
    static class ObservableGenericProcessor extends JSR223ScriptProcessor {
        boolean released;

        ObservableGenericProcessor(ScriptEngine engine) {
            super(engine, "js", "ctx");
        }

        @Override
        protected ScriptContext buildContext(RuleContext context) {
            released = false;
            return super.buildContext(context);
        }

        @Override
        protected void releaseContext(ScriptContext scriptContext) {
            released = true;
            super.releaseContext(scriptContext);
        }
    }

    private static RuleContext context() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("age", Integer.class, 42);
        bindings.bind("name", String.class, "Max");
        return RuleContext.builder().with(bindings).build();
    }

    private static <T> Script<T> compiled(String text) {
        return Script.builder().build(GraalJsScriptProcessorFactory.LANGUAGE_NAME, text);
    }

    private static <T> Script<T> interpreted(String text) {
        return new JSR223Script<>(GraalJsScriptProcessorFactory.LANGUAGE_NAME, text, null, Object.class);
    }

    private static void assertContextClosed(javax.script.Bindings bindings) {
        // GraalJSBindings route every read through the polyglot Context; a closed one refuses.
        Assertions.assertThrows(IllegalStateException.class, () -> bindings.get("ctx"));
    }

    // -----------------------------------------------------------------------
    // GraalJS processor: release decisions
    // -----------------------------------------------------------------------

    @Test
    public void testPrimitiveResultReleasesContext() {
        ObservableGraalProcessor processor = new ObservableGraalProcessor(TestScriptUtils.createEngine());
        Object result = processor.evaluate(compiled("ctx.age + 1"), context());
        Assertions.assertEquals(43, ((Number) result).intValue());
        Assertions.assertTrue(processor.released);
        assertContextClosed(processor.lastBindings);
    }

    @Test
    public void testStringAndBooleanResultsReleaseContext() {
        ObservableGraalProcessor processor = new ObservableGraalProcessor(TestScriptUtils.createEngine());
        Assertions.assertEquals("Max!", processor.evaluate(compiled("ctx.name + '!'"), context()));
        Assertions.assertTrue(processor.released);
        Assertions.assertEquals(Boolean.TRUE, processor.evaluate(compiled("ctx.age >= 18"), context()));
        Assertions.assertTrue(processor.released);
    }

    @Test
    public void testNullAndUndefinedResultsReleaseContext() {
        ObservableGraalProcessor processor = new ObservableGraalProcessor(TestScriptUtils.createEngine());
        Assertions.assertNull(processor.evaluate(compiled("null"), context()));
        Assertions.assertTrue(processor.released);
        Assertions.assertNull(processor.evaluate(compiled("undefined"), context()));
        Assertions.assertTrue(processor.released);
    }

    @Test
    public void testHostObjectResultReleasesContextAndStaysUsable() {
        ObservableGraalProcessor processor = new ObservableGraalProcessor(TestScriptUtils.createEngine());
        Object result = processor.evaluate(compiled("ctx"), context());
        Assertions.assertTrue(processor.released);
        Assertions.assertInstanceOf(Map.class, result);
        Assertions.assertEquals(42, ((Number) ((Map<?, ?>) result).get("age")).intValue());

        Object list = processor.evaluate(compiled("var l = new (Java.type('java.util.ArrayList'))(); l.add(ctx.age); l"), context());
        Assertions.assertTrue(processor.released);
        Assertions.assertEquals(1, ((List<?>) list).size());
    }

    @Test
    public void testJavaScriptObjectResultIsDetachedAndContextReleased() {
        ObservableGraalProcessor processor = new ObservableGraalProcessor(TestScriptUtils.createEngine());
        Object result = processor.evaluate(compiled("({ total: ctx.age * 2, who: ctx.name, nested: { flags: [true, false] } })"), context());
        Assertions.assertTrue(processor.released);
        assertContextClosed(processor.lastBindings);
        Assertions.assertEquals(LinkedHashMap.class, result.getClass());
        Map<?, ?> map = (Map<?, ?>) result;
        Assertions.assertEquals(84, ((Number) map.get("total")).intValue());
        Assertions.assertEquals("Max", map.get("who"));
        Assertions.assertEquals(List.of(true, false), ((Map<?, ?>) map.get("nested")).get("flags"));
    }

    @Test
    public void testJavaScriptArrayResultIsDetachedAndContextReleased() {
        ObservableGraalProcessor processor = new ObservableGraalProcessor(TestScriptUtils.createEngine());
        Object result = processor.evaluate(compiled("[ctx.age, ctx.age + 1, [ctx.name], { k: 1.5 }]"), context());
        Assertions.assertTrue(processor.released);
        assertContextClosed(processor.lastBindings);
        Assertions.assertEquals(ArrayList.class, result.getClass());
        List<?> list = (List<?>) result;
        Assertions.assertEquals(4, list.size());
        Assertions.assertEquals(43, ((Number) list.get(1)).intValue());
        Assertions.assertEquals(List.of("Max"), list.get(2));
        Assertions.assertEquals(1.5, ((Map<?, ?>) list.get(3)).get("k"));
    }

    @Test
    public void testJavaScriptMapSetAndDateResultsAreDetached() {
        ObservableGraalProcessor processor = new ObservableGraalProcessor(TestScriptUtils.createEngine());
        Object map = processor.evaluate(compiled("new Map([['a', 1], ['b', 2]])"), context());
        Assertions.assertTrue(processor.released);
        Assertions.assertEquals(Map.of("a", 1, "b", 2), map);

        Object set = processor.evaluate(compiled("new Set([3, 4, 3])"), context());
        Assertions.assertTrue(processor.released);
        Assertions.assertEquals(List.of(3, 4), set);

        Object date = processor.evaluate(compiled("new Date(0)"), context());
        Assertions.assertTrue(processor.released);
        Assertions.assertEquals(Instant.EPOCH, date);
    }

    @Test
    public void testValuesWrittenIntoBindingsSurviveContextRelease() {
        ObservableGraalProcessor processor = new ObservableGraalProcessor(TestScriptUtils.createEngine());
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("age", Integer.class, 42);
        bindings.bind("list", Object.class, null);
        bindings.bind("obj", Object.class, null);
        RuleContext ctx = RuleContext.builder().with(bindings).build();

        Object result = processor.evaluate(compiled("ctx.list = [ctx.age, 2]; ctx.obj = { ok: true, items: ['a'] }; true"), ctx);
        Assertions.assertEquals(Boolean.TRUE, result);
        Assertions.assertTrue(processor.released);
        assertContextClosed(processor.lastBindings);

        Assertions.assertEquals(List.of(42, 2), bindings.getValue("list"));
        Map<?, ?> obj = (Map<?, ?>) bindings.getValue("obj");
        Assertions.assertEquals(Boolean.TRUE, obj.get("ok"));
        Assertions.assertEquals(List.of("a"), obj.get("items"));
    }

    public static class Holder {
        private List<Integer> items;
        private Object payload;

        public Holder() {
            super();
        }

        public List<Integer> getItems() { return items; }
        public void setItems(List<Integer> items) { this.items = items; }
        public Object getPayload() { return payload; }
        public void setPayload(Object payload) { this.payload = payload; }
    }

    @Test
    public void testValuesPassedToJavaSettersSurviveContextRelease() {
        ObservableGraalProcessor processor = new ObservableGraalProcessor(TestScriptUtils.createEngine());
        Holder holder = new Holder();
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("holder", Holder.class, holder);
        RuleContext ctx = RuleContext.builder().with(bindings).build();

        processor.evaluate(compiled("ctx.holder.items = [1, 2, 3]; ctx.holder.payload = { a: [1] }; true"), ctx);
        Assertions.assertTrue(processor.released);
        Assertions.assertEquals(List.of(1, 2, 3), holder.getItems());
        Assertions.assertEquals(LinkedHashMap.class, holder.getPayload().getClass());
        Assertions.assertEquals(List.of(1), ((Map<?, ?>) holder.getPayload()).get("a"));
    }

    @Test
    public void testJavaScriptFunctionResultKeepsContext() {
        ObservableGraalProcessor processor = new ObservableGraalProcessor(TestScriptUtils.createEngine());
        Object result = processor.evaluate(compiled("(function (x) { return x * 2; })"), context());
        Assertions.assertFalse(processor.released);
        Assertions.assertNotNull(result);
    }

    @Test
    public void testFailedEvaluationReleasesContext() {
        ObservableGraalProcessor processor = new ObservableGraalProcessor(TestScriptUtils.createEngine());
        Assertions.assertThrows(EvaluationException.class,
                () -> processor.evaluate(compiled("throw new Error('boom')"), context()));
        Assertions.assertTrue(processor.released);
        assertContextClosed(processor.lastBindings);
    }

    @Test
    public void testInterpretedScriptFollowsSamePolicy() {
        ObservableGraalProcessor processor = new ObservableGraalProcessor(TestScriptUtils.createEngine());
        Assertions.assertEquals(42, ((Number) processor.evaluate(interpreted("ctx.age"), context())).intValue());
        Assertions.assertTrue(processor.released);
        assertContextClosed(processor.lastBindings);

        Object array = processor.evaluate(interpreted("[1, 2, 3]"), context());
        Assertions.assertTrue(processor.released);
        Assertions.assertEquals(List.of(1, 2, 3), array);

        processor.evaluate(interpreted("(function () {})"), context());
        Assertions.assertFalse(processor.released);

        Assertions.assertThrows(EvaluationException.class,
                () -> processor.evaluate(interpreted("throw new Error('boom')"), context()));
        Assertions.assertTrue(processor.released);
    }

    @Test
    public void testProcessorRemainsUsableAcrossManyEvaluations() {
        ObservableGraalProcessor processor = new ObservableGraalProcessor(TestScriptUtils.createEngine());
        Script<Object> script = compiled("ctx.age >= 18");
        RuleContext ctx = context();

        for (int i = 0; i < 300; i++) {
            Assertions.assertEquals(Boolean.TRUE, processor.evaluate(script, ctx));
            Assertions.assertTrue(processor.released);
        }
    }

    // -----------------------------------------------------------------------
    // Generic JSR-223 processor: conservative default policy
    // -----------------------------------------------------------------------

    @Test
    public void testGenericProcessorReleasesForPrimitiveLikeResults() {
        ObservableGenericProcessor processor = new ObservableGenericProcessor(TestScriptUtils.createEngine());
        processor.evaluate(compiled("ctx.age"), context());
        Assertions.assertTrue(processor.released);
        processor.evaluate(compiled("ctx.name"), context());
        Assertions.assertTrue(processor.released);
        processor.evaluate(compiled("true"), context());
        Assertions.assertTrue(processor.released);
        processor.evaluate(compiled("null"), context());
        Assertions.assertTrue(processor.released);
    }

    @Test
    public void testGenericProcessorKeepsContextForAnyOtherResult() {
        ObservableGenericProcessor processor = new ObservableGenericProcessor(TestScriptUtils.createEngine());
        // Both results are safe in reality (a host object and a detached copy), but the generic
        // processor cannot know that and must not guess.
        Object host = processor.evaluate(compiled("ctx"), context());
        Assertions.assertFalse(processor.released);
        Assertions.assertInstanceOf(Map.class, host);

        Object detached = processor.evaluate(compiled("[1, 2]"), context());
        Assertions.assertFalse(processor.released);
        Assertions.assertEquals(List.of(1, 2), detached);
    }

    @Test
    public void testGenericProcessorReleasesOnFailure() {
        ObservableGenericProcessor processor = new ObservableGenericProcessor(TestScriptUtils.createEngine());
        Assertions.assertThrows(EvaluationException.class,
                () -> processor.evaluate(compiled("throw new Error('boom')"), context()));
        Assertions.assertTrue(processor.released);
    }

    // -----------------------------------------------------------------------
    // Factory wiring and the compiler
    // -----------------------------------------------------------------------

    @Test
    public void testFactoryProvidesGraalAwareProcessorAndCompiler() {
        ScriptProcessorFactory factory = new GraalJsScriptProcessorFactory();
        Assertions.assertInstanceOf(GraalJsScriptProcessor.class, factory.getScriptProcessor());
        Assertions.assertInstanceOf(GraalJsScriptCompiler.class, factory.getScriptCompiler());
        Assertions.assertEquals(GraalJsScriptProcessorFactory.LANGUAGE_NAME, factory.getScriptCompiler().getLanguageName());
    }

    @Test
    public void testCompiledScriptEvaluatesAfterCompilerEngineIsClosed() {
        ScriptProcessorFactory factory = new GraalJsScriptProcessorFactory();
        Script<Object> script = factory.getScriptCompiler().compile("ctx.age * 2", Object.class);
        Assertions.assertNotNull(((JSR223Script<Object>) script).getCompiledScript());

        Object result = factory.getScriptProcessor().evaluate(script, context());
        Assertions.assertEquals(84, ((Number) result).intValue());
        // And again, to prove the compiled script is reusable.
        Assertions.assertEquals(84, ((Number) factory.getScriptProcessor().evaluate(script, context())).intValue());
    }

    @Test
    public void testCompilerStillReportsSyntaxErrors() {
        ScriptProcessorFactory factory = new GraalJsScriptProcessorFactory();
        Assertions.assertThrows(BuildScriptException.class,
                () -> factory.getScriptCompiler().compile("@@@ bad @@@", Object.class));
    }

    @Test
    public void testCompilerCanBeUsedRepeatedly() {
        ScriptProcessorFactory factory = new GraalJsScriptProcessorFactory();
        for (int i = 0; i < 50; i++) {
            Script<Object> script = factory.getScriptCompiler().compile("ctx.age + " + i, Object.class);
            Assertions.assertEquals(42 + i, ((Number) factory.getScriptProcessor().evaluate(script, context())).intValue());
        }
    }

    // -----------------------------------------------------------------------
    // End to end through the Function pipeline
    // -----------------------------------------------------------------------

    @Test
    public void testFunctionReturningJavaScriptArrayIsUsableByCaller() {
        Function<Object> function = Function.builder().build(compiled("[ctx.age, ctx.name]"));
        Object result = function.apply(context());
        Assertions.assertEquals(ArrayList.class, result.getClass());
        List<?> list = (List<?>) result;
        Assertions.assertEquals(42, ((Number) list.get(0)).intValue());
        Assertions.assertEquals("Max", list.get(1));
    }
}
