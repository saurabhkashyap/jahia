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
package org.jahia.ajax.gwt.client.widget.poller;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.atmosphere.gwt20.client.managed.RPCEvent;
import org.jahia.ajax.gwt.client.util.EventDataSupplier;

public class TaskEvent extends RPCEvent implements Serializable, EventDataSupplier {

    private static final long serialVersionUID = 7742645002324255207L;
    private String newTask;
    private String endedTask;
    private String endedWorkflow;

    @Deprecated
    public Integer getNumberOfTasks() {
        return 0;
    }

    @Deprecated
    public void setNumberOfTasks(Integer numberOfTasks) {
        // do nothing
    }

    public String getNewTask() {
        return newTask;
    }

    public void setNewTask(String newTaskDisplayName) {
        this.newTask = newTaskDisplayName;
    }

    public String getEndedTask() {
        return endedTask;
    }

    public void setEndedTask(String endedTask) {
        this.endedTask = endedTask;
    }

    public String getEndedWorkflow() {
        return endedWorkflow;
    }

    public void setEndedWorkflow(String endedWorkflowDisplayName) {
        this.endedWorkflow = endedWorkflowDisplayName;
    }

    @Override
    public Map<String, Object> getEventData() {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("type", "workflowTask");
        data.put("endedTask", getEndedTask());
        data.put("endedWorkflow", getEndedWorkflow());
        data.put("newTask", getNewTask());
        return data;
    }
}