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
import org.rulii.model.condition.Condition;
import org.rulii.script.Script;
import org.rulii.script.ScriptCompiler;
import org.rulii.script.ScriptOptions;
import org.rulii.script.ScriptProcessor;
import org.rulii.script.ScriptProcessorFactory;
import org.rulii.script.ScriptProcessorManager;
import org.rulii.script.graaljs.GraalJsScriptProcessorFactory;
import org.rulii.script.jsr223.JSR223ScriptProcessorFactory;

import com.oracle.truffle.js.scriptengine.GraalJSEngineFactory;

import javax.script.ScriptEngineFactory;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Tests for factory aliases: every name GraalJS answers to must resolve to the configured
 * {@link GraalJsScriptProcessorFactory}, never to the generic JSR-223 wrapper.
 */
public class GraalJsAliasTest {

    /** Stand-in factory registered under a unique name to test precedence; never evaluated. */
    static class StubFactory implements ScriptProcessorFactory {
        private final String languageName;

        StubFactory(String languageName) {
            super();
            this.languageName = languageName;
        }

        @Override public String getLanguageName() { return languageName; }
        @Override public String getBindingsName() { return ScriptOptions.DEFAULT.bindingsName(); }
        @Override public ScriptProcessor getScriptProcessor() { throw new UnsupportedOperationException(); }
        @Override public ScriptCompiler getScriptCompiler() { throw new UnsupportedOperationException(); }
    }

    /** GraalJS factory with test-controlled aliases, so the shared registry is not polluted with real names. */
    static class AliasedGraalFactory extends GraalJsScriptProcessorFactory {
        private final List<String> aliases;

        AliasedGraalFactory(String languageName, List<String> aliases) {
            super(languageName, ScriptOptions.DEFAULT.bindingsName());
            this.aliases = aliases;
        }

        @Override
        public Collection<String> getAliases() {
            return aliases;
        }
    }

    private static String unique(String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }

    // -----------------------------------------------------------------------
    // Alias set
    // -----------------------------------------------------------------------

    private static List<String> graalJsNames() {
        return new GraalJSEngineFactory().getNames();
    }

    @Test
    public void testAliasesAreTheGraalJsEngineNamesMinusOwnName() {
        List<String> names = graalJsNames();
        Assertions.assertTrue(names.containsAll(List.of("js", "JS", "JavaScript", "javascript", "ECMAScript", "graal.js")));

        ScriptProcessorFactory factory = new GraalJsScriptProcessorFactory();
        Assertions.assertFalse(factory.getAliases().contains(GraalJsScriptProcessorFactory.LANGUAGE_NAME));
        Assertions.assertEquals(names.size() - 1, factory.getAliases().size());
        Assertions.assertTrue(names.containsAll(factory.getAliases()));

        ScriptProcessorFactory ecma = new GraalJsScriptProcessorFactory("ECMAScript", "ctx");
        Assertions.assertFalse(ecma.getAliases().contains("ECMAScript"));
        Assertions.assertTrue(ecma.getAliases().contains("js"));
    }

    @Test
    public void testGenericJsr223FactoryAliasesAreTheEngineNamesMinusOwnName() {
        ScriptEngineFactory engineFactory = new FakeScriptEngineFactory();
        ScriptProcessorFactory wrapper = new JSR223ScriptProcessorFactory(engineFactory);
        Assertions.assertEquals(FakeScriptEngineFactory.LANGUAGE_NAME, wrapper.getLanguageName());
        Assertions.assertEquals(List.of("RuliiFake", "rulii-fake-js"), List.copyOf(wrapper.getAliases()));

        ScriptProcessorFactory renamed = new JSR223ScriptProcessorFactory(engineFactory, "RuliiFake", null);
        Assertions.assertEquals(List.of("rulii-fake", "rulii-fake-js"), List.copyOf(renamed.getAliases()));
    }

    @Test
    public void testDefaultFactoryHasNoAliases() {
        Assertions.assertTrue(new StubFactory("stub").getAliases().isEmpty());
    }

    // -----------------------------------------------------------------------
    // Registry resolution of the real names
    // -----------------------------------------------------------------------

    @Test
    public void testEveryGraalJsNameResolvesToTheConfiguredFactory() {
        ScriptProcessorManager manager = ScriptProcessorManager.getInstance();

        for (String name : graalJsNames()) {
            ScriptProcessorFactory factory = manager.getScriptProcessorFactory(name);
            Assertions.assertNotNull(factory, name);
            Assertions.assertInstanceOf(GraalJsScriptProcessorFactory.class, factory, name);
            Assertions.assertFalse(factory instanceof JSR223ScriptProcessorFactory, name);
        }
    }

    @Test
    public void testFallbackDiscoveryRegistersEngineNamesOnOneWrapper() {
        ScriptProcessorManager manager = ScriptProcessorManager.getInstance();

        // rulii ships no factory for the fake engine, so the first lookup falls back to JSR-223
        // discovery and wraps the engine; the other names must land on that same wrapper.
        ScriptProcessorFactory first = manager.getScriptProcessorFactory("rulii-fake-js");
        Assertions.assertNotNull(first);
        Assertions.assertInstanceOf(JSR223ScriptProcessorFactory.class, first);

        for (String name : FakeScriptEngineFactory.NAMES) {
            Assertions.assertSame(first, manager.getScriptProcessorFactory(name), name);
        }

        // And the wrapper works end to end under any of the spellings.
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("age", Integer.class, 42);
        RuleContext ctx = RuleContext.builder().with(bindings).build();
        Condition condition = Condition.builder().build(Script.builder().build("RuliiFake", "ctx.age >= 18"));
        Assertions.assertTrue(condition.isTrue(ctx));
    }

    @Test
    public void testAliasLookupsShareOneFactoryInstance() {
        ScriptProcessorManager manager = ScriptProcessorManager.getInstance();
        ScriptProcessorFactory primary = manager.getScriptProcessorFactory("js");
        Assertions.assertSame(primary, manager.getScriptProcessorFactory("JavaScript"));
        Assertions.assertSame(primary, manager.getScriptProcessorFactory("graal.js"));
        Assertions.assertSame(primary, manager.getScriptProcessorFactory("GraalJSPolyglot"));
    }

    // -----------------------------------------------------------------------
    // Precedence rules, using unique names so the shared registry stays clean
    // -----------------------------------------------------------------------

    @Test
    public void testAliasIsRegisteredWhenNameIsFree() {
        String language = unique("lang");
        String alias = unique("alias");
        ScriptProcessorFactory factory = new AliasedGraalFactory(language, List.of(alias));

        ScriptProcessorManager.getInstance().register(factory);

        Assertions.assertSame(factory, ScriptProcessorManager.getInstance().getScriptProcessorFactory(language));
        Assertions.assertSame(factory, ScriptProcessorManager.getInstance().getScriptProcessorFactory(alias));
    }

    @Test
    public void testExplicitRegistrationWinsOverAlias() {
        String taken = unique("taken");
        StubFactory explicit = new StubFactory(taken);
        ScriptProcessorManager.getInstance().register(explicit);

        ScriptProcessorFactory aliased = new AliasedGraalFactory(unique("lang"), List.of(taken));
        ScriptProcessorManager.getInstance().register(aliased);

        Assertions.assertSame(explicit, ScriptProcessorManager.getInstance().getScriptProcessorFactory(taken));
    }

    @Test
    public void testLaterExplicitRegistrationReplacesAlias() {
        String name = unique("name");
        ScriptProcessorFactory aliased = new AliasedGraalFactory(unique("lang"), List.of(name));
        ScriptProcessorManager.getInstance().register(aliased);
        Assertions.assertSame(aliased, ScriptProcessorManager.getInstance().getScriptProcessorFactory(name));

        StubFactory explicit = new StubFactory(name);
        ScriptProcessorManager.getInstance().register(explicit);
        Assertions.assertSame(explicit, ScriptProcessorManager.getInstance().getScriptProcessorFactory(name));
    }

    @Test
    public void testBlankAliasesAreIgnored() {
        String language = unique("lang");
        ScriptProcessorFactory factory = new AliasedGraalFactory(language, java.util.Arrays.asList("", "  ", null));
        Assertions.assertDoesNotThrow(() -> ScriptProcessorManager.getInstance().register(factory));
        Assertions.assertSame(factory, ScriptProcessorManager.getInstance().getScriptProcessorFactory(language));
    }

    // -----------------------------------------------------------------------
    // End to end: an aliased name gets the configured engine, not a bare one
    // -----------------------------------------------------------------------

    @Test
    public void testScriptBuiltUnderAliasUsesConfiguredEngine() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("person", NashornCompatTest.Person.class, new NashornCompatTest.Person("Max", 42, 7L, true));
        RuleContext ctx = RuleContext.builder().with(bindings).build();

        for (String name : List.of("JavaScript", "ECMAScript", "graal.js")) {
            // Bean-style access only works with the factory's Nashorn-compat engine; a bare
            // GraalJS engine from the generic wrapper would evaluate ctx.person.name to null.
            Script<Boolean> script = Script.builder().build(name, "ctx.person.name === 'Max' && ctx.person.age >= 18");
            // The script carries the factory's canonical name, so a RuleContext caches a single
            // processor for every spelling instead of one per alias.
            Assertions.assertEquals(GraalJsScriptProcessorFactory.LANGUAGE_NAME, script.getLanguageName(), name);
            Condition condition = Condition.builder().build(script);
            Assertions.assertTrue(condition.isTrue(ctx), name);
        }
    }
}
