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

/**
 * Configuration interface that groups the services and strategies used by a {@link RuleContext}.
 *
 * <p>An implementation supplies the binding-matching strategy, parameter resolver, message handling,
 * type converters, object factory, clock, locale, and executor service that the rule engine needs.
 * Use {@link #standard()} for the out-of-the-box defaults or provide a custom implementation to
 * override specific services.
 *
 * @author Max Arulananthan
 * @since 1.0
 * @see StandardRuleContextOptions
 * @see RuleContextBuilder
 */
public interface RuleContextOptions {

    /**
     * Returns a new instance of the standard (default) rule context options.
     *
     * @return a {@link StandardRuleContextOptions} instance; never null.
     */
    static RuleContextOptions standard() {
        return StandardRuleContextOptions.build();
    }

    /**
     * Returns the binding-matching strategy that defines how rule parameters are resolved against bindings.
     *
     * @return the matching strategy; never null.
     */
    BindingMatchingStrategy getMatchingStrategy();

    /**
     * Returns the parameter resolver used to match method parameters to bindings.
     *
     * @return the parameter resolver; never null.
     */
    ParameterResolver getParameterResolver();

    /**
     * Returns the message resolver used to look up messages by code and locale.
     *
     * @return the message resolver; never null.
     */
    MessageResolver getMessageResolver();

    /**
     * Returns the message formatter used to format messages with dynamic arguments.
     *
     * @return the message formatter; never null.
     */
    MessageFormatter getMessageFormatter();

    /**
     * Returns the object factory used to instantiate rules, conditions, and other framework objects.
     *
     * @return the object factory; never null.
     */
    ObjectFactory getObjectFactory();

    /**
     * Returns the converter registry used to look up type converters.
     *
     * @return the converter registry; never null.
     */
    ConverterRegistry getConverterRegistry();

    /**
     * Returns the clock used for time-sensitive rule operations.
     *
     * @return the clock; never null.
     */
    Clock getClock();

    /**
     * Returns the locale used for message resolution and formatting.
     *
     * @return the locale; never null.
     */
    Locale getLocale();

    /**
     * Returns the executor service used for async rule execution.
     *
     * @return the executor service; never null.
     */
    ExecutorService getExecutorService();

    /**
     * Returns the rule registry used to look up rules, rule sets, and rule flows by name.
     *
     * @return the rule registry; may be null if not configured.
     */
    RuleRegistry getRuleRegistry();
}
