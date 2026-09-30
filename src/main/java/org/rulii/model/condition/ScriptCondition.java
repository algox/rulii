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
package org.rulii.model.condition;

import org.rulii.lib.spring.util.Assert;
import org.rulii.model.ExpressionInfo;
import org.rulii.model.MethodDefinition;
import org.rulii.script.Script;

/**
 * A Condition backed by a {@link Script}. Runs exactly like a {@link DefaultCondition}; in addition
 * it keeps the script so that {@link #getExpression()} can report the language and source text.
 *
 * @author Max Arulananthan
 * @since 2.1
 */
public class ScriptCondition extends DefaultCondition {

    private final Script<?> script;

    ScriptCondition(Script<?> script, Object target, String name, MethodDefinition methodDefinition) {
        super(target, name, methodDefinition);
        Assert.notNull(script, "script cannot be null.");
        this.script = script;
    }

    /**
     * The script this condition evaluates.
     *
     * @return script; never null.
     */
    public Script<?> getScript() {
        return script;
    }

    @Override
    public ExpressionInfo getExpression() {
        return ExpressionInfo.script(script.getLanguageName(), script.getSourceText());
    }
}
