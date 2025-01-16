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
package org.rulii.test.core.condition;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.bind.Bindings;
import org.rulii.context.RuleContext;
import org.rulii.model.UnrulyException;
import org.rulii.model.condition.CompositeCondition;
import org.rulii.model.condition.Condition;

/**
 * This class contains unit tests for the CompositeCondition class, which represents
 * a condition that can be composed of multiple sub-conditions.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class CompositeConditionTest {

    public CompositeConditionTest() {
        super();
    }

    @Test
    public void testNotMethod() {
        Condition trueCondition = RuleContext -> true;
        Condition falseCondition = trueCondition.not();

        try {
            Assertions.assertFalse(falseCondition.isTrue(RuleContext.builder().build()));
        } catch (UnrulyException e) {
            Assertions.fail("Test failed due to UnrulyException: " + e.getMessage());
        }
    }

    Condition condition1 = new Condition() {
        @Override
        public Boolean run(RuleContext context) {
            return true;
        }
    };
    Condition condition2 = new Condition() {
        @Override
        public Boolean run(RuleContext context) {
            return false;
        }
    };

    // Testing the 'and()' method of the Condition interface
    @Test
    public void testConditionAnd() {
        // Creating an CompositeCondition by chaining the conditions using the 'and()' method
        CompositeCondition andCondition = condition1.and(condition2);
        // Asserting that both the left and right operands inside
        // the composite condition are as per the 'and()' chaining we provided.
        Assertions.assertEquals(condition1, andCondition.getLeftOperand());
        Assertions.assertEquals(condition2, andCondition.getRightOperand());
        // Checking the symbol of the composite condition - it should be '&&' for 'and()' operation
        Assertions.assertEquals("&&", andCondition.getSymbol());
        // Running the composite AND condition
        Assertions.assertFalse(andCondition.isTrue());
    }

    /**
     * Test case for 'or' method when both conditions are true.
     */
    @Test
    void or_condition_both_true() throws UnrulyException {
        Condition condition1 = (RuleContext context) -> true;
        Condition condition2 = (RuleContext context) -> true;
        CompositeCondition compositeCondition = condition1.or(condition2);
        Assertions.assertTrue(compositeCondition.isTrue());  // It should be true as both conditions are true.
    }

    /**
     * Test case for 'or' method when either of the conditions is true.
     */
    @Test
    void or_condition_either_true() throws UnrulyException {
        Condition condition1 = (RuleContext context) -> true;
        Condition condition2 = (RuleContext context) -> false;
        CompositeCondition compositeCondition = condition1.or(condition2);
        Assertions.assertTrue(compositeCondition.isTrue());  // It should be true as one of the conditions is true.
    }

    /**
     * Test case for 'or' method when both conditions are false.
     */
    @Test
    void or_condition_both_false() throws UnrulyException {
        Condition condition1 = (RuleContext context) -> false;
        Condition condition2 = (RuleContext context) -> false;
        CompositeCondition compositeCondition = condition1.or(condition2);
        Assertions.assertFalse(compositeCondition.isTrue());  // It should be false as both conditions are false.
    }

    @Test
    public void xorTest_sameConditions() throws UnrulyException {
        Condition condition1 = (context) -> true;
        CompositeCondition xorCondition = condition1.xor(condition1);
        RuleContext context = RuleContext.builder().build(Bindings.builder().standard());
        Assertions.assertFalse(xorCondition.isTrue(context));
    }

    @Test
    public void xorTest_differentConditions() throws UnrulyException {
        Condition condition1 = (context) -> true;
        Condition condition2 = (context) -> false;
        CompositeCondition xorCondition = condition1.xor(condition2);
        RuleContext context = RuleContext.builder().build(Bindings.builder().scoped());
        Assertions.assertTrue(xorCondition.isTrue(context));
    }
}
