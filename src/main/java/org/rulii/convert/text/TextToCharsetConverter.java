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
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

/**
 * Text to CharSet converter.
 *
 * @author Max Arulananthan
 * @since  1.0
 */
public class TextToCharsetConverter extends ConverterTemplate<CharSequence, Charset> {

    public TextToCharsetConverter() {
        super();
    }

    @Override
    public Charset convert(CharSequence value, Type toType) throws ConversionException {
        if (value == null) return null;
        try {
            return Charset.forName(value.toString());
        } catch (UnsupportedCharsetException e) {
            throw new ConversionException(e, value, CharSequence.class, toType);
        }
    }
}
