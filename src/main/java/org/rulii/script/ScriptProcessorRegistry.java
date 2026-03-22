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

/**
 * Registry that maps scripting-language names to their corresponding
 * {@link ScriptProcessor} implementations.
 *
 * <p>A {@code ScriptProcessorRegistry} is held by the
 * {@link org.rulii.context.RuleContext} and consulted whenever a {@link Script}
 * is run.  Processors may be registered explicitly via {@link #register} or,
 * when auto-discovery is enabled in the default implementation, looked up
 * automatically through the JSR-223 {@code ScriptEngineManager}.
 *
 * <p>New registry instances are created through the fluent builder API:
 * <pre>{@code
 * ScriptProcessorRegistry registry = ScriptProcessorRegistry.builder()
 *         .autoIncludeJSR223(true)
 *         .build();
 * }</pre>
 *
 * @author Max Arulananthan
 * @since 1.2
 * @see ScriptProcessor
 * @see ScriptProcessorRegistryBuilder
 * @see DefaultScriptProcessorRegistry
 */
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
     * Registers a {@link ScriptProcessor} with this registry, keyed by
     * {@link ScriptProcessor#getLanguageName()}.  Any previously registered
     * processor for the same language name is replaced.
     *
     * @param scriptProcessor the processor to register; must not be null.
     */
    void register(ScriptProcessor scriptProcessor);

    /**
     * Removes the given {@link ScriptProcessor} from this registry.
     * If no processor with the same language name is registered, this method
     * has no effect.
     *
     * @param scriptProcessor the processor to remove; must not be null.
     */
    void deregister(ScriptProcessor scriptProcessor);

    /**
     * Returns the {@link ScriptProcessor} registered for the given language name.
     *
     * <p>If no processor has been explicitly registered and auto-discovery is
     * enabled, the default implementation will attempt to locate a matching
     * JSR-223 engine, create a processor for it, register it, and return it.
     *
     * @param languageName the scripting language name (e.g. {@code "ECMAScript"}); must not be null or empty.
     * @return the processor for the requested language; never null.
     * @throws org.rulii.model.UnrulyException if no processor is available for the requested language.
     */
    ScriptProcessor getScriptProcessor(String languageName);

}
