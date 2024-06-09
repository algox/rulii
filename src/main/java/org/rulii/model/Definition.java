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
package org.rulii.model;

/**
 * Details of a "definable" object. A definable object can be anything from a method/method parameter/rule/rulset etc
 *
 * @author Max Arulananthan
 * @since 1.0
 * @see Definable
 */
public interface Definition extends Identifiable {

    /**
     * Source details of the definable object.
     *
     * @return source details.
     */
    SourceDefinition getSource();
}
