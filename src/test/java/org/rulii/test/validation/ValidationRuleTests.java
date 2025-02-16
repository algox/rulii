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
package org.rulii.test.validation;

import org.junit.jupiter.api.Test;
import org.rulii.rule.Rule;
import org.rulii.rule.RuleResult;
import org.rulii.validation.BindingValidationRule;
import org.rulii.validation.RuleViolations;
import org.rulii.validation.Severity;
import org.rulii.validation.rules.alpha.AlphaValidationRule;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
/**
 * Class containing test methods for validating the functionality of Validation Rules.
 *
 * @author Max Arulananthan
 * @since 1.0
 *
 */
public class ValidationRuleTests {

    public ValidationRuleTests() {
        super();
    }

    @Test
    public void alphaTest1() {
        AlphaValidationRule validationRule = new AlphaValidationRule("value");
        Rule rule = Rule.builder().build(validationRule);
        RuleResult result = rule.run(value -> "abc");
        assertTrue(result.status().isPass());
    }

    @Test
    public void alphaTest2() {
        LinkedHashMap<Class<?>, Object> customArgs = new LinkedHashMap<>();
        customArgs.put(boolean.class, false);

        AlphaValidationRule validationRule = createRule(AlphaValidationRule.class, "value",
                "error.1", Severity.ERROR, "Alpha Error Message", customArgs);
        Rule rule = Rule.builder().build(validationRule);

        RuleResult result = rule.run(value -> "abc");
        assertTrue(result.status().isPass());

        RuleViolations violations = new RuleViolations();
        result = rule.run(ruleViolations -> violations, value -> "abc~");
        assertTrue(result.status().isFail());
        assertTrue(violations.hasErrors());
        assertEquals(1, violations.getViolations().size());
        //violations.getViolations().get(0).
    }

    private static <T extends BindingValidationRule> T createRule(Class<T> clazz,
                                                    String bindingName, String errorCode, Severity severity,
                                                    String errorMessage, LinkedHashMap<Class<?>, Object> customArgs) {
        List<Class<?>> argTypes = new ArrayList<>();

        argTypes.add(String.class);
        argTypes.add(String.class);
        argTypes.add(Severity.class);
        argTypes.add(String.class);
        if (customArgs != null) argTypes.addAll(customArgs.keySet());

        try {
            Constructor<T> ctor = clazz.getDeclaredConstructor(argTypes.toArray(new Class[0]));
            List<Object> args = new ArrayList<>();
            args.add(bindingName);
            args.add(errorCode);
            args.add(severity);
            args.add(errorMessage);
            if (customArgs != null) args.addAll(customArgs.values());

            return ctor.newInstance(args.toArray());
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
