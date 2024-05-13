package com.ludwig.keyvaluestore.storage.storable;

import com.ludwig.keyvaluestore.storage.unit.StorageUnit;

public final class StorableFactory {

    public static ListStorable list(StorageUnit storageUnit) {
        return new ListStorableV1(storageUnit);
    }

    public static ValueStorable value(StorageUnit storageUnit) {
        return new ValueStorableV1(storageUnit);
    }
}
