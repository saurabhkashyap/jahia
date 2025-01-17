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
package org.jahia.bundles.jcrcommands.jcr;

import org.apache.commons.lang.StringUtils;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.Session;
import org.apache.karaf.shell.support.table.Col;
import org.apache.karaf.shell.support.table.ShellTable;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.JCRValueWrapper;

import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import java.util.ArrayList;
import java.util.List;

/**
 * Get property command
 */
@Command(scope = "jcr", name = "prop-get")
@Service
@SuppressWarnings("java:S106")
public class PropGetCommand extends JCRCommandSupport implements Action {

    @Argument(description = "Name")
    @Completion(JCRPropCompleter.class)
    private String name;

    @Reference
    Session session;

    @Override
    public Object execute() throws Exception {
        final ShellTable table = new ShellTable();
        table.column(new Col("Name"));
        table.column(new Col("Type"));
        table.column(new Col("Value"));

        JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, getCurrentWorkspace(session), null, jcrsession -> {
            JCRNodeWrapper n = jcrsession.getNode(getCurrentPath(session));
            PropertyIterator properties = name == null ? n.getProperties() : n.getProperties(name);
            while (properties.hasNext()) {
                JCRPropertyWrapper next = (JCRPropertyWrapper) properties.nextProperty();
                String value;
                if (next.isMultiple()) {
                    List<String> l = new ArrayList<>();
                    for (JCRValueWrapper wrapper : next.getValues()) {
                        l.add(wrapper.getString());
                    }
                    value = StringUtils.join(l, ", ");
                } else {
                    value = next.getValue().getString();
                }
                table.addRow().addContent(next.getName(), PropertyType.nameFromValue(next.getType()), value);
            }
            return null;
        });

        table.print(System.out, true);

        return null;
    }
}
