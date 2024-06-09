/*
 * This software is licensed under the Apache 2 license, quoted below.
 *
 * Copyright (c) 1999-2024, Algorithmx Inc.
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
package org.rulii.util;

import org.rulii.lang.Runnable;

import java.util.Comparator;

/**
 * A `Comparator` implementation that compares objects of type `Runnable`.
 * This class provides a way to sort runnable objects based on certain criteria.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class RunnableComparator implements Comparator<Runnable<?>> {

    public RunnableComparator() {
        super();
    }

    @Override
    public int compare(Runnable<?> r1, Runnable<?> r2) {
        if (r1 == r2) return 0;
        if (r1 == null) return 1;
        if (r2 == null) return 1;

        if (r1 instanceof Ordered && r2 instanceof Ordered) {
            return Integer.compare(((Ordered) r1).getOrder(), ((Ordered) r2).getOrder());
        }

        return 0;
    }
}
