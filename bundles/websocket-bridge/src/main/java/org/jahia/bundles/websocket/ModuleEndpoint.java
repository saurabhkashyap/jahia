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
package org.jahia.bundles.websocket;

import org.osgi.framework.ServiceObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * This is the endpoint wrapper to wrap the module endpoint instance.
 */
public class ModuleEndpoint extends Endpoint {

    private static final Logger logger = LoggerFactory.getLogger(ModuleEndpoint.class);
    private final ServiceObjects<Endpoint> endpointServiceRef;
    private final Endpoint endpoint;
    private final Set<Session> sessions = new HashSet<>();
    private volatile boolean closed;

    public ModuleEndpoint(ServiceObjects<Endpoint> endpointServiceRef) {
        this.endpointServiceRef = endpointServiceRef;
        this.endpoint = endpointServiceRef.getService();
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        if (closed) {
            return;
        }

        endpoint.onClose(session, closeReason);
        sessions.remove(session);
        endpointServiceRef.ungetService(endpoint);
    }

    @Override
    public void onError(Session session, Throwable throwable) {
        if (closed) {
            return;
        }

        endpoint.onError(session, throwable);
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        if (closed) {
            return;
        }

        endpoint.onOpen(session, endpointConfig);
        sessions.add(session);
    }

    protected void close() {
        closed = true;

        Iterator<Session> iterator = sessions.iterator();

        while (iterator.hasNext()) {
            Session session = iterator.next();
            iterator.remove();

            try {
                CloseReason closeReason = new CloseReason(CloseReason.CloseCodes.GOING_AWAY, "Service is unregistered");
                session.close(closeReason);
                endpoint.onClose(session, closeReason);
                endpointServiceRef.ungetService(endpoint);
            } catch (IOException e) {
                logger.error("Unable to close session", e);
            }
        }
    }
}