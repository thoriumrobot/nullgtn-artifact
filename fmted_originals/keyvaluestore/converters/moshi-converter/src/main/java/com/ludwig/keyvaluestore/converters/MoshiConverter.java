package com.ludwig.keyvaluestore.converters;

import com.ludwig.keyvaluestore.Converter;
import com.ludwig.keyvaluestore.ConverterException;
import com.ludwig.keyvaluestore.storage.unit.StorageUnit;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import io.reactivex.annotations.Nullable;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

public class MoshiConverter implements Converter {

    private final Moshi moshi;

    public MoshiConverter() {
        this(new Moshi.Builder().build());
    }

    public MoshiConverter(Moshi moshi) {
        this.moshi = moshi;
    }

    @Override
    public <T> void write(@Nullable T data, Type type, StorageUnit storageUnit) throws ConverterException {
        try {
            OutputStream outputStream = storageUnit.output();
            JsonAdapter<T> adapter = moshi.adapter(type);
            BufferedSink sink = Okio.buffer(Okio.sink(outputStream));
            adapter.toJson(sink, data);
            sink.close();
            outputStream.close();
        } catch (Exception e) {
            throw new ConverterException(e);
        }
    }

    @Override
    @Nullable
    @SuppressWarnings("TypeParameterUnusedInFormals")
    public <T> T read(StorageUnit storageUnit, Type type) {
        try {
            InputStream inputStream = storageUnit.input();
            JsonAdapter<T> adapter = moshi.adapter(type);
            BufferedSource source = Okio.buffer(Okio.source(inputStream));
            T value;
            if (source.exhausted()) {
                value = null;
            } else {
                value = adapter.nullSafe().fromJson(source);
            }
            source.close();
            inputStream.close();
            return value;
        } catch (Exception e) {
            throw new ConverterException(e);
        }
    }
}
