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
package org.rulii.annotation;

import org.rulii.bind.Bindings;
import org.rulii.bind.match.BindingMatch;
import org.rulii.bind.match.BindingMatchingStrategy;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Annotation which indicates that the method parameter's name should be taken from this annotation.
 * Example: <code>someMethod(@Param("name) String arg1)</code>
 *
 * @author Max Arulananthan
 * @since 1.0
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Param {

    String NOT_APPLICABLE = "N/A";

    /**
     * Parameter name.
     *
     * @return name of the parameter.
     */
    @AliasFor("name")
    String value() default NOT_APPLICABLE;

    /**
     * Parameter name.
     *
     * @return name of the parameter.
     */
    @AliasFor("value")
    String name() default NOT_APPLICABLE;

    /**
     * Default textual value.
     * @return default value.
     */
    String defaultValue() default NOT_APPLICABLE;

    /**
     * Determines the Binding strategy to use (during parameter matching).
     *
     * @return Binding Strategy to use.
     */
    Class<? extends BindingMatchingStrategy> matchUsing() default NoOpBindingMatchingStrategy.class;

    class NoOpBindingMatchingStrategy implements BindingMatchingStrategy {

        public NoOpBindingMatchingStrategy() {
            super();
        }

        @Override
        public <T> List<BindingMatch<T>> match(Bindings bindings, String name, Type type, boolean lambda) {
            throw new UnsupportedOperationException();
        }
    }
}
