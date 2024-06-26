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
package com.google.cloud.tools.jib.maven;

import com.google.cloud.tools.jib.configuration.CacheDirectoryCreationException;
import com.google.cloud.tools.jib.event.DefaultEventDispatcher;
import com.google.cloud.tools.jib.event.EventDispatcher;
import com.google.cloud.tools.jib.filesystem.AbsoluteUnixPath;
import com.google.cloud.tools.jib.image.InvalidImageReferenceException;
import com.google.cloud.tools.jib.plugins.common.BuildStepsExecutionException;
import com.google.cloud.tools.jib.plugins.common.BuildStepsRunner;
import com.google.cloud.tools.jib.plugins.common.HelpfulSuggestions;
import com.google.cloud.tools.jib.plugins.common.InferredAuthRetrievalException;
import com.google.cloud.tools.jib.plugins.common.InvalidAppRootException;
import com.google.cloud.tools.jib.plugins.common.InvalidContainerVolumeException;
import com.google.cloud.tools.jib.plugins.common.InvalidWorkingDirectoryException;
import com.google.cloud.tools.jib.plugins.common.MainClassInferenceException;
import com.google.cloud.tools.jib.plugins.common.PluginConfigurationProcessor;
import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * Builds a container image and exports to disk at {@code ${project.build.directory}/jib-image.tar}.
 */
@Mojo(name = BuildTarMojo.GOAL_NAME, requiresDependencyResolution = ResolutionScope.RUNTIME_PLUS_SYSTEM)
public class BuildTarMojo extends JibPluginConfiguration {

    @VisibleForTesting
    static final String GOAL_NAME = "buildTar";

    private static final String HELPFUL_SUGGESTIONS_PREFIX = "Building image tarball failed";

    @Override
    public void execute() throws MojoExecutionException {
        if (isSkipped()) {
            getLog().info("Skipping containerization because jib-maven-plugin: skip = true");
            return;
        }
        if ("pom".equals(getProject().getPackaging())) {
            getLog().info("Skipping containerization because packaging is 'pom'...");
            return;
        }
        try {
            AbsoluteUnixPath appRoot = MojoCommon.getAppRootChecked(this);
            MavenProjectProperties projectProperties = MavenProjectProperties.getForProject(getProject(), getLog(), MojoCommon.getExtraDirectoryPath(this), MojoCommon.convertPermissionsList(getExtraDirectoryPermissions()), appRoot);
            EventDispatcher eventDispatcher = new DefaultEventDispatcher(projectProperties.getEventHandlers());
            MavenHelpfulSuggestionsBuilder mavenHelpfulSuggestionsBuilder = new MavenHelpfulSuggestionsBuilder(HELPFUL_SUGGESTIONS_PREFIX, this);
            Path buildOutput = Paths.get(getProject().getBuild().getDirectory());
            Path tarOutputPath = buildOutput.resolve("jib-image.tar");
            PluginConfigurationProcessor pluginConfigurationProcessor = PluginConfigurationProcessor.processCommonConfigurationForTarImage(new MavenRawConfiguration(this), new MavenSettingsServerCredentials(getSession().getSettings(), getSettingsDecrypter(), eventDispatcher), projectProperties, tarOutputPath, mavenHelpfulSuggestionsBuilder.build());
            HelpfulSuggestions helpfulSuggestions = mavenHelpfulSuggestionsBuilder.setBaseImageReference(pluginConfigurationProcessor.getBaseImageReference()).setBaseImageHasConfiguredCredentials(pluginConfigurationProcessor.isBaseImageCredentialPresent()).setTargetImageReference(pluginConfigurationProcessor.getTargetImageReference()).build();
            BuildStepsRunner.forBuildTar(tarOutputPath).writeImageDigest(buildOutput.resolve("jib-image.digest")).writeImageId(buildOutput.resolve("jib-image.id")).build(pluginConfigurationProcessor.getJibContainerBuilder(), pluginConfigurationProcessor.getContainerizer(), eventDispatcher, projectProperties.getJavaLayerConfigurations().getLayerConfigurations(), helpfulSuggestions);
            getLog().info("");
        } catch (InvalidAppRootException ex) {
            throw new MojoExecutionException("<container><appRoot> is not an absolute Unix-style path: " + ex.getInvalidPathValue(), ex);
        } catch (InvalidWorkingDirectoryException ex) {
            throw new MojoExecutionException("<container><workingDirectory> is not an absolute Unix-style path: " + ex.getInvalidPathValue(), ex);
        } catch (InvalidContainerVolumeException ex) {
            throw new MojoExecutionException("<container><volumes> is not an absolute Unix-style path: " + ex.getInvalidVolume(), ex);
        } catch (InvalidImageReferenceException | IOException | CacheDirectoryCreationException | MainClassInferenceException | InferredAuthRetrievalException ex) {
            throw new MojoExecutionException(ex.getMessage(), ex);
        } catch (BuildStepsExecutionException ex) {
            throw new MojoExecutionException(ex.getMessage(), ex.getCause());
        }
    }
}
