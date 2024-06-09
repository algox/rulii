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
import org.rulii.trace.EventProcessor;
import org.rulii.trace.ExecutionListener;
import org.rulii.trace.Tracer;
import org.rulii.util.reflect.ObjectFactory;

import java.time.Clock;
import java.util.Locale;

public interface RuleContextOptions {

    BindingMatchingStrategy getMatchingStrategy();

    ParameterResolver getParameterResolver();

    MessageResolver getMessageResolver();

    MessageFormatter getMessageFormatter();

    ObjectFactory getObjectFactory();

    Tracer getTracer();

    ConverterRegistry getConverterRegistry();

    RuleRegistry getRuleRegistry();

    Clock getClock();

    Locale getLocale();

    ExecutionListener[] getExecutionListeners();
}
