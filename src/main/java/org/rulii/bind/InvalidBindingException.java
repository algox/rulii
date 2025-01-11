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

import org.rulii.lib.spring.util.Assert;
import org.rulii.model.UnrulyException;

import java.lang.reflect.Type;

/**
 * Thrown when you attempt to set an invalid value to a Binding.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class InvalidBindingException extends UnrulyException {

	/**
	 * Ctor taking an error message.
	 *
	 * @param message error message
	 */
	public InvalidBindingException(String message) {
		super(message);
	}

	/**
	 * Ctor with a Binding name, type and value.
     *
	 * @param name name of the binding.
	 * @param type type of the binding.
	 * @param attemptedValue value trying to set.
	 */
	public InvalidBindingException(String name, Type type, Object attemptedValue) {
		super(getMessage(name, type, attemptedValue));
	}

	private static String getMessage(String name, Type type, Object attemptedValue) {
		Assert.hasText(name, "name cannot be null.");
		Assert.notNull(type, "type cannot be null.");
		return String.format("Error trying to set binding [%s] of type [%s] with value [%s] type [%s]",
				name, type.getTypeName(), attemptedValue != null ? attemptedValue.toString() : "N/A",
				attemptedValue != null ? attemptedValue.getClass().toString() : "N/A");
	}
}
