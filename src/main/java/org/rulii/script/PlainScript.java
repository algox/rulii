package org.rulii.script;

import org.rulii.context.RuleContext;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.UnrulyException;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.util.Objects;

public class PlainScript<T> implements Script<T> {

    private final ScriptEngine scriptEngine;
    private final String script;

    /**
     * Creates a new {@code PlainScript}.
     *
     * @param scriptEngine the JSR-223 engine used to evaluate the script; must not be null.
     * @param script       the script source text; must not be null or empty.
     */
    public PlainScript(ScriptEngine scriptEngine, String script) {
        super();
        Assert.notNull(scriptEngine, "scriptEngine cannot be null.");
        Assert.hasText(script, "script cannot be empty.");
        this.scriptEngine = scriptEngine;
        this.script = script;
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
     * Returns the original source text of this script.
     *
     * @return the script source; never null or empty.
     */
    @Override
    public final String getScript() {
        return this.script;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PlainScript<?> that = (PlainScript<?>) o;
        return Objects.equals(script, that.script);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(script);
    }

    @Override
    public String toString() {
        return "PlainScript{" +
                "script='" + script + '\'' +
                '}';
    }
}
