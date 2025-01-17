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
package org.jahia.ajax.gwt.client.widget.edit.sidepanel;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridSelectionModel;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule;

import java.util.List;

/**
 *
 * User: toto
 * Date: Oct 27, 2010
 * Time: 2:09:55 PM
 *
 */
class TreeGridClickSelectionModel extends TreeGridSelectionModel<GWTJahiaNode> {
    private TreeGridSelectionModel<GWTJahiaNode> rightClickSelectionModel;

    TreeGridClickSelectionModel() {
        rightClickSelectionModel = new TreeGridSelectionModel<GWTJahiaNode>() {
            protected void onSelectChange(GWTJahiaNode model, boolean select) {
            }

            protected void setLastFocused(GWTJahiaNode lastFocused) {
            }
        };
        rightClickSelectionModel.setSelectionMode(Style.SelectionMode.SINGLE);
    }

    @Override
    protected void handleMouseDown(GridEvent<GWTJahiaNode> e) {
        if (!e.isRightClick()) {
            super.handleMouseDown(e);
        } else {
            if (!MainModule.isGlobalSelectionDisabled()) {
                if (selectionMode != Style.SelectionMode.SINGLE && isSelected(listStore.getAt(e.getRowIndex()))) {
                    return;
                }
                if (e.isRightClick()) {
                    rightClickSelectionModel.select(e.getModel(), false);
                }
            }
        }
    }

    @Override protected void handleMouseClick(GridEvent<GWTJahiaNode> e) {
        if (!e.isRightClick()) {
            super.handleMouseClick(e);
        }
    }

    public TreeGridSelectionModel<GWTJahiaNode> getRightClickSelectionModel() {
        return rightClickSelectionModel;
    }

    @Override protected void setLastFocused(GWTJahiaNode lastFocused) {
    }

    @Override public void bindGrid(Grid grid) {
        super.bindGrid(grid);
        rightClickSelectionModel.bind(grid.getStore());
    }

    @Override public void select(int start, int end, boolean keepExisting) {
        super.select(start, end, keepExisting);
        rightClickSelectionModel.select(start, end, keepExisting);
    }

    @Override public void select(List<GWTJahiaNode> items, boolean keepExisting) {
        super.select(items, keepExisting);
        rightClickSelectionModel.select(items, keepExisting);
    }

    @Override public void refresh() {
        super.refresh();
        rightClickSelectionModel.refresh();
    }
}
