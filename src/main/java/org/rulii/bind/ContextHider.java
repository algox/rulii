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
package org.rulii.bind;

/**
 * Marks {@link Bindings} that belong to another context and hide its reserved {@code ruleContext} and
 * {@code bindings}.
 *
 * <p>Such bindings are a whole stack that its owner keeps pushing to and popping from, on its own thread. Whoever
 * would share them by reference with work on another thread takes them as they stand instead: a
 * {@link ScopedBindings#fork() fork} inherits a scope of this kind as the Bindings visible in it at the time of the
 * fork, not as the live stack.
 *
 * @author Max Arulananthan
 * @since 2.3
 * @see HiddenContextBindings
 */
public interface ContextHider {
}
