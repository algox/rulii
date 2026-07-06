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
}
