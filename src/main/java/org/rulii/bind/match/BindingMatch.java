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
package org.rulii.bind.match;

import org.rulii.bind.Binding;
import org.rulii.lib.spring.util.Assert;

/**
 * Pairing of a Binding with the strategy type that was used to match it.
 *
 * @param <T> declared type of the Binding.
 * @author Max Arulananthan
 */
public class BindingMatch<T> {

    private final Binding<T> binding;
    private final Class<? extends BindingMatchingStrategy> strategyUsed;

    public BindingMatch(Binding<T> binding, Class<? extends BindingMatchingStrategy> strategyUsed) {
        super();
        Assert.notNull(strategyUsed, "strategyUsed cannot be null.");
        this.binding = binding;
        this.strategyUsed = strategyUsed;
    }

    public Binding<T> getBinding() {
        return binding;
    }

    public Class<? extends BindingMatchingStrategy> getStrategyUsed() {
        return strategyUsed;
    }

    @Override
    public String toString() {
        return "BindingMatch{" +
                "binding=" + binding +
                ", strategyUsed=" + strategyUsed +
                '}';
    }
}
