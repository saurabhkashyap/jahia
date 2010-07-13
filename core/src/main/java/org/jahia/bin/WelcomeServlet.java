package org.jahia.bin;

import org.apache.commons.collections.iterators.EnumerationIterator;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.bin.errors.DefaultErrorHandler;
import org.jahia.bin.errors.ErrorHandler;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesBaseService;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.settings.SettingsBean;

import javax.jcr.AccessDeniedException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Servlet for the first entry point in Jahia portal that performs a client-side redirect
 * to the home page of the appropriate site.
 * User: toto
 * Date: Apr 26, 2010
 * Time: 5:49:14 PM
 */
public class WelcomeServlet extends HttpServlet {

    /** The serialVersionUID. */
    private static final long serialVersionUID = -2055161334153523152L;
    
    private static final transient Logger logger = Logger.getLogger(ProcessingContext.class);
    
    private static final String DEFAULT_LOCALE = Locale.ENGLISH.toString();

    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        defaultRedirect(request, response, getServletContext());
    }

    protected void defaultRedirect(HttpServletRequest request, HttpServletResponse response, ServletContext context) throws IOException, ServletException {
        try {
            request.getSession(true);
            JahiaSite site = resolveSite(request);
            if (site == null) {
                response.sendRedirect(request.getContextPath() + "/administration");
            } else {
                String language = resolveLanguage(request, site);
                String base;

                final String jcrPath = "/sites/" + site.getSiteKey() + "/home";

                try {
                    JCRStoreService.getInstance().getSessionFactory()
                            .getCurrentUserSession(Constants.LIVE_WORKSPACE).getNode(jcrPath);
                    base = request.getContextPath() + Render.getRenderServletPath() + "/"
                            + Constants.LIVE_WORKSPACE + "/" + language;
                } catch (PathNotFoundException e) {
                    try {
                        JCRStoreService.getInstance().getSessionFactory().getCurrentUserSession()
                                .getNode(jcrPath);
                        base = request.getContextPath() + Edit.getEditServletPath() + "/"
                                + Constants.EDIT_WORKSPACE + "/" + language;
                    } catch (PathNotFoundException e2) {
                        JCRTemplate.getInstance().doExecuteWithSystemSession(
                                new JCRCallback<Object>() {
                                    public Object doInJCR(JCRSessionWrapper session)
                                            throws RepositoryException {
                                        session.getNode(jcrPath);
                                        throw new AccessDeniedException();
                                    }
                                });
                        throw new AccessDeniedException();
                    }
                }

                response.sendRedirect(base + jcrPath + ".html");

            }
        } catch (Exception e) {
            List<ErrorHandler> handlers = ServicesRegistry.getInstance()
                    .getJahiaTemplateManagerService().getErrorHandler();
            for (ErrorHandler handler : handlers) {
                if (handler.handle(e, request, response)) {
                    return;
                }
            }
            DefaultErrorHandler.getInstance().handle(e, request, response);
        }
    }

    protected JahiaSite resolveSite(HttpServletRequest request) throws JahiaException {
        JahiaSitesService siteService = JahiaSitesBaseService.getInstance();
        JahiaSite resolvedSite = siteService.getSiteByServerName(request.getServerName());
        if (resolvedSite == null) {
            resolvedSite = siteService.getDefaultSite();
        }
        return resolvedSite;
    }
    
    protected String resolveLanguage(HttpServletRequest request, JahiaSite site)
            throws JahiaException {
        List<Locale> newLocaleList = new ArrayList<Locale>();
        List<Locale> siteLanguages = Collections.emptyList();
        try {
            if (site != null) {
                siteLanguages = site.getLanguagesAsLocales();
            }
        } catch (Exception t) {
            logger.debug("Exception while getting language settings as locales", t);
        }

        // retrieve the browser locales
        for (@SuppressWarnings("unchecked")
        Iterator<Locale> browserLocales = new EnumerationIterator(request.getLocales()); browserLocales
                .hasNext();) {
            final Locale curLocale = browserLocales.next();
            if (siteLanguages.contains(curLocale)) {
                if (!newLocaleList.contains(curLocale)) {
                    newLocaleList.add(curLocale);
                }
            } else if (!StringUtils.isEmpty(curLocale.getCountry())) {
                final Locale langOnlyLocale = new Locale(curLocale.getLanguage());
                if (siteLanguages.contains(langOnlyLocale)) {
                    if (!newLocaleList.contains(langOnlyLocale)) {
                        newLocaleList.add(langOnlyLocale);
                    }
                }
            }
        }

        String language = DEFAULT_LOCALE;
        if (!newLocaleList.isEmpty()) {
            language = newLocaleList.get(0).toString();
        } else if (!siteLanguages.isEmpty()){
            language = siteLanguages.get(0).toString();            
        } else if (!StringUtils.isEmpty(SettingsBean.getInstance().getDefaultLanguageCode())) {
            language = SettingsBean.getInstance().getDefaultLanguageCode();
        }
        return language;
    }
}