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

import org.rulii.bind.*;
import org.rulii.bind.match.BindingMatchingStrategy;
import org.rulii.bind.match.ParameterResolver;
import org.rulii.convert.ConverterRegistry;
import org.rulii.lib.spring.util.Assert;
import org.rulii.registry.RuleRegistry;
import org.rulii.text.MessageFormatter;
import org.rulii.text.MessageResolver;
import org.rulii.trace.Tracer;
import org.rulii.util.reflect.ObjectFactory;

import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Fluent builder for constructing a {@link RuleContext}.
 *
 * <p>Instances are obtained from {@link RuleContextBuilderBuilder} (via {@link RuleContext#builder()})
 * rather than constructed directly.  All fields are pre-populated from {@link RuleContextOptions#standard()}
 * and can be selectively overridden before calling {@link #build()}.
 *
 * @author Max Arulananthan
 * @since 1.0
 * @see RuleContextBuilderBuilder
 * @see RuleContextOptions
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
    private RuleRegistry ruleRegistry;

    /** Creates a builder pre-populated with {@link RuleContextOptions#standard()} defaults. */
    RuleContextBuilder() {
        this(RuleContextOptions.standard());
    }

    /**
     * Creates a builder pre-populated from the given options.
     *
     * @param configuration the options to copy; must not be null.
     */
    RuleContextBuilder(RuleContextOptions configuration) {
        super();
        init(configuration);
    }

    /**
     * Creates a builder pre-populated from an existing {@link RuleContext}, preserving all its settings.
     *
     * <p>The context's bindings are flattened into a single fresh {@link Bindings}, copying every
     * binding visible across every scope the context currently has active — not just its
     * {@link ScopedBindings#GLOBAL_SCOPE} — while excluding {@link ReservedBindings reserved}
     * entries such as {@code $ruleContext}/{@code $bindings}. Copying the full {@link ScopedBindings}
     * hierarchy verbatim (reserved entries and all) into {@link #build()}, which binds a fresh pair
     * of its own, would leave two bindings of each reserved name once nested as the new context's
     * global scope, making any {@code RuleContext}-typed parameter ambiguous; copying only the
     * {@code GLOBAL_SCOPE} scope avoids that but silently drops bindings added in any other scope
     * (e.g. a flow's own scope, or a nested {@code scope()}/{@code forEach()} scope) — this
     * flatten-and-exclude approach avoids both problems.
     *
     * @param context the context to copy; must not be null.
     */
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
        this.bindings = copyNonReservedBindings(context.getBindings());
        this.executorService = context.getExecutorService();
        this.ruleRegistry = context.getRuleRegistry();
    }

    private static Bindings copyNonReservedBindings(Bindings source) {
        // Dedupe by name (last write wins) before binding: source may be a ScopedBindings whose
        // iterator() walks root-to-current across every scope, so a name shadowed in a nested
        // scope would otherwise appear twice and the second bind() would throw
        // BindingAlreadyExistsException. Iterating root-to-current means the innermost/child
        // scope's binding overwrites the outer one, matching ScopedBindings#getBinding's own
        // child-shadows-parent lookup semantics.
        Map<String, Binding<?>> deduped = new LinkedHashMap<>();

        for (Binding<?> binding : source) {
            if (!ReservedBindings.isReserved(binding.getName())) {
                deduped.put(binding.getName(), binding);
            }
        }

        Bindings result = source instanceof ScopedBindings ? Bindings.builder().scoped() : Bindings.builder().standard();
        for (Binding<?> binding : deduped.values()) {
            result.bind(binding);
        }

        return result;
    }

    /**
     * Copies all service references from the given options into this builder.
     *
     * @param options the options to copy from; must not be null.
     */
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
        this.ruleRegistry = options.getRuleRegistry();
    }

    /**
     * Sets the user-provided bindings that will be added as the global scope of the context.
     *
     * @param bindings the bindings to use; must not be null.
     * @return this builder, for method chaining.
     */
    public RuleContextBuilder bindings(Bindings bindings) {
        Assert.notNull(bindings, "bindings cannot be null.");
        this.bindings = bindings;
        return this;
    }

    /**
     * Sets the binding-matching strategy.
     *
     * @param strategy the strategy to use; must not be null.
     * @return this builder, for method chaining.
     */
    public RuleContextBuilder matchUsing(BindingMatchingStrategy strategy) {
        Assert.notNull(strategy, "strategy cannot be null.");
        this.matchingStrategy = strategy;
        return this;
    }

    /**
     * Sets the parameter resolver.
     *
     * @param parameterResolver the resolver to use; must not be null.
     * @return this builder, for method chaining.
     */
    public RuleContextBuilder paramResolver(ParameterResolver parameterResolver) {
        Assert.notNull(objectFactory, "parameterResolver cannot be null.");
        this.parameterResolver = parameterResolver;
        return this;
    }

    /**
     * Sets the message resolver.
     *
     * @param messageResolver the resolver to use; must not be null.
     * @return this builder, for method chaining.
     */
    public RuleContextBuilder messageResolver(MessageResolver messageResolver) {
        Assert.notNull(messageResolver, "messageResolver cannot be null.");
        this.messageResolver = messageResolver;
        return this;
    }

    /**
     * Sets the message resolver by creating one from the given resource-bundle base names.
     *
     * @param baseNames the resource-bundle base names; must not be null.
     * @return this builder, for method chaining.
     */
    public RuleContextBuilder messageResolver(String...baseNames) {
        Assert.notNull(baseNames, "baseNames cannot be null.");
        this.messageResolver = MessageResolver.builder(baseNames).build();
        return this;
    }

    /**
     * Sets the message formatter.
     *
     * @param messageFormatter the formatter to use; must not be null.
     * @return this builder, for method chaining.
     */
    public RuleContextBuilder messageFormatter(MessageFormatter messageFormatter) {
        Assert.notNull(messageFormatter, "messageFormatter cannot be null.");
        this.messageFormatter = messageFormatter;
        return this;
    }

    /**
     * Sets the object factory.
     *
     * @param objectFactory the factory to use; must not be null.
     * @return this builder, for method chaining.
     */
    public RuleContextBuilder objectFactory(ObjectFactory objectFactory) {
        Assert.notNull(objectFactory, "objectFactory cannot be null.");
        this.objectFactory = objectFactory;
        return this;
    }

    /**
     * Sets the tracer used to record rule execution events.
     *
     * @param tracer the tracer to use; must not be null.
     * @return this builder, for method chaining.
     */
    public RuleContextBuilder traceUsing(Tracer tracer) {
        Assert.notNull(tracer, "tracer cannot be null.");
        this.tracer = tracer;
        return this;
    }

    /**
     * Sets the converter registry.
     *
     * @param converterRegistry the registry to use; must not be null.
     * @return this builder, for method chaining.
     */
    public RuleContextBuilder converterRegistry(ConverterRegistry converterRegistry) {
        Assert.notNull(converterRegistry, "converterRegistry cannot be null.");
        this.converterRegistry = converterRegistry;
        return this;
    }

    /**
     * Sets the locale used for message resolution and formatting.
     *
     * @param locale the locale to use; must not be null.
     * @return this builder, for method chaining.
     */
    public RuleContextBuilder locale(Locale locale) {
        Assert.notNull(locale, "locale cannot be null.");
        this.locale = locale;
        return this;
    }

    /**
     * Sets the clock used for time-sensitive rule operations.
     *
     * @param clock the clock to use; must not be null.
     * @return this builder, for method chaining.
     */
    public RuleContextBuilder clock(Clock clock) {
        Assert.notNull(clock, "clock cannot be null.");
        this.clock = clock;
        return this;
    }

    /**
     * Sets the executor service used for async rule execution.
     *
     * @param executorService the executor service to use; must not be null.
     * @return this builder, for method chaining.
     */
    public RuleContextBuilder executeUsing(ExecutorService executorService) {
        Assert.notNull(executorService, "executorService cannot be null.");
        this.executorService = executorService;
        return this;
    }

    /**
     * Sets the rule registry used to look up rules, rule sets, and rule flows by name.
     *
     * @param ruleRegistry the registry to use; must not be null.
     * @return this builder, for method chaining.
     */
    public RuleContextBuilder registry(RuleRegistry ruleRegistry) {
        Assert.notNull(ruleRegistry, "ruleRegistry cannot be null.");
        this.ruleRegistry = ruleRegistry;
        return this;
    }

    /** @return the currently configured bindings; may be null if not yet set. */
    public Bindings getBindings() {
        return bindings;
    }

    /** @return the currently configured binding-matching strategy; never null after construction. */
    public BindingMatchingStrategy getMatchingStrategy() {
        return matchingStrategy;
    }

    /** @return the currently configured parameter resolver; never null after construction. */
    public ParameterResolver getParameterResolver() {
        return parameterResolver;
    }

    /** @return the currently configured message resolver; never null after construction. */
    public MessageResolver getMessageResolver() {
        return messageResolver;
    }

    /** @return the currently configured message formatter; never null after construction. */
    public MessageFormatter getMessageFormatter() {
        return messageFormatter;
    }

    /** @return the currently configured object factory; never null after construction. */
    public ObjectFactory getObjectFactory() {
        return objectFactory;
    }

    /** @return the currently configured tracer; never null after construction. */
    public Tracer getTracer() {
        return tracer;
    }

    /** @return the currently configured converter registry; never null after construction. */
    public ConverterRegistry getConverterRegistry() {
        return converterRegistry;
    }

    /** @return the currently configured clock; never null after construction. */
    public Clock getClock() {
        return clock;
    }

    /** @return the currently configured locale; never null after construction. */
    public Locale getLocale() {
        return locale;
    }

    /** @return the currently configured executor service; never null after construction. */
    public ExecutorService getExecutorService() {
        return executorService;
    }

    /** @return the currently configured rule registry; may be null if not set. */
    public RuleRegistry getRuleRegistry() {
        return ruleRegistry;
    }

    /**
     * Builds and returns a {@link RuleContext} from the current builder state.
     *
     * <p>A new {@link org.rulii.bind.ScopedBindings} is created internally. The reserved bindings
     * {@code $ruleBindings} and {@code $ruleContext} are injected into the root scope automatically.
     * If no bindings were set, an empty standard {@link org.rulii.bind.Bindings} is used as the
     * global scope.
     *
     * @return a fully initialised {@link RuleContext}; never null.
     */
    public RuleContext build() {
        ScopedBindings scopedBindings = Bindings.builder().scoped();

        RuleContext result  = new RuleContext(scopedBindings, locale, matchingStrategy, parameterResolver,
                messageResolver, messageFormatter, objectFactory, tracer,
                converterRegistry, clock, executorService, ruleRegistry);

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
