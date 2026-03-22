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
package org.rulii.script;

import org.rulii.lib.spring.util.Assert;
import org.rulii.model.UnrulyException;
import org.rulii.script.jsr223.JSR223ScriptProcessorFactory;

import javax.script.ScriptEngineManager;
import java.util.HashMap;
import java.util.Map;

public class DefaultScriptProcessorRegistry implements ScriptProcessorRegistry {

    private final Map<String, ScriptProcessorFactory> processors = new HashMap<>();
    private final ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
    private final boolean autoIncludeJSR223;

    /**
     * Constructs a new {@code DefaultScriptProcessorRegistry}.
     *
     * @param autoIncludeJSR223 when {@code true}, unknown language names trigger an
     *                          automatic search through the JSR-223
     *                          {@code ScriptEngineManager}; when {@code false}, only
     *                          explicitly registered processors are used.
     */
    public DefaultScriptProcessorRegistry(boolean autoIncludeJSR223) {
        super();
        this.autoIncludeJSR223 = autoIncludeJSR223;
    }

    /**
     * Registers a {@link ScriptProcessorFactory} with the registry, associating it
     * with the scripting language name provided by the factory.
     *
     * @param factory the {@link ScriptProcessorFactory} to register; must not be null.
     *                The factory's language name is used as the key for mapping.
     * @throws IllegalArgumentException if the provided factory is null.
     */
    @Override
    public void register(ScriptProcessorFactory factory) {
        Assert.notNull(factory, "factory cannot be null.");
        processors.put(factory.getLanguageName(), factory);
    }

    /**
     * Deregisters a {@link ScriptProcessorFactory} from the registry, removing any
     * association with its scripting language name.
     *
     * @param factory the {@link ScriptProcessorFactory} to deregister; must not be null.
     *                The factory's language name is used as the key for removal.
     * @throws IllegalArgumentException if the provided factory is null.
     */
    @Override
    public void deregister(ScriptProcessorFactory factory) {
        Assert.notNull(factory, "factory cannot be null.");
        processors.remove(factory.getLanguageName());
    }

    /**
     * Retrieves a {@link ScriptProcessorFactory} for the specified scripting language name.
     * If the language is not already registered and the {@code autoIncludeJSR223} flag is enabled,
     * the method attempts to locate a compatible factory using the JSR-223 {@code ScriptEngineManager}.
     * If no matching factory is found, an exception is thrown.
     *
     * @param languageName the name of the scripting language for which a {@link ScriptProcessorFactory}
     *                     is requested; must not be null or empty.
     * @return the {@link ScriptProcessorFactory} associated with the specified language name.
     * @throws IllegalArgumentException if {@code languageName} is null or empty.
     * @throws UnrulyException if the scripting language is unsupported or cannot be resolved.
     */
    @Override
    public ScriptProcessorFactory getScriptProcessorFactory(String languageName) {
        Assert.hasText(languageName, "languageName cannot be empty.");

        ScriptProcessorFactory result = processors.get(languageName);

        if (result != null) return result;

        if (!autoIncludeJSR223) throw new UnrulyException("Script Language [" + languageName + "] is not supported.");

        ScriptProcessorFactory jsr223ScriptProcessorFactory = getJSR223ScriptProcessor(languageName);

        if (jsr223ScriptProcessorFactory == null) throw new UnrulyException("Script Language [" + languageName + "] is not supported.");

        register(jsr223ScriptProcessorFactory);

        return jsr223ScriptProcessorFactory;
    }

    private ScriptProcessorFactory getJSR223ScriptProcessor(String languageName) {
        return scriptEngineManager.getEngineFactories()
                .stream()
                .filter(f -> f.getLanguageName().equalsIgnoreCase(languageName))
                .findFirst()
                .map(JSR223ScriptProcessorFactory::new)
                .orElse(null);
    }
}
