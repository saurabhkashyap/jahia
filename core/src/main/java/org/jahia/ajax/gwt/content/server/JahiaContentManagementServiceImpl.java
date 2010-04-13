/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.content.server;

import com.extjs.gxt.ui.client.data.BaseListLoadResult;
import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import ij.ImagePlus;
import ij.io.Opener;
import ij.process.ImageProcessor;
import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.data.*;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACE;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.analytics.GWTJahiaAnalyticsData;
import org.jahia.ajax.gwt.client.data.analytics.GWTJahiaAnalyticsProfile;
import org.jahia.ajax.gwt.client.data.analytics.GWTJahiaAnalyticsQuery;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.*;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.data.seo.GWTJahiaUrlMapping;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowAction;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowDefinition;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowInfo;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowOutcome;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.client.service.content.ExistingFileException;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.commons.server.JahiaRemoteService;
import org.jahia.ajax.gwt.helper.*;
import org.jahia.bin.Export;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.services.analytics.GoogleAnalyticsProfile;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.manager.bean.Item;
import org.jahia.services.manager.bean.ManagerConfiguration;
import org.jahia.services.toolbar.bean.Visibility;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.tools.imageprocess.ImageProcess;
import org.jahia.utils.FileUtils;
import org.jahia.utils.LanguageCodeConverters;

import javax.jcr.RepositoryException;
import javax.validation.ConstraintViolationException;

import java.io.*;
import java.util.*;

/**
 * GWT server code implementation for the DMS repository services.
 *
 * @author rfelden
 * @version 5 mai 2008 - 17:23:39
 */
public class JahiaContentManagementServiceImpl extends JahiaRemoteService implements JahiaContentManagementService {
// ------------------------------ FIELDS ------------------------------

    private static final transient Logger logger = Logger.getLogger(JahiaContentManagementServiceImpl.class);

    private NavigationHelper navigation;
    private ContentManagerHelper contentManager;
    private SearchHelper search;
    private PublicationHelper publication;
    private WorkflowHelper workflow;
    private VersioningHelper versioning;
    private MashupHelper mashup;
    private ContentDefinitionHelper contentDefinition;
    private ContentHubHelper contentHub;
    private PropertiesHelper properties;
    private LanguageHelper languages;
    private RolesPermissionsHelper rolesPermissions;
    private TemplateHelper template;
    private ZipHelper zip;
    private ACLHelper acl;
    private DiffHelper diff;
    private SeoHelper seo;
    private ToolbarHelper toolbar;
    private AnalyticsHelper analytics;

// --------------------- GETTER / SETTER METHODS ---------------------

    public void setAcl(ACLHelper acl) {
        this.acl = acl;
    }

    public void setContentDefinition(ContentDefinitionHelper contentDefinition) {
        this.contentDefinition = contentDefinition;
    }

    public void setContentHub(ContentHubHelper contentHub) {
        this.contentHub = contentHub;
    }

    public void setContentManager(ContentManagerHelper contentManager) {
        this.contentManager = contentManager;
    }

    public void setMashup(MashupHelper mashup) {
        this.mashup = mashup;
    }

    public void setNavigation(NavigationHelper navigation) {
        this.navigation = navigation;
    }

    public void setProperties(PropertiesHelper properties) {
        this.properties = properties;
    }

    public void setPublication(PublicationHelper publication) {
        this.publication = publication;
    }

    public void setWorkflow(WorkflowHelper workflow) {
        this.workflow = workflow;
    }

    public void setSearch(SearchHelper search) {
        this.search = search;
    }

    public void setLanguages(LanguageHelper languages) {
        this.languages = languages;
    }

    public void setRolesPermissions(RolesPermissionsHelper rolesPermissions) {
        this.rolesPermissions = rolesPermissions;
    }

    public void setDiff(DiffHelper diff) {
        this.diff = diff;
    }

    public void setTemplate(TemplateHelper template) {
        this.template = template;
    }

    public void setVersioning(VersioningHelper versioning) {
        this.versioning = versioning;
    }

    public void setZip(ZipHelper zip) {
        this.zip = zip;
    }

    public void setToolbar(ToolbarHelper toolbar) {
        this.toolbar = toolbar;
    }

    // ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface JahiaContentManagementServiceAsync ---------------------

    public GWTManagerConfiguration getConfiguration(String name) throws GWTJahiaServiceException {
        try {
            ManagerConfiguration config = (ManagerConfiguration) SpringContextSingleton.getBean(name);
            if (config != null) {
                logger.debug("Config. " + name + " found.");
                GWTManagerConfiguration gwtConfig = new GWTManagerConfiguration();
                gwtConfig.setName(name);

                //  create the gwt configuration
                gwtConfig.setEnableTextMenu(config.isEnableTextMenu());
                gwtConfig.setDisplayProvider(config.isDisplayProvider());
                gwtConfig.setDisplaySearchInPage(config.isDisplaySearchInPage());
                gwtConfig.setDisplaySearchInContent(config.isDisplaySearchInContent());
                gwtConfig.setDisplaySearchInFile(config.isDisplaySearchInFile());
                gwtConfig.setDisplaySearchInTag(config.isDisplaySearchInTag());

                // set toolbar group : ToDo replace it by toolbar bean
                gwtConfig.setToolbarGroup(config.getToolbarGroup());
                gwtConfig.setGwtJahiaToolbarSet(toolbar.getGWTToolbars(getSite(), getRemoteJahiaUser(), getLocale(),getUILocale(), getRequest(),config.getToolbarGroup()));


                // add table columns
                for (Item item : config.getTableColumns()) {
                    Visibility visibility = item.getVisibility();
                    if ((visibility != null && visibility.getRealValue(getSite(), getRemoteJahiaUser(), getLocale(), getRequest())) || visibility == null) {
                        gwtConfig.addColumn(item.getKey());
                    }
                }

                // add tabs
                for (Item item : config.getTabs()) {
                    Visibility visibility = item.getVisibility();
                    if ((visibility != null && visibility.getRealValue(getSite(), getRemoteJahiaUser(), getLocale(), getRequest())) || visibility == null) {
                        gwtConfig.addTab(item.getKey());
                    }

                }

                // add accordion panels
                for (Item item : config.getAccordionPanels()) {
                    Visibility visibility = item.getVisibility();
                    if ((visibility != null && visibility.getRealValue(getSite(), getRemoteJahiaUser(), getLocale(), getRequest())) || visibility == null) {
                        gwtConfig.addAccordion(item.getKey());
                    }

                }

                gwtConfig.setNodeTypes(config.getNodeTypes());
                gwtConfig.setFolderTypes(config.getFolderTypes());
                return gwtConfig;
            } else {
                logger.error("Config. " + name + " not found.");
                throw new GWTJahiaServiceException("Config. " + name + " not found.");
            }
        } catch (GWTJahiaServiceException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    public List<GWTJahiaNode> ls(GWTJahiaNode folder, String nodeTypes, String mimeTypes, String filters, boolean noFolders) throws GWTJahiaServiceException {
        return navigation.ls(folder, nodeTypes, mimeTypes, filters, noFolders, true, retrieveCurrentSession());
    }

    public ListLoadResult<GWTJahiaNode> lsLoad(GWTJahiaNode folder, String nodeTypes, String mimeTypes, String filters, boolean noFolders) throws GWTJahiaServiceException {
        return new BaseListLoadResult<GWTJahiaNode>(navigation.ls(folder, nodeTypes, mimeTypes, filters, noFolders, true, retrieveCurrentSession()));
    }

    public List<GWTJahiaNode> getRoot(String repositoryType, String nodeTypes, String mimeTypes, String filters, List<String> selectedNodes, List<String> openPaths) throws GWTJahiaServiceException {
        if (openPaths == null || openPaths.size() == 0) {
            openPaths = getOpenPathsForRepository(repositoryType);
        }

        logger.debug(new StringBuilder("retrieving open paths for ").append(repositoryType).append(" :\n").append(openPaths).toString());

        return navigation.retrieveRoot(repositoryType, nodeTypes, mimeTypes, filters, selectedNodes, openPaths,
                getSite(), retrieveCurrentSession());
    }

    public List<GWTJahiaNode> getRoot(String repositoryType, String nodeTypes, String mimeTypes, String filters, List<String> selectedNodes, List<String> openPaths, boolean forceCreate) throws GWTJahiaServiceException {
        if (openPaths == null || openPaths.size() == 0) {
            openPaths = getOpenPathsForRepository(repositoryType);
        }

        logger.debug(new StringBuilder("retrieving open paths for ").append(repositoryType).append(" :\n").append(openPaths).toString());

        return navigation.retrieveRoot(repositoryType, nodeTypes, mimeTypes, filters, selectedNodes, openPaths, getSite(), retrieveCurrentSession());
    }

    /**
     * Get a node by its path if existing.
     *
     * @param path path o fthe node you want
     * @return the founded node if existing
     * @throws org.jahia.ajax.gwt.client.service.GWTJahiaServiceException
     *          if node does not exist
     */
    public GWTJahiaNode getNode(String path) throws GWTJahiaServiceException {
        return navigation.getNode(path, retrieveCurrentSession());
    }

    public void saveOpenPathsForRepository(String repositoryType, List<String> paths) throws GWTJahiaServiceException {
        getRequest().getSession().setAttribute(navigation.SAVED_OPEN_PATHS + repositoryType, paths);
    }

    public List<GWTJahiaNode> search(String searchString, int limit) throws GWTJahiaServiceException {
        return search.search(searchString, limit, retrieveCurrentSession());
    }

    public PagingLoadResult<GWTJahiaNode> search(GWTJahiaSearchQuery searchQuery, int limit, int offset) throws GWTJahiaServiceException {
        // To do: find a better war to handle total size
        List<GWTJahiaNode> result = search.search(searchQuery, 0, 0, retrieveCurrentSession());
        int size = result.size();
        result = new ArrayList<GWTJahiaNode>(result.subList(offset, Math.min(size, offset + limit)));
        return new BasePagingLoadResult<GWTJahiaNode>(result, offset, size);
    }


    public List<GWTJahiaNode> search(String searchString, int limit, String nodeTypes, String mimeTypes, String filters) throws GWTJahiaServiceException {
        return search.search(searchString, limit, nodeTypes, mimeTypes, filters, retrieveCurrentSession());
    }

    public List<GWTJahiaPortletDefinition> searchPortlets(String match) throws GWTJahiaServiceException {
        try {
            return mashup.searchPortlets(match, getRemoteJahiaUser(), getLocale());
        } catch (Exception e) {
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    public GWTJahiaAnalyticsProfile getGAProfiles() {
        return analytics.getProfile(getSite());
    }

    public List<GWTJahiaNode> getSavedSearch() throws GWTJahiaServiceException {
        return search.getSavedSearch(retrieveCurrentSession());
    }

    public GWTJahiaNode saveSearch(String searchString, String name) throws GWTJahiaServiceException {
        return search.saveSearch(searchString, name, getSite(), retrieveCurrentSession());
    }

    public void saveSearch(String searchString, String path, String name) throws GWTJahiaServiceException {
        final GWTJahiaNode jahiaNode = search.saveSearch(searchString, path, name, retrieveCurrentSession());
    }

    public void saveSearch(GWTJahiaSearchQuery searchQuery, String path, String name) throws GWTJahiaServiceException {
        final GWTJahiaNode jahiaNode = search.saveSearch(searchQuery, path, name, retrieveCurrentSession());
    }

    public void saveSearchOnTopOf(String searchString, String path, String name) throws GWTJahiaServiceException {
        final GWTJahiaNode parentNode = navigation.getParentNode(path, retrieveCurrentSession());
        final GWTJahiaNode jahiaNode = search.saveSearch(searchString, parentNode.getPath(), name, retrieveCurrentSession());
        try {
            contentManager.moveOnTopOf(jahiaNode.getPath(), path, retrieveCurrentSession());
        } catch (RepositoryException e) {
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    public void mount(String path, String target, String root) throws GWTJahiaServiceException {
        contentHub.mount(target, root, getUser());
    }

    public List<GWTJahiaNode> getMountpoints() throws GWTJahiaServiceException {
        return navigation.getMountpoints(retrieveCurrentSession());
    }

    public void storePasswordForProvider(String providerKey, String username, String password) {
        contentHub.storePasswordForProvider(getUser(), providerKey, username, password);
    }

    public Map<String, String> getStoredPasswordsProviders() {
        return contentHub.getStoredPasswordsProviders(getUser());
    }

    public void setLock(List<String> paths, boolean locked) throws GWTJahiaServiceException {
        contentManager.setLock(paths, locked, retrieveCurrentSession());
    }

    public void deletePaths(List<String> paths) throws GWTJahiaServiceException {
        contentManager.deletePaths(paths, getUser(), retrieveCurrentSession());
    }

    public String getAbsolutePath(String path) throws GWTJahiaServiceException {
        return navigation.getAbsolutePath(path, retrieveParamBean(), retrieveCurrentSession());
    }

    public void checkWriteable(List<String> paths) throws GWTJahiaServiceException {
        contentManager.checkWriteable(paths, getUser(), retrieveCurrentSession());
    }

    public void paste(List<String> pathsToCopy, String destinationPath, String newName, boolean cut) throws GWTJahiaServiceException {
        contentManager.copy(pathsToCopy, destinationPath, newName, false, cut, false, retrieveCurrentSession());
    }

    public void copyAndSaveProperties(List<String> pathsToCopy, String destinationPath, List<String> mixin, GWTJahiaNodeACL acl, Map<String, List<GWTJahiaNodeProperty>> langCodeProperties, List<GWTJahiaNodeProperty> newsProps) throws GWTJahiaServiceException {
        try {
            String newName = null;
            List<GWTJahiaNodeProperty> l = langCodeProperties.get(getSite().getDefaultLanguage());
            if (l == null && langCodeProperties.size() > 0) {
                l = langCodeProperties.values().iterator().next();
            }
            if (l != null) {
                newName = contentManager.generateNameFromTitle(l);
            }

            List<GWTJahiaNode> nodes = contentManager.copy(pathsToCopy, destinationPath, newName, false, false, false, retrieveCurrentSession());
            for (GWTJahiaNode node : nodes) {
                node.getNodeTypes().addAll(mixin);
            }
            saveProperties(nodes, newsProps);

            // save shared properties
            if (langCodeProperties != null && !langCodeProperties.isEmpty()) {
                Iterator<String> langCode = langCodeProperties.keySet().iterator();
                // save properties per lang
                while (langCode.hasNext()) {
                    String currentLangCode = langCode.next();
                    List<GWTJahiaNodeProperty> properties = langCodeProperties.get(currentLangCode);
                    saveProperties(nodes, properties, currentLangCode);
                }
            }

            if (acl != null) {
                for (GWTJahiaNode node : nodes) {
                    setACL(node.getPath(), acl);
                }
            }
        } catch (Throwable e) {
            logger.error(e, e);
        }
    }

    public void pasteReferences(List<String> pathsToCopy, String destinationPath, String newName) throws GWTJahiaServiceException {
        contentManager.copy(pathsToCopy, destinationPath, newName, false, false, true, retrieveCurrentSession());
    }

    public void pasteOnTopOf(List<String> nodes, String path, String newName, boolean cut) throws GWTJahiaServiceException {
        contentManager.copy(nodes, path, newName, true, cut, false, retrieveCurrentSession());
    }

    public void pasteReferencesOnTopOf(List<String> pathsToCopy, String destinationPath, String newName) throws GWTJahiaServiceException {
        contentManager.copy(pathsToCopy, destinationPath, newName, true, false, true, retrieveCurrentSession());
    }

    public GWTJahiaGetPropertiesResult getProperties(String path, String langCode) throws GWTJahiaServiceException {
        if (langCode == null) {
            return getProperties(path);
        }
        return getProperties(path, LanguageCodeConverters.getLocaleFromCode(langCode));
    }


    public GWTJahiaGetPropertiesResult getProperties(String path) throws GWTJahiaServiceException {
        return getProperties(path, getLocale());
    }

    /**
     * Get GWTJahiaGetPropertiesResult
     *
     * @param path
     * @param locale
     * @return
     * @throws GWTJahiaServiceException
     */
    public GWTJahiaGetPropertiesResult getProperties(String path, Locale locale) throws GWTJahiaServiceException {
        final GWTJahiaNode node = navigation.getNode(path, retrieveCurrentSession());
        final HashMap<String, Object> map = new HashMap<String, Object>();
        try {
            JCRSessionWrapper sessionWrapper = retrieveCurrentSession();
            JCRNodeWrapper nodeWrapper = sessionWrapper.getNode(node.getPath());
            map.put("contextNode", nodeWrapper);
        } catch (RepositoryException e) {
            logger.error("Cannot get node", e);
        }

        // get node type
        final List<GWTJahiaNodeType> nodeTypes = contentDefinition.getNodeTypes(node.getNodeTypes(), map, getUILocale());

        // get properties
        final Map<String, GWTJahiaNodeProperty> props = properties.getProperties(path, retrieveCurrentSession(locale));

        final GWTJahiaGetPropertiesResult result = new GWTJahiaGetPropertiesResult(nodeTypes, props);
        result.setNode(node);
        result.setAvailabledLanguages(languages.getLanguages(getSite(), getRemoteJahiaUser(), getLocale()));
        result.setCurrentLocale(languages.getCurrentLang(getLocale()));
        return result;
    }

    /**
     * Save properties of for the given nodes
     *
     * @param nodes
     * @param newProps
     * @throws GWTJahiaServiceException
     */
    public void saveProperties(List<GWTJahiaNode> nodes, List<GWTJahiaNodeProperty> newProps) throws GWTJahiaServiceException {
        properties.saveProperties(nodes, newProps, getRemoteJahiaUser(), retrieveCurrentSession());
    }

    /**
     * Save properties by langCode
     *
     * @param nodes
     * @param newProps
     * @param local
     * @throws GWTJahiaServiceException
     */
    public void saveProperties(List<GWTJahiaNode> nodes, List<GWTJahiaNodeProperty> newProps, String local) throws GWTJahiaServiceException {
        properties.saveProperties(nodes, newProps, getRemoteJahiaUser(), retrieveCurrentSession(LanguageCodeConverters.languageCodeToLocale(local)));
    }

    /**
     * Save properties and acl for the given nodes
     *
     * @param nodes
     * @param acl
     * @param langCodeProperties, List<GWTJahiaNodeProperty> sharedProperties
     * @throws GWTJahiaServiceException
     */
    public void savePropertiesAndACL(List<GWTJahiaNode> nodes, GWTJahiaNodeACL acl, Map<String, List<GWTJahiaNodeProperty>> langCodeProperties, List<GWTJahiaNodeProperty> sharedProperties) throws GWTJahiaServiceException {
        Iterator<String> langCode = langCodeProperties.keySet().iterator();
        // save properties per lang
        while (langCode.hasNext()) {
            String currentLangCode = langCode.next();
            List<GWTJahiaNodeProperty> props = langCodeProperties.get(currentLangCode);
            saveProperties(nodes, props, currentLangCode);
        }

        // save shared properties
        saveProperties(nodes, sharedProperties);

        // save acl
        if (acl != null) {
            for (GWTJahiaNode node : nodes) {
                setACL(node.getPath(), acl);
            }
        }
    }

    /**
     * Create node
     *
     * @param parentPath
     * @param name
     * @param nodeType
     * @param mixin
     * @param acl
     * @param props
     * @param captcha
     * @return
     * @throws GWTJahiaServiceException
     */
    public GWTJahiaNode createNode(String parentPath, String name, String nodeType, List<String> mixin, GWTJahiaNodeACL acl, List<GWTJahiaNodeProperty> props, String captcha) throws GWTJahiaServiceException {
        ParamBean context = retrieveParamBean();
        if (captcha != null && !contentManager.checkCaptcha(context, captcha)) {
            throw new GWTJahiaServiceException("Invalid captcha");
        }
        GWTJahiaNode res;
        if (captcha != null) {
            res = contentManager.unsecureCreateNode(parentPath, name, nodeType, mixin, props, retrieveCurrentSession());
        } else {
            res = contentManager.createNode(parentPath, name, nodeType, mixin, props, retrieveCurrentSession());
        }
        if (acl != null) {
            setACL(res.getPath(), acl);
        }
        return res;
    }

    /**
     * Create node with multilangue
     *
     * @param parentPath
     * @param name
     * @param nodeType
     * @param mixin
     * @param acl
     * @param props
     * @param langCodeProperties
     * @param captcha
     * @return
     * @throws GWTJahiaServiceException
     */
    public GWTJahiaNode createNode(String parentPath, String name, String nodeType, List<String> mixin, GWTJahiaNodeACL acl, List<GWTJahiaNodeProperty> props, Map<String, List<GWTJahiaNodeProperty>> langCodeProperties, String captcha) throws GWTJahiaServiceException {
        if (name == null) {
            String defaultLanguage = getLocale().toString();
            if (getSite() != null) {
                defaultLanguage = getSite().getDefaultLanguage();
            }
            List<GWTJahiaNodeProperty> l = langCodeProperties.get(defaultLanguage);
            if (l == null && langCodeProperties.size() > 0) {
                l = langCodeProperties.values().iterator().next();
            }
            if (l != null) {
                name = contentManager.generateNameFromTitle(l);
            }
        }

        GWTJahiaNode node = createNode(parentPath, name, nodeType, mixin, acl, props, captcha);
        // save shared properties
        if (langCodeProperties != null && !langCodeProperties.isEmpty()) {
            List<GWTJahiaNode> nodes = new ArrayList<GWTJahiaNode>();
            nodes.add(node);
            Iterator<String> langCode = langCodeProperties.keySet().iterator();
            // save properties per lang
            while (langCode.hasNext()) {
                String currentLangCode = langCode.next();
                List<GWTJahiaNodeProperty> properties = langCodeProperties.get(currentLangCode);
                saveProperties(nodes, properties, currentLangCode);
            }
        }
        return node;
    }

    /**
     * Move and create node
     *
     * @param path
     * @param name
     * @param nodeType
     * @param mixin
     * @param acl
     * @param properties
     * @param captcha
     * @throws GWTJahiaServiceException
     */
    public GWTJahiaNode createNodeAndMoveBefore(String path, String name, String nodeType, List<String> mixin, GWTJahiaNodeACL acl, List<GWTJahiaNodeProperty> properties, Map<String, List<GWTJahiaNodeProperty>> langCodeProperties, String captcha) throws GWTJahiaServiceException {
        if (name == null) {
            List<GWTJahiaNodeProperty> l = langCodeProperties.get(getSite().getDefaultLanguage());
            if (l == null && langCodeProperties.size() > 0) {
                l = langCodeProperties.values().iterator().next();
            }
            if (l != null) {
                name = contentManager.generateNameFromTitle(l);
            }
        }

        final GWTJahiaNode parentNode = navigation.getParentNode(path, retrieveCurrentSession());
        final GWTJahiaNode jahiaNode = contentManager.createNode(parentNode.getPath(), name, nodeType, mixin, properties, retrieveCurrentSession());

        try {
            contentManager.moveOnTopOf(jahiaNode.getPath(), path, retrieveCurrentSession());
        } catch (RepositoryException e) {
            throw new GWTJahiaServiceException(e.getMessage());
        }
        if (acl != null) {
            setACL(jahiaNode.getPath(), acl);
        }

        // save shared properties
        if (langCodeProperties != null && !langCodeProperties.isEmpty()) {
            List<GWTJahiaNode> nodes = new ArrayList<GWTJahiaNode>();
            nodes.add(jahiaNode);
            Iterator<String> langCode = langCodeProperties.keySet().iterator();
            // save properties per lang
            while (langCode.hasNext()) {
                String currentLangCode = langCode.next();
                List<GWTJahiaNodeProperty> props = langCodeProperties.get(currentLangCode);
                saveProperties(nodes, props, currentLangCode);
            }
        }
        return jahiaNode;
    }

    /**
     * Create Folder
     *
     * @param parentPath
     * @param name
     * @throws GWTJahiaServiceException
     */
    public void createFolder(String parentPath, String name) throws GWTJahiaServiceException {
        contentManager.createFolder(parentPath, name, retrieveCurrentSession());
    }

    public GWTJahiaNode createPortletInstance(String path, GWTJahiaNewPortletInstance wiz) throws GWTJahiaServiceException {
        return mashup.createPortletInstance(path, wiz, retrieveCurrentSession());
    }

    public GWTJahiaNode createRSSPortletInstance(String path, String name, String url) throws GWTJahiaServiceException {
        return mashup.createRSSPortletInstance(path, name, url, getSite(), retrieveCurrentSession());
    }

    public GWTJahiaNode createGoogleGadgetPortletInstance(String path, String name, String script) throws GWTJahiaServiceException {
        return mashup.createGoogleGadgetPortletInstance(path, name, script, getSite(), retrieveCurrentSession());
    }

    public void checkExistence(String path) throws GWTJahiaServiceException {
        if (contentManager.checkExistence(path, retrieveCurrentSession())) {
            throw new ExistingFileException(path);
        }
    }

    public void rename(String path, String newName) throws GWTJahiaServiceException {
        contentManager.rename(path, newName, retrieveCurrentSession());
    }

    public void move(String sourcePath, String targetPath) throws GWTJahiaServiceException {
        try {
            contentManager.move(sourcePath, targetPath, retrieveCurrentSession());
        } catch (RepositoryException e) {
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    public void moveAtEnd(String sourcePath, String targetPath) throws GWTJahiaServiceException {
        try {
            contentManager.moveAtEnd(sourcePath, targetPath, retrieveCurrentSession());
        } catch (RepositoryException e) {
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    public void moveOnTopOf(String sourcePath, String targetPath) throws GWTJahiaServiceException {
        try {
            contentManager.moveOnTopOf(sourcePath, targetPath, retrieveCurrentSession());
        } catch (RepositoryException e) {
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    public GWTJahiaNodeACL getACL(String path) throws GWTJahiaServiceException {
        return contentManager.getACL(path, false, retrieveCurrentSession(), getLocale());
    }

    public GWTJahiaNodeACL getNewACL(String parentPath) throws GWTJahiaServiceException {
        return contentManager.getACL(parentPath, true, retrieveCurrentSession(), getLocale());
    }

    public void setACL(String path, GWTJahiaNodeACL acl) throws GWTJahiaServiceException {
        contentManager.setACL(path, acl, retrieveCurrentSession());
    }

    public GWTJahiaNodeACE createDefaultUsersGroupACE(List<String> permissions, boolean grand) throws GWTJahiaServiceException {
        return acl.createUsersGroupACE(permissions, grand, getSite());
    }

    public List<GWTJahiaNodeUsage> getUsages(String path) throws GWTJahiaServiceException {
        return navigation.getUsages(path, retrieveCurrentSession());
    }

    public void zip(List<String> paths, String archiveName) throws GWTJahiaServiceException {
        zip.zip(paths, archiveName, retrieveCurrentSession());
    }

    public void unzip(List<String> paths) throws GWTJahiaServiceException {
        zip.unzip(paths, false, retrieveCurrentSession());
    }

    public String getExportUrl(String path) throws GWTJahiaServiceException {
        try {
            JCRSessionWrapper jcrSessionWrapper = JCRSessionFactory.getInstance().getCurrentUserSession();
            return "/jahia" + Export.getExportServletPath() + "/" + jcrSessionWrapper.getWorkspace().getName() + path;
        } catch (RepositoryException e) {
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    public void cropImage(String path, String target, int top, int left, int width, int height, boolean forceReplace) throws GWTJahiaServiceException {
        try {
            JCRNodeWrapper node = JCRSessionFactory.getInstance().getCurrentUserSession().getNode(path);
            if (contentManager.checkExistence(node.getPath().replace(node.getName(), target), retrieveCurrentSession()) && !forceReplace) {
                throw new ExistingFileException("The file " + target + " already exists.");
            }

            File tmp = File.createTempFile("image", "tmp");
            FileUtils.copyStream(node.getFileContent().downloadFile(), new FileOutputStream(tmp));
            Opener op = new Opener();
            ImagePlus ip = op.openImage(tmp.getPath());
            ImageProcessor processor = ip.getProcessor();

            processor.setRoi(left, top, width, height);
            processor = processor.crop();
            ip.setProcessor(null, processor);

            File f = File.createTempFile("image", "tmp");
            ImageProcess.save(op.getFileType(tmp.getPath()), ip, f);
            ((JCRNodeWrapper) node.getParent()).uploadFile(target, new FileInputStream(f), node.getFileContent().getContentType());
            node.getParent().save();
            tmp.delete();
            f.delete();
        } catch (ExistingFileException e) {
            throw e;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    public void resizeImage(String path, String target, int width, int height, boolean forceReplace) throws GWTJahiaServiceException {
        try {
            JCRNodeWrapper node = JCRSessionFactory.getInstance().getCurrentUserSession().getNode(path);
            if (contentManager.checkExistence(node.getPath().replace(node.getName(), target), retrieveCurrentSession()) && !forceReplace) {
                throw new ExistingFileException("The file " + target + " already exists.");
            }

            File tmp = File.createTempFile("image", "tmp");
            FileUtils.copyStream(node.getFileContent().downloadFile(), new FileOutputStream(tmp));
            Opener op = new Opener();
            ImagePlus ip = op.openImage(tmp.getPath());
            ImageProcessor processor = ip.getProcessor();
            processor = processor.resize(width, height);
            ip.setProcessor(null, processor);

            File f = File.createTempFile("image", "tmp");
            ImageProcess.save(op.getFileType(tmp.getPath()), ip, f);
            ((JCRNodeWrapper) node.getParent()).uploadFile(target, new FileInputStream(f), node.getFileContent().getContentType());
            node.getParent().save();
            tmp.delete();
            f.delete();
        } catch (ExistingFileException e) {
            throw e;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    public void rotateImage(String path, String target, boolean clockwise, boolean forceReplace) throws GWTJahiaServiceException {
        try {
            JCRNodeWrapper node = JCRSessionFactory.getInstance().getCurrentUserSession().getNode(path);
            if (contentManager.checkExistence(node.getPath().replace(node.getName(), target), retrieveCurrentSession()) && !forceReplace) {
                throw new ExistingFileException("The file " + target + " already exists.");
            }

            File tmp = File.createTempFile("image", "tmp");
            FileUtils.copyStream(node.getFileContent().downloadFile(), new FileOutputStream(tmp));
            Opener op = new Opener();
            ImagePlus ip = op.openImage(tmp.getPath());
            ImageProcessor processor = ip.getProcessor();
            if (clockwise) {
                processor = processor.rotateRight();
            } else {
                processor = processor.rotateLeft();
            }
            ip.setProcessor(null, processor);

            File f = File.createTempFile("image", "tmp");
            ImageProcess.save(op.getFileType(tmp.getPath()), ip, f);
            ((JCRNodeWrapper) node.getParent()).uploadFile(target, new FileInputStream(f), node.getFileContent().getContentType());
            node.getParent().save();
            tmp.delete();
            f.delete();
        } catch (ExistingFileException e) {
            throw e;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    public void activateVersioning(List<String> path) throws GWTJahiaServiceException {
        versioning.activateVersioning(path, retrieveCurrentSession());
    }

    public List<GWTJahiaNodeVersion> getVersions(String path) throws GWTJahiaServiceException {
        try {
            return navigation.getVersions(JCRSessionFactory.getInstance().getCurrentUserSession().getNode(path));
        } catch (RepositoryException e) {
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    public PagingLoadResult<GWTJahiaNodeVersion> getVersions(GWTJahiaNode node, String workspace, int limit, int offset) throws GWTJahiaServiceException {
        try {
            List<GWTJahiaNodeVersion> result = navigation.getPublishedVersions(JCRSessionFactory.getInstance().getCurrentUserSession(workspace).getNode(node.getPath()));

            // add current workspace version
            final GWTJahiaNodeVersion currentWorkspaceVersion = new GWTJahiaNodeVersion();
            currentWorkspaceVersion.setNode(node);
            result.add(0, currentWorkspaceVersion);

            // get sublist: Todo Find a better way
            int size = result.size();
            result = new ArrayList<GWTJahiaNodeVersion>(result.subList(offset, Math.min(size, offset + limit)));
            return new BasePagingLoadResult<GWTJahiaNodeVersion>(result, offset, size);
        } catch (RepositoryException e) {
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    public void restoreNode(GWTJahiaNodeVersion gwtJahiaNodeVersion) throws GWTJahiaServiceException {
        String nodeUuid = gwtJahiaNodeVersion.getNode().getUUID();
        String versiOnUuid = gwtJahiaNodeVersion.getUUID();
        versioning.restoreNode(nodeUuid, versiOnUuid, retrieveCurrentSession());
    }

    public void uploadedFile(String location, String tmpName, int operation, String newName) throws GWTJahiaServiceException {
        contentManager.uploadedFile(location, tmpName, operation, newName, retrieveCurrentSession());
    }

    public GWTRenderResult getRenderedContent(String path, String workspace, String locale, String template, String templateWrapper, Map<String, String> contextParams, boolean editMode, String configName) throws GWTJahiaServiceException {
        if (locale != null) {
            final Locale value = LanguageCodeConverters.languageCodeToLocale(locale);
            if (!getLocale().equals(value)) {
                getRequest().getSession().setAttribute(ProcessingContext.SESSION_LOCALE, value);
            }
        }
        return this.template.getRenderedContent(path, template, templateWrapper, contextParams, editMode, configName, getRequest(), getResponse(), retrieveCurrentSession());
    }

    public String getNodeURL(String path, String locale, int mode) throws GWTJahiaServiceException {
        return this.template.getNodeURL(path, mode, retrieveParamBean().getRequest(), retrieveParamBean().getResponse(), retrieveCurrentSession());
    }

    public String getNodeURL(String path, String version, String workspace, String locale, int mode) throws GWTJahiaServiceException {
        return this.template.getNodeURL(path, version, mode, retrieveParamBean().getRequest(), retrieveParamBean().getResponse(), retrieveCurrentSession(workspace));
    }

    public void importContent(String parentPath, String fileKey) throws GWTJahiaServiceException {
        contentManager.importContent(parentPath, fileKey);
    }

    public List<GWTJahiaNode> getNodesWithPublicationInfo(List<String> pathes) throws GWTJahiaServiceException {
        List<GWTJahiaNode> list = new ArrayList<GWTJahiaNode>();
        for (String path : pathes) {
            try {
                GWTJahiaNode gwtJahiaNode = navigation.getNode(path, retrieveCurrentSession());
                gwtJahiaNode.setPublicationInfo(getPublicationInfo(gwtJahiaNode.getPath(), false));
                gwtJahiaNode.setWorkflowInfo(getWorkflowInfo(gwtJahiaNode.getPath()));
                list.add(gwtJahiaNode);
            } catch (GWTJahiaServiceException e) {
                logger.debug(e, e);
            }
        }
        return list;
    }


    public void startWorkflow(String path, GWTJahiaWorkflowDefinition workflowDefinition) throws GWTJahiaServiceException {
        workflow.startWorkflow(path, workflowDefinition, retrieveCurrentSession());
    }

    public void assignAndCompleteTask(String path, GWTJahiaWorkflowAction action, GWTJahiaWorkflowOutcome outcome) throws GWTJahiaServiceException {
        workflow.assignAndCompleteTask(path, action, outcome, retrieveCurrentSession());
    }

    /**
     * Publish the specified path.
     *
     * @param path the path to publish, will not auto publish the parents
     * @throws GWTJahiaServiceException
     */
    public void publish(String path, boolean allSubTree, String comments, boolean reverse) throws GWTJahiaServiceException {
        JCRSessionWrapper session = retrieveCurrentSession();
        publication.publish(path, Collections.singleton(session.getLocale().toString()), allSubTree, reverse, session);
    }

    /**
     * Publish the specified paths.
     *
     * @param paths the list of node paths to publish, will not auto publish the parents
     * @throws GWTJahiaServiceException
     */
    public void publish(List<String> paths, boolean reverse) throws GWTJahiaServiceException {
        JCRSessionWrapper session = retrieveCurrentSession();
        publication.publish(paths, Collections.singleton(session.getLocale().toString()), getRemoteJahiaUser(), false, session);
    }

    /**
     * Unpublish the specified path and its subnodes.
     *
     * @param path the path to unpublish, will not unpublish the references
     * @throws GWTJahiaServiceException
     */
    public void unpublish(String path) throws GWTJahiaServiceException {
        long l = System.currentTimeMillis();
        JCRSessionWrapper session = retrieveCurrentSession();
        publication.unpublish(path, Collections.singleton(session.getLocale().toString()), session.getUser());
        System.out.println("-->" + (System.currentTimeMillis() - l));
    }

    /**
     * Get the publication status information for a particular path.
     *
     * @param path path to get publication info from
     * @return a GWTJahiaPublicationInfo object filled with the right status for the publication state of this path
     * @throws GWTJahiaServiceException
     */
    public GWTJahiaPublicationInfo getPublicationInfo(String path, boolean full) throws GWTJahiaServiceException {
        JCRSessionWrapper session = retrieveCurrentSession();
        return publication.getPublicationInfo(path, Collections.singleton(session.getLocale().toString()), full, session);
    }


    public GWTJahiaWorkflowInfo getWorkflowInfo(String path) throws GWTJahiaServiceException {
        return workflow.getWorkflowInfo(path, retrieveCurrentSession());
    }

    /**
     * Get the publication status information for multiple pathes.
     *
     * @param pathes path to get publication info from
     * @return a GWTJahiaPublicationInfo object filled with the right status for the publication state of this path
     * @throws GWTJahiaServiceException
     */
    public Map<String, GWTJahiaPublicationInfo> getPublicationInfo(List<String> pathes) throws GWTJahiaServiceException {
        Map<String, GWTJahiaPublicationInfo> map = new HashMap<String, GWTJahiaPublicationInfo>();
        for (String path : pathes) {
            JCRSessionWrapper session = retrieveCurrentSession();
            map.put(path, publication.getPublicationInfo(path, Collections.singleton(session.getLocale().toString()), false, session));
        }

        return map;
    }

    /**
     * Get site languages
     *
     * @return
     * @throws GWTJahiaServiceException
     */
    public List<GWTJahiaLanguage> getSiteLanguages() throws GWTJahiaServiceException {
        return languages.getLanguages(getSite(), getRemoteJahiaUser(), getLocale());
    }

    /**
     * Get granted permission to the current user
     *
     * @return
     * @throws GWTJahiaServiceException
     */
    public List<GWTJahiaPermission> getGrantedPermissions() throws GWTJahiaServiceException {
        return rolesPermissions.getGrantedPermissions(getSite(), getRemoteJahiaUser());
    }


    /**
     * Get roles with permission
     *
     * @param principalKey
     * @return
     */
    public List<GWTJahiaRole> getRoles(String siteKey, boolean isGroup, String principalKey) throws GWTJahiaServiceException {
        return rolesPermissions.getRoles(siteKey, isGroup, principalKey);
    }

    /**
     * Get principal in role
     *
     * @param role
     * @return
     * @throws GWTJahiaServiceException
     */
    public List<GWTJahiaPrincipal> getPrincipalsInRole(GWTJahiaRole role) throws GWTJahiaServiceException {
        return rolesPermissions.getPrincipalsInRole(role);
    }


    /**
     * Get all roles and all permissions
     *
     * @param site the current site key or {@code null} if the server roles and permissions are requested
     * @return
     * @throws GWTJahiaServiceException
     */
    public GWTRolesPermissions getRolesAndPermissions(String site) throws GWTJahiaServiceException {
        return rolesPermissions.getRolesAndPermissions(site);
    }

    /**
     * add permission to role
     *
     * @param role
     * @throws GWTJahiaServiceException
     */
    public void addRolePermissions(GWTJahiaRole role, List<GWTJahiaPermission> permissions) throws GWTJahiaServiceException {
        rolesPermissions.addRolePermissions(role, permissions);
    }

    /**
     * remove permissin from role
     *
     * @param role
     * @param permissions
     * @throws GWTJahiaServiceException
     */
    public void removeRolePermissions(GWTJahiaRole role, List<GWTJahiaPermission> permissions) throws GWTJahiaServiceException {
        rolesPermissions.removeRolePermissions(role, permissions);
    }

    /**
     * Grant role toe users
     *
     * @param role
     */
    public void grantRoleToUser(GWTJahiaRole role, boolean isGroup, String principalKey) throws GWTJahiaServiceException {
        rolesPermissions.grantRoleToUser(role, isGroup, principalKey);
    }

    /**
     * Remove role to user
     *
     * @param role
     * @throws GWTJahiaServiceException
     */
    public void removeRoleToPrincipal(GWTJahiaRole role, boolean isGroup, String principalKey) throws GWTJahiaServiceException {
        rolesPermissions.removeRoleToPrincipal(role, isGroup, principalKey);
    }

    /**
     * Grant role to principals
     *
     * @param role
     * @param principals
     * @throws GWTJahiaServiceException in case of an error
     */
    public void grantRoleToPrincipals(GWTJahiaRole role, List<GWTJahiaPrincipal> principals) throws GWTJahiaServiceException {
        rolesPermissions.grantRoleToPrincipals(role, principals);
    }

    /**
     * remove role to principals
     *
     * @param role
     * @param principals
     * @throws GWTJahiaServiceException in case of an error
     */
    public void removeRoleToPrincipals(GWTJahiaRole role, List<GWTJahiaPrincipal> principals) throws GWTJahiaServiceException {
        rolesPermissions.removeRoleToPrincipals(role, principals);
    }

    /**
     * Gwt Highlighted
     *
     * @param original
     * @param amendment
     * @return
     */
    public String getHighlighted(String original, String amendment) {
        return diff.getHighlighted(original, amendment);
    }


    private List<String> getOpenPathsForRepository(String repositoryType) {
        return (List<String>) getRequest().getSession().getAttribute(navigation.SAVED_OPEN_PATHS + repositoryType);
    }

    private JahiaUser getUser() {
        return getRemoteJahiaUser();
    }

    public GWTJahiaPermission createPermission(String name, String group, String siteKey) throws GWTJahiaServiceException {
        return rolesPermissions.createPermission(name, group, siteKey);
    }

    /* (non-Javadoc)
     * @see org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService#getUrlMappings(org.jahia.ajax.gwt.client.data.node.GWTJahiaNode, java.lang.String)
     */

    public List<GWTJahiaUrlMapping> getUrlMappings(GWTJahiaNode node, String locale) throws GWTJahiaServiceException {
        try {
            return seo.getUrlMappings(node, locale, retrieveCurrentSession());
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    /* (non-Javadoc)
     * @see org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService#saveUrlMappings(org.jahia.ajax.gwt.client.data.node.GWTJahiaNode, java.util.Set, java.util.List)
     */

    public void saveUrlMappings(GWTJahiaNode node, Set<String> updatedLocales, List<GWTJahiaUrlMapping> mappings)
            throws GWTJahiaServiceException {
        try {
            seo.saveUrlMappings(node, updatedLocales, mappings, retrieveCurrentSession());
        } catch (ConstraintViolationException e) {
            // TODO handle it and display correct messages for the user
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(e.getMessage());
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    /**
     * Get analytics data
     *
     * @param query
     * @return
     */
    public List<GWTJahiaAnalyticsData> getAnalyticsData(GWTJahiaAnalyticsQuery query) throws GWTJahiaServiceException {
        if (!getSite().hasGoogleAnalyticsProfile()) {
            logger.debug("There is no configured google analytics account");
            return new ArrayList<GWTJahiaAnalyticsData>();
        }

        // get google analytics account
        final GoogleAnalyticsProfile googleAnalyticsProfile = getSite().getGoogleAnalyticsProfile();

        // get its parameter
        final String gaAccount = googleAnalyticsProfile.getAccount();
        final String login = googleAnalyticsProfile.getLogin();
        final String pwd = googleAnalyticsProfile.getPassword();


        // check parameters
        if (gaAccount == null) {
            logger.error("There is no google analytics account configured");
            throw new GWTJahiaServiceException("There is no google analytics account configured");
        }

        // get data
        return analytics.queryData(login, pwd, gaAccount, query);
    }

    public void synchro(Map<String, String> pathsToSyncronize) throws GWTJahiaServiceException {
        contentManager.synchro(pathsToSyncronize, retrieveCurrentSession());
    }


    /**
     * @param seoHelper the seoHelper to set
     */
    public void setSeo(SeoHelper seoHelper) {
        this.seo = seoHelper;
    }

    public void setAnalytics(AnalyticsHelper analyticsHelper) {
        this.analytics = analyticsHelper;
    }

}