package org.rulii.script;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

public final class ScriptBuilderBuilder {

    private static final ScriptEngineManager manager = new ScriptEngineManager();

    /**
     * Singleton instance of ScriptBuilderBuilder class used to access and construct RuleSet objects.
     */
    private static final ScriptBuilderBuilder instance = new ScriptBuilderBuilder();

    private ScriptBuilderBuilder() {
        super();
    }

    /**
     * Returns the singleton instance of ScriptBuilderBuilder for accessing and constructing Script objects.
     *
     * @return the singleton instance of ScriptBuilderBuilder
     */
    public static ScriptBuilderBuilder getInstance() {
        return instance;
    }

    public <T> Script<T> build(String name, String script) {
        return new ScriptBuilder(manager.getEngineByName(name), script).build();
    }

    public <T> Script<T> build(ScriptEngine engine, String script) {
        return new ScriptBuilder(engine, script).build();
    }

    public <T> Script<T> build(ScriptEngineManager manager, String name, String script) {
        return new ScriptBuilder(manager.getEngineByName(name), script).build();
    }

    public <T> Script<T> build(ScriptEngineFactory factory, String script) {
        return new ScriptBuilder(factory.getScriptEngine(), script).build();
    }
}
