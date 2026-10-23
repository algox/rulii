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

import org.rulii.annotation.Description;
import org.rulii.annotation.Rule;
import org.rulii.context.RuleContext;
import org.rulii.lib.apache.validation.UrlValidator;
import org.rulii.model.function.Function;
import org.rulii.validation.RuleViolationBuilder;
import org.rulii.validation.Severity;
import org.rulii.validation.ValidationRuleException;
import org.rulii.validation.ValueValidationRule;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Validation Rule to make sure the value is a URL. Optionally the URL must also have a given
 * protocol, host or port, and match a regular expression.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
@Rule
@Description("Value must be a URL.")
public class UrlValidationRule extends ValueValidationRule {

    public static final List<Class<?>> SUPPORTED_TYPES = List.of(CharSequence.class);

    public static final String ERROR_CODE       = "urlValidationRule.errorCode";
    public static final String DEFAULT_MESSAGE  = "Value {0} is not a valid Url";

    /**
     * Marks the port as not checked.
     */
    public static final int ANY_PORT = -1;

    private final UrlValidator validator;
    private final String protocol;
    private final String host;
    private final int port;
    private final String regexp;
    private final Pattern compiledRegexp;

    /**
     * Creates a new builder for this validation rule.
     *
     * @param function the function that supplies the value to validate
     * @return a new {@link UrlValidationRuleBuilder}
     */
    public static UrlValidationRuleBuilder builder(Function<?> function) {
        return new UrlValidationRuleBuilder(function);
    }

    UrlValidationRule(Function<?> valueFunction, String errorCode, Severity severity, String errorMessage, String valueName) {
        this(valueFunction, errorCode, severity, errorMessage, valueName, null, null, ANY_PORT, null, 0);
    }

    UrlValidationRule(Function<?> valueFunction, String errorCode, Severity severity, String errorMessage, String valueName,
                      String protocol, String host, int port, String regexp, int flags) {
        super(valueFunction, errorCode, severity, errorMessage, DEFAULT_MESSAGE, valueName);
        this.validator = new UrlValidator();
        this.protocol = protocol == null || protocol.isEmpty() ? null : protocol;
        this.host = host == null || host.isEmpty() ? null : host;
        this.port = port;
        this.regexp = regexp == null || regexp.isEmpty() ? null : regexp;
        this.compiledRegexp = this.regexp != null ? Pattern.compile(this.regexp, flags) : null;
    }

    @Override
    protected boolean isValid(RuleContext ruleContext, Object value) {
        if (value == null) return false;

        if (!(value instanceof CharSequence))
            throw new ValidationRuleException("UrlValidationRule only applies to CharSequences(eg: Strings)."
                    + "Supplied Class [" + value.getClass() + "] value [" + value + "]");

        String text = value.toString();

        if (!validator.isValid(text)) return false;
        if (compiledRegexp != null && !compiledRegexp.matcher(text).matches()) return false;
        if (protocol == null && host == null && port == ANY_PORT) return true;

        URI uri;

        try {
            uri = new URI(text);
        } catch (URISyntaxException e) {
            return false;
        }

        if (protocol != null && !protocol.equals(uri.getScheme())) return false;
        if (host != null && !host.equals(uri.getHost())) return false;
        return port == ANY_PORT || port == uri.getPort();
    }

    @Override
    protected void customizeViolation(RuleContext ruleContext, RuleViolationBuilder builder) {
        if (protocol != null) builder.param("protocol", protocol);
        if (host != null) builder.param("host", host);
        if (port != ANY_PORT) builder.param("port", port);
        if (regexp != null) builder.param("regexp", regexp);
    }

    @Override
    public List<Class<?>> getSupportedTypes() {
        return SUPPORTED_TYPES;
    }

    /**
     * The protocol the URL must have.
     *
     * @return the protocol; null when not checked.
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * The host the URL must have.
     *
     * @return the host; null when not checked.
     */
    public String getHost() {
        return host;
    }

    /**
     * The port the URL must have.
     *
     * @return the port; {@link #ANY_PORT} when not checked.
     */
    public int getPort() {
        return port;
    }

    /**
     * The regular expression the URL must match.
     *
     * @return the expression; null when not checked.
     */
    public String getRegexp() {
        return regexp;
    }

    @Override
    public String toString() {
        return "UrlValidationRule{protocol=" + protocol + ", host=" + host + ", port=" + port + ", regexp=" + regexp + '}';
    }
}
