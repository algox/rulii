package org.rulii.test.script.js;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rulii.model.UnrulyException;
import org.rulii.script.DefaultScriptProcessorRegistry;
import org.rulii.script.ScriptProcessor;
import org.rulii.script.ScriptProcessorRegistry;
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
        registry.register(processor);
        Assertions.assertNotNull(registry.getScriptProcessor(processor.getLanguageName()));
    }

    @Test
    public void testRegisteredProcessorIsSameInstance() {
        ScriptProcessorRegistry registry = new DefaultScriptProcessorRegistry(false);
        ScriptProcessor processor = new JSR223ScriptProcessor(engine);
        registry.register(processor);
        Assertions.assertSame(processor, registry.getScriptProcessor(processor.getLanguageName()));
    }

    @Test
    public void testRegisterOverwritesPreviousForSameLanguage() {
        ScriptProcessorRegistry registry = new DefaultScriptProcessorRegistry(false);
        ScriptProcessor first  = new JSR223ScriptProcessor(engine, "ECMAScript", "ctx");
        ScriptProcessor second = new JSR223ScriptProcessor(engine, "ECMAScript", "bindings");
        registry.register(first);
        registry.register(second);
        Assertions.assertSame(second, registry.getScriptProcessor("ECMAScript"));
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
        registry.register(processor);
        registry.deregister(processor);
        Assertions.assertThrows(UnrulyException.class,
                () -> registry.getScriptProcessor(processor.getLanguageName()));
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
        Assertions.assertDoesNotThrow(() -> registry.deregister(processor));
    }

    // -----------------------------------------------------------------------
    // unknown language — autoIncludeJSR223 = false
    // -----------------------------------------------------------------------

    @Test
    public void testUnknownLanguageNoAutoJsr223Throws() {
        ScriptProcessorRegistry registry = new DefaultScriptProcessorRegistry(false);
        Assertions.assertThrows(UnrulyException.class,
                () -> registry.getScriptProcessor("ECMAScript"));
    }

    @Test
    public void testNullLanguageNameThrows() {
        ScriptProcessorRegistry registry = new DefaultScriptProcessorRegistry(false);
        Assertions.assertThrows(Exception.class, () -> registry.getScriptProcessor(null));
    }

    @Test
    public void testEmptyLanguageNameThrows() {
        ScriptProcessorRegistry registry = new DefaultScriptProcessorRegistry(false);
        Assertions.assertThrows(Exception.class, () -> registry.getScriptProcessor(""));
    }

    // -----------------------------------------------------------------------
    // autoIncludeJSR223 = true
    // -----------------------------------------------------------------------

    @Test
    public void testAutoJsr223DiscoversEcmaScript() {
        ScriptProcessorRegistry registry = new DefaultScriptProcessorRegistry(true);
        // GraalJS is on the test classpath and registers "ECMAScript"
        ScriptProcessor processor = registry.getScriptProcessor("ECMAScript");
        Assertions.assertNotNull(processor);
    }

    @Test
    public void testAutoJsr223CachesAfterFirstLookup() {
        ScriptProcessorRegistry registry = new DefaultScriptProcessorRegistry(true);
        ScriptProcessor first  = registry.getScriptProcessor("ECMAScript");
        ScriptProcessor second = registry.getScriptProcessor("ECMAScript");
        Assertions.assertSame(first, second);
    }

    @Test
    public void testAutoJsr223UnknownLanguageThrows() {
        ScriptProcessorRegistry registry = new DefaultScriptProcessorRegistry(true);
        Assertions.assertThrows(UnrulyException.class,
                () -> registry.getScriptProcessor("no-such-language-xyz"));
    }

    // -----------------------------------------------------------------------
    // multiple processors
    // -----------------------------------------------------------------------

    @Test
    public void testMultipleProcessorsCoexist() {
        ScriptProcessorRegistry registry = new DefaultScriptProcessorRegistry(false);
        ScriptProcessor ecma   = new JSR223ScriptProcessor(engine, "ECMAScript", "ctx");
        ScriptProcessor custom = new JSR223ScriptProcessor(engine, "MyLang", "ctx");
        registry.register(ecma);
        registry.register(custom);
        Assertions.assertSame(ecma,   registry.getScriptProcessor("ECMAScript"));
        Assertions.assertSame(custom, registry.getScriptProcessor("MyLang"));
    }
}
