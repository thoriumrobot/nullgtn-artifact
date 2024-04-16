package com.ludwig.keyvaluestore.storage;

import com.ludwig.keyvaluestore.storage.unit.StorageUnit;
import io.reactivex.Single;
import java.io.*;

public interface StorageAdapter {

    Single<Boolean> exists(String key);

    Single<Boolean> createNew(String key);

    Single<Boolean> delete(String key);

    Reader reader(String key) throws IOException;

    InputStream input(String key) throws IOException;

    Writer writer(String key) throws IOException;

    OutputStream output(String key) throws IOException;

    StorageUnit storageUnit(String key);
}
