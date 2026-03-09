package org.rulii.script;

import org.rulii.model.Runnable;

public interface Script<T> extends Runnable<T> {

    static ScriptBuilderBuilder builder() {
        return ScriptBuilderBuilder.getInstance();
    }

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

}
