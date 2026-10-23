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
package org.rulii.validation.rules.url;

import org.rulii.model.function.Function;
import org.rulii.validation.Severity;
import org.rulii.validation.ValueValidationRuleBuilder;

/**
 * Builder for {@link UrlValidationRule}. Use {@link #build()} to create the configured {@link org.rulii.rule.Rule}.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class UrlValidationRuleBuilder
        extends ValueValidationRuleBuilder<UrlValidationRuleBuilder, UrlValidationRule> {

    private String protocol;
    private String host;
    private int port = UrlValidationRule.ANY_PORT;
    private String regexp;
    private int flags = 0;

    /**
     * Creates a new builder for {@link UrlValidationRule}.
     *
     * @param valueFunction the function that supplies the value to validate
     */
    public UrlValidationRuleBuilder(Function<?> valueFunction) {
        super(valueFunction);
        errorCode(UrlValidationRule.ERROR_CODE);
        severity(Severity.ERROR);
    }

    /**
     * The protocol the URL must have; not checked by default.
     *
     * @param protocol the protocol, such as {@code https}; null or empty for any
     * @return this builder
     */
    public UrlValidationRuleBuilder protocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    /**
     * The host the URL must have; not checked by default.
     *
     * @param host the host; null or empty for any
     * @return this builder
     */
    public UrlValidationRuleBuilder host(String host) {
        this.host = host;
        return this;
    }

    /**
     * The port the URL must have; not checked by default.
     *
     * @param port the port; {@link UrlValidationRule#ANY_PORT} for any
     * @return this builder
     */
    public UrlValidationRuleBuilder port(int port) {
        this.port = port;
        return this;
    }

    /**
     * A regular expression the whole URL must match as well; not checked by default.
     *
     * @param regexp the expression; null or empty for none
     * @return this builder
     */
    public UrlValidationRuleBuilder regexp(String regexp) {
        this.regexp = regexp;
        return this;
    }

    /**
     * The {@link java.util.regex.Pattern} flags of the regular expression; none by default.
     *
     * @param flags the flags
     * @return this builder
     */
    public UrlValidationRuleBuilder flags(int flags) {
        this.flags = flags;
        return this;
    }

    /**
     * Creates a new {@link UrlValidationRule} configured with this builder's settings.
     *
     * @return a new {@link UrlValidationRule}
     */
    @Override
    protected UrlValidationRule createValueValidationRule() {
        return new UrlValidationRule(getValueFunction(), getErrorCode(), getSeverity(), getErrorMessage(), getValueName(),
                protocol, host, port, regexp, flags);
    }
}
