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

import jakarta.el.ValueExpression;
import org.rulii.lib.spring.util.Assert;
import org.rulii.script.AbstractScript;

/**
 * A compiled EL expression.
 *
 * @param <T> the result type.
 * @author Max Arulananthan
 * @since 2.2
 */
public class ElScript<T> extends AbstractScript<T> {

    private final ValueExpression expression;

    public ElScript(String languageName, String script, ValueExpression expression, Class<?> returnType) {
        super(languageName, script, returnType);
        Assert.notNull(expression, "expression cannot be null.");
        this.expression = expression;
    }

    /**
     * The compiled expression, evaluated against an EL context per evaluation.
     *
     * @return the value expression.
     */
    public ValueExpression getExpression() {
        return expression;
    }

    @Override
    public String toString() {
        return "ElScript{languageName='" + getLanguageName() + "', script='" + getScript() + "'}";
    }
}
