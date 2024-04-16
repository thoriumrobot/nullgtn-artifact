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
package com.ludwig.keyvaluestore.storage.storable;

import com.ludwig.keyvaluestore.Converter;
import com.ludwig.keyvaluestore.storage.unit.StorageUnit;
import com.ludwig.keyvaluestore.types.ValueUpdate;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.PublishSubject;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Optional;

public class ValueStorableV1 implements ValueStorable {

    protected final PublishSubject updateSubject = PublishSubject.create();

    private StorageUnit storageUnit;

    ValueStorableV1(StorageUnit storageUnit) {
        this.storageUnit = storageUnit;
    }

    @Override
    public <T> Maybe<T> get(Converter converter, Type type) {
        return Completable.fromAction(storageUnit::startRead).andThen(storageUnit.exists()).filter(Boolean::booleanValue).map(exists -> Optional.ofNullable(converter.<T>read(storageUnit, type))).filter(Optional::isPresent).map(Optional::get).doFinally(storageUnit::endRead);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Single<T> put(Converter converter, Type type, T value) {
        return Completable.fromAction(storageUnit::startWrite).andThen(storageUnit.exists()).flatMap(exists -> exists ? Single.just(true) : storageUnit.createNew()).flatMap(createSuccess -> {
            if (!createSuccess) {
                throw new IOException("Could not create store.");
            }
            return storageUnit.converterWrite(value, converter, type);
        }).doOnSuccess(o -> updateSubject.onNext(new ValueUpdate<>(value))).doFinally(storageUnit::endWrite);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Observable<ValueUpdate<T>> observe(Converter converter, Type type) {
        return updateSubject.startWith(get(converter, type).map(value -> new ValueUpdate<>((T) value)).defaultIfEmpty(ValueUpdate.empty()).toObservable()).hide();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Completable clear() {
        return Completable.fromAction(storageUnit::startWrite).andThen(storageUnit.exists()).filter(Boolean::booleanValue).flatMapSingle(exists -> storageUnit.delete()).doOnSuccess(deleteSuccess -> {
            if (!deleteSuccess) {
                throw new IOException("Clear operation on store failed.");
            }
            updateSubject.onNext(ValueUpdate.<T>empty());
        }).doFinally(storageUnit::endWrite).ignoreElement();
    }
}
