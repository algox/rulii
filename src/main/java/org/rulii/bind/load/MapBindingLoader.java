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

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Loads the desired keys in the give Map as separate Bindings.
 *
 * You do have the option to control which keys get added by using the (Filter/IgnoreKeys/IncludeKeys)
 * You also have the option to change the BindingName using the NameGenerator property.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class MapBindingLoader extends AbstractBindingLoader<String, Map<String, Object>> {

    private static final Log logger = LogFactory.getLog(MapBindingLoader.class);

    public MapBindingLoader() {
        super();
        setFilter(key -> true);
        setNameGenerator(key -> key);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void load(Bindings bindings, Map<String, Object> map) {
        Assert.notNull(map, "map cannot be null.");

        logger.info("Loading Map into Bindings.");

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (getFilter() != null && !getFilter().test(entry.getKey())) continue;
            String bindingName = getNameGenerator().apply(entry.getKey());

            Supplier getter = () -> map.get(entry.getKey());
            Consumer setter = (value) -> map.put(entry.getKey(), value);

            bindings.bind(Binding.builder()
                    .with(bindingName)
                    .delegate(getter, setter)
                    .build());
        }
    }

    @Override
    protected String getItemName(String item) {
        Assert.notNull(item, "item cannot be null.");
        return item;
    }
}
