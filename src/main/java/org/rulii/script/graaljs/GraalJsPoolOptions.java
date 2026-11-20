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
package org.rulii.script.graaljs;

import org.rulii.lib.spring.util.Assert;

import java.time.Duration;

/**
 * How a {@link GraalJsScriptProcessorFactory} that reuses JavaScript contexts keeps them.
 *
 * <p>Handing these options to the factory is what turns reuse on; see
 * {@link GraalJsScriptProcessorFactory#isReuseContexts()} for what reuse changes for scripts.
 *
 * @param maxIdle      how many contexts are kept between evaluations; the ones beyond are closed when they are
 *                     given back. At least one.
 * @param idleTimeout  a context that has not been used for this long is closed; {@link Duration#ZERO} for never.
 * @param maxAge       a context is closed once it has lived this long, however busy it was; {@link Duration#ZERO}
 *                     for never.
 * @param maxUses      a context is closed after this many evaluations; zero for no limit. One gives every
 *                     evaluation a context of its own, as without reuse.
 * @param initialSize  how many contexts are created when the pool is, so that the first evaluations find one
 *                     ready; between zero and {@code maxIdle}.
 * @param cleanGlobals whether the global variables a script leaves behind (one it assigned without declaring, for
 *                     one) are removed before the context is used again.
 *
 * @author Max Arulananthan
 * @since 2.3
 * @see GraalJsScriptProcessorFactory
 */
public record GraalJsPoolOptions(int maxIdle, Duration idleTimeout, Duration maxAge, int maxUses, int initialSize,
                                 boolean cleanGlobals) {

    /** Prefix of the system properties {@link #fromSystemProperties()} reads: {@value}. */
    public static final String PROPERTY_PREFIX      = "rulii.script.graaljs.pool.";

    /** One context per processor, at least two. */
    public static final int DEFAULT_MAX_IDLE         = Math.max(2, Runtime.getRuntime().availableProcessors());
    /** Five minutes. */
    public static final Duration DEFAULT_IDLE_TIMEOUT = Duration.ofMinutes(5);
    /** Thirty minutes. */
    public static final Duration DEFAULT_MAX_AGE     = Duration.ofMinutes(30);
    /** A thousand evaluations. */
    public static final int DEFAULT_MAX_USES         = 1_000;

    /** The defaults: see the constants of this record; no context created up front, globals cleaned. */
    public static final GraalJsPoolOptions DEFAULT   = builder().build();

    /**
     * Validates the options.
     *
     * @throws IllegalArgumentException if one of them is out of range.
     */
    public GraalJsPoolOptions {
        Assert.isTrue(maxIdle >= 1, "maxIdle must be at least one.");
        Assert.notNull(idleTimeout, "idleTimeout cannot be null.");
        Assert.isTrue(!idleTimeout.isNegative(), "idleTimeout cannot be negative.");
        Assert.notNull(maxAge, "maxAge cannot be null.");
        Assert.isTrue(!maxAge.isNegative(), "maxAge cannot be negative.");
        Assert.isTrue(maxUses >= 0, "maxUses cannot be negative.");
        Assert.isTrue(initialSize >= 0 && initialSize <= maxIdle, "initialSize must be between zero and maxIdle.");
    }

    /**
     * A builder that starts from the defaults.
     *
     * @return a new builder; never null.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * The defaults, each overridden by its system property when that is set:
     * {@code rulii.script.graaljs.pool.maxIdle}, {@code .idleTimeout} and {@code .maxAge} (ISO-8601 durations such
     * as {@code PT5M}), {@code .maxUses}, {@code .initialSize} and {@code .cleanGlobals}.
     *
     * @return the options; never null.
     * @throws IllegalArgumentException if a property cannot be read as its option or is out of range.
     */
    public static GraalJsPoolOptions fromSystemProperties() {
        Builder result = builder();
        String value;

        if ((value = property("maxIdle")) != null) result.maxIdle(number("maxIdle", value));
        if ((value = property("idleTimeout")) != null) result.idleTimeout(duration("idleTimeout", value));
        if ((value = property("maxAge")) != null) result.maxAge(duration("maxAge", value));
        if ((value = property("maxUses")) != null) result.maxUses(number("maxUses", value));
        if ((value = property("initialSize")) != null) result.initialSize(number("initialSize", value));
        if ((value = property("cleanGlobals")) != null) result.cleanGlobals(Boolean.parseBoolean(value));

        return result.build();
    }

    private static String property(String name) {
        String result = System.getProperty(PROPERTY_PREFIX + name);
        return result != null && !result.isBlank() ? result.trim() : null;
    }

    private static int number(String name, String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(PROPERTY_PREFIX + name + " [" + value + "] is not a number.", e);
        }
    }

    private static Duration duration(String name, String value) {
        try {
            return Duration.parse(value);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(PROPERTY_PREFIX + name + " [" + value
                    + "] is not a duration such as PT5M.", e);
        }
    }

    /**
     * Builds {@link GraalJsPoolOptions}, starting from the defaults.
     */
    public static final class Builder {

        private int maxIdle = DEFAULT_MAX_IDLE;
        private Duration idleTimeout = DEFAULT_IDLE_TIMEOUT;
        private Duration maxAge = DEFAULT_MAX_AGE;
        private int maxUses = DEFAULT_MAX_USES;
        private int initialSize = 0;
        private boolean cleanGlobals = true;

        private Builder() {
            super();
        }

        /**
         * @param maxIdle how many contexts are kept between evaluations; at least one.
         * @return this builder.
         */
        public Builder maxIdle(int maxIdle) {
            this.maxIdle = maxIdle;
            return this;
        }

        /**
         * @param idleTimeout how long a context may go unused before it is closed; {@link Duration#ZERO} for never.
         * @return this builder.
         */
        public Builder idleTimeout(Duration idleTimeout) {
            this.idleTimeout = idleTimeout;
            return this;
        }

        /**
         * @param maxAge how long a context may live; {@link Duration#ZERO} for no limit.
         * @return this builder.
         */
        public Builder maxAge(Duration maxAge) {
            this.maxAge = maxAge;
            return this;
        }

        /**
         * @param maxUses how many evaluations a context serves; zero for no limit.
         * @return this builder.
         */
        public Builder maxUses(int maxUses) {
            this.maxUses = maxUses;
            return this;
        }

        /**
         * @param initialSize how many contexts are created up front; between zero and {@code maxIdle}.
         * @return this builder.
         */
        public Builder initialSize(int initialSize) {
            this.initialSize = initialSize;
            return this;
        }

        /**
         * @param cleanGlobals whether the globals a script leaves behind are removed before the context is used again.
         * @return this builder.
         */
        public Builder cleanGlobals(boolean cleanGlobals) {
            this.cleanGlobals = cleanGlobals;
            return this;
        }

        /**
         * @return the options; never null.
         * @throws IllegalArgumentException if one of them is out of range.
         */
        public GraalJsPoolOptions build() {
            return new GraalJsPoolOptions(maxIdle, idleTimeout, maxAge, maxUses, initialSize, cleanGlobals);
        }
    }
}
