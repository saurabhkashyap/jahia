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
package org.jahia.bundles.config.fileinstall;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.apache.commons.io.output.WriterOutputStream;
import org.apache.felix.fileinstall.ArtifactInstaller;
import org.apache.felix.fileinstall.ArtifactListener;
import org.apache.felix.fileinstall.internal.DirectoryWatcher;
import org.apache.tika.io.IOUtils;
import org.jahia.bundles.config.ConfigUtil;
import org.jahia.bundles.config.impl.ConfigImpl;
import org.jahia.bundles.config.Format;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationEvent;
import org.osgi.service.cm.ConfigurationListener;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;

import static org.jahia.bundles.config.ConfigUtil.flatten;

/**
 * Install configuration written in YAML
 */
@Component(service = {ConfigurationListener.class, ArtifactInstaller.class, ArtifactListener.class})
public class YamlConfigInstaller implements ArtifactInstaller, ConfigurationListener {
    private static final Logger logger = LoggerFactory.getLogger(YamlConfigInstaller.class);
    private final Map<String, String> pidToFile = new HashMap<>();
    private BundleContext context;
    private ConfigurationAdmin configAdmin;
    private final YAMLMapper yamlMapper = new YAMLMapper();


    @Reference
    public void setConfigAdmin(ConfigurationAdmin configAdmin) {
        this.configAdmin = configAdmin;
    }

    /**
     * Activate
     * @param context context
     */
    @Activate
    public void activate(BundleContext context) {
        this.context = context;
        try {
            Configuration[] configs = configAdmin.listConfigurations(null);
            if (configs != null) {
                for (Configuration config : configs) {
                    Dictionary<String,Object> dict = config.getProperties();
                    String fileName = dict != null ? (String) dict.get(DirectoryWatcher.FILENAME) : null;
                    if (fileName != null) {
                        pidToFile.put(config.getPid(), fileName);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Unable to initialize configurations list", e);
        }
    }

    public boolean canHandle(File artifact) {
        return artifact.getName().endsWith(".yaml") || artifact.getName().endsWith(".yml");
    }

    public void install(File artifact) throws Exception {
        setConfig(artifact);
    }

    public void update(File artifact) throws Exception {
        setConfig(artifact);
    }

    public void uninstall(File artifact) throws Exception {
        deleteConfig(artifact);
    }

    public void configurationEvent(final ConfigurationEvent configurationEvent) {
        if (System.getSecurityManager() != null) {
            AccessController.doPrivileged(
                    (PrivilegedAction<Void>) () -> {
                        doConfigurationEvent(configurationEvent);
                        return null;
                    }
            );
        } else {
            doConfigurationEvent(configurationEvent);
        }
    }

    private void doConfigurationEvent(ConfigurationEvent configurationEvent) {
        // Check if writing back configurations has been disabled.
        if (!shouldSaveConfig()) {
            return;
        }

        if (configurationEvent.getType() == ConfigurationEvent.CM_UPDATED) {
            try {
                update(configurationEvent);
            } catch (Exception e) {
                logger.error("Unable to save configuration", e);
            }
        }

        if (configurationEvent.getType() == ConfigurationEvent.CM_DELETED) {
            try {
                String fileName = pidToFile.remove(configurationEvent.getPid());
                File file = fileName != null ? fromConfigKey(fileName) : null;
                if (file != null && file.isFile()) {
                    Files.delete(file.toPath());
                }
            } catch (Exception e) {
                logger.error("Unable to delete configuration file", e);
            }
        }
    }

    private void update(ConfigurationEvent configurationEvent) throws IOException {
        Configuration configuration = getConfigurationAdmin().getConfiguration(configurationEvent.getPid(), "?");
        Dictionary<String, ?> dict = configuration.getProperties();
        String fileName = dict != null ? (String) dict.get(DirectoryWatcher.FILENAME) : null;
        File file = fileName != null ? fromConfigKey(fileName) : null;
        if (file != null && file.isFile() && canHandle(file)) {
            pidToFile.put(configuration.getPid(), fileName);
            Map<String, Object> previousValues;

            StringWriter previousContent = new StringWriter();
            try (InputStream input = new FileInputStream(file);
                 OutputStream out = new WriterOutputStream(previousContent, StandardCharsets.UTF_8)) {
                IOUtils.copy(input, out);
            }

            // read YAML file
            try (Reader r = new StringReader(previousContent.getBuffer().toString())) {
                previousValues = yamlMapper.readValue(r, new TypeReference<Map<String, Object>>() {
                });
            }

            String[] parsed = parsePid(file.getName());
            ConfigImpl configImpl = new ConfigImpl(configuration, parsed[1], Format.YAML);
            Map<String, Object> newValues = configImpl.getValues().getStructuredMap();

            if (!previousValues.equals(newValues)) {
                try (StringReader input = new StringReader(configImpl.getContent());
                     Writer fw = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
                    IOUtils.copy(input, fw);
                }
            }
        }
    }

    boolean shouldSaveConfig() {
        String str = this.context.getProperty(DirectoryWatcher.ENABLE_CONFIG_SAVE);
        if (str == null) {
            str = this.context.getProperty(DirectoryWatcher.DISABLE_CONFIG_SAVE);
        }
        if (str != null) {
            return Boolean.parseBoolean(str);
        }
        return true;
    }

    ConfigurationAdmin getConfigurationAdmin() {
        return configAdmin;
    }

    /**
     * Set the configuration based on the config file.
     *
     * @param f Configuration file
     * @return <code>true</code> if the configuration has been updated
     * @throws Exception
     */
    boolean setConfig(final File f) throws IOException, InvalidSyntaxException{
        final Map<String, String> ht = new HashMap<>();
        try (InputStream in = new BufferedInputStream(new FileInputStream(f))) {
            Map<String, Object> m = yamlMapper.readValue(in, new TypeReference<Map<String, Object>>() {
            });
            flatten(ht, "", m);
        }

        String[] pid = parsePid(f.getName());
        Configuration config = getConfiguration(toConfigKey(f), pid[0], pid[1]);

        Dictionary<String, Object> props = config.getProperties();
        Map<String, Object> old = props != null ? new HashMap<>(ConfigUtil.getMap(props)) : null;
        if (old != null) {
            old.remove(DirectoryWatcher.FILENAME);
            old.remove(Constants.SERVICE_PID);
            old.remove(ConfigurationAdmin.SERVICE_FACTORYPID);
        }

        if (!ht.equals(old)) {
            ht.put(DirectoryWatcher.FILENAME, toConfigKey(f));
            if (old == null) {
                logger.info("Creating configuration from {}", f.getName());
            } else {
                logger.info("Updating configuration from {}", f.getName());
            }
            config.update(new Hashtable<>(ht));
            return true;
        } else {
            return false;
        }
    }



    /**
     * Remove the configuration.
     *
     * @param f File where the configuration in was defined.
     * @return <code>true</code>
     * @throws IOException exception
     * @throws InvalidSyntaxException exception
     */
    boolean deleteConfig(File f) throws IOException, InvalidSyntaxException {
        String[] pid = parsePid(f.getName());
        logger.info("Deleting configuration from {}{}.yml", pid[0], (pid[1] == null ? "" : "-" + pid[1]));
        Configuration config = getConfiguration(toConfigKey(f), pid[0], pid[1]);
        config.delete();
        return true;
    }

    String toConfigKey(File f) {
        return f.getAbsoluteFile().toURI().toString();
    }

    File fromConfigKey(String key) {
        return new File(URI.create(key));
    }

    String[] parsePid(String path) {
        String pid = path.substring(0, path.lastIndexOf('.'));
        int n = pid.indexOf('-');
        if (n > 0) {
            String factoryPid = pid.substring(n + 1);
            pid = pid.substring(0, n);
            return new String[]{pid, factoryPid};
        } else {
            return new String[]{pid, null};
        }
    }

    Configuration getConfiguration(String fileName, String pid, String factoryPid)
            throws IOException, InvalidSyntaxException {
        Configuration oldConfiguration = findExistingConfiguration(fileName);
        if (oldConfiguration != null) {
            return oldConfiguration;
        } else {
            Configuration newConfiguration;
            if (factoryPid != null) {
                newConfiguration = getConfigurationAdmin().createFactoryConfiguration(pid, "?");
            } else {
                newConfiguration = getConfigurationAdmin().getConfiguration(pid, "?");
            }
            return newConfiguration;
        }
    }

    Configuration findExistingConfiguration(String fileName) throws IOException, InvalidSyntaxException {
        String filter = "(" + DirectoryWatcher.FILENAME + "=" + escapeFilterValue(fileName) + ")";
        Configuration[] configurations = getConfigurationAdmin().listConfigurations(filter);
        if (configurations != null && configurations.length > 0) {
            return configurations[0];
        } else {
            return null;
        }
    }

    private String escapeFilterValue(String s) {
        return s.replaceAll("[(]", "\\\\(").
                replaceAll("[)]", "\\\\)").
                replaceAll("[=]", "\\\\=").
                replaceAll("[\\*]", "\\\\*");
    }

}
