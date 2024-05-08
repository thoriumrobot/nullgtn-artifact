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

import io.reactivex.*;
import io.reactivex.annotations.Nullable;
import io.reactivex.schedulers.Schedulers;

/**
 * Store a single object.
 */
public interface ValueType<T> {

    /**
     * Retrieve the current value from this store using Rx. If this store has not had a value written
     * then the returned {@link Maybe} completes without a value.
     */
    Maybe<T> get();

    /**
     * Retrieve the current value from this store in a blocking manner. This may take time. If the
     * store has not yet had a value written then this method returns null.
     */
    @Nullable
    T blockingGet();

    /**
     * Write an object to this store and observe the operation. The value returned in the {@link
     * Single} is the value written to this store, making it useful for chaining.
     */
    Single<T> observePut(final T value);

    /**
     * Asynchronously write a value to this store. The write operation occurs on {@link
     * Schedulers#io()}. If you wish to specify the {@link Scheduler} then use {@link #put(Object,
     * Scheduler)}.
     */
    void put(T value);

    /**
     * Write a value to this store on a specified {@link Scheduler}.
     */
    void put(T value, Scheduler scheduler);

    /**
     * Observe changes to the value in this store. {@code onNext(valueUpdate)} will be invoked
     * immediately with the current value upon subscription and subsequent changes thereafter.
     *
     * <p>Values are wrapped inside of {@link ValueUpdate ValueUpdate} objects, as it's possible for a
     * store to hold no current value. If that's the case {@link ValueUpdate#empty update.empty} will
     * be true and {@link ValueUpdate#value update.value} will be null.
     */
    Observable<ValueUpdate<T>> observe();

    /**
     * Clear the value in this store and observe the operation. (Useful for chaining).
     */
    Completable observeClear();

    /**
     * Asynchronously clear the value in this store. The clear operation occurs on {@link
     * Schedulers#io()}. If you wish to specify the {@link Scheduler} then use {@link
     * #clear(Scheduler)}.
     */
    void clear();

    /**
     * Clear the value from this store on a specified {@link Scheduler}.
     */
    void clear(Scheduler scheduler);
}
