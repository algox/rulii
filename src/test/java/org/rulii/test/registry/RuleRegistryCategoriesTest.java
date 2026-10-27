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
package org.rulii.test.registry;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.model.Categorized;
import org.rulii.model.Runnable;
import org.rulii.model.function.Function;
import org.rulii.registry.DefaultRuleRegistry;
import org.rulii.registry.RuleRegistry;
import org.rulii.rule.Rule;
import org.rulii.ruleflow.RuleFlow;
import org.rulii.ruleset.RuleSet;

import java.util.List;
import java.util.Set;

import static org.rulii.model.condition.Conditions.condition;

/**
 * Listing a registry by the category and tags its runnables declare.
 */
public class RuleRegistryCategoriesTest {

    public RuleRegistryCategoriesTest() {
        super();
    }

    private static DefaultRuleRegistry registry() {
        DefaultRuleRegistry registry = new DefaultRuleRegistry();
        registry.register(Rule.builder().name("discountRule").given(condition(() -> true)).category("Pricing/Discounts").tags("vip").build());
        registry.register(RuleSet.builder().with("pricingSet").category("Pricing").tags("vip", "b2b").build());
        registry.register(RuleFlow.builder().name("shippingFlow").category("Shipping").tags("vip").bind("a", 1).build());
        registry.register(Rule.builder().name("plainRule").given(condition(() -> true)).build());
        registry.register(Function.builder().with(() -> 1).name("plainFunction").build());
        return registry;
    }

    private static List<String> names(List<Runnable<?>> runnables) {
        return runnables.stream().map(Runnable::getName).toList();
    }

    @Test
    public void categorizedReadsTheDefinitionOfRulesRuleSetsAndFlows() {
        DefaultRuleRegistry registry = registry();

        Categorized rule = RuleRegistry.categorized(registry.get("discountRule"));
        Assertions.assertEquals("Pricing/Discounts", rule.getCategory());
        Assertions.assertEquals(List.of("vip"), rule.getTags());
        Assertions.assertEquals("Pricing", RuleRegistry.categorized(registry.get("pricingSet")).getCategory());
        Assertions.assertEquals("Shipping", RuleRegistry.categorized(registry.get("shippingFlow")).getCategory());
        Assertions.assertNull(RuleRegistry.categorized(registry.get("plainRule")).getCategory());
        Assertions.assertNull(RuleRegistry.categorized(registry.get("plainFunction")), "a function declares neither");
        Assertions.assertThrows(IllegalArgumentException.class, () -> RuleRegistry.categorized(null));
    }

    @Test
    public void getByCategoryIncludesTheCategoriesBelow() {
        DefaultRuleRegistry registry = registry();

        Assertions.assertEquals(List.of("discountRule", "pricingSet"), names(registry.getByCategory("Pricing")));
        Assertions.assertEquals(List.of("discountRule"), names(registry.getByCategory("Pricing/Discounts")));
        Assertions.assertEquals(List.of("discountRule"), names(registry.getByCategory(" Pricing / Discounts ")), "normalised as a definition's");
        Assertions.assertEquals(List.of("shippingFlow"), names(registry.getByCategory("Shipping")));
        Assertions.assertTrue(registry.getByCategory("Pric").isEmpty(), "a prefix of a level is not a parent category");
        Assertions.assertTrue(registry.getByCategory("Nothing").isEmpty());
        Assertions.assertThrows(IllegalArgumentException.class, () -> registry.getByCategory(" "));
        Assertions.assertThrows(UnsupportedOperationException.class, () -> registry.getByCategory("Pricing").clear());
    }

    @Test
    public void getByTagsRequiresEveryTag() {
        DefaultRuleRegistry registry = registry();

        Assertions.assertEquals(List.of("discountRule", "pricingSet", "shippingFlow"), names(registry.getByTags("vip")));
        Assertions.assertEquals(List.of("pricingSet"), names(registry.getByTags("vip", "b2b")));
        Assertions.assertEquals(List.of("pricingSet"), names(registry.getByTags(" b2b ")));
        Assertions.assertTrue(registry.getByTags("nobody").isEmpty());
        Assertions.assertThrows(IllegalArgumentException.class, () -> registry.getByTags());
        Assertions.assertThrows(IllegalArgumentException.class, () -> registry.getByTags(" "));
    }

    @Test
    public void getCategoriesAndGetTagsListEachOnceInRegistrationOrder() {
        DefaultRuleRegistry registry = registry();

        Assertions.assertEquals(List.of("Pricing/Discounts", "Pricing", "Shipping"), List.copyOf(registry.getCategories()));
        Assertions.assertEquals(List.of("vip", "b2b"), List.copyOf(registry.getTags()));
        Assertions.assertThrows(UnsupportedOperationException.class, () -> registry.getTags().clear());

        Set<String> empty = new DefaultRuleRegistry().getCategories();
        Assertions.assertTrue(empty.isEmpty());
    }
}
