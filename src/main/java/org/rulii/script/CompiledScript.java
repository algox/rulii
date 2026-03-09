package org.rulii.script;

import org.rulii.context.RuleContext;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.UnrulyException;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.util.Objects;

public class CompiledScript<T> implements Script<T> {

    private final String script;
    private final ScriptEngine scriptEngine;
    private final javax.script.CompiledScript compiledScript;

    /**
     * Creates a new {@code CompiledScript}.
     *
     * @param script         the original source text; must not be null or empty.
     * @param scriptEngine   the engine that produced the compiled form; must not be null.
     * @param compiledScript the pre-compiled script object; must not be null.
     */
    public CompiledScript(String script, ScriptEngine scriptEngine, javax.script.CompiledScript compiledScript) {
        super();
        Assert.hasText(script, "script cannot be empty.");
        Assert.notNull(compiledScript, "compiledScript cannot be null.");
        Assert.notNull(scriptEngine, "scriptEngine cannot be null.");
        this.script = script;
        this.scriptEngine = scriptEngine;
        this.compiledScript = compiledScript;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T run(RuleContext ruleContext) throws UnrulyException {
        try {
            return (T) scriptEngine.eval(script, ruleContext.getScriptContext(scriptEngine));
        } catch (ScriptException e) {
            throw new EvaluationException(script, e.getMessage(), e);
        }
    }

    /**
     * Always returns {@code true} because this script has been pre-compiled.
     *
     * @return {@code true}.
     */
    @Override
    public final boolean isCompiled() {
        return true;
    }

    /**
     * Returns the original source text of this script.
     *
     * @return the script source; never null or empty.
     */
    @Override
    public final String getScript() {
        return script;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CompiledScript<?> that = (CompiledScript<?>) o;
        return Objects.equals(script, that.script) && Objects.equals(compiledScript, that.compiledScript);
    }

    @Override
    public int hashCode() {
        return Objects.hash(script, compiledScript);
    }

    @Override
    public String toString() {
        return "CompiledScript{" +
                "script='" + script + '\'' +
                ", compiledScript=" + compiledScript +
                '}';
    }
}
