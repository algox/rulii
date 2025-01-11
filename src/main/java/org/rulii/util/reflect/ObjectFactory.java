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

import org.rulii.bind.match.BindingMatchingStrategy;
import org.rulii.convert.Converter;
import org.rulii.model.UnrulyException;

/**
 * Factory use to defaultObjectFactory Objects. Framework requires Object instances to be created (such as Rules), the ObjectFactory is
 * used to defaultObjectFactory those Objects.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public interface ObjectFactory {

    /**
     * Returns the ObjectFactoryBuilder.
     *
     * @return instance of the ObjectFactoryBuilder.
     */
    static ObjectFactoryBuilder builder() {
        return new ObjectFactoryBuilder();
    }

    /**
     * Creates an instance of a BindingMatchingStrategy based on the given class type.
     *
     * @param <T> the type of BindingMatchingStrategy
     * @param type the class type of the BindingMatchingStrategy
     * @return an instance of the BindingMatchingStrategy
     * @throws UnrulyException if there is an error creating the BindingMatchingStrategy
     */
    <T extends BindingMatchingStrategy> T createBindingMatchingStrategy(Class<T> type) throws UnrulyException;

    /**
     * Creates an instance of the specified type.
     *
     * @param <T> the type of object to create
     * @param type the class type of the object to create
     * @return an instance of the specified type
     * @throws UnrulyException if there is an error creating the object
     */
    <T> T createAction(Class<T> type) throws UnrulyException;

    /**
     * Creates an instance of the specified type of condition.
     *
     * @param <T> the type of condition to create
     * @param type the class type of the condition to create
     * @return an instance of the specified type
     * @throws UnrulyException if there is an error creating the condition
     */
    <T> T createCondition(Class<T> type) throws UnrulyException;

    /**
     * Creates an instance of the specified type.
     *
     * @param <T> the type of object to create
     * @param type the class type of the object to create
     * @return an instance of the specified type
     * @throws UnrulyException if there is an error creating the object
     */
    <T> T createFunction(Class<T> type) throws UnrulyException;

    /**
     * Creates an instance of a Converter based on the given class type.
     *
     * @param <T> the source type of the converter
     * @param <R> the target type of the converter
     * @param type the class type of the Converter implementation
     * @return an instance of a Converter
     * @throws UnrulyException if there is an error creating the Converter
     */
    <T, R> Converter<T, R> createConverter(Class<? extends Converter<T, R>> type) throws UnrulyException;

    /**
     * Creates an instance of the specified type of rule.
     *
     * @param <T> the type of rule to create
     * @param type the class type of the rule to create
     * @return an instance of the specified type of rule
     * @throws UnrulyException if there is an error creating the rule
     */
    <T> T createRule(Class<T> type) throws UnrulyException;

    /**
     * Creates a new instance of the desired Type.
     *
     * @param type desired type.
     * @param <T> generic Type.
     * @param isUseCache use object cache.
     * @return new instance of Type.
     * @throws UnrulyException thrown in case we are unable to defaultObjectFactory the type at runtime.
     */
    <T> T create(Class<T> type, boolean isUseCache) throws UnrulyException;
}
