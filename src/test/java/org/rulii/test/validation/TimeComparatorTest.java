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
package org.rulii.test.validation;

import org.rulii.util.TimeComparator;
import org.junit.Assert;
import org.junit.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;

public class TimeComparatorTest {

    public TimeComparatorTest() {
        super();
    }

    @Test
    public void test1() throws ParseException {
        Clock clock = Clock.systemDefaultZone();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date pastDate = format.parse("1980-01-01");
        Date futureDate = format.parse("2180-01-01");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(pastDate);
        Clock fixedPastClock = Clock.fixed(Instant.parse("1980-01-01T12:00:00.00Z"), ZoneId.of("America/Chicago"));
        Clock fixedFutureClock = Clock.fixed(Instant.parse("2180-01-01T12:00:00.00Z"), ZoneId.of("America/Chicago"));

        Assert.assertTrue(TimeComparator.compare(pastDate, clock) < 0);
        Assert.assertTrue(TimeComparator.compare(futureDate, clock) > 0);
        Assert.assertTrue(TimeComparator.compare(calendar, clock) < 0);
        Assert.assertTrue(TimeComparator.compare(fixedPastClock.instant(), clock) < 0);
        Assert.assertTrue(TimeComparator.compare(fixedFutureClock.instant(), clock) > 0);
        Assert.assertTrue(TimeComparator.compare(LocalDateTime.now(fixedPastClock), clock) < 0);
        Assert.assertTrue(TimeComparator.compare(LocalDateTime.now(fixedFutureClock), clock) > 0);
        Assert.assertTrue(TimeComparator.compare(LocalDate.now(fixedPastClock), clock) < 0);
        Assert.assertTrue(TimeComparator.compare(LocalDate.now(fixedFutureClock), clock) > 0);
        Assert.assertTrue(TimeComparator.compare(OffsetDateTime.now(fixedPastClock), clock) < 0);
        Assert.assertTrue(TimeComparator.compare(OffsetDateTime.now(fixedFutureClock), clock) > 0);
        Assert.assertTrue(TimeComparator.compare(YearMonth.now(fixedPastClock), clock) < 0);
        Assert.assertTrue(TimeComparator.compare(YearMonth.now(fixedFutureClock), clock) > 0);
        Assert.assertTrue(TimeComparator.compare(ZonedDateTime.now(fixedPastClock), clock) < 0);
        Assert.assertTrue(TimeComparator.compare(ZonedDateTime.now(fixedFutureClock), clock) > 0);
    }
}
