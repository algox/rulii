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

import org.junit.jupiter.api.Test;
import org.rulii.bind.Bindings;

/**
 * Tests for updates to Binding.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class UpdateRuleTest {

    public UpdateRuleTest() {
        super();
    }

    @Test
    public void test1() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("x", int.class, 11);
        bindings.bind("y", int.class, 10);

        // TODO : Fix (Binding<Integer> y
        /*Rule rule = ruleFactory.rule(cond1((Integer x) -> x > 10))
                .then(act1((Binding<Integer> y) -> y.setValue(100)));
        rule.run(binds);

        Assert.assertTrue((int) binds.get("y") == 100);*/
    }
}
