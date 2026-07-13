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
package org.rulii.test.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.bind.Binding;
import org.rulii.bind.Bindings;
import org.rulii.bind.match.BindingMatch;
import org.rulii.bind.match.BindingMatchingStrategy;
import org.rulii.bind.match.ParameterMatch;
import org.rulii.model.MethodDefinition;
import org.rulii.model.ParameterDefinition;
import org.rulii.model.SourceDefinition;
import org.rulii.model.UnrulyException;
import org.rulii.rule.Rule;
import org.rulii.rule.RuleDefinition;
import org.rulii.ruleset.RuleSet;
import org.rulii.util.RuleUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static org.rulii.model.condition.Conditions.condition;

/**
 * Test cases for RuleUtils.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class RuleUtilsTest {

    public RuleUtilsTest() {
        super();
    }

    // Used via reflection to obtain named ParameterDefinitions.
    public void sampleMethod(Integer count, String label) {
        // no-op
    }

    private static Method sampleMethod() throws NoSuchMethodException {
        return RuleUtilsTest.class.getDeclaredMethod("sampleMethod", Integer.class, String.class);
    }

    // =========================================================================
    // PASS / FAIL / isValidName
    // =========================================================================

    @Test
    public void testPassAndFail() {
        Assertions.assertTrue(RuleUtils.PASS());
        Assertions.assertFalse(RuleUtils.FAIL());
    }

    @Test
    public void testIsValidName() {
        Assertions.assertTrue(RuleUtils.isValidName("myRule"));
        Assertions.assertTrue(RuleUtils.isValidName("MyRule123"));
        Assertions.assertTrue(RuleUtils.isValidName("$rule"));
        Assertions.assertTrue(RuleUtils.isValidName("_rule"));

        Assertions.assertFalse(RuleUtils.isValidName(null));
        Assertions.assertFalse(RuleUtils.isValidName(""));
        Assertions.assertFalse(RuleUtils.isValidName("   "));
        Assertions.assertFalse(RuleUtils.isValidName("1rule"));
        Assertions.assertFalse(RuleUtils.isValidName("my rule"));
        Assertions.assertFalse(RuleUtils.isValidName("my-rule"));
    }

    // =========================================================================
    // merge
    // =========================================================================

    @Test
    public void testMerge() {
        Rule rule1 = Rule.builder().name("ruleOne").given(condition(() -> true)).build();
        Rule rule2 = Rule.builder().name("ruleTwo").given(condition(() -> false)).build();
        Rule rule3 = Rule.builder().name("ruleThree").given(condition(() -> true)).build();

        Rule[] merged = RuleUtils.merge(rule1, new Rule[]{rule2, rule3});
        Assertions.assertEquals(3, merged.length);
        Assertions.assertSame(rule1, merged[0]);
        Assertions.assertSame(rule2, merged[1]);
        Assertions.assertSame(rule3, merged[2]);
    }

    @Test
    public void testMerge_nullOrEmptyOthers_throws() {
        Rule rule1 = Rule.builder().name("ruleOne").given(condition(() -> true)).build();
        Assertions.assertThrows(IllegalArgumentException.class, () -> RuleUtils.merge(rule1, null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> RuleUtils.merge(rule1, new Rule[0]));
    }

    // =========================================================================
    // createPackageRuleFilter
    // =========================================================================

    @Test
    public void testCreatePackageRuleFilter_noPackages_acceptsAll() {
        Rule rule = Rule.builder().build(PackagedRule.class);
        Assertions.assertTrue(RuleUtils.createPackageRuleFilter().test(rule));
        Assertions.assertTrue(RuleUtils.createPackageRuleFilter((String[]) null).test(rule));
    }

    @Test
    public void testCreatePackageRuleFilter_matchingAndNonMatchingPackage() {
        Rule rule = Rule.builder().build(PackagedRule.class);
        String rulePackage = rule.getTarget().getClass().getPackage().getName();

        Predicate<Rule> matching = RuleUtils.createPackageRuleFilter(rulePackage);
        Predicate<Rule> nonMatching = RuleUtils.createPackageRuleFilter("com.no.such.pkg");

        Assertions.assertTrue(matching.test(rule));
        Assertions.assertFalse(nonMatching.test(rule));
    }

    // =========================================================================
    // convert / getSignature
    // =========================================================================

    @Test
    public void testConvert() throws NoSuchMethodException {
        List<ParameterDefinition> defs = ParameterDefinition.load(sampleMethod(), true, SourceDefinition.build());
        List<ParameterMatch> matches = new ArrayList<>();
        for (ParameterDefinition def : defs) {
            matches.add(new ParameterMatch(def, null));
        }

        Map<String, Object> result = RuleUtils.convert(matches, List.of(42, "hello"));
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(42, result.get("count"));
        Assertions.assertEquals("hello", result.get("label"));
        // The result must be unmodifiable.
        Assertions.assertThrows(UnsupportedOperationException.class, () -> result.put("x", 1));
    }

    @Test
    public void testConvert_nullOrEmptyInputs_returnEmptyMap() throws NoSuchMethodException {
        List<ParameterDefinition> defs = ParameterDefinition.load(sampleMethod(), true, SourceDefinition.build());
        List<ParameterMatch> matches = List.of(new ParameterMatch(defs.get(0), null));

        Assertions.assertTrue(RuleUtils.convert(null, List.of(1)).isEmpty());
        Assertions.assertTrue(RuleUtils.convert(List.of(), List.of(1)).isEmpty());
        Assertions.assertTrue(RuleUtils.convert(matches, null).isEmpty());
        Assertions.assertTrue(RuleUtils.convert(matches, List.of()).isEmpty());
    }

    @Test
    public void testConvert_sizeMismatch_throwsUnrulyException() throws NoSuchMethodException {
        List<ParameterDefinition> defs = ParameterDefinition.load(sampleMethod(), true, SourceDefinition.build());
        List<ParameterMatch> matches = List.of(new ParameterMatch(defs.get(0), null));
        Assertions.assertThrows(UnrulyException.class, () -> RuleUtils.convert(matches, List.of(1, 2)));
    }

    @Test
    public void testGetSignature_withArgs() throws NoSuchMethodException {
        Rule rule = Rule.builder().name("sigRule").description("My signature rule")
                .given(condition(() -> true)).build();

        List<ParameterDefinition> defs = ParameterDefinition.load(sampleMethod(), true, SourceDefinition.build());
        List<ParameterMatch> matches = new ArrayList<>();
        for (ParameterDefinition def : defs) {
            matches.add(new ParameterMatch(def, null));
        }

        String signature = RuleUtils.getSignature(rule, matches, List.of(42, "hello"));
        Assertions.assertTrue(signature.contains("My signature rule"));
        Assertions.assertTrue(signature.contains("args ["));
        Assertions.assertTrue(signature.contains("count = 42"));
        Assertions.assertTrue(signature.contains("label = hello"));
    }

    @Test
    public void testGetSignature_noArgs_isJustTheDescription() {
        Rule rule = Rule.builder().name("sigRule").description("My signature rule")
                .given(condition(() -> true)).build();
        String signature = RuleUtils.getSignature(rule, List.of(), List.of());
        Assertions.assertEquals("My signature rule", signature);
    }

    // =========================================================================
    // getDefaultRuleName
    // =========================================================================

    @Test
    public void testGetDefaultRuleName() {
        Assertions.assertEquals("string", RuleUtils.getDefaultRuleName(String.class));
        Assertions.assertEquals("packagedRule", RuleUtils.getDefaultRuleName(PackagedRule.class));
        Assertions.assertThrows(IllegalArgumentException.class, () -> RuleUtils.getDefaultRuleName(null));
    }

    // =========================================================================
    // Descriptions
    // =========================================================================

    @Test
    public void testGetRuleSetDescription() {
        Rule rule = Rule.builder().name("ruleOne").given(condition(() -> true)).build();
        RuleSet<?> ruleSet = RuleSet.builder().with("descRuleSet").rule(rule).build();

        String description = RuleUtils.getRuleSetDescription(ruleSet, "> ");
        Assertions.assertTrue(description.contains("descRuleSet"));
        Assertions.assertTrue(description.contains("Number of Rules : 1"));
        Assertions.assertTrue(description.contains("PreCondition    : false"));
        Assertions.assertTrue(description.startsWith("> "));
    }

    @Test
    public void testGetRuleDescription() {
        Rule rule = Rule.builder().name("describedRule").description("A rule with a description")
                .given(condition((Integer count, String label) -> true)).build();
        RuleDefinition ruleDefinition = rule.getDefinition();

        String description = RuleUtils.getRuleDescription(ruleDefinition,
                ruleDefinition.getConditionDefinition(), "  ");
        Assertions.assertTrue(description.contains("describedRule"));
        Assertions.assertTrue(description.contains("A rule with a description"));
        Assertions.assertTrue(description.contains("Method"));
    }

    @Test
    public void testGetMethodDescription() throws NoSuchMethodException {
        MethodDefinition methodDefinition = MethodDefinition.load(sampleMethod(), true, SourceDefinition.build());

        String description = RuleUtils.getMethodDescription(methodDefinition, null, null, "");
        Assertions.assertTrue(description.contains("Method :"));
        Assertions.assertTrue(description.contains("Parameter Matches :"));
    }

    @Test
    public void testGetArgumentDescriptions_nullMatchesAndValues_returnsEmpty() throws NoSuchMethodException {
        MethodDefinition methodDefinition = MethodDefinition.load(sampleMethod(), true, SourceDefinition.build());
        Assertions.assertEquals("", RuleUtils.getArgumentDescriptions(methodDefinition, null, null, ""));
    }

    @Test
    public void testGetArgumentDescriptions_withCompatibleBinding() throws NoSuchMethodException {
        MethodDefinition methodDefinition = MethodDefinition.load(sampleMethod(), true, SourceDefinition.build());

        Bindings bindings = Bindings.builder().standard();
        bindings.bind("count", 100);
        Binding<Object> binding = bindings.getBinding("count");

        ParameterMatch match = new ParameterMatch(methodDefinition.getParameterDefinition(0),
                new BindingMatch<>(binding, BindingMatchingStrategy.class));

        String description = RuleUtils.getArgumentDescriptions(methodDefinition,
                new ParameterMatch[]{match, null}, new Object[]{100, null}, "\t");

        Assertions.assertTrue(description.contains("count = 100"));
        Assertions.assertTrue(description.contains("Matched Binding"));
        // Integer binding against Integer parameter - no type mismatch warning expected.
        Assertions.assertFalse(description.contains("Types Incompatible"));
        // Second parameter had no value.
        Assertions.assertTrue(description.contains("null)"));
    }

    @Test
    public void testGetArgumentDescriptions_withIncompatibleBinding() throws NoSuchMethodException {
        MethodDefinition methodDefinition = MethodDefinition.load(sampleMethod(), true, SourceDefinition.build());

        Bindings bindings = Bindings.builder().standard();
        bindings.bind("count", "not a number");
        Binding<Object> binding = bindings.getBinding("count");

        ParameterMatch match = new ParameterMatch(methodDefinition.getParameterDefinition(0),
                new BindingMatch<>(binding, BindingMatchingStrategy.class));

        String description = RuleUtils.getArgumentDescriptions(methodDefinition,
                new ParameterMatch[]{match}, new Object[]{"not a number"}, "\t");

        Assertions.assertTrue(description.contains("Types Incompatible"));
    }

    // =========================================================================
    // getTabs / getSummaryTextValue
    // =========================================================================

    @Test
    public void testGetTabs() {
        Assertions.assertEquals("", RuleUtils.getTabs(0));
        Assertions.assertEquals("", RuleUtils.getTabs(-3));
        Assertions.assertEquals("\t\t\t", RuleUtils.getTabs(3));
    }

    @Test
    public void testGetSummaryTextValue() {
        Assertions.assertEquals("null", RuleUtils.getSummaryTextValue(null));
        Assertions.assertEquals("short", RuleUtils.getSummaryTextValue("short"));

        String longText = "x".repeat(100);
        String summary = RuleUtils.getSummaryTextValue(longText);
        Assertions.assertEquals("x".repeat(80) + "...", summary);

        Assertions.assertEquals("abcde...", RuleUtils.getSummaryTextValue("abcdefgh", 5));
        Assertions.assertEquals("abc", RuleUtils.getSummaryTextValue("abc", 5));

        // Objects whose toString() returns null are summarized as "null".
        Assertions.assertEquals("null", RuleUtils.getSummaryTextValue(new NullToString()));
    }

    private static class NullToString {

        public NullToString() {
            super();
        }

        @Override
        public String toString() {
            return null;
        }
    }

    @org.rulii.annotation.Rule
    public static class PackagedRule {

        public PackagedRule() {
            super();
        }

        @org.rulii.annotation.Given
        public boolean when() {
            return true;
        }
    }
}
