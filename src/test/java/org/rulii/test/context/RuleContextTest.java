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
package org.rulii.test.context;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.bind.BindingAlreadyExistsException;
import org.rulii.bind.Bindings;
import org.rulii.bind.ReservedBindings;
import org.rulii.context.RuleContext;
import org.rulii.context.RuleContextBuilder;
import org.rulii.context.RuleContextOptions;

import java.time.Clock;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class contains unit tests for the RuleContext class.
 * It includes tests for building RuleContext with different inputs,
 * handling exceptions, and testing RuleContext options.
 *
 * @author Max Arulananthan
 * @since 1.0
 *
 */
public class RuleContextTest {

    public RuleContextTest() {
        super();
    }

    @Test
    public void test1() {
        Assertions.assertThrows(BindingAlreadyExistsException.class, () -> {
            RuleContext context = RuleContext.builder().build(Bindings.builder().standard());
            context.getBindings().bind(ReservedBindings.RULE_CONTEXT.getName(), 25);
        });
    }

    @Test
    public void testBuildContextWithEmptyInputs() {
        RuleContextBuilder builder = RuleContext.builder().standard();
        RuleContext context = builder.build();
        Assertions.assertNotNull(context, "Rule context should not be null.");
    }

    @Test
    public void testBuildContextWithPopulatedInputs() {
        Clock clock = Clock.systemDefaultZone();
        Locale locale = Locale.getDefault();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        RuleContextBuilder builder = RuleContext.builder().standard();
        builder.locale(locale);
        builder.clock(clock);
        builder.executeUsing(executorService);
        RuleContext context = builder.build();
        Assertions.assertEquals(locale, context.getLocale(), "Expected and actual locale do not match.");
        Assertions.assertEquals(clock, context.getClock(), "Expected and actual clock do not match.");
        Assertions.assertEquals(executorService, context.getExecutorService(), "Expected and actual executor service do not match.");
    }

    @Test
    public void testBuildContextWithRuleContextOptions() {
        RuleContextOptions options = RuleContextOptions.standard();
        RuleContextBuilder builder = RuleContext.builder().with(options);
        RuleContext context = builder.build();
        Assertions.assertNotNull(context, "Rule context should not be null.");
    }
}
