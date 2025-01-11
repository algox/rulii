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
package org.rulii.bind.load;

import org.rulii.bind.Binding;
import org.rulii.bind.Bindings;
import org.rulii.lib.apache.commons.logging.Log;
import org.rulii.lib.apache.commons.logging.LogFactory;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.UnrulyException;

import java.beans.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Loads the desired properties in the give Bean as separate Bindings.
 *
 * You do have the option to control which properties get added by using the (Filter/IgnoreProperties/IncludeProperties)
 * You also have the option to change the BindingName using the NameGenerator property.
 *
 * @param <T> Bean Type.
 * @author Max Arulananthan
 * @since 1.0
 */
public class PropertyBindingLoader<T> extends AbstractBindingLoader<PropertyDescriptor, T> {

    private static final Log logger = LogFactory.getLog(PropertyBindingLoader.class);

    public PropertyBindingLoader() {
        super();
        setFilter(property -> true);
        setNameGenerator(FeatureDescriptor::getName);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void load(Bindings bindings, T bean) {
        Assert.notNull(bindings, "bindings cannot be null.");
        Assert.notNull(bean, "bean cannot be null.");
        Assert.notNull(getNameGenerator(), "name generator cannot be null.");

        logger.info("Loading Bean [" + bean.getClass().getName() + "] Properties into Bindings.");

        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());

            // Go through all the properties
            for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
                if (getFilter() != null && !getFilter().test(propertyDescriptor)) continue;

                Method getterMethod = propertyDescriptor.getReadMethod();
                Method setterMethod = propertyDescriptor.getWriteMethod();

                // There is no getter; ignore property
                if (getterMethod == null) continue;

                Supplier getter = () -> {
                    try {
                        return getterMethod.invoke(bean);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new UnrulyException("Unable to get property value ["
                                + getterMethod.getDeclaringClass().getSimpleName() + "." + propertyDescriptor.getName() + "]", e);
                    }
                };

                Consumer setter = null;

                if (setterMethod != null) {
                    setter = (value) -> {
                        try {
                            setterMethod.invoke(bean, value);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new UnrulyException("Unable to set property value ["
                                    + getterMethod.getDeclaringClass().getSimpleName() + "." + propertyDescriptor.getName() + "]", e);
                        }
                    };
                }

                String bindingName = getNameGenerator().apply(propertyDescriptor);
                // Bind the property
                bindings.bind(Binding.builder().with(bindingName)
                        .type(propertyDescriptor.getReadMethod().getGenericReturnType())
                        .delegate(getter, setter)
                        .editable(setterMethod != null)
                        .build());
            }
        } catch (IntrospectionException e) {
            throw new UnrulyException("Error trying to Introspect [" + bean.getClass() + "]", e);
        } catch (Exception e) {
            throw new UnrulyException("Error trying to use BindingLoader [" + bean.getClass() + "]", e);
        }
    }

    @Override
    protected String getItemName(PropertyDescriptor item) {
        Assert.notNull(item, "item cannot be null.");
        return item.getName();
    }
}
