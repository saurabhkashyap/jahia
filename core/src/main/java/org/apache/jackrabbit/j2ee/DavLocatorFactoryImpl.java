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
package org.apache.jackrabbit.j2ee;

import org.apache.jackrabbit.webdav.AbstractLocatorFactory;

/**
 * 
 * User: toto
 * Date: 19 févr. 2008
 * Time: 16:47:37
 * 
 */
public class DavLocatorFactoryImpl extends AbstractLocatorFactory  {


    /**
     * Create a new factory
     *
     * @param pathPrefix Prefix, that needs to be removed in order to retrieve
     *                   the path of the repository item from a given <code>DavResourceLocator</code>.
     */
    public DavLocatorFactoryImpl(String pathPrefix) {
        super(pathPrefix);
    }

    /**
     *
     * @see org.apache.jackrabbit.webdav.AbstractLocatorFactory#getRepositoryPath(String, String)
     */
    protected String getRepositoryPath(String resourcePath, String wspPath) {
        if (resourcePath == null) {
            return resourcePath;
        }

        if (resourcePath.equals(wspPath) || startsWithWorkspace(resourcePath, wspPath)) {
            String repositoryPath = resourcePath.substring(wspPath.length());
            repositoryPath = repositoryPath.replace("[","\\5B");
            repositoryPath = repositoryPath.replace("]","\\5C");
            repositoryPath = repositoryPath.replace("'","\\27");

            return (repositoryPath.length() == 0) ? "/" : repositoryPath;
        } else {
            throw new IllegalArgumentException("Unexpected format of resource path.");
        }
    }

    /**
     *
     * @see org.apache.jackrabbit.webdav.AbstractLocatorFactory#getResourcePath(String, String)
     */
    protected String getResourcePath(String repositoryPath, String wspPath) {
        if (repositoryPath == null) {
            throw new IllegalArgumentException("Cannot build resource path from 'null' repository path");
        }
        repositoryPath = repositoryPath.replace("\\5B","[");
        repositoryPath = repositoryPath.replace("\\5C","]");
        repositoryPath = repositoryPath.replace("\\27","'");
        return (startsWithWorkspace(repositoryPath, wspPath)) ? repositoryPath : wspPath + repositoryPath;
    }

    private boolean startsWithWorkspace(String repositoryPath, String wspPath) {
        if (wspPath == null) {
            return true;
        } else {
            return repositoryPath.startsWith(wspPath + "/");
        }
    }
}
