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

import com.ludwig.keyvaluestore.Converter;
import com.ludwig.keyvaluestore.storage.storable.ValueStorable;
import io.reactivex.*;
import io.reactivex.annotations.Nullable;
import io.reactivex.schedulers.Schedulers;
import java.lang.reflect.Type;

final class ValueTypeV1<T> implements ValueType<T> {

    private final Converter converter;

    private final Type type;

    private ValueStorable storage;

    ValueTypeV1(ValueStorable storage, Converter converter, Type type) {
        this.storage = storage;
        this.converter = converter;
        this.type = type;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Maybe<T> get() {
        return storage.<T>get(converter, type);
    }

    @Override
    @Nullable
    public T blockingGet() {
        return get().blockingGet();
    }

    @Override
    public void put(T value) {
        put(value, Schedulers.io());
    }

    @Override
    public void put(T value, Scheduler scheduler) {
        observePut(value).subscribeOn(scheduler).subscribe();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Single<T> observePut(final T value) {
        return storage.put(converter, type, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Observable<ValueUpdate<T>> observe() {
        return storage.observe(converter, type);
    }

    @Override
    public Completable observeClear() {
        return storage.clear();
    }

    @Override
    public void clear() {
        clear(Schedulers.io());
    }

    @Override
    public void clear(Scheduler scheduler) {
        observeClear().subscribeOn(scheduler).subscribe();
    }
}
