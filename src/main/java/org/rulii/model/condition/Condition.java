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
package org.rulii.model.condition;

import org.rulii.bind.BindingDeclaration;
import org.rulii.bind.Bindings;
import org.rulii.context.RuleContext;
import org.rulii.model.Runnable;
import org.rulii.model.Definable;
import org.rulii.model.MethodDefinition;
import org.rulii.model.UnrulyException;

/**
 * Represents an operation(if condition) that accepts input arguments performs some logic to return a boolean value.
 *
 * @author Max Arulananthan
 * @since 1.0
 *
 */
@FunctionalInterface
public interface Condition extends Runnable<Boolean> {

    static ConditionBuilderBuilder builder() {
        return ConditionBuilderBuilder.getInstance();
    }

    /**
     * Derives all the arguments and executes this Condition.
     *
     * @param context Rule Context.
     * @return result of the function.
     * @throws UnrulyException thrown if there are any errors during the Condition execution.
     */
    default boolean isTrue(RuleContext context) throws UnrulyException {
        return run(context);
    }

    /**
     * Derives all the arguments and executes this Condition.
     *
     * @param params Condition Parameters.
     * @return true if the Condition passed; false otherwise.
     * @throws UnrulyException thrown if there are any errors during the Condition execution.
     */
    default boolean isTrue(BindingDeclaration<?>...params) throws UnrulyException {
        Bindings bindings = Bindings.builder().standard(params);
        return run(RuleContext.builder().build(bindings));
    }

    /**
     * Creates a NOT of this condition.
     *
     * @return NOT condition.
     */
    default Condition not() {
        return new NotCondition(this);
    }

    /**
     * Creates chained AND condition of this condition AND the given one.
     *
     * @param condition desired right side of an AND condition.
     * @return new AND condition.
     */
    default CompositeCondition and(Condition condition) {
        return new DefaultCompositeCondition(this, condition, "&&", (a, b) -> a && b);
    }

    /**
     * Creates chained OR condition of this condition OR the given one.
     *
     * @param condition desired right side of an OR condition.
     * @return new OR condition.
     */
    default CompositeCondition or(Condition condition) {
        return new DefaultCompositeCondition(this, condition, "||", (a, b) -> a || b);
    }

    /**
     * Creates chained XOR (exclusive or) condition of this condition XOR the given one.
     *
     * @param condition desired right side of an XOR condition.
     * @return new XOR condition.
     */
    default CompositeCondition xor(Condition condition) {
        return new DefaultCompositeCondition(this, condition, "^", (a, b) -> a ^ b);
    }

    /**
     * Meta information about the Function.
     *
     * @return Function meta information.
     */
    @SuppressWarnings("unchecked")
    default MethodDefinition getDefinition() {
        return this instanceof Definable ? ((Definable<MethodDefinition>) this).getDefinition() : null;
    }

    @Override
    default String getName() {
        return "anonymous-condition";
    }
}
