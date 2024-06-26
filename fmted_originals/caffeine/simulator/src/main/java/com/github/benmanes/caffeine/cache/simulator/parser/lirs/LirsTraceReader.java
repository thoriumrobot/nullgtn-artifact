/*
 * Copyright 2015 Ben Manes. All Rights Reserved.
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
package com.github.benmanes.caffeine.cache.simulator.parser.lirs;

import java.io.IOException;
import java.util.List;
import java.util.stream.LongStream;
import com.github.benmanes.caffeine.cache.simulator.parser.TextTraceReader;

/**
 * A reader for the trace files provided by the authors of the LIRS algorithm.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
public final class LirsTraceReader extends TextTraceReader {

    public LirsTraceReader(List<String> filePaths) {
        super(filePaths);
    }

    @Override
    public LongStream events() throws IOException {
        return lines().filter(line -> !line.isEmpty()).filter(line -> !line.equals("*")).mapToLong(Long::parseLong);
    }
}
