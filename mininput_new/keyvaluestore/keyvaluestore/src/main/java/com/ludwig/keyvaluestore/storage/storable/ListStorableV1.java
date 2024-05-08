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
import com.ludwig.keyvaluestore.types.ListType;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.PublishSubject;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class ListStorableV1 implements ListStorable {

    private final PublishSubject updateSubject = PublishSubject.create();

    private final StorageUnit storageUnit;

    ListStorableV1(StorageUnit storageUnit) {
        this.storageUnit = storageUnit;
    }

    @Override
    public <T> Single<List<T>> get(Converter converter, Type type) {
        return Completable.fromAction(storageUnit::startRead).andThen(storageUnit.exists()).filter(Boolean::booleanValue).map(exists -> Optional.ofNullable(converter.<List<T>>read(storageUnit, type))).filter(Optional::isPresent).map(Optional::get).toSingle(Collections.emptyList()).doFinally(storageUnit::endRead);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Single<List<T>> put(Converter converter, Type type, List<T> list) {
        return Completable.fromAction(storageUnit::startWrite).andThen(storageUnit.exists()).flatMap(exists -> exists ? Single.just(true) : storageUnit.createNew()).flatMap(exists -> {
            if (!exists) {
                throw new IOException("Could not create store.");
            }
            return storageUnit.converterWrite(list, converter, type);
        }).doOnSuccess(updateSubject::onNext).doFinally(storageUnit::endWrite);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Observable<List<T>> observe(Converter converter, Type type) {
        return updateSubject.startWith(get(converter, type).toObservable()).hide();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Single<List<T>> clear() {
        return Completable.fromAction(storageUnit::startWrite).andThen(storageUnit.exists()).flatMap(exists -> exists ? storageUnit.delete() : Single.just(true)).map(success -> {
            if (!success) {
                throw new IOException("Clear operation on store failed.");
            }
            return Collections.<T>emptyList();
        }).doOnSuccess(o -> updateSubject.onNext(Collections.<T>emptyList())).doFinally(storageUnit::endWrite);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Single<List<T>> append(T value, Converter converter, Type type) {
        return Completable.fromAction(storageUnit::startWrite).andThen(storageUnit.exists()).flatMap(exists -> exists ? Single.just(true) : storageUnit.createNew()).map(success -> {
            if (!success) {
                throw new IOException("Could not create store.");
            }
            return Optional.ofNullable(converter.<List<T>>read(storageUnit, type));
        }).filter(Optional::isPresent).map(Optional::get).toSingle(Collections.emptyList()).flatMap(originalList -> {
            List<T> result = new ArrayList<>(originalList.size() + 1);
            result.addAll(originalList);
            result.add(value);
            return storageUnit.converterWrite(result, converter, type);
        }).doOnSuccess(updateSubject::onNext).doFinally(storageUnit::endWrite);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Single<List<T>> replace(T value, ListType.PredicateFunc<T> predicateFunc, Converter converter, Type type) {
        return Completable.fromAction(storageUnit::startWrite).andThen(storageUnit.exists()).filter(Boolean::booleanValue).flatMap(exists -> {
            List<T> originalList = converter.read(storageUnit, type);
            if (originalList == null)
                originalList = Collections.emptyList();
            int indexOfItemToReplace = -1;
            for (int i = 0; i < originalList.size(); i++) {
                if (predicateFunc.test(originalList.get(i))) {
                    indexOfItemToReplace = i;
                    break;
                }
            }
            if (indexOfItemToReplace != -1) {
                List<T> modifiedList = new ArrayList<T>(originalList);
                modifiedList.remove(indexOfItemToReplace);
                modifiedList.add(indexOfItemToReplace, value);
                return storageUnit.converterWrite(modifiedList, converter, type).toMaybe();
            }
            return Maybe.just(originalList);
        }).toSingle(Collections.<T>emptyList()).doOnSuccess(updateSubject::onNext).doFinally(storageUnit::endWrite);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Single<List<T>> addOrReplace(T value, ListType.PredicateFunc<T> predicateFunc, Converter converter, Type type) {
        return Completable.fromAction(storageUnit::startWrite).andThen(storageUnit.exists()).flatMap(exists -> exists ? Single.just(true) : storageUnit.createNew()).flatMap(createSuccess -> {
            if (!createSuccess) {
                throw new IOException("Could not create store.");
            }
            List<T> originalList = converter.read(storageUnit, type);
            if (originalList == null)
                originalList = Collections.emptyList();
            int indexOfItemToReplace = -1;
            for (int i = 0; i < originalList.size(); i++) {
                if (predicateFunc.test(originalList.get(i))) {
                    indexOfItemToReplace = i;
                    break;
                }
            }
            int modifiedListSize = indexOfItemToReplace == -1 ? originalList.size() + 1 : originalList.size();
            List<T> modifiedList = new ArrayList<T>(modifiedListSize);
            modifiedList.addAll(originalList);
            if (indexOfItemToReplace == -1) {
                modifiedList.add(value);
            } else {
                modifiedList.remove(indexOfItemToReplace);
                modifiedList.add(indexOfItemToReplace, value);
            }
            return storageUnit.converterWrite(modifiedList, converter, type);
        }).doOnSuccess(updateSubject::onNext).doFinally(storageUnit::endWrite);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Single<List<T>> remove(final ListType.PredicateFunc<T> predicateFunc, Converter converter, Type type) {
        return Completable.fromAction(storageUnit::startWrite).andThen(storageUnit.exists()).filter(Boolean::booleanValue).flatMap(exists -> {
            List<T> originalList = converter.read(storageUnit, type);
            if (originalList == null)
                originalList = Collections.emptyList();
            List<T> modifiedList = new ArrayList<T>(originalList);
            boolean removed = false;
            final Iterator<T> each = modifiedList.iterator();
            while (each.hasNext()) {
                if (predicateFunc.test(each.next())) {
                    each.remove();
                    removed = true;
                    break;
                }
            }
            if (removed) {
                return storageUnit.converterWrite(modifiedList, converter, type).toMaybe();
            }
            return Maybe.just(modifiedList);
        }).toSingle(Collections.emptyList()).doOnSuccess(updateSubject::onNext).doFinally(storageUnit::endWrite);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Single<List<T>> removeAll(final ListType.PredicateFunc<T> predicateFunc, Converter converter, Type type) {
        return Completable.fromAction(storageUnit::startWrite).andThen(storageUnit.exists()).filter(Boolean::booleanValue).flatMap(exists -> {
            List<T> originalList = converter.read(storageUnit, type);
            if (originalList == null)
                originalList = Collections.emptyList();
            List<T> modifiedList = new ArrayList<T>(originalList);
            boolean removed = false;
            final Iterator<T> each = modifiedList.iterator();
            while (each.hasNext()) {
                if (predicateFunc.test(each.next())) {
                    each.remove();
                    removed = true;
                }
            }
            if (removed) {
                return storageUnit.converterWrite(modifiedList, converter, type).toMaybe();
            }
            return Maybe.just(modifiedList);
        }).toSingle(Collections.emptyList()).doOnSuccess(updateSubject::onNext).doFinally(storageUnit::endWrite);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Single<List<T>> remove(int position, Converter converter, Type type) {
        return Completable.fromAction(storageUnit::startWrite).andThen(storageUnit.exists()).filter(Boolean::booleanValue).flatMap(exists -> {
            List<T> originalList = converter.read(storageUnit, type);
            if (originalList == null)
                originalList = Collections.emptyList();
            List<T> modifiedList = new ArrayList<T>(originalList);
            modifiedList.remove(position);
            return storageUnit.converterWrite(modifiedList, converter, type).toMaybe();
        }).toSingle(Collections.emptyList()).doOnSuccess(updateSubject::onNext).doFinally(storageUnit::endWrite);
    }
}
