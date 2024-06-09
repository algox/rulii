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

import org.rulii.lib.apache.reflect.TypeUtils;
import org.rulii.lib.spring.util.Assert;
import org.rulii.bind.match.ParameterMatch;
import org.rulii.model.UnrulyException;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Thrown if a ClassCastException is thrown during method execution.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class ParameterMismatchException extends UnrulyException {

    private final Method method;
    private final List<ParameterMatch> matches;
    private final List<Object> args;

    public ParameterMismatchException(Method method, List<ParameterMatch> matches, List<Object> args, ClassCastException e) {
        super(e);
        Assert.notNull(method, "method cannot be null");
        Assert.notNull(matches, "matches cannot be null");
        Assert.notNull(args, "args cannot be null");
        this.method = method;
        this.matches = matches;
        this.args = args;
    }

    public Method getMethod() {
        return method;
    }

    public List<ParameterMatch> getMatches() {
        return matches;
    }

    public List<Object> getArgs() {
        return args;
    }

    @Override
    public String getMessage() {
        return "Parameter mismatch trying to execute Action [" + method
                + "] with arguments [" + args + "]" + System.lineSeparator()
                + findMismatches(matches, args);
    }

    private static String findMismatches(List<ParameterMatch> matches, List<Object> args) {
        StringBuilder result = new StringBuilder();

        int size = matches.size();
        for (int i = 0; i < size; i++) {
            if (i > args.size()-1 || args.get(i) == null || matches.get(i) == null || !matches.get(i).isMatched()) continue;

            if (!TypeUtils.isAssignable(args.get(i).getClass(), matches.get(i).getDefinition().getType())) {
                result.append("\tparameter mismatch [" + matches.get(i).getDefinition().getTypeAndName()
                        + "] would not accept [" + args.get(i).getClass().getSimpleName() + " " + args.get(i)
                        + "] Matched Binding [" + matches.get(i).getBinding() + "] "
                        + matches.get(i).getDescription());
                if (i < size - 1) result.append(System.lineSeparator());
            }
        }

        return result.toString();
    }
}
