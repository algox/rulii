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
package org.rulii.util.reflect;

import org.rulii.bind.match.BindingMatchingStrategy;
import org.rulii.convert.Converter;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.UnrulyException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Default Object Factory implementation. Objects are created via reflection using the default ctor.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class DefaultObjectFactory implements ObjectFactory {

    // Post Ctor cache by class.
    private static final Map<Class<?>, Method> postConstructorCache = new WeakHashMap<>();
    private static final Map<Class<?>, Object> objectCache          = new WeakHashMap<>();

    private final boolean useCache;

    public DefaultObjectFactory() {
        this(true);
    }

    public DefaultObjectFactory(boolean useCache) {
        super();
        this.useCache = useCache;
    }

    /**
     * Creates a binding matching strategy of the specified type.
     *
     * @param type The class of the binding matching strategy.
     * @return An instance of the specified binding matching strategy.
     * @throws UnrulyException If the binding matching strategy cannot be created.
     */
    @Override
    public <T extends BindingMatchingStrategy> T createBindingMatchingStrategy(Class<T> type) throws UnrulyException {
        return create(type, isUseCache());
    }

    /**
     * Creates an instance of the specified class.
     *
     * @param <T>  the type of the instance to be created
     * @param type the class of the instance to be created
     * @return an instance of the specified class
     * @throws UnrulyException if the instance cannot be created
     */
    @Override
    public <T> T createAction(Class<T> type) throws UnrulyException {
        return create(type, isUseCache());
    }

    /**
     * Creates an instance of the specified class.
     *
     * @param <T>       the type of the instance to be created
     * @param type      the class of the instance to be created
     * @return an instance of the specified class
     * @throws UnrulyException  if the instance cannot be created
     */
    @Override
    public <T> T createCondition(Class<T> type) throws UnrulyException {
        return create(type, isUseCache());
    }

    /**
     * Creates an instance of the specified class using the default object factory.
     * If a cached instance of the specified type exists and caching is enabled,
     * the cached instance is returned. Otherwise, a new instance is created using
     * the internal create method and the post constructor (if present) is invoked.
     * If caching is enabled, the newly created instance is cached.
     *
     * @param <T> the type of the instance to be created
     * @param type the class of the instance to be created
     * @return an instance of the specified class
     * @throws UnrulyException if the instance cannot be created
     */
    @Override
    public <T> T createFunction(Class<T> type) throws UnrulyException {
        return create(type, isUseCache());
    }

    /**
     * Creates an instance of the specified class using the default object factory.
     * If a cached instance of the specified type exists and caching is enabled, the cached instance is returned.
     * Otherwise, a new instance is created using the internal create method and the post constructor (if present) is invoked.
     * If caching is enabled, the newly created instance is cached.
     *
     * @param <T> the type of the instance to be created
     * @param type the class of the instance to be created
     * @return an instance of the specified class
     * @throws UnrulyException if the instance cannot be created
     */
    @Override
    public <T> T createRule(Class<T> type) throws UnrulyException {
        return create(type, isUseCache());
    }

    /**
     * Creates a converter of the specified type.
     *
     * @param type The class of the converter.
     * @param <T> The source type of the converter.
     * @param <R> The target type of the converter.
     * @return An instance of the specified converter class.
     * @throws UnrulyException If the converter cannot be created.
     */
    @Override
    public <T, R> Converter<T, R> createConverter(Class<? extends Converter<T, R>> type) throws UnrulyException {
        return create(type, isUseCache());
    }

    /**
     * Creates an instance of the specified class with an optional caching option.
     *
     * @param <T>       the type of the instance to be created
     * @param type      the class of the instance to be created
     * @param isUseCache true if caching is enabled and false otherwise
     * @return an instance of the specified class
     * @throws IllegalArgumentException if type is null
     * @throws UnrulyException          if the instance cannot be created
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T create(Class<T> type, boolean isUseCache) {
        Assert.notNull(type, "type cannot be null.");

        if (isUseCache && objectCache.containsKey(type)) return (T) objectCache.get(type);

        // Create the object
        T result = createInternal(type);

        Method postConstructor;

        // Check if we cached the post constructor
        if (postConstructorCache.containsKey(type)) {
            postConstructor = postConstructorCache.get(type);
        } else {
            // Find the post constructor if one exists.
            postConstructor = ReflectionUtils.getPostConstructMethods(type);
            // Cache the post constructor
            postConstructorCache.put(type, postConstructor);
        }

        if (postConstructor != null) {
            // Call the Post Constructor
            ReflectionUtils.invokePostConstruct(postConstructor, result);
        }

        // Cache it
        if (isUseCache) objectCache.put(type, result);

        return result;
    }

    protected boolean isUseCache() {
        return useCache;
    }

    /**
     * Creates an instance of the specified class using reflection.
     *
     * @param <T>  the type of the instance to be created
     * @param type the class of the instance to be created
     * @return an instance of the specified class
     * @throws UnrulyException if the instance cannot be created
     */
    protected <T> T createInternal(Class<T> type) throws UnrulyException {
        try {
            return type.getConstructor().newInstance();
        } catch (NoSuchMethodException | InvocationTargetException| InstantiationException | IllegalAccessException e) {
            throw new UnrulyException("Unable to instantiate type [" + type + "]. Does it have a default ctor ?", e);
        }
    }

    @Override
    public String toString() {
        return "DefaultObjectFactory{" +
                "useCache=" + useCache +
                '}';
    }
}
