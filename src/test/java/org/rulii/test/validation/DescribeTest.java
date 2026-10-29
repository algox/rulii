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
package org.rulii.test.validation;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.rulii.validation.rules.Validators.*;

/**
 * A value rule built without a description says what it checks, naming the value.
 *
 * @author Max Arulananthan
 * @since 2.2
 */
public class DescribeTest {

    @Test
    public void rulesDescribeWhatTheyCheckNamingTheValue() {
        assertEquals("code size must be between 3 and 20", size(binding("code"), 3, 20).build().getDefinition().getDescription());
        assertEquals("email must not be null", notNull(binding("email")).build().getDefinition().getDescription());
        assertEquals("country must match [A-Z]{2}", pattern(binding("country"), "[A-Z]{2}").build().getDefinition().getDescription());
        assertEquals("quantity must be greater than or equal to 1", min(binding("quantity"), 1).build().getDefinition().getDescription());
        assertEquals("status must be one of [a, b]", in(binding("status"), List.of("a", "b")).build().getDefinition().getDescription());
    }

    @Test
    public void builderDescribesTheConfigurationSoFar() {
        assertEquals("total must be less than or equal to 10", decimalMax(binding("total"), BigDecimal.TEN).describe());
        assertEquals("total must be less than 10", decimalMax(binding("total"), BigDecimal.TEN).inclusive(false).describe());
    }

    @Test
    public void anExplicitDescriptionWins() {
        assertEquals("the code", size(binding("code"), 3, 20).description("the code").build().getDefinition().getDescription());
    }
}
