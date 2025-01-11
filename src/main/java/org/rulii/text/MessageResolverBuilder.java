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
package org.rulii.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Constructs a new MessageResolverBuilder with the specified base names.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class MessageResolverBuilder {

    private final List<String> baseNames = new ArrayList<>();

    /**
     * Constructs a new MessageResolverBuilder with the specified base names.
     *
     * @param baseNames an array of base names for the resource bundles to be used for message resolution
     */
    public MessageResolverBuilder(String...baseNames) {
        super();
        if (baseNames != null) this.baseNames.addAll(Arrays.asList(baseNames));
    }

    /**
     * Adds a base name to the list of base names for the resource bundles used for message resolution.
     *
     * @param name the base name to add
     * @return the MessageResolverBuilder instance
     */
    public MessageResolverBuilder baseName(String name) {
        this.baseNames.add(name);
        return this;
    }

    /**
     * Builds a MessageResolver instance with the specified base names.
     * The MessageResolver is responsible for resolving message codes to messages.
     *
     * @return a MessageResolver instance
     */
    public MessageResolver build() {
        return baseNames.size() == 1 ? new ResourceBundleMessageResolver(baseNames.get(0)) : new CompositeResourceBundleMessageResolver(baseNames);
    }
}
