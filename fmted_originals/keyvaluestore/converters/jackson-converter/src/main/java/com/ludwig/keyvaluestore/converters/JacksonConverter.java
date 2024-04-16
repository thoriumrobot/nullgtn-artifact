package com.ludwig.keyvaluestore.converters;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ludwig.keyvaluestore.Converter;
import com.ludwig.keyvaluestore.ConverterException;
import com.ludwig.keyvaluestore.storage.unit.StorageUnit;
import io.reactivex.annotations.Nullable;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.reflect.Type;

/**
 * A {@link Converter} that uses a Jackson {@link ObjectMapper} to get the job done.
 */
public class JacksonConverter implements Converter {

    private final ObjectMapper objectMapper;

    public JacksonConverter() {
        this(new ObjectMapper());
    }

    public JacksonConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> void write(@Nullable T data, Type type, StorageUnit storageUnit) throws ConverterException {
        try {
            OutputStream outputStream = storageUnit.output();
            objectMapper.writeValue(outputStream, data);
            outputStream.close();
        } catch (Exception e) {
            throw new ConverterException(e);
        }
    }

    @Override
    @Nullable
    @SuppressWarnings("TypeParameterUnusedInFormals")
    public <T> T read(StorageUnit storageUnit, Type type) throws ConverterException {
        JavaType javaType = objectMapper.getTypeFactory().constructType(type);
        try {
            Reader reader = storageUnit.reader();
            T value;
            if (!reader.ready()) {
                value = null;
            } else {
                value = objectMapper.readValue(reader, javaType);
            }
            reader.close();
            return value;
        } catch (Exception e) {
            throw new ConverterException(e);
        }
    }
}
