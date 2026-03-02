package org.rulii.script;

import org.rulii.bind.Bindings;
import org.rulii.lib.spring.util.Assert;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;
import java.util.Objects;

/**
 * A {@link Script} backed by a pre-compiled JSR-223 {@link javax.script.CompiledScript}.
 *
 * <p>Instances are created by {@link DefaultScriptProcessor} when the underlying
 * {@link ScriptEngine} implements {@link javax.script.Compilable}. Pre-compiling avoids repeated
 * parsing overhead and is generally faster for scripts that are evaluated many times.</p>
 *
 * @author Max Arulananthan
 * @since 1.2
 * @see PlainScript
 * @see DefaultScriptProcessor
 */
public class CompiledScript implements Script {

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

    /**
     * Evaluates the pre-compiled script against the supplied {@link Bindings} and returns the result.
     *
     * @param <T>      the expected return type.
     * @param bindings the variable bindings to expose to the script; must not be null.
     * @return the value produced by the script, or {@code null} if it yields no value.
     * @throws EvaluationException if the script throws an error during evaluation.
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T eval(Bindings bindings) {
        Assert.notNull(script, "script cannot be null.");

        try {
            ScriptContext scriptContext = new SimpleScriptContext();
            scriptContext.setReader(scriptEngine.getContext().getReader());
            scriptContext.setWriter(scriptEngine.getContext().getWriter());
            scriptContext.setErrorWriter(scriptEngine.getContext().getErrorWriter());
            scriptContext.setBindings(new DelegatingBindings(bindings), ScriptContext.GLOBAL_SCOPE);
            return (T) compiledScript.eval(scriptContext);
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
        CompiledScript that = (CompiledScript) o;
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
