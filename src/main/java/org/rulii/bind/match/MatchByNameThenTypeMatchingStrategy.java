/*
 * This software is licensed under the Apache 2 license, quoted below.
 *
 * Copyright (c) 1999-2026, Algorithmx Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.rulii.bind.match;

/**
 * Matches by name first and, only when no binding carries the name, by type
 * ({@link SmartMatchByTypeMatchingStrategy}): the default composite, as a class with a no-argument
 * constructor so that a parameter can pin it ({@code @Param(matchUsing = MatchByNameThenTypeMatchingStrategy.class)})
 * whatever strategy the context runs with. A parameter whose binding goes by the conventional name
 * ({@code ruleViolations}, say) is then one map lookup; one bound under another name still resolves
 * through the type scan, so pinning this instead of a by-type strategy changes no outcome, only the cost.
 *
 * @author Max Arulananthan
 * @since 2.2
 */
public class MatchByNameThenTypeMatchingStrategy extends CompositeBindingMatchingStrategy {

    public MatchByNameThenTypeMatchingStrategy() {
        super(true, new MatchByNameMatchingStrategy(), new SmartMatchByTypeMatchingStrategy());
    }
}
