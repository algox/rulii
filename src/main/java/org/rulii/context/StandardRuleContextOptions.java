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
package org.rulii.context;

import org.rulii.bind.match.BindingMatchingStrategy;
import org.rulii.bind.match.ParameterResolver;
import org.rulii.convert.ConverterRegistry;
import org.rulii.registry.RuleRegistry;
import org.rulii.text.MessageFormatter;
import org.rulii.text.MessageResolver;
import org.rulii.util.reflect.ObjectFactory;

import java.time.Clock;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Default implementation of {@link RuleContextOptions} that wires together the standard services
 * used by the rule engine.
 *
 * <p>All components are initialized with their own default builders:
 * <ul>
 *   <li>{@link BindingMatchingStrategy} — match-by-name-and-type</li>
 *   <li>{@link ParameterResolver} — standard resolver</li>
 *   <li>{@link MessageFormatter} and {@link MessageResolver} — standard builders</li>
 *   <li>{@link ConverterRegistry} — standard converters</li>
 *   <li>{@link ObjectFactory} — standard factory</li>
 *   <li>{@link Clock} — system default zone</li>
 *   <li>{@link java.util.Locale} — JVM default locale</li>
 *   <li>{@link java.util.concurrent.ExecutorService} — fixed thread pool sized to available processors</li>
 * </ul>
 *
 * <p>Obtain an instance via {@link RuleContextOptions#standard()} or {@link #build()}.
 *
 * @author Max Arulananthan
 * @since 1.0
 * @see RuleContextOptions
 */
public class StandardRuleContextOptions implements RuleContextOptions {

    private static final ExecutorService DEFAULT_EXECUTOR_SERVICE = Executors.newFixedThreadPool(Math.max(2, Runtime.getRuntime().availableProcessors()));

    private final BindingMatchingStrategy matchingStrategy = BindingMatchingStrategy.builder().build();
    private final ParameterResolver parameterResolver = ParameterResolver.builder().build();
    private final MessageFormatter messageFormatter = MessageFormatter.builder().build();
    private final ConverterRegistry converterRegistry = ConverterRegistry.builder().build();
    private final ObjectFactory objectFactory = ObjectFactory.builder().build();
    private final Clock clock = Clock.systemDefaultZone();
    private final Locale locale = Locale.getDefault();
    private final MessageResolver messageResolver = MessageResolver.builder().build();
    private final RuleRegistry ruleRegistry = RuleRegistry.builder().build();

    public StandardRuleContextOptions() {
        super();
    }

    /**
     * Creates and returns a new {@code StandardRuleContextOptions} instance with default settings.
     *
     * @return a new instance; never null.
     */
    public static StandardRuleContextOptions build() {
        return new StandardRuleContextOptions();
    }

    @Override
    public BindingMatchingStrategy getMatchingStrategy() {
        return matchingStrategy;
    }

    @Override
    public ParameterResolver getParameterResolver() {
        return parameterResolver;
    }

    @Override
    public MessageResolver getMessageResolver() {
        return messageResolver;
    }

    @Override
    public MessageFormatter getMessageFormatter() {
        return messageFormatter;
    }

    @Override
    public ObjectFactory getObjectFactory() {
        return objectFactory;
    }

    @Override
    public ConverterRegistry getConverterRegistry() {
        return converterRegistry;
    }

    @Override
    public Clock getClock() {
        return clock;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public ExecutorService getExecutorService() {
        return DEFAULT_EXECUTOR_SERVICE;
    }

    @Override
    public RuleRegistry getRuleRegistry() {
        return ruleRegistry;
    }

    @Override
    public String toString() {
        return "StandardRuleContextOptions{" +
                "matchingStrategy=" + matchingStrategy +
                ", parameterResolver=" + parameterResolver +
                ", messageFormatter=" + messageFormatter +
                ", converterRegistry=" + converterRegistry +
                ", objectFactory=" + objectFactory +
                ", clock=" + clock +
                ", locale=" + locale +
                ", messageResolver=" + messageResolver +
                ", executorService=" + getExecutorService() +
                '}';
    }
}
