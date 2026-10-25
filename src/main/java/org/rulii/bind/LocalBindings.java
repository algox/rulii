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
 * Bindings local to one run of one rule. A method of a rule class (a class annotated with {@code @Rule}) that
 * declares a parameter of this type receives a fresh instance for each run of the rule, shared by every method
 * of that run: what the pre-condition or the condition binds, the then and otherwise actions read. Nothing in it
 * reaches the rule's caller, and the caller's bindings are untouched: the engine never binds the instance into
 * the caller's stack, it resolves the parameter from a view it builds for the run (see
 * {@code RuleContext.withLocalBindings()}). The instance is gone when the run ends, so recursive and concurrent
 * runs of one rule never share it.
 *
 * <p>Conditions run with immutable bindings, but the local bindings they receive are this very object, so they
 * can bind into it: that is the sanctioned way for a condition to hand something to the actions of its run.
 *
 * <p>Available to class-based rules only, where the declaration is read at build time; a lambda-based rule
 * declaring the parameter fails to build, and a script has no parameters to declare. Reached by the name
 * {@code localBindings} or by this exact type; a parameter of type {@code Bindings} never resolves to it.
 *
 * @author Max Arulananthan
 * @since 2.2
 */
public class LocalBindings extends DefaultBindings {

    LocalBindings() {
        super();
    }

    /**
     * Local bindings are never made immutable: a condition receives its arguments as immutable copies, and these
     * are the one argument a condition is meant to write. Returns this.
     *
     * @return this.
     */
    @Override
    public Bindings asImmutable() {
        return this;
    }

    @Override
    public String toString() {
        return "LocalBindings(" + size() + ")";
    }
}
