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
package org.rulii.script.el;

import org.rulii.lib.spring.util.Assert;
import org.rulii.lib.spring.util.ClassUtils;
import org.rulii.script.ScriptCompiler;
import org.rulii.script.ScriptOptions;
import org.rulii.script.ScriptProcessor;
import org.rulii.script.ScriptProcessorFactory;

import java.util.Collection;
import java.util.List;

/**
 * Jakarta Expression Language as a rulii scripting language, registered as {@code jakarta-el} (alias
 * {@code jel}; the plain {@code el} names Spring Expression Language in rulii-spring). Available when the Jakarta EL API and an implementation of it (Expressly, Tomcat's
 * {@code tomcat-embed-el}, ...) are on the classpath; both are {@code provided} dependencies of rulii,
 * so the application brings them, as it does the GraalJS and Janino engines.
 *
 * <p>An EL script is an expression in EL's own delimiters, {@code ${ctx.age >= 18}}. The bindings are
 * reachable two ways: through the root object the other rulii languages use, {@code ctx} by default
 * ({@code ${ctx.age}}), and, since EL resolves an identifier with no base through its resolver chain
 * rather than through a scope, as bare identifiers ({@code ${age}}). A binding whose name is an EL
 * reserved word ({@code empty}, {@code and}, ...) is reachable through the root object in bracket form only,
 * {@code ${ctx['empty']}}, since EL does not accept a reserved word as an identifier anywhere. Beyond that
 * the standard resolvers apply: bean properties, map keys, list and array indexes, method calls.</p>
 *
 * <p>Expressions are compiled once ({@link ElScriptCompiler}) and evaluated against an EL context built
 * per evaluation ({@link ElScriptProcessor}), so a compiled script is safe to share between threads.
 * Note that EL's delimiters are the same {@code ${...}} a script-text resolver may treat as
 * placeholders ({@code ScriptProcessorManager.setScriptTextResolver}); an application installing such a
 * resolver must make it leave EL text alone.</p>
 *
 * @author Max Arulananthan
 * @since 2.2
 */
public class ElScriptProcessorFactory implements ScriptProcessorFactory {

    public static final String LANGUAGE_NAME = "jakarta-el";

    private static final boolean available = ClassUtils.isPresent("jakarta.el.ExpressionFactory",
            ElScriptProcessorFactory.class.getClassLoader()) && ElSupport.hasImplementation();

    private final String languageName;
    private final String bindingsName;

    public ElScriptProcessorFactory() {
        this(LANGUAGE_NAME, ScriptOptions.DEFAULT.bindingsName());
    }

    public ElScriptProcessorFactory(String languageName, String bindingsName) {
        super();
        Assert.hasText(languageName, "languageName cannot be empty.");
        Assert.hasText(bindingsName, "bindingsName cannot be empty.");
        this.languageName = languageName;
        this.bindingsName = bindingsName;
    }

    /**
     * Whether the Jakarta EL API and an implementation are on the classpath.
     */
    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public String getLanguageName() {
        return languageName;
    }

    @Override
    public Collection<String> getAliases() {
        return LANGUAGE_NAME.equals(languageName) ? List.of("jel") : List.of();
    }

    @Override
    public String getBindingsName() {
        return bindingsName;
    }

    @Override
    public ScriptProcessor getScriptProcessor() {
        return new ElScriptProcessor(languageName, bindingsName);
    }

    @Override
    public ScriptCompiler getScriptCompiler() {
        return new ElScriptCompiler(languageName);
    }

    @Override
    public String toString() {
        return "ElScriptProcessorFactory{languageName='" + languageName + "', bindingsName='" + bindingsName + "', available=" + available + '}';
    }
}
