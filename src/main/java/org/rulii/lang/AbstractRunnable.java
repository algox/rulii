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
package org.rulii.lang;

import org.rulii.bind.match.ParameterMatch;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.*;
import org.rulii.util.reflect.MethodExecutor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Parent class for all Runnable(s).
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public abstract class AbstractRunnable implements Identifiable, Definable<MethodDefinition> {

    private final MethodDefinition methodDefinition;
    private final MethodExecutor methodExecutor;
    private final Object target;
    private final String name;
    private final String description;

    protected AbstractRunnable(MethodDefinition methodDefinition, MethodExecutor methodExecutor,
                               Object target, String name, String description) {
        super();
        Assert.notNull(methodDefinition, "methodDefinition cannot be null.");
        Assert.notNull(methodExecutor, "methodExecutor cannot be null.");
        Assert.notNull(name, "name cannot be null.");
        this.methodDefinition = methodDefinition;
        this.methodExecutor = methodExecutor;
        this.target = target;
        this.name = name;
        this.description = description != null
                ? description
                : getName() + "(" + (methodDefinition.getMethodParameterSignature()) + ")";
    }

    /**
     * Executes the given action with the matched Bindings.
     *
     * @param matches matched Bindings.
     * @param args actual method args.
     * @return result of the execution.
     */
    protected Object run(List<ParameterMatch> matches, List<Object> args) {
        // Execute the Action Method
        try {
            return methodExecutor.execute(target, !couldChangeState() ? immutable(args) : args);
        } catch (ClassCastException e) {
            throw new ParameterMismatchException(getDefinition().getMethod(), matches, args, e);
        } catch (UnrulyException e) {
            throw e;
        } catch (Throwable e) {
            throw new UnrulyException("Error trying to execute Action [" + getDefinition().getMethod()
                    + "] with arguments [" + args + "]", e);
        }
    }

    /**
     * Determines if the runnable could change state. ie: create scopes/add bindings/change bindings.
     * @return true if this runnable can change state; false otherwise.
     */
    protected abstract boolean couldChangeState();

    /**
     * Change Bindings/RuleContext into Immutable versions.
     *
     * @param args supplied args.
     * @return converted args.
     */
    protected List<Object> immutable(List<Object> args) {
        if (args == null || args.isEmpty()) return args;

        int size = args.size();
        Object[] result = new Object[size];

        for (int  i = 0; i < size; i++) {
            Object value = args.get(i);

            // Check if the argument could be converted to an Immutable version.
            if (value instanceof Immutator) {
                value = ((Immutator<?>) value).asImmutable();
            } else if (value instanceof Optional) {
                // Looks like an Optional value; convert the value if needed.
                Optional<?> optional = (Optional<?>) value;

                if (optional.isPresent() && optional.get() instanceof Immutator) {
                    value = Optional.of(((Immutator<?>) optional.get()).asImmutable());
                }
            }

            result[i] = value;
        }

        return Collections.unmodifiableList(Arrays.asList(result));
    }

    public MethodDefinition getDefinition() {
        return methodDefinition;
    }

    public Object getTarget() {
        return target;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    protected MethodExecutor getMethodExecutor() {
        return methodExecutor;
    }

    @Override
    public String toString() {
        return getDescription();
    }
}
