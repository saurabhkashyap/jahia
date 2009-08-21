package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.util.Rectangle;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.dnd.DragSource;
import com.extjs.gxt.ui.client.dnd.DND;
import com.extjs.gxt.ui.client.dnd.DropTarget;
import com.extjs.gxt.ui.client.dnd.Insert;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.DOM;
import com.allen_sauer.gwt.log.client.Log;

import java.util.Map;

import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 19, 2009
 * Time: 12:25:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class SimpleModule extends ContentPanel implements Module {

    private GWTJahiaNode node;
    private Element element;
    private HTML html;
    private String path;

    private EditManager editManager;

    public SimpleModule(final String path, String s, final EditManager editManager) {
//        super(new FitLayout());
        setHeaderVisible(false);
        setScrollMode(Style.Scroll.AUTO);
        setBorders(false);

        this.path = path;
        this.editManager = editManager;

        html = new HTML(s);
        add(html);
    }

    public void parse() {
        Map<Element, Module> m = ModuleHelper.parse(this);
        boolean last = m.isEmpty();

        if (last) {

            DragSource source = new SimpleModuleDragSource(this);
            source.addDNDListener(editManager.getDndListener());

            DropTarget target = new SimpleModuleDropTarget(this);
            target.setOperation(DND.Operation.COPY);
            target.setFeedback(DND.Feedback.INSERT);

            target.addDNDListener(editManager.getDndListener());
            sinkEvents(Event.ONCLICK + Event.ONDBLCLICK);
            Listener<ComponentEvent> listener = new Listener<ComponentEvent>() {
                public void handleEvent(ComponentEvent ce) {
                    Log.info("click" + path);
                    editManager.setSelection(SimpleModule.this);
                }
            };
            addListener(Events.OnClick, listener);
            addListener(Events.OnDoubleClick, new Listener<ComponentEvent>() {
                public void handleEvent(ComponentEvent ce) {
                    new EditContentEngine(path).show();
                }
            });
        }
    }

    public HTML getHtml() {
        return html;
    }

    public LayoutContainer getContainer() {
        return this;
    }

    public String getPath() {
        return path;
    }

    public GWTJahiaNode getNode() {
        return node;
    }

    public void setNode(GWTJahiaNode node) {
        this.node = node;
    }

    public class SimpleModuleDragSource extends EditModeDragSource {
        private final SimpleModule simpleModule;

        public SimpleModuleDragSource(SimpleModule simpleModule) {
            super(simpleModule);
            this.simpleModule = simpleModule;
        }

        public SimpleModule getSimpleModule() {
            return simpleModule;
        }

        protected void onDragEnd(DNDEvent e) {
            if (e.getStatus().getData("operationCalled") == null) {
                DOM.setStyleAttribute(html.getElement(), "display", "block");
            }
            super.onDragEnd(e);
        }

        @Override
        protected void onDragStart(DNDEvent e) {
            e.setCancelled(false);
            e.setData(this);
            e.setOperation(DND.Operation.MOVE);
            if (getStatusText() == null) {
                e.getStatus().update(DOM.clone(html.getElement(),true));

                e.getStatus().setData("element",html.getElement());
                DOM.setStyleAttribute(html.getElement(), "display", "none");

            }

        }

    }

    public class SimpleModuleDropTarget extends DropTarget {
        private final SimpleModule simpleModule;

        public SimpleModuleDropTarget(SimpleModule simpleModule) {
            super(simpleModule);
            this.simpleModule = simpleModule;
        }

        public SimpleModule getSimpleModule() {
            return simpleModule;
        }

        @Override
        protected void onDragMove(DNDEvent event) {
            event.setCancelled(false);
        }

        @Override
        protected void showFeedback(DNDEvent event) {
            showInsert(event, this.getComponent().getElement(), true);
        }

        private void showInsert(DNDEvent event, Element row, boolean before) {
//            Element toDrag = event.getStatus().getData("element");
//            if (toDrag != null) {
//                Element parent = DOM.getParent(row);
//                parent.insertBefore(toDrag, row);
//            }
            Insert insert = Insert.get();
            insert.setVisible(true);
            Rectangle rect = El.fly(row).getBounds();
            int y = !before ? (rect.y + rect.height - 4) : rect.y - 2;
            insert.el().setBounds(rect.x, y, rect.width, 20);
        }

    }
}
