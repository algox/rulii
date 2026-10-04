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

import java.util.List;

/**
 * The business classification of a rule, rule set or rule flow: where it belongs and what it is
 * about. Implemented by their definitions ({@code RuleDefinition}, {@code RuleSetDefinition},
 * {@code RuleFlowDefinition}) and by nothing else: a method, parameter or return type has no
 * category. Both parts are descriptive only; rulii never reads them when it runs. Tools such as
 * rulii-explorer group and filter by them.
 *
 * <p>Values are normalised when the definition is built ({@code RuleUtils.normalizeCategory} and
 * {@code normalizeTags}): a category has each {@code /} level trimmed and empty levels dropped,
 * and is null when nothing is left; tags are trimmed, blanks dropped, and each kept once in the
 * order first given. Nothing is ever inferred from a package, a file or a name.
 *
 * @author Max Arulananthan
 * @since 2.1
 * @see org.rulii.annotation.Category
 * @see org.rulii.annotation.Tags
 */
public interface Categorized {

    /**
     * The business category, such as {@code "Pricing"} or {@code "Pricing/Discounts"} ({@code /}
     * separates the levels of a hierarchy). At most one.
     *
     * @return the category, or null when none.
     */
    String getCategory();

    /**
     * Short labels saying what the object is about, such as {@code "vip"} or {@code "fraud"}.
     *
     * @return unmodifiable list; empty when none, never null.
     */
    List<String> getTags();
}
