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
 * Fluent builder for constructing a {@link ScriptProcessorRegistry}.
 *
 * <p>Obtained via {@link ScriptProcessorRegistry#builder()}, this builder lets
 * callers configure registry options before creating a
 * {@link DefaultScriptProcessorRegistry}:
 *
 * <pre>{@code
 * ScriptProcessorRegistry registry = ScriptProcessorRegistry.builder()
 *         .autoIncludeJSR223(true)
 *         .build();
 * }</pre>
 *
 * @author Max Arulananthan
 * @since 1.2
 * @see ScriptProcessorRegistry
 * @see DefaultScriptProcessorRegistry
 */
public class ScriptProcessorRegistryBuilder {

    private boolean autoIncludeJSR223 = true;

    /**
     * Constructs a new {@code ScriptProcessorRegistryBuilder} with default settings
     * (JSR-223 auto-discovery enabled).
     */
    public ScriptProcessorRegistryBuilder() {
        super();
    }

    /**
     * Configures whether the registry should automatically discover and register
     * JSR-223 script engines when a requested language has no explicitly registered
     * processor.
     *
     * <p>When {@code true} (the default), the registry will consult the
     * {@code javax.script.ScriptEngineManager} on the first request for an unknown
     * language and, if a matching engine is found, wrap it in a
     * {@link org.rulii.script.jsr223.JSR223ScriptProcessor} and register it for
     * future use.
     *
     * @param autoIncludeJSR223 {@code true} to enable JSR-223 auto-discovery; {@code false} to disable it.
     * @return this builder, for method chaining.
     */
    public ScriptProcessorRegistryBuilder autoIncludeJSR223(boolean autoIncludeJSR223) {
        this.autoIncludeJSR223 = autoIncludeJSR223;
        return this;
    }

    /**
     * Builds and returns a new {@link ScriptProcessorRegistry} configured with the
     * options set on this builder.
     *
     * @return a new {@link DefaultScriptProcessorRegistry}; never null.
     */
    public ScriptProcessorRegistry build() {
        return new DefaultScriptProcessorRegistry(autoIncludeJSR223);
    }
}
