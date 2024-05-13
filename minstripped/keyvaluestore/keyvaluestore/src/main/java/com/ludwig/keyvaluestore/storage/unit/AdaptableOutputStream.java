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

import static java.nio.charset.StandardCharsets.UTF_8;
import java.io.IOException;
import java.io.OutputStream;

class AdaptableOutputStream extends OutputStream {

    private final Object closeLock = new Object();

    private final String key;

    private final WritableAdapter writableAdapter;

    StringBuilder buffer = new StringBuilder();

    private volatile boolean closed = false;

    public AdaptableOutputStream(String key, WritableAdapter writableAdapter) {
        this.key = key;
        this.writableAdapter = writableAdapter;
    }

    @Override
    public void write(int b) throws IOException {
        write(new byte[] { (byte) b }, 0, 1);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (closed && len > 0) {
            throw new IOException("Stream Closed");
        }
        buffer.append(new String(b, off, len, UTF_8));
    }

    @Override
    public void close() {
        synchronized (closeLock) {
            if (closed) {
                return;
            }
            writableAdapter.write(key, buffer.toString()).blockingAwait();
            closed = true;
            buffer = new StringBuilder();
        }
    }
}
