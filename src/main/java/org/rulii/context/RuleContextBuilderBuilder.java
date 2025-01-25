/*
 * This software is licensed under the Apache 2 license, quoted below.
 *
 * Copyright (c) 1999-2025, Algorithmx Inc.
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
 * RuleContextBuilderBuilder is a utility class that provides methods for building RuleContextBuilder objects with various configurations.
 * It follows the singleton design pattern to ensure only one instance of RuleContextBuilderBuilder exists.
 *
 * @author Max Arulananthan
 * @since 1.0
 *
 */
public class RuleContextBuilderBuilder {

    private static final RuleContextBuilderBuilder instance = new RuleContextBuilderBuilder();

    private RuleContextBuilderBuilder() {
        super();
    }

    /**
     * Retrieves an instance of RuleContextBuilderBuilder.
     *
     * @return The singleton instance of RuleContextBuilderBuilder.
     */
    public static RuleContextBuilderBuilder getInstance() {
        return instance;
    }

    /**
     * Constructs a new RuleContextBuilder with default settings.
     *
     * @return a new RuleContextBuilder object.
     */
    public RuleContextBuilder standard() {
        return new RuleContextBuilder();
    }

    /**
     * Constructs a new RuleContextBuilder with the provided RuleContextOptions.
     *
     * @param options the RuleContextOptions to use for constructing the RuleContextBuilder
     * @return a new RuleContextBuilder object initialized with the provided options
     */
    public RuleContextBuilder with(RuleContextOptions options) {
        return new RuleContextBuilder(options);
    }

    /**
     * Constructs a new RuleContextBuilder with the provided RuleContext.
     *
     * @param context the RuleContext to use for constructing the RuleContextBuilder
     * @return a new RuleContextBuilder object initialized with the provided context
     */
    public RuleContextBuilder with(RuleContext context) {
        return new RuleContextBuilder(context);
    }

    /**
     * Sets the Bindings to use.
     *
     * @param bindings Bindings to use.
     * @return this for fluency.
     */
    public RuleContextBuilder with(Bindings bindings) {
        Assert.notNull(bindings, "bindings cannot be null.");
        RuleContextBuilder result = new RuleContextBuilder();
        result.bindings(bindings);
        return result;
    }

    /**
     * Sets the Bindings to use for building the RuleContextBuilder.
     *
     * @param params the BindingDeclarations to bind to the Bindings.
     * @return a RuleContextBuilder with the provided BindingDeclarations bound to the Bindings.
     */
    public RuleContextBuilder with(BindingDeclaration<?>...params) {
        Bindings bindings = Bindings.builder().standard();
        if (params != null) bindings.bind(params);
        return with(bindings);
    }

    /**
     * Builds a RuleContext object with the default Bindings. This method constructs a RuleContext object with
     * standard Bindings and returns it.
     *
     * @return a RuleContext object with default Bindings.
     */
    public RuleContext build() {
        return with(Bindings.builder().standard()).build();
    }

    /**
     * Builds a new RuleContext object with the provided Bindings.
     *
     * @param bindings the Bindings object to be used for constructing the RuleContext
     * @return a new RuleContext object constructed with the provided Bindings
     */
    public RuleContext build(Bindings bindings) {
        return with(bindings).build();
    }

    /**
     * Builds a RuleContext object with the provided BindingDeclarations.
     *
     * @param params the BindingDeclarations to bind to the Bindings
     * @return a RuleContext object with the provided BindingDeclarations bound to the Bindings
     */
    public RuleContext build(BindingDeclaration<?>...params) {
        return with(params).build();
    }

}
