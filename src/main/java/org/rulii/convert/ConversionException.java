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
package org.rulii.convert;

import org.rulii.model.UnrulyException;

import java.lang.reflect.Type;

/**
 * Exception thrown during Conversion process.
 *
 * @author Max Arulananthan.
 * @since 1.0
 */
public class ConversionException extends UnrulyException {

    private final Object value;
    private final Type sourceType;
    private final Type targetType;

    /**
     * Ctor taking all params.
     *
     * @param value Source value.
     * @param sourceType Source Type.
     * @param targetType Target Type.
     */
    public ConversionException(Object value, Type sourceType, Type targetType) {
        super("Unable to convert value [" + value + "] of type [" + sourceType + "] to [" + targetType + "]");
        this.value = value;
        this.sourceType = sourceType;
        this.targetType = targetType;
    }

    /**
     * Ctor taking all params.
     *
     * @param cause cause of the conversion error.
     * @param value Source value.
     * @param sourceType Source Type.
     * @param targetType Target Type.
     */
    public ConversionException(Throwable cause, Object value, Type sourceType, Type targetType) {
        super("Unable to convert value [" + value + "] of type [" + sourceType + "] to [" + targetType + "]", cause);
        this.value = value;
        this.sourceType = sourceType;
        this.targetType = targetType;
    }

    /**
     * Source value.
     *
     * @return source value.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Source Type.
     *
     * @return source type.
     */
    public Type getSourceType() {
        return sourceType;
    }

    /**
     * Target Type.
     *
     * @return target type.
     */
    public Type getTargetType() {
        return targetType;
    }
}
