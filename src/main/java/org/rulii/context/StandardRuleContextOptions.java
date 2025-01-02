package org.rulii.context;

import org.rulii.bind.match.BindingMatchingStrategy;
import org.rulii.bind.match.ParameterResolver;
import org.rulii.convert.ConverterRegistry;
import org.rulii.registry.RuleRegistry;
import org.rulii.text.MessageFormatter;
import org.rulii.text.MessageResolver;
import org.rulii.trace.DefaultTracer;
import org.rulii.trace.Tracer;
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
    private final RuleRegistry ruleRegistry = RuleRegistry.builder().build();
    private final ObjectFactory objectFactory = ObjectFactory.builder().build();
    private final Clock clock = Clock.systemDefaultZone();
    private final Locale locale = Locale.getDefault();
    private final MessageResolver messageResolver = MessageResolver.builder().build();
    private final Tracer tracer = Tracer.builder().build();

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
                ", ruleRegistry=" + ruleRegistry +
                ", objectFactory=" + objectFactory +
                ", clock=" + clock +
                ", locale=" + locale +
                ", messageResolver=" + messageResolver +
                ", tracer=" + tracer +
                '}';
    }
}
