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

/**
 * Converts a String value to a Enum.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class TextToEnumConverter extends ConverterTemplate<CharSequence, Enum> {

    public TextToEnumConverter() {
        super();
    }

    @Override
    public Enum convert(CharSequence value, Type toType) throws ConversionException {
        if (value == null) return null;

        try {
            return Enum.valueOf((Class) toType, value.toString());
        } catch (IllegalArgumentException e) {
            throw new ConversionException(e, value, getSourceType(), getTargetType());
        }
    }
}
