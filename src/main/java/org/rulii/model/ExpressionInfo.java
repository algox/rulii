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
package org.rulii.model;

import org.rulii.lib.spring.util.Assert;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Describes what a {@link org.rulii.model.condition.Condition}, {@link org.rulii.model.action.Action}
 * or {@link org.rulii.model.function.Function} <em>is</em>, without running it.
 *
 * <p>There are three kinds:
 * <ul>
 *   <li>{@link Kind#SCRIPT}: built from a {@link org.rulii.script.Script}. Carries the language and
 *       the source text as written (see {@link org.rulii.script.Script#getSourceText()}).</li>
 *   <li>{@link Kind#COMPILED}: compiled code (a lambda, a method or a class). Carries the
 *       {@link MethodDefinition} when one is known; {@code method} is {@code null} when the
 *       implementation is not introspectable, in which case only the runtime class is known.</li>
 *   <li>{@link Kind#COMPOSITE}: built out of other expressions, for example {@code a.and(b)} or
 *       {@code condition.not()}. Carries the operator and the operands in evaluation order.</li>
 * </ul>
 *
 * <p>Fields that don't apply to a kind are {@code null}; {@code operands} is never null.
 *
 * @param kind       what the expression is made of.
 * @param language   scripting language name ({@code SCRIPT} only).
 * @param sourceText unresolved script text ({@code SCRIPT} only).
 * @param method     method signature ({@code COMPILED} only; may be null).
 * @param operator   operator symbol or name ({@code COMPOSITE} only).
 * @param operands   operand expressions in evaluation order ({@code COMPOSITE} only; empty otherwise).
 *
 * @author Max Arulananthan
 * @since 2.1
 */
public record ExpressionInfo(Kind kind, String language, String sourceText, MethodDefinition method,
                                   String operator, List<ExpressionInfo> operands) {

    /**
     * What an expression is made of.
     */
    public enum Kind { SCRIPT, COMPILED, COMPOSITE }

    public ExpressionInfo {
        Assert.notNull(kind, "kind cannot be null.");
        operands = operands == null ? Collections.emptyList() : Collections.unmodifiableList(operands);

        switch (kind) {
            case SCRIPT -> {
                Assert.hasText(language, "language cannot be empty for a SCRIPT expression.");
                Assert.hasText(sourceText, "sourceText cannot be empty for a SCRIPT expression.");
            }
            case COMPOSITE -> {
                Assert.hasText(operator, "operator cannot be empty for a COMPOSITE expression.");
                Assert.notEmpty(operands, "operands cannot be empty for a COMPOSITE expression.");
            }
            case COMPILED -> { /* method may be null when the implementation is not introspectable. */ }
        }
    }

    /**
     * A script expression.
     *
     * @param language   scripting language name; must not be empty.
     * @param sourceText script text as written; must not be empty.
     * @return new definition.
     */
    public static ExpressionInfo script(String language, String sourceText) {
        return new ExpressionInfo(Kind.SCRIPT, language, sourceText, null, null, null);
    }

    /**
     * A compiled expression with a known method.
     *
     * @param method method signature; may be null when the implementation is not introspectable.
     * @return new definition.
     */
    public static ExpressionInfo compiled(MethodDefinition method) {
        return new ExpressionInfo(Kind.COMPILED, null, null, method, null, null);
    }

    /**
     * A composite expression.
     *
     * @param operator operator symbol or name; must not be empty.
     * @param operands operands in evaluation order; must not be empty.
     * @return new definition.
     */
    public static ExpressionInfo composite(String operator, ExpressionInfo... operands) {
        Assert.notNull(operands, "operands cannot be null.");
        return new ExpressionInfo(Kind.COMPOSITE, null, null, null, operator, Arrays.asList(operands));
    }

    /**
     * A composite expression.
     *
     * @param operator operator symbol or name; must not be empty.
     * @param operands operands in evaluation order; must not be empty.
     * @return new definition.
     */
    public static ExpressionInfo composite(String operator, List<ExpressionInfo> operands) {
        return new ExpressionInfo(Kind.COMPOSITE, null, null, null, operator, operands);
    }

    /**
     * Whether this expression can be shown as text (a script) or only as a signature.
     *
     * @return true for {@code SCRIPT}, and for a {@code COMPOSITE} whose operands are all readable.
     */
    public boolean isReadable() {
        return switch (kind) {
            case SCRIPT -> true;
            case COMPILED -> false;
            case COMPOSITE -> operands.stream().allMatch(o -> o != null && o.isReadable());
        };
    }
}
