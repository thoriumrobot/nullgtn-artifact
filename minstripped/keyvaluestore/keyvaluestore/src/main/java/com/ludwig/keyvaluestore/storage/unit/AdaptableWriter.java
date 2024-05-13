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

import java.io.Writer;

class AdaptableWriter extends Writer {

    private final Object closeLock = new Object();

    private volatile boolean closed = false;

    private StringBuilder buffer = new StringBuilder();

    private String key;

    private WritableAdapter writableAdapter;

    public AdaptableWriter(String key, WritableAdapter writableAdapter) {
        this.key = key;
        this.writableAdapter = writableAdapter;
    }

    @Override
    public void write(char[] b, int off, int len) {
        buffer.append(new String(b, off, len));
    }

    @Override
    public void flush() {
        writableAdapter.write(key, buffer.toString()).blockingAwait();
    }

    @Override
    public void close() {
        synchronized (closeLock) {
            if (closed) {
                return;
            }
            closed = true;
            buffer = new StringBuilder();
        }
    }
}
