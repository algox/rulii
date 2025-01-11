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

/**
 * Thrown when you attempt to declare an already existing Binding.
 *
 * @author Max Arulananthan
 * @since 1.0
 *
 */
public class BindingAlreadyExistsException extends UnrulyException {

	private final Binding existingBinding;
	private final Binding newBinding;

	/**
	 * Ctor with the existing binding.
	 *
	 * @param existingBinding existing binding.
	 * @param newBinding new binding.
	 */
	public BindingAlreadyExistsException(Binding existingBinding, Binding newBinding) {
		this(existingBinding, newBinding, false);
	}

	/**
	 * Ctor with the existing binding.
	 * 
	 * @param existingBinding existing binding.
	 * @param newBinding new binding.
	 * @param isFinal is this being thrown because the existing binding is final.
	 */
	public BindingAlreadyExistsException(Binding existingBinding, Binding newBinding, boolean isFinal) {
		super(getMessage(existingBinding, isFinal));
		this.existingBinding = existingBinding;
		this.newBinding = newBinding;
	}

	public Binding getExistingBinding() {
		return existingBinding;
	}

	public Binding getNewBinding() {
		return newBinding;
	}

	protected static String getMessage(Binding binding, boolean isFinal) {
		Assert.notNull(binding, "binding cannot be null.");
		return String.format((isFinal ? "FINAL " : "") + ("Binding with name [%s] type [%s] value [%s] already exists. You cannot re-define it."),
				binding.getName(), binding.getType().getTypeName(), binding.getTextValue());
	}
}
