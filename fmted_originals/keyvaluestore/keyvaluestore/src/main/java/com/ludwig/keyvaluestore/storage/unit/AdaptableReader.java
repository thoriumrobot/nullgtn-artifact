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

import io.reactivex.annotations.Nullable;
import java.io.Reader;

class AdaptableReader extends Reader {

    private final Object closeLock = new Object();

    @Nullable
    private volatile String buffer;

    private String key;

    private ReadableAdapter readableAdapter;

    public AdaptableReader(String key, ReadableAdapter readableAdapter) {
        this.key = key;
        this.readableAdapter = readableAdapter;
    }

    @Override
    public int read(char[] b, int off, int len) {
        if (buffer == null) {
            buffer = readableAdapter.read(key).blockingGet();
        }
        int read = 0;
        for (int i = off; i < buffer.length() && read < len; i++, read++) {
            b[i] = buffer.charAt(i);
        }
        return read;
    }

    @Override
    public void close() {
        synchronized (closeLock) {
            if (buffer == null) {
                return;
            }
            buffer = null;
        }
    }
}
