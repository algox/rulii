/**
 * This software is licensed under the Apache 2 license, quoted below.
 *
 * Copyright (c) 2019, algorithmx.org (dev@algorithmx.org)
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
package org.rulii.lib.spring.core;

import org.rulii.lib.spring.util.Assert;
import org.rulii.lib.spring.core.annotation.AnnotationUtils;
import org.rulii.annotation.Param;
import org.rulii.lib.apache.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * Discovers parameter names using the following logic.
 *
 * If the parameter has the <code>@Param</code>then it is used to getBinding the name
 * Uses standard Java 8 reflection to retrieve the parameter name. The compiler must be told to store the parameter information
 * for this tactic to work. Please use -parameters option during compilation.
 * Uses byte code inspection to retrieve the name. This will work any concrete classes (ie: not generated at runtime)
 * that sit in the CLASSPATH.
 *
 * <p>Illegal argument exception is thrown if we are unable to retrieve the parameter name. In this remedy the problem by
 * following either of the first two methods.</p>
 *
 * @author Max Arulananthan
 * @since 1.0
 */

public class DefaultParameterNameDiscoverer implements ParameterNameDiscoverer {

    private final ParameterNameDiscoverer bytecodeParameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();

    // TODO : Add Cache

    /**
     * Default Ctor
     */
    DefaultParameterNameDiscoverer() {
        super();
    }

    @Override
    public String[] getParameterNames(Method method) {
        Assert.notNull(method, "method cannot be null.");
        return getParameterNames(method, bytecodeParameterNameDiscoverer.getParameterNames(method));
    }

    @Override
    public String[] getParameterNames(Constructor<?> ctor) {
        Assert.notNull(ctor, "ctor cannot be null.");
        return getParameterNames(ctor, bytecodeParameterNameDiscoverer.getParameterNames(ctor));
    }

    /**
     * See logic defined at the class level.
     *
     * @param executable method or ctor
     * @param names names from bytecode
     * @return parameter names
     */
    private String[] getParameterNames(Executable executable, String[] names) {
        Parameter[] parameters = executable.getParameters();
        String[] result = new String[parameters.length];
        boolean namesAvail = names != null && names.length  == parameters.length;

        for (int i = 0; i < parameters.length; i++) {
            Param param = AnnotationUtils.getAnnotation(parameters[i], Param.class);

            if (param != null && !Param.NOT_APPLICABLE.equals(param.value())) {
                result[i] = param.value();
            } else if (parameters[i].isNamePresent()) {
                result[i] = parameters[i].getName();
            } else {
                if (namesAvail && StringUtils.isNotEmpty(names[i])) {
                    result[i] = names[i];
                } else {
                    result[i] = parameters[i].getName();
                }
            }
        }

        return result;
    }
}
