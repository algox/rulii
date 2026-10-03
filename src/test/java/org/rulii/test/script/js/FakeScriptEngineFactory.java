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
package org.rulii.test.script.js;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import java.io.Reader;
import java.util.Collections;
import java.util.List;

/**
 * A JSR-223 engine factory registered with {@code ScriptEngineManager} through
 * {@code META-INF/services} in the test resources. It answers to several names and hands out a
 * GraalJS engine, which lets tests exercise {@link org.rulii.script.ScriptProcessorManager}'s
 * fall-back discovery path and its alias registration with an engine rulii has no factory for.
 */
public class FakeScriptEngineFactory implements ScriptEngineFactory {

    public static final String LANGUAGE_NAME = "rulii-fake";
    public static final List<String> NAMES = List.of(LANGUAGE_NAME, "RuliiFake", "rulii-fake-js");

    public FakeScriptEngineFactory() {
        super();
    }

    @Override public String getEngineName() { return "rulii-fake-engine"; }
    @Override public String getEngineVersion() { return "1"; }
    @Override public List<String> getExtensions() { return Collections.emptyList(); }
    @Override public List<String> getMimeTypes() { return Collections.emptyList(); }
    @Override public List<String> getNames() { return NAMES; }
    @Override public String getLanguageName() { return LANGUAGE_NAME; }
    @Override public String getLanguageVersion() { return "1"; }
    @Override public Object getParameter(String key) { return null; }
    @Override public String getMethodCallSyntax(String obj, String m, String... args) { return obj + "." + m + "()"; }
    @Override public String getOutputStatement(String toDisplay) { return "print(" + toDisplay + ")"; }
    @Override public String getProgram(String... statements) { return String.join(";", statements); }
    @Override public ScriptEngine getScriptEngine() { return new FakeEngine(TestScriptUtils.createEngine(), this); }

    /**
     * Delegates everything to a GraalJS engine but reports {@link FakeScriptEngineFactory} as its
     * factory, which is what {@code ScriptProcessorManager} wraps on the fall-back path.
     */
    static final class FakeEngine implements ScriptEngine, Compilable {
        private final ScriptEngine delegate;
        private final ScriptEngineFactory factory;

        FakeEngine(ScriptEngine delegate, ScriptEngineFactory factory) {
            super();
            this.delegate = delegate;
            this.factory = factory;
        }

        @Override public ScriptEngineFactory getFactory() { return factory; }
        @Override public CompiledScript compile(String script) throws ScriptException { return ((Compilable) delegate).compile(script); }
        @Override public CompiledScript compile(Reader script) throws ScriptException { return ((Compilable) delegate).compile(script); }
        @Override public Object eval(String script, ScriptContext context) throws ScriptException { return delegate.eval(script, context); }
        @Override public Object eval(Reader reader, ScriptContext context) throws ScriptException { return delegate.eval(reader, context); }
        @Override public Object eval(String script) throws ScriptException { return delegate.eval(script); }
        @Override public Object eval(Reader reader) throws ScriptException { return delegate.eval(reader); }
        @Override public Object eval(String script, Bindings n) throws ScriptException { return delegate.eval(script, n); }
        @Override public Object eval(Reader reader, Bindings n) throws ScriptException { return delegate.eval(reader, n); }
        @Override public void put(String key, Object value) { delegate.put(key, value); }
        @Override public Object get(String key) { return delegate.get(key); }
        @Override public Bindings getBindings(int scope) { return delegate.getBindings(scope); }
        @Override public void setBindings(Bindings bindings, int scope) { delegate.setBindings(bindings, scope); }
        @Override public Bindings createBindings() { return delegate.createBindings(); }
        @Override public ScriptContext getContext() { return delegate.getContext(); }
        @Override public void setContext(ScriptContext context) { delegate.setContext(context); }
    }
}
