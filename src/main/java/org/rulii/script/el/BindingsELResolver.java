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

import jakarta.el.ELContext;
import jakarta.el.ELResolver;
import jakarta.el.PropertyNotWritableException;
import org.rulii.bind.Binding;
import org.rulii.bind.Bindings;
import org.rulii.lib.spring.util.Assert;

/**
 * Resolves rulii bindings for EL. An identifier with no base resolves to the binding of that name,
 * or to the bindings themselves under the root name ({@code ctx}); a property of the root object
 * resolves to the binding of that name as well, so {@code ${age}} and {@code ${ctx.age}} read the same
 * value. Anything else is left to the standard resolvers. Bindings are read-only from EL.
 *
 * @author Max Arulananthan
 * @since 2.2
 */
public class BindingsELResolver extends ELResolver {

    private final Bindings bindings;
    private final String bindingsName;

    public BindingsELResolver(Bindings bindings, String bindingsName) {
        super();
        Assert.notNull(bindings, "bindings cannot be null.");
        Assert.hasText(bindingsName, "bindingsName cannot be empty.");
        this.bindings = bindings;
        this.bindingsName = bindingsName;
    }

    @Override
    public Object getValue(ELContext context, Object base, Object property) {
        Binding<Object> binding = binding(base, property);

        if (base == null && bindingsName.equals(property)) {
            context.setPropertyResolved(true);
            return bindings;
        }

        if (binding == null) return null;

        context.setPropertyResolved(base, property);
        return binding.getValue();
    }

    @Override
    public Class<?> getType(ELContext context, Object base, Object property) {
        if (base == null && bindingsName.equals(property)) {
            context.setPropertyResolved(true);
            return Bindings.class;
        }

        Binding<Object> binding = binding(base, property);
        if (binding == null) return null;

        context.setPropertyResolved(base, property);
        return binding.getType() instanceof Class<?> type ? type : Object.class;
    }

    @Override
    public void setValue(ELContext context, Object base, Object property, Object value) {
        if ((base == null && bindingsName.equals(property)) || binding(base, property) != null) {
            throw new PropertyNotWritableException("binding [" + property + "] cannot be assigned from an EL expression.");
        }
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property) {
        if ((base == null && bindingsName.equals(property)) || binding(base, property) != null) {
            context.setPropertyResolved(true);
            return true;
        }

        return false;
    }

    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base) {
        return base == null || base == bindings ? String.class : null;
    }

    /**
     * The binding an EL reference means: a bare identifier, or a property of the root object.
     */
    private Binding<Object> binding(Object base, Object property) {
        if (!(property instanceof String name)) return null;
        if (base != null && base != bindings) return null;
        if (base == null && bindingsName.equals(name)) return null;
        return bindings.contains(name) ? bindings.getBinding(name) : null;
    }

    @Override
    public String toString() {
        return "BindingsELResolver{bindingsName='" + bindingsName + "'}";
    }
}
