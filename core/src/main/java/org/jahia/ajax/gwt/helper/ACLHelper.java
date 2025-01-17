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
package org.jahia.ajax.gwt.helper;

import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACE;
import org.jahia.services.content.decorator.JCRGroupNode;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.usermanager.JahiaGroupManagerService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: toto
 * Date: Nov 26, 2008 - 7:40:56 PM
 */
public class ACLHelper {

    private JahiaGroupManagerService jahiaGroupManagerService;

    public void setJahiaGroupManagerService(JahiaGroupManagerService jahiaGroupManagerService) {
        this.jahiaGroupManagerService = jahiaGroupManagerService;
    }

    public GWTJahiaNodeACE createUsersGroupACE(List<String> permissions, boolean grand, JCRSiteNode site) {
        JCRGroupNode usersGroup = jahiaGroupManagerService.lookupGroup(site.getSiteKey(), JahiaGroupManagerService.USERS_GROUPNAME);
        GWTJahiaNodeACE ace = new GWTJahiaNodeACE();
        ace.setPrincipalType('g');
        ace.setPrincipal(usersGroup.getName());
        ace.setPrincipalKey(usersGroup.getPath());
        Map<String, Boolean> permissionsMap = new HashMap<String, Boolean>();
        for (String perm : permissions) {
            if (grand) {
                permissionsMap.put(perm, true);
            } else {
                permissionsMap.put(perm, false);
            }
        }
        ace.setRoles(permissionsMap);
        ace.setInheritedRoles(new HashMap<String, Boolean>());
        ace.setInherited(false);
        return ace;
    }


}
