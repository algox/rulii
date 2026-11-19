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
package org.rulii.validation;

import org.rulii.bind.Bindings;
import org.rulii.bind.ReservedBindings;
import org.rulii.context.RuleContext;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.Definable;
import org.rulii.model.MethodDefinition;
import org.rulii.model.ParameterMismatchException;
import org.rulii.model.UnrulyException;
import org.rulii.model.function.Function;

import java.lang.reflect.Method;
import java.util.List;

/**
 * A {@link BindingFunction} whose value is the named binding itself: what {@code Validators.binding(name)}
 * hands out. Its target reads the binding straight from the context instead of running a function built
 * over a lambda, which gives the same value at a fraction of the cost.
 *
 * @author Max Arulananthan
 * @since 2.3
 */
public class BindingLookupFunction extends BindingFunction<Object> {

    /**
     * Creates a function that reads the named binding.
     *
     * @param bindingName the binding to read; also the parameter key in rule violations
     */
    public BindingLookupFunction(String bindingName) {
        super(bindingName, new Lookup(bindingName));
    }

    /** Reads the binding the way a function over {@code ruleContext.getBindings().getValue(name)} does. */
    private static final class Lookup implements Function<Object>, Definable<MethodDefinition> {

        private static final String NAME            = "binding";
        private static final Method RUN             = runMethod();
        private static final String BOUND_BINDINGS  = ReservedBindings.BI   NDINGS.getName();

        private final String bindingName;
        // Rebuilt by readResolve: a MethodDefinition holds a Method and cannot be written.
        private final transient MethodDefinition definition;

        private Lookup(String bindingName) {
            super();
            this.bindingName = bindingName;
            this.definition = ValueFunctionDefinition.of(RUN, NAME, NAME + "(" + bindingName + ")");
        }

        @Override
        public Object run(RuleContext ruleContext) throws UnrulyException {
            Assert.notNull(ruleContext, "context cannot be null.");

            try {
                // Read the bindings bound beside the context (the reserved "bindings"), not those of the context
                // handed in: a function taking (RuleContext ruleContext) reads the context bound under that name.
                // Through their read-only view, as a function sees them: a value that can be written through
                // (a RuleContext, Bindings) comes back read-only, converted once.
                Bindings bound = ruleContext.getBindings().getValue(BOUND_BINDINGS);
                return bound.asImmutable().getValue(bindingName);
            } catch (ClassCastException e) {
                throw new ParameterMismatchException(RUN, List.of(), List.of(ruleContext), e);
            } catch (UnrulyException e) {
                throw e;
            } catch (Throwable e) {
                // Anything else is reported the way a function built over a lambda reports it.
                throw new UnrulyException("Error trying to execute [" + RUN + "] with arguments [[" + ruleContext + "]]", e);
            }
        }

        @Override
        public MethodDefinition getDefinition() {
            return definition;
        }

        @Override
        public String getName() {
            return definition.getName();
        }

        @Override
        public String getDescription() {
            return definition.getDescription();
        }

        @Override
        public String toString() {
            return getDescription();
        }

        private Object readResolve() {
            return new Lookup(bindingName);
        }

        private static Method runMethod() {
            try {
                return Lookup.class.getDeclaredMethod("run", RuleContext.class);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
