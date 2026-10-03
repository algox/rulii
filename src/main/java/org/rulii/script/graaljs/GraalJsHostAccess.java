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

import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Host access policy used by {@link GraalJsScriptProcessorFactory}.
 *
 * <p>Starts from {@link HostAccess#ALL} and adds target type mappings that <em>detach</em>
 * JavaScript values at the moment they cross into Java: as the result of an evaluation, as a value
 * written into the rule bindings ({@code ctx.out = [1, 2]}), or as an argument to a Java method
 * or bean setter whose parameter is typed {@link Object}, {@link List} or {@link Collection}.
 * (A parameter typed {@link Map} still receives a live proxy: GraalJS's JSR-223 layer itself
 * reads and writes bindings through a {@code Map} view of the script's global object, and a
 * mapping for that target would detach the view and cut the bindings off from the script.)
 * Without the mappings GraalJS hands Java a live proxy ({@code PolyglotList},
 * {@code PolyglotMap}, ...) that is only usable while the polyglot {@code Context} that produced
 * it stays open. Since {@link GraalJsScriptProcessor} closes that context right after each
 * evaluation, a live proxy would be dead by the time a rule's caller looked at it. A detached copy
 * has no such dependency.
 *
 * <p>Detachment rules:
 * <ul>
 *   <li>JavaScript arrays, {@code Set}s and other iterables become {@link ArrayList}s.</li>
 *   <li>JavaScript object literals and {@code Map}s become {@link LinkedHashMap}s with string keys.</li>
 *   <li>JavaScript {@code Date}s become {@link java.time.Instant}s; temporal values without a zone
 *       become {@link java.time.LocalDate}, {@link java.time.LocalTime} or {@link LocalDateTime}.</li>
 *   <li>Numbers become {@link Integer}, {@link Long}, {@link Double} or {@link java.math.BigInteger},
 *       whichever is the smallest that fits; strings and booleans become their Java counterparts.</li>
 *   <li>Java host objects pass through untouched, by identity.</li>
 *   <li>Functions and classes cannot be detached and remain live proxies. The processor detects
 *       those and keeps their context open.</li>
 * </ul>
 * Nesting is followed to a depth of {@value #MAX_DEPTH}; anything deeper is left as a proxy.
 *
 * <p>Consequence worth knowing: a value a script writes into the bindings is a Java copy, so
 * reading it back inside the same script yields a Java {@code List}/{@code Map}, not the original
 * JavaScript object ({@code ctx.out.add(4)} rather than {@code ctx.out.push(4)}).
 *
 * @author Max Arulananthan
 * @since 2.1
 * @see GraalJsScriptProcessorFactory
 * @see GraalJsScriptProcessor
 */
public final class GraalJsHostAccess {

    /** Deepest nesting level that is copied; deeper values stay live proxies. */
    public static final int MAX_DEPTH = 128;

    /** {@link HostAccess#ALL} plus the detaching target type mappings described above. */
    public static final HostAccess DETACHING = HostAccess.newBuilder(HostAccess.ALL)
            .targetTypeMapping(Value.class, Object.class, GraalJsHostAccess::isDetachable, GraalJsHostAccess::detach)
            .targetTypeMapping(Value.class, List.class, GraalJsHostAccess::isDetachableSequence, v -> (List<?>) detach(v))
            .targetTypeMapping(Value.class, Collection.class, GraalJsHostAccess::isDetachableSequence, v -> (Collection<?>) detach(v))
            // Deliberately no mapping for a Map target: GraalJS's own JSR-223 layer views the
            // JavaScript global object as a Map<String, Object> and writes bindings through that
            // view. Detaching it would silently disconnect every binding from the script.
            .build();

    private GraalJsHostAccess() {
        super();
    }

    /**
     * Returns whether {@code value} is a guest value that {@link #detach(Value)} can turn into a
     * plain Java copy: a non-host, non-executable array, iterable, hash, temporal or object value.
     *
     * @param value the value about to cross into Java; may be null.
     * @return {@code true} if the value should be detached rather than proxied.
     */
    public static boolean isDetachable(Value value) {
        if (value == null || value.isNull() || value.isHostObject() || value.isProxyObject()) return false;
        if (value.canExecute() || value.canInstantiate()) return false;
        if (value.isString() || value.isNumber() || value.isBoolean()) return false;
        return value.hasArrayElements() || value.hasHashEntries() || value.hasIterator()
                || value.isInstant() || value.isDate() || value.isTime() || value.isDuration()
                || value.hasMembers();
    }

    private static boolean isDetachableSequence(Value value) {
        return isDetachable(value) && (value.hasArrayElements() || value.hasIterator()) && !value.hasHashEntries();
    }

    /**
     * Produces a Java copy of {@code value} that does not depend on the originating context.
     * Values that cannot be copied (functions, classes, values nested deeper than
     * {@value #MAX_DEPTH}) are returned as GraalJS's default proxy instead.
     *
     * @param value the value to detach; must not be null.
     * @return the detached Java value; may be null when the guest value is null or undefined.
     */
    public static Object detach(Value value) {
        return detach(value, 0);
    }

    private static Object detach(Value value, int depth) {
        if (value.isNull()) return null;
        if (value.isHostObject()) return value.asHostObject();
        if (value.isProxyObject()) return value.asProxyObject();
        if (value.isString()) return value.asString();
        if (value.isBoolean()) return value.asBoolean();
        if (value.isNumber()) return detachNumber(value);
        if (value.isInstant()) return value.asInstant();
        if (value.isDate() && value.isTime()) return LocalDateTime.of(value.asDate(), value.asTime());
        if (value.isDate()) return value.asDate();
        if (value.isTime()) return value.asTime();
        if (value.isDuration()) return value.asDuration();
        if (depth >= MAX_DEPTH || value.canExecute() || value.canInstantiate()) return value.as(Object.class);

        if (value.hasArrayElements()) {
            long size = value.getArraySize();
            List<Object> result = new ArrayList<>((int) Math.min(size, Integer.MAX_VALUE));
            for (long i = 0; i < size; i++) {
                result.add(detach(value.getArrayElement(i), depth + 1));
            }
            return result;
        }

        if (value.hasHashEntries()) {
            Map<Object, Object> result = new LinkedHashMap<>();
            Value iterator = value.getHashEntriesIterator();
            while (iterator.hasIteratorNextElement()) {
                Value entry = iterator.getIteratorNextElement();
                result.put(detach(entry.getArrayElement(0), depth + 1), detach(entry.getArrayElement(1), depth + 1));
            }
            return result;
        }

        if (value.hasIterator()) {
            List<Object> result = new ArrayList<>();
            Value iterator = value.getIterator();
            while (iterator.hasIteratorNextElement()) {
                result.add(detach(iterator.getIteratorNextElement(), depth + 1));
            }
            return result;
        }

        if (value.hasMembers()) {
            Map<String, Object> result = new LinkedHashMap<>();
            for (String key : value.getMemberKeys()) {
                result.put(key, detach(value.getMember(key), depth + 1));
            }
            return result;
        }

        return value.as(Object.class);
    }

    private static Object detachNumber(Value value) {
        if (value.fitsInInt()) return value.asInt();
        if (value.fitsInLong()) return value.asLong();
        if (value.fitsInDouble()) return value.asDouble();
        if (value.fitsInBigInteger()) return value.asBigInteger();
        return value.as(Number.class);
    }
}
