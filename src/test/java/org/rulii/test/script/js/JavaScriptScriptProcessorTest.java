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
package org.rulii.test.script.js;

import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.rulii.script.*;

import javax.script.ScriptEngine;
import java.util.List;

/**
 * Tests for {@link ScriptProcessorFactory} and {@link DefaultScriptProcessor} using GraalJS.
 *
 * @author Max Arulananthan
 * @since 1.2
 */
public class JavaScriptScriptProcessorTest {

    private static ScriptEngine graalJsEngine;

    @BeforeAll
    static void setup() {
        graalJsEngine = createScriptEngine(createEngine());
        Assumptions.assumeTrue(graalJsEngine != null, "GraalJS engine not available on classpath – skipping tests.");
    }

    // -----------------------------------------------------------------------
    // ScriptProcessorFactory tests
    // -----------------------------------------------------------------------

    @Test
    void factoryCreateReturnsNonNull() {
        Assertions.assertNotNull(ScriptProcessorFactory.create());
    }

    @Test
    void factoryStartsEmpty() {
        ScriptProcessorFactory factory = ScriptProcessorFactory.create();
        Assertions.assertTrue(factory.getAvailableLanguages().isEmpty());
    }

    @Test
    void registerAndFindProcessor() {
        ScriptProcessorFactory factory = ScriptProcessorFactory.create();
        ScriptProcessor processor = new DefaultScriptProcessor(graalJsEngine, "javascript");
        factory.register(processor);

        ScriptProcessor found = factory.find("javascript");
        Assertions.assertNotNull(found);
        Assertions.assertSame(processor, found);
    }

    @Test
    void findIsCaseInsensitive() {
        ScriptProcessorFactory factory = ScriptProcessorFactory.create();
        factory.register(new DefaultScriptProcessor(graalJsEngine, "javascript"));

        Assertions.assertNotNull(factory.find("javascript"));
        Assertions.assertNotNull(factory.find("JavaScript"));
        Assertions.assertNotNull(factory.find("JAVASCRIPT"));
        Assertions.assertNotNull(factory.find("JaVaScRiPt"));
    }

    @Test
    void findUnknownLanguageReturnsNull() {
        ScriptProcessorFactory factory = ScriptProcessorFactory.create();
        Assertions.assertNull(factory.find("unknownlanguage"));
    }

    @Test
    void getReturnsRegisteredProcessor() {
        ScriptProcessorFactory factory = ScriptProcessorFactory.create();
        factory.register(new DefaultScriptProcessor(graalJsEngine, "javascript"));
        Assertions.assertNotNull(factory.get("javascript"));
    }

    @Test
    void getThrowsScriptProcessorNotFoundExceptionForUnknownLanguage() {
        ScriptProcessorFactory factory = ScriptProcessorFactory.create();
        Assertions.assertThrows(ScriptProcessorNotFoundException.class,
                () -> factory.get("nonexistentlang"));
    }

    @Test
    void supportsReturnsTrueForRegisteredLanguage() {
        ScriptProcessorFactory factory = ScriptProcessorFactory.create();
        factory.register(new DefaultScriptProcessor(graalJsEngine, "javascript"));
        Assertions.assertTrue(factory.supports("javascript"));
        Assertions.assertTrue(factory.supports("JavaScript"));
    }

    @Test
    void supportsReturnsFalseForUnregisteredLanguage() {
        ScriptProcessorFactory factory = ScriptProcessorFactory.create();
        Assertions.assertFalse(factory.supports("python"));
    }

    @Test
    void getAvailableLanguagesReturnsSortedList() {
        ScriptProcessorFactory factory = ScriptProcessorFactory.create();
        factory.register(new DefaultScriptProcessor(graalJsEngine, "ruby"));
        factory.register(new DefaultScriptProcessor(graalJsEngine, "groovy"));
        factory.register(new DefaultScriptProcessor(graalJsEngine, "javascript"));

        List<String> langs = factory.getAvailableLanguages();
        Assertions.assertEquals(List.of("groovy", "javascript", "ruby"), langs);
    }

    @Test
    void getAvailableLanguagesIsUnmodifiable() {
        ScriptProcessorFactory factory = ScriptProcessorFactory.create();
        factory.register(new DefaultScriptProcessor(graalJsEngine, "javascript"));
        List<String> langs = factory.getAvailableLanguages();
        Assertions.assertThrows(UnsupportedOperationException.class, () -> langs.add("kotlin"));
    }

    @Test
    void registerReplacesExistingProcessorForSameLanguage() {
        ScriptProcessorFactory factory = ScriptProcessorFactory.create();
        ScriptProcessor p1 = new DefaultScriptProcessor(graalJsEngine, "javascript");
        ScriptProcessor p2 = new DefaultScriptProcessor(graalJsEngine, "javascript");

        factory.register(p1);
        factory.register(p2);

        Assertions.assertSame(p2, factory.find("javascript"));
        Assertions.assertEquals(1, factory.getAvailableLanguages().size());
    }

    @Test
    void registerNullProcessorThrowsException() {
        ScriptProcessorFactory factory = ScriptProcessorFactory.create();
        Assertions.assertThrows(Exception.class, () -> factory.register(null));
    }

    @Test
    void findNullLanguageThrowsException() {
        ScriptProcessorFactory factory = ScriptProcessorFactory.create();
        Assertions.assertThrows(Exception.class, () -> factory.find(null));
    }

    @Test
    void findEmptyLanguageThrowsException() {
        ScriptProcessorFactory factory = ScriptProcessorFactory.create();
        Assertions.assertThrows(Exception.class, () -> factory.find(""));
    }

    // -----------------------------------------------------------------------
    // DefaultScriptProcessor tests
    // -----------------------------------------------------------------------

    @Test
    void processorReturnsCustomLanguageName() {
        DefaultScriptProcessor processor = new DefaultScriptProcessor(graalJsEngine, "javascript");
        Assertions.assertEquals("javascript", processor.getLanguage());
    }

    @Test
    void processorReturnsEngineLanguageNameWhenNoCustomName() {
        DefaultScriptProcessor processor = new DefaultScriptProcessor(graalJsEngine);
        Assertions.assertNotNull(processor.getLanguage());
        Assertions.assertFalse(processor.getLanguage().isEmpty());
    }

    @Test
    void processorReturnsNonEmptyLanguageVersion() {
        DefaultScriptProcessor processor = new DefaultScriptProcessor(graalJsEngine, "javascript");
        String version = processor.getLanguageVersion();
        Assertions.assertNotNull(version);
        Assertions.assertFalse(version.isEmpty());
    }

    @Test
    void loadSimpleScriptReturnsNonNull() {
        DefaultScriptProcessor processor = new DefaultScriptProcessor(graalJsEngine, "javascript");
        Script script = processor.load("1 + 1");
        Assertions.assertNotNull(script);
    }

    @Test
    void loadReturnsCompiledScriptBecauseGraalJsIsCompilable() {
        DefaultScriptProcessor processor = new DefaultScriptProcessor(graalJsEngine, "javascript");
        Script script = processor.load("1 + 1");
        Assertions.assertTrue(script.isCompiled(),
                "GraalJS implements Compilable; loaded script should be pre-compiled.");
    }

    @Test
    void loadPreservesOriginalScriptText() {
        DefaultScriptProcessor processor = new DefaultScriptProcessor(graalJsEngine, "javascript");
        String source = "x * 2 + y";
        Script script = processor.load(source);
        Assertions.assertEquals(source, script.getScript());
    }

    @Test
    void loadWithSyntaxErrorThrowsLoadScriptException() {
        DefaultScriptProcessor processor = new DefaultScriptProcessor(graalJsEngine, "javascript");
        Assertions.assertThrows(LoadScriptException.class,
                () -> processor.load("{{{{ this is not valid js ]]]]"));
    }

    @Test
    void loadWithNullScriptThrowsException() {
        DefaultScriptProcessor processor = new DefaultScriptProcessor(graalJsEngine, "javascript");
        Assertions.assertThrows(Exception.class, () -> processor.load(null));
    }

    @Test
    void loadWithEmptyScriptThrowsException() {
        DefaultScriptProcessor processor = new DefaultScriptProcessor(graalJsEngine, "javascript");
        Assertions.assertThrows(Exception.class, () -> processor.load(""));
    }

    private static Engine createEngine() {
        return Engine.newBuilder()
                .option("engine.WarnInterpreterOnly", "false")
                .allowExperimentalOptions(true)
                .build();
    }

    private static GraalJSScriptEngine createScriptEngine(Engine engine) {
        return GraalJSScriptEngine.create(engine,
                Context.newBuilder("js")
                        .allowHostAccess(HostAccess.NONE)
                        .option("js.ecmascript-version", "2022"));

    }
}
