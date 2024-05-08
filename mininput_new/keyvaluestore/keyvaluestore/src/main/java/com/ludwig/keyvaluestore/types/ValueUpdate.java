/*
 * Copyright (C) 2018 Ludwig
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ludwig.keyvaluestore.types;

import io.reactivex.annotations.Nullable;

/**
 * Wraps the current value in a {@link ValueType}. This is useful as {@link ValueType#observe()} is
 * unable to deliver null objects in {@code onNext()} to represent an empty state. To that end, you
 * may query {@link ValueUpdate#empty update.empty} to determine if the current value is empty and
 * whether or not {@link ValueUpdate#value update.value} is null.
 */
public final class ValueUpdate<T> {

    // Empty instance needs no type as value is always null.
    @SuppressWarnings("unchecked")
    private static final ValueUpdate EMPTY = new ValueUpdate(null);

    @Nullable
    public final T value;

    public final boolean empty;

    public ValueUpdate(@Nullable T value) {
        this.value = value;
        this.empty = value == null;
    }

    @SuppressWarnings("unchecked")
    public static <T> ValueUpdate<T> empty() {
        return (ValueUpdate<T>) EMPTY;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof ValueUpdate))
            return false;
        ValueUpdate other = (ValueUpdate) obj;
        if (this.empty)
            return other.empty;
        return this.value == other.value || (this.value != null && other.value != null && this.value.equals(other.value));
    }

    @Override
    public int hashCode() {
        int valueHash = value == null ? 0 : value.hashCode();
        int emptyHash = empty ? 1 : 0;
        return 37 * (valueHash + emptyHash);
    }
}
