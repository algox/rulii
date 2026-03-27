package org.rulii.script;

import org.rulii.context.RuleContext;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.UnrulyException;

import java.util.Collections;
import java.util.List;

public abstract class AbstractScript<T> implements Script<T> {

    private final String languageName;
    private final String script;
    private final List<ScriptParameter> scriptParameters;

    /**
     * Creates a new Script.
     *
     * @param languageName     the scripting language name; must not be null or empty.
     * @param script           the raw script source text; must not be null or empty.
     * @param scriptParameters the parameter declarations; may be null (treated as empty list).
     */
    protected AbstractScript(String languageName, String script, List<ScriptParameter> scriptParameters) {
        super();
        Assert.hasText(languageName, "languageName cannot be empty.");
        Assert.hasText(script, "script cannot be empty.");
        this.languageName = languageName;
        this.script = script;
        this.scriptParameters = scriptParameters != null ? Collections.unmodifiableList(scriptParameters) : List.of();
    }

    @Override
    public T run(RuleContext ruleContext) throws UnrulyException {
        return ruleContext.getScriptProcessor(getLanguageName()).evaluate(this, ruleContext);
    }

    @Override
    public String getLanguageName() {
        return languageName;
    }

    @Override
    public String getScript() {
        return script;
    }

    @Override
    public List<ScriptParameter> getScriptParameters() {
        return scriptParameters;
    }

    @Override
    public String toString() {
        return "JSR223Script{" +
                "languageName='" + languageName + '\'' +
                ", script='" + script + '\'' +
                ", scriptParameters=" + scriptParameters +
                '}';
    }
}
