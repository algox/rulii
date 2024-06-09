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
package org.rulii.annotation;

import java.lang.annotation.*;

/**
 * Annotation to mark a Rule.
 *
 * The main requirement for a class to be considered a Rule is to have a public "given" method (aka Condition in standard Rule terms).
 * The given method can take arbitrary number of arguments but must return a boolean value. The boolean is the result of the rule.
 * The Rule class can also have optionally one or more "then" methods (aka Then action in standard Rule terms).
 * The Rule class can also have an optional preCondition method which will determine whether the Rule should be run at all.
 *
 * Example:
 *
 * <pre>
 * {@code @Rule}
 * public class TestRule {
 *
 *    // Default Ctor
 *    public TestRule() {
 *        super();
 *    }
 *
 *    {@code @PreCondition}
 *    public boolean given(Date age) {
 *        // Pre-condition logic
 *    }
 *
 *    {@code @Given}
 *    public boolean given(Date age) {
 *      // Rule Logic
 *      return true;
 *    }
 *
 *    {@code @Then}
 *    public void then(Date age) {
 *        // Then Action 1
 *    }
 *
 *    {@code @Otherwise}
 *    public void then(Date age) {
 *      // Else Action
 *    }
 * }
 *</pre>
 *
 * @author Max Arulananthan
 * @since 1.0
 * @see org.rulii.rule.Rule
 *
 * Please note that Rules created under a non-managed environment(like Spring) will require a default constructor.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Rule {

    String NOT_APPLICABLE = "";

    /**
     * Name of the Rule.
     *
     * @return Name of the Rule.
     */
    String name() default NOT_APPLICABLE;
}
