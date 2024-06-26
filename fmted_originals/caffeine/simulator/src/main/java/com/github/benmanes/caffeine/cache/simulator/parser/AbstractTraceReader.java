/*
 * Copyright 2016 Ben Manes. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.benmanes.caffeine.cache.simulator.parser;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.tukaani.xz.XZInputStream;

/**
 * A skeletal implementation that reads the trace files into a data stream.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
public abstract class AbstractTraceReader implements TraceReader {

    private static final int BUFFER_SIZE = 1 << 16;

    protected final List<String> filePaths;

    public AbstractTraceReader(List<String> filePaths) {
        this.filePaths = requireNonNull(filePaths);
    }

    /**
     * Returns the input stream of the trace data.
     */
    protected InputStream readFiles() throws IOException {
        List<InputStream> inputs = new ArrayList<>(filePaths.size());
        for (String filePath : filePaths) {
            inputs.add(readFile(filePath));
        }
        return new SequenceInputStream(Collections.enumeration(inputs));
    }

    /**
     * Returns the input stream, decompressing if required.
     */
    private InputStream readFile(String filePath) throws IOException {
        BufferedInputStream input = new BufferedInputStream(openFile(filePath), BUFFER_SIZE);
        input.mark(100);
        try {
            return new XZInputStream(input);
        } catch (IOException e) {
            input.reset();
        }
        try {
            return new CompressorStreamFactory().createCompressorInputStream(input);
        } catch (CompressorException e) {
            input.reset();
        }
        try {
            return new ArchiveStreamFactory().createArchiveInputStream(input);
        } catch (ArchiveException e) {
            input.reset();
        }
        return input;
    }

    /**
     * Returns the input stream for the raw file.
     */
    private InputStream openFile(String filePath) throws IOException {
        Path file = Paths.get(filePath);
        if (Files.exists(file)) {
            return Files.newInputStream(file);
        }
        InputStream input = getClass().getResourceAsStream(filePath);
        checkArgument(input != null, "Could not find file: " + filePath);
        return input;
    }
}
