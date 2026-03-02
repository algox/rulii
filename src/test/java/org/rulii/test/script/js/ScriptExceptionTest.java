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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.rulii.bind.Bindings;
import org.rulii.script.*;

import javax.script.ScriptEngine;
import java.util.List;

/**
 * Tests for script-related exception types:
 * {@link EvaluationException}, {@link LoadScriptException}, and
 * {@link ScriptProcessorNotFoundException}.
 *
 * @author Max Arulananthan
 * @since 1.2
 */
public class ScriptExceptionTest {

    private static ScriptProcessor processor;

    @BeforeAll
    static void setup() {
        ScriptEngine engine = createScriptEngine(createEngine());
        processor = new DefaultScriptProcessor(engine, "javascript");
    }

    // -----------------------------------------------------------------------
    // EvaluationException – direct construction
    // -----------------------------------------------------------------------

    @Test
    void evaluationExceptionStoresScriptSource() {
        EvaluationException ex = new EvaluationException("x + y", "some error");
        Assertions.assertEquals("x + y", ex.getScript());
    }

    @Test
    void evaluationExceptionStoresMessage() {
        EvaluationException ex = new EvaluationException("x + y", "evaluation failed");
        Assertions.assertEquals("evaluation failed", ex.getMessage());
    }

    @Test
    void evaluationExceptionWithNullScript() {
        EvaluationException ex = new EvaluationException(null, "error");
        Assertions.assertNull(ex.getScript());
        Assertions.assertEquals("error", ex.getMessage());
    }

    @Test
    void evaluationExceptionWithCausePreservesAllFields() {
        RuntimeException cause = new RuntimeException("underlying cause");
        EvaluationException ex = new EvaluationException("script source", "wrapper message", cause);
        Assertions.assertEquals("script source", ex.getScript());
        Assertions.assertEquals("wrapper message", ex.getMessage());
        Assertions.assertSame(cause, ex.getCause());
    }

    @Test
    void evaluationExceptionIsRaisedOnRuntimeError() {
        Script script = processor.load("undeclaredFn()");
        EvaluationException ex = Assertions.assertThrows(EvaluationException.class,
                () -> script.eval(Bindings.builder().standard()));
        Assertions.assertEquals("undeclaredFn()", ex.getScript());
        Assertions.assertNotNull(ex.getCause());
    }

    // -----------------------------------------------------------------------
    // LoadScriptException – direct construction
    // -----------------------------------------------------------------------

    @Test
    void loadScriptExceptionStoresScriptSource() {
        LoadScriptException ex = new LoadScriptException("{{ bad js", "parse error");
        Assertions.assertEquals("{{ bad js", ex.getScript());
    }

    @Test
    void loadScriptExceptionStoresMessage() {
        LoadScriptException ex = new LoadScriptException("bad", "syntax error");
        Assertions.assertEquals("syntax error", ex.getMessage());
    }

    @Test
    void loadScriptExceptionWithNullScript() {
        LoadScriptException ex = new LoadScriptException(null, "error");
        Assertions.assertNull(ex.getScript());
    }

    @Test
    void loadScriptExceptionWithCausePreservesAllFields() {
        RuntimeException cause = new RuntimeException("parser failed");
        LoadScriptException ex = new LoadScriptException("bad script", "load error", cause);
        Assertions.assertEquals("bad script", ex.getScript());
        Assertions.assertEquals("load error", ex.getMessage());
        Assertions.assertSame(cause, ex.getCause());
    }

    @Test
    void loadScriptExceptionIsRaisedOnSyntaxError() {
        LoadScriptException ex = Assertions.assertThrows(LoadScriptException.class,
                () -> processor.load("{{{{ this is not valid javascript ]]]]"));
        Assertions.assertNotNull(ex.getScript());
        Assertions.assertNotNull(ex.getCause());
    }

    // -----------------------------------------------------------------------
    // ScriptProcessorNotFoundException – direct construction
    // -----------------------------------------------------------------------

    @Test
    void scriptProcessorNotFoundExceptionStoresLanguage() {
        List<String> available = List.of("javascript", "groovy");
        ScriptProcessorNotFoundException ex =
                new ScriptProcessorNotFoundException("python", available);
        Assertions.assertEquals("python", ex.getLanguage());
    }

    @Test
    void scriptProcessorNotFoundExceptionStoresAvailableLanguages() {
        List<String> available = List.of("javascript", "groovy");
        ScriptProcessorNotFoundException ex =
                new ScriptProcessorNotFoundException("python", available);
        Assertions.assertEquals(available, ex.getAvailableLanguages());
    }

    @Test
    void scriptProcessorNotFoundExceptionMessageContainsRequestedLanguage() {
        ScriptProcessorNotFoundException ex =
                new ScriptProcessorNotFoundException("python", List.of("javascript"));
        Assertions.assertTrue(ex.getMessage().contains("python"),
                "Message should mention the requested language.");
    }

    @Test
    void scriptProcessorNotFoundExceptionMessageContainsAvailableLanguages() {
        ScriptProcessorNotFoundException ex =
                new ScriptProcessorNotFoundException("python", List.of("javascript", "groovy"));
        Assertions.assertTrue(ex.getMessage().contains("javascript"),
                "Message should list available languages.");
    }

    @Test
    void scriptProcessorNotFoundExceptionIsRaisedByFactory() {
        ScriptProcessorFactory factory = ScriptProcessorFactory.create();
        ScriptProcessorNotFoundException ex = Assertions.assertThrows(
                ScriptProcessorNotFoundException.class,
                () -> factory.get("nonexistentlang"));
        Assertions.assertEquals("nonexistentlang", ex.getLanguage());
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
