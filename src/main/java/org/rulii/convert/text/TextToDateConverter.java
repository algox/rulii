/*
 * This software is licensed under the Apache 2 license, quoted below.
 *
 * Copyright (c) 1999-2025, Algorithmx Inc.
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
package org.rulii.convert.text;

import org.rulii.convert.ConversionException;
import org.rulii.convert.ConverterTemplate;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * TextToDateConverter is a Converter class that converts a CharSequence representing a date/time string into a Date object.
 * It supports conversion from the following formats:
 * - Date only: "yyyy-MM-dd"
 * - Date and Time: "yyyy-MM-dd'T'HH:mm:ss"
 * - Date, Time, and Time Zone: "yyyy-MM-dd'T'HH:mm:ssZ"
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class TextToDateConverter extends ConverterTemplate<CharSequence, Date> {

    public static final String DATE_ONLY_FORMAT        = "yyyy-MM-dd";
    public static final String DATE_TIME_FORMAT        = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String DATE_TIME_ZONE_FORMAT   = "yyyy-MM-dd'T'HH:mm:ssZ";

    public TextToDateConverter() {
        super();
    }

    @Override
    public Date convert(CharSequence text, Type toType) throws ConversionException {
        if (text == null) return null;

        String value = text.toString();
        try {
            int timeIndex = value.indexOf('T');

            if (timeIndex > 0) {
                int zoneIndex = value.indexOf('-', timeIndex);
                return zoneIndex > timeIndex ? parseDateTimeZone(value) : parseDateTime(value);
            } else {
                return parseDate(value);
            }
        } catch (Exception e) {
            throw new ConversionException(e, value, getSourceType(), getTargetType());
        }
    }

    private Date parseDate(String value) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_ONLY_FORMAT);
        return dateFormat.parse(value);
    }

    private Date parseDateTime(String value) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_TIME_FORMAT);
        return dateFormat.parse(value);
    }

    private Date parseDateTimeZone(String value) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_TIME_ZONE_FORMAT);
        return dateFormat.parse(value);
    }
}
