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
package org.rulii.lang.condition;

import org.rulii.context.RuleContext;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.UnrulyException;

import java.util.function.BiPredicate;

/**
 * Default implementation of a Composition Condition.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class DefaultCompositeCondition implements CompositeCondition {

    private final Condition leftOperand;
    private final Condition rightOperand;
    private final String symbol;
    private final BiPredicate<Boolean, Boolean> predicate;

    public DefaultCompositeCondition(Condition leftOperand, Condition rightOperand, String symbol, BiPredicate<Boolean, Boolean> predicate) {
        super();
        Assert.notNull(leftOperand, "leftOperand cannot be null.");
        Assert.notNull(rightOperand, "rightOperand cannot be null.");
        Assert.notNull(symbol, "symbol cannot be null.");
        Assert.notNull(predicate, "predicate cannot be null.");
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
        this.symbol = symbol;
        this.predicate = predicate;
    }

    @Override
    public Boolean run(RuleContext context) throws UnrulyException {
        boolean leftResult = leftOperand.isTrue(context);
        boolean rightResult = rightOperand.isTrue(context);
        return predicate.test(leftResult, rightResult);
    }

    @Override
    public Condition getLeftOperand() {
        return leftOperand;
    }

    @Override
    public Condition getRightOperand() {
        return rightOperand;
    }

    @Override
    public String getSymbol() {
        return symbol;
    }

    @Override
    public String getName() {
        return symbol;
    }

    @Override
    public String getDescription() {
        return "(" + leftOperand.getDescription() + " " + symbol + " " + rightOperand.getDescription() + ")";
    }

    @Override
    public String toString() {
        return getDescription();
    }
}
