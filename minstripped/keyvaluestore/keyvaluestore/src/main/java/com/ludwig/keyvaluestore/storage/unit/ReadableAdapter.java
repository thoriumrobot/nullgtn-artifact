package com.ludwig.keyvaluestore.storage.unit;

import io.reactivex.Single;

public interface ReadableAdapter {

    Single<String> read(String key);
}
