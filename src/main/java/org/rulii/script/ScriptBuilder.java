package org.rulii.script;

import org.rulii.lib.spring.util.Assert;

import javax.script.Compilable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

public class ScriptBuilder {

    private final ScriptEngine engine;
    private final String script;

    ScriptBuilder(ScriptEngine engine, String script) {
        super();
        Assert.notNull(engine, "engine cannot be null.");
        Assert.hasText(script, "script cannot be empty.");
        this.engine = engine;
        this.script = script;
    }

    public <T> Script<T> build() {

        if (engine instanceof Compilable compilable) {
            try {
                return new CompiledScript<>(script, engine, compilable.compile(script));
            } catch (ScriptException e) {
                throw new BuildScriptException(script, e.getMessage(), e);
            }
        }

        return new PlainScript<>(engine, script);
    }
}
