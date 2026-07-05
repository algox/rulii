/*
 * This software is licensed under the Apache 2 license, quoted below.
 *
 * Copyright (c) 1999-2026, Algorithmx Inc.
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
import org.rulii.bind.ScopedBindings;
import org.rulii.bind.match.BindingMatchingStrategy;
import org.rulii.bind.match.ParameterResolver;
import org.rulii.context.RuleContext;
import org.rulii.context.RuleContextBuilder;
import org.rulii.context.RuleContextOptions;
import org.rulii.convert.ConverterRegistry;
import org.rulii.registry.RuleRegistry;
import org.rulii.script.Script;
import org.rulii.script.ScriptCompiler;
import org.rulii.script.ScriptProcessor;
import org.rulii.script.ScriptProcessorFactory;
import org.rulii.script.ScriptProcessorManager;
import org.rulii.text.MessageFormatter;
import org.rulii.text.MessageResolver;
import org.rulii.trace.Tracer;
import org.rulii.util.reflect.ObjectFactory;

import java.time.Clock;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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

    @Test
    public void testBuildFromOptions_picksUpCustomTracer() {
        Tracer customTracer = Tracer.builder().build();
        RuleContextOptions defaults = RuleContextOptions.standard();

        RuleContextOptions options = new RuleContextOptions() {
            @Override
            public BindingMatchingStrategy getMatchingStrategy() {
                return defaults.getMatchingStrategy();
            }

            @Override
            public ParameterResolver getParameterResolver() {
                return defaults.getParameterResolver();
            }

            @Override
            public MessageResolver getMessageResolver() {
                return defaults.getMessageResolver();
            }

            @Override
            public MessageFormatter getMessageFormatter() {
                return defaults.getMessageFormatter();
            }

            @Override
            public ObjectFactory getObjectFactory() {
                return defaults.getObjectFactory();
            }

            @Override
            public ConverterRegistry getConverterRegistry() {
                return defaults.getConverterRegistry();
            }

            @Override
            public Clock getClock() {
                return defaults.getClock();
            }

            @Override
            public Locale getLocale() {
                return defaults.getLocale();
            }

            @Override
            public ExecutorService getExecutorService() {
                return defaults.getExecutorService();
            }

            @Override
            public RuleRegistry getRuleRegistry() {
                return defaults.getRuleRegistry();
            }

            @Override
            public Tracer getTracer() {
                return customTracer;
            }
        };

        RuleContext context = RuleContext.builder().with(options).build();

        Assertions.assertSame(customTracer, context.getTracer(),
                "A custom RuleContextOptions' tracer must be picked up when building a fresh context.");
    }

    @Test
    public void testAsImmutable_preservesIdAndCreationTime() {
        RuleContext context = RuleContext.builder().build(Bindings.builder().standard());

        RuleContext immutable = context.asImmutable();

        Assertions.assertEquals(context.getId(), immutable.getId(),
                "asImmutable() must preserve the source context's identity.");
        Assertions.assertEquals(context.getCreationTime(), immutable.getCreationTime(),
                "asImmutable() must preserve the source context's creation time.");
    }

    @Test
    public void testStandardOptions_executorService_isBoundedWithCallerRunsPolicy() {
        ExecutorService executorService = RuleContextOptions.standard().getExecutorService();

        Assertions.assertInstanceOf(ThreadPoolExecutor.class, executorService);
        ThreadPoolExecutor pool = (ThreadPoolExecutor) executorService;

        Assertions.assertInstanceOf(ThreadPoolExecutor.CallerRunsPolicy.class, pool.getRejectedExecutionHandler(),
                "Default executor must use a caller-runs rejection policy for graceful degradation under load.");
        Assertions.assertNotEquals(Integer.MAX_VALUE, pool.getQueue().remainingCapacity(),
                "Default executor's work queue must be bounded, not unbounded.");
    }

    @Test
    public void testParamResolver_nullArgument_throwsIllegalArgumentException() {
        RuleContextBuilder builder = RuleContext.builder().standard();
        Assertions.assertThrows(IllegalArgumentException.class, () -> builder.paramResolver(null));
    }

    @Test
    public void testCopyContext_shadowedBindingNameAcrossScopes_doesNotThrow() {
        RuleContext context = RuleContext.builder().build(Bindings.builder().standard(x -> "outer"));
        ScopedBindings scopedBindings = context.getBindings();
        scopedBindings.addScope("nested-scope", Bindings.builder().standard(x -> "inner"));

        RuleContext copy = RuleContext.builder().with(context).build();

        Assertions.assertEquals("inner", copy.getBindings().getValue("x"),
                "Copy should keep the innermost (child) scope's shadowed value.");
    }

    @Test
    public void testCopyContext_laterMutationOfOriginal_notVisibleInCopy() {
        RuleContext context = RuleContext.builder().build(Bindings.builder().standard(x -> "original"));

        RuleContext copy = RuleContext.builder().with(context).build();

        context.getBindings().setValue("x", "mutated");

        Assertions.assertEquals("mutated", context.getBindings().getValue("x"));
        Assertions.assertEquals("original", copy.getBindings().getValue("x"),
                "Copy must reflect the value at copy time, not later mutations of the original.");
    }

    @Test
    public void testGetScriptProcessor_concurrentAccess_returnsSameCachedInstance() throws InterruptedException {
        String language = "race-test-lang-" + UUID.randomUUID();

        ScriptProcessorFactory factory = new ScriptProcessorFactory() {
            @Override
            public String getLanguageName() {
                return language;
            }

            @Override
            public String getBindingsName() {
                return "ctx";
            }

            @Override
            public ScriptProcessor getScriptProcessor() {
                return new ScriptProcessor() {
                    @Override
                    public String getLanguageName() {
                        return language;
                    }

                    @Override
                    public String getBindingsName() {
                        return "ctx";
                    }

                    @Override
                    public <T> T evaluate(Script<T> script, RuleContext ruleContext) {
                        return null;
                    }
                };
            }

            @Override
            public ScriptCompiler getScriptCompiler() {
                return new ScriptCompiler() {
                    @Override
                    public String getLanguageName() {
                        return language;
                    }

                    @Override
                    public <T> Script<T> compile(String script, Class<?> returnType) {
                        return null;
                    }
                };
            }
        };
        new ScriptProcessorManager().register(factory);

        RuleContext context = RuleContext.builder().build(Bindings.builder().standard());

        int threadCount = 32;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        Set<ScriptProcessor> observed = ConcurrentHashMap.newKeySet();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                observed.add(context.getScriptProcessor(language));
            });
        }

        ready.await();
        start.countDown();
        executor.shutdown();
        Assertions.assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        Assertions.assertEquals(1, observed.size(), "All threads must observe the same cached ScriptProcessor instance.");
    }
}
