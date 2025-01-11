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
package org.rulii.bind;

import org.rulii.lib.spring.util.Assert;

import java.lang.reflect.Type;

/**
 * Immutable version of a Binding. The value cannot be changed with this Binding.
 *
 * @param <T> Binding Type.
 * @author Max Arulananthan
 * @since 1.0
 */
public final class ImmutableBinding<T> implements Binding<T>  {

    private final Binding<T> target;

    ImmutableBinding(Binding<T> target) {
        super();
        Assert.notNull(target, "target cannot be null.");
        this.target = target;
    }

    private Binding<T> getTarget() {
        return target;
    }

    @Override
    public String getId() {
        return getTarget().getId();
    }

    @Override
    public String getName() {
        return getTarget().getName();
    }

    @Override
    public Type getType() {
        return getTarget().getType();
    }

    @Override
    public T getValue() {
        return getTarget().getValue();
    }

    @Override
    public String getDescription() {
        return getTarget().getDescription();
    }

    @Override
    public String getTypeName() {
        return getTarget().getTypeName();
    }

    @Override
    public String getTypeAndName() {
        return getTarget().getTypeAndName();
    }

    @Override
    public String getSummary() {
        return getTarget().getSummary();
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public boolean isFinal() {
        return getTarget().isFinal();
    }

    @Override
    public String getTextValue() {
        return getTarget().getTextValue();
    }

    @Override
    public boolean isPrimary() {
        return getTarget().isPrimary();
    }

    @Override
    public boolean isTypeAcceptable(Type type) {
        return getTarget().isTypeAcceptable(type);
    }

    @Override
    public boolean isAssignable(Type type) {
        return getTarget().isAssignable(type);
    }

    @Override
    public Binding<T> asImmutable() {
        return this;
    }

    @Override
    public void setValue(T value) {
        throw new IllegalStateException("This Binding [" + getName() + "] is immutable. It cannot be edited in this context.");
    }

    @Override
    public boolean equals(Object o) {
        return getTarget().equals(o);
    }

    @Override
    public int hashCode() {
        return getTarget().hashCode();
    }

    @Override
    public void addValueListener(BindingValueListener listener) {
        // No need to store listeners as value is immutable
    }

    @Override
    public boolean removeValueListener(BindingValueListener listener) {
        return false;
    }

    @Override
    public String toString() {
        return "Name = " + getName() +
                ", Type = " + getTypeName() +
                ", Value = " + getTextValue() +
                ", Primary = " + isPrimary() +
                ", Editable = " + isEditable() +
                ", Description = " + getDescription();
    }
}
