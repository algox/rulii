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
import org.rulii.lib.spring.util.StringUtils;
import org.rulii.model.UnrulyException;
import org.rulii.script.jsr223.JSR223ScriptProcessorFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;

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
 * <p>The manager also holds the process-wide <b>default scripting language</b>: the language a
 * script is compiled in when none is named (see {@link ScriptBuilderBuilder#with(String)}). It is
 * declared, never inferred: through {@link #setDefaultLanguage(String)} or the system property
 * {@value #DEFAULT_LANGUAGE_PROPERTY}; without either there is no default.
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

    /**
     * System property naming the default scripting language ({@value}). Read by
     * {@link #getDefaultLanguage()} while no language is set through {@link #setDefaultLanguage(String)},
     * and kept once it names one; a name no factory or engine answers to fails that call.
     */
    public static final String DEFAULT_LANGUAGE_PROPERTY = "rulii.script.defaultLanguage";

    private static final Map<String, ScriptProcessorFactory> factories = new ConcurrentHashMap<>();
    private static final ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
    private static final Object LOCK = new Object();
    private static final ScriptProcessorManager INSTANCE = new ScriptProcessorManager();
    private static volatile boolean initialized = false;
    private static volatile UnaryOperator<String> scriptTextResolver = UnaryOperator.identity();
    private static volatile String defaultLanguage = null;
    private static volatile boolean defaultLanguageSeeded = false;

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
     * Registers a {@link ScriptProcessorFactory} for its declared language and its aliases.
     *
     * <p>If a factory for the same language name was previously registered, it is replaced.
     * Each name in {@link ScriptProcessorFactory#getAliases()} is registered too, but only
     * where no factory holds that name yet: an explicit registration always wins over an alias.
     *
     * @param factory the factory to register; must not be null.
     */
    public void register(ScriptProcessorFactory factory) {
        Assert.notNull(factory, "factory cannot be null.");
        factories.put(factory.getLanguageName(), factory);
        registerAliases(factory);
    }

    /**
     * Removes a previously registered {@link ScriptProcessorFactory}: every name (language and
     * aliases) that currently maps to this exact instance is dropped. Lets an integration that
     * registered a factory tied to its own lifecycle (e.g. a Spring bean) take it back out when
     * that lifecycle ends, so the factory and whatever it references do not outlive it in this
     * process-wide registry. A name taken over by a different factory since is left alone.
     *
     * <p>Names this factory had displaced are refilled from service-loader discovery (once
     * discovery has run), so unregistering an override restores the stock factory for that
     * language rather than leaving it without one.
     *
     * @param factory the factory to remove, matched by identity; must not be null.
     * @return true if at least one name mapped to this factory; false if it was not registered.
     */
    public boolean unregister(ScriptProcessorFactory factory) {
        Assert.notNull(factory, "factory cannot be null.");

        synchronized (LOCK) {
            boolean removed = factories.entrySet().removeIf(entry -> entry.getValue() == factory);
            if (removed && initialized) discover(false);
            return removed;
        }
    }

    /**
     * Registers {@code factory} under each of its {@link ScriptProcessorFactory#getAliases() aliases}
     * that is not already taken.
     */
    private static void registerAliases(ScriptProcessorFactory factory) {
        Collection<String> aliases = factory.getAliases();
        if (aliases == null) return;

        for (String alias : aliases) {
            if (StringUtils.hasText(alias)) factories.putIfAbsent(alias, factory);
        }
    }

    /**
     * Sets the script-text resolver applied to every script's source text before it is
     * compiled (see {@code ScriptBuilderBuilder#with}). Integrations use this to
     * pre-process script text — e.g. the Spring integration resolves
     * {@code ${property:default}} placeholders against the application environment.
     *
     * <p>The default is the identity function (no pre-processing). Like the factory
     * registrations, the resolver is process-wide state.
     *
     * @param resolver the resolver to apply; must not be null — pass
     *                 {@link UnaryOperator#identity()} to remove a previously set resolver.
     */
    public void setScriptTextResolver(UnaryOperator<String> resolver) {
        Assert.notNull(resolver, "resolver cannot be null.");
        scriptTextResolver = resolver;
    }

    /**
     * Resets the script-text resolver to the identity function, but only when the
     * currently active resolver is the given one. Lets an integration tear down the
     * resolver it installed without clobbering one installed later by someone else
     * (e.g. a newer application context in the same JVM).
     *
     * @param expected the resolver believed to be active; must not be null.
     * @return true if the resolver was reset; false if a different resolver was active.
     */
    public boolean clearScriptTextResolver(UnaryOperator<String> expected) {
        Assert.notNull(expected, "expected cannot be null.");

        synchronized (LOCK) {
            if (scriptTextResolver != expected) return false;
            scriptTextResolver = UnaryOperator.identity();
            return true;
        }
    }

    /**
     * Applies the configured script-text resolver to the given script source text.
     *
     * @param script the raw script text; may be null.
     * @return the resolved text (unchanged when no resolver is configured).
     */
    public String resolveScriptText(String script) {
        return script == null ? null : scriptTextResolver.apply(script);
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
                // The engine's own language name and every other spelling it answers to map onto
                // this same wrapper, so a later lookup under "Groovy" after "groovy" does not
                // build a second one. Names already taken are left alone.
                factories.putIfAbsent(result.getLanguageName(), result);
                registerAliases(result);
            }
        }

        return result;
    }

    /**
     * Returns the names of the scripting languages a script can be compiled in right now: the
     * language name of every registered (or service-loaded) factory, plus the language name of
     * every JSR-223 engine the JVM's {@link ScriptEngineManager} reports that no registered factory
     * already covers (an engine is covered when any name it answers to is a registered name or
     * alias, so GraalJS registered as {@code js} is listed once, as {@code js}). Aliases are not
     * listed. Discovery runs first, as for a lookup.
     *
     * @return the available language names, sorted; never null, unmodifiable, computed per call.
     */
    public Set<String> getAvailableLanguages() {
        if (!initialized) load();

        Set<String> result = new TreeSet<>();

        for (ScriptProcessorFactory factory : factories.values()) {
            result.add(factory.getLanguageName());
        }

        try {
            for (ScriptEngineFactory engineFactory : scriptEngineManager.getEngineFactories()) {
                try {
                    if (isCovered(engineFactory)) continue;
                    result.add(engineFactory.getLanguageName());
                } catch (Throwable e) {
                    LOGGER.warn("Error reading a JSR-223 ScriptEngineFactory; skipping it", e);
                }
            }
        } catch (Throwable e) {
            LOGGER.warn("Error listing JSR-223 ScriptEngineFactories", e);
        }

        return Collections.unmodifiableSet(result);
    }

    /**
     * Whether a registered factory already answers to the given engine: its language name or any
     * of the names it registers with {@link ScriptEngineManager} is a registered name or alias.
     */
    private static boolean isCovered(ScriptEngineFactory engineFactory) {
        if (factories.containsKey(engineFactory.getLanguageName())) return true;

        List<String> names = engineFactory.getNames();
        if (names == null) return false;

        for (String name : names) {
            if (factories.containsKey(name)) return true;
        }

        return false;
    }

    /**
     * Returns the default scripting language: the one set through {@link #setDefaultLanguage(String)},
     * else the one the system property {@value #DEFAULT_LANGUAGE_PROPERTY} names, else null. The
     * property is read on every call that finds nothing declared; once it names a language that
     * language is the default, and the property is not read again until
     * {@link #clearDefaultLanguage(String)}.
     *
     * @return the default language name, or null when none is declared.
     * @throws UnrulyException if the system property names a language no factory or engine is available for.
     */
    public String getDefaultLanguage() {
        if (!defaultLanguageSeeded) seedDefaultLanguage();
        return defaultLanguage;
    }

    /**
     * Sets the default scripting language. The name is resolved through
     * {@link #getScriptProcessorFactory(String)}, so a registered name and a JSR-223 engine name
     * are both accepted and a misspelt one fails here. Takes precedence over the system property.
     * Like the factory registrations, the default is process-wide state.
     *
     * @param languageName the language name; must not be empty.
     * @throws UnrulyException if no factory or engine is available for the language.
     */
    public void setDefaultLanguage(String languageName) {
        Assert.hasText(languageName, "languageName cannot be empty.");

        synchronized (LOCK) {
            defaultLanguage = requireAvailable(languageName, "Default scripting language");
            defaultLanguageSeeded = true;
        }
    }

    /**
     * Clears the default scripting language, but only when the currently active default is the
     * given one. Lets an integration tear down the default it installed without clobbering one
     * installed later by someone else (e.g. a newer application context in the same JVM). After
     * clearing, the system property is read again on the next {@link #getDefaultLanguage()}.
     *
     * @param expected the language name believed to be the active default; must not be empty.
     * @return true if the default was cleared; false if a different default (or none) was active.
     */
    public boolean clearDefaultLanguage(String expected) {
        Assert.hasText(expected, "expected cannot be empty.");

        synchronized (LOCK) {
            if (!expected.equals(defaultLanguage)) return false;
            defaultLanguage = null;
            defaultLanguageSeeded = false;
            return true;
        }
    }

    /**
     * Reads the system property into the default language. A property that names a language seeds
     * the default once; an absent property leaves it unseeded, so a property set later is still
     * seen. A property naming an unknown language throws and leaves the default unseeded, so every
     * later read fails the same way rather than silently running without a default.
     */
    private void seedDefaultLanguage() {
        synchronized (LOCK) {
            if (defaultLanguageSeeded) return;

            String property = System.getProperty(DEFAULT_LANGUAGE_PROPERTY);
            if (!StringUtils.hasText(property)) return;

            defaultLanguage = requireAvailable(property.trim(), "System property [" + DEFAULT_LANGUAGE_PROPERTY + "]");
            defaultLanguageSeeded = true;
        }
    }

    /**
     * Returns {@code languageName} if a factory or engine is available for it; throws otherwise,
     * naming who declared it and listing {@link #getAvailableLanguages()}.
     */
    private String requireAvailable(String languageName, String declaredBy) {
        if (getScriptProcessorFactory(languageName) != null) return languageName;

        throw new UnrulyException(declaredBy + " names scripting language [" + languageName
                + "] but no ScriptProcessorFactory or JSR-223 engine is available for it. Available languages: "
                + getAvailableLanguages());
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
            discover(true);
            initialized = true;
        }
    }

    /**
     * Runs service-loader discovery. With {@code replace} the discovered factories take over
     * their names (initial load); without it they only fill names nobody holds (refilling
     * after {@link #unregister}). Callers hold {@link #LOCK}.
     *
     * @param replace whether discovered factories replace existing registrations.
     */
    private void discover(boolean replace) {
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
                    if (!factory.isAvailable()) continue;
                    if (replace) register(factory);
                    else registerIfAbsent(factory);
                } catch (Throwable e) {
                    LOGGER.warn("Error registering ScriptProcessorFactory [" + factory + "]", e);
                }
            }
        } catch (Throwable e) {
            LOGGER.warn("Error loading ScriptProcessorFactory providers", e);
        }
    }

    /**
     * Registers {@code factory} for its language and aliases, but only under names that no
     * factory currently holds.
     */
    private static void registerIfAbsent(ScriptProcessorFactory factory) {
        ScriptProcessorFactory canonical = factories.get(factory.getLanguageName());

        if (canonical == null) {
            Collection<String> aliases = factory.getAliases();

            if (aliases != null) {
                for (String alias : aliases) {
                    if (!StringUtils.hasText(alias)) continue;
                    ScriptProcessorFactory existing = factories.get(alias);
                    if (existing == null) continue;
                    if (!factory.getLanguageName().equals(existing.getLanguageName())) continue;
                    canonical = existing;
                    break;
                }
            }
        }

        if (canonical == null) canonical = factory;

        factories.putIfAbsent(factory.getLanguageName(), canonical);

        Collection<String> aliases = factory.getAliases();
        if (aliases == null) return;

        for (String alias : aliases) {
            if (StringUtils.hasText(alias)) factories.putIfAbsent(alias, canonical);
        }
    }
}
