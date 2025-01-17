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
package org.jahia.services.modulemanager.spi;

import org.jahia.services.modulemanager.ModuleManagementException;

/**
 * Indicates errors that happen during remote invocation of another cluster node.
 */
public class RemoteModuleManagementException extends ModuleManagementException {

    private static final long serialVersionUID = -5591954704941402764L;
    private final String nodeId;

    /**
     * Create an exception instance.
     *
     * @param message Error message
     * @param cause Cause if any
     * @param nodeId ID of the cluster node that failed remote invocation
     */
    public RemoteModuleManagementException(String message, Throwable cause, String nodeId) {
        super(message, cause);
        this.nodeId = nodeId;
    }

    /**
     * Create an exception instance.
     *
     * @param cause Cause if any
     * @param nodeId ID of the cluster node that failed remote invocation
     */
    public RemoteModuleManagementException(Throwable cause, String nodeId) {
        this(null, cause, nodeId);
    }

    /**
     * @return ID of the cluster node that failed remote invocation
     */
    public String getNodeId() {
        return nodeId;
    }
}
