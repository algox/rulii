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

import org.rulii.bind.match.BindingMatchingStrategy;
import org.rulii.bind.match.ParameterResolver;
import org.rulii.convert.ConverterRegistry;
import org.rulii.registry.RuleRegistry;
import org.rulii.text.MessageFormatter;
import org.rulii.text.MessageResolver;
import org.rulii.trace.Tracer;
import org.rulii.util.reflect.ObjectFactory;

import java.time.Clock;
import java.util.Locale;
import java.util.concurrent.ExecutorService;

/**
 * Represents a set of options or configurations for the Rule Context in the system.
 * This interface provides a variety of access methods to retrieve objects and services
 * that are essential for handling rules, parameter resolution, message formatting,
 * conversion, and tracing within the context of rule execution.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public interface RuleContextOptions {

    /**
     * Provides access to the standard implementation of the RuleContextOptions interface.
     * This method returns an instance of StandardRuleContextOptions, which offers a default
     * configuration and services for rule execution contexts.
     *
     * @return an instance of StandardRuleContextOptions representing the default rule context options.
     */
    static RuleContextOptions standard() {
        return StandardRuleContextOptions.getInstance();
    }

    /**
     * Retrieves the current BindingMatchingStrategy that defines how bindings are
     * matched to specific criteria (e.g., name, type) within the context.
     *
     * @return the BindingMatchingStrategy used for binding evaluation.
     */
    BindingMatchingStrategy getMatchingStrategy();

    /**
     * Retrieves the ParameterResolver instance associated with the Rule Context Options.
     * The ParameterResolver is used to match and resolve method parameters against bindings
     * using a specific MatchingStrategy and various contextual services, such as the ObjectFactory.
     *
     * @return the ParameterResolver responsible for parameter matching and resolution functionality.
     */
    ParameterResolver getParameterResolver();

    /**
     * Retrieves the MessageResolver instance associated with the current context.
     * The MessageResolver is responsible for resolving message codes to their corresponding
     * messages based on the locale and other parameters.
     *
     * @return the MessageResolver for message code resolution.
     */
    MessageResolver getMessageResolver();

    /**
     * Retrieves the MessageFormatter instance associated with the current context.
     * The MessageFormatter is responsible for formatting messages with placeholders,
     * allowing integration of dynamic content and localization.
     *
     * @return the MessageFormatter for formatting messages based on locale and arguments.
     */
    MessageFormatter getMessageFormatter();

    /**
     * Retrieves the ObjectFactory instance associated with the current context.
     * The ObjectFactory is responsible for creating instances of various objects,
     * such as rules, conditions, converters, functions, and more, as required by
     * the framework. It facilitates dynamic object creation, allowing for custom
     * implementations and runtime type resolution.
     *
     * @return the ObjectFactory used for creating and managing framework-specific objects.
     */
    ObjectFactory getObjectFactory();

    /**
     * Retrieves the Tracer instance associated with the current context.
     * The Tracer is used to monitor and log events that occur during the
     * rule execution process, such as binding changes, rule execution stages,
     * and the lifecycle of rule sets. It provides methods to add or remove
     * various listeners and to trigger specific tracing events.
     *
     * @return the Tracer for tracking and logging rule execution events.
     */
    Tracer getTracer();

    /**
     * Retrieves the ConverterRegistry instance associated with the current context.
     * The ConverterRegistry is responsible for managing and providing converters
     * that facilitate the transformation of objects between different types.
     * It allows registration of custom converters and lookup of existing converters.
     *
     * @return the ConverterRegistry for handling type conversions.
     */
    ConverterRegistry getConverterRegistry();

    /**
     * Retrieves the RuleRegistry instance associated with the current context.
     * The RuleRegistry is responsible for managing and storing rules and rule sets.
     *
     * @return the RuleRegistry instance for accessing and managing rules and rule sets.
     */
    RuleRegistry getRuleRegistry();

    /**
     * Retrieves the Clock instance associated with the current context.
     * The Clock provides access to the current time and date information
     * in a manner that can be controlled or overridden, which is useful
     * for testing or applications that require a consistent or custom notion
     * of time.
     *
     * @return the Clock used for time-related operations in the context.
     */
    Clock getClock();

    /**
     * Retrieves the Locale instance associated with the current context.
     * The Locale determines the regional settings, including language
     * and formatting conventions, used within the context.
     *
     * @return the Locale associated with the current context.
     */
    Locale getLocale();

    /**
     * Retrieves the ExecutorService instance associated with the current context.
     * The ExecutorService is responsible for managing and executing asynchronous
     * tasks, enabling multi-threaded operations within the framework.
     *
     * @return the ExecutorService used for managing and executing asynchronous tasks.
     */
    ExecutorService getExecutorService();

}
