/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content.impl.jackrabbit;

import org.apache.jackrabbit.core.config.*;
import org.apache.jackrabbit.core.data.DataStore;
import org.apache.jackrabbit.core.fs.FileSystem;
import org.apache.jackrabbit.core.query.QueryHandler;
import org.apache.jackrabbit.core.query.QueryHandlerContext;
import org.apache.jackrabbit.core.util.RepositoryLockMechanism;
import org.apache.jackrabbit.core.util.db.ConnectionFactory;
import org.xml.sax.InputSource;

import javax.jcr.RepositoryException;

import static org.apache.jackrabbit.core.config.RepositoryConfigurationParser.REPOSITORY_HOME_VARIABLE;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.Properties;

/**
 * DX specific Jackrabbit repository configuration handler.
 *
 * @author toto
 */
public class JahiaRepositoryConfig extends RepositoryConfig {
    private RepositoryConfig config;

    /**
     * Factory method to instantiate and initialize repository configuration. Uses custom instance of {@link RepositoryConfigurationParser}
     * for parsing the configuration file that can handle nested value placeholders.
     *
     * @param file repository configuration file name
     * @param home repository home directory
     * @return repository configuration
     * @throws ConfigurationException on configuration errors
     */
    public static JahiaRepositoryConfig create(String file, String home) throws ConfigurationException {
        URI uri = new File(file).toURI();
        Properties variables = new Properties(System.getProperties());
        variables.setProperty(REPOSITORY_HOME_VARIABLE, home);

        JahiaRepositoryConfigurationParser parser = new JahiaRepositoryConfigurationParser(variables);

        RepositoryConfig config = parser.parseRepositoryConfig(new InputSource(uri.toString()));
        config.init();

        return new JahiaRepositoryConfig(config);
    }

    public JahiaRepositoryConfig(RepositoryConfig config) {
        super(null,null,null,null,null,null,0,null,null,null,null,null,null,null,null,null);
        this.config = config;
    }

    @Override
    public String getHomeDir() {
        return config.getHomeDir();
    }

    @Override
    public FileSystem getFileSystem() throws RepositoryException {
        return config.getFileSystem();
    }

    @Deprecated
    @Override
    @SuppressWarnings("squid:MissingDeprecatedCheck")
    public String getAppName() {
        return config.getAppName();
    }

    @Deprecated
    @Override
    @SuppressWarnings("squid:MissingDeprecatedCheck")
    public AccessManagerConfig getAccessManagerConfig() {
        return config.getAccessManagerConfig();
    }

    @Deprecated
    @Override
    @SuppressWarnings("squid:MissingDeprecatedCheck")
    public LoginModuleConfig getLoginModuleConfig() {
        return config.getLoginModuleConfig();
    }

    @Override
    public SecurityConfig getSecurityConfig() {
        return config.getSecurityConfig();
    }

    @Override
    public String getWorkspacesConfigRootDir() {
        return config.getWorkspacesConfigRootDir();
    }

    @Override
    public String getDefaultWorkspaceName() {
        return config.getDefaultWorkspaceName();
    }

    @Override
    public int getWorkspaceMaxIdleTime() {
        return config.getWorkspaceMaxIdleTime();
    }

    @Override
    public Collection<WorkspaceConfig> getWorkspaceConfigs() {
        return config.getWorkspaceConfigs();
    }

    @Override
    public WorkspaceConfig getWorkspaceConfig(String name) {
        return config.getWorkspaceConfig(name);
    }

    @Override
    public VersioningConfig getVersioningConfig() {
        return config.getVersioningConfig();
    }

    @Override
    public boolean isSearchEnabled() {
        return config.isSearchEnabled();
    }

    @Override
    public QueryHandler getQueryHandler(QueryHandlerContext context) throws RepositoryException {
        return config.getQueryHandler(context);
    }

    @Override
    public ClusterConfig getClusterConfig() {
        if ("true".equals(System.getProperty("cluster.activated"))) {
            return config.getClusterConfig();
        } else {
            return null;
        }
    }

    @Override
    public ConnectionFactory getConnectionFactory() {
        return config.getConnectionFactory();
    }

    @Override
    public DataStore getDataStore() throws RepositoryException {
        return config.getDataStore();
    }

    @Override
    public RepositoryLockMechanism getRepositoryLockMechanism() throws RepositoryException {
        return config.getRepositoryLockMechanism();
    }

    @Override
    public WorkspaceConfig createWorkspaceConfig(String name, StringBuffer configContent) throws ConfigurationException {
        return config.createWorkspaceConfig(name, configContent);
    }

    @Override
    public WorkspaceConfig createWorkspaceConfig(String name, InputSource template) throws ConfigurationException {
        return config.createWorkspaceConfig(name, template);
    }

}
