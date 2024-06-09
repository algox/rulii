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
package org.rulii.convert.text;

import org.rulii.convert.ConversionException;
import org.rulii.convert.ConverterTemplate;

import java.lang.reflect.Type;
import java.math.BigDecimal;

/**
 * Converts a String value to a BigDecimal.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class TextToBigDecimalConverter extends ConverterTemplate<CharSequence, BigDecimal> {

    public TextToBigDecimalConverter() {
        super();
    }

    @Override
    public BigDecimal convert(CharSequence value, Type toType) {
        if (value == null) return null;

        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            throw new ConversionException(e, value, getSourceType(), getTargetType());
        }
    }
}
