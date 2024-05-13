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
package com.ludwig.keyvaluestore.storage.unit;

import com.ludwig.keyvaluestore.Converter;
import com.ludwig.keyvaluestore.storage.StorageAdapter;
import io.reactivex.Single;
import java.io.*;
import java.lang.reflect.Type;

public class AdaptableStorageUnit implements StorageUnit {

    protected final String key;

    private final StorageAdapter storageAdapter;

    public AdaptableStorageUnit(String key, StorageAdapter storageAdapter) {
        this.key = key;
        this.storageAdapter = storageAdapter;
    }

    @Override
    public Reader reader() throws IOException {
        return storageAdapter.reader(key);
    }

    @Override
    public InputStream input() throws IOException {
        return storageAdapter.input(key);
    }

    @Override
    public Writer writer() throws IOException {
        return storageAdapter.writer(key);
    }

    @Override
    public OutputStream output() throws IOException {
        return storageAdapter.output(key);
    }

    @Override
    public Single<Boolean> exists() {
        return storageAdapter.exists(key);
    }

    @Override
    public Single<Boolean> createNew() {
        return storageAdapter.createNew(key);
    }

    @Override
    public Single<Boolean> delete() {
        return storageAdapter.delete(key);
    }

    @Override
    public <T> Single<T> converterWrite(T value, Converter converter, Type type) {
        return Single.fromCallable(() -> {
            converter.write(value, type, AdaptableStorageUnit.this);
            return value;
        });
    }

    @Override
    public void startRead() {
    }

    @Override
    public void endRead() {
    }

    @Override
    public void startWrite() {
    }

    @Override
    public void endWrite() {
    }
}
