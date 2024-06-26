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
package com.google.cloud.tools.jib.builder.steps;

import com.google.cloud.tools.jib.async.AsyncStep;
import com.google.cloud.tools.jib.async.NonBlockingSteps;
import com.google.cloud.tools.jib.builder.TimerEventDispatcher;
import com.google.cloud.tools.jib.configuration.BuildConfiguration;
import com.google.cloud.tools.jib.configuration.credentials.Credential;
import com.google.cloud.tools.jib.event.events.ProgressEvent;
import com.google.cloud.tools.jib.event.progress.Allocation;
import com.google.cloud.tools.jib.http.Authorization;
import com.google.cloud.tools.jib.http.Authorizations;
import com.google.cloud.tools.jib.registry.RegistryAuthenticationFailedException;
import com.google.cloud.tools.jib.registry.RegistryAuthenticator;
import com.google.cloud.tools.jib.registry.RegistryException;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nullable;

/**
 * Authenticates push to a target registry using Docker Token Authentication.
 *
 * @see <a
 *     href="https://docs.docker.com/registry/spec/auth/token/">https://docs.docker.com/registry/spec/auth/token/</a>
 */
class AuthenticatePushStep implements AsyncStep<Authorization>, Callable<Authorization> {

    private static final String DESCRIPTION = "Authenticating with push to %s";

    private final BuildConfiguration buildConfiguration;

    private final Allocation parentProgressAllocation;

    private final RetrieveRegistryCredentialsStep retrieveTargetRegistryCredentialsStep;

    private final ListenableFuture<Authorization> listenableFuture;

    AuthenticatePushStep(ListeningExecutorService listeningExecutorService, BuildConfiguration buildConfiguration, Allocation parentProgressAllocation, RetrieveRegistryCredentialsStep retrieveTargetRegistryCredentialsStep) {
        this.buildConfiguration = buildConfiguration;
        this.parentProgressAllocation = parentProgressAllocation;
        this.retrieveTargetRegistryCredentialsStep = retrieveTargetRegistryCredentialsStep;
        listenableFuture = Futures.whenAllSucceed(retrieveTargetRegistryCredentialsStep.getFuture()).call(this, listeningExecutorService);
    }

    @Override
    public ListenableFuture<Authorization> getFuture() {
        return listenableFuture;
    }

    @Override
    @Nullable
    public Authorization call() throws ExecutionException, RegistryAuthenticationFailedException, IOException, RegistryException {
        String registry = buildConfiguration.getTargetImageConfiguration().getImageRegistry();
        Allocation progressAllocation = parentProgressAllocation.newChild("authenticate push to " + registry, 1);
        buildConfiguration.getEventDispatcher().dispatch(new ProgressEvent(progressAllocation, 0));
        try (TimerEventDispatcher ignored = new TimerEventDispatcher(buildConfiguration.getEventDispatcher(), String.format(DESCRIPTION, registry))) {
            Credential registryCredential = NonBlockingSteps.get(retrieveTargetRegistryCredentialsStep);
            Authorization registryAuthorization = registryCredential == null ? null : Authorizations.withBasicCredentials(registryCredential.getUsername(), registryCredential.getPassword());
            RegistryAuthenticator registryAuthenticator = RegistryAuthenticator.initializer(buildConfiguration.getEventDispatcher(), buildConfiguration.getTargetImageConfiguration().getImageRegistry(), buildConfiguration.getTargetImageConfiguration().getImageRepository()).setAllowInsecureRegistries(buildConfiguration.getAllowInsecureRegistries()).initialize();
            if (registryAuthenticator == null) {
                buildConfiguration.getEventDispatcher().dispatch(new ProgressEvent(progressAllocation, 1));
                return registryAuthorization;
            }
            Authorization authorization = registryAuthenticator.setAuthorization(registryAuthorization).authenticatePush();
            buildConfiguration.getEventDispatcher().dispatch(new ProgressEvent(progressAllocation, 1));
            return authorization;
        }
    }
}
