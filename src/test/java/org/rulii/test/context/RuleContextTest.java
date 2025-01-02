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
package org.rulii.test.context;

import org.rulii.bind.BindingAlreadyExistsException;
import org.rulii.bind.Bindings;
import org.rulii.bind.ReservedBindings;
import org.rulii.context.RuleContext;
import org.junit.Test;

public class RuleContextTest {

    public RuleContextTest() {
        super();
    }

    @Test()
    public void test1() {
        RuleContext context = RuleContext.builder().build(Bindings.builder().standard());
        //context.getBindings().bind(ReservedBindings.RULE_CONTEXT.getName(), 25);

        System.err.println(context.toString());
    }
}
