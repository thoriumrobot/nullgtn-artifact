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
package com.example.keyvaluestore;

import com.ludwig.keyvaluestore.KeyValueStore;
import com.ludwig.keyvaluestore.KeyValueStoreFactory;
import com.ludwig.keyvaluestore.converters.MoshiConverter;
import com.ludwig.keyvaluestore.storage.FileStorage;
import com.ludwig.keyvaluestore.storage.FileStorageAdapter;
import com.ludwig.keyvaluestore.types.ListType;
import com.ludwig.keyvaluestore.types.ValueType;
import com.ludwig.keyvaluestore.types.ValueUpdate;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import java.util.ArrayList;
import java.util.List;

public class KeyValueStoreExample {

    @SuppressWarnings({ "CheckReturnValue", "CatchAndPrintStackTrace" })
    public static void main(String[] args) {
        KeyValueStore store = KeyValueStoreFactory.build(new FileStorage(new FileStorageAdapter("/tmp")), new MoshiConverter());
        ValueType<String> valueStore = store.value("value", String.class);
        ListType<String> listStore = store.list("list", String.class);
        valueStore.observe().skip(1).subscribe(new Observer<ValueUpdate<String>>() {

            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(ValueUpdate<String> stringValueUpdate) {
                System.out.println("value update: " + stringValueUpdate.value);
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        });
        listStore.observe().skip(1).subscribe(new Observer<List<String>>() {

            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(List<String> strings) {
                System.out.println("list update: " + strings);
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        });
        valueStore.observePut("value1").blockingGet();
        valueStore.observePut("value2").blockingGet();
        valueStore.observePut("value1").blockingGet();
        valueStore.observePut("value2").blockingGet();
        listStore.observeClear().ignoreElement().blockingAwait();
        int threadCount = 25;
        List<Thread> threads = new ArrayList<>(threadCount * 2);
        int elementsPerThread = 20;
        for (int j = 0; j < threadCount; j++) {
            final int _j = j;
            Runnable runnable = () -> {
                for (int i = _j * elementsPerThread; i < (_j + 1) * elementsPerThread; i++) {
                    listStore.observeAdd("lv" + _j + ": " + i).ignoreElement().blockingAwait();
                    try {
                        Thread.sleep((int) (Math.random() * 20));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            threads.add(new Thread(runnable));
        }
        for (int j = 0; j < threadCount; j++) {
            final int _j = j;
            Runnable runnable = () -> {
                for (int i = _j * elementsPerThread; i < (_j + 1) * elementsPerThread; i++) {
                    final int _i = i;
                    listStore.observeRemove(value -> value.equals("lv" + _j + ": " + _i)).ignoreElement().blockingAwait();
                    try {
                        Thread.sleep((int) (Math.random() * 20));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            threads.add(new Thread(runnable));
        }
        for (Thread t : threads) {
            t.start();
        }
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
