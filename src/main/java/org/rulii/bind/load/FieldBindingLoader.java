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
package org.rulii.bind.load;

import org.rulii.bind.Binding;
import org.rulii.bind.Bindings;
import org.rulii.lib.apache.commons.logging.Log;
import org.rulii.lib.apache.commons.logging.LogFactory;
import org.rulii.lib.spring.util.Assert;
import org.rulii.lib.spring.util.ReflectionUtils;
import org.rulii.model.UnrulyException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Loads the desired fields in the given Bean as separate Bindings.
 *
 * You do have the option to control which fields get added by using the (Filter/IgnoreFields/IncludeFields)
 * You also have the option to change the BindingName using the NameGenerator property.
 *
 * @param <T> Bean Type.
 * @author Max Arulananthan
 * @since 1.0
 * @see BindingLoader
 */
public class FieldBindingLoader<T> extends AbstractBindingLoader<Field, T> {

    private static final Log logger = LogFactory.getLog(FieldBindingLoader.class);

    private static final Map<Class<?>, List<Field>> CLASS_FIELD_CACHE = new ConcurrentHashMap<>();

    public FieldBindingLoader() {
        super();
        setFilter(field -> !field.isSynthetic() && !Modifier.isStatic(field.getModifiers()));
        setNameGenerator(Field::getName);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void load(Bindings bindings, T bean) {
        Assert.notNull(bean, "bean cannot be null.");

        logger.info("Loading Class [" + bean.getClass().getName() + "] Field(s) into Bindings.");

        Class<?> type = bean.getClass();
        List<Field> fields = getClassFields(type);

        for (Field field : fields) {
            // Check the filter
            if (!getFilter().test(field)) continue;

            Supplier<T> getter = () -> {
                try {
                    return (T) field.get(bean);
                } catch (IllegalAccessException e) {
                    throw new UnrulyException("Unable to get field value ["
                            + field.getDeclaringClass().getSimpleName() + "." + field.getName() + "]", e);
                }
            };

            Consumer<T> setter = (value) -> {
                try {
                    field.set(bean, value);
                } catch (IllegalAccessException e) {
                    throw new UnrulyException("Unable to set field value ["
                            + field.getDeclaringClass().getSimpleName() + "." + field.getName() + "]", e);
                }
            };

            bindings.bind(Binding.builder()
                    .with(getNameGenerator().apply(field))
                    .type(field.getGenericType())
                    .delegate(getter, setter)
                    .build());
        }
    }

    @Override
    protected String getItemName(Field item) {
        Assert.notNull(item, "item cannot be null.");
        return item.getName();
    }

    private List<Field> getClassFields(Class<?> type) {
        return CLASS_FIELD_CACHE.computeIfAbsent(type, (Class<?> c) -> {
            Map<String, Field> fields = new LinkedHashMap<>();
            ReflectionUtils.doWithFields(type, field -> {
                ReflectionUtils.makeAccessible(field);
                fields.put(field.getName(), field);
            });
            return new LinkedList<Field>(fields.values());
        });
    }
}
