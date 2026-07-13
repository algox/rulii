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
import org.rulii.context.RuleContext;
import org.rulii.model.Runnable;
import org.rulii.util.Ordered;
import org.rulii.util.RunnableComparator;

/**
 * Tests for RunnableComparator.
 *
 * @author Max Arulananthan
 */
public class RunnableComparatorTest {

    public RunnableComparatorTest() {
        super();
    }

    @Test
    public void testCompare_nullHandling_isAntisymmetric() {
        RunnableComparator comparator = new RunnableComparator();
        Runnable<Object> nonNull = ctx -> null;

        int nonNullVsNull = comparator.compare(nonNull, null);
        int nullVsNonNull = comparator.compare(null, nonNull);

        // A non-null value must sort before null (nulls-last), and the two comparisons must
        // have opposite signs - Comparator's antisymmetry contract: sgn(compare(x,y)) == -sgn(compare(y,x)).
        Assertions.assertTrue(nonNullVsNull < 0);
        Assertions.assertTrue(nullVsNonNull > 0);
    }

    @Test
    public void testCompare_bothNull_isZero() {
        RunnableComparator comparator = new RunnableComparator();
        Assertions.assertEquals(0, comparator.compare(null, null));
    }

    @Test
    public void testCompare_sameInstance_isZero() {
        RunnableComparator comparator = new RunnableComparator();
        Runnable<Object> runnable = ctx -> null;
        Assertions.assertEquals(0, comparator.compare(runnable, runnable));
    }

    @Test
    public void testCompare_bothOrdered_sortsByOrder() {
        RunnableComparator comparator = new RunnableComparator();
        OrderedRunnable low = new OrderedRunnable(1);
        OrderedRunnable high = new OrderedRunnable(10);

        Assertions.assertTrue(comparator.compare(low, high) < 0);
        Assertions.assertTrue(comparator.compare(high, low) > 0);
        Assertions.assertEquals(0, comparator.compare(low, new OrderedRunnable(1)));
    }

    @Test
    public void testCompare_onlyOneOrdered_isZero() {
        RunnableComparator comparator = new RunnableComparator();
        OrderedRunnable ordered = new OrderedRunnable(5);
        Runnable<Object> plain = ctx -> null;

        Assertions.assertEquals(0, comparator.compare(ordered, plain));
        Assertions.assertEquals(0, comparator.compare(plain, ordered));
    }

    private static class OrderedRunnable implements Runnable<Object>, Ordered {

        private final int order;

        public OrderedRunnable(int order) {
            super();
            this.order = order;
        }

        @Override
        public Object run(RuleContext ctx) {
            return null;
        }

        @Override
        public int getOrder() {
            return order;
        }
    }
}
