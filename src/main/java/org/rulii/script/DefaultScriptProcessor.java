package org.rulii.script;

import org.rulii.lib.spring.util.Assert;

import javax.script.Compilable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

/**
 * Default {@link ScriptProcessor} implementation backed by a JSR-223 {@link ScriptEngine}.
 *
 * <p>On {@link #load}, if the engine implements {@link Compilable} the script source is compiled
 * eagerly and a {@link CompiledScript} is returned for efficient repeated evaluation. Otherwise a
 * {@link PlainScript} that interprets the source on each call is returned.</p>
 *
 * @author Max Arulananthan
 * @since 1.2
 * @see ScriptProcessorFactory
 */
public class DefaultScriptProcessor implements ScriptProcessor {

    private final ScriptEngine scriptEngine;
    private final String language;
    private final Compilable compilable;

    public DefaultScriptProcessor(ScriptEngine scriptEngine) {
        this(scriptEngine, null);
    }

    public DefaultScriptProcessor(ScriptEngine scriptEngine, String language) {
        super();
        Assert.notNull(scriptEngine, "scriptEngine cannot be null.");
        this.scriptEngine = scriptEngine;
        this.language = language != null ? language : scriptEngine.getFactory().getLanguageName();
        this.compilable = scriptEngine instanceof Compilable ? (Compilable) scriptEngine : null;
    }

    /**
     * Loads the given script source, compiling it eagerly if the engine supports
     * {@link Compilable}, or wrapping it in a {@link PlainScript} otherwise.
     *
     * @param script the script source text; must not be null or empty.
     * @return a {@link CompiledScript} or {@link PlainScript} ready for evaluation; never null.
     * @throws LoadScriptException if compilation fails.
     */
    @Override
    public Script load(String script) {
        Assert.hasText(script, "script cannot be empty.");

        if (compilable != null) {
            try {
                javax.script.CompiledScript compiledScript = compilable.compile(script);
                return new CompiledScript(script, scriptEngine, compiledScript);
            } catch (ScriptException e) {
                throw new LoadScriptException(script, e.getMessage(), e);
            }
        }

        return new PlainScript(scriptEngine, script);
    }

    /**
     * Returns the language name reported by the underlying engine factory
     * (e.g. {@code "ECMAScript"}).
     *
     * @return the language name; never null.
     */
    @Override
    public final String getLanguage() {
        return language;
    }

    /**
     * Returns the language version reported by the underlying engine factory
     * (e.g. {@code "ECMA - 262 Edition 5.1"}).
     *
     * @return the language version; never null.
     */
    @Override
    public final String getLanguageVersion() {
        return scriptEngine.getFactory().getLanguageVersion();
    }
}
