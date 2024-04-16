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
import com.ludwig.keyvaluestore.types.ValueUpdate;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.lang.reflect.Type;

public interface ValueStorable {

    <T> Maybe<T> get(Converter converter, Type type);

    <T> Single<T> put(Converter converter, Type type, T value);

    <T> Observable<ValueUpdate<T>> observe(Converter converter, Type type);

    <T> Completable clear();
}
