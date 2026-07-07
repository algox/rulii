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

import java.lang.annotation.*;

/**
 * Annotation to mark the Then method of a Rule. Then is the action is run as a result of Rule condition being met.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Then {

    String name() default "then";

    /**
     * Get the order of the Rule Action method.
     *
     * <p>Actions are sorted by this value. When two or more actions share the same order
     * (including the shared default), their relative execution order is <em>unspecified</em> —
     * it happens to follow declaration order on the JDK implementations in common use today, but
     * that is an artifact of {@link Class#getDeclaredMethods()}, which the JDK documents as
     * returning elements in no particular order. Assign distinct {@code order()} values to any
     * actions whose relative sequence matters.
     *
     * @return the order of the Rule Action (then) method.
     */
    int order() default Integer.MAX_VALUE;
}
