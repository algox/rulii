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

import org.rulii.bind.ScopedBindings;
import org.rulii.bind.match.BindingMatchingStrategy;
import org.rulii.bind.match.ParameterResolver;
import org.rulii.convert.ConverterRegistry;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.Immutator;
import org.rulii.text.MessageFormatter;
import org.rulii.text.MessageResolver;
import org.rulii.trace.Tracer;
import org.rulii.util.reflect.ObjectFactory;

import java.time.Clock;
import java.util.Date;
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
    private final Clock clock;
    private final ExecutorService executorService;

    RuleContext(ScopedBindings bindings, Locale locale, BindingMatchingStrategy matchingStrategy,
                ParameterResolver parameterResolver, MessageResolver messageResolver,
                MessageFormatter messageFormatter, ObjectFactory objectFactory,
                Tracer tracer, ConverterRegistry converterRegistry,
                Clock clock, ExecutorService executorService) {
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
        this.clock = clock;
        this.executorService = executorService;
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
    public ParameterResolver getParameterResolver() {
        return parameterResolver;
    }

    /**
     * Retrieves the MessageResolver associated with this RuleContext.
     *
     * @return the MessageResolver instance linked to this RuleContext
     */
    public MessageResolver getMessageResolver() {
        return messageResolver;
    }

    /**
     * Retrieves the MessageFormatter associated with this RuleContext.
     *
     * @return the MessageFormatter instance linked to this RuleContext
     */
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

    /**
     * Retrieves the Tracer associated with this RuleContext.
     *
     * @return the Tracer instance used for tracing within the RuleContext
     */
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

    /**
     * Returns the locale associated with this RuleContext.
     *
     * @return the locale of the RuleContext
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Get the Clock instance associated with this RuleContext.
     *
     * @return the Clock instance used in the RuleContext. Cannot be null.
     */
    public Clock getClock() {
        return clock;
    }

    /**
     * Retrieves the identifier of this RuleContext.
     *
     * @return the identifier as a String. Cannot be null.
     */
    public String getId() {
        return id;
    }

    /**
     * Retrieves the creation time of the RuleContext.
     *
     * @return The creation time of the RuleContext as a Date object.
     */
    public Date getCreationTime() {
        return creationTime;
    }

    /**
     * Get the ExecutorService instance associated with this RuleContext.
     *
     * @return the ExecutorService instance linked to this RuleContext
     */
    public ExecutorService getExecutorService() {
        return executorService;
    }

    @Override
    public RuleContext asImmutable() {
        return new RuleContext(bindings.asImmutable(), locale, matchingStrategy, parameterResolver, messageResolver,
                messageFormatter, objectFactory, tracer, converterRegistry, clock, executorService);
    }

    @Override
    public String toString() {
        return "RuleContext{" +
                "id='" + id + '\'' +
                ", creationTime=" + creationTime +
                ", bindings=" + bindings +
                ", locale=" + locale +
                ", matchingStrategy=" + matchingStrategy +
                ", parameterResolver=" + parameterResolver +
                ", messageResolver=" + messageResolver +
                ", messageFormatter=" + messageFormatter +
                ", objectFactory=" + objectFactory +
                ", tracer=" + tracer +
                ", converterRegistry=" + converterRegistry +
                ", clock=" + clock  +
                ", executorService=" + executorService +
                '}';
    }
}
