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

import com.ludwig.keyvaluestore.types.ListType;
import com.ludwig.keyvaluestore.types.ValueType;
import java.lang.reflect.Type;

public interface KeyValueStore {

    <T> ListType<T> list(String key, Type type);

    <T> ValueType<T> value(String key, Type type);
}
