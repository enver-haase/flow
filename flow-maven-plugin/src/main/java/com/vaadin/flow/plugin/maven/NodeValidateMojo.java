/*
 * Copyright 2000-2018 Vaadin Ltd.
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
package com.vaadin.flow.plugin.maven;

import com.vaadin.flow.plugin.common.ArtifactData;
import com.vaadin.flow.plugin.common.FlowPluginFrontendUtils;
import com.vaadin.flow.plugin.common.JarContentsManager;
import com.vaadin.flow.plugin.production.ProductionModeCopyStep;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import com.vaadin.flow.server.frontend.FrontendUtils;

import static com.vaadin.flow.server.Constants.RESOURCES_FRONTEND_DEFAULT;
import static com.vaadin.flow.server.frontend.FrontendUtils.FLOW_NPM_PACKAGE_NAME;

/**
 * Goal checks that node and npm tools are installed, and copies frontend
 * resources available inside `.jar` files of the project dependencies.
 */
@Mojo(name = "validate", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class NodeValidateMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;
    
    /**
     * The path to the {@literal node_modules} directory of the project.
     */
    @Parameter(defaultValue = "${project.basedir}/node_modules/")
    private File nodeModulesPath;
    
    
    /**
     * Comma separated values for the paths that should be analyzed in every
     * project dependency jar and, if files suitable for copying present in
     * those paths, those should be copied.
     */
    @Parameter(defaultValue = RESOURCES_FRONTEND_DEFAULT)
    private String jarResourcePathsToCopy;

    /**
     * Comma separated wildcards for files and directories that should be
     * copied. Default is only .js and .css files.
     */
    @Parameter(defaultValue = "**/*.js,**/*.css", required = true)
    private String includes;
    
    

    @Override
    public void execute() {

        // Do nothing when bower mode
        if (FlowPluginFrontendUtils.isBowerMode(getLog())) {
            getLog().debug("Skipped 'validate' goal because `vaadin.bowerMode` is set.");
            return;
        }

        FrontendUtils.getNodeExecutable();
        FrontendUtils.getNpmExecutable();
        
        copyFlowModuleDependencies();
    }
    
    private void copyFlowModuleDependencies() {
        List<ArtifactData> projectArtifacts = project.getArtifacts().stream()
            .filter(artifact -> "jar".equals(artifact.getType()))
            .map(artifact -> new ArtifactData(artifact.getFile(),
                artifact.getArtifactId(), artifact.getVersion()))
            .collect(Collectors.toList());

        File frontendNodeDirectory = new File(nodeModulesPath,
                FLOW_NPM_PACKAGE_NAME);
        ProductionModeCopyStep copyHelper = new ProductionModeCopyStep(
                new JarContentsManager(), projectArtifacts);
        for (String path : jarResourcePathsToCopy.split(",")) {
            copyHelper.copyFrontendJavaScriptFiles(frontendNodeDirectory, includes,
                    path);
        }
    }    
}
