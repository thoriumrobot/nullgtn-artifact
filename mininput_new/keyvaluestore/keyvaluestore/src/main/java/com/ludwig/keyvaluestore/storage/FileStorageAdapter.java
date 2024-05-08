package com.ludwig.keyvaluestore.storage;

import com.ludwig.keyvaluestore.storage.unit.FileStorageUnit;
import io.reactivex.Single;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;

public class FileStorageAdapter implements StorageAdapter {

    private String basePath;

    public FileStorageAdapter(String basePath) {
        this.basePath = basePath;
    }

    @Override
    public Single<Boolean> exists(String key) {
        return Single.fromCallable(() -> file(key).exists());
    }

    @Override
    public Single<Boolean> createNew(String key) {
        return Single.fromCallable(() -> file(key).createNewFile());
    }

    @Override
    public Single<Boolean> delete(String key) {
        return Single.fromCallable(() -> file(key).delete());
    }

    @Override
    public Reader reader(String key) throws IOException {
        return Files.newBufferedReader(file(key).toPath(), Charset.defaultCharset());
    }

    @Override
    public InputStream input(String key) throws IOException {
        return new FileInputStream(file(key));
    }

    @Override
    public Writer writer(String key) throws IOException {
        return Files.newBufferedWriter(file(key).toPath(), Charset.defaultCharset());
    }

    @Override
    public OutputStream output(String key) throws IOException {
        return new FileOutputStream((file(key)));
    }

    @Override
    public FileStorageUnit storageUnit(String key) {
        return new FileStorageUnit(key, this);
    }

    public File file(String key) {
        return new File(path(key));
    }

    private String path(String key) {
        return basePath + "/" + key + ".json";
    }
}
