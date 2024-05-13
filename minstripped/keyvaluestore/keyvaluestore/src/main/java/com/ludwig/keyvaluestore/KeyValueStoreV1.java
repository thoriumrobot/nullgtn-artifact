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
package com.ludwig.keyvaluestore;

import com.ludwig.keyvaluestore.storage.Storage;
import com.ludwig.keyvaluestore.types.ListType;
import com.ludwig.keyvaluestore.types.TypeFactory;
import com.ludwig.keyvaluestore.types.ValueType;
import java.lang.reflect.Type;

class KeyValueStoreV1 implements KeyValueStore {

    private final Storage storage;

    private final Converter converter;

    KeyValueStoreV1(Storage storage, Converter converter) {
        this.storage = storage;
        this.converter = converter;
    }

    @Override
    public <T> ValueType<T> value(String key, Type type) {
        return TypeFactory.build(storage.value(key), converter, type);
    }

    @Override
    public <T> ListType<T> list(String key, Type type) {
        return TypeFactory.build(storage.list(key), converter, type);
    }
}
