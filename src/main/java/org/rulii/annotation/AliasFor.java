/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.rulii.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is a copy of the Spring's @AliasFor annotation.
 * <p>
 * see org.springframework.core.annotation.AliasFor
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface AliasFor {

	/**
	 * Alias for {@link #attribute}.
	 * <p>Intended to be used instead of {@link #attribute} when {@link #annotation}
	 * is not declared &mdash; for example: {@code @AliasFor("value")} instead of
	 * {@code @AliasFor(attribute = "value")}.
	 * @return value of the attribute to be overridden.
	 */
	@AliasFor("attribute")
	String value() default "";

	/**
	 * The name of the attribute that <em>this</em> attribute is an alias for.
	 * @see #value
	 * @return name of the attribute to be overridden.
	 */
	@AliasFor("value")
	String attribute() default "";

	/**
	 * The type of annotation in which the aliased {@link #attribute} is declared.
	 * <p>Defaults to {@link Annotation}, implying that the aliased attribute is
	 * declared in the same annotation as <em>this</em> attribute.
	 * @return Annotation class being overridden.
	 */
	Class<? extends Annotation> annotation() default Annotation.class;

}
