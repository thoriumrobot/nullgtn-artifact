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
package com.ludwig.keyvaluestore.storage;

import com.ludwig.keyvaluestore.storage.storable.ListStorable;
import com.ludwig.keyvaluestore.storage.storable.StorableFactory;
import com.ludwig.keyvaluestore.storage.storable.ValueStorable;

public class AdaptableStorage implements Storage {

    private StorageAdapter storageAdapter;

    public AdaptableStorage(StorageAdapter storageAdapter) {
        this.storageAdapter = storageAdapter;
    }

    @Override
    public ValueStorable value(String key) {
        return StorableFactory.value(storageAdapter.storageUnit(key));
    }

    @Override
    public ListStorable list(String key) {
        return StorableFactory.list(storageAdapter.storageUnit(key));
    }
}
