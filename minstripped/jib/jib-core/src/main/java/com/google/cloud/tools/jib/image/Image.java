/*
 * Copyright 2017 Google LLC.
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
package com.google.cloud.tools.jib.image;

import com.google.cloud.tools.jib.configuration.DockerHealthCheck;
import com.google.cloud.tools.jib.configuration.Port;
import com.google.cloud.tools.jib.filesystem.AbsoluteUnixPath;
import com.google.cloud.tools.jib.image.json.HistoryEntry;
import com.google.cloud.tools.jib.image.json.ManifestTemplate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * Represents an image.
 */
public class Image<T extends Layer> {

    /**
     * Builds the immutable {@link Image}.
     */
    public static class Builder<T extends Layer> {

        private final Class<? extends ManifestTemplate> imageFormat;

        private final ImageLayers.Builder<T> imageLayersBuilder = ImageLayers.builder();

        private final ImmutableList.Builder<HistoryEntry> historyBuilder = ImmutableList.builder();

        // Don't use ImmutableMap.Builder because it does not allow for replacing existing keys with new
        // values.
        private final Map<String, String> environmentBuilder = new HashMap<>();

        private final Map<String, String> labelsBuilder = new HashMap<>();

        private final Set<Port> exposedPortsBuilder = new HashSet<>();

        private final Set<AbsoluteUnixPath> volumesBuilder = new HashSet<>();

        private Instant created;

        private ImmutableList<String> entrypoint;

        private ImmutableList<String> programArguments;

        private DockerHealthCheck healthCheck;

        private String workingDirectory;

        private String user;

        private Builder(Class<? extends ManifestTemplate> imageFormat) {
            this.imageFormat = imageFormat;
        }

        /**
         * Sets the image creation time.
         *
         * @param created the creation time
         * @return this
         */
        public Builder<T> setCreated(Instant created) {
            this.created = created;
            return this;
        }

        /**
         * Adds a map of environment variables to the current map.
         *
         * @param environment the map of environment variables
         * @return this
         */
        public Builder<T> addEnvironment(@Nullable Map<String, String> environment) {
            if (environment != null) {
                this.environmentBuilder.putAll(environment);
            }
            return this;
        }

        /**
         * Adds an environment variable with a given name and value.
         *
         * @param name the name of the variable
         * @param value the value to set it to
         * @return this
         */
        public Builder<T> addEnvironmentVariable(String name, String value) {
            environmentBuilder.put(name, value);
            return this;
        }

        /**
         * Sets the entrypoint of the image.
         *
         * @param entrypoint the list of entrypoint tokens
         * @return this
         */
        public Builder<T> setEntrypoint(@Nullable List<String> entrypoint) {
            this.entrypoint = (entrypoint == null) ? null : ImmutableList.copyOf(entrypoint);
            return this;
        }

        /**
         * Sets the user/group to run the container as.
         *
         * @param user the username/UID and optionally the groupname/GID
         * @return this
         */
        public Builder<T> setUser(@Nullable String user) {
            this.user = user;
            return this;
        }

        /**
         * Sets the items in the "Cmd" field in the container configuration.
         *
         * @param programArguments the list of arguments to append to the image entrypoint
         * @return this
         */
        public Builder<T> setProgramArguments(@Nullable List<String> programArguments) {
            this.programArguments = (programArguments == null) ? null : ImmutableList.copyOf(programArguments);
            return this;
        }

        /**
         * Sets the container's healthcheck configuration.
         *
         * @param healthCheck the healthcheck configuration
         * @return this
         */
        public Builder<T> setHealthCheck(@Nullable DockerHealthCheck healthCheck) {
            this.healthCheck = healthCheck;
            return this;
        }

        /**
         * Adds items to the "ExposedPorts" field in the container configuration.
         *
         * @param exposedPorts the exposed ports to add
         * @return this
         */
        public Builder<T> addExposedPorts(@Nullable Set<Port> exposedPorts) {
            if (exposedPorts != null) {
                exposedPortsBuilder.addAll(exposedPorts);
            }
            return this;
        }

        /**
         * Adds items to the "Volumes" field in the container configuration.
         *
         * @param volumes the directories to create volumes
         * @return this
         */
        public Builder<T> addVolumes(@Nullable Set<AbsoluteUnixPath> volumes) {
            if (volumes != null) {
                volumesBuilder.addAll(ImmutableSet.copyOf(volumes));
            }
            return this;
        }

        /**
         * Adds items to the "Labels" field in the container configuration.
         *
         * @param labels the map of labels to add
         * @return this
         */
        public Builder<T> addLabels(@Nullable Map<String, String> labels) {
            if (labels != null) {
                labelsBuilder.putAll(labels);
            }
            return this;
        }

        /**
         * Adds an item to the "Labels" field in the container configuration.
         *
         * @param name the name of the label
         * @param value the value of the label
         * @return this
         */
        public Builder<T> addLabel(String name, String value) {
            labelsBuilder.put(name, value);
            return this;
        }

        /**
         * Sets the item in the "WorkingDir" field in the container configuration.
         *
         * @param workingDirectory the working directory
         * @return this
         */
        public Builder<T> setWorkingDirectory(@Nullable String workingDirectory) {
            this.workingDirectory = workingDirectory;
            return this;
        }

        /**
         * Adds a layer to the image.
         *
         * @param layer the layer to add
         * @return this
         * @throws LayerPropertyNotFoundException if adding the layer fails
         */
        public Builder<T> addLayer(T layer) throws LayerPropertyNotFoundException {
            imageLayersBuilder.add(layer);
            return this;
        }

        /**
         * Adds a history element to the image.
         *
         * @param history the history object to add
         * @return this
         */
        public Builder<T> addHistory(HistoryEntry history) {
            historyBuilder.add(history);
            return this;
        }

        public Image<T> build() {
            return new Image<>(imageFormat, created, imageLayersBuilder.build(), historyBuilder.build(), ImmutableMap.copyOf(environmentBuilder), entrypoint, programArguments, healthCheck, ImmutableSet.copyOf(exposedPortsBuilder), ImmutableSet.copyOf(volumesBuilder), ImmutableMap.copyOf(labelsBuilder), workingDirectory, user);
        }
    }

    public static <T extends Layer> Builder<T> builder(Class<? extends ManifestTemplate> imageFormat) {
        return new Builder<>(imageFormat);
    }

    /**
     * The image format.
     */
    private final Class<? extends ManifestTemplate> imageFormat;

    /**
     * The image creation time.
     */
    private final Instant created;

    /**
     * The layers of the image, in the order in which they are applied.
     */
    private final ImageLayers<T> layers;

    /**
     * The commands used to build each layer of the image
     */
    private final ImmutableList<HistoryEntry> history;

    /**
     * Environment variable definitions for running the image, in the format {@code NAME=VALUE}.
     */
    private final ImmutableMap<String, String> environment;

    /**
     * Initial command to run when running the image.
     */
    private final ImmutableList<String> entrypoint;

    /**
     * Arguments to append to the image entrypoint when running the image.
     */
    private final ImmutableList<String> programArguments;

    /**
     * Healthcheck configuration.
     */
    private final DockerHealthCheck healthCheck;

    /**
     * Ports that the container listens on.
     */
    private final ImmutableSet<Port> exposedPorts;

    /**
     * Directories to mount as volumes.
     */
    private final ImmutableSet<AbsoluteUnixPath> volumes;

    /**
     * Labels on the container configuration
     */
    private final ImmutableMap<String, String> labels;

    /**
     * Working directory on the container configuration
     */
    private final String workingDirectory;

    /**
     * User on the container configuration
     */
    private final String user;

    private Image(Class<? extends ManifestTemplate> imageFormat, @Nullable Instant created, ImageLayers<T> layers, ImmutableList<HistoryEntry> history, @Nullable ImmutableMap<String, String> environment, @Nullable ImmutableList<String> entrypoint, @Nullable ImmutableList<String> programArguments, @Nullable DockerHealthCheck healthCheck, @Nullable ImmutableSet<Port> exposedPorts, @Nullable ImmutableSet<AbsoluteUnixPath> volumes, @Nullable ImmutableMap<String, String> labels, @Nullable String workingDirectory, @Nullable String user) {
        this.imageFormat = imageFormat;
        this.created = created;
        this.layers = layers;
        this.history = history;
        this.environment = environment;
        this.entrypoint = entrypoint;
        this.programArguments = programArguments;
        this.healthCheck = healthCheck;
        this.exposedPorts = exposedPorts;
        this.volumes = volumes;
        this.labels = labels;
        this.workingDirectory = workingDirectory;
        this.user = user;
    }

    public Class<? extends ManifestTemplate> getImageFormat() {
        return this.imageFormat;
    }

    public Instant getCreated() {
        return created;
    }

    public ImmutableMap<String, String> getEnvironment() {
        return environment;
    }

    public ImmutableList<String> getEntrypoint() {
        return entrypoint;
    }

    public ImmutableList<String> getProgramArguments() {
        return programArguments;
    }

    public DockerHealthCheck getHealthCheck() {
        return healthCheck;
    }

    public ImmutableSet<Port> getExposedPorts() {
        return exposedPorts;
    }

    public ImmutableSet<AbsoluteUnixPath> getVolumes() {
        return volumes;
    }

    public ImmutableMap<String, String> getLabels() {
        return labels;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public String getUser() {
        return user;
    }

    public ImmutableList<T> getLayers() {
        return layers.getLayers();
    }

    public ImmutableList<HistoryEntry> getHistory() {
        return history;
    }
}
