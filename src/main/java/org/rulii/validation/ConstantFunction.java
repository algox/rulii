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
package org.rulii.validation;

import org.rulii.context.RuleContext;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.Definable;
import org.rulii.model.MethodDefinition;
import org.rulii.model.UnrulyException;
import org.rulii.model.function.Function;

import java.lang.reflect.Method;

/**
 * A {@link Function} that always returns the value it was created with: what {@code Validators.value(value)}
 * hands out. It returns the value directly instead of running a function built over a lambda.
 *
 * @author Max Arulananthan
 * @since 2.3
 */
public final class ConstantFunction implements Function<Object>, Definable<MethodDefinition> {

    private static final String NAME        = "value";
    private static final String DESCRIPTION = "constant value";
    private static final Method RUN         = runMethod();

    private final Object value;
    // Rebuilt by readResolve: a MethodDefinition holds a Method and cannot be written.
    private final transient MethodDefinition definition;

    /**
     * Creates a function that returns the given value.
     *
     * @param value the value to return; may be null
     */
    public ConstantFunction(Object value) {
        super();
        this.value = value;
        // The value itself is left out of the description: it is often the data under validation.
        this.definition = ValueFunctionDefinition.of(RUN, NAME, DESCRIPTION);
    }

    @Override
    public Object run(RuleContext ruleContext) throws UnrulyException {
        Assert.notNull(ruleContext, "context cannot be null.");
        return value;
    }

    @Override
    public MethodDefinition getDefinition() {
        return definition;
    }

    @Override
    public String getName() {
        return definition.getName();
    }

    @Override
    public String getDescription() {
        return definition.getDescription();
    }

    @Override
    public String toString() {
        return getDescription();
    }

    private Object readResolve() {
        return new ConstantFunction(value);
    }

    private static Method runMethod() {
        try {
            return ConstantFunction.class.getDeclaredMethod("run", RuleContext.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }
}
