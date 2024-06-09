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
package org.rulii.test.core;

import org.rulii.annotation.Description;
import org.rulii.annotation.Given;
import org.rulii.annotation.PreCondition;
import org.rulii.annotation.Rule;
import org.rulii.annotation.Then;
import org.rulii.bind.Binding;

@Rule(name = "TestRule") @Description("Test Rule")
public class TestRule5 {

    public TestRule5() {
        super();
    }

    @PreCondition
    public boolean preCondition(int x) {
        return x > 10;
    }

    @Given
    public boolean when(int x) {
        return x > 100;
    }

    @Then
    public void then(Binding<Integer> x) {
        x.setValue(0);
    }
}
