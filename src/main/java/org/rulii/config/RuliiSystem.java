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
package org.rulii.config;

import org.rulii.util.reflect.MethodResolver;
import org.rulii.util.reflect.ObjectFactory;

public final class RuliiSystem {

    private static final RuliiSystem instance = new RuliiSystem();

    private MethodResolver methodResolver = MethodResolver.builder().build();
    private ObjectFactory objectFactory = ObjectFactory.builder().build();

    private RuliiSystem() {
        super();
    }

    public static RuliiSystem getInstance() {
        return instance;
    }

    public MethodResolver getMethodResolver() {
        return methodResolver;
    }

    public void setMethodResolver(MethodResolver methodResolver) {
        this.methodResolver = methodResolver;
    }

    public ObjectFactory getObjectFactory() {
        return objectFactory;
    }

    public void setObjectFactory(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }

    @Override
    public String toString() {
        return "RuliiSystem{" +
                ", methodResolver=" + methodResolver +
                ", objectFactory=" + objectFactory +
                '}';
    }
}
