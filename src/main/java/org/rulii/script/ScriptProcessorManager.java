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

import org.rulii.lib.apache.commons.logging.Log;
import org.rulii.lib.apache.commons.logging.LogFactory;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.UnrulyException;
import org.rulii.script.jsr223.JSR223ScriptProcessorFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry and lookup service for {@link ScriptProcessorFactory} instances.
 *
 * <p>{@code ScriptProcessorManager} maintains a language-keyed map of factories and
 * provides two discovery mechanisms:
 * <ol>
 *   <li><b>Manual registration</b> — call {@link #register(ScriptProcessorFactory)} to add a
 *       factory programmatically at any time.</li>
 *   <li><b>Service-loader discovery</b> — on the first call to
 *       {@link #getScriptProcessorFactory(String)}, all {@link ScriptProcessorFactory} implementations
 *       listed in {@code META-INF/services/org.rulii.script.ScriptProcessorFactory} whose
 *       {@link ScriptProcessorFactory#isAvailable()} returns {@code true} are automatically loaded.</li>
 * </ol>
 *
 * <p>If no explicitly registered factory is found for a requested language, the manager falls back
 * to creating a generic {@link JSR223ScriptProcessorFactory} wrapping whatever
 * {@link javax.script.ScriptEngine} the JVM's {@link ScriptEngineManager} can provide.
 *
 * <p>{@code ScriptProcessorManager} is a process-wide singleton — obtain the shared instance via
 * {@link #getInstance()}. All registrations and lookups are visible to every caller in the JVM;
 * there is exactly one registry, not one per caller.
 *
 * @author Max Arulananthan
 * @since 1.2
 * @see ScriptProcessorFactory
 * @see ScriptProcessor
 */
public final class ScriptProcessorManager {

    private static final Log LOGGER = LogFactory.getLog(ScriptProcessorManager.class);

    private static final Map<String, ScriptProcessorFactory> factories = new ConcurrentHashMap<>();
    private static final ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
    private static final Object LOCK = new Object();
    private static final ScriptProcessorManager INSTANCE = new ScriptProcessorManager();
    private static volatile boolean initialized = false;

    private ScriptProcessorManager() {
        super();
    }

    /**
     * Returns the single, process-wide {@code ScriptProcessorManager} instance.
     *
     * @return the singleton instance; never null.
     */
    public static ScriptProcessorManager getInstance() {
        return INSTANCE;
    }

    /**
     * Registers a {@link ScriptProcessorFactory} for its declared language.
     *
     * <p>If a factory for the same language name was previously registered, it is replaced.
     *
     * @param factory the factory to register; must not be null.
     */
    public void register(ScriptProcessorFactory factory) {
        Assert.notNull(factory, "factory cannot be null.");
        factories.put(factory.getLanguageName(), factory);
    }

    /**
     * Returns the {@link ScriptProcessorFactory} for the given scripting language.
     *
     * <p>Lookup order:
     * <ol>
     *   <li>Previously registered (or service-loaded) factory.</li>
     *   <li>On first invocation, service-loader discovery is performed and results cached.</li>
     *   <li>Fall-back: a {@link JSR223ScriptProcessorFactory} wrapping the JSR-223 engine
     *       returned by the JVM for {@code languageName}.</li>
     * </ol>
     *
     * @param languageName the engine/language name to look up (e.g. {@code "js"}, {@code "groovy"}).
     * @return the matching factory, or {@code null} if no engine is available for the language.
     */
    public ScriptProcessorFactory getScriptProcessorFactory(String languageName) {
        if (!initialized) load();
        ScriptProcessorFactory result = factories.get(languageName);

        if (result == null) {
            ScriptEngine scriptEngine = scriptEngineManager.getEngineByName(languageName);

            if (scriptEngine != null) {
                result = new JSR223ScriptProcessorFactory(scriptEngine.getFactory());
                factories.put(languageName, result);
            }
        }

        return result;
    }

    /**
     * Assigns an alias to an existing {@link ScriptProcessorFactory} associated with a specific scripting language.
     * The alias can then be used as an alternate name to refer to the same {@link ScriptProcessorFactory}.
     *
     * @param languageName the name of the scripting language for which the {@link ScriptProcessorFactory} is registered; must not be empty.
     * @param aliasName the alias to assign to the {@link ScriptProcessorFactory}; must not be empty.
     * @return the {@link ScriptProcessorFactory} associated with the specified scripting language.
     * @throws IllegalArgumentException if either {@code languageName} or {@code aliasName} is empty.
     * @throws UnrulyException if no {@link ScriptProcessorFactory} is found for the specified {@code languageName}.
     */
    public ScriptProcessorFactory alias(String languageName, String aliasName) {
        Assert.hasText(languageName, "languageName cannot be empty.");
        Assert.hasText(aliasName, "aliasName cannot be empty.");

        ScriptProcessorFactory factory = getScriptProcessorFactory(languageName);

        if (factory == null) throw new UnrulyException("No ScriptProcessorFactory found for language " + languageName);

        factories.put(aliasName, factory);
        return factory;
    }

    /**
     * Discovers and registers all available {@link ScriptProcessorFactory} implementations
     * via the {@link ServiceLoader} mechanism.  Called lazily on the first lookup.
     *
     * <p>Each provider is instantiated and registered independently — a single provider that
     * fails to load (e.g. a stale {@code META-INF/services} entry throwing
     * {@link java.util.ServiceConfigurationError}) is logged and skipped without preventing
     * discovery of the remaining providers.
     */
    private void load() {
        if (initialized) return;

        synchronized (LOCK) {
            if (initialized) return;

            try {
                Iterator<ScriptProcessorFactory> iterator = ServiceLoader.load(ScriptProcessorFactory.class).iterator();

                while (true) {
                    ScriptProcessorFactory factory;

                    try {
                        if (!iterator.hasNext()) break;
                        factory = iterator.next();
                    } catch (Throwable e) {
                        LOGGER.warn("Error loading a ScriptProcessorFactory provider; skipping it", e);
                        continue;
                    }

                    try {
                        if (factory.isAvailable()) register(factory);
                    } catch (Throwable e) {
                        LOGGER.warn("Error registering ScriptProcessorFactory [" + factory + "]", e);
                    }
                }
            } catch (Throwable e) {
                LOGGER.warn("Error loading ScriptProcessorFactory providers", e);
            }

            initialized = true;
        }
    }
}
