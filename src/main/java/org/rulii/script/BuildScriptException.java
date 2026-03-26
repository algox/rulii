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

package org.rulii.script;

import org.rulii.model.UnrulyException;

/**
 * Thrown when a script source string cannot be compiled into an executable {@link Script}.
 *
 * <p>This exception is typically raised by a {@link ScriptCompiler} implementation when the
 * underlying script engine reports a syntax or compilation error.  The offending source text
 * is available via {@link #getScript()}.
 *
 * @author Max Arulananthan
 * @since 1.2
 * @see ScriptCompiler
 * @see EvaluationException
 */
public class BuildScriptException extends UnrulyException {

    private final String script;

    /**
     * Creates a new {@code LoadScriptException}.
     *
     * @param script  the script source that could not be loaded; may be null.
     * @param message a human-readable description of the error.
     */
    public BuildScriptException(String script, String message) {
        super(message);
        this.script = script;
    }

    /**
     * Creates a new {@code LoadScriptException} with a root cause.
     *
     * @param script  the script source that could not be loaded; may be null.
     * @param message a human-readable description of the error.
     * @param cause   the underlying exception; may be null.
     */
    public BuildScriptException(String script, String message, Throwable cause) {
        super(message, cause);
        this.script = script;
    }

    /**
     * Returns the script source text that triggered this exception.
     *
     * @return the script source; may be null.
     */
    public String getScript() {
        return script;
    }
}
