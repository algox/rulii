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

import org.rulii.model.UnrulyException;

import java.util.List;

/**
 * Thrown when no {@link ScriptProcessor} is registered for a requested language.
 *
 * @author Max Arulananthan
 * @since 1.2
 */
public class ScriptProcessorNotFoundException extends UnrulyException {

    private final String language;
    private final List<String> availableLanguages;

    /**
     * Creates a new {@code ScriptProcessorNotFoundException}.
     *
     * @param language           the language name for which no processor was found; must not be null.
     * @param availableLanguages the list of languages that do have registered processors; must not be null.
     */
    public ScriptProcessorNotFoundException(String language, List<String> availableLanguages) {
        super("No ScriptProcessor found for language [" + language + "]. Available languages: " + availableLanguages);
        this.language = language;
        this.availableLanguages = availableLanguages;
    }

    /**
     * Returns the language name that was requested but could not be resolved.
     *
     * @return the unresolved language name; never null.
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Returns the list of language names for which processors are currently registered.
     *
     * @return an unmodifiable list of available language names; never null.
     */
    public List<String> getAvailableLanguages() {
        return availableLanguages;
    }
}