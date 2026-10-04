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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Short labels saying what a rule is about, such as {@code "vip"}, {@code "fraud"} or
 * {@code "regulatory"}. A rule can carry any number; tools such as rulii-explorer show them as
 * chips and filter by them. Compare {@link Category}, which is the one place a rule belongs.
 *
 * <p>Read from {@code @Rule} classes; rules built in code use the builder's {@code tags(...)}.
 * Blank entries are dropped and duplicates kept once, in the order written.
 *
 * @author Max Arulananthan
 * @since 2.1
 * @see Category
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Tags {

    /**
     * The tags.
     *
     * @return the tags.
     */
    String[] value();
}
