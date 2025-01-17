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
//
//  JahiaApplicationsService
//  EV      29.11.2000
//
//  getAppOutput( fieldID, appID, params )
//

package org.jahia.services.applications;

import org.jahia.exceptions.JahiaException;
import org.jahia.services.JahiaService;
import org.jahia.services.usermanager.JahiaUser;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This service generates the dispatching and aggregation on an application.
 * This functionality is central to the portal behavior of Jahia, allowing it
 * to display multiple applications on a web page and interact with them
 * simultaneously.
 *
 * @author Serge Huber
 * @author Eric Vassalli
 * @version 1.0
 */
public abstract class DispatchingService extends JahiaService {

    /**
     * Dispatches processing to an application, and retrieves it's output for
     * Jahia to aggregate
     *
     * @param fieldID     identifier of Jahia's field
     * @param entryPointIDStr application identifier passed as a String (converted
     *                    from an integer)
     * @param jahiaUser the Jahia user that will be passed to the portlet when dispatching
     * @param httpServletRequest the request object that will be passed to the portlet
     * @param httpServletResponse
     * @param servletContext
     * @param workspaceName the workspace name against which to check for mode and various other permissions
     *
     * @throws JahiaException generated if there was a problem dispatching,
     *                        during processing of the application, or when recuperating the application's
     *                        output.
     * @return String containing the output of the application
     */
    public abstract String getAppOutput (int fieldID, String entryPointIDStr, JahiaUser jahiaUser, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, ServletContext servletContext, String workspaceName)
            throws JahiaException;

} // end JahiaApplicationsService
