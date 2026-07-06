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
import org.rulii.script.ScriptCompiler;
import org.rulii.script.ScriptProcessor;
import org.rulii.script.jsr223.JSR223ScriptProcessorFactory;

import javax.script.ScriptEngine;

/**
 * Tests for JSR223ScriptProcessorFactory.
 *
 * @author Max Arulananthan
 */
public class JSR223ScriptProcessorFactoryTest {

    public JSR223ScriptProcessorFactoryTest() {
        super();
    }

    @Test
    public void testGetScriptCompiler_usesLanguageNameOverride_notEngineNativeName() {
        ScriptEngine engine = TestScriptUtils.createEngine();
        String engineNativeName = engine.getFactory().getLanguageName();
        String alias = "myLang";
        // Sanity check the override genuinely differs from the engine's native name, otherwise
        // this test can't distinguish "override applied" from "override ignored".
        Assertions.assertNotEquals(engineNativeName, alias);

        JSR223ScriptProcessorFactory factory = new JSR223ScriptProcessorFactory(engine.getFactory(), alias, "ctx");

        ScriptProcessor processor = factory.getScriptProcessor();
        ScriptCompiler compiler = factory.getScriptCompiler();

        Assertions.assertEquals(alias, processor.getLanguageName());
        Assertions.assertEquals(alias, compiler.getLanguageName());
    }
}
