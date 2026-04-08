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
package org.rulii.validation.rules;

import org.rulii.context.RuleContext;
import org.rulii.model.function.Function;
import org.rulii.validation.BindingFunction;
import org.rulii.validation.rules.alpha.AlphaValidationRule;
import org.rulii.validation.rules.alpha.AlphaValidationRuleBuilder;
import org.rulii.validation.rules.alphnumeric.AlphaNumericValidationRule;
import org.rulii.validation.rules.alphnumeric.AlphaNumericValidationRuleBuilder;
import org.rulii.validation.rules.ascii.AsciiValidationRule;
import org.rulii.validation.rules.ascii.AsciiValidationRuleBuilder;
import org.rulii.validation.rules.asssert.*;
import org.rulii.validation.rules.blank.BlankValidationRule;
import org.rulii.validation.rules.blank.BlankValidationRuleBuilder;
import org.rulii.validation.rules.decimal.DecimalValidationRule;
import org.rulii.validation.rules.decimal.DecimalValidationRuleBuilder;
import org.rulii.validation.rules.digits.DigitsValidationRule;
import org.rulii.validation.rules.digits.DigitsValidationRuleBuilder;
import org.rulii.validation.rules.email.EmailValidationRule;
import org.rulii.validation.rules.email.EmailValidationRuleBuilder;
import org.rulii.validation.rules.endswith.EndsWithValidationRule;
import org.rulii.validation.rules.endswith.EndsWithValidationRuleBuilder;
import org.rulii.validation.rules.fileexists.FileExistsValidationRule;
import org.rulii.validation.rules.fileexists.FileExistsValidationRuleBuilder;
import org.rulii.validation.rules.future.FutureOrPresentValidationRule;
import org.rulii.validation.rules.future.FutureOrPresentValidationRuleBuilder;
import org.rulii.validation.rules.future.FutureValidationRule;
import org.rulii.validation.rules.future.FutureValidationRuleBuilder;
import org.rulii.validation.rules.in.InValidationRule;
import org.rulii.validation.rules.in.InValidationRuleBuilder;
import org.rulii.validation.rules.lowercase.LowerCaseValidationRule;
import org.rulii.validation.rules.lowercase.LowerCaseValidationRuleBuilder;
import org.rulii.validation.rules.max.DecimalMaxValidationRule;
import org.rulii.validation.rules.max.DecimalMaxValidationRuleBuilder;
import org.rulii.validation.rules.max.MaxValidationRule;
import org.rulii.validation.rules.max.MaxValidationRuleBuilder;
import org.rulii.validation.rules.min.DecimalMinValidationRule;
import org.rulii.validation.rules.min.DecimalMinValidationRuleBuilder;
import org.rulii.validation.rules.min.MinValidationRule;
import org.rulii.validation.rules.min.MinValidationRuleBuilder;
import org.rulii.validation.rules.negative.NegativeOrZeroValidationRule;
import org.rulii.validation.rules.negative.NegativeOrZeroValidationRuleBuilder;
import org.rulii.validation.rules.negative.NegativeValidationRule;
import org.rulii.validation.rules.negative.NegativeValidationRuleBuilder;
import org.rulii.validation.rules.notblank.NotBlankValidationRule;
import org.rulii.validation.rules.notblank.NotBlankValidationRuleBuilder;
import org.rulii.validation.rules.notempty.NotEmptyValidationRule;
import org.rulii.validation.rules.notempty.NotEmptyValidationRuleBuilder;
import org.rulii.validation.rules.notnull.NotNullValidationRule;
import org.rulii.validation.rules.notnull.NotNullValidationRuleBuilder;
import org.rulii.validation.rules.nulll.NullValidationRule;
import org.rulii.validation.rules.nulll.NullValidationRuleBuilder;
import org.rulii.validation.rules.numeric.NumericValidationRule;
import org.rulii.validation.rules.numeric.NumericValidationRuleBuilder;
import org.rulii.validation.rules.past.PastOrPresentValidationRule;
import org.rulii.validation.rules.past.PastOrPresentValidationRuleBuilder;
import org.rulii.validation.rules.past.PastValidationRule;
import org.rulii.validation.rules.past.PastValidationRuleBuilder;
import org.rulii.validation.rules.pattern.PatternValidationRule;
import org.rulii.validation.rules.pattern.PatternValidationRuleBuilder;
import org.rulii.validation.rules.positive.PositiveOrZeroValidationRule;
import org.rulii.validation.rules.positive.PositiveOrZeroValidationRuleBuilder;
import org.rulii.validation.rules.positive.PositiveValidationRule;
import org.rulii.validation.rules.positive.PositiveValidationRuleBuilder;
import org.rulii.validation.rules.size.SizeValidationRule;
import org.rulii.validation.rules.size.SizeValidationRuleBuilder;
import org.rulii.validation.rules.startswith.StartsWithValidationRule;
import org.rulii.validation.rules.startswith.StartsWithValidationRuleBuilder;
import org.rulii.validation.rules.uppercase.UpperCaseValidationRule;
import org.rulii.validation.rules.uppercase.UpperCaseValidationRuleBuilder;
import org.rulii.validation.rules.url.UrlValidationRule;
import org.rulii.validation.rules.url.UrlValidationRuleBuilder;

import java.math.BigDecimal;
import java.util.Collection;

import static org.rulii.model.function.Functions.function;

/**
 * Static factory class for all built-in validation rule builders. Each method returns a pre-configured
 * builder that can be further customized and then materialized into a {@link org.rulii.rule.Rule}
 * via {@link org.rulii.validation.ValueValidationRuleBuilder#build()}.
 *
 * <p>Use {@link #binding(String)} to create a function that fetches a named binding from the
 * {@link org.rulii.context.RuleContext} at rule execution time, or {@link #value(Object)} to supply
 * a constant value. These are the typical arguments passed to the factory methods below.
 *
 * <pre>{@code
 * Rule rule = Validators.notNull(Validators.binding("age")).message("Age is required.").build();
 * }</pre>
 *
 * @author Max Arulananthan
 * @since 1.2
 */
public final class Validators {

    private Validators() {
        super();
    }

    /**
     * Creates a builder for {@link org.rulii.validation.rules.alpha.AlphaValidationRule}.
     *
     * @param function the function that supplies the value to validate
     * @return a new {@link AlphaValidationRuleBuilder}
     */
    public static AlphaValidationRuleBuilder alpha(Function<?> function) {
        return AlphaValidationRule.builder(function);
    }

    /**
     * Creates a builder for {@link org.rulii.validation.rules.alphnumeric.AlphaNumericValidationRule}.
     *
     * @param function the function that supplies the value to validate
     * @return a new {@link AlphaNumericValidationRuleBuilder}
     */
    public static AlphaNumericValidationRuleBuilder alphaNumeric(Function<?> function) {
        return AlphaNumericValidationRule.builder(function);
    }

    /**
     * Creates a builder for {@link org.rulii.validation.rules.ascii.AsciiValidationRule}.
     *
     * @param function the function that supplies the value to validate
     * @return a new {@link AsciiValidationRuleBuilder}
     */
    public static AsciiValidationRuleBuilder ascii(Function<?> function) {
        return AsciiValidationRule.builder(function);
    }

    /**
     * Creates a builder for {@link org.rulii.validation.rules.asssert.AssertFalseValidationRule}.
     *
     * @param function the function that supplies the value to validate
     * @return a new {@link AssertFalseValidationRuleBuilder}
     */
    public static AssertFalseValidationRuleBuilder assertFalse(Function<?> function) {
        return AssertFalseValidationRule.builder(function);
    }

    /**
     * Creates a builder for {@link org.rulii.validation.rules.asssert.AssertTrueValidationRule}.
     *
     * @param function the function that supplies the value to validate
     * @return a new {@link AssertTrueValidationRuleBuilder}
     */
    public static AssertTrueValidationRuleBuilder assertTrue(Function<?> function) {
        return AssertTrueValidationRule.builder(function);
    }

    /**
     * Creates a builder for {@link org.rulii.validation.rules.asssert.AssertEqualsValidationRule}.
     *
     * @param function the function that supplies the value to validate
     * @param value    the expected value that the supplied value must equal
     * @return a new {@link AssertEqualsValidationRuleBuilder}
     */
    public static AssertEqualsValidationRuleBuilder assertEquals(Function<?> function, Object value) {
        return AssertEqualsValidationRule.builder(function, value);
    }

    /**
     * Creates a builder for {@link org.rulii.validation.rules.asssert.AssertNotEqualsValidationRule}.
     *
     * @param function the function that supplies the value to validate
     * @param value    the value that the supplied value must not equal
     * @return a new {@link AssertNotEqualsValidationRuleBuilder}
     */
    public static AssertNotEqualsValidationRuleBuilder assertNotEquals(Function<?> function, Object value) {
        return AssertNotEqualsValidationRule.builder(function, value);
    }

    /**
     * Creates a builder for {@link org.rulii.validation.rules.blank.BlankValidationRule}.
     *
     * @param function the function that supplies the value to validate
     * @return a new {@link BlankValidationRuleBuilder}
     */
    public static BlankValidationRuleBuilder blank(Function<?> function) {
        return BlankValidationRule.builder(function);
    }

    /**
     * Creates a builder for {@link org.rulii.validation.rules.decimal.DecimalValidationRule}.
     *
     * @param function the function that supplies the value to validate
     * @return a new {@link DecimalValidationRuleBuilder}
     */
    public static DecimalValidationRuleBuilder decimal(Function<?> function) {
        return DecimalValidationRule.builder(function);
    }

    /**
     * Creates a builder for {@link org.rulii.validation.rules.digits.DigitsValidationRule}.
     *
     * @param function           the function that supplies the value to validate
     * @param maxIntegerLength   the maximum number of integral digits allowed
     * @param maxFractionLength  the maximum number of fractional digits allowed
     * @return a new {@link DigitsValidationRuleBuilder}
     */
    public static DigitsValidationRuleBuilder digits(Function<?> function, int maxIntegerLength, int maxFractionLength) {
        return DigitsValidationRule.builder(function, maxIntegerLength, maxFractionLength);
    }

    /**
     * Creates a builder for {@link org.rulii.validation.rules.email.EmailValidationRule}.
     *
     * @param function the function that supplies the value to validate
     * @return a new {@link EmailValidationRuleBuilder}
     */
    public static EmailValidationRuleBuilder email(Function<?> function) {
        return EmailValidationRule.builder(function);
    }

    /**
     * Creates a builder for {@link org.rulii.validation.rules.endswith.EndsWithValidationRule}.
     *
     * @param function the function that supplies the value to validate
     * @param suffixes one or more suffixes that the value must end with
     * @return a new {@link EndsWithValidationRuleBuilder}
     */
    public static EndsWithValidationRuleBuilder endsWith(Function<?> function, String... suffixes) {
        return EndsWithValidationRule.builder(function, suffixes);
    }

    /**
     * Creates a builder for {@link org.rulii.validation.rules.fileexists.FileExistsValidationRule}.
     *
     * @param function the function that supplies the file path value to validate
     * @return a new {@link FileExistsValidationRuleBuilder}
     */
    public static FileExistsValidationRuleBuilder fileExists(Function<?> function) {
        return FileExistsValidationRule.builder(function);
    }

    /**
     * Creates a builder for {@link org.rulii.validation.rules.future.FutureValidationRule}.
     *
     * @param function the function that supplies the date/time value to validate
     * @return a new {@link FutureValidationRuleBuilder}
     */
    public static FutureValidationRuleBuilder future(Function<?> function) {
        return FutureValidationRule.builder(function);
    }

    /**
     * Creates a builder for {@link org.rulii.validation.rules.future.FutureOrPresentValidationRule}.
     *
     * @param function the function that supplies the date/time value to validate
     * @return a new {@link FutureOrPresentValidationRuleBuilder}
     */
    public static FutureOrPresentValidationRuleBuilder futureOrPresent(Function<?> function) {
        return FutureOrPresentValidationRule.builder(function);
    }

    /**
     * Creates a builder for {@link org.rulii.validation.rules.in.InValidationRule}.
     *
     * @param function the function that supplies the value to validate
     * @param values   the collection of acceptable values
     * @return a new {@link InValidationRuleBuilder}
     */
    public static InValidationRuleBuilder in(Function<?> function, Collection<?> values) {
        return InValidationRule.builder(function, values);
    }

    /**
     * Creates a builder for {@link org.rulii.validation.rules.lowercase.LowerCaseValidationRule}.
     *
     * @param function the function that supplies the value to validate
     * @return a new {@link LowerCaseValidationRuleBuilder}
     */
    public static LowerCaseValidationRuleBuilder lowerCase(Function<?> function) {
        return LowerCaseValidationRule.builder(function);
    }

    /**
     * Creates a builder for {@link org.rulii.validation.rules.max.MaxValidationRule}.
     *
     * @param function the function that supplies the value to validate
     * @param max      the maximum allowed value (inclusive)
     * @return a new {@link MaxValidationRuleBuilder}
     */
    public static MaxValidationRuleBuilder max(Function<?> function, long max) {
        return MaxValidationRule.builder(function, max);
    }

    /**
     * Creates a builder for {@link org.rulii.validation.rules.max.DecimalMaxValidationRule}.
     *
     * @param function the function that supplies the value to validate
     * @param max      the maximum allowed decimal value
     * @return a new {@link DecimalMaxValidationRuleBuilder}
     */
    public static DecimalMaxValidationRuleBuilder decimalMax(Function<?> function, BigDecimal max) {
        return DecimalMaxValidationRule.builder(function, max);
    }

    /**
     * Creates a builder for {@link org.rulii.validation.rules.min.MinValidationRule}.
     *
     * @param function the function that supplies the value to validate
     * @param min      the minimum allowed value (inclusive)
     * @return a new {@link MinValidationRuleBuilder}
     */
    public static MinValidationRuleBuilder min(Function<?> function, long min) {
        return MinValidationRule.builder(function, min);
    }

    /**
     * Creates a builder for {@link org.rulii.validation.rules.min.DecimalMinValidationRule}.
     *
     * @param function the function that supplies the value to validate
     * @param min      the minimum allowed decimal value
     * @return a new {@link DecimalMinValidationRuleBuilder}
     */
    public static DecimalMinValidationRuleBuilder decimalMin(Function<?> function, BigDecimal min) {
        return DecimalMinValidationRule.builder(function, min);
    }

    /**
     * Creates a builder for {@link org.rulii.validation.rules.negative.NegativeValidationRule}.
     *
     * @param function the function that supplies the value to validate
     * @return a new {@link NegativeValidationRuleBuilder}
     */
    public static NegativeValidationRuleBuilder negative(Function<?> function) {
        return NegativeValidationRule.builder(function);
    }

    /**
     * Creates a builder for {@link org.rulii.validation.rules.negative.NegativeOrZeroValidationRule}.
     *
     * @param function the function that supplies the value to validate
     * @return a new {@link NegativeOrZeroValidationRuleBuilder}
     */
    public static NegativeOrZeroValidationRuleBuilder negativeOrZero(Function<?> function) {
        return NegativeOrZeroValidationRule.builder(function);
    }

    /**
     * Creates a builder for {@link org.rulii.validation.rules.notblank.NotBlankValidationRule}.
     *
     * @param function the function that supplies the value to validate
     * @return a new {@link NotBlankValidationRuleBuilder}
     */
    public static NotBlankValidationRuleBuilder notBlank(Function<?> function) {
        return NotBlankValidationRule.builder(function);
    }

    /**
     * Creates a builder for {@link org.rulii.validation.rules.notempty.NotEmptyValidationRule}.
     *
     * @param function the function that supplies the value to validate
     * @return a new {@link NotEmptyValidationRuleBuilder}
     */
    public static NotEmptyValidationRuleBuilder notEmpty(Function<?> function) {
        return NotEmptyValidationRule.builder(function);
    }

    /**
     * Creates a builder for {@link org.rulii.validation.rules.notnull.NotNullValidationRule}.
     *
     * @param function the function that supplies the value to validate
     * @return a new {@link NotNullValidationRuleBuilder}
     */
    public static NotNullValidationRuleBuilder notNull(Function<?> function) {
        return NotNullValidationRule.builder(function);
    }

    /**
     * Creates a builder for {@link org.rulii.validation.rules.nulll.NullValidationRule}.
     *
     * @param function the function that supplies the value to validate
     * @return a new {@link NullValidationRuleBuilder}
     */
    public static NullValidationRuleBuilder isNull(Function<?> function) {
        return NullValidationRule.builder(function);
    }

    /**
     * Creates a builder for {@link org.rulii.validation.rules.numeric.NumericValidationRule}.
     *
     * @param function the function that supplies the value to validate
     * @return a new {@link NumericValidationRuleBuilder}
     */
    public static NumericValidationRuleBuilder numeric(Function<?> function) {
        return NumericValidationRule.builder(function);
    }

    /**
     * Creates a builder for {@link org.rulii.validation.rules.past.PastValidationRule}.
     *
     * @param function the function that supplies the date/time value to validate
     * @return a new {@link PastValidationRuleBuilder}
     */
    public static PastValidationRuleBuilder past(Function<?> function) {
        return PastValidationRule.builder(function);
    }

    /**
     * Creates a builder for {@link org.rulii.validation.rules.past.PastOrPresentValidationRule}.
     *
     * @param function the function that supplies the date/time value to validate
     * @return a new {@link PastOrPresentValidationRuleBuilder}
     */
    public static PastOrPresentValidationRuleBuilder pastOrPresent(Function<?> function) {
        return PastOrPresentValidationRule.builder(function);
    }

    /**
     * Creates a builder for {@link org.rulii.validation.rules.pattern.PatternValidationRule}.
     *
     * @param function the function that supplies the value to validate
     * @param pattern  the regex pattern that the value must match
     * @return a new {@link PatternValidationRuleBuilder}
     */
    public static PatternValidationRuleBuilder pattern(Function<?> function, String pattern) {
        return PatternValidationRule.builder(function, pattern);
    }

    /**
     * Creates a builder for {@link org.rulii.validation.rules.positive.PositiveValidationRule}.
     *
     * @param function the function that supplies the value to validate
     * @return a new {@link PositiveValidationRuleBuilder}
     */
    public static PositiveValidationRuleBuilder positive(Function<?> function) {
        return PositiveValidationRule.builder(function);
    }

    /**
     * Creates a builder for {@link org.rulii.validation.rules.positive.PositiveOrZeroValidationRule}.
     *
     * @param function the function that supplies the value to validate
     * @return a new {@link PositiveOrZeroValidationRuleBuilder}
     */
    public static PositiveOrZeroValidationRuleBuilder positiveOrZero(Function<?> function) {
        return PositiveOrZeroValidationRule.builder(function);
    }

    /**
     * Creates a builder for {@link org.rulii.validation.rules.size.SizeValidationRule}.
     *
     * @param function the function that supplies the value to validate
     * @param min      the minimum size (inclusive)
     * @param max      the maximum size (inclusive)
     * @return a new {@link SizeValidationRuleBuilder}
     */
    public static SizeValidationRuleBuilder size(Function<?> function, int min, int max) {
        return SizeValidationRule.builder(function, min, max);
    }

    /**
     * Creates a builder for {@link org.rulii.validation.rules.startswith.StartsWithValidationRule}.
     *
     * @param function the function that supplies the value to validate
     * @param prefixes one or more prefixes that the value must start with
     * @return a new {@link StartsWithValidationRuleBuilder}
     */
    public static StartsWithValidationRuleBuilder startsWith(Function<?> function, String... prefixes) {
        return StartsWithValidationRule.builder(function, prefixes);
    }

    /**
     * Creates a builder for {@link org.rulii.validation.rules.uppercase.UpperCaseValidationRule}.
     *
     * @param function the function that supplies the value to validate
     * @return a new {@link UpperCaseValidationRuleBuilder}
     */
    public static UpperCaseValidationRuleBuilder upperCase(Function<?> function) {
        return UpperCaseValidationRule.builder(function);
    }

    /**
     * Creates a builder for {@link org.rulii.validation.rules.url.UrlValidationRule}.
     *
     * @param function the function that supplies the value to validate
     * @return a new {@link UrlValidationRuleBuilder}
     */
    public static UrlValidationRuleBuilder url(Function<?> function) {
        return UrlValidationRule.builder(function);
    }

    /**
     * Creates a {@link org.rulii.validation.BindingFunction} that fetches the named binding from the
     * {@link org.rulii.context.RuleContext} at rule execution time. The binding name is also used as
     * the parameter key in rule violations.
     *
     * @param bindingName the name of the binding to look up in the rule context
     * @return a {@link Function} that retrieves the binding value by name
     */
    public static Function<?> binding(String bindingName) {
        return new BindingFunction<>(bindingName, function((RuleContext ruleContext) -> ruleContext.getBindings().getValue(bindingName)));
    }

    /**
     * Creates a constant {@link Function} that always returns the given value, regardless of the rule
     * context. Uses {@code "value"} as the default parameter key name in rule violations.
     *
     * @param value the constant value to return
     * @return a {@link Function} that always returns {@code value}
     */
    public static Function<?> value(Object value) {
        return function((RuleContext ruleContext) -> value);
    }
}
