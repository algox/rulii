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
package org.rulii.context;

import org.rulii.bind.match.BindingMatchingStrategy;
import org.rulii.bind.match.ParameterResolver;
import org.rulii.convert.ConverterRegistry;
import org.rulii.text.MessageFormatter;
import org.rulii.text.MessageResolver;
import org.rulii.util.reflect.ObjectFactory;

import java.time.Clock;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A standard implementation of the {@link RuleContextOptions} interface,
 * which provides default configurations and services for rule execution contexts.
 * This class initializes and manages various components necessary for handling
 * parameter resolution, message formatting, tracing, rule registry, and more.
 *
 * This implementation is intended to serve as the default or baseline configuration
 * within rule-engine frameworks, offering pre-configured instances of commonly used
 * components while still allowing for customization through dependency injection or
 * subclassing if needed.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class StandardRuleContextOptions implements RuleContextOptions {

    private static final ExecutorService DEFAULT_EXECUTOR_SERVICE = Executors.newFixedThreadPool(Math.max(2, Runtime.getRuntime().availableProcessors()));
    private static final StandardRuleContextOptions INSTANCE = new StandardRuleContextOptions();

    private final BindingMatchingStrategy matchingStrategy = BindingMatchingStrategy.builder().build();
    private final ParameterResolver parameterResolver = ParameterResolver.builder().build();
    private final MessageFormatter messageFormatter = MessageFormatter.builder().build();
    private final ConverterRegistry converterRegistry = ConverterRegistry.builder().build();
    private final ObjectFactory objectFactory = ObjectFactory.builder().build();
    private final Clock clock = Clock.systemDefaultZone();
    private final Locale locale = Locale.getDefault();
    private final MessageResolver messageResolver = MessageResolver.builder().build();

    public StandardRuleContextOptions() {
        super();
    }

    /**
     * Provides access to the single shared instance of {@code StandardRuleContextOptions}.
     *
     * @return the singleton instance of {@code StandardRuleContextOptions}.
     */
    public static StandardRuleContextOptions getInstance() {
        return INSTANCE;
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
                '}';
    }
}
