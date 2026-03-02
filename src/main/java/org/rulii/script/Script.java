package org.rulii.script;

import org.rulii.bind.Bindings;

/**
 * Represents a loaded script that is ready for evaluation against a set of {@link Bindings}.
 *
 * <p>A {@code Script} is produced by a {@link ScriptProcessor} from raw source text. Depending on
 * whether the underlying engine supports compilation, the implementation may be a
 * {@link CompiledScript} (pre-compiled for faster repeated evaluation) or a {@link PlainScript}
 * (interpreted on each call to {@link #eval}).</p>
 *
 * @author Max Arulananthan
 * @since 1.2
 * @see ScriptProcessor
 * @see CompiledScript
 * @see PlainScript
 */
public interface Script {

    /**
     * Returns the original source text of this script.
     *
     * @return the script source; never null or empty.
     */
    String getScript();

    /**
     * Returns {@code true} if this script has been pre-compiled by the underlying engine.
     *
     * <p>Compiled scripts avoid repeated parsing overhead and are generally faster for
     * repeated evaluations. The default implementation returns {@code false}.</p>
     *
     * @return {@code true} if pre-compiled, {@code false} otherwise.
     */
    default boolean isCompiled() {
        return false;
    }

    /**
     * Evaluates this script against the supplied {@link Bindings} and returns the result.
     *
     * @param <T>      the expected return type.
     * @param bindings the variable bindings to expose to the script; must not be null.
     * @return the value produced by the script, or {@code null} if the script yields no value.
     * @throws EvaluationException if the script throws an error during evaluation.
     */
    <T> T eval(Bindings bindings);
}
