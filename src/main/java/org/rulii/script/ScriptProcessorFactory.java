/*
 * This software is licensed under the Apache 2 license, quoted below.
 *
 * Copyright (c) 1999-2025, Algorithmx Inc.
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

import javax.script.ScriptEngineManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory responsible for finding and managing {@link ScriptProcessor} instances for a given scripting language.
 *
 * <p>On creation, the factory auto-discovers all {@link ScriptProcessor} implementations available on the
 * classpath via {@link ScriptEngineManager}. Additional processors can be registered at any time via
 * {@link #register(ScriptProcessor)}. Lookup is case-insensitive on the language name.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 *   ScriptProcessorFactory factory = ScriptProcessorFactory.create();
 *   ScriptProcessor processor = factory.find("javascript");
 * }</pre>
 *
 * @author Max Arulananthan
 * @since 1.2
 */
public class ScriptProcessorFactory {

    private final Map<String, ScriptProcessor> processors = new ConcurrentHashMap<>();

    private ScriptProcessorFactory() {
        super();
    }

    /**
     * Creates a new {@code ScriptProcessorFactory} using the context class loader and auto-discovers
     * all available scripting engines on the classpath.
     *
     * @return a new instance of {@code ScriptProcessorFactory}.
     */
    public static ScriptProcessorFactory create() {
        return new ScriptProcessorFactory();
    }

    /**
     * Finds a {@link ScriptProcessor} for the given language name.
     *
     * <p>The lookup is case-insensitive. Returns {@code null} if no processor is registered for
     * the specified language.</p>
     *
     * @param language the scripting language name (e.g. {@code "JavaScript"}, {@code "groovy"}); must not be null or empty.
     * @return the matching {@link ScriptProcessor}, or {@code null} if none is found.
     */
    public ScriptProcessor find(String language) {
        Assert.hasText(language, "language cannot be empty.");
        return processors.get(language.toLowerCase());
    }

    /**
     * Returns the {@link ScriptProcessor} for the given language, throwing an exception if none is found.
     *
     * @param language the scripting language name; must not be null or empty.
     * @return the matching {@link ScriptProcessor}.
     * @throws ScriptProcessorNotFoundException if no processor is registered for the given language.
     */
    public ScriptProcessor get(String language) {
        ScriptProcessor processor = find(language);

        if (processor == null) {
            throw new ScriptProcessorNotFoundException(language, getAvailableLanguages());
        }

        return processor;
    }

    /**
     * Registers a custom {@link ScriptProcessor}, keyed on {@link ScriptProcessor#getLanguage()}.
     * If a processor for the same language is already registered it will be replaced.
     *
     * @param processor the processor to register; must not be null.
     */
    public void register(ScriptProcessor processor) {
        Assert.notNull(processor, "processor cannot be null.");
        Assert.hasText(processor.getLanguage(), "processor.getLanguage() cannot be empty.");
        processors.put(processor.getLanguage().toLowerCase(), processor);
    }

    /**
     * Returns {@code true} if a {@link ScriptProcessor} is registered for the given language.
     *
     * @param language the scripting language name; must not be null or empty.
     * @return {@code true} if a processor exists for the language, {@code false} otherwise.
     */
    public boolean supports(String language) {
        Assert.hasText(language, "language cannot be empty.");
        return processors.containsKey(language.toLowerCase());
    }

    /**
     * Returns an unmodifiable list of all currently registered language names (in lower-case).
     *
     * @return list of available language names.
     */
    public List<String> getAvailableLanguages() {
        List<String> result = new ArrayList<>(processors.keySet());
        Collections.sort(result);
        return Collections.unmodifiableList(result);
    }
}