package org.rulii.script;

import org.rulii.bind.Bindings;
import org.rulii.lib.spring.util.Assert;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;
import java.util.Objects;

/**
 * A {@link Script} that evaluates source text directly via a {@link ScriptEngine} without
 * pre-compilation.
 *
 * <p>Instances are created by {@link DefaultScriptProcessor} when the underlying engine does not
 * implement {@link javax.script.Compilable}. The source text is re-parsed and interpreted on every
 * call to {@link #eval}.</p>
 *
 * @author Max Arulananthan
 * @since 1.2
 * @see CompiledScript
 * @see DefaultScriptProcessor
 */
public class PlainScript implements Script {

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

    /**
     * Evaluates the script source against the supplied {@link Bindings} and returns the result.
     *
     * @param <T>      the expected return type.
     * @param bindings the variable bindings to expose to the script; must not be null.
     * @return the value produced by the script, or {@code null} if it yields no value.
     * @throws EvaluationException if the script throws an error during evaluation.
     */
    @SuppressWarnings("unchecked")
    public <T> T eval(Bindings bindings) {
        try {
            ScriptContext scriptContext = new SimpleScriptContext();
            scriptContext.setReader(scriptEngine.getContext().getReader());
            scriptContext.setWriter(scriptEngine.getContext().getWriter());
            scriptContext.setErrorWriter(scriptEngine.getContext().getErrorWriter());
            scriptContext.setBindings(new DelegatingBindings(bindings), ScriptContext.GLOBAL_SCOPE);
            return (T) scriptEngine.eval(script, scriptContext);
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
        PlainScript that = (PlainScript) o;
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
