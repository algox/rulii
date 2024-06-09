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

import java.time.*;
import java.time.chrono.HijrahDate;
import java.time.chrono.JapaneseDate;
import java.time.chrono.MinguoDate;
import java.time.chrono.ThaiBuddhistDate;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * The TimeComparator class provides static methods for comparing temporal objects with the current time.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public final class TimeComparator {

    private static final Map<Class<?>, TemporalComparator<?>> comparators = new HashMap<>();

    static {
        comparators.put(Calendar.class, (TemporalComparator<Calendar>) TimeComparator::compare);
        comparators.put(Date.class, (TemporalComparator<Date>) TimeComparator::compare);
        comparators.put(java.sql.Date.class, (TemporalComparator<java.sql.Date>) TimeComparator::compare);
        comparators.put(Instant.class, (TemporalComparator<Instant>) TimeComparator::compare);
        comparators.put(LocalDate.class, (TemporalComparator<LocalDate>) TimeComparator::compare);
        comparators.put(LocalDateTime.class, (TemporalComparator<LocalDateTime>) TimeComparator::compare);
        comparators.put(LocalTime.class, (TemporalComparator<LocalTime>) TimeComparator::compare);
        comparators.put(MonthDay.class, (TemporalComparator<MonthDay>) TimeComparator::compare);
        comparators.put(OffsetDateTime.class, (TemporalComparator<OffsetDateTime>) TimeComparator::compare);
        comparators.put(OffsetTime.class, (TemporalComparator<OffsetTime>) TimeComparator::compare);
        comparators.put(Year.class, (TemporalComparator<Year>) TimeComparator::compare);
        comparators.put(YearMonth.class, (TemporalComparator<YearMonth>) TimeComparator::compare);
        comparators.put(ZonedDateTime.class, (TemporalComparator<ZonedDateTime>) TimeComparator::compare);
        comparators.put(HijrahDate.class, (TemporalComparator<HijrahDate>) TimeComparator::compare);
        comparators.put(JapaneseDate.class, (TemporalComparator<JapaneseDate>) TimeComparator::compare);
        comparators.put(MinguoDate.class, (TemporalComparator<MinguoDate>) TimeComparator::compare);
        comparators.put(ThaiBuddhistDate.class, (TemporalComparator<ThaiBuddhistDate>) TimeComparator::compare);
    }

    private TimeComparator() {
        super();
    }

    /**
     * Compares the given value with the current time using a suitable comparator.
     *
     * @param value the value to compare
     * @param clock the clock to use for comparison
     * @return 0 if the value is equal to the current time, a negative value if the value is before the current time,
     *         a positive value if the value is after the current time, or {@code null} if a comparator could not be found
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Integer compare(Object value, Clock clock) {
        TemporalComparator comparator = comparators.get(value.getClass());

        if (comparator != null) return comparator.compare(value, clock);
        if (value instanceof Date) return compare((Date) value, clock);
        if (value instanceof Calendar) return compare((Calendar) value, clock);

        // Could not find a comparator
        return null;
    }

    /**
     * Compares the given value with the current time using a suitable comparator.
     *
     * @param value the value to compare
     * @param clock the clock to use for comparison
     * @return 0 if the value is equal to the current time, a negative value if the value is before the current time,
     *         a positive value if the value is after the current time, or {@code null} if a comparator could not be found
     */
    public static int compare(Calendar value, Clock clock) {
        return value.toInstant().compareTo(clock.instant());
    }

    /**
     * Compares the given value with the current time using a suitable comparator.
     *
     * @param value the value to compare
     * @param clock the clock to use for comparison
     * @return 0 if the value is equal to the current time, a negative value if the value is before the current time,
     *         a positive value if the value is after the current time, or {@code null} if a comparator could not be found
     */
    public static int compare(Date value, Clock clock) {
        return value.toInstant().compareTo(clock.instant());
    }

    /**
     * Compares a java.sql.Date object with the current instant provided by a Clock object.
     *
     * @param value the java.sql.Date object to compare
     * @param clock the Clock object used to get the current instant
     * @return a negative integer if the value is before the current instant,
     *         zero if the value is equal to the current instant,
     *         or a positive integer if the value is after the current instant
     */
    public static int compare(java.sql.Date value, Clock clock) {
        return value.toInstant().compareTo(clock.instant());
    }

    /**
     * Compares the given value with the current time using a suitable comparator.
     *
     * @param value the value to compare
     * @param clock the clock to use for comparison
     * @return 0 if the value is equal to the current time, a negative value if the value is before the current time,
     *         a positive value if the value is after the current time, or {@code null} if a comparator could not be found
     */
    public static int compare(Instant value, Clock clock) {
        return value.compareTo(clock.instant());
    }

    /**
     * Compares the given value with the current time using a suitable comparator.
     *
     * @param value the value to compare
     * @param clock the clock to use for comparison
     * @return 0 if the value is equal to the current time, a negative value if the value is before the current time,
     *         a positive value if the value is after the current time
     */
    public static int compare(LocalDate value, Clock clock) {
        return value.compareTo(LocalDate.now(clock));
    }

    /**
     * Compares the given LocalDateTime value with the current time using a specified Clock.
     *
     * @param value the LocalDateTime value to compare
     * @param clock the Clock to use for comparison
     * @return 0 if the value is equal to the current time, a negative value if the value is before the current time,
     *         a positive value if the value is after the current time
     */
    public static int compare(LocalDateTime value, Clock clock) {
        return value.compareTo(LocalDateTime.now(clock));
    }

    /**
     * Compares the given LocalTime value with the current time using a specified Clock.
     *
     * @param value the LocalTime value to compare
     * @param clock the Clock to use for comparison
     * @return 0 if the value is equal to the current time, a negative value if the value is before the current time,
     *         a positive value if the value is after the current time
     */
    public static int compare(LocalTime value, Clock clock) {
        return value.compareTo(LocalTime.now(clock));
    }

    /**
     * Compares the given OffsetDateTime value with the current time using the specified Clock object.
     *
     * @param value the OffsetDateTime value to compare
     * @param clock the Clock object used to get the current time
     * @return 0 if the value is equal to the current time, a negative value if the value is before the current time,
     *         a positive value if the value is after the current time
     */
    public static int compare(OffsetDateTime value, Clock clock) {
        return value.compareTo(OffsetDateTime.now(clock));
    }

    /**
     * Compares the given OffsetTime value with the current time using the specified Clock.
     *
     * @param value the value to compare
     * @param clock the clock to use for comparison
     * @return 0 if the value is equal to the current time, a negative value if the value is before the current time,
     *         a positive value if the value is after the current time
     */
    public static int compare(OffsetTime value, Clock clock) {
        return value.compareTo(OffsetTime.now(clock));
    }

    /**
     * Compares the given MonthDay value with the current time using a specified Clock.
     *
     * @param value the MonthDay value to compare
     * @param clock the Clock to use for comparison
     * @return 0 if the value is equal to the current time, a negative value if the value is before the current time,
     *         a positive value if the value is after the current time
     */
    public static int compare(MonthDay value, Clock clock) {
        return value.compareTo(MonthDay.now(clock));
    }

    /**
     * Compares the given value with the current time using a suitable comparator.
     *
     * @param value the value to compare
     * @param clock the clock to use for comparison
     * @return 0 if the value is equal to the current time, a negative value if the value is before the current time,
     *         a positive value if the value is after the current time, or {@code null} if a comparator could not be found
     */
    public static int compare(Year value, Clock clock) {
        return value.compareTo(Year.now(clock));
    }

    /**
     * Compares the given YearMonth value with the current time using the specified Clock.
     *
     * @param value the YearMonth value to compare
     * @param clock the Clock to use for comparison
     * @return 0 if the value is equal to the current time, a negative value if the value is before the current time,
     *         a positive value if the value is after the current time
     */
    public static int compare(YearMonth value, Clock clock) {
        return value.compareTo(YearMonth.now(clock));
    }

    /**
     * Compares the given value with the current time using a specified clock.
     *
     * @param value the value to compare
     * @param clock the clock to use for comparison
     * @return 0 if the value is equal to the current time, a negative value if the value is before the current time,
     *         a positive value if the value is after the current time
     */
    public static int compare(ZonedDateTime value, Clock clock) {
        return value.compareTo(ZonedDateTime.now(clock));
    }

    /**
     * Compares the given HijrahDate value with the current time using a specified Clock.
     *
     * @param value the HijrahDate value to compare
     * @param clock the Clock to use for comparison
     * @return 0 if the value is equal to the current time, a negative value if the value is before the current time,
     *         a positive value if the value is after the current time
     */
    public static int compare(HijrahDate value, Clock clock) {
        return value.compareTo(HijrahDate.now(clock));
    }

    /**
     * Compares the given JapaneseDate value with the current time using a specified Clock.
     *
     * @param value the JapaneseDate value to compare
     * @param clock the Clock to use for comparison
     * @return 0 if the value is equal to the current time, a negative value if the value is before the current time,
     *         a positive value if the value is after the current time
     */
    public static int compare(JapaneseDate value, Clock clock) {
        return value.compareTo(JapaneseDate.now(clock));
    }

    /**
     * Compares the given MinguoDate value with the current time using the specified Clock.
     *
     * @param value the MinguoDate value to compare
     * @param clock the Clock to use for comparison
     * @return 0 if the value is equal to the current time, a negative value if the value is before the current time,
     *         a positive value if the value is after the current time
     */
    public static int compare(MinguoDate value, Clock clock) {
        return value.compareTo(MinguoDate.now(clock));
    }

    /**
     * Compares the given ThaiBuddhistDate value with the current time using a suitable comparator.
     *
     * @param value the ThaiBuddhistDate value to compare
     * @param clock the clock to use for comparison
     * @return 0 if the value is equal to the current time, a negative value if the value is before the current time,
     *         a positive value if the value is after the current time, or null if a comparator could not be found
     */
    public static int compare(ThaiBuddhistDate value, Clock clock) {
        return value.compareTo(ThaiBuddhistDate.now(clock));
    }

    /**
     * A functional interface for comparing a value with the current time using a specified clock.
     *
     * @param <T> the type of value to compare
     */
    private interface TemporalComparator<T> {
        int compare(T value, Clock clock);
    }
}
