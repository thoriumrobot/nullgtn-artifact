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
import com.ludwig.keyvaluestore.storage.FileStorageAdapter;
import io.reactivex.Single;
import io.reactivex.annotations.Nullable;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FileStorageUnit extends AdaptableStorageUnit {

    protected final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    private Integer readCount;

    private FileStorageAdapter fileStorageAdapter;

    public FileStorageUnit(String key, FileStorageAdapter fileStorageAdapter) {
        super(key, fileStorageAdapter);
        this.fileStorageAdapter = fileStorageAdapter;
    }

    @Override
    public <T> Single<T> converterWrite(T value, Converter converter, Type type) {
        return createTemp().map(tmpFile -> {
            converter.write(value, type, tmpFile);
            return tmpFile;
        }).flatMap(tmpFile -> delete().flatMap(deleted -> {
            if (!deleted) {
                throw new IOException("Failed to write get to store.");
            }
            return set(tmpFile).map(isSet -> {
                if (!isSet) {
                    throw new IOException("Failed to write get to store.");
                }
                return value;
            });
        }));
    }

    @Override
    public void startRead() {
        readWriteLock.readLock().lock();
    }

    @Override
    public void endRead() {
        readWriteLock.readLock().unlock();
    }

    @Override
    public void startWrite() {
        Lock readLock = readWriteLock.readLock();
        readCount = readWriteLock.getWriteHoldCount() == 0 ? readWriteLock.getReadHoldCount() : 0;
        for (int i = 0; i < readCount; i++) {
            readLock.unlock();
        }
        Lock writeLock = readWriteLock.writeLock();
        writeLock.lock();
    }

    @Override
    public void endWrite() {
        Lock readLock = readWriteLock.readLock();
        Lock writeLock = readWriteLock.writeLock();
        for (int i = 0; i < (readCount != null ? readCount : 0); i++) {
            readLock.lock();
        }
        readCount = null;
        writeLock.unlock();
    }

    private Single<FileStorageUnit> createTemp() {
        return Single.fromCallable(() -> new FileStorageUnit(key + ".tmp", fileStorageAdapter));
    }

    private Single<Boolean> set(FileStorageUnit storageUnit) {
        return Single.fromCallable(() -> fileStorageAdapter.file(storageUnit.key).renameTo(fileStorageAdapter.file(key)));
    }
}
