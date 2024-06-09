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
package org.rulii.bind;

import org.rulii.model.UnrulyException;
import org.rulii.util.RuleUtils;
import org.rulii.util.reflect.LambdaUtils;
import org.rulii.util.reflect.ReflectionUtils;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.function.Function;

/**
 * Convenient Function to create Bindings in the form of name -&gt; value.
 *
 * For Example : name -&gt; String, value -&gt; Integer, salary -&gt; java.util.BigDecimal.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
@FunctionalInterface
public interface BindingDeclaration<T> extends Function<String, T>, Serializable {

    long serialVersionUID = -0L;

    /**
     * Retrieves the name of the Binding from the Function (Lambda).
     *
     * @return name of the Binding.
     */
    default String name() {
        // Serialize the Lambda to be able to retrieve the details of the Lambda.
        SerializedLambda lambda = LambdaUtils.getSafeSerializedLambda(this);

        // It's not a Lambda
        if (lambda == null) {
            throw new UnrulyException("BindingDeclaration can only be used as a Lambda expression");
        }

        // Find the class
        Class<?> implementationClass = LambdaUtils.getImplementationClass(lambda);
        // Find the method
        Method implementationMethod = LambdaUtils.getImplementationMethod(lambda, implementationClass);
        // Get the parameter names. In this ase there will be a single parameter.
        String[] parameterNames = ReflectionUtils.getParameterNames(implementationMethod);
        // Extract the name
        return parameterNames[parameterNames.length - 1];
    }

    /**
     * Retrieves the type of the Binding from the Function (Lambda).
     *
     * @return type (class) of the Binding.
     */
    default T value() {
        return apply(name());
    }

    /**
     * Binding description.
     *
     * @return description.
     */
    default String getDescription() {
        return name() + " = " + RuleUtils.getSummaryTextValue(value());
    }
}
