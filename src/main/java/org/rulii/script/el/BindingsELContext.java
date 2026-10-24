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

import jakarta.el.ArrayELResolver;
import jakarta.el.BeanELResolver;
import jakarta.el.CompositeELResolver;
import jakarta.el.ELContext;
import jakarta.el.ELResolver;
import jakarta.el.ExpressionFactory;
import jakarta.el.FunctionMapper;
import jakarta.el.ListELResolver;
import jakarta.el.MapELResolver;
import jakarta.el.ResourceBundleELResolver;
import jakarta.el.StaticFieldELResolver;
import jakarta.el.ValueExpression;
import jakarta.el.VariableMapper;
import org.rulii.bind.Bindings;
import org.rulii.lib.spring.util.Assert;

import java.lang.reflect.Method;

/**
 * The EL context of one evaluation: the {@link BindingsELResolver} first, then the standard resolvers
 * (stream, static fields, maps, resource bundles, lists, arrays, beans). No functions and no EL
 * variables are defined, so an identifier is always a binding, and nothing can be created or assigned
 * from an expression.
 *
 * @author Max Arulananthan
 * @since 2.2
 */
public class BindingsELContext extends ELContext {

    private static final FunctionMapper NO_FUNCTIONS = new FunctionMapper() {
        @Override
        public Method resolveFunction(String prefix, String localName) {
            return null;
        }
    };

    private static final VariableMapper NO_VARIABLES = new VariableMapper() {
        @Override
        public ValueExpression resolveVariable(String variable) {
            return null;
        }

        @Override
        public ValueExpression setVariable(String variable, ValueExpression expression) {
            return null;
        }
    };

    private final ELResolver resolver;

    /**
     * Creates the context.
     *
     * @param factory the expression factory, for its stream resolver.
     * @param bindings the bindings of the evaluation.
     * @param bindingsName the root name the bindings are reachable under.
     */
    public BindingsELContext(ExpressionFactory factory, Bindings bindings, String bindingsName) {
        super();
        Assert.notNull(factory, "factory cannot be null.");
        CompositeELResolver composite = new CompositeELResolver();
        composite.add(new BindingsELResolver(bindings, bindingsName));
        ELResolver stream = factory.getStreamELResolver();
        if (stream != null) composite.add(stream);
        composite.add(new StaticFieldELResolver());
        composite.add(new MapELResolver());
        composite.add(new ResourceBundleELResolver());
        composite.add(new ListELResolver());
        composite.add(new ArrayELResolver());
        composite.add(new BeanELResolver());
        this.resolver = composite;
    }

    @Override
    public ELResolver getELResolver() {
        return resolver;
    }

    @Override
    public FunctionMapper getFunctionMapper() {
        return NO_FUNCTIONS;
    }

    @Override
    public VariableMapper getVariableMapper() {
        return NO_VARIABLES;
    }
}
