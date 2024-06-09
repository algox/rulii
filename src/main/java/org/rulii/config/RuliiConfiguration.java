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
import org.rulii.context.RuleContextOptions;
import org.rulii.registry.RuleRegistry;
import org.rulii.lib.spring.util.Assert;
import org.rulii.text.MessageFormatter;
import org.rulii.text.MessageResolver;
import org.rulii.trace.EventProcessor;
import org.rulii.trace.ExecutionListener;
import org.rulii.trace.Tracer;
import org.rulii.util.reflect.MethodResolver;
import org.rulii.util.reflect.ObjectFactory;

import java.time.Clock;
import java.util.Locale;

public class RuliiConfiguration implements RuleContextOptions {

    private BindingMatchingStrategy matchingStrategy;
    private ParameterResolver parameterResolver;
    private MessageResolver messageResolver;
    private MethodResolver methodResolver;
    private MessageFormatter messageFormatter;
    private ConverterRegistry converterRegistry;
    private RuleRegistry ruleRegistry;
    private String scriptLanguage;
    private ObjectFactory objectFactory;
    private Clock clock;
    private Locale locale;

    RuliiConfiguration(BindingMatchingStrategy matchingStrategy, ParameterResolver parameterResolver,
                              MethodResolver methodResolver, MessageResolver messageResolver,
                              MessageFormatter messageFormatter, ConverterRegistry converterRegistry,
                              RuleRegistry ruleRegistry, ObjectFactory objectFactory, String scriptLanguage,
                              Clock clock, Locale locale) {
        super();
        Assert.notNull(matchingStrategy, "matchingStrategy cannot be null.");
        Assert.notNull(parameterResolver, "parameterResolver cannot be null.");
        Assert.notNull(methodResolver, "methodResolver cannot be null.");
        Assert.notNull(messageResolver, "messageResolver cannot be null.");
        Assert.notNull(messageFormatter, "messageFormatter cannot be null.");
        Assert.notNull(converterRegistry, "converterRegistry cannot be null.");
        Assert.notNull(ruleRegistry, "ruleRegistry cannot be null.");
        Assert.notNull(objectFactory, "objectFactory cannot be null.");
        Assert.notNull(clock, "clock cannot be null.");
        Assert.notNull(locale, "locale cannot be null.");
        this.matchingStrategy = matchingStrategy;
        this.parameterResolver = parameterResolver;
        this.methodResolver = methodResolver;
        this.messageResolver = messageResolver;
        this.messageFormatter = messageFormatter;
        this.converterRegistry = converterRegistry;
        this.ruleRegistry = ruleRegistry;
        this.objectFactory = objectFactory;
        this.clock = clock;
        this.locale = locale;
        this.scriptLanguage = scriptLanguage;
    }

    public static RuliiConfigurationBuilder builder() {
        return new RuliiConfigurationBuilder();
    }

    public BindingMatchingStrategy getMatchingStrategy() {
        return matchingStrategy;
    }

    public ParameterResolver getParameterResolver() {
        return parameterResolver;
    }

    public MethodResolver getMethodResolver() {
        return methodResolver;
    }

    public MessageFormatter getMessageFormatter() {
        return messageFormatter;
    }

    public ConverterRegistry getConverterRegistry() {
        return converterRegistry;
    }

    public RuleRegistry getRuleRegistry() {
        return ruleRegistry;
    }

    public ObjectFactory getObjectFactory() {
        return objectFactory;
    }

    public Clock getClock() {
        return clock;
    }

    public Locale getLocale() {
        return locale;
    }

    public MessageResolver getMessageResolver() {
        return messageResolver;
    }

    public String getScriptLanguage() {
        return scriptLanguage;
    }

    @Override
    public Tracer getTracer() {
        return null;
    }

    @Override
    public ExecutionListener[] getExecutionListeners() {
        return null;
    }

    void setScriptLanguage(String scriptLanguage) {
        Assert.notNull(scriptLanguage, "scriptLanguage cannot be null.");
        this.scriptLanguage = scriptLanguage;
    }

    void setMatchingStrategy(BindingMatchingStrategy matchingStrategy) {
        Assert.notNull(matchingStrategy, "matchingStrategy cannot be null.");
        this.matchingStrategy = matchingStrategy;
    }

    void setParameterResolver(ParameterResolver parameterResolver) {
        Assert.notNull(parameterResolver, "parameterResolver cannot be null.");
        this.parameterResolver = parameterResolver;
    }

    void setMessageResolver(MessageResolver messageResolver) {
        Assert.notNull(messageResolver, "messageResolver cannot be null.");
        this.messageResolver = messageResolver;
    }

    void setMethodResolver(MethodResolver methodResolver) {
        Assert.notNull(methodResolver, "methodResolver cannot be null.");
        this.methodResolver = methodResolver;
    }

    void setMessageFormatter(MessageFormatter messageFormatter) {
        Assert.notNull(messageFormatter, "messageFormatter cannot be null.");
        this.messageFormatter = messageFormatter;
    }

    void setConverterRegistry(ConverterRegistry converterRegistry) {
        Assert.notNull(converterRegistry, "converterRegistry cannot be null.");
        this.converterRegistry = converterRegistry;
    }

    void setRuleRegistry(RuleRegistry ruleRegistry) {
        this.ruleRegistry = ruleRegistry;
    }

    void setObjectFactory(ObjectFactory objectFactory) {
        Assert.notNull(objectFactory, "objectFactory cannot be null.");
        this.objectFactory = objectFactory;
    }

    void setClock(Clock clock) {
        Assert.notNull(clock, "clock cannot be null.");
        this.clock = clock;
    }

    void setLocale(Locale locale) {
        Assert.notNull(locale, "locale cannot be null.");
        this.locale = locale;
    }

    @Override
    public String toString() {
        return "RuleContext{" +
                ", locale=" + locale +
                ", parameterResolver=" + parameterResolver.getClass().getSimpleName() +
                ", messageResolver=" + messageResolver.getClass().getSimpleName() +
                ", messageFormatter=" + messageFormatter.getClass().getSimpleName() +
                ", objectFactory=" + objectFactory.getClass().getSimpleName() +
                ", converterRegistry=" + converterRegistry.getClass().getSimpleName() +
                ", ruleRegistry=" + ruleRegistry.getClass().getSimpleName() +
                ", clock=" + clock +
                ", matchingStrategy=" + matchingStrategy.getClass().getSimpleName() +
                '}';
    }
}
