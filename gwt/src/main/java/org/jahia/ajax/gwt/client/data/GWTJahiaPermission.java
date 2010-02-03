package org.jahia.ajax.gwt.client.data;

import com.extjs.gxt.ui.client.data.BaseModel;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Feb 2, 2010
 * Time: 11:43:21 AM
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaPermission extends BaseModel {
    private String id;

    public GWTJahiaPermission() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return get("label");
    }

    public void setLabel(String label) {
        set("label",label);
    }

    public String getGroup() {
        return get("group");
    }

    public void setGroup(String g) {
        set("group",g);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GWTJahiaPermission that = (GWTJahiaPermission) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }


}
