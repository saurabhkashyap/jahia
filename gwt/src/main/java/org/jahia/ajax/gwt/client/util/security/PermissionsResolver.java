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
package org.jahia.ajax.gwt.client.util.security;

import java.util.Iterator;
import java.util.List;

/**
 * Provides different strategies to resolve permissions from a list.
 *
 * @author cmoitrier
 */
public enum PermissionsResolver {

    /** All given permissions must match with the {@link Matcher} */
    MATCH_ALL {
        @Override
        public boolean resolve(List<String> permissions, Matcher matcher) {
            if (permissions.isEmpty()) {
                return true;
            }

            boolean result = true;
            for (Iterator<String> it = permissions.iterator(); it.hasNext() && result == true; ) {
                String permission = it.next();
                result = matcher.matches(permission);
            }
            return result;
        }
    },

    /** At least one of the given permissions must match with the {@link Matcher} */
    MATCH_ANY {
        @Override
        public boolean resolve(List<String> permissions, Matcher matcher) {
            if (permissions.isEmpty()) {
                return true;
            }

            for (Iterator<String> it = permissions.iterator(); it.hasNext(); ) {
                String permission = it.next();
                if (matcher.matches(permission)) {
                    return true;
                }
            }
            return false;
        }

    };

    public abstract boolean resolve(List<String> permissions, Matcher matcher);

    public interface Matcher {
        boolean matches(String permission);
    }

}
