package com.ludwig.keyvaluestore.converters;

import com.google.gson.Gson;
import com.ludwig.keyvaluestore.Converter;
import com.ludwig.keyvaluestore.ConverterException;
import com.ludwig.keyvaluestore.storage.unit.StorageUnit;
import io.reactivex.annotations.Nullable;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;

public class GsonConverter implements Converter {

    private Gson gson;

    public GsonConverter() {
        this(new Gson());
    }

    public GsonConverter(Gson gson) {
        this.gson = gson;
    }

    @Override
    public <T> void write(@Nullable T data, Type type, StorageUnit storageUnit) throws ConverterException {
        try {
            Writer writer = storageUnit.writer();
            gson.toJson(data, type, writer);
            writer.close();
        } catch (Exception e) {
            throw new ConverterException(e);
        }
    }

    @Override
    @SuppressWarnings({ "unchecked", "TypeParameterUnusedInFormals" })
    @Nullable
    public <T> T read(StorageUnit storageUnit, Type type) throws ConverterException {
        try {
            Reader reader = storageUnit.reader();
            T value = gson.fromJson(reader, type);
            reader.close();
            return value;
        } catch (Exception e) {
            throw new ConverterException(e);
        }
    }
}
