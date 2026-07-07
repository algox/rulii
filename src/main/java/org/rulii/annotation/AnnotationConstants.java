/*
 * This software is licensed under the Apache 2 license, quoted below.
 *
 * Copyright (c) 1999-2026, Algorithmx Inc.
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
package org.rulii.annotation;

/**
 * Single source of truth for the "unset name" sentinel shared by {@link Action}, {@link Condition}
 * and {@link Function}. Each of those annotations still exposes its own public {@code NOT_APPLICABLE}
 * constant for API compatibility; they all reference this value instead of redeclaring the literal.
 *
 * @author Max Arulananthan
 * @since 2.0
 */
final class AnnotationConstants {

    static final String NOT_APPLICABLE = "N/A";

    private AnnotationConstants() {
        super();
    }
}
