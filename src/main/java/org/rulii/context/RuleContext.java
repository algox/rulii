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

import org.rulii.bind.ScopedBindings;
import org.rulii.bind.match.BindingMatchingStrategy;
import org.rulii.bind.match.ParameterResolver;
import org.rulii.convert.ConverterRegistry;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.Immutator;
import org.rulii.model.UnrulyException;
import org.rulii.registry.RuleRegistry;
import org.rulii.script.ScriptProcessor;
import org.rulii.script.ScriptProcessorFactory;
import org.rulii.script.ScriptProcessorManager;
import org.rulii.text.MessageFormatter;
import org.rulii.text.MessageResolver;
import org.rulii.trace.Tracer;
import org.rulii.util.reflect.ObjectFactory;

import java.time.Clock;
import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * Carries all state and services needed during rule execution.
 *
 * <p>A {@code RuleContext} is the single object passed through the rule engine pipeline. It bundles
 * the active {@link ScopedBindings}, the locale, the matching strategy, message handling, conversion,
 * tracing, scripting support, and the executor service into one cohesive unit.
 *
 * <p>Instances are created via the fluent builder API:
 * <pre>{@code
 * RuleContext ctx = RuleContext.builder().build(bindings);
 * }</pre>
 *
 * @author Max Arulananthan
 * @since 1.0
 * @see RuleContextBuilder
 * @see RuleContextOptions
 */
public class RuleContext implements Immutator<RuleContext> {

    /**
     * Returns the entry point for the fluent rule-context building DSL.
     *
     * @return the singleton {@link RuleContextBuilderBuilder}; never null.
     */
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
    private final RuleRegistry ruleRegistry;

    private final ScriptProcessorManager scriptProcessorManager = new ScriptProcessorManager();

    private final Map<String, ScriptProcessor> scriptProcessors = Collections.synchronizedMap(new HashMap<>());

    /**
     * Package-private constructor — use {@link RuleContext#builder()} to obtain instances.
     *
     * @param bindings           the scoped bindings for this context; must not be null.
     * @param locale             the locale; must not be null.
     * @param matchingStrategy   the binding-matching strategy; must not be null.
     * @param parameterResolver  the parameter resolver; must not be null.
     * @param messageResolver    the message resolver; must not be null.
     * @param messageFormatter   the message formatter; must not be null.
     * @param objectFactory      the object factory; must not be null.
     * @param tracer             the execution tracer; must not be null.
     * @param converterRegistry  the type-converter registry; must not be null.
     * @param clock              the clock used for time-sensitive operations; must not be null.
     * @param executorService    the executor for async tasks; must not be null.
     * @param ruleRegistry       the rule registry; may be null.
     */
    RuleContext(ScopedBindings bindings, Locale locale, BindingMatchingStrategy matchingStrategy,
                ParameterResolver parameterResolver, MessageResolver messageResolver,
                MessageFormatter messageFormatter, ObjectFactory objectFactory,
                Tracer tracer, ConverterRegistry converterRegistry,
                Clock clock, ExecutorService executorService, RuleRegistry ruleRegistry) {
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
        this.ruleRegistry = ruleRegistry;
    }

    /**
     * Returns the scoped bindings for this context.
     *
     * @return the bindings; never null.
     */
    public ScopedBindings getBindings() {
        return bindings;
    }

    /**
     * Returns the binding-matching strategy used to resolve parameters.
     *
     * @return the matching strategy; never null.
     */
    public BindingMatchingStrategy getMatchingStrategy() {
        return matchingStrategy;
    }

    /**
     * Returns the parameter resolver used to match method parameters against bindings.
     *
     * @return the parameter resolver; never null.
     */
    public ParameterResolver getParameterResolver() {
        return parameterResolver;
    }

    /**
     * Returns the message resolver used to look up message codes.
     *
     * @return the message resolver; never null.
     */
    public MessageResolver getMessageResolver() {
        return messageResolver;
    }

    /**
     * Returns the message formatter used to format messages with dynamic content.
     *
     * @return the message formatter; never null.
     */
    public MessageFormatter getMessageFormatter() {
        return messageFormatter;
    }

    /**
     * Returns the object factory used to create rule, condition, and converter instances.
     *
     * @return the object factory; never null.
     */
    public ObjectFactory getObjectFactory() {
        return objectFactory;
    }

    /**
     * Returns the tracer used to record rule execution events.
     *
     * @return the tracer; never null.
     */
    public Tracer getTracer() {
        return tracer;
    }

    /**
     * Returns the converter registry used to look up type converters.
     *
     * @return the converter registry; never null.
     */
    public ConverterRegistry getConverterRegistry() {
        return converterRegistry;
    }

    /**
     * Returns the locale used for message resolution and formatting.
     *
     * @return the locale; never null.
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Returns the clock used for time-sensitive rule operations.
     *
     * @return the clock; never null.
     */
    public Clock getClock() {
        return clock;
    }

    /**
     * Returns the unique identifier of this context (a random UUID assigned at creation time).
     *
     * @return the context ID; never null.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the timestamp at which this context was created.
     *
     * @return the creation time; never null.
     */
    public Date getCreationTime() {
        return creationTime;
    }

    /**
     * Returns the executor service used for async rule execution.
     *
     * @return the executor service; never null.
     */
    public ExecutorService getExecutorService() {
        return executorService;
    }

    /**
     * Returns the rule registry associated with this context, or {@code null} if none was configured.
     *
     * @return the rule registry; may be null.
     */
    public RuleRegistry getRuleRegistry() {
        return ruleRegistry;
    }

    /**
     * Returns the {@link ScriptProcessor} for the given scripting language, creating and caching it on first access.
     *
     * @param languageName the scripting language name (e.g. {@code "js"}); must not be null or empty.
     * @return the processor for the language; never null.
     * @throws UnrulyException if no factory is registered for the language or the processor cannot be created.
     */
    public ScriptProcessor getScriptProcessor(String languageName) {
        Assert.hasText(languageName, "languageName cannot be null or empty.");

        ScriptProcessor cached = scriptProcessors.get(languageName);
        if (cached != null) return cached;
        ScriptProcessorFactory scriptProcessorFactory = scriptProcessorManager.getScriptProcessorFactory(languageName);
        if (scriptProcessorFactory == null) throw new UnrulyException("No ScriptProcessor found for language: " + languageName);
        ScriptProcessor scriptProcessor = scriptProcessorFactory.getScriptProcessor();
        if (scriptProcessor == null) throw new UnrulyException("Unable to create ScriptProcessor for language: " + languageName);
        scriptProcessors.putIfAbsent(languageName, scriptProcessor);
        return scriptProcessor;
    }

    /**
     * Returns an immutable view of this context with immutable bindings.
     *
     * @return a new {@code RuleContext} backed by immutable bindings; never null.
     */
    @Override
    public RuleContext asImmutable() {
        return new RuleContext(bindings.asImmutable(), locale, matchingStrategy, parameterResolver, messageResolver,
                messageFormatter, objectFactory, tracer, converterRegistry, clock, executorService, ruleRegistry);
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
                ", ruleRegistry=" + ruleRegistry +
                '}';
    }
}
