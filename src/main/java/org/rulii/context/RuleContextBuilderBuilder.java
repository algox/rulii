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
package org.rulii.context;

import org.rulii.bind.BindingDeclaration;
import org.rulii.bind.Bindings;
import org.rulii.lib.spring.util.Assert;

/**
 * Entry point for the fluent rule-context building DSL; provides factory methods for creating
 * {@link RuleContextBuilder} instances.
 *
 * <p>This class is a singleton obtained via {@link #getInstance()} or through
 * {@link RuleContext#builder()}.
 *
 * @author Max Arulananthan
 * @since 1.0
 * @see RuleContextBuilder
 * @see RuleContext#builder()
 */
public class RuleContextBuilderBuilder {

    private static final RuleContextBuilderBuilder instance = new RuleContextBuilderBuilder();

    private RuleContextBuilderBuilder() {
        super();
    }

    /**
     * Returns the singleton instance of {@code RuleContextBuilderBuilder}.
     *
     * @return the singleton instance; never null.
     */
    public static RuleContextBuilderBuilder getInstance() {
        return instance;
    }

    /**
     * Returns a new {@link RuleContextBuilder} pre-populated with {@link RuleContextOptions#standard()} defaults.
     *
     * @return a new builder; never null.
     */
    public RuleContextBuilder standard() {
        return new RuleContextBuilder();
    }

    /**
     * Returns a new {@link RuleContextBuilder} pre-populated from the given options.
     *
     * @param options the options to copy; must not be null.
     * @return a new builder; never null.
     */
    public RuleContextBuilder with(RuleContextOptions options) {
        return new RuleContextBuilder(options);
    }

    /**
     * Returns a new {@link RuleContextBuilder} pre-populated from an existing {@link RuleContext},
     * preserving all its settings.
     *
     * @param context the context to copy; must not be null.
     * @return a new builder; never null.
     */
    public RuleContextBuilder with(RuleContext context) {
        return new RuleContextBuilder(context);
    }

    /**
     * Returns a new {@link RuleContextBuilder} with the given bindings set as the global scope.
     *
     * @param bindings the bindings to use; must not be null.
     * @return a new builder; never null.
     */
    public RuleContextBuilder with(Bindings bindings) {
        Assert.notNull(bindings, "bindings cannot be null.");
        RuleContextBuilder result = new RuleContextBuilder();
        result.bindings(bindings);
        return result;
    }

    /**
     * Returns a new {@link RuleContextBuilder} with a standard {@link Bindings} pre-populated
     * from the given declarations.
     *
     * @param params the binding declarations to add; may be null or empty.
     * @return a new builder; never null.
     */
    public RuleContextBuilder with(BindingDeclaration<?>...params) {
        Bindings bindings = Bindings.builder().standard();
        if (params != null) bindings.bind(params);
        return with(bindings);
    }

    /**
     * Builds and returns a {@link RuleContext} with an empty standard {@link Bindings}.
     *
     * @return a new context; never null.
     */
    public RuleContext build() {
        return with(Bindings.builder().standard()).build();
    }

    /**
     * Builds and returns a {@link RuleContext} using the given bindings as the global scope.
     *
     * @param bindings the bindings to use; must not be null.
     * @return a new context; never null.
     */
    public RuleContext build(Bindings bindings) {
        return with(bindings).build();
    }

    /**
     * Builds and returns a {@link RuleContext} with a standard {@link Bindings} pre-populated
     * from the given declarations.
     *
     * @param params the binding declarations to add; may be null or empty.
     * @return a new context; never null.
     */
    public RuleContext build(BindingDeclaration<?>...params) {
        return with(params).build();
    }

}
