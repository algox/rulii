/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.rulii.lib.spring.core;

import java.util.Comparator;

import org.rulii.lib.spring.lang.Nullable;
import org.rulii.lib.spring.util.ObjectUtils;
import org.rulii.lib.spring.core.annotation.AnnotationAwareOrderComparator;

/**
 * {@link Comparator} implementation for {@link Ordered} objects, sorting by order value ascending,
 * respectively by priority descending.
 *
 * <h3>Same Order Objects</h3>
 * <p>
 * Objects that have the same order value will be sorted with arbitrary ordering with respect to
 * other objects with the same order value.
 *
 * <h3>Non-ordered Objects</h3>
 * <p>
 * Any object that does not provide its own order value is implicitly assigned a value of
 * {@link Ordered#LOWEST_PRECEDENCE}, thus ending up at the end of a sorted collection in arbitrary
 * order with respect to other objects with the same order value.
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 07.04.2003
 * @see Ordered
 * @see AnnotationAwareOrderComparator
 * @see java.util.List#sort(Comparator)
 * @see java.util.Arrays#sort(Object[], Comparator)
 */

public class OrderComparator implements Comparator<Object> {

  /**
   * Build an adapted order comparator with the given source provider.
   *
   * @param sourceProvider the order source provider to use
   * @return the adapted comparator
   * @since 4.1
   */
  public Comparator<Object> withSourceProvider(final OrderSourceProvider sourceProvider) { // NO_UCD (test only)
    return (o1, o2) -> doCompare(o1, o2, sourceProvider);
  }

  @Override
  public int compare(@Nullable Object o1, @Nullable Object o2) {
    return doCompare(o1, o2, null);
  }

  private int doCompare(@Nullable Object o1, @Nullable Object o2, @Nullable OrderSourceProvider sourceProvider) {
    boolean p1 = (o1 instanceof PriorityOrdered);
    boolean p2 = (o2 instanceof PriorityOrdered);
    if (p1 && !p2) {
      return -1;
    } else if (p2 && !p1) {
      return 1;
    }

    // Direct evaluation instead of Integer.compareTo to avoid unnecessary object creation.
    int i1 = getOrder(o1, sourceProvider);
    int i2 = getOrder(o2, sourceProvider);
    return (i1 < i2) ? -1 : (i1 > i2) ? 1 : 0;
  }

  /**
   * Determine the order value for the given object.
   * <p>
   * The default implementation checks against the given {@link OrderSourceProvider} using
   * {@link #findOrder} and falls back to a regular {@link #getOrder(Object)} call.
   *
   * @param obj the object to check
   * @return the order value, or {@code Ordered.LOWEST_PRECEDENCE} as fallback
   */
  private int getOrder(@Nullable Object obj, @Nullable OrderSourceProvider sourceProvider) {
    Integer order = null;
    if (obj != null && sourceProvider != null) {
      Object orderSource = sourceProvider.getOrderSource(obj);
      if (orderSource != null) {
        if (orderSource.getClass().isArray()) {
          Object[] sources = ObjectUtils.toObjectArray(orderSource);
          for (Object source : sources) {
            order = findOrder(source);
            if (order != null) {
              break;
            }
          }
        } else {
          order = findOrder(orderSource);
        }
      }
    }
    return (order != null ? order : getOrder(obj));
  }

  /**
   * Determine the order value for the given object.
   * <p>
   * The default implementation checks against the {@link Ordered} interface through delegating to
   * {@link #findOrder}. Can be overridden in subclasses.
   *
   * @param obj the object to check
   * @return the order value, or {@code Ordered.LOWEST_PRECEDENCE} as fallback
   */
  protected int getOrder(@Nullable Object obj) {
    if (obj != null) {
      Integer order = findOrder(obj);
      if (order != null) {
        return order;
      }
    }
    return Ordered.LOWEST_PRECEDENCE;
  }

  /**
   * Find an order value indicated by the given object.
   * <p>
   * The default implementation checks against the {@link Ordered} interface. Can be overridden in
   * subclasses.
   *
   * @param obj the object to check
   * @return the order value, or {@code null} if none found
   */
  @Nullable
  protected Integer findOrder(Object obj) {
    return (obj instanceof Ordered ? ((Ordered) obj).getOrder() : null);
  }

  /**
   * Determine a priority value for the given object, if any.
   * <p>
   * The default implementation always returns {@code null}. Subclasses may override this to give
   * specific kinds of values a 'priority' characteristic, in addition to their 'order' semantics. A
   * priority indicates that it may be used for selecting one object over another, in addition to
   * serving for ordering purposes in a list/array.
   *
   * @param obj the object to check
   * @return the priority value, or {@code null} if none
   * @since 4.1
   */
  @Nullable
  public Integer getPriority(Object obj) {
    return null;
  }

  /**
   * Strategy interface to provide an order source for a given object.
   *
   * @since 4.1
   */
  @FunctionalInterface
  public interface OrderSourceProvider {

    /**
     * Return an order source for the specified object, i.e. an object that should be checked for an
     * order value as a replacement to the given object.
     * <p>
     * Can also be an array of order source objects.
     * <p>
     * If the returned object does not indicate any order, the comparator will fall back to checking
     * the original object.
     *
     * @param obj the object to find an order source for
     * @return the order source for that object, or {@code null} if none found
     */
    @Nullable
    Object getOrderSource(Object obj);
  }

}
