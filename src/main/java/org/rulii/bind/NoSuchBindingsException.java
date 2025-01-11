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
package org.rulii.bind;

import org.rulii.model.UnrulyException;


/**
 * Exception thrown when the specified bindings do not exist in the scope.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class NoSuchBindingsException extends UnrulyException {

    private static final long serialVersionUID = 0L;

    private final Bindings bindings;
    private final String name;

    public NoSuchBindingsException(Bindings bindings) {
        super("Bindings does not exist in the scope.");
        this.bindings = bindings;
        this.name = null;
    }

    public NoSuchBindingsException(String name) {
        super("Bindings [" + name + "] does not exist in the scope.");
        this.name = name;
        this.bindings = null;
    }

    public Bindings getBindings() {
        return bindings;
    }

    public String getName() {
        return name;
    }
}
