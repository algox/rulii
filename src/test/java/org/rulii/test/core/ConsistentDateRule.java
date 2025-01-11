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
package org.rulii.test.core;

import org.rulii.annotation.Description;
import org.rulii.annotation.Given;
import org.rulii.annotation.Otherwise;
import org.rulii.annotation.Rule;
import org.rulii.annotation.Then;

import java.util.Date;

@Rule
@Description("This Rule will validate that the first date is before the second.")
public class ConsistentDateRule {

    public ConsistentDateRule() {
        super();
    }

    @Given // Condition
    public boolean isValid(Date date1, Date date2) {
        return date1.compareTo(date2) < 0;
    }

    @Then // Action
    public void then() {
        System.out.println("Your dates are consistent.");
    }

    @Otherwise() // Else Action
    public void otherwise(Date date1, Date date2) {
        System.out.println("Inconsistent dates. Date 1[" + date1 + "] Date2 [" + date2 + "]");
    }

    /*public static void main(String[] args) {
        org.algorithmx.rules.core.rule.Rule rule = RuleBuilder
                .given(ConditionBuilder.build((Date date1, Date date2) -> date1.compareTo(date2) < 0))
                .then(ActionBuilder.build(() -> System.out.println("Your dates are consistent.")))
                .otherwise(ActionBuilder.build((Date date1, Date date2) -> System.out.println("Inconsistent dates. Date 1[" + date1 + "] Date2 [" + date2 + "]")))
                .given(ConditionBuilder.build((Date date1, Date date2) -> date1.compareTo(date2) < 0))
                .name("ConsistentDateRule")
                .description("This Rule will validate that the first date is before the second.")
                .build();

        rule.run(date1 -> new Date(), date2 -> new Date());
    }*/
}
