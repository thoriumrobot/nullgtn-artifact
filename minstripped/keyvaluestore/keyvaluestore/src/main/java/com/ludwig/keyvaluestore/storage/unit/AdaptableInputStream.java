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
import java.io.IOException;
import java.io.InputStream;

class AdaptableInputStream extends InputStream {

    private final String key;

    private final ReadableAdapter readableAdapter;

    private volatile String buffer;

    private volatile boolean closed = false;

    public AdaptableInputStream(String key, ReadableAdapter readableAdapter) {
        this.key = key;
        this.readableAdapter = readableAdapter;
    }

    @Override
    public int read() throws IOException {
        byte[] b = new byte[1];
        return (read(b, 0, 1) != -1) ? b[0] & 0xff : -1;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (closed && len > 0) {
            throw new IOException("Stream Closed");
        }
        if (buffer == null) {
            buffer = readableAdapter.read(key).blockingGet();
        }
        int read = 0;
        for (int i = off; i < buffer.length() && read < len; i++, read++) {
            b[i] = (byte) buffer.charAt(i);
        }
        return read;
    }

    @Override
    public long skip(long n) throws IOException {
        if (closed) {
            throw new IOException("Stream Closed");
        }
        buffer = null;
        return 0;
    }
}
