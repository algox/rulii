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

public interface ScriptProcessorRegistry {

    /**
     * Returns a new {@link ScriptProcessorRegistryBuilder} for constructing a
     * {@code ScriptProcessorRegistry}.
     *
     * @return a fresh builder instance; never null.
     */
    static ScriptProcessorRegistryBuilder builder() {
        return new ScriptProcessorRegistryBuilder();
    }

    /**
     * Registers a {@link ScriptProcessorFactory} for creating {@link ScriptProcessor}
     * instances associated with a specific scripting language.
     *
     * @param factory the {@link ScriptProcessorFactory} to register; must not be null.
     *                The factory provides details such as the scripting language name
     *                and the logic for creating {@link ScriptProcessor} instances.
     */
    void register(ScriptProcessorFactory factory);

    /**
     * Deregisters a previously registered {@link ScriptProcessorFactory} from the registry.
     * This method is used to remove a factory, thereby disabling the creation of
     * {@link ScriptProcessor} instances for the scripting language associated with the factory.
     *
     * @param factory the {@link ScriptProcessorFactory} to deregister; must not be null.
     *                The factory to be removed should have been registered previously.
     */
    void deregister(ScriptProcessorFactory factory);

    /**
     * Retrieves a {@link ScriptProcessorFactory} associated with the specified scripting language.
     * If no factory is registered for the given language name, this method may return null or
     * throw an exception, depending on the implementation.
     *
     * @param languageName the name of the scripting language for which the factory is requested; must not be null or empty.
     * @return the {@link ScriptProcessorFactory} associated with the specified language, or null if no factory is registered.
     */
    ScriptProcessorFactory getScriptProcessorFactory(String languageName);

}
