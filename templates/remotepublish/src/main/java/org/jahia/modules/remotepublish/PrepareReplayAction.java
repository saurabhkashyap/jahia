package org.jahia.modules.remotepublish;

import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Apr 22, 2010
 * Time: 3:05:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class PrepareReplayAction implements Action {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource,
                                  Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        final JCRNodeWrapper node = resource.getNode();

        Map map = new HashMap();
        if (node.isNodeType("jmix:remotelyPublished")) {
            String uuid = node.getProperty("uuid").getString();
            if (uuid.equals(parameters.get("sourceUuid").get(0))) {
                map.put("ready", Boolean.TRUE);
                if (node.hasProperty("lastReplay")) {
                    Calendar last = node.getProperty("lastReplay").getDate();
                    map.put("lastDate", last);
                }
            }
        } else {
            node.addMixin("jmix:remotelyPublished");
            node.setProperty("uuid", parameters.get("sourceUuid").get(0));
            node.getSession().save();
            map.put("ready", Boolean.TRUE);
        }
        return new ActionResult(200, null, new JSONObject(map));
    }
}
