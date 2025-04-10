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

import org.rulii.bind.*;
import org.rulii.bind.match.BindingMatchingStrategy;
import org.rulii.bind.match.ParameterResolver;
import org.rulii.convert.ConverterRegistry;
import org.rulii.lib.spring.util.Assert;
import org.rulii.text.MessageFormatter;
import org.rulii.text.MessageResolver;
import org.rulii.trace.Tracer;
import org.rulii.util.reflect.ObjectFactory;

import java.time.Clock;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Builder class to properly build a RuleContext with the bells and whistles.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class RuleContextBuilder {

    private static final ExecutorService DEFAULT_EXECUTOR_SERVICE = Executors.newFixedThreadPool(Math.max(2, Runtime.getRuntime().availableProcessors()));

    private Bindings bindings;
    private BindingMatchingStrategy matchingStrategy;
    private ParameterResolver parameterResolver;
    private MessageResolver messageResolver;
    private MessageFormatter messageFormatter;
    private ObjectFactory objectFactory;
    private ConverterRegistry converterRegistry;
    private Clock clock;
    private Locale locale;
    private Tracer tracer = Tracer.builder().build();
    private ExecutorService executorService = DEFAULT_EXECUTOR_SERVICE;

    RuleContextBuilder() {
        this(RuleContextOptions.standard());
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
        this.clock = context.getClock();
        this.locale = context.getLocale();
        this.bindings = context.getBindings();
    }

    protected void init(RuleContextOptions options) {
        Assert.notNull(options, "options cannot be null.");
        this.matchingStrategy = options.getMatchingStrategy();
        this.parameterResolver = options.getParameterResolver();
        this.messageResolver = options.getMessageResolver();
        this.messageFormatter = options.getMessageFormatter();
        this.objectFactory = options.getObjectFactory();
        this.converterRegistry = options.getConverterRegistry();
        this.clock = options.getClock();
        this.locale = options.getLocale();
        this.executorService = options.getExecutorService();
    }

    /**
     * Sets the bindings for the RuleContextBuilder.
     *
     * @param bindings the bindings to be set
     * @return RuleContextBuilder for fluency
     */
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

    /**
     * Sets the ParameterResolver for the RuleContextBuilder.
     *
     * @param parameterResolver the ParameterResolver to be set
     * @return RuleContextBuilder for method chaining
     */
    public RuleContextBuilder paramResolver(ParameterResolver parameterResolver) {
        Assert.notNull(objectFactory, "parameterResolver cannot be null.");
        this.parameterResolver = parameterResolver;
        return this;
    }

    /**
     * Sets the MessageResolver for the RuleContextBuilder.
     *
     * @param messageResolver the MessageResolver to be set
     * @return RuleContextBuilder for method chaining
     */
    public RuleContextBuilder messageResolver(MessageResolver messageResolver) {
        Assert.notNull(messageResolver, "messageResolver cannot be null.");
        this.messageResolver = messageResolver;
        return this;
    }

    /**
     * Sets the MessageResolver for the RuleContextBuilder.
     *
     * @param baseNames the base names to be used for the MessageResolver
     * @return RuleContextBuilder for method chaining
     */
    public RuleContextBuilder messageResolver(String...baseNames) {
        Assert.notNull(baseNames, "baseNames cannot be null.");
        this.messageResolver = MessageResolver.builder(baseNames).build();
        return this;
    }

    /**
     * Sets the MessageFormatter for the RuleContextBuilder.
     *
     * @param messageFormatter the MessageFormatter to be set
     * @return RuleContextBuilder for method chaining
     */
    public RuleContextBuilder messageFormatter(MessageFormatter messageFormatter) {
        Assert.notNull(messageFormatter, "messageFormatter cannot be null.");
        this.messageFormatter = messageFormatter;
        return this;
    }

    /**
     * Sets the ObjectFactory for the RuleContextBuilder.
     *
     * @param objectFactory the ObjectFactory to be set
     * @return RuleContextBuilder for method chaining
     */
    public RuleContextBuilder objectFactory(ObjectFactory objectFactory) {
        Assert.notNull(objectFactory, "objectFactory cannot be null.");
        this.objectFactory = objectFactory;
        return this;
    }

    /**
     * Sets the Tracer for the RuleContextBuilder to use for tracing.
     *
     * @param tracer the Tracer to set for tracing
     * @return instance of RuleContextBuilder for method chaining
     */
    public RuleContextBuilder traceUsing(Tracer tracer) {
        Assert.notNull(tracer, "tracer cannot be null.");
        this.tracer = tracer;
        return this;
    }

    /**
     * Sets the ConverterRegistry for the RuleContextBuilder.
     *
     * @param converterRegistry the ConverterRegistry to be set
     * @return RuleContextBuilder for method chaining
     */
    public RuleContextBuilder converterRegistry(ConverterRegistry converterRegistry) {
        Assert.notNull(converterRegistry, "converterRegistry cannot be null.");
        this.converterRegistry = converterRegistry;
        return this;
    }

    /**
     * Sets the locale for the RuleContextBuilder.
     *
     * @param locale The locale to be set.
     * @return RuleContextBuilder for method chaining.
     */
    public RuleContextBuilder locale(Locale locale) {
        Assert.notNull(locale, "locale cannot be null.");
        this.locale = locale;
        return this;
    }

    /**
     * Sets the clock to be used by the RuleContextBuilder.
     *
     * @param clock the Clock instance to set
     * @return RuleContextBuilder for method chaining
     */
    public RuleContextBuilder clock(Clock clock) {
        Assert.notNull(clock, "clock cannot be null.");
        this.clock = clock;
        return this;
    }

    /**
     * Executes using the provided ExecutorService.
     *
     * @param executorService the ExecutorService to execute with
     * @return RuleContextBuilder instance for method chaining
     */
    public RuleContextBuilder executeUsing(ExecutorService executorService) {
        Assert.notNull(executorService, "executorService cannot be null.");
        this.executorService = executorService;
        return this;
    }

    public Bindings getBindings() {
        return bindings;
    }

    public BindingMatchingStrategy getMatchingStrategy() {
        return matchingStrategy;
    }

    public ParameterResolver getParameterResolver() {
        return parameterResolver;
    }

    public MessageResolver getMessageResolver() {
        return messageResolver;
    }

    public MessageFormatter getMessageFormatter() {
        return messageFormatter;
    }

    public ObjectFactory getObjectFactory() {
        return objectFactory;
    }

    public Tracer getTracer() {
        return tracer;
    }

    public ConverterRegistry getConverterRegistry() {
        return converterRegistry;
    }

    public Clock getClock() {
        return clock;
    }

    public Locale getLocale() {
        return locale;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    /**
     * Builds and returns a RuleContext instance with the configured settings.
     *
     * @return RuleContext instance built with the specified bindings, locale, matching strategy,
     * parameter resolver, message resolver, message formatter, object factory, tracer,
     * converter registry, rule registry, clock, and executor service.
     */
    public RuleContext build() {
        ScopedBindings scopedBindings = Bindings.builder().scoped();

        RuleContext result  = new RuleContext(scopedBindings, locale, matchingStrategy, parameterResolver,
                messageResolver, messageFormatter, objectFactory, tracer,
                converterRegistry, clock, executorService);

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
