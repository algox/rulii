package org.rulii.script;

import org.rulii.lib.spring.util.Assert;

public record ScriptOptions(String bindingsName, String contextName) {

    public static final ScriptOptions DEFAULT = new ScriptOptions("b", "ctx");

    public ScriptOptions {
        Assert.hasText(bindingsName, "bindingsName cannot be empty.");
        Assert.hasText(contextName, "contextName cannot be empty.");
    }
}
