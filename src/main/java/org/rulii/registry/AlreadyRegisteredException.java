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
package org.rulii.registry;

import org.rulii.model.Runnable;
import org.rulii.model.UnrulyException;

/**
 * Thrown when you attempt to register an already existing Rule/RuleSet.
 *
 * @author Max Arulananthan
 * @since 1.0
 *
 */
public class AlreadyRegisteredException extends UnrulyException {

    private static final long serialVersionUID = 0L;

    private final Runnable<?> runnable;

    /**
     * Ctor with the existing rule/ruleSet.
     *
     * @param name name that is registered under.
     * @param runnable existing rule/ruleSet.
     */
    public AlreadyRegisteredException(String name, Runnable<?> runnable) {
        super("Registry already has name [" + name + "] registered. [" + runnable + "]");
        this.runnable = runnable;
    }

    public Runnable<?> getExistingValue() {
        return runnable;
    }
}
