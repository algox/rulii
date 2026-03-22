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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rulii.model.UnrulyException;
import org.rulii.script.*;
import org.rulii.script.jsr223.JSR223ScriptProcessor;

import javax.script.ScriptEngine;

/**
 * Unit tests for DefaultScriptProcessorRegistry — covers register, deregister,
 * lookup, auto-JSR223 discovery, and guard conditions.
 */
public class ScriptProcessorRegistryTest {

    private ScriptEngine engine;

    @BeforeEach
    public void setUp() {
        engine = TestScriptUtils.createEngine();
    }

    // -----------------------------------------------------------------------
    // register / getScriptProcessor
    // -----------------------------------------------------------------------

    @Test
    public void testRegisterAndRetrieve() {
        ScriptProcessorRegistry registry = new DefaultScriptProcessorRegistry(false);
        ScriptProcessor processor = new JSR223ScriptProcessor(engine);
        registry.register(new SingleScriptProcessorFactory(processor));
        Assertions.assertNotNull(registry.getScriptProcessorFactory(processor.getLanguageName()));
    }

    @Test
    public void testRegisteredProcessorIsSameInstance() {
        ScriptProcessorRegistry registry = new DefaultScriptProcessorRegistry(false);
        ScriptProcessorFactory processor = new SingleScriptProcessorFactory(new JSR223ScriptProcessor(engine));
        registry.register(processor);
        Assertions.assertSame(processor, registry.getScriptProcessorFactory(processor.getLanguageName()));
    }

    @Test
    public void testRegisterOverwritesPreviousForSameLanguage() {
        ScriptProcessorRegistry registry = new DefaultScriptProcessorRegistry(false);
        ScriptProcessorFactory first  = new SingleScriptProcessorFactory(new JSR223ScriptProcessor(engine, "ECMAScript", "ctx"));
        ScriptProcessorFactory second = new SingleScriptProcessorFactory(new JSR223ScriptProcessor(engine, "ECMAScript", "bindings"));
        registry.register(first);
        registry.register(second);
        Assertions.assertSame(second, registry.getScriptProcessorFactory("ECMAScript"));
    }

    @Test
    public void testRegisterNullThrows() {
        ScriptProcessorRegistry registry = new DefaultScriptProcessorRegistry(false);
        Assertions.assertThrows(Exception.class, () -> registry.register(null));
    }

    // -----------------------------------------------------------------------
    // deregister
    // -----------------------------------------------------------------------

    @Test
    public void testDeregisterRemovesProcessor() {
        ScriptProcessorRegistry registry = new DefaultScriptProcessorRegistry(false);
        ScriptProcessor processor = new JSR223ScriptProcessor(engine);
        registry.register(new SingleScriptProcessorFactory(processor));
        registry.deregister(new SingleScriptProcessorFactory(processor));
        Assertions.assertThrows(UnrulyException.class,
                () -> registry.getScriptProcessorFactory(processor.getLanguageName()));
    }

    @Test
    public void testDeregisterNullThrows() {
        ScriptProcessorRegistry registry = new DefaultScriptProcessorRegistry(false);
        Assertions.assertThrows(Exception.class, () -> registry.deregister(null));
    }

    @Test
    public void testDeregisterUnknownProcessorIsNoOp() {
        ScriptProcessorRegistry registry = new DefaultScriptProcessorRegistry(false);
        ScriptProcessor processor = new JSR223ScriptProcessor(engine);
        // Never registered — should not throw
        Assertions.assertDoesNotThrow(() -> registry.deregister(new SingleScriptProcessorFactory(processor)));
    }

    // -----------------------------------------------------------------------
    // unknown language — autoIncludeJSR223 = false
    // -----------------------------------------------------------------------

    @Test
    public void testUnknownLanguageNoAutoJsr223Throws() {
        ScriptProcessorRegistry registry = new DefaultScriptProcessorRegistry(false);
        Assertions.assertThrows(UnrulyException.class,
                () -> registry.getScriptProcessorFactory("ECMAScript"));
    }

    @Test
    public void testNullLanguageNameThrows() {
        ScriptProcessorRegistry registry = new DefaultScriptProcessorRegistry(false);
        Assertions.assertThrows(Exception.class, () -> registry.getScriptProcessorFactory(null));
    }

    @Test
    public void testEmptyLanguageNameThrows() {
        ScriptProcessorRegistry registry = new DefaultScriptProcessorRegistry(false);
        Assertions.assertThrows(Exception.class, () -> registry.getScriptProcessorFactory(""));
    }

    // -----------------------------------------------------------------------
    // autoIncludeJSR223 = true
    // -----------------------------------------------------------------------

    @Test
    public void testAutoJsr223DiscoversEcmaScript() {
        ScriptProcessorRegistry registry = new DefaultScriptProcessorRegistry(true);
        // GraalJS is on the test classpath and registers "ECMAScript"
        ScriptProcessorFactory factory = registry.getScriptProcessorFactory("ECMAScript");
        Assertions.assertNotNull(factory);
    }

    @Test
    public void testAutoJsr223CachesAfterFirstLookup() {
        ScriptProcessorRegistry registry = new DefaultScriptProcessorRegistry(true);
        ScriptProcessorFactory first  = registry.getScriptProcessorFactory("ECMAScript");
        ScriptProcessorFactory second = registry.getScriptProcessorFactory("ECMAScript");
        Assertions.assertSame(first, second);
    }

    @Test
    public void testAutoJsr223UnknownLanguageThrows() {
        ScriptProcessorRegistry registry = new DefaultScriptProcessorRegistry(true);
        Assertions.assertThrows(UnrulyException.class,
                () -> registry.getScriptProcessorFactory("no-such-language-xyz"));
    }

    // -----------------------------------------------------------------------
    // multiple processors
    // -----------------------------------------------------------------------

    @Test
    public void testMultipleProcessorsCoexist() {
        ScriptProcessorRegistry registry = new DefaultScriptProcessorRegistry(false);
        ScriptProcessorFactory ecma   = new SingleScriptProcessorFactory(new JSR223ScriptProcessor(engine, "ECMAScript", "ctx"));
        ScriptProcessorFactory custom = new SingleScriptProcessorFactory(new JSR223ScriptProcessor(engine, "MyLang", "ctx"));
        registry.register(ecma);
        registry.register(custom);
        Assertions.assertSame(ecma,   registry.getScriptProcessorFactory("ECMAScript"));
        Assertions.assertSame(custom, registry.getScriptProcessorFactory("MyLang"));
    }

    private static class SingleScriptProcessorFactory implements ScriptProcessorFactory {

        private final ScriptProcessor processor;

        public SingleScriptProcessorFactory(ScriptProcessor processor) {
            super();
            this.processor = processor;
        }

        @Override
        public String getLanguageName() {
            return processor.getLanguageName();
        }

        @Override
        public String getBindingName() {
            return ScriptOptions.DEFAULT.bindingsName();
        }

        @Override
        public ScriptProcessor create() {
            return processor;
        }
    }
}
