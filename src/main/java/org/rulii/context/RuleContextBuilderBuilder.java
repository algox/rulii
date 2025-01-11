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

public class RuleContextBuilderBuilder {

    private static final RuleContextBuilderBuilder instance = new RuleContextBuilderBuilder();

    private RuleContextBuilderBuilder() {
        super();
    }

    public static RuleContextBuilderBuilder getInstance() {
        return instance;
    }

    public RuleContextBuilder standard() {
        return new RuleContextBuilder();
    }

    public RuleContextBuilder with(RuleContextOptions options) {
        return new RuleContextBuilder(options);
    }

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

    public RuleContextBuilder with(BindingDeclaration...params) {
        Bindings bindings = Bindings.builder().standard();
        if (params != null) bindings.bind(params);
        return with(bindings);
    }

    public RuleContext build() {
        return with(Bindings.builder().standard()).build();
    }

    public RuleContext build(Bindings bindings) {
        return with(bindings).build();
    }

    public RuleContext build(BindingDeclaration...params) {
        return with(params).build();
    }

}
