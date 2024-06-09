/*
 * This software is licensed under the Apache 2 license, quoted below.
 *
 * Copyright (c) 1999-2024, Algorithmx Inc.
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
package org.rulii.lang;

import org.rulii.bind.BindingDeclaration;
import org.rulii.bind.Bindings;
import org.rulii.context.RuleContext;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.Identifiable;
import org.rulii.model.UnrulyException;
import org.rulii.rule.RuleExecutionException;

/**
 * Anything that can be executed given a RuleContext.
 *
 * @param <T> return type.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
@FunctionalInterface
public interface Runnable<T> extends Identifiable {

    /**
     * Executes the runnable within the given context.
     *
     * @param ruleContext non-null rule context.
     * @return result.
     * @throws UnrulyException errors during execution.
     */
    T run(RuleContext ruleContext) throws UnrulyException;

    /**
     * Derives all the arguments, creates a default RuleContext and executes this Rule.
     *
     * @param bindings Bindings.
     * @return result.
     * @throws UnrulyException thrown if there are any runtime errors during the execution.
     */
    default T run(Bindings bindings) throws RuleExecutionException {
        Assert.notNull(bindings, "bindings cannot be null.");
        return run(RuleContext.builder().build(bindings));
    }

    /**
     * Derives all the arguments, creates a default RuleContext and executes this Rule.
     *
     * @param params Parameters.
     * @return result.
     * @throws UnrulyException thrown if there are any runtime errors during the execution.
     */
    default T run(BindingDeclaration<?>...params) throws RuleExecutionException {
        Bindings bindings = Bindings.builder().standard(params);
        return run(RuleContext.builder().build(bindings));
    }

    @Override
    default String getName() {
        return "anonymous-runnable";
    }

    /**
     * Underlying Target Object.
     * @return target object.
     */
    @SuppressWarnings("unchecked")
    default <R> R getTarget() {
        return (R) this;
    }
}
