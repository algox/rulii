/*
 * This software is licensed under the Apache 2 license, quoted below.
 *
 * Copyright (c) 1999-2021, Algorithmx Inc.
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
 * Thrown when an error occurs while loading or compiling a script.
 *
 * <p>This exception is raised by {@link ScriptProcessor#load} when the underlying JSR-223
 * {@link javax.script.Compilable} engine rejects the script source (e.g. due to a syntax error).
 * The original source text is preserved for diagnostic purposes.</p>
 *
 * @author Max Arulananthan
 * @since 1.2
 * @see ScriptProcessor#load
 */
public class LoadScriptException extends UnrulyException {

    private final String script;

    /**
     * Creates a new {@code LoadScriptException}.
     *
     * @param script  the script source that could not be loaded; may be null.
     * @param message a human-readable description of the error.
     */
    public LoadScriptException(String script, String message) {
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
    public LoadScriptException(String script, String message, Throwable cause) {
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
