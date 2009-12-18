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
package org.jahia.engines.importexport;

import org.apache.log4j.Logger;
import org.jahia.bin.JahiaAdministration;
import org.jahia.content.ContentObject;
import org.jahia.content.ContentObjectKey;
import org.jahia.data.JahiaData;
import org.jahia.engines.EngineToolBox;
import org.jahia.engines.JahiaEngine;
import org.jahia.engines.validation.EngineValidationHelper;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.importexport.ImportExportService;
import org.jahia.services.lock.LockKey;
import org.jahia.services.lock.LockPrerequisites;
import org.jahia.services.lock.LockRegistry;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.SiteLanguageSettings;
import org.jahia.services.version.EntryLoadRequest;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.ItemNotFoundException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.net.HttpURLConnection;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 13 sept. 2005
 * Time: 12:03:42
 * To change this template use File | Settings | File Templates.
 */
public class ExportEngine implements JahiaEngine {
    private static Logger logger = Logger.getLogger(ExportEngine.class);
    public static final String ENGINE_NAME = "export";

    private EngineToolBox toolBox;

    public ExportEngine() {
        toolBox = EngineToolBox.getInstance();
    }

    public boolean authoriseRender(ProcessingContext processingContext) {
        return true;
    }

    public String getName() {
        return ENGINE_NAME;
    }

    public String renderLink(ProcessingContext processingContext, Object theObj) throws JahiaException {
//        ContentObject object = (ContentObject) theObj;
        return processingContext.composeEngineUrl(ENGINE_NAME);
    }

    public boolean needsJahiaData(ProcessingContext processingContext) {
        return false;
    }

    public EngineValidationHelper handleActions(ProcessingContext processingContext, JahiaData jData) throws JahiaException {
        try {
            Map engineMap = new HashMap();

            export(processingContext);
            engineMap.put(JahiaEngine.RENDER_TYPE_PARAM, new Integer(-1));

            toolBox.displayScreen(processingContext, (Map) engineMap);
        } catch (Exception e) {
            throw new JahiaException("Exception occured", "Exception occured", JahiaException.ENGINE_ERROR, JahiaException.ERROR_SEVERITY, e);
        }
        return null;
    }

    private void export(ProcessingContext processingContext) throws Exception {
        ParamBean jParams = (ParamBean) processingContext;
        HttpServletResponse resp = jParams.getRealResponse();
        ServletOutputStream outputStream = resp.getOutputStream();
        //make sure this file is not cached by the client (or a proxy middleman)
        resp.setHeader("Expires", "Thu, 01 Jan 1970 00:00:00 GMT");
        resp.setHeader("Pragma", "no-cache");
        resp.setHeader("Cache-Control", "no-cache");

        Map params = new HashMap();

        params.put(ImportExportService.VIEW_CONTENT, Boolean.valueOf(!"false".equals(processingContext.getParameter("viewContent"))));
        params.put(ImportExportService.VIEW_VERSION, Boolean.valueOf("true".equals(processingContext.getParameter("viewVersion"))));
        params.put(ImportExportService.VIEW_ACL, Boolean.valueOf(!"false".equals(processingContext.getParameter("viewAcl"))));
        params.put(ImportExportService.VIEW_METADATA, Boolean.valueOf(!"false".equals(processingContext.getParameter("viewMetadata"))));
        params.put(ImportExportService.VIEW_JAHIALINKS, Boolean.valueOf(!"false".equals(processingContext.getParameter("viewLinks"))));
        params.put(ImportExportService.VIEW_WORKFLOW, Boolean.valueOf("true".equals(processingContext.getParameter("viewWorkflow"))));
        boolean enforceLanguage = "true".equals(processingContext.getParameter("enforceLanguage"));


        try {
            ImportExportService ie = ServicesRegistry.getInstance().getImportExportService();

            if ("all".equals(processingContext.getParameter("exportformat"))) {
                if (!processingContext.getUser().isRoot()) {
                    resp.setStatus(HttpURLConnection.HTTP_UNAUTHORIZED);
                    return;
                }

                resp.setContentType("application/zip");
                params.put(ImportExportService.INCLUDE_ALL_FILES, Boolean.TRUE);
                params.put(ImportExportService.INCLUDE_TEMPLATES, Boolean.TRUE);
                params.put(ImportExportService.INCLUDE_SITE_INFOS, Boolean.TRUE);
                params.put(ImportExportService.INCLUDE_DEFINITIONS, Boolean.TRUE);
                params.put(ImportExportService.VIEW_WORKFLOW, Boolean.TRUE);
                params.put(ImportExportService.VIEW_PID, Boolean.TRUE);

                ie.exportAll(outputStream, params, processingContext);
                outputStream.close();
                return;
            } else if ("site".equals(processingContext.getParameter("exportformat"))) {
                if (!processingContext.getUser().isRoot()) {
                    resp.setStatus(HttpURLConnection.HTTP_UNAUTHORIZED);
                    return;
                }

                List sites = new ArrayList();
                String[] sitekeys = processingContext.getParameterValues("sitebox");
                if (sitekeys != null) {
                    for (int i = 0; i < sitekeys.length; i++) {
                        String sitekey = sitekeys[i];
                        JahiaSite site = ServicesRegistry.getInstance().getJahiaSitesService().getSiteByKey(sitekey);
                        sites.add(site);
                    }
                }

                if (sites.isEmpty()) {
                    JahiaAdministration.doRedirect(jParams.getRequest(), jParams.getResponse(), jParams.getSession(),
                            JahiaAdministration.JSP_PATH + "no_sites_selected.jsp");
                } else {
                    resp.setContentType("application/zip");
                    params.put(ImportExportService.INCLUDE_ALL_FILES, Boolean.TRUE);
                    params.put(ImportExportService.INCLUDE_TEMPLATES, Boolean.TRUE);
                    params.put(ImportExportService.INCLUDE_SITE_INFOS, Boolean.TRUE);
                    params.put(ImportExportService.INCLUDE_DEFINITIONS, Boolean.TRUE);
                    params.put(ImportExportService.VIEW_WORKFLOW, Boolean.TRUE);
                    params.put(ImportExportService.VIEW_PID, Boolean.TRUE);

                    ie.exportSites(outputStream, params, processingContext, sites);
                    outputStream.close();
                }
                return;
            } else if ("doc".equals(processingContext.getParameter("exportformat"))) {
                resp.setContentType("text/xml");
                JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
                Node node = session.getNode(processingContext.getParameter("path"));
                session.exportDocumentView(node.getPath(), outputStream, true, false);
                outputStream.close();
                return;
            } else if ("zip".equals(processingContext.getParameter("exportformat"))) {
                resp.setContentType("application/zip");
                JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
                JCRNodeWrapper node = session.getNode(processingContext.getParameter("path"));
                ie.exportZip(node, outputStream);
                outputStream.close();
                return;
            }
        } catch (Exception e) {
            logger.error("Exception during export", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
