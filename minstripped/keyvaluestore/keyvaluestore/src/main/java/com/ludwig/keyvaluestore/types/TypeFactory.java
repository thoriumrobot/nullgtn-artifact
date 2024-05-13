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
import com.ludwig.keyvaluestore.storage.storable.ListStorable;
import com.ludwig.keyvaluestore.storage.storable.ValueStorable;
import java.lang.reflect.Type;

public final class TypeFactory {

    public static <T> ListType<T> build(ListStorable storage, Converter converter, Type type) {
        return new ListTypeV1<T>(storage, converter, type);
    }

    public static <T> ValueType<T> build(ValueStorable storage, Converter converter, Type type) {
        return new ValueTypeV1<T>(storage, converter, type);
    }
}
