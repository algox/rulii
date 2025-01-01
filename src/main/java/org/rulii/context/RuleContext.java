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

import org.rulii.bind.ScopedBindings;
import org.rulii.bind.match.BindingMatchingStrategy;
import org.rulii.bind.match.ParameterMatch;
import org.rulii.bind.match.ParameterResolver;
import org.rulii.convert.ConverterRegistry;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.Immutator;
import org.rulii.model.MethodDefinition;
import org.rulii.registry.RuleRegistry;
import org.rulii.text.MessageFormatter;
import org.rulii.text.MessageResolver;
import org.rulii.trace.ExecutionTracker;
import org.rulii.trace.Tracer;
import org.rulii.util.reflect.ObjectFactory;

import java.time.Clock;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

/**
 * Responsible for state management during Rule execution. This class provides access to everything that is required
 * by the Rule Engine to execute a given set of Rules.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class RuleContext implements Immutator<RuleContext> {

    public static RuleContextBuilderBuilder builder() {
        return RuleContextBuilderBuilder.getInstance();
    }

    private final String id = UUID.randomUUID().toString();
    private final Date creationTime = new Date();
    private final ScopedBindings bindings;
    private final Locale locale;
    private final BindingMatchingStrategy matchingStrategy;
    private final ParameterResolver parameterResolver;
    private final MessageResolver messageResolver;
    private final MessageFormatter messageFormatter;
    private final ObjectFactory objectFactory;
    private final Tracer tracer;
    private final ConverterRegistry converterRegistry;
    private final RuleRegistry ruleRegistry;
    private final Clock clock;
    private final ExecutionTracker executionContext = new ExecutionTracker();
    private final ExecutorService executorService;

    RuleContext(ScopedBindings bindings, Locale locale, BindingMatchingStrategy matchingStrategy,
                ParameterResolver parameterResolver, MessageResolver messageResolver,
                MessageFormatter messageFormatter, ObjectFactory objectFactory,
                Tracer tracer, ConverterRegistry converterRegistry,
                RuleRegistry ruleRegistry, Clock clock, ExecutorService executorService) {
        super();
        Assert.notNull(bindings, "bindings cannot be null.");
        Assert.notNull(locale, "locale cannot be null.");
        Assert.notNull(matchingStrategy, "matchingStrategy cannot be null.");
        Assert.notNull(parameterResolver, "parameterResolver cannot be null.");
        Assert.notNull(messageFormatter, "messageFormatter cannot be null.");
        Assert.notNull(messageResolver, "messageResolver cannot be null.");
        Assert.notNull(objectFactory, "objectFactory cannot be null.");
        Assert.notNull(tracer, "tracer cannot be null.");
        Assert.notNull(converterRegistry, "converterRegistry cannot be null.");
        Assert.notNull(ruleRegistry, "ruleRegistry cannot be null.");
        Assert.notNull(clock, "clock cannot be null.");
        Assert.notNull(executorService, "executorService cannot be null.");
        this.bindings = bindings;
        this.locale = locale;
        this.matchingStrategy = matchingStrategy;
        this.parameterResolver = parameterResolver;
        this.messageFormatter = messageFormatter;
        this.messageResolver = messageResolver;
        this.objectFactory = objectFactory;
        this.tracer = tracer;
        this.converterRegistry = converterRegistry;
        this.ruleRegistry = ruleRegistry;
        this.clock = clock;
        this.executorService = executorService;
    }

    public List<ParameterMatch> match(MethodDefinition definition) {
        return getParameterResolver().match(definition, getBindings(), getMatchingStrategy(), getObjectFactory());
    }

    public List<Object> resolve(List<ParameterMatch> matches, MethodDefinition definition) {
        return getParameterResolver().resolve(matches, definition, getBindings(), matchingStrategy, getConverterRegistry(), getObjectFactory());
    }

    public String resolveMessage(String code) {
        return resolveMessage(code, null);
    }

    public String resolveMessage(String code, String defaultMessage) {
        return getMessageResolver().resolve(getLocale(), code, defaultMessage);
    }

    /**
     * Returns the Bindings.
     *
     * @return Bindings. Cannot be null.
     */
    public ScopedBindings getBindings() {
        return bindings;
    }

    /**
     * Returns the matching strategy to be used.
     *
     * @return matching strategy (cannot be null).
     */
    public BindingMatchingStrategy getMatchingStrategy() {
        return matchingStrategy;
    }

    /**
     * Returns the Parameter resolver being used.
     *
     * @return parameter resolver. Cannot be null.
     */
    protected ParameterResolver getParameterResolver() {
        return parameterResolver;
    }

    public MessageResolver getMessageResolver() {
        return messageResolver;
    }

    public MessageFormatter getMessageFormatter() {
        return messageFormatter;
    }

    /**
     * Returns the ObjectFactory being used.
     *
     * @return Object Factory. cannot be null.
     */
    public ObjectFactory getObjectFactory() {
        return objectFactory;
    }

    public Tracer getTracer() {
        return tracer;
    }
    /**
     * Returns the ConverterRegistry being used.
     *
     * @return Converter Registry. Cannot be null.
     */
    public ConverterRegistry getConverterRegistry() {
        return converterRegistry;
    }

    public RuleRegistry getRuleRegistry() {
        return ruleRegistry;
    }

    public Locale getLocale() {
        return locale;
    }

    public Clock getClock() {
        return clock;
    }

    public String getId() {
        return id;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public ExecutionTracker getExecutionContext() {
        return executionContext;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    @Override
    public RuleContext asImmutable() {
        return new RuleContext(bindings.asImmutable(), locale, matchingStrategy, parameterResolver, messageResolver,
                messageFormatter, objectFactory, tracer, converterRegistry, ruleRegistry, clock, executorService);
    }

    @Override
    public String toString() {
        return "RuleContext{" +
                "id='" + id + '\'' +
                ", creationTime=" + creationTime +
                ", bindings=" + bindings.getClass() +
                ", locale=" + locale +
                ", matchingStrategy=" + matchingStrategy +
                ", parameterResolver=" + parameterResolver +
                ", messageResolver=" + messageResolver +
                ", messageFormatter=" + messageFormatter +
                ", objectFactory=" + objectFactory +
                ", tracer=" + tracer +
                ", converterRegistry=" + converterRegistry +
                ", ruleRegistry=" + ruleRegistry +
                ", clock=" + clock +
                ", executionContext=" + executionContext +
                ", executorService=" + executorService +
                '}';
    }
}
