/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.ajax.gwt.commons.server;

import com.google.gwt.user.client.rpc.RemoteService;
import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.decorator.JCRUserNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jahia.ajax.gwt.client.core.SessionExpirationException;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.preferences.user.UserPreferencesHelper;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.utils.i18n.Messages;
import org.springframework.web.context.ServletContextAware;

import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Locale;

/**
 * Base class for Jahia GWT services.
 *
 * @author Sergiy Shyrkov
 */
public abstract class JahiaRemoteService implements RemoteService, ServletContextAware, RequestResponseAware {

    private static final transient Logger logger = LoggerFactory.getLogger(JahiaRemoteService.class);

    private HttpServletRequest request;
    private HttpServletResponse response;
    private ServletContext servletContext;

    /**
     * Retrieve current session
     *
     * @return
     * @throws GWTJahiaServiceException
     */
    protected JCRSessionWrapper retrieveCurrentSession() throws GWTJahiaServiceException {
        return retrieveCurrentSession(getLocale());
    }

    /**
     * Retrieve current session by locale
     *
     * @param locale
     * @return
     * @throws GWTJahiaServiceException
     */
    protected JCRSessionWrapper retrieveCurrentSession(Locale locale) throws GWTJahiaServiceException {
        return retrieveCurrentSession(getWorkspace(), locale, false);
    }

    /**
     * Retrieve current session by workspace
     *
     * @return
     * @throws GWTJahiaServiceException
     */
    protected JCRSessionWrapper retrieveCurrentSession(String workspace, Locale locale, boolean useSiteFallbackLanguage) throws GWTJahiaServiceException {
        checkSession();
        try {
            return JCRSessionFactory.getInstance().getCurrentUserSession(workspace, locale, useSiteFallbackLanguage ? getFallbackLocale() : null);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(Messages.getInternal("label.gwt.error.cannot.open.user.session", getUILocale()));
        }
    }

    /**
     * Get current locale
     *
     * @return
     */
    protected Locale getLocale() {
        Locale locale = LanguageCodeConverters.languageCodeToLocale(request.getParameter("lang"));
        return locale;
    }

    private Locale getFallbackLocale() throws GWTJahiaServiceException {
        Locale fallback = null;

        try {
            if (request.getParameter("site") == null) {
                return null;
            }
            JCRSiteNode site = (JCRSiteNode) JCRSessionFactory.getInstance().getCurrentUserSession(getWorkspace()).getNodeByUUID(request.getParameter("site"));

            if (site.isMixLanguagesActive()) {
                fallback = LanguageCodeConverters.getLocaleFromCode(site.getDefaultLanguage());
            }
            return fallback;
        } catch (ItemNotFoundException e) {
            return null;
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(Messages.getInternal("label.gwt.error.cannot.open.user.session", getUILocale()));
        }
    }

    /**
     * Get site
     *
     * @return
     */
    protected JCRSiteNode getSite() {
        try {
            if (!StringUtils.isEmpty(request.getParameter("site"))) {
                return (JCRSiteNode) retrieveCurrentSession().getNodeByUUID(request.getParameter("site"));
            } else {
                return (JCRSiteNode) retrieveCurrentSession().getNode(JCRContentUtils.getSystemSitePath());
            }
        } catch (Exception e) {
            logger.error("Cannot get site",e);
        }
        return null;
    }

    /**
     * Get workspace
     *
     * @return
     */
    protected String getWorkspace() {
        if (!StringUtils.isEmpty(request.getParameter("workspace"))) {
            return request.getParameter("workspace");
        }
        return "default";
    }

    /**
     * Get current UI locale
     *
     * @return
     */
    protected Locale getUILocale() throws GWTJahiaServiceException {
        Locale sessionLocale = (Locale) getSession().getAttribute(Constants.SESSION_UI_LOCALE);
        Locale locale = sessionLocale != null ? UserPreferencesHelper.getPreferredLocale(getRemoteJahiaUserNode(), sessionLocale) : UserPreferencesHelper.getPreferredLocale(getRemoteJahiaUserNode(), LanguageCodeConverters.resolveLocaleForGuest(request));
        if (locale == null) {
            if(JahiaUserManagerService.isNotGuest(getRemoteJahiaUser().getLocalPath())) {
                locale = UserPreferencesHelper.getPreferredLocale(getRemoteJahiaUserNode());
            }
            if (locale == null) {
                locale = getLocale();
            }
            request.getSession(false).setAttribute(Constants.SESSION_UI_LOCALE, locale);
        }
        return locale;
    }

    /**
     * Get remote jahiaUser
     *
     * @return
     */
    protected JahiaUser getRemoteJahiaUser() {
        return JCRSessionFactory.getInstance().getCurrentUser();
    }

    protected JCRUserNode getRemoteJahiaUserNode() {
        JahiaUser jUser = getRemoteJahiaUser();
        if (jUser == null) {
            return null;
        }
        return JahiaUserManagerService.getInstance().lookupUserByKey(jUser.getUserKey());
    }

    /**
     * Get remote user
     *
     * @return
     */
    protected String getRemoteUser() {
        //retrieve user
        JahiaUser jUser = getRemoteJahiaUser();
        if (jUser != null) {
            return jUser.getUserKey();
        }
        return null;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    protected HttpSession getSession() throws SessionExpirationException {
        checkSession();
        return getRequest().getSession();
    }

    private void checkSession() throws SessionExpirationException {
        if (request.getSession(false) == null) {
            throw new SessionExpirationException();
        }
    }


}
