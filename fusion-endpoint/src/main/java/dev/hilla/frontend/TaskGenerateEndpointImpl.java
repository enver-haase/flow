/*
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package dev.hilla.frontend;

import java.io.File;
import java.util.Objects;

import com.vaadin.experimental.FeatureFlags;
import com.vaadin.flow.server.ExecutionFailedException;
import com.vaadin.flow.server.frontend.TaskGenerateEndpoint;

import dev.hilla.generator.ClientAPIGenerator;
import dev.hilla.generator.MainGenerator;

/**
 * Starts the generation of TS files for endpoints.
 */
public class TaskGenerateEndpointImpl extends AbstractTaskEndpointGenerator
        implements TaskGenerateEndpoint {

    private final File frontendDirectory;
    private final File openApi;
    private final File outputFolder;
    private FeatureFlags featureFlags;

    TaskGenerateEndpointImpl(File applicationProperties, File openApi,
            File outputFolder, File frontendDirectory,
            FeatureFlags featureFlags) {
        super(applicationProperties);
        this.featureFlags = featureFlags;
        Objects.requireNonNull(openApi,
                "Vaadin OpenAPI file should not be null.");
        Objects.requireNonNull(outputFolder,
                "Vaadin output folder should not be null.");
        this.openApi = openApi;
        this.outputFolder = outputFolder;
        this.frontendDirectory = frontendDirectory;
    }

    @Override
    public void execute() throws ExecutionFailedException {
        File customConnectClient = new File(frontendDirectory,
                ClientAPIGenerator.CUSTOM_CONNECT_CLIENT_NAME);
        String customName = customConnectClient.exists()
                ? ("../" + ClientAPIGenerator.CUSTOM_CONNECT_CLIENT_NAME)
                : null;

        new MainGenerator(openApi, outputFolder, readApplicationProperties(),
                customName, featureFlags).start();
    }
}
