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

/**
 * Composite Condition provides the ability to chain conditions. Such as AND, OR, XOR etc.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public interface CompositeCondition extends Condition {

    /**
     * Textual representation of the composite condition.
     *
     * @return composite action symbol.
     */
    String getSymbol();

    /**
     * Left side of the composite condition.
     *
     * @return left side condition.
     */
    Condition getLeftOperand();

    /**
     * Right side of the composite condition.
     *
     * @return right side condition.
     */
    Condition getRightOperand();

}
