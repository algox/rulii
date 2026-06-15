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
package org.rulii.model;

import org.rulii.bind.BindingDeclaration;
import org.rulii.bind.Bindings;
import org.rulii.context.RuleContext;
import org.rulii.lib.spring.util.Assert;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * Anything that can be executed given a RuleContext.
 *
 * @param <T> return type.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
@FunctionalInterface
public interface Runnable<T> extends Identifiable, Serializable {

    @Serial
    long serialVersionUID = 1L;

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
    default T run(Bindings bindings) throws UnrulyException {
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
    default T run(BindingDeclaration<?>...params) throws UnrulyException {
        Bindings bindings = Bindings.builder().standard(params);
        return run(RuleContext.builder().build(bindings));
    }

    /**
     * Derives all the arguments, creates a default RuleContext and executes this Runnable.
     *
     * <p>The {@code params} argument is dispatched as follows:
     * <ul>
     *   <li>{@link BindingDeclaration} — delegates to {@link #run(BindingDeclaration[])}.</li>
     *   <li>{@link Bindings} — delegates to {@link #run(Bindings)}.</li>
     *   <li>{@link Map Map&lt;String, Object&gt;} — each map entry becomes a binding.</li>
     *   <li>Any other object — its JavaBean properties are reflected and each property becomes a binding.</li>
     * </ul>
     *
     * <p><strong>Overload-resolution note:</strong> a stored {@code BindingDeclaration<?>} variable
     * passed here routes to this method (not the varargs overload) because Java prefers non-varargs.
     * The {@code instanceof} check inside this method corrects the dispatch automatically.
     *
     * @param params non-null parameter source; a {@code BindingDeclaration}, {@code Bindings},
     *               {@code Map<String, Object>}, or a JavaBean POJO.
     * @return result.
     * @throws IllegalArgumentException if {@code params} is null.
     * @throws UnrulyException thrown if there are any runtime errors during the execution.
     */
    @SuppressWarnings("unchecked")
    default T run(Object params) throws UnrulyException {
        Assert.notNull(params, "params cannot be null.");

        // Delegate when needed
        if (params instanceof BindingDeclaration<?> bindingDeclaration) return run(new BindingDeclaration<?>[] { bindingDeclaration });
        if (params instanceof Bindings bindings) return run(bindings);

        Bindings bindings = Bindings.builder().standard();

        if (params instanceof Map<?, ?> map) {
            bindings.loadMap((Map<String, Object>) map);
        } else {
            bindings.loadProperties(params);
        }

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
