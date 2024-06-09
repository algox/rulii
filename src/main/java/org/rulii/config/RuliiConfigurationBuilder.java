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
package org.rulii.config;

import org.rulii.bind.match.BindingMatchingStrategy;
import org.rulii.bind.match.ParameterResolver;
import org.rulii.convert.ConverterRegistry;
import org.rulii.registry.RuleRegistry;
import org.rulii.lib.spring.util.Assert;
import org.rulii.text.MessageFormatter;
import org.rulii.text.MessageResolver;
import org.rulii.util.reflect.MethodResolver;
import org.rulii.util.reflect.ObjectFactory;

import java.time.Clock;
import java.util.Locale;

public class RuliiConfigurationBuilder {

    private BindingMatchingStrategy matchingStrategy = BindingMatchingStrategy.builder().build();
    private ParameterResolver parameterResolver = ParameterResolver.builder().build();
    private MethodResolver methodResolver = MethodResolver.builder().build();
    private MessageFormatter messageFormatter = MessageFormatter.builder().build();
    private ConverterRegistry converterRegistry = ConverterRegistry.builder().build();
    private RuleRegistry ruleRegistry = RuleRegistry.builder().build();
    private ObjectFactory objectFactory = ObjectFactory.builder().build();
    private Clock clock = Clock.systemDefaultZone();
    private Locale locale = Locale.getDefault();
    private MessageResolver messageResolver = MessageResolver.builder().build();
    private String scriptLanguage = null;

    RuliiConfigurationBuilder() {
        super();
    }

    public RuliiConfigurationBuilder matchingStrategy(BindingMatchingStrategy matchingStrategy) {
        Assert.notNull(matchingStrategy, "matchingStrategy cannot be null.");
        this.matchingStrategy = matchingStrategy;
        return this;
    }

    public RuliiConfigurationBuilder parameterResolver(ParameterResolver parameterResolver) {
        Assert.notNull(parameterResolver, "parameterResolver cannot be null.");
        this.parameterResolver = parameterResolver;
        return this;
    }

    public RuliiConfigurationBuilder methodResolver(MethodResolver methodResolver) {
        Assert.notNull(methodResolver, "methodResolver cannot be null.");
        this.methodResolver = methodResolver;
        return this;
    }

    public RuliiConfigurationBuilder messageFormatter(MessageFormatter messageFormatter) {
        Assert.notNull(messageFormatter, "messageFormatter cannot be null.");
        this.messageFormatter = messageFormatter;
        return this;
    }

    public RuliiConfigurationBuilder converterRegistry(ConverterRegistry converterRegistry) {
        Assert.notNull(converterRegistry, "converterRegistry cannot be null.");
        this.converterRegistry = converterRegistry;
        return this;
    }

    public RuliiConfigurationBuilder ruleRegistry(RuleRegistry ruleRegistry) {
        Assert.notNull(ruleRegistry, "ruleRegistry cannot be null.");
        this.ruleRegistry = ruleRegistry;
        return this;
    }

    public RuliiConfigurationBuilder objectFactory(ObjectFactory objectFactory) {
        Assert.notNull(objectFactory, "objectFactory cannot be null.");
        this.objectFactory = objectFactory;
        return this;
    }

    public RuliiConfigurationBuilder clock(Clock clock) {
        Assert.notNull(clock, "clock cannot be null.");
        this.clock = clock;
        return this;
    }

    public RuliiConfigurationBuilder locale(Locale locale) {
        Assert.notNull(locale, "locale cannot be null.");
        this.locale = locale;
        return this;
    }

    public RuliiConfigurationBuilder messageResolver(MessageResolver messageResolver) {
        Assert.notNull(messageResolver, "messageResolver cannot be null.");
        this.messageResolver = messageResolver;
        return this;
    }

    public RuliiConfigurationBuilder messageResolver(String...baseNames) {
        this.messageResolver = MessageResolver.builder(baseNames).build();
        return this;
    }

    public RuliiConfigurationBuilder scriptLanguage(String scriptLanguage) {
        Assert.notNull(scriptLanguage, "scriptLanguage cannot be null.");
        this.scriptLanguage = scriptLanguage;
        return this;
    }

    public RuliiConfiguration build() {
        return new RuliiConfiguration(matchingStrategy, parameterResolver, methodResolver, messageResolver,
                messageFormatter, converterRegistry, ruleRegistry, objectFactory, scriptLanguage, clock, locale);
    }
}
