/*
 * Copyright 2018 Google LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.cloud.tools.jib.plugins.common;

/**
 * Indicates that the {@code container.volumes} config value has at least one invalid path. (The
 * path is not in the absolute unix-path style).
 */
public class InvalidContainerVolumeException extends Exception {

    private String invalidVolume;

    InvalidContainerVolumeException(String message, String invalidVolume, Throwable cause) {
        super(message, cause);
        this.invalidVolume = invalidVolume;
    }

    public String getInvalidVolume() {
        return invalidVolume;
    }
}
