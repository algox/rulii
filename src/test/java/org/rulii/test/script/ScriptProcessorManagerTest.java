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
package org.rulii.test.script;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.script.Script;
import org.rulii.script.ScriptCompiler;
import org.rulii.script.ScriptProcessor;
import org.rulii.script.ScriptProcessorFactory;
import org.rulii.script.ScriptProcessorManager;

import java.util.UUID;

/**
 * Tests for ScriptProcessorManager.
 *
 * @author Max Arulananthan
 */
public class ScriptProcessorManagerTest {

    public ScriptProcessorManagerTest() {
        super();
    }

    private static ScriptProcessorFactory namedFactory(String language) {
        return new ScriptProcessorFactory() {
            @Override
            public String getLanguageName() {
                return language;
            }

            @Override
            public String getBindingsName() {
                return "ctx";
            }

            @Override
            public ScriptProcessor getScriptProcessor() {
                return new ScriptProcessor() {
                    @Override
                    public String getLanguageName() {
                        return language;
                    }

                    @Override
                    public String getBindingsName() {
                        return "ctx";
                    }

                    @Override
                    public <T> T evaluate(Script<T> script, org.rulii.context.RuleContext ruleContext) {
                        return null;
                    }
                };
            }

            @Override
            public ScriptCompiler getScriptCompiler() {
                return new ScriptCompiler() {
                    @Override
                    public String getLanguageName() {
                        return language;
                    }

                    @Override
                    public <T> Script<T> compile(String script, Class<?> returnType) {
                        return null;
                    }
                };
            }
        };
    }

    @Test
    public void testGetInstance_returnsSameInstanceEveryCall() {
        Assertions.assertSame(ScriptProcessorManager.getInstance(), ScriptProcessorManager.getInstance());
    }

    @Test
    public void testRegister_isVisibleToEveryCallerOfGetInstance() {
        String language = "singleton-test-lang-" + UUID.randomUUID();
        ScriptProcessorFactory factory = namedFactory(language);

        ScriptProcessorManager.getInstance().register(factory);

        // A second, independently-obtained reference to the singleton must see the same registration -
        // there is exactly one global registry, not one per caller.
        Assertions.assertSame(factory, ScriptProcessorManager.getInstance().getScriptProcessorFactory(language));
    }

    @Test
    public void testUnregister_removesLanguageAndAliasesByIdentity() {
        String language = "unregister-test-lang-" + UUID.randomUUID();
        String alias = language + "-alias";
        ScriptProcessorFactory factory = namedFactory(language);
        ScriptProcessorManager manager = ScriptProcessorManager.getInstance();

        manager.register(factory);
        manager.alias(language, alias);
        Assertions.assertSame(factory, manager.getScriptProcessorFactory(alias));

        Assertions.assertTrue(manager.unregister(factory));
        Assertions.assertNull(manager.getScriptProcessorFactory(language), "the language name must be released");
        Assertions.assertNull(manager.getScriptProcessorFactory(alias), "every alias of the factory must be released");
        Assertions.assertFalse(manager.unregister(factory), "a second unregister must report nothing removed");
    }

    @Test
    public void testUnregister_leavesNameTakenOverByAnotherFactoryAlone() {
        String language = "unregister-takeover-lang-" + UUID.randomUUID();
        ScriptProcessorFactory first = namedFactory(language);
        ScriptProcessorFactory second = namedFactory(language);
        ScriptProcessorManager manager = ScriptProcessorManager.getInstance();

        manager.register(first);
        manager.register(second);

        try {
            Assertions.assertFalse(manager.unregister(first), "first no longer holds any name");
            Assertions.assertSame(second, manager.getScriptProcessorFactory(language),
                    "unregistering a displaced factory must not clobber its successor");
        } finally {
            manager.unregister(second);
        }
    }

    @Test
    public void testUnregister_restoresServiceLoaderDefaultForDisplacedLanguage() {
        ScriptProcessorManager manager = ScriptProcessorManager.getInstance();
        // "js" ships via META-INF/services in rulii core; find out what the stock factory is.
        ScriptProcessorFactory stock = manager.getScriptProcessorFactory("js");
        Assertions.assertNotNull(stock, "test needs a service-loaded language to displace");

        ScriptProcessorFactory override = namedFactory("js");
        manager.register(override);
        Assertions.assertSame(override, manager.getScriptProcessorFactory("js"));

        Assertions.assertTrue(manager.unregister(override));
        ScriptProcessorFactory restored = manager.getScriptProcessorFactory("js");
        Assertions.assertNotNull(restored, "the stock factory must come back after the override is removed");
        Assertions.assertNotSame(override, restored);
        Assertions.assertEquals(stock.getClass(), restored.getClass());
    }

    @Test
    public void testUnregister_nullRejected() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> ScriptProcessorManager.getInstance().unregister(null));
    }

    @Test
    public void testScriptTextResolver_defaultIsIdentity() {
        Assertions.assertEquals("${unresolved}",
                ScriptProcessorManager.getInstance().resolveScriptText("${unresolved}"));
        Assertions.assertNull(ScriptProcessorManager.getInstance().resolveScriptText(null));
    }

    @Test
    public void testScriptTextResolver_isAppliedByScriptBuilder() {
        String language = "resolver-test-lang-" + UUID.randomUUID();
        // Compiler that records the text it receives so we can assert pre-processing happened.
        StringBuilder seen = new StringBuilder();
        ScriptProcessorFactory factory = new ScriptProcessorFactory() {
            @Override
            public String getLanguageName() { return language; }
            @Override
            public String getBindingsName() { return "ctx"; }
            @Override
            public ScriptProcessor getScriptProcessor() { return namedFactory(language).getScriptProcessor(); }
            @Override
            public ScriptCompiler getScriptCompiler() {
                return new ScriptCompiler() {
                    @Override
                    public String getLanguageName() { return language; }
                    @Override
                    public <T> Script<T> compile(String script, Class<?> returnType) {
                        seen.setLength(0);
                        seen.append(script);
                        return null;
                    }
                };
            }
        };
        ScriptProcessorManager.getInstance().register(factory);

        try {
            ScriptProcessorManager.getInstance().setScriptTextResolver(text -> text.replace("${answer}", "42"));
            Script.builder().build(language, "value == ${answer}");
            Assertions.assertEquals("value == 42", seen.toString(),
                    "the resolver must run before the compiler sees the text");
        } finally {
            ScriptProcessorManager.getInstance().setScriptTextResolver(java.util.function.UnaryOperator.identity());
        }

        Script.builder().build(language, "value == ${answer}");
        Assertions.assertEquals("value == ${answer}", seen.toString(),
                "resetting to identity must stop pre-processing");
    }

    @Test
    public void testScriptTextResolver_nullResolverRejected() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> ScriptProcessorManager.getInstance().setScriptTextResolver(null));
    }

    @Test
    public void testClearScriptTextResolver_onlyResetsWhenExpectedResolverIsActive() {
        java.util.function.UnaryOperator<String> mine = text -> text + "-mine";
        java.util.function.UnaryOperator<String> other = text -> text + "-other";

        try {
            ScriptProcessorManager.getInstance().setScriptTextResolver(mine);
            Assertions.assertFalse(ScriptProcessorManager.getInstance().clearScriptTextResolver(other),
                    "clearing with a non-active resolver must be a no-op");
            Assertions.assertEquals("x-mine", ScriptProcessorManager.getInstance().resolveScriptText("x"));

            Assertions.assertTrue(ScriptProcessorManager.getInstance().clearScriptTextResolver(mine));
            Assertions.assertEquals("x", ScriptProcessorManager.getInstance().resolveScriptText("x"),
                    "clearing the active resolver must restore identity");
        } finally {
            ScriptProcessorManager.getInstance().setScriptTextResolver(java.util.function.UnaryOperator.identity());
        }
    }
}
