package org.jahia.ajax.gwt.client.data.toolbar;

import org.jahia.ajax.gwt.client.messages.Messages;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Apr 6, 2010
 * Time: 7:27:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class GWTSidePanelTab  implements Serializable {
    private String name;
    private String tooltip;
    private GWTJahiaToolbar treeContextMenu;
    private GWTJahiaToolbar tableContextMenu;
    private Map<String, String> params;
    private List<String> paths = new ArrayList<String>();
    private List<String> folderTypes = new ArrayList<String>();
    private List<String> nodeTypes = new ArrayList<String>();
    private List<String> filters = new ArrayList<String>();
    private List<String> mimeTypes = new ArrayList<String>();

    private List<GWTColumn> treeColumns;
    private List<String> treeColumnKeys;

    private List<GWTColumn> tableColumns;
    private List<String> tableColumnKeys;
    private String icon;

    public GWTSidePanelTab() {
        treeColumns = new ArrayList<GWTColumn>();
        treeColumnKeys = new ArrayList<String>();
        tableColumns = new ArrayList<GWTColumn>();
        tableColumnKeys = new ArrayList<String>();
    }


    public GWTSidePanelTab(String name) {
        this();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTooltip() {
        return tooltip;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    public GWTJahiaToolbar getTreeContextMenu() {
        return treeContextMenu;
    }

    public void setTreeContextMenu(GWTJahiaToolbar treeContextMenu) {
        this.treeContextMenu = treeContextMenu;
    }

    public GWTJahiaToolbar getTableContextMenu() {
        return tableContextMenu;
    }

    public void setTableContextMenu(GWTJahiaToolbar tableContextMenu) {
        this.tableContextMenu = tableContextMenu;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public List<String> getPaths() {
        return paths;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }

    public List<String> getFolderTypes() {
        return folderTypes;
    }

    public void setFolderTypes(List<String> folderTypes) {
        this.folderTypes = folderTypes;
    }

    public List<String> getNodeTypes() {
        return nodeTypes;
    }

    public void setNodeTypes(List<String> nodeTypes) {
        this.nodeTypes = nodeTypes;
    }

    public List<String> getFilters() {
        return filters;
    }

    public void setFilters(List<String> filters) {
        this.filters = filters;
    }

    public List<String> getMimeTypes() {
        return mimeTypes;
    }

    public void setMimeTypes(List<String> mimeTypes) {
        this.mimeTypes = mimeTypes;
    }

    public void addTableColumn(GWTColumn col) {
        tableColumns.add(col);
        tableColumnKeys.add(col.getKey());
    }

    public void addTreeColumn(GWTColumn col) {
        treeColumns.add(col);
        treeColumnKeys.add(col.getKey());
    }

    public List<GWTColumn> getTableColumns() {
        if (tableColumns.isEmpty()) {
            addTableColumn(new GWTColumn("name", Messages.getResource("label.name"),-1));
        }
        return tableColumns;
    }

    public List<String> getTableColumnKeys() {
        if (tableColumns.isEmpty()) {
            addTableColumn(new GWTColumn("name", Messages.getResource("label.name"),-1));
        }
        return tableColumnKeys;
    }

    public List<GWTColumn> getTreeColumns() {
        if (treeColumns.isEmpty()) {
            addTreeColumn(new GWTColumn("name", Messages.getResource("label.name"),-1));
        }
        return treeColumns;
    }

    public List<String> getTreeColumnKeys() {
        if (treeColumns.isEmpty()) {
            addTreeColumn(new GWTColumn("name", Messages.getResource("label.name"),-1));
        }
        return treeColumnKeys;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
