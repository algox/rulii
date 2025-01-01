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
package org.rulii.context;

import org.rulii.bind.*;
import org.rulii.bind.match.BindingMatchingStrategy;
import org.rulii.bind.match.ParameterResolver;
import org.rulii.convert.ConverterRegistry;
import org.rulii.lib.spring.util.Assert;
import org.rulii.registry.RuleRegistry;
import org.rulii.text.MessageFormatter;
import org.rulii.text.MessageResolver;
import org.rulii.trace.DefaultTracer;
import org.rulii.trace.ExecutionListener;
import org.rulii.trace.Tracer;
import org.rulii.util.reflect.ObjectFactory;

import java.time.Clock;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Builder class to properly build a RuleContext with the bells and whistles.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class RuleContextBuilder implements RuleContextOptions {

    private static final ExecutorService DEFAULT_EXECUTOR_SERVICE = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private Bindings bindings;
    private BindingMatchingStrategy matchingStrategy;
    private ParameterResolver parameterResolver;
    private MessageResolver messageResolver;
    private MessageFormatter messageFormatter;
    private ObjectFactory objectFactory;
    private Tracer tracer;
    private ConverterRegistry converterRegistry;
    private RuleRegistry ruleRegistry;
    private Clock clock;
    private Locale locale;
    private ExecutorService executorService = DEFAULT_EXECUTOR_SERVICE;

    RuleContextBuilder() {
        super();
    }

    RuleContextBuilder(RuleContextOptions configuration) {
        super();
        init(configuration);
    }

    RuleContextBuilder(RuleContext context) {
        super();
        Assert.notNull(context, "context cannot be null.");
        this.matchingStrategy = context.getMatchingStrategy();
        this.parameterResolver = context.getParameterResolver();
        this.messageResolver = context.getMessageResolver();
        this.messageFormatter = context.getMessageFormatter();
        this.objectFactory = context.getObjectFactory();
        this.tracer = context.getTracer();
        this.converterRegistry = context.getConverterRegistry();
        this.ruleRegistry = context.getRuleRegistry();
        this.clock = context.getClock();
        this.locale = context.getLocale();
        this.bindings = context.getBindings();
    }

    protected void init(RuleContextOptions configuration) {
        Assert.notNull(configuration, "configuration cannot be null.");
        this.matchingStrategy = configuration.getMatchingStrategy();
        this.parameterResolver = configuration.getParameterResolver();
        this.messageResolver = configuration.getMessageResolver();
        this.messageFormatter = configuration.getMessageFormatter();
        this.objectFactory = configuration.getObjectFactory();
        this.converterRegistry = configuration.getConverterRegistry();
        this.ruleRegistry = configuration.getRuleRegistry();
        this.clock = configuration.getClock();
        this.locale = configuration.getLocale();
    }

    public RuleContextBuilder merge(RuleContextOptions options) {
        Assert.notNull(options, "options cannot be null.");
        if (options.getMatchingStrategy() != null) this.matchingStrategy = options.getMatchingStrategy();
        if (options.getParameterResolver() != null) this.parameterResolver = options.getParameterResolver();
        if (options.getMessageResolver() != null) this.messageResolver = options.getMessageResolver();
        if (options.getMessageFormatter() != null) this.messageFormatter = options.getMessageFormatter();
        if (options.getObjectFactory() != null) this.objectFactory = options.getObjectFactory();
        if (options.getConverterRegistry() != null) this.converterRegistry = options.getConverterRegistry();
        if (options.getRuleRegistry() != null) this.ruleRegistry = options.getRuleRegistry();
        if (options.getClock() != null) this.clock = options.getClock();
        if (options.getLocale() != null) this.locale = options.getLocale();
        return this;
    }

    public RuleContextOptions getOptions() {
        return this;
    }

    public RuleContextBuilder bindings(Bindings bindings) {
        Assert.notNull(bindings, "bindings cannot be null.");
        this.bindings = bindings;
        return this;
    }

    /**
     * Sets the matching strategy to use.
     *
     * @param strategy matching strategy.
     * @return this for fluency.
     */
    public RuleContextBuilder matchUsing(BindingMatchingStrategy strategy) {
        Assert.notNull(strategy, "strategy cannot be null.");
        this.matchingStrategy = strategy;
        return this;
    }

    public RuleContextBuilder paramResolver(ParameterResolver parameterResolver) {
        Assert.notNull(objectFactory, "parameterResolver cannot be null.");
        this.parameterResolver = parameterResolver;
        return this;
    }

    public RuleContextBuilder messageResolver(MessageResolver messageResolver) {
        Assert.notNull(messageResolver, "messageResolver cannot be null.");
        this.messageResolver = messageResolver;
        return this;
    }

    public RuleContextBuilder messageResolver(String...baseNames) {
        Assert.notNull(baseNames, "baseNames cannot be null.");
        this.messageResolver = MessageResolver.builder(baseNames).build();
        return this;
    }

    public RuleContextBuilder messageFormatter(MessageFormatter messageFormatter) {
        Assert.notNull(messageFormatter, "messageFormatter cannot be null.");
        this.messageFormatter = messageFormatter;
        return this;
    }

    public RuleContextBuilder objectFactory(ObjectFactory objectFactory) {
        Assert.notNull(objectFactory, "objectFactory cannot be null.");
        this.objectFactory = objectFactory;
        return this;
    }

    public RuleContextBuilder traceUsing(Tracer tracer) {
        Assert.notNull(tracer, "tracer cannot be null.");
        this.tracer = tracer;
        return this;
    }

    public RuleContextBuilder converterRegistry(ConverterRegistry converterRegistry) {
        Assert.notNull(converterRegistry, "converterRegistry cannot be null.");
        this.converterRegistry = converterRegistry;
        return this;
    }

    public RuleContextBuilder ruleRegistry(RuleRegistry ruleRegistry) {
        Assert.notNull(ruleRegistry, "ruleRegistry cannot be null.");
        this.ruleRegistry = ruleRegistry;
        return this;
    }

    public RuleContextBuilder locale(Locale locale) {
        Assert.notNull(locale, "locale cannot be null.");
        this.locale = locale;
        return this;
    }

    public RuleContextBuilder clock(Clock clock) {
        Assert.notNull(clock, "clock cannot be null.");
        this.clock = clock;
        return this;
    }

    public RuleContextBuilder executorService(ExecutorService executorService) {
        Assert.notNull(executorService, "executorService cannot be null.");
        this.executorService = executorService;
        return this;
    }

    public Bindings getBindings() {
        return bindings;
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
    public Tracer getTracer() {
        return tracer;
    }

    @Override
    public ConverterRegistry getConverterRegistry() {
        return converterRegistry;
    }

    @Override
    public RuleRegistry getRuleRegistry() {
        return ruleRegistry;
    }

    public Clock getClock() {
        return clock;
    }

    public Locale getLocale() {
        return locale;
    }

    /**
     * Builds a Rule Context with desired parameters.
     *
     * @return new Rule Context.
     */
    public RuleContext build() {
        ScopedBindings scopedBindings = Bindings.builder().scoped();

        RuleContext result  = new RuleContext(scopedBindings, locale, matchingStrategy, parameterResolver,
                messageResolver, messageFormatter, objectFactory, tracer != null ? tracer : new DefaultTracer(),
                converterRegistry, ruleRegistry, clock, executorService);

        // Make the Bindings are avail.
        ((PromiscuousBinder) (scopedBindings.getRootScope().getBindings())).promiscuousBind(Binding.builder()
                .with(ReservedBindings.BINDINGS.getName())
                    .type(Bindings.class)
                    .isFinal(true)
                    .value(scopedBindings)
                .build());

        // Make the Context avail in the bindings.
        ((PromiscuousBinder) (scopedBindings.getRootScope().getBindings())).promiscuousBind(Binding.builder()
                .with(ReservedBindings.RULE_CONTEXT.getName())
                    .type(RuleContext.class)
                    .isFinal(true)
                    .value(result)
                .build());

        scopedBindings.addScope(ScopedBindings.GLOBAL_SCOPE, bindings != null ? bindings : Bindings.builder().standard());

        return result;
    }
}
