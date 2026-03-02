package org.rulii.script;

/**
 * Processes scripts for a specific scripting language.
 *
 * <p>A {@code ScriptProcessor} is responsible for loading raw script source text into executable
 * {@link Script} instances. If the backing engine supports compilation (JSR-223
 * {@link javax.script.Compilable}), the returned {@link Script} will be pre-compiled for efficient
 * repeated evaluation; otherwise a plain interpreted script is returned.</p>
 *
 * <p>Instances are obtained through {@link ScriptProcessorFactory}:</p>
 * <pre>{@code
 *   ScriptProcessorFactory factory = ScriptProcessorFactory.create();
 *   ScriptProcessor processor = factory.get("javascript");
 *   Script script = processor.load("x + y");
 * }</pre>
 *
 * @author Max Arulananthan
 * @since 1.2
 * @see ScriptProcessorFactory
 * @see Script
 */
public interface ScriptProcessor {

    /**
     * Returns the name of the scripting language handled by this processor (e.g. {@code "ECMAScript"},
     * {@code "groovy"}).
     *
     * @return the language name; never null or empty.
     */
    String getLanguage();

    /**
     * Returns the version string of the scripting language handled by this processor
     * (e.g. {@code "ECMA - 262 Edition 5.1"}).
     *
     * @return the language version; never null or empty.
     */
    String getLanguageVersion();

    /**
     * Loads the given script source text and returns a {@link Script} ready for evaluation.
     *
     * <p>If the underlying engine supports {@link javax.script.Compilable}, the script will be
     * compiled eagerly and a {@link CompiledScript} is returned. Otherwise a {@link PlainScript}
     * that interprets the source on each {@link Script#eval} call is returned.</p>
     *
     * @param script the script source text; must not be null or empty.
     * @return a {@link Script} ready for evaluation; never null.
     * @throws LoadScriptException if the script source cannot be compiled.
     */
    Script load(String script);
}
