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
package org.rulii.test.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.annotation.Category;
import org.rulii.annotation.Description;
import org.rulii.annotation.Given;
import org.rulii.annotation.Tags;
import org.rulii.model.Categorized;
import org.rulii.model.Definition;
import org.rulii.model.MethodDefinition;
import org.rulii.model.ParameterDefinition;
import org.rulii.model.ReturnTypeDefinition;
import org.rulii.rule.Rule;
import org.rulii.rule.RuleDefinition;
import org.rulii.ruleflow.RuleFlow;
import org.rulii.ruleflow.RuleFlowDefinition;
import org.rulii.ruleset.RuleSet;
import org.rulii.ruleset.RuleSetDefinition;
import org.rulii.util.RuleUtils;
import org.rulii.validation.rules.notnull.NotNullValidationRule;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.rulii.model.condition.Conditions.condition;
import static org.rulii.validation.rules.Validators.binding;

/**
 * Category and tags on rules, rule sets and rule flows: from annotations, from the builders,
 * normalised the same way everywhere, and absent by default.
 *
 * @author Max Arulananthan
 * @since 2.1
 */
public class CategoryTagsTest {

    @org.rulii.annotation.Rule("labelledRule")
    @Description("A rule with a category and tags.")
    @Category(" Pricing / Discounts ")
    @Tags({"vip", " discount ", "vip", "", "  "})
    public static class LabelledRule {
        @Given
        public boolean when(Integer total) {
            return total > 10;
        }
    }

    @org.rulii.annotation.Rule("plainRule")
    public static class PlainRule {
        @Given
        public boolean when(Integer total) {
            return total > 10;
        }
    }

    @Test
    public void annotationsAreReadAndNormalised() {
        Rule rule = Rule.builder().with(LabelledRule.class).build();
        Assertions.assertEquals("Pricing/Discounts", rule.getDefinition().getCategory());
        Assertions.assertEquals(Arrays.asList("vip", "discount"), rule.getDefinition().getTags());
        Assertions.assertEquals("A rule with a category and tags.", rule.getDefinition().getDescription());
    }

    @Test
    public void absentByDefault() {
        Rule rule = Rule.builder().with(PlainRule.class).build();
        Assertions.assertNull(rule.getDefinition().getCategory());
        Assertions.assertEquals(Collections.emptyList(), rule.getDefinition().getTags());
        // Only the three artifact definitions are Categorized; a method, parameter or return type is not
        Assertions.assertTrue(Categorized.class.isAssignableFrom(RuleDefinition.class));
        Assertions.assertTrue(Categorized.class.isAssignableFrom(RuleSetDefinition.class));
        Assertions.assertTrue(Categorized.class.isAssignableFrom(RuleFlowDefinition.class));
        Assertions.assertFalse(Categorized.class.isAssignableFrom(MethodDefinition.class));
        Assertions.assertFalse(Categorized.class.isAssignableFrom(ParameterDefinition.class));
        Assertions.assertFalse(Categorized.class.isAssignableFrom(ReturnTypeDefinition.class));
        Assertions.assertFalse(Categorized.class.isAssignableFrom(Definition.class));
    }

    @Test
    public void lambdaRuleBuilder() {
        Rule rule = Rule.builder()
                .name("fraudRule")
                .category(" Fraud / Scoring ")
                .tags("fraud", "scoring")
                .tags(Arrays.asList("fraud", null, " "))
                .given(condition((Integer score) -> score > 50))
                .build();
        Assertions.assertEquals("Fraud/Scoring", rule.getDefinition().getCategory());
        Assertions.assertEquals(Arrays.asList("fraud", "scoring"), rule.getDefinition().getTags());
    }

    @Test
    public void blankCategoryMeansNone() {
        Rule rule = Rule.builder().name("blankRule").category(" / / ").given(condition(() -> true)).build();
        Assertions.assertNull(rule.getDefinition().getCategory());
        Assertions.assertNull(RuleUtils.normalizeCategory(null));
        Assertions.assertNull(RuleUtils.normalizeCategory("   "));
        Assertions.assertEquals("A/B", RuleUtils.normalizeCategory("/A//B/"));
        Assertions.assertEquals(Collections.emptyList(), RuleUtils.normalizeTags(null));
        Assertions.assertThrows(UnsupportedOperationException.class, () -> RuleUtils.normalizeTags(List.of("a")).add("b"));
    }

    @Test
    public void suppliedValidationRule() {
        Rule rule = Rule.builder().validationRule("checkRule", condition(() -> true)).errorCode("E1")
                .category("Checks").tags("supplied").build();
        Assertions.assertEquals("Checks", rule.getDefinition().getCategory());
        Assertions.assertEquals(List.of("supplied"), rule.getDefinition().getTags());
    }

    @Test
    public void valueValidationRule() {
        Rule rule = NotNullValidationRule.builder(binding("fieldA")).category("Checks/Presence").tags("required").build();
        Assertions.assertEquals("Checks/Presence", rule.getDefinition().getCategory());
        Assertions.assertEquals(List.of("required"), rule.getDefinition().getTags());
    }

    @Test
    public void ruleSetAndRuleFlow() {
        RuleSet<?> ruleSet = RuleSet.builder().with("pricingRules").category("Pricing").tags("pricing", "pricing", "catalogue").build();
        Assertions.assertEquals("Pricing", ruleSet.getDefinition().getCategory());
        Assertions.assertEquals(Arrays.asList("pricing", "catalogue"), ruleSet.getDefinition().getTags());
        Assertions.assertNull(RuleSet.builder().with("plainRules").build().getDefinition().getCategory());

        RuleFlow<?> flow = RuleFlow.builder().name("fulfilmentFlow").category("Fulfilment").tags("nightly").bind("a", 1).build();
        Assertions.assertEquals("Fulfilment", flow.getDefinition().getCategory());
        Assertions.assertEquals(List.of("nightly"), flow.getDefinition().getTags());
        Assertions.assertEquals(Collections.emptyList(), RuleFlow.builder().name("plainFlow").bind("a", 1).build().getDefinition().getTags());
    }
}
