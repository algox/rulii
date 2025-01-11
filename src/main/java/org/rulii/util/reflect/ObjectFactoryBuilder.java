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
package org.rulii.util.reflect;

/**
 * ObjectFactoryBuilder is a utility class that builds an instance of ObjectFactory.
 * It provides methods to configure the builder and create an ObjectFactory instance.
 */
public final class ObjectFactoryBuilder {

    private boolean useCache;

    /**
     * ObjectFactoryBuilder is a utility class that builds an instance of ObjectFactory.
     * It provides methods to configure the builder and create an ObjectFactory instance.
     */
    public ObjectFactoryBuilder() {
        this(true);
    }

    public ObjectFactoryBuilder(boolean useCache) {
        super();
        this.useCache = useCache;
    }

    /**
     * Sets whether to use caching in the ObjectFactoryBuilder.
     * Caching allows for reusing previously created objects, improving performance.
     *
     * @param useCache true if caching should be used, false otherwise
     * @return the ObjectFactoryBuilder instance
     */
    public ObjectFactoryBuilder useCache(boolean useCache) {
        this.useCache = useCache;
        return this;
    }

    /**
     * Builds an instance of ObjectFactory using the configured settings.
     *
     * @return an instance of ObjectFactory
     */
    public ObjectFactory build() {
        return new DefaultObjectFactory(useCache);
    }
}
