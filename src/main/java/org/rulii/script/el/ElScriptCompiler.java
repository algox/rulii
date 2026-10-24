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
package org.rulii.script.el;

import jakarta.el.ELException;
import jakarta.el.ExpressionFactory;
import jakarta.el.StandardELContext;
import jakarta.el.ValueExpression;
import org.rulii.lib.spring.util.Assert;
import org.rulii.lib.spring.util.ClassUtils;
import org.rulii.script.BuildScriptException;
import org.rulii.script.Script;
import org.rulii.script.ScriptCompiler;

/**
 * Compiles an EL expression once into an {@link ElScript}. The expression is written in EL's own
 * delimiters ({@code ${...}}); text outside them is literal, as in EL everywhere. The expected type is
 * the script's return type, which EL coerces the result to ({@code boolean} for a condition);
 * {@code void} means any object.
 *
 * @author Max Arulananthan
 * @since 2.2
 */
public class ElScriptCompiler implements ScriptCompiler {

    private final String languageName;

    public ElScriptCompiler(String languageName) {
        super();
        Assert.hasText(languageName, "languageName cannot be empty.");
        this.languageName = languageName;
    }

    @Override
    public String getLanguageName() {
        return languageName;
    }

    @Override
    public <T> Script<T> compile(String script, Class<?> returnType) {
        Assert.hasText(script, "script cannot be empty.");
        Assert.notNull(returnType, "returnType cannot be null.");
        ExpressionFactory factory = ElSupport.expressionFactory();
        Class<?> expected = returnType == void.class || returnType == Void.class ? Object.class : ClassUtils.resolvePrimitiveIfNecessary(returnType);

        try {
            ValueExpression expression = factory.createValueExpression(new StandardELContext(factory), script, expected);
            return new ElScript<>(languageName, script, expression, returnType);
        } catch (ELException e) {
            throw new BuildScriptException(script, "cannot compile EL expression [" + script + "]: " + e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        return "ElScriptCompiler{languageName='" + languageName + "'}";
    }
}
