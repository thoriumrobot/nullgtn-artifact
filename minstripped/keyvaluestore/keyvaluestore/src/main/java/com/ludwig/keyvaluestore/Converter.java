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

import com.ludwig.keyvaluestore.storage.unit.StorageUnit;
import io.reactivex.annotations.Nullable;
import java.lang.reflect.Type;

/**
 * Convert objects to and from serializable formats and get/write them from reader/writer.
 */
public interface Converter {

    /**
     * Convert data into a serializable format and write to writer.
     */
    <T> void write(@Nullable T data, Type type, StorageUnit storageUnit) throws ConverterException;

    /**
     * Pull typed data out of reader.
     */
    @SuppressWarnings("TypeParameterUnusedInFormals")
    <T> T read(StorageUnit storageUnit, Type type) throws ConverterException;
}
