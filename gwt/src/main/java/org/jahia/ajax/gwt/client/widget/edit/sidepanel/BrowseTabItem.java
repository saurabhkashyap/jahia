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
import com.extjs.gxt.ui.client.dnd.DND;
import com.extjs.gxt.ui.client.dnd.TreeGridDropTarget;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTSidePanelTab;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.widget.NodeColumnConfigList;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.EditModeDNDListener;
import org.jahia.ajax.gwt.client.widget.node.GWTJahiaNodeTreeFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository browser tab.
 * User: toto
 * Date: Dec 21, 2009
 * Time: 2:22:30 PM
 */
abstract class BrowseTabItem extends SidePanelTabItem {
    protected List<String> folderTypes = new ArrayList<String>();
    protected List<String> excludedFolderTypes = new ArrayList<String>();
    private List<String> paths = new ArrayList<String>();

    protected transient LayoutContainer treeContainer;
    protected transient TreeGrid<GWTJahiaNode> tree;
    protected transient TreeGridDropTarget treeDropTarget;
    protected transient String repositoryType;
    protected transient GWTJahiaNodeTreeFactory factory;

    @Override
    public TabItem create(GWTSidePanelTab config) {
        super.create(config);
        VBoxLayout l = new VBoxLayout();
        l.setVBoxLayoutAlign(VBoxLayout.VBoxLayoutAlign.STRETCH);
        tab.setLayout(l);

        treeContainer = new LayoutContainer();
        treeContainer.setBorders(false);
        treeContainer.setScrollMode(Style.Scroll.AUTO);
        treeContainer.setLayout(new FitLayout());
        factory = new GWTJahiaNodeTreeFactory(paths);
        factory.setNodeTypes(this.folderTypes);
        factory.setHiddenTypes(this.excludedFolderTypes);
        factory.setFields(config.getTreeColumnKeys());

        NodeColumnConfigList columns = new NodeColumnConfigList(config.getTreeColumns());
        columns.init();
        columns.get(0).setRenderer(NodeColumnConfigList.NAME_TREEGRID_RENDERER);

        tree = factory.getTreeGrid(new ColumnModel(columns));
        tree.setAutoExpandColumn(columns.getAutoExpand());
        tree.getTreeView().setRowHeight(25);
        tree.getTreeView().setForceFit(true);
        tree.setHeight("100%");
        tree.setIconProvider(ContentModelIconProvider.getInstance());

        treeContainer.add(tree);

        VBoxLayoutData treeVBoxData = new VBoxLayoutData();
        treeVBoxData.setFlex(1);

        tab.add(treeContainer, treeVBoxData);

        return tab;
    }

    @Override
    public void initWithLinker(EditLinker linker) {
        super.initWithLinker(linker);
        if (linker.getConfig().isDragAndDropEnabled()) {
            treeDropTarget = new BrowseTreeGridDropTarget();
            treeDropTarget.addDNDListener(linker.getDndListener());
            treeDropTarget.setAllowDropOnLeaf(true);
            treeDropTarget.setAllowSelfAsSource(false);
            treeDropTarget.setAutoExpand(true);
            treeDropTarget.setFeedback(DND.Feedback.APPEND);
        }
    }

    class BrowseTreeGridDropTarget extends TreeGridDropTarget {
        public BrowseTreeGridDropTarget() {
            super(BrowseTabItem.this.tree);
        }

        @Override
        protected void onDragEnter(DNDEvent e) {
            super.onDragEnter(e);
            setStatus(e);
        }

        private void setStatus(DNDEvent e) {
            if (EditModeDNDListener.SIMPLEMODULE_TYPE.equals(e.getStatus().getData(EditModeDNDListener.SOURCE_TYPE))) {
                List<GWTJahiaNode> nodes = e.getStatus().getData(EditModeDNDListener.SOURCE_NODES);
                if (acceptNode(nodes.get(0))) {
                    e.getStatus().setData(EditModeDNDListener.TARGET_TYPE, EditModeDNDListener.BROWSETREE_TYPE);
                    e.getStatus().setStatus(true);
                } else {
                    e.getStatus().setStatus(false);
                }
            } else {
                e.getStatus().setStatus(false);
            }
            e.setCancelled(false);
        }

        @Override
        protected void showFeedback(DNDEvent e) {
            super.showFeedback(e);
            setStatus(e);
            if (activeItem != null) {
                GWTJahiaNode activeNode = (GWTJahiaNode) activeItem.getModel();
                e.getStatus().setData(EditModeDNDListener.TARGET_PATH, activeNode.get("path"));
            } else {
                e.getStatus().setData(EditModeDNDListener.TARGET_PATH, null);
            }
        }

        public AsyncCallback<Object> getCallback() {
            AsyncCallback<Object> callback = new BaseAsyncCallback<Object>() {

                @Override
                public void onSuccess(Object o) {
                    Map<String, Object> data = new HashMap<String, Object>();
                    data.put("event", "browseTreeGridDrop");
                    refresh(data);
                }

            };
            return callback;
        }

    }

    @Override
    public boolean needRefresh(Map<String, Object> data) {
        return data.containsKey("event")
                && ("unmount".equals(data.get("event"))
                || "fileUploaded".equals(data.get("event"))
                || "publicationSuccess".equals(data.get("event"))
                || "browseTreeGridDrop".equals(data.get("event")));
    }

    @Override
    public void doRefresh() {
        List<String> selectedPath = factory.getSelectedPath();
        factory.getStore().removeAll();
        factory.setSelectedPath(selectedPath);
        factory.getLoader().load();
    }

    protected abstract boolean acceptNode(GWTJahiaNode node);

    public List<String> getFolderTypes() {
        return folderTypes;
    }

    public void setFolderTypes(List<String> folderTypes) {
        this.folderTypes = folderTypes;
    }

    public List<String> getExcludedFolderTypes() {
        return excludedFolderTypes;
    }

    public void setExcludedFolderTypes(List<String> excludedFolderTypes) {
        this.excludedFolderTypes = excludedFolderTypes;
    }

    public List<String> getPaths() {
        return paths;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }
}
