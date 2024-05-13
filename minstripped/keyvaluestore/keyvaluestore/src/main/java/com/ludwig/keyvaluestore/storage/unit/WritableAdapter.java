package com.ludwig.keyvaluestore.storage.unit;

import io.reactivex.Completable;

public interface WritableAdapter {

    Completable write(String key, String value);
}
