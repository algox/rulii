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
import org.rulii.script.jsr223.JSR223ScriptProcessor;

import javax.script.ScriptEngineManager;
import java.util.HashMap;
import java.util.Map;

/**
 * Default {@link HashMap}-backed implementation of {@link ScriptProcessorRegistry}.
 *
 * <p>Processors are stored by their {@link ScriptProcessor#getLanguageName() language name}.
 * When {@code autoIncludeJSR223} is {@code true} (the default when constructed via
 * {@link ScriptProcessorRegistry#builder()}), a first lookup for an unknown language
 * will transparently attempt to locate a matching JSR-223 engine via
 * {@link ScriptEngineManager}, wrap it in a
 * {@link org.rulii.script.jsr223.JSR223ScriptProcessor}, register it, and return it.
 * Subsequent lookups for the same language will be served from the map directly.
 *
 * <p>This class is not thread-safe. If concurrent access is required, external
 * synchronization must be applied.
 *
 * @author Max Arulananthan
 * @since 1.2
 * @see ScriptProcessorRegistry
 * @see ScriptProcessorRegistryBuilder
 * @see org.rulii.script.jsr223.JSR223ScriptProcessor
 */
public class DefaultScriptProcessorRegistry implements ScriptProcessorRegistry {

    private final Map<String, ScriptProcessor> processors = new HashMap<>();
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
     * Registers a {@link ScriptProcessor}, keyed by
     * {@link ScriptProcessor#getLanguageName()}.  Any previously registered
     * processor for the same language name is silently replaced.
     *
     * @param scriptProcessor the processor to register; must not be null.
     */
    @Override
    public void register(ScriptProcessor scriptProcessor) {
        Assert.notNull(scriptProcessor, "scriptProcessor cannot be null.");
        processors.put(scriptProcessor.getLanguageName(), scriptProcessor);
    }

    /**
     * Removes the given {@link ScriptProcessor} from this registry by its language
     * name.  If no matching processor is registered, this method has no effect.
     *
     * @param scriptProcessor the processor to remove; must not be null.
     */
    @Override
    public void deregister(ScriptProcessor scriptProcessor) {
        Assert.notNull(scriptProcessor, "scriptProcessor cannot be null.");
        processors.remove(scriptProcessor.getLanguageName());
    }

    /**
     * Returns the {@link ScriptProcessor} registered for the given language name.
     *
     * <p>If no processor has been explicitly registered and {@code autoIncludeJSR223}
     * is {@code true}, the registry will attempt to locate a JSR-223 engine whose
     * {@link javax.script.ScriptEngineFactory#getLanguageName()} matches
     * {@code languageName} (case-insensitive).  If found, the engine is wrapped in a
     * {@link JSR223ScriptProcessor}, registered for future use, and returned.
     *
     * @param languageName the scripting language name (e.g. {@code "ECMAScript"}); must not be null or empty.
     * @return the processor for the requested language; never null.
     * @throws UnrulyException if no processor is available for the requested language.
     */
    @Override
    public ScriptProcessor getScriptProcessor(String languageName) {
        Assert.hasText(languageName, "languageName cannot be empty.");

        ScriptProcessor result = processors.get(languageName);

        if (result != null) return result;

        if (!autoIncludeJSR223) throw new UnrulyException("Script Language [" + languageName + "] is not supported.");

        ScriptProcessor jsr223ScriptProcessor = getJSR223ScriptProcessor(languageName);

        if (jsr223ScriptProcessor == null) throw new UnrulyException("Script Language [" + languageName + "] is not supported.");

        register(jsr223ScriptProcessor);

        return jsr223ScriptProcessor;
    }

    /**
     * Searches the JSR-223 {@code ScriptEngineManager} for a factory whose language
     * name matches {@code languageName} (case-insensitive) and, if found, returns a
     * new {@link JSR223ScriptProcessor} wrapping a fresh engine from that factory.
     *
     * @param languageName the language name to search for.
     * @return a new {@link JSR223ScriptProcessor}, or {@code null} if no matching
     *         JSR-223 engine is available.
     */
    private ScriptProcessor getJSR223ScriptProcessor(String languageName) {
        return scriptEngineManager.getEngineFactories()
                .stream()
                .filter(f -> f.getLanguageName().equalsIgnoreCase(languageName))
                .findFirst()
                .map(scriptEngineFactory -> new JSR223ScriptProcessor(scriptEngineFactory.getScriptEngine()))
                .orElse(null);
    }
}
